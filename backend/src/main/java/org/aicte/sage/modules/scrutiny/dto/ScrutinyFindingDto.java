package org.aicte.sage.modules.scrutiny.dto;

import org.aicte.sage.modules.documents.model.DocumentType;
import org.aicte.sage.modules.scrutiny.model.DeficiencyType;
import org.aicte.sage.modules.scrutiny.model.FindingStatus;
import org.aicte.sage.modules.scrutiny.model.ScrutinyFinding;

import java.time.Instant;
import java.util.UUID;

public class ScrutinyFindingDto {

    private UUID id;
    private UUID evaluationId;
    private UUID applicationId;
    private String ruleId;
    private String ruleName;
    private String aphClause;
    private Integer aphPageNumber;
    private String severity;
    private UUID documentId;
    private DocumentType documentType;
    private FindingStatus status;
    private DeficiencyType deficiencyType;
    private String explanation;
    private String evidenceText;
    private boolean resolved;
    private String resolutionNotes;
    private String resolvedByName;
    private Instant resolvedAt;
    private Instant createdAt;

    public ScrutinyFindingDto() {}

    public static ScrutinyFindingDto fromEntity(ScrutinyFinding f) {
        ScrutinyFindingDto dto = new ScrutinyFindingDto();
        dto.setId(f.getId());
        if (f.getEvaluation() != null) {
            dto.setEvaluationId(f.getEvaluation().getId());
        }
        if (f.getApplication() != null) {
            dto.setApplicationId(f.getApplication().getId());
        }
        if (f.getRule() != null) {
            dto.setRuleId(f.getRule().getId());
            dto.setRuleName(f.getRule().getName());
            dto.setAphClause(f.getRule().getAphClause());
            dto.setAphPageNumber(f.getRule().getAphPageNumber());
            dto.setSeverity(f.getRule().getSeverity());
        }
        if (f.getDocument() != null) {
            dto.setDocumentId(f.getDocument().getId());
            dto.setDocumentType(f.getDocument().getDocumentType());
        }
        dto.setStatus(f.getStatus());
        dto.setDeficiencyType(f.getDeficiencyType());
        dto.setExplanation(f.getExplanation());
        dto.setEvidenceText(f.getEvidenceText());
        dto.setResolved(f.isResolved());
        dto.setResolutionNotes(f.getResolutionNotes());
        if (f.getResolvedBy() != null) {
            dto.setResolvedByName(f.getResolvedBy().getFullName());
        }
        dto.setResolvedAt(f.getResolvedAt());
        dto.setCreatedAt(f.getCreatedAt());
        return dto;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getEvaluationId() { return evaluationId; }
    public void setEvaluationId(UUID evaluationId) { this.evaluationId = evaluationId; }

    public UUID getApplicationId() { return applicationId; }
    public void setApplicationId(UUID applicationId) { this.applicationId = applicationId; }

    public String getRuleId() { return ruleId; }
    public void setRuleId(String ruleId) { this.ruleId = ruleId; }

    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }

    public String getAphClause() { return aphClause; }
    public void setAphClause(String aphClause) { this.aphClause = aphClause; }

    public Integer getAphPageNumber() { return aphPageNumber; }
    public void setAphPageNumber(Integer aphPageNumber) { this.aphPageNumber = aphPageNumber; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public UUID getDocumentId() { return documentId; }
    public void setDocumentId(UUID documentId) { this.documentId = documentId; }

    public DocumentType getDocumentType() { return documentType; }
    public void setDocumentType(DocumentType documentType) { this.documentType = documentType; }

    public FindingStatus getStatus() { return status; }
    public void setStatus(FindingStatus status) { this.status = status; }

    public DeficiencyType getDeficiencyType() { return deficiencyType; }
    public void setDeficiencyType(DeficiencyType deficiencyType) { this.deficiencyType = deficiencyType; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public String getEvidenceText() { return evidenceText; }
    public void setEvidenceText(String evidenceText) { this.evidenceText = evidenceText; }

    public boolean isResolved() { return resolved; }
    public void setResolved(boolean resolved) { this.resolved = resolved; }

    public String getResolutionNotes() { return resolutionNotes; }
    public void setResolutionNotes(String resolutionNotes) { this.resolutionNotes = resolutionNotes; }

    public String getResolvedByName() { return resolvedByName; }
    public void setResolvedByName(String resolvedByName) { this.resolvedByName = resolvedByName; }

    public Instant getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(Instant resolvedAt) { this.resolvedAt = resolvedAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
