package org.aicte.sage.modules.governance.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "committee_appointments")
public class CommitteeAppointment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "committee_type", nullable = false, length = 64)
    private String committeeType;

    @Column(name = "role_in_committee", nullable = false, length = 64)
    private String roleInCommittee;

    @Column(name = "qualification_details", length = 255)
    private String qualificationDetails;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "valid_from", nullable = false)
    private Instant validFrom = Instant.now();

    @Column(name = "valid_until", nullable = false)
    private Instant validUntil;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public CommitteeAppointment() {}

    public CommitteeAppointment(UUID userId, String committeeType, String roleInCommittee,
                                String qualificationDetails, Instant validFrom, Instant validUntil) {
        this.userId = userId;
        this.committeeType = committeeType;
        this.roleInCommittee = roleInCommittee;
        this.qualificationDetails = qualificationDetails;
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

    public String getCommitteeType() {
        return committeeType;
    }

    public void setCommitteeType(String committeeType) {
        this.committeeType = committeeType;
    }

    public String getRoleInCommittee() {
        return roleInCommittee;
    }

    public void setRoleInCommittee(String roleInCommittee) {
        this.roleInCommittee = roleInCommittee;
    }

    public String getQualificationDetails() {
        return qualificationDetails;
    }

    public void setQualificationDetails(String qualificationDetails) {
        this.qualificationDetails = qualificationDetails;
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
        return validUntil != null && !now.isAfter(validUntil);
    }
}
