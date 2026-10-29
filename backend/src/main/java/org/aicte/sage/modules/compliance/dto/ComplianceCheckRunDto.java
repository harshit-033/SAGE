package org.aicte.sage.modules.compliance.dto;

import org.aicte.sage.modules.compliance.entity.ComplianceCheckRun;

import java.time.Instant;
import java.util.UUID;

public record ComplianceCheckRunDto(
        UUID id,
        String runType,
        String triggerSource,
        UUID institutionId,
        String institutionName,
        Instant startedAt,
        Instant completedAt,
        String status,
        int institutionsChecked,
        int findingsGenerated,
        int alertsCreated,
        int deadlinesCreated,
        String summaryNotes,
        String createdByName
) {
    public static ComplianceCheckRunDto fromEntity(ComplianceCheckRun r) {
        return new ComplianceCheckRunDto(
                r.getId(),
                r.getRunType(),
                r.getTriggerSource(),
                r.getInstitution() != null ? r.getInstitution().getId() : null,
                r.getInstitution() != null ? r.getInstitution().getName() : null,
                r.getStartedAt(),
                r.getCompletedAt(),
                r.getStatus(),
                r.getInstitutionsChecked(),
                r.getFindingsGenerated(),
                r.getAlertsCreated(),
                r.getDeadlinesCreated(),
                r.getSummaryNotes(),
                r.getCreatedByUser() != null ? r.getCreatedByUser().getFullName() : null
        );
    }
}
