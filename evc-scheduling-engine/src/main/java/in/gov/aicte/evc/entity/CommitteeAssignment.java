package in.gov.aicte.evc.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents the assignment of an eligible expert to an EVC inspection committee.
 * Enforces uniqueness so an expert cannot be assigned multiple times to the same inspection.
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CommitteeAssignment that)) return false;
        return Objects.equals(inspection != null ? inspection.getId() : null, that.inspection != null ? that.inspection.getId() : null) &&
               Objects.equals(expert != null ? expert.getId() : null, that.expert != null ? that.expert.getId() : null);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            inspection != null ? inspection.getId() : null,
            expert != null ? expert.getId() : null
        );
    }

    @Override
    public String toString() {
        return "CommitteeAssignment{" +
                "id=" + id +
                ", role=" + role +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }
}
