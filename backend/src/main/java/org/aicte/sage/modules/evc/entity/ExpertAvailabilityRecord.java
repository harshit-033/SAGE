package org.aicte.sage.modules.evc.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Normalized entity recording an expert's calendar availability for a single date.
 */
@Entity
@Table(
    name = "expert_availabilities",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_expert_available_date",
        columnNames = {"expert_id", "available_date"}
    ),
    indexes = {
        @Index(name = "idx_expert_availabilities_expert", columnList = "expert_id"),
        @Index(name = "idx_expert_availabilities_date", columnList = "available_date"),
        @Index(name = "idx_expert_availabilities_expert_date", columnList = "expert_id, available_date")
    }
)
public class ExpertAvailabilityRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExpertAvailabilityRecord that = (ExpertAvailabilityRecord) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
