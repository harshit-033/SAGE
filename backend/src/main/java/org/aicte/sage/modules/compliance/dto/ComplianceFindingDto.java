package org.aicte.sage.modules.compliance.dto;

import org.aicte.sage.modules.compliance.entity.ComplianceFinding;
import org.aicte.sage.modules.compliance.entity.ComplianceFindingStatus;
import org.aicte.sage.modules.compliance.entity.FindingType;
import org.aicte.sage.modules.notifications.entity.AlertSeverity;

import java.time.Instant;
import java.util.UUID;

public record ComplianceFindingDto(
        UUID id,
        UUID checkRunId,
        UUID institutionId,
        String institutionName,
        UUID applicationId,
        UUID documentId,
        String ruleCode,
        String aphClause,
        FindingType findingType,
        AlertSeverity severity,
        String title,
        String description,
        String evidenceSummary,
        ComplianceFindingStatus status,
        Instant resolvedAt,
        String resolvedByName,
        String resolutionNotes,
        Instant createdAt,
        Instant updatedAt
) {
    public static ComplianceFindingDto fromEntity(ComplianceFinding f) {
        return new ComplianceFindingDto(
                f.getId(),
                f.getCheckRun() != null ? f.getCheckRun().getId() : null,
                f.getInstitution() != null ? f.getInstitution().getId() : null,
                f.getInstitution() != null ? f.getInstitution().getName() : null,
                f.getApplication() != null ? f.getApplication().getId() : null,
                f.getDocument() != null ? f.getDocument().getId() : null,
                f.getRuleCode(),
                f.getAphClause(),
                f.getFindingType(),
                f.getSeverity(),
                f.getTitle(),
                f.getDescription(),
                f.getEvidenceSummary(),
                f.getStatus(),
                f.getResolvedAt(),
                f.getResolvedByUser() != null ? f.getResolvedByUser().getFullName() : null,
                f.getResolutionNotes(),
                f.getCreatedAt(),
                f.getUpdatedAt()
        );
    }
}
