package in.gov.aicte.evc.api.dto;

import in.gov.aicte.evc.scheduler.model.SchedulingStatus;

import java.util.List;

/**
 * Top-level response returned to SAGE when submitting a batch scheduling request.
 */
public record ScheduleApiResponse(
        String requestId,
        SchedulingStatus status,
        boolean success,
        boolean persisted,
        String explanation,
        int totalInspectionsRequested,
        int totalInspectionsScheduled,
        long solveDurationMs,
        List<ScheduledInspectionApiDto> scheduledInspections
) {
}

