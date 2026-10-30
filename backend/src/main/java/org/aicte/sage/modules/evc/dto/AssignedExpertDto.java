package org.aicte.sage.modules.evc.dto;

import org.aicte.sage.modules.evc.entity.AssignmentRole;
import org.aicte.sage.modules.evc.entity.AssignmentStatus;

import java.util.UUID;

public record AssignedExpertDto(
        UUID id,
        String name,
        String discipline,
        AssignmentRole role,
        AssignmentStatus status
) {}
