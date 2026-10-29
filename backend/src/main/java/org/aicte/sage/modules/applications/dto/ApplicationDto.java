package org.aicte.sage.modules.applications.dto;

import org.aicte.sage.modules.applications.model.Application;
import org.aicte.sage.modules.applications.model.ApplicationCategory;
import org.aicte.sage.modules.applications.model.ApplicationState;

import java.time.Instant;
import java.util.UUID;

public class ApplicationDto {

    private UUID id;
    private String applicationNumber;
    private UUID institutionId;
    private String institutionName;
    private String academicYear;
    private ApplicationCategory category;
    private ApplicationState currentState;
    private String workflowCode;
    private String workflowName;
    private boolean requiresEvc;
    private boolean requiresShc;
    private boolean requiresSac;
    private Instant submittedAt;
    private Instant finalDecisionAt;
    private Long version;
    private Instant createdAt;
    private Instant updatedAt;

    public ApplicationDto() {}

    public static ApplicationDto fromEntity(Application app) {
        ApplicationDto dto = new ApplicationDto();
        dto.setId(app.getId());
        dto.setApplicationNumber(app.getApplicationNumber());
        if (app.getInstitution() != null) {
            dto.setInstitutionId(app.getInstitution().getId());
            dto.setInstitutionName(app.getInstitution().getName());
        }
        dto.setAcademicYear(app.getAcademicYear());
        dto.setCategory(app.getCategory());
        dto.setCurrentState(app.getCurrentState());
        if (app.getWorkflowDefinition() != null) {
            dto.setWorkflowCode(app.getWorkflowDefinition().getCode());
            dto.setWorkflowName(app.getWorkflowDefinition().getName());
            dto.setRequiresEvc(app.getWorkflowDefinition().isRequiresEvc());
            dto.setRequiresShc(app.getWorkflowDefinition().isRequiresShc());
            dto.setRequiresSac(app.getWorkflowDefinition().isRequiresSac());
        }
        dto.setSubmittedAt(app.getSubmittedAt());
        dto.setFinalDecisionAt(app.getFinalDecisionAt());
        dto.setVersion(app.getVersion());
        dto.setCreatedAt(app.getCreatedAt());
        dto.setUpdatedAt(app.getUpdatedAt());
        return dto;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getApplicationNumber() { return applicationNumber; }
    public void setApplicationNumber(String applicationNumber) { this.applicationNumber = applicationNumber; }

    public UUID getInstitutionId() { return institutionId; }
    public void setInstitutionId(UUID institutionId) { this.institutionId = institutionId; }

    public String getInstitutionName() { return institutionName; }
    public void setInstitutionName(String institutionName) { this.institutionName = institutionName; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    public ApplicationCategory getCategory() { return category; }
    public void setCategory(ApplicationCategory category) { this.category = category; }

    public ApplicationState getCurrentState() { return currentState; }
    public void setCurrentState(ApplicationState currentState) { this.currentState = currentState; }

    public String getWorkflowCode() { return workflowCode; }
    public void setWorkflowCode(String workflowCode) { this.workflowCode = workflowCode; }

    public String getWorkflowName() { return workflowName; }
    public void setWorkflowName(String workflowName) { this.workflowName = workflowName; }

    public boolean isRequiresEvc() { return requiresEvc; }
    public void setRequiresEvc(boolean requiresEvc) { this.requiresEvc = requiresEvc; }

    public boolean isRequiresShc() { return requiresShc; }
    public void setRequiresShc(boolean requiresShc) { this.requiresShc = requiresShc; }

    public boolean isRequiresSac() { return requiresSac; }
    public void setRequiresSac(boolean requiresSac) { this.requiresSac = requiresSac; }

    public Instant getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(Instant submittedAt) { this.submittedAt = submittedAt; }

    public Instant getFinalDecisionAt() { return finalDecisionAt; }
    public void setFinalDecisionAt(Instant finalDecisionAt) { this.finalDecisionAt = finalDecisionAt; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
