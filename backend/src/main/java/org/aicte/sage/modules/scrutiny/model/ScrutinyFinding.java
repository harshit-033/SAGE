package org.aicte.sage.modules.scrutiny.model;

import jakarta.persistence.*;
import org.aicte.sage.modules.applications.model.Application;
import org.aicte.sage.modules.documents.model.Document;
import org.aicte.sage.modules.users.entity.User;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "scrutiny_findings")
public class ScrutinyFinding {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "evaluation_id", nullable = false)
    private ScrutinyEvaluation evaluation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "rule_id", nullable = false)
    private ScrutinyRule rule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id")
    private Document document;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private FindingStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "deficiency_type", length = 64)
    private DeficiencyType deficiencyType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String explanation;

    @Column(name = "evidence_text", columnDefinition = "TEXT")
    private String evidenceText;

    @Column(name = "is_resolved", nullable = false)
    private boolean resolved = false;

    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by")
    private User resolvedBy;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public ScrutinyFinding() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public ScrutinyEvaluation getEvaluation() { return evaluation; }
    public void setEvaluation(ScrutinyEvaluation evaluation) { this.evaluation = evaluation; }

    public Application getApplication() { return application; }
    public void setApplication(Application application) { this.application = application; }

    public ScrutinyRule getRule() { return rule; }
    public void setRule(ScrutinyRule rule) { this.rule = rule; }

    public Document getDocument() { return document; }
    public void setDocument(Document document) { this.document = document; }

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

    public User getResolvedBy() { return resolvedBy; }
    public void setResolvedBy(User resolvedBy) { this.resolvedBy = resolvedBy; }

    public Instant getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(Instant resolvedAt) { this.resolvedAt = resolvedAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
