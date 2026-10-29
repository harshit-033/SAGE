package org.aicte.sage.modules.compliance.entity;

import jakarta.persistence.*;
import org.aicte.sage.modules.applications.model.Application;
import org.aicte.sage.modules.documents.model.Document;
import org.aicte.sage.modules.institutions.entity.Institution;
import org.aicte.sage.modules.notifications.entity.AlertSeverity;
import org.aicte.sage.modules.users.entity.User;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "compliance_findings")
public class ComplianceFinding {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "check_run_id", nullable = false)
    private ComplianceCheckRun checkRun;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id", nullable = false)
    private Institution institution;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id")
    private Application application;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id")
    private Document document;

    @Column(name = "rule_code", nullable = false, length = 64)
    private String ruleCode;

    @Column(name = "aph_clause", nullable = false, length = 128)
    private String aphClause;

    @Enumerated(EnumType.STRING)
    @Column(name = "finding_type", nullable = false, length = 64)
    private FindingType findingType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AlertSeverity severity;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "evidence_summary", columnDefinition = "TEXT")
    private String evidenceSummary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ComplianceFindingStatus status = ComplianceFindingStatus.OPEN;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by_user_id")
    private User resolvedByUser;

    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public ComplianceFinding() {
        this.id = UUID.randomUUID();
    }

    public ComplianceFinding(
            ComplianceCheckRun checkRun,
            Institution institution,
            Application application,
            Document document,
            String ruleCode,
            String aphClause,
            FindingType findingType,
            AlertSeverity severity,
            String title,
            String description,
            String evidenceSummary) {
        this();
        this.checkRun = checkRun;
        this.institution = institution;
        this.application = application;
        this.document = document;
        this.ruleCode = ruleCode;
        this.aphClause = aphClause;
        this.findingType = findingType;
        this.severity = severity;
        this.title = title;
        this.description = description;
        this.evidenceSummary = evidenceSummary;
        this.status = ComplianceFindingStatus.OPEN;
    }

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = Instant.now();
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public ComplianceCheckRun getCheckRun() {
        return checkRun;
    }

    public void setCheckRun(ComplianceCheckRun checkRun) {
        this.checkRun = checkRun;
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public String getRuleCode() {
        return ruleCode;
    }

    public void setRuleCode(String ruleCode) {
        this.ruleCode = ruleCode;
    }

    public String getAphClause() {
        return aphClause;
    }

    public void setAphClause(String aphClause) {
        this.aphClause = aphClause;
    }

    public FindingType getFindingType() {
        return findingType;
    }

    public void setFindingType(FindingType findingType) {
        this.findingType = findingType;
    }

    public AlertSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(AlertSeverity severity) {
        this.severity = severity;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEvidenceSummary() {
        return evidenceSummary;
    }

    public void setEvidenceSummary(String evidenceSummary) {
        this.evidenceSummary = evidenceSummary;
    }

    public ComplianceFindingStatus getStatus() {
        return status;
    }

    public void setStatus(ComplianceFindingStatus status) {
        this.status = status;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(Instant resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public User getResolvedByUser() {
        return resolvedByUser;
    }

    public void setResolvedByUser(User resolvedByUser) {
        this.resolvedByUser = resolvedByUser;
    }

    public String getResolutionNotes() {
        return resolutionNotes;
    }

    public void setResolutionNotes(String resolutionNotes) {
        this.resolutionNotes = resolutionNotes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
