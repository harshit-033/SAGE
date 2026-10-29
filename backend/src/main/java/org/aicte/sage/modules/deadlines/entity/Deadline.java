package org.aicte.sage.modules.deadlines.entity;

import jakarta.persistence.*;
import org.aicte.sage.modules.institutions.entity.Institution;
import org.aicte.sage.modules.users.entity.User;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "deadlines")
public class Deadline {

    @Id
    private UUID id;

    @Column(name = "deadline_code", nullable = false, length = 64)
    private String deadlineCode;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "deadline_type", nullable = false, length = 32)
    private DeadlineType deadlineType;

    @Column(name = "statutory_clause", length = 128)
    private String statutoryClause;

    @Column(name = "entity_type", nullable = false, length = 64)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private UUID entityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id", nullable = false)
    private Institution institution;

    @Column(name = "target_date", nullable = false)
    private Instant targetDate;

    @Column(name = "grace_period_days", nullable = false)
    private int gracePeriodDays = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private DeadlineStatus status = DeadlineStatus.PENDING;

    @Column(name = "reminder_schedule_json", columnDefinition = "TEXT")
    private String reminderScheduleJson;

    @Column(name = "last_evaluated_at")
    private Instant lastEvaluatedAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by_user_id")
    private User resolvedByUser;

    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;

    @Version
    @Column(nullable = false)
    private Long version = 0L;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public Deadline() {
        this.id = UUID.randomUUID();
    }

    public Deadline(
            String deadlineCode,
            String title,
            String description,
            DeadlineType deadlineType,
            String statutoryClause,
            String entityType,
            UUID entityId,
            Institution institution,
            Instant targetDate,
            int gracePeriodDays,
            String reminderScheduleJson) {
        this();
        this.deadlineCode = deadlineCode;
        this.title = title;
        this.description = description;
        this.deadlineType = deadlineType;
        this.statutoryClause = statutoryClause;
        this.entityType = entityType;
        this.entityId = entityId;
        this.institution = institution;
        this.targetDate = targetDate;
        this.gracePeriodDays = gracePeriodDays;
        this.reminderScheduleJson = reminderScheduleJson;
        this.status = DeadlineStatus.PENDING;
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

    public String getDeadlineCode() {
        return deadlineCode;
    }

    public void setDeadlineCode(String deadlineCode) {
        this.deadlineCode = deadlineCode;
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

    public DeadlineType getDeadlineType() {
        return deadlineType;
    }

    public void setDeadlineType(DeadlineType deadlineType) {
        this.deadlineType = deadlineType;
    }

    public String getStatutoryClause() {
        return statutoryClause;
    }

    public void setStatutoryClause(String statutoryClause) {
        this.statutoryClause = statutoryClause;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public UUID getEntityId() {
        return entityId;
    }

    public void setEntityId(UUID entityId) {
        this.entityId = entityId;
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public Instant getTargetDate() {
        return targetDate;
    }

    public void setTargetDate(Instant targetDate) {
        this.targetDate = targetDate;
    }

    public int getGracePeriodDays() {
        return gracePeriodDays;
    }

    public void setGracePeriodDays(int gracePeriodDays) {
        this.gracePeriodDays = gracePeriodDays;
    }

    public DeadlineStatus getStatus() {
        return status;
    }

    public void setStatus(DeadlineStatus status) {
        this.status = status;
    }

    public String getReminderScheduleJson() {
        return reminderScheduleJson;
    }

    public void setReminderScheduleJson(String reminderScheduleJson) {
        this.reminderScheduleJson = reminderScheduleJson;
    }

    public Instant getLastEvaluatedAt() {
        return lastEvaluatedAt;
    }

    public void setLastEvaluatedAt(Instant lastEvaluatedAt) {
        this.lastEvaluatedAt = lastEvaluatedAt;
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

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
