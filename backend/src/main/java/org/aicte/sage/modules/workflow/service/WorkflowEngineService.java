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

    public WorkflowEngineService(
            ApplicationRepository applicationRepository,
            WorkflowTransitionRepository transitionRepository,
            UserRepository userRepository,
            AuthoritativeAuthorizationService authService,
            AuditService auditService
    ) {
        this.applicationRepository = applicationRepository;
        this.transitionRepository = transitionRepository;
        this.userRepository = userRepository;
        this.authService = authService;
        this.auditService = auditService;
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
                fromState + " -> " + toState,
                null,
                "State transition from " + fromState + " to " + toState + " executed by " + principal.getUsername() + ". Reason: " + transition.getReason()
        );

        log.info("Application {} transitioned from {} to {} by {}",
                application.getApplicationNumber(), fromState, toState, principal.getUsername());

        return WorkflowTransitionDto.fromEntity(savedTransition);
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
            if ((from == ApplicationState.DRAFT || from == ApplicationState.SUBMITTED) && to == ApplicationState.WITHDRAWN) {
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
                    if (isAdmin || isRegionalOfficer || isEvcCoordinator || isCommitteeMember) return;
                }
                break;

            case SHC_SCHEDULED:
            case SHC_REVIEW:
                if (from == ApplicationState.DEFICIENT || from == ApplicationState.EVC_CONDUCTED || from == ApplicationState.UNDER_SCRUTINY) {
                    if (isAdmin || isRegionalOfficer || isCommitteeMember) return;
                }
                break;

            case SAC_REVIEW:
                if (from == ApplicationState.SHC_REVIEW || from == ApplicationState.DEFICIENT) {
                    if (isAdmin || isRegionalOfficer || isCommitteeMember) return;
                }
                break;

            case APPROVED:
                if (from == ApplicationState.SCRUTINY_PASSED && !application.getWorkflowDefinition().isRequiresEvc() && !application.getWorkflowDefinition().isRequiresShc()) {
                    // Straightforward EoA fast-track approval
                    if (isAdmin || isRegionalOfficer) return;
                }
                if (from == ApplicationState.EVC_CONDUCTED || from == ApplicationState.SHC_REVIEW || from == ApplicationState.SAC_REVIEW || from == ApplicationState.SCRUTINY_PASSED) {
                    if (isAdmin || isRegionalOfficer || isCommitteeMember) return;
                }
                break;

            case REJECTED:
                if (from == ApplicationState.SHC_REVIEW || from == ApplicationState.SAC_REVIEW || from == ApplicationState.DEFICIENT) {
                    if (isAdmin || isRegionalOfficer || isCommitteeMember) return;
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
