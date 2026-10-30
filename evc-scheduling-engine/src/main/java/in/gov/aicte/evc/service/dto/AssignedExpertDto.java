package in.gov.aicte.evc.service.dto;

import in.gov.aicte.evc.entity.AssignmentRole;
import in.gov.aicte.evc.entity.AssignmentStatus;

/**
 * Immutable DTO representing an expert assigned to an inspection committee.
 */
public record AssignedExpertDto(
        Long expertId,
        String expertName,
        String discipline,
        AssignmentRole role,
        AssignmentStatus status
) {
}

