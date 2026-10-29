package org.aicte.sage.modules.notifications.entity;

import jakarta.persistence.*;
import org.aicte.sage.modules.institutions.entity.Institution;
import org.aicte.sage.modules.users.entity.User;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "alerts")
public class Alert {

    @Id
    private UUID id;

    @Column(name = "alert_code", nullable = false, length = 64)
    private String alertCode;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AlertCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AlertSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AlertPriority priority;

    @Column(name = "source_entity_type", nullable = false, length = 64)
    private String sourceEntityType;

    @Column(name = "source_entity_id", nullable = false)
    private UUID sourceEntityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id", nullable = false)
    private Institution institution;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_user_id")
    private User recipientUser;

    @Column(name = "target_role", length = 64)
    private String targetRole;

    @Column(name = "jurisdiction_state", length = 64)
    private String jurisdictionState;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AlertStatus status = AlertStatus.UNREAD;

    @Column(name = "acknowledged_at")
    private Instant acknowledgedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acknowledged_by_user_id")
    private User acknowledgedByUser;

    @Column(name = "acknowledgement_notes", columnDefinition = "TEXT")
    private String acknowledgementNotes;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by_user_id")
    private User resolvedByUser;

    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;

    @Column(name = "is_escalated", nullable = false)
    private boolean escalated = false;

    @Column(name = "escalated_to_role", length = 64)
    private String escalatedToRole;

    @Column(name = "escalated_at")
    private Instant escalatedAt;

    @Column(name = "deduplication_hash", nullable = false, unique = true, length = 128)
    private String deduplicationHash;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public Alert() {
        this.id = UUID.randomUUID();
    }

    public Alert(
            String alertCode,
            String title,
            String message,
            AlertCategory category,
            AlertSeverity severity,
            AlertPriority priority,
            String sourceEntityType,
            UUID sourceEntityId,
            Institution institution,
            User recipientUser,
            String targetRole,
            String jurisdictionState,
            String deduplicationHash) {
        this();
        this.alertCode = alertCode;
        this.title = title;
        this.message = message;
        this.category = category;
        this.severity = severity;
        this.priority = priority;
        this.sourceEntityType = sourceEntityType;
        this.sourceEntityId = sourceEntityId;
        this.institution = institution;
        this.recipientUser = recipientUser;
        this.targetRole = targetRole;
        this.jurisdictionState = jurisdictionState;
        this.deduplicationHash = deduplicationHash;
        this.status = AlertStatus.UNREAD;
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

    public String getAlertCode() {
        return alertCode;
    }

    public void setAlertCode(String alertCode) {
        this.alertCode = alertCode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public AlertCategory getCategory() {
        return category;
    }

    public void setCategory(AlertCategory category) {
        this.category = category;
    }

    public AlertSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(AlertSeverity severity) {
        this.severity = severity;
    }

    public AlertPriority getPriority() {
        return priority;
    }

    public void setPriority(AlertPriority priority) {
        this.priority = priority;
    }

    public String getSourceEntityType() {
        return sourceEntityType;
    }

    public void setSourceEntityType(String sourceEntityType) {
        this.sourceEntityType = sourceEntityType;
    }

    public UUID getSourceEntityId() {
        return sourceEntityId;
    }

    public void setSourceEntityId(UUID sourceEntityId) {
        this.sourceEntityId = sourceEntityId;
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public User getRecipientUser() {
        return recipientUser;
    }

    public void setRecipientUser(User recipientUser) {
        this.recipientUser = recipientUser;
    }

    public String getTargetRole() {
        return targetRole;
    }

    public void setTargetRole(String targetRole) {
        this.targetRole = targetRole;
    }

    public String getJurisdictionState() {
        return jurisdictionState;
    }

    public void setJurisdictionState(String jurisdictionState) {
        this.jurisdictionState = jurisdictionState;
    }

    public AlertStatus getStatus() {
        return status;
    }

    public void setStatus(AlertStatus status) {
        this.status = status;
    }

    public Instant getAcknowledgedAt() {
        return acknowledgedAt;
    }

    public void setAcknowledgedAt(Instant acknowledgedAt) {
        this.acknowledgedAt = acknowledgedAt;
    }

    public User getAcknowledgedByUser() {
        return acknowledgedByUser;
    }

    public void setAcknowledgedByUser(User acknowledgedByUser) {
        this.acknowledgedByUser = acknowledgedByUser;
    }

    public String getAcknowledgementNotes() {
        return acknowledgementNotes;
    }

    public void setAcknowledgementNotes(String acknowledgementNotes) {
        this.acknowledgementNotes = acknowledgementNotes;
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

    public boolean isEscalated() {
        return escalated;
    }

    public void setEscalated(boolean escalated) {
        this.escalated = escalated;
    }

    public String getEscalatedToRole() {
        return escalatedToRole;
    }

    public void setEscalatedToRole(String escalatedToRole) {
        this.escalatedToRole = escalatedToRole;
    }

    public Instant getEscalatedAt() {
        return escalatedAt;
    }

    public void setEscalatedAt(Instant escalatedAt) {
        this.escalatedAt = escalatedAt;
    }

    public String getDeduplicationHash() {
        return deduplicationHash;
    }

    public void setDeduplicationHash(String deduplicationHash) {
        this.deduplicationHash = deduplicationHash;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
