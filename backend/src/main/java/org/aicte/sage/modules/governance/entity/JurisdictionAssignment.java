package org.aicte.sage.modules.governance.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "jurisdiction_assignments")
public class JurisdictionAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 64)
    private String region;

    @Column(nullable = false, length = 100)
    private String state;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "valid_from", nullable = false)
    private Instant validFrom = Instant.now();

    @Column(name = "valid_until")
    private Instant validUntil;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public JurisdictionAssignment() {}

    public JurisdictionAssignment(UUID userId, String region, String state, Instant validFrom, Instant validUntil) {
        this.userId = userId;
        this.region = region;
        this.state = state;
        this.validFrom = validFrom != null ? validFrom : Instant.now();
        this.validUntil = validUntil;
        this.active = true;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Instant getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(Instant validFrom) {
        this.validFrom = validFrom;
    }

    public Instant getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(Instant validUntil) {
        this.validUntil = validUntil;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public boolean isCurrentlyValid() {
        if (!active) return false;
        Instant now = Instant.now();
        if (validFrom != null && now.isBefore(validFrom)) return false;
        return validUntil == null || !now.isAfter(validUntil);
    }
}
