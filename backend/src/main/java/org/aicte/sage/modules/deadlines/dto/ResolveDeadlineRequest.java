package org.aicte.sage.modules.deadlines.dto;

import jakarta.validation.constraints.NotBlank;

public record ResolveDeadlineRequest(
        @NotBlank(message = "Resolution notes are mandatory for statutory audit trail")
        String resolutionNotes
) {}
