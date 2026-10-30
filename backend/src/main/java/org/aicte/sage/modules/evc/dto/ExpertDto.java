package org.aicte.sage.modules.evc.dto;

import org.aicte.sage.modules.evc.entity.Expert;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

public record ExpertDto(
        UUID id,
        String name,
        String email,
        String discipline,
        String designation,
        UUID affiliatedInstitutionId,
        String affiliatedInstitutionName,
        boolean active,
        Set<LocalDate> availableDates
) {
    public static ExpertDto fromEntity(Expert expert, Set<LocalDate> availableDates) {
        UUID affilId = expert.getAffiliatedInstitution() != null ? expert.getAffiliatedInstitution().getId() : null;
        String affilName = expert.getAffiliatedInstitution() != null ? expert.getAffiliatedInstitution().getName() : null;

        return new ExpertDto(
                expert.getId(),
                expert.getName(),
                expert.getEmail(),
                expert.getDiscipline(),
                expert.getDesignation(),
                affilId,
                affilName,
                expert.isActive(),
                availableDates != null ? availableDates : Collections.emptySet()
        );
    }
}
