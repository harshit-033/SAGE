package org.aicte.sage.modules.documents.dto;

import org.aicte.sage.modules.documents.model.CorrectiveActionStatus;
import org.aicte.sage.modules.documents.model.DocumentCorrectiveAction;

import java.time.Instant;
import java.util.UUID;

public record DocumentCorrectiveActionDto(
        UUID id,
        UUID institutionId,
        String institutionName,
        UUID applicationId,
        String applicationNumber,
        UUID documentId,
        String documentName,
        String documentType,
        UUID findingId,
        String actionType,
        String description,
        String priority,
        String responsibleParty,
        Instant dueDate,
        CorrectiveActionStatus status,
        String institutionResponse,
        UUID resolutionEvidenceDocId,
        String resolutionEvidenceDocName,
        String officerNotes,
        String resolvedByName,
        Instant resolvedAt,
        Instant createdAt,
        Instant updatedAt
) {
    public static DocumentCorrectiveActionDto fromEntity(DocumentCorrectiveAction action) {
        if (action == null) return null;
        return new DocumentCorrectiveActionDto(
                action.getId(),
                action.getInstitution() != null ? action.getInstitution().getId() : null,
                action.getInstitution() != null ? action.getInstitution().getName() : null,
                action.getApplication() != null ? action.getApplication().getId() : null,
                action.getApplication() != null ? action.getApplication().getApplicationNumber() : null,
                action.getDocument() != null ? action.getDocument().getId() : null,
                action.getDocument() != null ? action.getDocument().getDocumentName() : null,
                action.getDocument() != null ? action.getDocument().getDocumentType().name() : null,
                action.getFindingId(),
                action.getActionType(),
                action.getDescription(),
                action.getPriority(),
                action.getResponsibleParty(),
                action.getDueDate(),
                action.getStatus(),
                action.getInstitutionResponse(),
                action.getResolutionEvidenceDoc() != null ? action.getResolutionEvidenceDoc().getId() : null,
                action.getResolutionEvidenceDoc() != null ? action.getResolutionEvidenceDoc().getDocumentName() : null,
                action.getOfficerNotes(),
                action.getResolvedBy() != null ? action.getResolvedBy().getFullName() : null,
                action.getResolvedAt(),
                action.getCreatedAt(),
                action.getUpdatedAt()
        );
    }
}
