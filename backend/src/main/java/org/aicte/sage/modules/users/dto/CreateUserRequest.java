package org.aicte.sage.modules.users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.Set;
import java.util.UUID;

public class CreateUserRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 64, message = "Username must be between 3 and 64 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 128)
    private String email;

    @NotBlank(message = "Full name is required")
    @Size(max = 255)
    private String fullName;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 128, message = "Password must be at least 8 characters long")
    private String password;

    @NotEmpty(message = "At least one role must be assigned")
    private Set<String> roles;

    private UUID institutionId;

    public CreateUserRequest() {}

    public CreateUserRequest(String username, String email, String fullName, String password,
                             Set<String> roles, UUID institutionId) {
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.password = password;
        this.roles = roles;
        this.institutionId = institutionId;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public UUID getInstitutionId() {
        return institutionId;
    }

    public void setInstitutionId(UUID institutionId) {
        this.institutionId = institutionId;
    }
}
