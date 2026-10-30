package in.gov.aicte.evc.api.dto;

import in.gov.aicte.evc.entity.AssignmentRole;
import in.gov.aicte.evc.entity.AssignmentStatus;

/**
 * Details of an expert assigned to an inspection committee.
 */
public record AssignedExpertApiDto(
        Long expertId,
        String name,
        String discipline,
        AssignmentRole role,
        AssignmentStatus status
) {
}

