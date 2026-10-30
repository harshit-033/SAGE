package in.gov.aicte.evc.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.Map;

/**
 * Request payload submitted by SAGE to execute synchronized committee scheduling.
 * Durations, committee sizes, and permissible start windows are loaded authoritatively
 * from the database and cannot be arbitrarily overridden by API clients.
 */
public record ScheduleApiRequest(
        @NotEmpty(message = "At least one inspection ID must be provided")
        List<Long> inspectionIds,

        boolean allowRescheduling,

        @Min(value = 1, message = "Time limit seconds must be at least 1")
        @Max(value = 60, message = "Time limit seconds cannot exceed 60")
        Integer timeLimitSeconds,

        Map<Long, List<Long>> eligibleExpertOverrides,

        String requestId
) {
}

