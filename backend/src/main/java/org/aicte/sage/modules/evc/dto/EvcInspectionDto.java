package org.aicte.sage.modules.evc.dto;

import org.aicte.sage.modules.evc.entity.EvcInspection;
import org.aicte.sage.modules.evc.entity.InspectionStatus;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public record EvcInspectionDto(
        UUID id,
        UUID institutionId,
        String institutionName,
        String institutionCode,
        UUID applicationId,
        String applicationNumber,
        int durationInDays,
        int requiredMemberCount,
        LocalDate earliestStartDate,
        LocalDate latestStartDate,
        LocalDate scheduledStartDate,
        LocalDate scheduledEndDate,
        InspectionStatus status,
        String specialInstructions,
        List<AssignedExpertDto> assignedCommittee
) {
    public static EvcInspectionDto fromEntity(EvcInspection inspection, List<AssignedExpertDto> committee) {
        String instName = inspection.getInstitution() != null ? inspection.getInstitution().getName() : "Unknown";
        String instCode = inspection.getInstitution() != null ? inspection.getInstitution().getCode() : "";
        UUID instId = inspection.getInstitution() != null ? inspection.getInstitution().getId() : null;
        UUID appId = inspection.getApplication() != null ? inspection.getApplication().getId() : null;
        String appNum = inspection.getApplication() != null ? inspection.getApplication().getApplicationNumber() : "";

        return new EvcInspectionDto(
                inspection.getId(),
                instId,
                instName,
                instCode,
                appId,
                appNum,
                inspection.getDurationInDays(),
                inspection.getRequiredMemberCount(),
                inspection.getEarliestStartDate(),
                inspection.getLatestStartDate(),
                inspection.getScheduledStartDate(),
                inspection.getScheduledEndDate(),
                inspection.getStatus(),
                inspection.getSpecialInstructions(),
                committee != null ? committee : Collections.emptyList()
        );
    }
}
