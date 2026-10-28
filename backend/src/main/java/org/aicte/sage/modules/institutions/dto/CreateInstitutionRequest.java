package org.aicte.sage.modules.institutions.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CreateInstitutionRequest {

    @NotBlank(message = "Institution PID code is required")
    @Pattern(regexp = "^1-\\d{8,10}$", message = "Institution code must match AICTE PID format (e.g., 1-500123498)")
    private String code;

    @NotBlank(message = "Legal institution name is required")
    @Size(min = 3, max = 255, message = "Name must be between 3 and 255 characters")
    private String name;

    @NotBlank(message = "Application type is required")
    private String applicationType;

    @NotBlank(message = "State is required")
    private String state;

    @NotBlank(message = "District is required")
    private String district;

    public CreateInstitutionRequest() {}

    public CreateInstitutionRequest(String code, String name, String applicationType, String state, String district) {
        this.code = code;
        this.name = name;
        this.applicationType = applicationType;
        this.state = state;
        this.district = district;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getApplicationType() {
        return applicationType;
    }

    public void setApplicationType(String applicationType) {
        this.applicationType = applicationType;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }
}
