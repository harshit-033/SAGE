package org.aicte.sage.modules.applications.dto;

import org.aicte.sage.modules.applications.model.ApplicationCategory;
import org.aicte.sage.modules.applications.model.ApplicationState;
import org.aicte.sage.modules.documents.model.DocumentType;

import java.util.List;
import java.util.UUID;

public record SubmissionReadinessDto(
        UUID applicationId,
        String applicationNumber,
        ApplicationCategory category,
        String categoryDisplayName,
        ApplicationState currentState,
        boolean canSubmit,
        int totalRequired,
        int totalUploaded,
        List<String> missingDocumentTypes,
        List<String> missingDocumentNames,
        List<String> highRiskDocumentNames,
        List<RequiredDocumentItemDto> checklist,
        String blockingReason
) {
    public record RequiredDocumentItemDto(
            DocumentType documentType,
            String displayName,
            String description,
            String aphClause,
            boolean isMandatory,
            boolean isUploaded,
            UUID documentId,
            String documentName,
            String originalFilename,
            String forgeryRisk, // "LOW", "MEDIUM", "HIGH"
            boolean isHighRisk,
            String complianceStatus, // "COMPLIANT", "EXPIRING_SOON", "EXPIRED", etc.
            String uploadedAt
    ) {}
}
