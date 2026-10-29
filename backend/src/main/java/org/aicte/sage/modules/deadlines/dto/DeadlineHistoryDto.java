package org.aicte.sage.modules.deadlines.dto;

import org.aicte.sage.modules.deadlines.entity.DeadlineHistory;

import java.time.Instant;
import java.util.UUID;

public record DeadlineHistoryDto(
        UUID id,
        UUID deadlineId,
        Instant previousTargetDate,
        Instant newTargetDate,
        String reason,
        String changedByName,
        Instant createdAt
) {
    public static DeadlineHistoryDto fromEntity(DeadlineHistory h) {
        return new DeadlineHistoryDto(
                h.getId(),
                h.getDeadline().getId(),
                h.getPreviousTargetDate(),
                h.getNewTargetDate(),
                h.getReason(),
                h.getChangedByUser() != null ? h.getChangedByUser().getFullName() : null,
                h.getCreatedAt()
        );
    }
}
