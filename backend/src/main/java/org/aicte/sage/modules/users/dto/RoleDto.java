package org.aicte.sage.modules.users.dto;

import org.aicte.sage.modules.users.entity.Role;

import java.util.UUID;

public record RoleDto(
        UUID id,
        String code,
        String name,
        String description
) {
    public static RoleDto fromEntity(Role role) {
        if (role == null) return null;
        return new RoleDto(
                role.getId(),
                role.getCode(),
                role.getName(),
                role.getDescription()
        );
    }
}
