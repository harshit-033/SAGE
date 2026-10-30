package org.aicte.sage.modules.documents.model;

import jakarta.persistence.*;
import org.aicte.sage.modules.applications.model.Application;
import org.aicte.sage.modules.institutions.entity.Institution;
import org.aicte.sage.modules.users.entity.User;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "document_corrective_actions")
public class DocumentCorrectiveAction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "institution_id", nullable = false)
    private Institution institution;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id")
    private Application application;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id")
    private Document document;

    @Column(name = "finding_id")
    private UUID findingId;

    @Column(name = "action_type", nullable = false, length = 64)
    private String actionType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 32)
    private String priority = "HIGH";

    @Column(name = "responsible_party", nullable = false, length = 64)
    private String responsibleParty = "INSTITUTION_OFFICER";

    @Column(name = "due_date")
    private Instant dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private CorrectiveActionStatus status = CorrectiveActionStatus.OPEN;

    @Column(name = "institution_response", columnDefinition = "TEXT")
    private String institutionResponse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolution_evidence_doc_id")
    private Document resolutionEvidenceDoc;

    @Column(name = "officer_notes", columnDefinition = "TEXT")
    private String officerNotes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by")
    private User resolvedBy;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public DocumentCorrectiveAction() {}

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Institution getInstitution() { return institution; }
    public void setInstitution(Institution institution) { this.institution = institution; }

    public Application getApplication() { return application; }
    public void setApplication(Application application) { this.application = application; }

    public Document getDocument() { return document; }
    public void setDocument(Document document) { this.document = document; }

    public UUID getFindingId() { return findingId; }
    public void setFindingId(UUID findingId) { this.findingId = findingId; }

    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getResponsibleParty() { return responsibleParty; }
    public void setResponsibleParty(String responsibleParty) { this.responsibleParty = responsibleParty; }

    public Instant getDueDate() { return dueDate; }
    public void setDueDate(Instant dueDate) { this.dueDate = dueDate; }

    public CorrectiveActionStatus getStatus() { return status; }
    public void setStatus(CorrectiveActionStatus status) { this.status = status; }

    public String getInstitutionResponse() { return institutionResponse; }
    public void setInstitutionResponse(String institutionResponse) { this.institutionResponse = institutionResponse; }

    public Document getResolutionEvidenceDoc() { return resolutionEvidenceDoc; }
    public void setResolutionEvidenceDoc(Document resolutionEvidenceDoc) { this.resolutionEvidenceDoc = resolutionEvidenceDoc; }

    public String getOfficerNotes() { return officerNotes; }
    public void setOfficerNotes(String officerNotes) { this.officerNotes = officerNotes; }

    public User getResolvedBy() { return resolvedBy; }
    public void setResolvedBy(User resolvedBy) { this.resolvedBy = resolvedBy; }

    public Instant getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(Instant resolvedAt) { this.resolvedAt = resolvedAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
