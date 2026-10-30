package in.gov.aicte.evc.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents an individual peer evaluator or specialist eligible to serve on an EVC committee.
 * Experts are assigned dynamically to individual inspections based on discipline and availability.
 */
@Entity
@Table(name = "experts")
public class Expert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Expert() {
    }

    public Expert(String name, String email, String discipline, boolean active) {
        this.name = name;
        this.email = email;
        this.discipline = discipline;
        this.active = active;
    }

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Expert expert)) return false;
        return id != null && Objects.equals(id, expert.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Expert{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", discipline='" + discipline + '\'' +
                ", active=" + active +
                '}';
    }
}
