package org.aicte.sage.modules.workflow.service;

import org.aicte.sage.common.audit.AuditService;
import org.aicte.sage.common.exception.ResourceNotFoundException;
import org.aicte.sage.common.security.AuthoritativeAuthorizationService;
import org.aicte.sage.common.security.UserPrincipal;
import org.aicte.sage.modules.applications.model.Application;
import org.aicte.sage.modules.applications.model.ApplicationState;
import org.aicte.sage.modules.applications.repository.ApplicationRepository;
import org.aicte.sage.modules.users.entity.User;
import org.aicte.sage.modules.users.repository.UserRepository;
import org.aicte.sage.modules.workflow.dto.TransitionRequest;
import org.aicte.sage.modules.workflow.dto.WorkflowTransitionDto;
import org.aicte.sage.modules.workflow.model.WorkflowTransition;
import org.aicte.sage.modules.workflow.repository.WorkflowTransitionRepository;
import org.aicte.sage.modules.notifications.entity.AlertCategory;
import org.aicte.sage.modules.notifications.entity.AlertPriority;
import org.aicte.sage.modules.notifications.entity.AlertSeverity;
import org.aicte.sage.modules.notifications.service.AlertService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
@Transactional
public class WorkflowEngineService {

    private static final Logger log = LoggerFactory.getLogger(WorkflowEngineService.class);

    private final ApplicationRepository applicationRepository;
    private final WorkflowTransitionRepository transitionRepository;
    private final UserRepository userRepository;
    private final AuthoritativeAuthorizationService authService;
    private final AuditService auditService;
    private final AlertService alertService;

    public WorkflowEngineService(
            ApplicationRepository applicationRepository,
            WorkflowTransitionRepository transitionRepository,
            UserRepository userRepository,
            AuthoritativeAuthorizationService authService,
            AuditService auditService,
            AlertService alertService
    ) {
        this.applicationRepository = applicationRepository;
        this.transitionRepository = transitionRepository;
        this.userRepository = userRepository;
        this.authService = authService;
        this.auditService = auditService;
        this.alertService = alertService;
    }

    /**
     * Executes a validated state transition with authoritative authorization and audit recording.
     */
    public WorkflowTransitionDto transitionState(UUID applicationId, TransitionRequest request, UserPrincipal principal) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application with ID '" + applicationId + "' not found"));

        // 1. Authoritative tenancy and access check
        authService.checkAccessToInstitution(principal, application.getInstitution().getId(), "WORKFLOW_TRANSITION");

        ApplicationState fromState = application.getCurrentState();
        ApplicationState toState = request.getTargetState();

        if (fromState == toState) {
            throw new IllegalArgumentException("Application is already in state: " + fromState);
        }

        // 2. Validate transition permissions and role authority
        validateTransitionAuthority(principal, application, fromState, toState);

        // 3. Update application state
        application.setCurrentState(toState);
        if (toState == ApplicationState.SUBMITTED && application.getSubmittedAt() == null) {
            application.setSubmittedAt(Instant.now());
        }
        if (toState == ApplicationState.APPROVED || toState == ApplicationState.REJECTED || toState == ApplicationState.WITHDRAWN) {
            application.setFinalDecisionAt(Instant.now());
        }
        applicationRepository.save(application);

        // 4. Record transition history
        User actor = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User '" + principal.getId() + "' not found"));

        String actorRole = principal.getAuthorities().stream()
                .map(Object::toString)
                .filter(a -> a.startsWith("ROLE_"))
                .findFirst()
                .orElse("ROLE_USER");

        WorkflowTransition transition = new WorkflowTransition();
        transition.setApplication(application);
        transition.setFromState(fromState);
        transition.setToState(toState);
        transition.setActor(actor);
        transition.setActorRole(actorRole);
        transition.setReason(request.getReason() != null ? request.getReason() : "State transition executed");
        transition.setEvidenceSummary(request.getEvidenceSummary());
        WorkflowTransition savedTransition = transitionRepository.save(transition);

        // 5. Mandatory regulatory audit outbox
        auditService.recordEvent(
                "WORKFLOW_STATE_TRANSITION",
                principal.getUsername(),
                application.getInstitution().getId(),
                "APPLICATION",
                application.getId().toString(),
                "STATE_TRANSITION",
                null,
                "State transition from " + fromState + " to " + toState + " executed by " + principal.getUsername() + ". Reason: " + transition.getReason()
        );

        // 6. Workflow Lifecycle Notifications
        try {
            emitWorkflowAlert(application, fromState, toState);
        } catch (Exception e) {
            log.warn("Failed to dispatch workflow alert for application {}: {}", application.getId(), e.getMessage());
        }

        log.info("Application {} transitioned from {} to {} by {}",
                application.getApplicationNumber(), fromState, toState, principal.getUsername());

        return WorkflowTransitionDto.fromEntity(savedTransition);
    }

    private void emitWorkflowAlert(Application app, ApplicationState from, ApplicationState to) {
        UUID instId = app.getInstitution().getId();
        String instName = app.getInstitution().getName();
        String appNum = app.getApplicationNumber();

        switch (to) {
            case SUBMITTED -> alertService.createAlert(
                    "ALERT_APP_SUBMITTED_" + app.getId(),
                    "New Application Submitted: " + appNum,
                    "Application " + appNum + " for " + instName + " (" + app.getCategory() + ") was submitted and is pending scrutiny review.",
                    AlertCategory.WORKFLOW,
                    AlertSeverity.INFO,
                    AlertPriority.MEDIUM,
                    "APPLICATION",
                    app.getId(),
                    instId,
                    null,
                    "ROLE_REGIONAL_OFFICER",
                    null
            );
            case RESUBMITTED -> alertService.createAlert(
                    "ALERT_APP_RESUBMITTED_" + app.getId(),
                    "Application Resubmitted After Rectification: " + appNum,
                    "Application " + appNum + " for " + instName + " has been resubmitted after deficiency rectification.",
                    AlertCategory.WORKFLOW,
                    AlertSeverity.INFO,
                    AlertPriority.MEDIUM,
                    "APPLICATION",
                    app.getId(),
                    instId,
                    null,
                    "ROLE_REGIONAL_OFFICER",
                    null
            );
            case DEFICIENT -> alertService.createAlert(
                    "ALERT_APP_DEFICIENT_" + app.getId(),
                    "Deficiencies Flagged: " + appNum,
                    "Statutory scrutiny or evaluation identified deficiencies for application " + appNum + ". Institutional rectification required.",
                    AlertCategory.WORKFLOW,
                    AlertSeverity.WARNING,
                    AlertPriority.HIGH,
                    "APPLICATION",
                    app.getId(),
                    instId,
                    null,
                    "ROLE_INSTITUTION_OFFICER",
                    null
            );
            case SCRUTINY_PASSED -> alertService.createAlert(
                    "ALERT_SCRUTINY_PASSED_" + app.getId(),
                    "Scrutiny Cleared: " + appNum,
                    "Application " + appNum + " for " + instName + " successfully cleared statutory document scrutiny.",
                    AlertCategory.WORKFLOW,
                    AlertSeverity.INFO,
                    AlertPriority.MEDIUM,
                    "APPLICATION",
                    app.getId(),
                    instId,
                    null,
                    "ROLE_REGIONAL_OFFICER",
                    null
            );
            case APPROVED -> alertService.createAlert(
                    "ALERT_APP_APPROVED_" + app.getId(),
                    "AICTE Approval Granted: " + appNum,
                    "Congratulations! Application " + appNum + " for " + instName + " has been officially APPROVED by AICTE.",
                    AlertCategory.WORKFLOW,
                    AlertSeverity.INFO,
                    AlertPriority.URGENT,
                    "APPLICATION",
                    app.getId(),
                    instId,
                    null,
                    "ROLE_INSTITUTION_OFFICER",
                    null
            );
            case REJECTED -> alertService.createAlert(
                    "ALERT_APP_REJECTED_" + app.getId(),
                    "Approval Decision Notice: " + appNum,
                    "Statutory rejection order issued for application " + appNum + " (" + instName + ").",
                    AlertCategory.WORKFLOW,
                    AlertSeverity.CRITICAL,
                    AlertPriority.URGENT,
                    "APPLICATION",
                    app.getId(),
                    instId,
                    null,
                    "ROLE_INSTITUTION_OFFICER",
                    null
            );
            default -> {}
        }
    }

    private void validateTransitionAuthority(
            UserPrincipal principal,
            Application application,
            ApplicationState from,
            ApplicationState to
    ) {
        boolean isAdmin = principal.hasRole("ROLE_AICTE_ADMIN");
        boolean isRegionalOfficer = principal.hasRole("ROLE_REGIONAL_OFFICER");
        boolean isCommitteeMember = principal.hasRole("ROLE_COMMITTEE_MEMBER");
        boolean isEvcCoordinator = principal.hasRole("ROLE_EVC_COORDINATOR");
        boolean isInstOfficer = principal.hasRole("ROLE_INSTITUTION_OFFICER");

        // 1. Institution Officer actions (Self-Service)
        if (isInstOfficer && !isAdmin && !isRegionalOfficer) {
            if (from == ApplicationState.DRAFT && to == ApplicationState.SUBMITTED) {
                return; // Valid submission
            }
            if (from == ApplicationState.DEFICIENT && to == ApplicationState.RESUBMITTED) {
                return; // Valid resubmission after correction
            }
            if ((from == ApplicationState.DRAFT || from == ApplicationState.SUBMITTED || from == ApplicationState.DEFICIENT) && to == ApplicationState.WITHDRAWN) {
                return; // Valid withdrawal
            }
            throw new AccessDeniedException("Institution officers cannot transition application from " + from + " to " + to);
        }

        // 2. Regulatory Officer & Committee transitions
        switch (to) {
            case UNDER_SCRUTINY:
                if (from == ApplicationState.SUBMITTED || from == ApplicationState.RESUBMITTED) {
                    if (isAdmin || isRegionalOfficer || isCommitteeMember) return;
                }
                break;

            case DEFICIENT:
            case SCRUTINY_PASSED:
                if (from == ApplicationState.UNDER_SCRUTINY) {
                    if (isAdmin || isRegionalOfficer || isCommitteeMember) return;
                }
                break;

            case EVC_SCHEDULED:
                if (from == ApplicationState.SCRUTINY_PASSED) {
                    if (!application.getWorkflowDefinition().isRequiresEvc() && !isAdmin) {
                        throw new IllegalArgumentException("Workflow category '" + application.getCategory() + "' does not require EVC inspection per APH norms");
                    }
                    if (isAdmin || isRegionalOfficer || isEvcCoordinator) return;
                }
                break;

            case EVC_CONDUCTED:
                if (from == ApplicationState.EVC_SCHEDULED) {
                    // Must have explicit EVC appointment or be EVC coordinator or Admin
                    boolean hasEvcAppointment = authService.hasStatutoryCommitteeAppointment(principal.getId(), "EVC");
                    if (isAdmin || isEvcCoordinator || hasEvcAppointment) return;
                    throw new AccessDeniedException("User " + principal.getUsername() + " does not hold a statutory EVC committee appointment");
                }
                break;

            case SHC_SCHEDULED:
            case SHC_REVIEW:
                // SHC is conditional upon deficiencies or adverse EVC recommendations per APH Chapter I/II
                if (from == ApplicationState.DEFICIENT || from == ApplicationState.EVC_CONDUCTED || from == ApplicationState.UNDER_SCRUTINY) {
                    if (to == ApplicationState.SHC_SCHEDULED) {
                        if (isAdmin || isRegionalOfficer) return;
                    }
                    if (to == ApplicationState.SHC_REVIEW) {
                        boolean hasShcAppointment = authService.hasStatutoryCommitteeAppointment(principal.getId(), "SHC")
                                || authService.hasStatutoryCommitteeAppointment(principal.getId(), "SHC_SAC");
                        if (isAdmin || hasShcAppointment) return;
                        throw new AccessDeniedException("User " + principal.getUsername() + " does not hold a statutory Standing Hearing Committee (SHC) appointment");
                    }
                }
                break;

            case SAC_REVIEW:
                // SAC is a conditional appellate gate following SHC or formal rejection appeal
                if (from == ApplicationState.SHC_REVIEW || from == ApplicationState.DEFICIENT) {
                    boolean hasSacAppointment = authService.hasStatutoryCommitteeAppointment(principal.getId(), "SAC")
                            || authService.hasStatutoryCommitteeAppointment(principal.getId(), "SHC_SAC");
                    if (isAdmin || hasSacAppointment) return;
                    throw new AccessDeniedException("User " + principal.getUsername() + " does not hold a statutory Standing Appellate Committee (SAC) appointment");
                }
                break;

            case APPROVED:
                if (from == ApplicationState.SCRUTINY_PASSED) {
                    if (application.getWorkflowDefinition().isRequiresEvc()) {
                        throw new AccessDeniedException("Application category '" + application.getCategory() + "' requires mandatory EVC inspection before approval per APH Chapter I");
                    }
                    if (application.getWorkflowDefinition().isRequiresShc()) {
                        throw new AccessDeniedException("Application category '" + application.getCategory() + "' requires mandatory committee clearance before approval");
                    }
                    // Fast-track EoA approval rests with statutory Regional Officer / Admin
                    if (isAdmin || isRegionalOfficer) return;
                }
                if (from == ApplicationState.EVC_CONDUCTED || from == ApplicationState.SHC_REVIEW || from == ApplicationState.SAC_REVIEW) {
                    // Final statutory approval decision rests with Regional Officer / Central Admin
                    if (isAdmin || isRegionalOfficer) return;
                }
                break;

            case REJECTED:
                // Statutory rejection rests with Regional Officer / Central Admin following due process
                if (from == ApplicationState.SHC_REVIEW || from == ApplicationState.SAC_REVIEW || from == ApplicationState.DEFICIENT) {
                    if (isAdmin || isRegionalOfficer) return;
                }
                break;

            case WITHDRAWN:
                if (isAdmin || isRegionalOfficer) return;
                break;

            default:
                break;
        }

        throw new AccessDeniedException("Illegal or unauthorized transition from '" + from + "' to '" + to + "' for user " + principal.getUsername());
    }

    @Transactional(readOnly = true)
    public List<WorkflowTransitionDto> getTransitionsForApplication(UUID applicationId, UserPrincipal principal) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application with ID '" + applicationId + "' not found"));

        authService.checkAccessToInstitution(principal, application.getInstitution().getId(), "VIEW_WORKFLOW_HISTORY");

        return transitionRepository.findByApplicationIdOrderByCreatedAtAsc(applicationId).stream()
                .map(WorkflowTransitionDto::fromEntity)
                .toList();
    }
}
