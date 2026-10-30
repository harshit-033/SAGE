package org.aicte.sage.modules.scrutiny.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SubmitEvaluatorAssessmentRequest(
        @NotNull(message = "Evaluation score is required (0-100)")
        @Min(value = 0, message = "Score cannot be less than 0")
        @Max(value = 100, message = "Score cannot exceed 100")
        Integer score,

        @NotBlank(message = "Statutory recommendation is required")
        String recommendation,

        @NotBlank(message = "Evaluator remarks and statutory justification are required")
        String remarks,

        String summary,

        String criteriaBreakdownJson
) {}
