package org.aicte.sage.modules.applications.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SaveBranchDetailsRequest(
        @NotBlank(message = "Existing approved programme is required")
        String existingProgramme,

        @NotBlank(message = "Existing department is required")
        String existingDepartment,

        @NotBlank(message = "Proposed branch/specialization name is required")
        String proposedBranchName,

        @NotNull(message = "Proposed intake is required")
        @Min(value = 1, message = "Proposed intake must be at least 1")
        Integer proposedIntake,

        Integer existingApprovedIntake,
        Integer facultyCount,
        Integer staffCount,
        Integer availableClassrooms,
        Integer laboratoryCount,
        String laboratoryRequirements,
        String equipmentAvailability,
        String institutionDeclarations
) {}
