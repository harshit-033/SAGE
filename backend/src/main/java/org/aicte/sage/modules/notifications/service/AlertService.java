package org.aicte.sage.modules.notifications.service;

import org.aicte.sage.common.audit.AuditService;
import org.aicte.sage.common.exception.ResourceNotFoundException;
import org.aicte.sage.common.security.AuthoritativeAuthorizationService;
import org.aicte.sage.common.security.UserPrincipal;
import org.aicte.sage.modules.governance.repository.JurisdictionAssignmentRepository;
import org.aicte.sage.modules.institutions.entity.Institution;
import org.aicte.sage.modules.institutions.repository.InstitutionRepository;
import org.aicte.sage.modules.notifications.channel.NotificationChannelAdapter;
import org.aicte.sage.modules.notifications.dto.AlertDto;
import org.aicte.sage.modules.notifications.entity.*;
import org.aicte.sage.modules.notifications.repository.AlertDeliveryRepository;
import org.aicte.sage.modules.notifications.repository.AlertRepository;
import org.aicte.sage.modules.users.entity.User;
import org.aicte.sage.modules.users.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@Transactional
public class AlertService {

    private static final Logger log = LoggerFactory.getLogger(AlertService.class);

    private final AlertRepository alertRepository;
    private final AlertDeliveryRepository deliveryRepository;
    private final InstitutionRepository institutionRepository;
    private final UserRepository userRepository;
    private final JurisdictionAssignmentRepository jurisdictionRepository;
    private final AuthoritativeAuthorizationService authService;
    private final AuditService auditService;
    private final List<NotificationChannelAdapter> channelAdapters;

    public AlertService(
            AlertRepository alertRepository,
            AlertDeliveryRepository deliveryRepository,
            InstitutionRepository institutionRepository,
            UserRepository userRepository,
            JurisdictionAssignmentRepository jurisdictionRepository,
            AuthoritativeAuthorizationService authService,
            AuditService auditService,
            List<NotificationChannelAdapter> channelAdapters) {
        this.alertRepository = alertRepository;
        this.deliveryRepository = deliveryRepository;
        this.institutionRepository = institutionRepository;
        this.userRepository = userRepository;
        this.jurisdictionRepository = jurisdictionRepository;
        this.authService = authService;
        this.auditService = auditService;
        this.channelAdapters = channelAdapters;
    }

    /**
     * Create and dispatch a deduplicated alert.
     */
    public Alert createAlert(
            String alertCode,
            String title,
            String message,
            AlertCategory category,
            AlertSeverity severity,
            AlertPriority priority,
            String sourceEntityType,
            UUID sourceEntityId,
            UUID institutionId,
            UUID recipientUserId,
            String targetRole,
            String jurisdictionState) {

        Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("Institution not found with ID: " + institutionId));

        User recipientUser = null;
        if (recipientUserId != null) {
            recipientUser = userRepository.findById(recipientUserId).orElse(null);
        }

        // Deduplication Hash: SHA-256(code + entityType + entityId + role/user)
        String rawHashKey = String.format("%s:%s:%s:%s:%s",
                alertCode,
                sourceEntityType,
                sourceEntityId,
                recipientUserId != null ? recipientUserId.toString() : (targetRole != null ? targetRole : "GLOBAL"),
                institutionId);
        String deduplicationHash = sha256Hex(rawHashKey);

        Optional<Alert> existingOpt = alertRepository.findByDeduplicationHash(deduplicationHash);
        if (existingOpt.isPresent()) {
            Alert existing = existingOpt.get();
            // If already active (unread or acknowledged), do not spawn duplicate alert
            if (existing.getStatus() == AlertStatus.UNREAD || existing.getStatus() == AlertStatus.ACKNOWLEDGED) {
                log.info("Duplicate alert suppressed for hash key: {}", deduplicationHash);
                return existing;
            }
        }

        Alert alert = new Alert(
                alertCode,
                title,
                message,
                category,
                severity,
                priority,
                sourceEntityType,
                sourceEntityId,
                institution,
                recipientUser,
                targetRole,
                jurisdictionState != null ? jurisdictionState : institution.getState(),
                deduplicationHash
        );

        Alert savedAlert = alertRepository.save(alert);

        // Resolve delivery channels & create delivery records
        createDeliveriesForAlert(savedAlert, recipientUser);

        auditService.recordEvent(
                "ALERT_GENERATED",
                "SYSTEM",
                institutionId,
                "ALERT",
                savedAlert.getId().toString(),
                "ALERT_DISPATCH",
                null,
                "Alert generated: " + title + " (" + alertCode + ") Severity: " + severity
        );

        return savedAlert;
    }

    private void createDeliveriesForAlert(Alert alert, User recipientUser) {
        String inAppTarget = recipientUser != null ? recipientUser.getUsername() : (alert.getTargetRole() != null ? alert.getTargetRole() : "BROADCAST");
        String emailTarget = recipientUser != null ? recipientUser.getEmail() : institutionRepository.findById(alert.getInstitution().getId()).map(Institution::getOfficialEmail).orElse("notifications@aicte.gov.in");

        // 1. IN_APP Delivery
        AlertDelivery inAppDelivery = new AlertDelivery(alert, DeliveryChannel.IN_APP, inAppTarget, DeliveryStatus.PENDING);
        deliveryRepository.save(inAppDelivery);
        dispatchDelivery(inAppDelivery, alert);

        // 2. EMAIL Delivery
        if (emailTarget != null && !emailTarget.isBlank()) {
            AlertDelivery emailDelivery = new AlertDelivery(alert, DeliveryChannel.EMAIL, emailTarget, DeliveryStatus.PENDING);
            deliveryRepository.save(emailDelivery);
            dispatchDelivery(emailDelivery, alert);
        }
    }

    private void dispatchDelivery(AlertDelivery delivery, Alert alert) {
        for (NotificationChannelAdapter adapter : channelAdapters) {
            if (adapter.supports(delivery.getChannel())) {
                try {
                    delivery.setAttemptCount(delivery.getAttemptCount() + 1);
                    delivery.setLastAttemptAt(Instant.now());
                    NotificationChannelAdapter.DeliveryResult result = adapter.deliver(alert, delivery.getRecipientAddress());
                    if (result.isSuccess()) {
                        delivery.setDeliveryStatus(DeliveryStatus.SENT);
                        delivery.setErrorMessage(null);
                    } else {
                        scheduleRetryOrMarkFailed(delivery, result.errorMessage());
                    }
                } catch (Exception e) {
                    scheduleRetryOrMarkFailed(delivery, e.getMessage());
                }
                deliveryRepository.save(delivery);
                break;
            }
        }
    }

    private void scheduleRetryOrMarkFailed(AlertDelivery delivery, String errorMessage) {
        delivery.setErrorMessage(errorMessage);
        if (delivery.getAttemptCount() >= 3) {
            delivery.setDeliveryStatus(DeliveryStatus.FAILED);
            delivery.setNextRetryAt(null);
        } else {
            delivery.setDeliveryStatus(DeliveryStatus.RETRYING);
            // Exponential backoff: attempt 1 -> 1 min, attempt 2 -> 5 min
            long backoffMinutes = delivery.getAttemptCount() == 1 ? 1 : 5;
            delivery.setNextRetryAt(Instant.now().plus(backoffMinutes, ChronoUnit.MINUTES));
        }
    }

    /**
     * Retry pending deliveries.
     */
    public int retryFailedDeliveries() {
        List<AlertDelivery> pending = deliveryRepository.findPendingRetries(DeliveryStatus.RETRYING, Instant.now());
        int retried = 0;
        for (AlertDelivery d : pending) {
            dispatchDelivery(d, d.getAlert());
            retried++;
        }
        return retried;
    }

    /**
     * Acknowledge an active alert.
     */
    public AlertDto acknowledgeAlert(UUID alertId, String notes, UserPrincipal principal) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found with ID: " + alertId));

        authService.checkAccessToInstitution(principal, alert.getInstitution().getId(), "ACKNOWLEDGE_ALERT");

        User user = userRepository.findById(principal.getId()).orElse(null);
        alert.setStatus(AlertStatus.ACKNOWLEDGED);
        alert.setAcknowledgedAt(Instant.now());
        alert.setAcknowledgedByUser(user);
        alert.setAcknowledgementNotes(notes);

        Alert updated = alertRepository.save(alert);

        auditService.recordEvent(
                "ALERT_ACKNOWLEDGED",
                principal.getUsername(),
                alert.getInstitution().getId(),
                "ALERT",
                alert.getId().toString(),
                "ALERT_ACKNOWLEDGE",
                null,
                "Alert acknowledged with notes: " + notes
        );

        return AlertDto.fromEntity(updated);
    }

    /**
     * Resolve an alert.
     */
    public AlertDto resolveAlert(UUID alertId, String notes, UserPrincipal principal) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found with ID: " + alertId));

        authService.checkAccessToInstitution(principal, alert.getInstitution().getId(), "RESOLVE_ALERT");

        User user = userRepository.findById(principal.getId()).orElse(null);
        alert.setStatus(AlertStatus.RESOLVED);
        alert.setResolvedAt(Instant.now());
        alert.setResolvedByUser(user);
        alert.setResolutionNotes(notes);

        Alert updated = alertRepository.save(alert);

        auditService.recordEvent(
                "ALERT_RESOLVED",
                principal.getUsername(),
                alert.getInstitution().getId(),
                "ALERT",
                alert.getId().toString(),
                "ALERT_RESOLVE",
                null,
                "Alert resolved with notes: " + notes
        );

        return AlertDto.fromEntity(updated);
    }

    /**
     * Escalate an overdue alert to a higher supervisory role.
     */
    public AlertDto escalateAlert(UUID alertId, String escalateToRole, UserPrincipal principal) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found with ID: " + alertId));

        if (!principal.hasRole("ROLE_AICTE_ADMIN") && !principal.hasRole("ROLE_REGIONAL_OFFICER")) {
            throw new AccessDeniedException("Only AICTE Administrators or Regional Officers may execute alert escalations.");
        }

        alert.setEscalated(true);
        alert.setEscalatedToRole(escalateToRole);
        alert.setEscalatedAt(Instant.now());
        alert.setPriority(AlertPriority.URGENT);

        Alert updated = alertRepository.save(alert);

        auditService.recordEvent(
                "ALERT_ESCALATED",
                principal.getUsername(),
                alert.getInstitution().getId(),
                "ALERT",
                alert.getId().toString(),
                "ALERT_ESCALATE",
                null,
                "Alert escalated to role: " + escalateToRole
        );

        return AlertDto.fromEntity(updated);
    }

    /**
     * Scoped alert query matching caller permissions and institutional boundaries.
     */
    @Transactional(readOnly = true)
    public Page<AlertDto> getAlerts(
            UUID institutionId,
            AlertStatus status,
            AlertSeverity severity,
            Pageable pageable,
            UserPrincipal principal) {

        if (principal.hasRole("ROLE_AICTE_ADMIN")) {
            if (institutionId != null) {
                if (status != null) {
                    return alertRepository.findByInstitutionIdAndStatus(institutionId, status, pageable).map(AlertDto::fromEntity);
                }
                return alertRepository.findByInstitutionId(institutionId, pageable).map(AlertDto::fromEntity);
            }
            if (status != null) {
                return alertRepository.findByStatus(status, pageable).map(AlertDto::fromEntity);
            }
            return alertRepository.findAll(pageable).map(AlertDto::fromEntity);
        }

        if (principal.hasRole("ROLE_REGIONAL_OFFICER")) {
            List<String> states = jurisdictionRepository.findActiveStatesByUserId(principal.getId());
            return alertRepository.findScopedAlerts(Collections.emptyList(), states, pageable).map(AlertDto::fromEntity);
        }

        // Institution Representative: strictly isolated to authorized institution
        Set<UUID> myInsts = principal.getAuthorizedInstitutionIds();
        if (myInsts.isEmpty()) {
            return Page.empty(pageable);
        }

        UUID targetInst = (institutionId != null && myInsts.contains(institutionId)) ? institutionId : myInsts.iterator().next();
        if (status != null) {
            return alertRepository.findByInstitutionIdAndStatus(targetInst, status, pageable).map(AlertDto::fromEntity);
        }
        return alertRepository.findByInstitutionId(targetInst, pageable).map(AlertDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public AlertDto getAlertById(UUID alertId, UserPrincipal principal) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found with ID: " + alertId));
        authService.checkAccessToInstitution(principal, alert.getInstitution().getId(), "READ_ALERT");
        return AlertDto.fromEntity(alert);
    }

    private String sha256Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return Integer.toHexString(input.hashCode());
        }
    }
}
