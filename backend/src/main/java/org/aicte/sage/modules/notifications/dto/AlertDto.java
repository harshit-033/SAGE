package org.aicte.sage.modules.notifications.dto;

import org.aicte.sage.modules.notifications.entity.Alert;
import org.aicte.sage.modules.notifications.entity.AlertCategory;
import org.aicte.sage.modules.notifications.entity.AlertPriority;
import org.aicte.sage.modules.notifications.entity.AlertSeverity;
import org.aicte.sage.modules.notifications.entity.AlertStatus;

import java.time.Instant;
import java.util.UUID;

public record AlertDto(
        UUID id,
        String alertCode,
        String title,
        String message,
        AlertCategory category,
        AlertSeverity severity,
        AlertPriority priority,
        String sourceEntityType,
        UUID sourceEntityId,
        UUID institutionId,
        String institutionName,
        UUID recipientUserId,
        String targetRole,
        String jurisdictionState,
        AlertStatus status,
        Instant acknowledgedAt,
        String acknowledgedByName,
        String acknowledgementNotes,
        Instant resolvedAt,
        String resolvedByName,
        String resolutionNotes,
        boolean isEscalated,
        String escalatedToRole,
        Instant escalatedAt,
        String deduplicationHash,
        Instant createdAt,
        Instant updatedAt
) {
    public static AlertDto fromEntity(Alert a) {
        return new AlertDto(
                a.getId(),
                a.getAlertCode(),
                a.getTitle(),
                a.getMessage(),
                a.getCategory(),
                a.getSeverity(),
                a.getPriority(),
                a.getSourceEntityType(),
                a.getSourceEntityId(),
                a.getInstitution() != null ? a.getInstitution().getId() : null,
                a.getInstitution() != null ? a.getInstitution().getName() : null,
                a.getRecipientUser() != null ? a.getRecipientUser().getId() : null,
                a.getTargetRole(),
                a.getJurisdictionState(),
                a.getStatus(),
                a.getAcknowledgedAt(),
                a.getAcknowledgedByUser() != null ? a.getAcknowledgedByUser().getFullName() : null,
                a.getAcknowledgementNotes(),
                a.getResolvedAt(),
                a.getResolvedByUser() != null ? a.getResolvedByUser().getFullName() : null,
                a.getResolutionNotes(),
                a.isEscalated(),
                a.getEscalatedToRole(),
                a.getEscalatedAt(),
                a.getDeduplicationHash(),
                a.getCreatedAt(),
                a.getUpdatedAt()
        );
    }
}
