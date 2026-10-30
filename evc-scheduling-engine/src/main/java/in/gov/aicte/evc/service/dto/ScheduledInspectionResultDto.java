package in.gov.aicte.evc.service.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * Immutable DTO representing the successfully scheduled state of an inspection.
 */
public record ScheduledInspectionResultDto(
        Long inspectionId,
        Long institutionId,
        String institutionName,
        LocalDate scheduledStartDate,
        LocalDate scheduledEndDate,
        int durationInDays,
        int requiredMemberCount,
        List<AssignedExpertDto> assignedExperts
) {
}

