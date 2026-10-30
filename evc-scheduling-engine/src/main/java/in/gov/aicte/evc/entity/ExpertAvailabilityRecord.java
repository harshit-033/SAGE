package in.gov.aicte.evc.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Normalized persistence entity representing an individual calendar date on which
 * an expert is available for EVC inspection committee assignment.
 * Enforces uniqueness so an expert cannot have duplicate availability records for the same date.
 */
@Entity
@Table(
    name = "expert_availabilities",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_expert_available_date",
        columnNames = {"expert_id", "available_date"}
    ),
    indexes = {
        @Index(name = "idx_availabilities_expert", columnList = "expert_id"),
        @Index(name = "idx_availabilities_date", columnList = "available_date"),
        @Index(name = "idx_availabilities_expert_date", columnList = "expert_id, available_date")
    }
)
public class ExpertAvailabilityRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Expert is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "expert_id", nullable = false)
    private Expert expert;

    @NotNull(message = "Available date is required")
    @Column(name = "available_date", nullable = false)
    private LocalDate availableDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public ExpertAvailabilityRecord() {
    }

    public ExpertAvailabilityRecord(Expert expert, LocalDate availableDate) {
        this.expert = expert;
        this.availableDate = availableDate;
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

    public Expert getExpert() {
        return expert;
    }

    public void setExpert(Expert expert) {
        this.expert = expert;
    }

    public LocalDate getAvailableDate() {
        return availableDate;
    }

    public void setAvailableDate(LocalDate availableDate) {
        this.availableDate = availableDate;
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
        if (!(o instanceof ExpertAvailabilityRecord that)) return false;
        return Objects.equals(expert != null ? expert.getId() : null, that.expert != null ? that.expert.getId() : null) &&
               Objects.equals(availableDate, that.availableDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            expert != null ? expert.getId() : null,
            availableDate
        );
    }

    @Override
    public String toString() {
        return "ExpertAvailabilityRecord{" +
                "id=" + id +
                ", expertId=" + (expert != null ? expert.getId() : null) +
                ", availableDate=" + availableDate +
                ", createdAt=" + createdAt +
                '}';
    }
}

