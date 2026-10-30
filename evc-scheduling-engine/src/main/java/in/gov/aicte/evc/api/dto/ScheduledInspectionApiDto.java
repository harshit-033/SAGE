package in.gov.aicte.evc.api.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * Persisted scheduling result for an inspection.
 * Actual inspection days strictly exclude Sundays.
 */
public record ScheduledInspectionApiDto(
        Long inspectionId,
        Long institutionId,
        String institutionName,
        LocalDate scheduledStartDate,
        LocalDate scheduledEndDate,
        int durationInDays,
        int requiredMemberCount,
        List<LocalDate> actualInspectionDays,
        List<AssignedExpertApiDto> assignedExperts
) {
}

