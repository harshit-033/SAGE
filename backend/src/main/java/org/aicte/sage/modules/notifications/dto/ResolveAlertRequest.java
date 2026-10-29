package org.aicte.sage.modules.notifications.dto;

import jakarta.validation.constraints.NotBlank;

public record ResolveAlertRequest(
        @NotBlank(message = "Resolution notes are mandatory for statutory audit trail")
        String resolutionNotes
) {}
