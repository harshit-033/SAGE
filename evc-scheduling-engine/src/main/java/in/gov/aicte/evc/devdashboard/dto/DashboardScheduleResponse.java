package in.gov.aicte.evc.devdashboard.dto;

import in.gov.aicte.evc.scheduler.model.SchedulingStatus;

import java.util.List;

/**
 * Response payload returned by the dev dashboard scheduling API.
 */
public class DashboardScheduleResponse {

    private SchedulingStatus status;
    private boolean success;
    private String explanation;
    private int totalInspectionsRequested;
    private int totalAssignmentsRequested;
    private int totalInspectionsScheduled;
    private long solveDurationMs;
    private List<DashboardScheduledInspectionDto> scheduledInspections;

    public DashboardScheduleResponse() {
    }

    public DashboardScheduleResponse(SchedulingStatus status,
                                     boolean success,
                                     String explanation,
                                     int totalInspectionsRequested,
                                     int totalAssignmentsRequested,
                                     int totalInspectionsScheduled,
                                     long solveDurationMs,
                                     List<DashboardScheduledInspectionDto> scheduledInspections) {
        this.status = status;
        this.success = success;
        this.explanation = explanation;
        this.totalInspectionsRequested = totalInspectionsRequested;
        this.totalAssignmentsRequested = totalAssignmentsRequested;
        this.totalInspectionsScheduled = totalInspectionsScheduled;
        this.solveDurationMs = solveDurationMs;
        this.scheduledInspections = scheduledInspections;
    }

    public SchedulingStatus getStatus() {
        return status;
    }

    public void setStatus(SchedulingStatus status) {
        this.status = status;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public int getTotalInspectionsRequested() {
        return totalInspectionsRequested;
    }

    public void setTotalInspectionsRequested(int totalInspectionsRequested) {
        this.totalInspectionsRequested = totalInspectionsRequested;
    }

    public int getTotalAssignmentsRequested() {
        return totalAssignmentsRequested;
    }

    public void setTotalAssignmentsRequested(int totalAssignmentsRequested) {
        this.totalAssignmentsRequested = totalAssignmentsRequested;
    }

    public int getTotalInspectionsScheduled() {
        return totalInspectionsScheduled;
    }

    public void setTotalInspectionsScheduled(int totalInspectionsScheduled) {
        this.totalInspectionsScheduled = totalInspectionsScheduled;
    }

    public long getSolveDurationMs() {
        return solveDurationMs;
    }

    public void setSolveDurationMs(long solveDurationMs) {
        this.solveDurationMs = solveDurationMs;
    }

    public List<DashboardScheduledInspectionDto> getScheduledInspections() {
        return scheduledInspections;
    }

    public void setScheduledInspections(List<DashboardScheduledInspectionDto> scheduledInspections) {
        this.scheduledInspections = scheduledInspections;
    }
}

