package org.aicte.sage.modules.evc.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Persisted appointment linking an expert to a scheduled EVC inspection.
 */
@Entity
@Table(
    name = "committee_assignments",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_assignment_inspection_expert",
        columnNames = {"inspection_id", "expert_id"}
    ),
    indexes = {
        @Index(name = "idx_assignments_inspection", columnList = "inspection_id"),
        @Index(name = "idx_assignments_expert", columnList = "expert_id")
    }
)
public class CommitteeAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @NotNull(message = "Inspection is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inspection_id", nullable = false)
    private EvcInspection inspection;

    @NotNull(message = "Expert is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "expert_id", nullable = false)
    private Expert expert;

    @NotNull(message = "Assignment role is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 30)
    private AssignmentRole role;

    @NotNull(message = "Assignment status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private AssignmentStatus status = AssignmentStatus.PROPOSED;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public CommitteeAssignment() {
    }

    public CommitteeAssignment(EvcInspection inspection, Expert expert, AssignmentRole role, AssignmentStatus status) {
        this.inspection = inspection;
        this.expert = expert;
        this.role = role;
        this.status = status != null ? status : AssignmentStatus.PROPOSED;
    }

    @PrePersist
    protected void onCreate() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public EvcInspection getInspection() {
        return inspection;
    }

    public void setInspection(EvcInspection inspection) {
        this.inspection = inspection;
    }

    public Expert getExpert() {
        return expert;
    }

    public void setExpert(Expert expert) {
        this.expert = expert;
    }

    public AssignmentRole getRole() {
        return role;
    }

    public void setRole(AssignmentRole role) {
        this.role = role;
    }

    public AssignmentStatus getStatus() {
        return status;
    }

    public void setStatus(AssignmentStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommitteeAssignment that = (CommitteeAssignment) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
