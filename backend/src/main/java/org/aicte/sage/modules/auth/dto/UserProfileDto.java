package org.aicte.sage.modules.auth.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserProfileDto {
    private UUID id;
    private String username;
    private String email;
    private String fullName;
    private List<String> roles = new ArrayList<>();
    private List<String> permissions = new ArrayList<>();
    private List<UUID> institutions = new ArrayList<>();

    public UserProfileDto() {}

    public UserProfileDto(UUID id, String username, String email, String fullName,
                          List<String> roles, List<UUID> institutions) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.roles = roles != null ? roles : new ArrayList<>();
        this.institutions = institutions != null ? institutions : new ArrayList<>();
    }

    public UserProfileDto(UUID id, String username, String email, String fullName,
                          List<String> roles, List<String> permissions, List<UUID> institutions) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.roles = roles != null ? roles : new ArrayList<>();
        this.permissions = permissions != null ? permissions : new ArrayList<>();
        this.institutions = institutions != null ? institutions : new ArrayList<>();
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

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }

    public List<UUID> getInstitutions() {
        return institutions;
    }

    public void setInstitutions(List<UUID> institutions) {
        this.institutions = institutions;
    }
}
