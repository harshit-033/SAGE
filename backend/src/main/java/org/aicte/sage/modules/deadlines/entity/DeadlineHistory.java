package org.aicte.sage.modules.deadlines.entity;

import jakarta.persistence.*;
import org.aicte.sage.modules.users.entity.User;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "deadline_history")
public class DeadlineHistory {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deadline_id", nullable = false)
    private Deadline deadline;

    @Column(name = "previous_target_date", nullable = false)
    private Instant previousTargetDate;

    @Column(name = "new_target_date", nullable = false)
    private Instant newTargetDate;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by_user_id")
    private User changedByUser;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public DeadlineHistory() {
        this.id = UUID.randomUUID();
    }

    public DeadlineHistory(
            Deadline deadline,
            Instant previousTargetDate,
            Instant newTargetDate,
            String reason,
            User changedByUser) {
        this();
        this.deadline = deadline;
        this.previousTargetDate = previousTargetDate;
        this.newTargetDate = newTargetDate;
        this.reason = reason;
        this.changedByUser = changedByUser;
    }

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Deadline getDeadline() {
        return deadline;
    }

    public void setDeadline(Deadline deadline) {
        this.deadline = deadline;
    }

    public Instant getPreviousTargetDate() {
        return previousTargetDate;
    }

    public void setPreviousTargetDate(Instant previousTargetDate) {
        this.previousTargetDate = previousTargetDate;
    }

    public Instant getNewTargetDate() {
        return newTargetDate;
    }

    public void setNewTargetDate(Instant newTargetDate) {
        this.newTargetDate = newTargetDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public User getChangedByUser() {
        return changedByUser;
    }

    public void setChangedByUser(User changedByUser) {
        this.changedByUser = changedByUser;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
