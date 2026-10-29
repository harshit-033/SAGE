package org.aicte.sage.modules.notifications.channel;

import org.aicte.sage.modules.notifications.entity.Alert;
import org.aicte.sage.modules.notifications.entity.DeliveryChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class InAppNotificationChannelAdapter implements NotificationChannelAdapter {

    private static final Logger log = LoggerFactory.getLogger(InAppNotificationChannelAdapter.class);

    @Override
    public boolean supports(DeliveryChannel channel) {
        return channel == DeliveryChannel.IN_APP;
    }

    @Override
    public DeliveryResult deliver(Alert alert, String recipientAddress) {
        log.info("Dispatched IN_APP alert [{}] for recipient [{}] - Title: {}",
                alert.getAlertCode(), recipientAddress, alert.getTitle());
        return DeliveryResult.ok();
    }
}
