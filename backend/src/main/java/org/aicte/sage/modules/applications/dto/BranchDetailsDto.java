package org.aicte.sage.modules.applications.dto;

import org.aicte.sage.modules.applications.model.ApplicationBranchDetails;

import java.util.UUID;

public record BranchDetailsDto(
        UUID id,
        UUID applicationId,
        String existingProgramme,
        String existingDepartment,
        String proposedBranchName,
        Integer proposedIntake,
        Integer existingApprovedIntake,
        Integer facultyCount,
        Integer staffCount,
        Integer availableClassrooms,
        Integer laboratoryCount,
        String laboratoryRequirements,
        String equipmentAvailability,
        String institutionDeclarations
) {
    public static BranchDetailsDto fromEntity(ApplicationBranchDetails entity) {
        if (entity == null) return null;
        return new BranchDetailsDto(
                entity.getId(),
                entity.getApplication().getId(),
                entity.getExistingProgramme(),
                entity.getExistingDepartment(),
                entity.getProposedBranchName(),
                entity.getProposedIntake(),
                entity.getExistingApprovedIntake(),
                entity.getFacultyCount(),
                entity.getStaffCount(),
                entity.getAvailableClassrooms(),
                entity.getLaboratoryCount(),
                entity.getLaboratoryRequirements(),
                entity.getEquipmentAvailability(),
                entity.getInstitutionDeclarations()
        );
    }
}
