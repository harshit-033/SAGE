package org.aicte.sage.modules.institutions.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public class AddInstitutionUserRequest {

    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotBlank(message = "Role in institution is required (e.g. DIRECTOR, REGISTRAR, NODAL_OFFICER)")
    @Size(max = 64)
    private String roleInInstitution;

    private boolean primaryContact = false;

    public AddInstitutionUserRequest() {}

    public AddInstitutionUserRequest(UUID userId, String roleInInstitution, boolean primaryContact) {
        this.userId = userId;
        this.roleInInstitution = roleInInstitution;
        this.primaryContact = primaryContact;
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
}
