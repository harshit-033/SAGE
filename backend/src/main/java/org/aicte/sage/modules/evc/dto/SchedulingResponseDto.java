package org.aicte.sage.modules.evc.dto;

import org.aicte.sage.modules.evc.scheduler.model.SchedulingStatus;

import java.util.Collections;
import java.util.List;

public record SchedulingResponseDto(
        SchedulingStatus status,
        String message,
        int inspectionsRequested,
        long solveDurationMs,
        List<ScheduledInspectionResultDto> scheduledInspections,
        String explanation
) {
    public static SchedulingResponseDto success(
            SchedulingStatus status,
            int inspectionsRequested,
            long solveDurationMs,
            List<ScheduledInspectionResultDto> scheduledInspections
    ) {
        return new SchedulingResponseDto(
                status,
                "EVC inspection schedule successfully computed and persisted.",
                inspectionsRequested,
                solveDurationMs,
                scheduledInspections != null ? scheduledInspections : Collections.emptyList(),
                "Optimal or feasible committee schedule committed to database."
        );
    }

    public static SchedulingResponseDto infeasible(
            int inspectionsRequested,
            long solveDurationMs,
            String explanation
    ) {
        return new SchedulingResponseDto(
                SchedulingStatus.INFEASIBLE,
                "The requested inspection schedule is mathematically infeasible under statutory constraints.",
                inspectionsRequested,
                solveDurationMs,
                Collections.emptyList(),
                explanation
        );
    }

    public static SchedulingResponseDto timeLimitReached(
            int inspectionsRequested,
            long solveDurationMs,
            String explanation
    ) {
        return new SchedulingResponseDto(
                SchedulingStatus.TIME_LIMIT_REACHED,
                "Solver reached the allotted computation time limit without proving feasibility.",
                inspectionsRequested,
                solveDurationMs,
                Collections.emptyList(),
                explanation
        );
    }

    public static SchedulingResponseDto invalidInput(String explanation) {
        return new SchedulingResponseDto(
                SchedulingStatus.INVALID_INPUT,
                "Invalid input parameters provided for scheduling.",
                0,
                0,
                Collections.emptyList(),
                explanation
        );
    }

    public static SchedulingResponseDto error(String explanation) {
        return new SchedulingResponseDto(
                SchedulingStatus.ERROR,
                "An unexpected solver error occurred during schedule generation.",
                0,
                0,
                Collections.emptyList(),
                explanation
        );
    }
}
