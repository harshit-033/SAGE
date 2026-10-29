package org.aicte.sage.modules.deadlines.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.aicte.sage.modules.deadlines.entity.DeadlineType;

import java.time.Instant;
import java.util.UUID;

public record CreateDeadlineRequest(
        @NotBlank(message = "Deadline code is mandatory")
        String deadlineCode,

        @NotBlank(message = "Title is mandatory")
        String title,

        String description,

        @NotNull(message = "Deadline type is mandatory")
        DeadlineType deadlineType,

        String statutoryClause,

        @NotBlank(message = "Entity type is mandatory")
        String entityType,

        @NotNull(message = "Entity ID is mandatory")
        UUID entityId,

        @NotNull(message = "Institution ID is mandatory")
        UUID institutionId,

        @NotNull(message = "Target date is mandatory")
        Instant targetDate,

        Integer gracePeriodDays,

        String reminderScheduleJson
) {}
