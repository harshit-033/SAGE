package org.aicte.sage.modules.compliance.dto;

import jakarta.validation.constraints.NotBlank;

public record ResolveComplianceFindingRequest(
        @NotBlank(message = "Resolution notes are mandatory for statutory audit trail")
        String resolutionNotes
) {}
