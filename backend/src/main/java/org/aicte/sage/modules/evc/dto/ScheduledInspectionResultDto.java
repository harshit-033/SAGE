package org.aicte.sage.modules.evc.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ScheduledInspectionResultDto(
        UUID inspectionId,
        UUID institutionId,
        String institutionName,
        UUID applicationId,
        String applicationNumber,
        LocalDate scheduledStartDate,
        LocalDate scheduledEndDate,
        int durationInDays,
        int requiredMemberCount,
        List<AssignedExpertDto> assignedExperts
) {}
