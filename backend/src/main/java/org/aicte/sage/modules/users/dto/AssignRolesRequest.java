package org.aicte.sage.modules.users.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public class AssignRolesRequest {

    @NotEmpty(message = "At least one role code must be specified")
    private Set<String> roles;

    public AssignRolesRequest() {}

    public AssignRolesRequest(Set<String> roles) {
        this.roles = roles;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }
}
