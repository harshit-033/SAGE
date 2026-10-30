package org.aicte.sage.modules.documents.dto;

import org.aicte.sage.modules.scrutiny.dto.ScrutinyFindingDto;

import java.util.List;

public record DocumentDetailResponse(
        DocumentDto document,
        List<ExtractedFieldDto> extractedFields,
        List<ScrutinyFindingDto> findings,
        List<DocumentCorrectiveActionDto> correctiveActions,
        List<DocumentDto> versions
) {}
