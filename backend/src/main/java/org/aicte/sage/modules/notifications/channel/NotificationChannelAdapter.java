package org.aicte.sage.modules.notifications.channel;

import org.aicte.sage.modules.notifications.entity.Alert;
import org.aicte.sage.modules.notifications.entity.DeliveryChannel;

public interface NotificationChannelAdapter {

    boolean supports(DeliveryChannel channel);

    DeliveryResult deliver(Alert alert, String recipientAddress);

    record DeliveryResult(boolean isSuccess, String errorMessage) {
        public static DeliveryResult ok() {
            return new DeliveryResult(true, null);
        }

        public static DeliveryResult failure(String errorMessage) {
            return new DeliveryResult(false, errorMessage);
        }
    }
}
