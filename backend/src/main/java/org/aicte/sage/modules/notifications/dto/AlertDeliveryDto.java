package org.aicte.sage.modules.notifications.dto;

import org.aicte.sage.modules.notifications.entity.AlertDelivery;
import org.aicte.sage.modules.notifications.entity.DeliveryChannel;
import org.aicte.sage.modules.notifications.entity.DeliveryStatus;

import java.time.Instant;
import java.util.UUID;

public record AlertDeliveryDto(
        UUID id,
        UUID alertId,
        DeliveryChannel channel,
        String recipientAddress,
        DeliveryStatus deliveryStatus,
        int attemptCount,
        Instant lastAttemptAt,
        Instant nextRetryAt,
        String errorMessage,
        Instant createdAt
) {
    public static AlertDeliveryDto fromEntity(AlertDelivery d) {
        return new AlertDeliveryDto(
                d.getId(),
                d.getAlert().getId(),
                d.getChannel(),
                d.getRecipientAddress(),
                d.getDeliveryStatus(),
                d.getAttemptCount(),
                d.getLastAttemptAt(),
                d.getNextRetryAt(),
                d.getErrorMessage(),
                d.getCreatedAt()
        );
    }
}
