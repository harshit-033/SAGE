package in.gov.aicte.evc.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents an educational institution requiring an EVC inspection.
 */
@Entity
@Table(
    name = "institutions",
    uniqueConstraints = @UniqueConstraint(name = "uk_institutions_aicte_id", columnNames = "aicte_id")
)
public class Institution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "AICTE identifier is required")
    @Size(max = 50, message = "AICTE identifier must not exceed 50 characters")
    @Column(name = "aicte_id", nullable = false, unique = true, length = 50)
    private String aicteId;

    @NotBlank(message = "Institution name is required")
    @Size(max = 255, message = "Institution name must not exceed 255 characters")
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City must not exceed 100 characters")
    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @NotBlank(message = "State is required")
    @Size(max = 100, message = "State must not exceed 100 characters")
    @Column(name = "state", nullable = false, length = 100)
    private String state;

    @NotBlank(message = "Region is required")
    @Size(max = 50, message = "Region must not exceed 50 characters")
    @Column(name = "region", nullable = false, length = 50)
    private String region;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Institution() {
    }

    public Institution(String aicteId, String name, String city, String state, String region) {
        this.aicteId = aicteId;
        this.name = name;
        this.city = city;
        this.state = state;
        this.region = region;
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

    public String getAicteId() {
        return aicteId;
    }

    public void setAicteId(String aicteId) {
        this.aicteId = aicteId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
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
        if (!(o instanceof Institution that)) return false;
        return aicteId != null && Objects.equals(aicteId, that.aicteId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(aicteId);
    }

    @Override
    public String toString() {
        return "Institution{" +
                "id=" + id +
                ", aicteId='" + aicteId + '\'' +
                ", name='" + name + '\'' +
                ", city='" + city + '\'' +
                ", state='" + state + '\'' +
                ", region='" + region + '\'' +
                '}';
    }
}
