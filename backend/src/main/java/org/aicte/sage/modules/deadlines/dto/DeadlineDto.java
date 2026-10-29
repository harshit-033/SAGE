package org.aicte.sage.modules.deadlines.dto;

import org.aicte.sage.modules.deadlines.entity.Deadline;
import org.aicte.sage.modules.deadlines.entity.DeadlineStatus;
import org.aicte.sage.modules.deadlines.entity.DeadlineType;

import java.time.Instant;
import java.util.UUID;

public record DeadlineDto(
        UUID id,
        String deadlineCode,
        String title,
        String description,
        DeadlineType deadlineType,
        String statutoryClause,
        String entityType,
        UUID entityId,
        UUID institutionId,
        String institutionName,
        Instant targetDate,
        int gracePeriodDays,
        DeadlineStatus status,
        String reminderScheduleJson,
        Instant lastEvaluatedAt,
        Instant resolvedAt,
        String resolvedByName,
        String resolutionNotes,
        Long version,
        Instant createdAt,
        Instant updatedAt
) {
    public static DeadlineDto fromEntity(Deadline d) {
        return new DeadlineDto(
                d.getId(),
                d.getDeadlineCode(),
                d.getTitle(),
                d.getDescription(),
                d.getDeadlineType(),
                d.getStatutoryClause(),
                d.getEntityType(),
                d.getEntityId(),
                d.getInstitution() != null ? d.getInstitution().getId() : null,
                d.getInstitution() != null ? d.getInstitution().getName() : null,
                d.getTargetDate(),
                d.getGracePeriodDays(),
                d.getStatus(),
                d.getReminderScheduleJson(),
                d.getLastEvaluatedAt(),
                d.getResolvedAt(),
                d.getResolvedByUser() != null ? d.getResolvedByUser().getFullName() : null,
                d.getResolutionNotes(),
                d.getVersion(),
                d.getCreatedAt(),
                d.getUpdatedAt()
        );
    }
}
