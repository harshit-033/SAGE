package org.aicte.sage.modules.notifications.dto;

import jakarta.validation.constraints.NotBlank;

public record AcknowledgeAlertRequest(
        @NotBlank(message = "Acknowledgement notes are mandatory")
        String acknowledgementNotes
) {}
