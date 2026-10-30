package org.aicte.sage.common.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

public class UserPrincipal implements UserDetails {

    private final UUID id;
    private final String username;
    private final String email;
    private final String password;
    private final String fullName;
    private final boolean active;
    private final Collection<? extends GrantedAuthority> authorities;
    private final Set<UUID> authorizedInstitutionIds;

    public UserPrincipal(UUID id, String username, String email, String password, String fullName,
                         boolean active, Collection<? extends GrantedAuthority> authorities,
                         Set<UUID> authorizedInstitutionIds) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.active = active;
        this.authorities = authorities;
        this.authorizedInstitutionIds = authorizedInstitutionIds != null ? authorizedInstitutionIds : Collections.emptySet();
    }

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public Set<UUID> getAuthorizedInstitutionIds() {
        return authorizedInstitutionIds;
    }

    public boolean hasRole(String roleName) {
        String checkRole = roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName;
        return authorities.stream().anyMatch(a -> a.getAuthority().equals(checkRole));
    }

    public boolean hasAnyRole(String... roleNames) {
        return Arrays.stream(roleNames).anyMatch(this::hasRole);
    }

    public boolean hasAuthority(String authority) {
        return authorities.stream().anyMatch(a -> a.getAuthority().equals(authority));
    }

    public boolean isAuthorizedForInstitution(UUID institutionId) {
        if (institutionId == null) {
            return false;
        }
        if (hasRole("ROLE_AICTE_ADMIN")) {
            return true;
        }
        return authorizedInstitutionIds.contains(institutionId);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}
