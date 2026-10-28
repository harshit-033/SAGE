package org.aicte.sage.modules.auth.dto;

import java.util.List;
import java.util.UUID;

public class UserProfileDto {
    private UUID id;
    private String username;
    private String email;
    private String fullName;
    private List<String> roles;
    private List<UUID> institutions;

    public UserProfileDto() {}

    public UserProfileDto(UUID id, String username, String email, String fullName,
                          List<String> roles, List<UUID> institutions) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.roles = roles;
        this.institutions = institutions;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public List<UUID> getInstitutions() {
        return institutions;
    }

    public void setInstitutions(List<UUID> institutions) {
        this.institutions = institutions;
    }
}
