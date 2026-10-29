package org.aicte.sage.modules.scrutiny.dto;

import org.aicte.sage.modules.scrutiny.model.ScrutinyEvaluation;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ScrutinyEvaluationDto {

    private UUID id;
    private UUID applicationId;
    private String status;
    private String summary;
    private String evaluatedByName;
    private Instant evaluatedAt;
    private List<ScrutinyFindingDto> findings = new ArrayList<>();

    public ScrutinyEvaluationDto() {}

    public static ScrutinyEvaluationDto fromEntity(ScrutinyEvaluation e) {
        ScrutinyEvaluationDto dto = new ScrutinyEvaluationDto();
        dto.setId(e.getId());
        if (e.getApplication() != null) {
            dto.setApplicationId(e.getApplication().getId());
        }
        dto.setStatus(e.getStatus());
        dto.setSummary(e.getSummary());
        if (e.getEvaluatedByUser() != null) {
            dto.setEvaluatedByName(e.getEvaluatedByUser().getFullName());
        } else {
            dto.setEvaluatedByName("Automated Scrutiny Engine");
        }
        dto.setEvaluatedAt(e.getEvaluatedAt());
        return dto;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getApplicationId() { return applicationId; }
    public void setApplicationId(UUID applicationId) { this.applicationId = applicationId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getEvaluatedByName() { return evaluatedByName; }
    public void setEvaluatedByName(String evaluatedByName) { this.evaluatedByName = evaluatedByName; }

    public Instant getEvaluatedAt() { return evaluatedAt; }
    public void setEvaluatedAt(Instant evaluatedAt) { this.evaluatedAt = evaluatedAt; }

    public List<ScrutinyFindingDto> getFindings() { return findings; }
    public void setFindings(List<ScrutinyFindingDto> findings) { this.findings = findings; }
}
