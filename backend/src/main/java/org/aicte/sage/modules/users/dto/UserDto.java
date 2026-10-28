package org.aicte.sage.modules.users.dto;

import org.aicte.sage.modules.users.entity.Role;
import org.aicte.sage.modules.users.entity.User;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class UserDto {
    private UUID id;
    private String username;
    private String email;
    private String fullName;
    private boolean active;
    private List<String> roles = new ArrayList<>();
    private List<UUID> institutionIds = new ArrayList<>();
    private Instant createdAt;
    private Instant updatedAt;

    public UserDto() {}

    public UserDto(UUID id, String username, String email, String fullName, boolean active,
                   List<String> roles, List<UUID> institutionIds, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.active = active;
        this.roles = roles != null ? roles : new ArrayList<>();
        this.institutionIds = institutionIds != null ? institutionIds : new ArrayList<>();
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static UserDto fromEntity(User user, List<UUID> institutionIds) {
        List<String> roleCodes = user.getRoles().stream()
                .map(Role::getCode)
                .collect(Collectors.toList());

        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.isActive(),
                roleCodes,
                institutionIds,
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public List<UUID> getInstitutionIds() {
        return institutionIds;
    }

    public void setInstitutionIds(List<UUID> institutionIds) {
        this.institutionIds = institutionIds;
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
}
