package org.aicte.sage.modules.evc.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SubmitInspectionReportRequest(
        @NotBlank(message = "Report findings and observations are required")
        String reportFindings,

        @NotBlank(message = "Committee recommendation is required (e.g. RECOMMENDED, DEFICIENT, REQUIRES_HEARING)")
        String recommendation,

        @NotNull(message = "Infrastructure verification flag is required")
        Boolean infrastructureVerified,

        @NotNull(message = "Faculty cadre verification flag is required")
        Boolean facultyVerified,

        String remarks
) {}
