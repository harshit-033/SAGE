package org.aicte.sage.modules.deadlines.service;

import org.aicte.sage.common.audit.AuditService;
import org.aicte.sage.common.exception.ResourceNotFoundException;
import org.aicte.sage.common.security.AuthoritativeAuthorizationService;
import org.aicte.sage.common.security.UserPrincipal;
import org.aicte.sage.modules.deadlines.dto.CreateDeadlineRequest;
import org.aicte.sage.modules.deadlines.dto.DeadlineDto;
import org.aicte.sage.modules.deadlines.dto.DeadlineHistoryDto;
import org.aicte.sage.modules.deadlines.entity.Deadline;
import org.aicte.sage.modules.deadlines.entity.DeadlineHistory;
import org.aicte.sage.modules.deadlines.entity.DeadlineStatus;
import org.aicte.sage.modules.deadlines.repository.DeadlineHistoryRepository;
import org.aicte.sage.modules.deadlines.repository.DeadlineRepository;
import org.aicte.sage.modules.governance.repository.JurisdictionAssignmentRepository;
import org.aicte.sage.modules.institutions.entity.Institution;
import org.aicte.sage.modules.institutions.repository.InstitutionRepository;
import org.aicte.sage.modules.notifications.entity.AlertCategory;
import org.aicte.sage.modules.notifications.entity.AlertPriority;
import org.aicte.sage.modules.notifications.entity.AlertSeverity;
import org.aicte.sage.modules.notifications.service.AlertService;
import org.aicte.sage.modules.users.entity.User;
import org.aicte.sage.modules.users.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@Transactional
public class DeadlineService {

    private static final Logger log = LoggerFactory.getLogger(DeadlineService.class);

    private final DeadlineRepository deadlineRepository;
    private final DeadlineHistoryRepository historyRepository;
    private final InstitutionRepository institutionRepository;
    private final UserRepository userRepository;
    private final JurisdictionAssignmentRepository jurisdictionRepository;
    private final AlertService alertService;
    private final AuthoritativeAuthorizationService authService;
    private final AuditService auditService;

    public DeadlineService(
            DeadlineRepository deadlineRepository,
            DeadlineHistoryRepository historyRepository,
            InstitutionRepository institutionRepository,
            UserRepository userRepository,
            JurisdictionAssignmentRepository jurisdictionRepository,
            AlertService alertService,
            AuthoritativeAuthorizationService authService,
            AuditService auditService) {
        this.deadlineRepository = deadlineRepository;
        this.historyRepository = historyRepository;
        this.institutionRepository = institutionRepository;
        this.userRepository = userRepository;
        this.jurisdictionRepository = jurisdictionRepository;
        this.alertService = alertService;
        this.authService = authService;
        this.auditService = auditService;
    }

    /**
     * Create a deadline with idempotency checks.
     */
    public DeadlineDto createDeadline(CreateDeadlineRequest req, UserPrincipal principal) {
        authService.checkAccessToInstitution(principal, req.institutionId(), "CREATE_DEADLINE");

        Institution institution = institutionRepository.findById(req.institutionId())
                .orElseThrow(() -> new ResourceNotFoundException("Institution not found with ID: " + req.institutionId()));

        Optional<Deadline> existing = deadlineRepository.findByEntityTypeAndEntityIdAndDeadlineCode(
                req.entityType(), req.entityId(), req.deadlineCode());

        if (existing.isPresent()) {
            Deadline d = existing.get();
            if (d.getStatus() != DeadlineStatus.RESOLVED && d.getStatus() != DeadlineStatus.CANCELLED) {
                log.info("Active deadline already exists for entity {} and code {}, returning existing", req.entityId(), req.deadlineCode());
                return DeadlineDto.fromEntity(d);
            }
        }

        Deadline deadline = new Deadline(
                req.deadlineCode(),
                req.title(),
                req.description(),
                req.deadlineType(),
                req.statutoryClause(),
                req.entityType(),
                req.entityId(),
                institution,
                req.targetDate(),
                req.gracePeriodDays() != null ? req.gracePeriodDays() : 0,
                req.reminderScheduleJson()
        );

        Deadline saved = deadlineRepository.save(deadline);

        auditService.recordEvent(
                "DEADLINE_CREATED",
                principal.getUsername(),
                institution.getId(),
                "DEADLINE",
                saved.getId().toString(),
                "CREATE_DEADLINE",
                null,
                "Deadline created: " + req.title() + " Target: " + req.targetDate()
        );

        return DeadlineDto.fromEntity(saved);
    }

    /**
     * Recalculate or extend an existing deadline with mandatory audit history.
     */
    public DeadlineDto recalculateDeadline(UUID deadlineId, Instant newTargetDate, String reason, UserPrincipal principal) {
        Deadline deadline = deadlineRepository.findById(deadlineId)
                .orElseThrow(() -> new ResourceNotFoundException("Deadline not found with ID: " + deadlineId));

        authService.checkAccessToInstitution(principal, deadline.getInstitution().getId(), "RECALCULATE_DEADLINE");

        User user = userRepository.findById(principal.getId()).orElse(null);

        // Record history entry
        DeadlineHistory history = new DeadlineHistory(
                deadline,
                deadline.getTargetDate(),
                newTargetDate,
                reason,
                user
        );
        historyRepository.save(history);

        deadline.setTargetDate(newTargetDate);
        if (deadline.getStatus() == DeadlineStatus.OVERDUE && newTargetDate.isAfter(Instant.now())) {
            deadline.setStatus(DeadlineStatus.PENDING);
        }

        Deadline updated = deadlineRepository.save(deadline);

        auditService.recordEvent(
                "DEADLINE_RECALCULATED",
                principal.getUsername(),
                deadline.getInstitution().getId(),
                "DEADLINE",
                deadline.getId().toString(),
                "RECALCULATE_DEADLINE",
                null,
                "Deadline target date shifted to " + newTargetDate + " Reason: " + reason
        );

        return DeadlineDto.fromEntity(updated);
    }

    /**
     * Resolve a deadline when required actions are completed.
     */
    public DeadlineDto resolveDeadline(UUID deadlineId, String resolutionNotes, UserPrincipal principal) {
        Deadline deadline = deadlineRepository.findById(deadlineId)
                .orElseThrow(() -> new ResourceNotFoundException("Deadline not found with ID: " + deadlineId));

        authService.checkAccessToInstitution(principal, deadline.getInstitution().getId(), "RESOLVE_DEADLINE");

        User user = userRepository.findById(principal.getId()).orElse(null);

        deadline.setStatus(DeadlineStatus.RESOLVED);
        deadline.setResolvedAt(Instant.now());
        deadline.setResolvedByUser(user);
        deadline.setResolutionNotes(resolutionNotes);

        Deadline updated = deadlineRepository.save(deadline);

        auditService.recordEvent(
                "DEADLINE_RESOLVED",
                principal.getUsername(),
                deadline.getInstitution().getId(),
                "DEADLINE",
                deadline.getId().toString(),
                "RESOLVE_DEADLINE",
                null,
                "Deadline resolved. Notes: " + resolutionNotes
        );

        return DeadlineDto.fromEntity(updated);
    }

    /**
     * Evaluates all active deadlines, transitions statuses, and dispatches alerts.
     */
    public int evaluateDeadlines() {
        List<Deadline> activeDeadlines = deadlineRepository.findByStatusIn(
                List.of(DeadlineStatus.PENDING, DeadlineStatus.DUE_SOON, DeadlineStatus.DUE_TODAY)
        );

        Instant now = Instant.now();
        int updatedCount = 0;

        for (Deadline d : activeDeadlines) {
            d.setLastEvaluatedAt(now);
            Instant target = d.getTargetDate();

            if (target.isBefore(now)) {
                if (d.getStatus() != DeadlineStatus.OVERDUE) {
                    d.setStatus(DeadlineStatus.OVERDUE);
                    updatedCount++;

                    // Trigger OVERDUE Alert
                    alertService.createAlert(
                            "ALERT_DEADLINE_OVERDUE",
                            "OVERDUE: " + d.getTitle(),
                            "Statutory deadline expired on " + target + ". Immediate regulatory action required.",
                            AlertCategory.DEADLINE,
                            AlertSeverity.CRITICAL,
                            AlertPriority.URGENT,
                            "DEADLINE",
                            d.getId(),
                            d.getInstitution().getId(),
                            null,
                            "ROLE_INSTITUTION_OFFICER",
                            d.getInstitution().getState()
                    );
                }
            } else if (target.isBefore(now.plus(24, ChronoUnit.HOURS))) {
                if (d.getStatus() != DeadlineStatus.DUE_TODAY) {
                    d.setStatus(DeadlineStatus.DUE_TODAY);
                    updatedCount++;

                    // Trigger DUE_TODAY Alert
                    alertService.createAlert(
                            "ALERT_DEADLINE_DUE_TODAY",
                            "DUE TODAY: " + d.getTitle(),
                            "Deadline expires within 24 hours (" + target + ").",
                            AlertCategory.DEADLINE,
                            AlertSeverity.WARNING,
                            AlertPriority.HIGH,
                            "DEADLINE",
                            d.getId(),
                            d.getInstitution().getId(),
                            null,
                            "ROLE_INSTITUTION_OFFICER",
                            d.getInstitution().getState()
                    );
                }
            } else if (target.isBefore(now.plus(7, ChronoUnit.DAYS))) {
                if (d.getStatus() != DeadlineStatus.DUE_SOON) {
                    d.setStatus(DeadlineStatus.DUE_SOON);
                    updatedCount++;

                    // Trigger DUE_SOON Alert
                    alertService.createAlert(
                            "ALERT_DEADLINE_APPROACHING",
                            "Upcoming Deadline: " + d.getTitle(),
                            "Deadline approaching on " + target + ". Please ensure compliance obligations are satisfied.",
                            AlertCategory.DEADLINE,
                            AlertSeverity.INFO,
                            AlertPriority.MEDIUM,
                            "DEADLINE",
                            d.getId(),
                            d.getInstitution().getId(),
                            null,
                            "ROLE_INSTITUTION_OFFICER",
                            d.getInstitution().getState()
                    );
                }
            }
            deadlineRepository.save(d);
        }

        return updatedCount;
    }

    @Transactional(readOnly = true)
    public Page<DeadlineDto> getDeadlines(UUID institutionId, DeadlineStatus status, Pageable pageable, UserPrincipal principal) {
        if (principal.hasRole("ROLE_AICTE_ADMIN")) {
            if (institutionId != null) {
                if (status != null) {
                    return deadlineRepository.findByInstitutionIdAndStatus(institutionId, status, pageable).map(DeadlineDto::fromEntity);
                }
                return deadlineRepository.findByInstitutionId(institutionId, pageable).map(DeadlineDto::fromEntity);
            }
            if (status != null) {
                return deadlineRepository.findByStatus(status, pageable).map(DeadlineDto::fromEntity);
            }
            return deadlineRepository.findAll(pageable).map(DeadlineDto::fromEntity);
        }

        if (principal.hasRole("ROLE_REGIONAL_OFFICER")) {
            // Regional Officer can view deadlines within assigned jurisdiction
            if (institutionId != null) {
                authService.checkAccessToInstitution(principal, institutionId, "READ_DEADLINES");
                if (status != null) {
                    return deadlineRepository.findByInstitutionIdAndStatus(institutionId, status, pageable).map(DeadlineDto::fromEntity);
                }
                return deadlineRepository.findByInstitutionId(institutionId, pageable).map(DeadlineDto::fromEntity);
            }
            // General query: fall back to authorized institutions or all
            return deadlineRepository.findAll(pageable).map(DeadlineDto::fromEntity);
        }

        // Institution Representative: strictly isolated to authorized institution
        Set<UUID> myInsts = principal.getAuthorizedInstitutionIds();
        if (myInsts.isEmpty()) {
            return Page.empty(pageable);
        }

        UUID targetInst = (institutionId != null && myInsts.contains(institutionId)) ? institutionId : myInsts.iterator().next();
        if (status != null) {
            return deadlineRepository.findByInstitutionIdAndStatus(targetInst, status, pageable).map(DeadlineDto::fromEntity);
        }
        return deadlineRepository.findByInstitutionId(targetInst, pageable).map(DeadlineDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public DeadlineDto getDeadlineById(UUID deadlineId, UserPrincipal principal) {
        Deadline deadline = deadlineRepository.findById(deadlineId)
                .orElseThrow(() -> new ResourceNotFoundException("Deadline not found with ID: " + deadlineId));
        authService.checkAccessToInstitution(principal, deadline.getInstitution().getId(), "READ_DEADLINE");
        return DeadlineDto.fromEntity(deadline);
    }

    @Transactional(readOnly = true)
    public List<DeadlineHistoryDto> getDeadlineHistory(UUID deadlineId, UserPrincipal principal) {
        Deadline deadline = deadlineRepository.findById(deadlineId)
                .orElseThrow(() -> new ResourceNotFoundException("Deadline not found with ID: " + deadlineId));
        authService.checkAccessToInstitution(principal, deadline.getInstitution().getId(), "READ_DEADLINE_HISTORY");
        return historyRepository.findByDeadlineIdOrderByCreatedAtDesc(deadlineId).stream()
                .map(DeadlineHistoryDto::fromEntity)
                .toList();
    }
}
