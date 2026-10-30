package org.aicte.sage.modules.documents.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record RespondCorrectiveActionRequest(
        @NotBlank(message = "Institution response explanation is required")
        String institutionResponse,

        UUID resolutionEvidenceDocId
) {}
