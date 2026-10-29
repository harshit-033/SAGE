package org.aicte.sage.modules.notifications.channel;

import org.aicte.sage.modules.notifications.entity.Alert;
import org.aicte.sage.modules.notifications.entity.DeliveryChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EmailNotificationChannelAdapter implements NotificationChannelAdapter {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationChannelAdapter.class);

    @Value("${sage.notifications.email.enabled:false}")
    private boolean emailEnabled;

    @Value("${sage.notifications.email.from-address:notifications@aicte.gov.in}")
    private String fromAddress;

    @Override
    public boolean supports(DeliveryChannel channel) {
        return channel == DeliveryChannel.EMAIL;
    }

    @Override
    public DeliveryResult deliver(Alert alert, String recipientAddress) {
        if (recipientAddress == null || recipientAddress.isBlank()) {
            return DeliveryResult.failure("Invalid empty recipient address");
        }

        try {
            // Simulated secure transactional SMTP/Email dispatch
            log.info("Simulated SMTP dispatch from [{}] to [{}] - Subject: [AICTE Alert - {}] {} | Severity: {}",
                    fromAddress, recipientAddress, alert.getSeverity(), alert.getTitle(), alert.getSeverity());
            return DeliveryResult.ok();
        } catch (Exception ex) {
            log.error("Failed to dispatch email for alert [{}] to [{}]: {}",
                    alert.getId(), recipientAddress, ex.getMessage());
            return DeliveryResult.failure("Email gateway error: " + ex.getMessage());
        }
    }
}
