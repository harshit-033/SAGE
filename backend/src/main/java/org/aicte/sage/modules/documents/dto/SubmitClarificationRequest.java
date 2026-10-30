package org.aicte.sage.modules.documents.dto;

import jakarta.validation.constraints.NotBlank;

public record SubmitClarificationRequest(
        @NotBlank(message = "Clarification text is required")
        String clarificationText
) {}
