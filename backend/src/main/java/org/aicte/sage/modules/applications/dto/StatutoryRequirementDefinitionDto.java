package org.aicte.sage.modules.applications.dto;

import org.aicte.sage.modules.documents.model.DocumentType;

public record StatutoryRequirementDefinitionDto(
        DocumentType documentType,
        String displayName,
        String description,
        String aphClause,
        boolean isMandatory
) {}
