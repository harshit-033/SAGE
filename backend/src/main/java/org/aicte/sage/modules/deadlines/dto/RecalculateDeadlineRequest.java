package org.aicte.sage.modules.deadlines.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record RecalculateDeadlineRequest(
        @NotNull(message = "New target date is mandatory")
        Instant newTargetDate,

        @NotBlank(message = "Justification reason is mandatory for audit trail")
        String reason
) {}
