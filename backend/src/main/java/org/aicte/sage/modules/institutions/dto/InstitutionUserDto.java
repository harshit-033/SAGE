package org.aicte.sage.modules.institutions.dto;

import java.time.Instant;
import java.util.UUID;

public class InstitutionUserDto {
    private UUID id;
    private UUID institutionId;
    private UUID userId;
    private String username;
    private String email;
    private String fullName;
    private String roleInInstitution;
    private boolean primaryContact;
    private Instant createdAt;

    public InstitutionUserDto() {}

    public InstitutionUserDto(UUID id, UUID institutionId, UUID userId, String username,
                              String email, String fullName, String roleInInstitution,
                              boolean primaryContact, Instant createdAt) {
        this.id = id;
        this.institutionId = institutionId;
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.roleInInstitution = roleInInstitution;
        this.primaryContact = primaryContact;
        this.createdAt = createdAt;
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
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
