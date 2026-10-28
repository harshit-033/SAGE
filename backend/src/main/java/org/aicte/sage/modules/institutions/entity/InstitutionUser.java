package org.aicte.sage.modules.institutions.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "institution_users",
        uniqueConstraints = @UniqueConstraint(columnNames = {"institution_id", "user_id"})
)
public class InstitutionUser {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "institution_id", nullable = false)
    private UUID institutionId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "role_in_institution", nullable = false, length = 64)
    private String roleInInstitution;

    @Column(name = "is_primary_contact", nullable = false)
    private boolean primaryContact = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public InstitutionUser() {}

    public InstitutionUser(UUID institutionId, UUID userId, String roleInInstitution, boolean primaryContact) {
        this.institutionId = institutionId;
        this.userId = userId;
        this.roleInInstitution = roleInInstitution;
        this.primaryContact = primaryContact;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getInstitutionId() {
        return institutionId;
    }

    public void setInstitutionId(UUID institutionId) {
        this.institutionId = institutionId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getRoleInInstitution() {
        return roleInInstitution;
    }

    public void setRoleInInstitution(String roleInInstitution) {
        this.roleInInstitution = roleInInstitution;
    }

    public boolean isPrimaryContact() {
        return primaryContact;
    }

    public void setPrimaryContact(boolean primaryContact) {
        this.primaryContact = primaryContact;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
