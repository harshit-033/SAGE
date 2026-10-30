package org.aicte.sage.modules.evc.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.aicte.sage.modules.institutions.entity.Institution;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Accredited peer review expert eligible for appointment to EVC physical/virtual inspection committees.
 * Models conflict-of-interest isolation via affiliatedInstitution.
 */
@Entity
@Table(
    name = "experts",
    indexes = {
        @Index(name = "idx_experts_discipline", columnList = "discipline"),
        @Index(name = "idx_experts_active", columnList = "active")
    }
)
public class Expert {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @NotBlank(message = "Expert name is required")
    @Size(max = 150, message = "Expert name must not exceed 150 characters")
    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Size(max = 150, message = "Email must not exceed 150 characters")
    @Column(name = "email", length = 150)
    private String email;

    @NotBlank(message = "Discipline is required")
    @Size(max = 100, message = "Discipline must not exceed 100 characters")
    @Column(name = "discipline", nullable = false, length = 100)
    private String discipline;

    @Size(max = 150, message = "Designation must not exceed 150 characters")
    @Column(name = "designation", length = 150)
    private String designation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "affiliated_institution_id")
    private Institution affiliatedInstitution;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Expert() {
    }

    public Expert(String name, String email, String discipline, String designation, boolean active) {
        this.name = name;
        this.email = email;
        this.discipline = discipline;
        this.designation = designation;
        this.active = active;
    }

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDiscipline() {
        return discipline;
    }

    public void setDiscipline(String discipline) {
        this.discipline = discipline;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public Institution getAffiliatedInstitution() {
        return affiliatedInstitution;
    }

    public void setAffiliatedInstitution(Institution affiliatedInstitution) {
        this.affiliatedInstitution = affiliatedInstitution;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Expert expert = (Expert) o;
        return Objects.equals(id, expert.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
