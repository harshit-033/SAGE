package org.aicte.sage.modules.applications.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.aicte.sage.modules.applications.model.ApplicationCategory;

import java.util.UUID;

public class CreateApplicationRequest {

    @NotNull(message = "Institution ID is required")
    private UUID institutionId;

    @NotBlank(message = "Academic year is required (e.g. 2024-25)")
    private String academicYear;

    @NotNull(message = "Application category is required")
    private ApplicationCategory category;

    public CreateApplicationRequest() {}

    public CreateApplicationRequest(UUID institutionId, String academicYear, ApplicationCategory category) {
        this.institutionId = institutionId;
        this.academicYear = academicYear;
        this.category = category;
    }

    public UUID getInstitutionId() { return institutionId; }
    public void setInstitutionId(UUID institutionId) { this.institutionId = institutionId; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    public ApplicationCategory getCategory() { return category; }
    public void setCategory(ApplicationCategory category) { this.category = category; }
}
