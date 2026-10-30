package in.gov.aicte.evc.service.dto;

import in.gov.aicte.evc.scheduler.model.SchedulingStatus;

import java.util.Collections;
import java.util.List;

/**
 * Encapsulates the application-level outcome of a scheduling orchestration execution,
 * including solver status, persistence status, diagnostics, and scheduled committee details.
 */
public class SchedulingServiceResult {

    private final SchedulingStatus status;
    private final boolean success;
    private final String explanation;
    private final int totalInspectionsRequested;
    private final int totalInspectionsScheduled;
    private final long solveDurationMs;
    private final boolean persisted;
    private final List<ScheduledInspectionResultDto> scheduledInspections;

    public SchedulingServiceResult(SchedulingStatus status,
                                   boolean success,
                                   String explanation,
                                   int totalInspectionsRequested,
                                   int totalInspectionsScheduled,
                                   long solveDurationMs,
                                   boolean persisted,
                                   List<ScheduledInspectionResultDto> scheduledInspections) {
        this.status = status;
        this.success = success;
        this.explanation = explanation;
        this.totalInspectionsRequested = totalInspectionsRequested;
        this.totalInspectionsScheduled = totalInspectionsScheduled;
        this.solveDurationMs = solveDurationMs;
        this.persisted = persisted;
        this.scheduledInspections = scheduledInspections != null
                ? Collections.unmodifiableList(scheduledInspections)
                : Collections.emptyList();
    }

    public static SchedulingServiceResult success(SchedulingStatus status,
                                                  int totalRequested,
                                                  long solveDurationMs,
                                                  List<ScheduledInspectionResultDto> scheduledInspections) {
        return new SchedulingServiceResult(
                status,
                true,
                "Schedule successfully generated and persisted",
                totalRequested,
                scheduledInspections.size(),
                solveDurationMs,
                true,
                scheduledInspections
        );
    }

    public static SchedulingServiceResult infeasible(String explanation, int totalRequested, long solveDurationMs) {
        return new SchedulingServiceResult(
                SchedulingStatus.INFEASIBLE,
                false,
                explanation,
                totalRequested,
                0,
                solveDurationMs,
                false,
                Collections.emptyList()
        );
    }

    public static SchedulingServiceResult invalidInput(String message) {
        return new SchedulingServiceResult(
                SchedulingStatus.INVALID_INPUT,
                false,
                message,
                0,
                0,
                0,
                false,
                Collections.emptyList()
        );
    }

    public static SchedulingServiceResult timeLimitReached(String explanation, int totalRequested, long solveDurationMs) {
        return new SchedulingServiceResult(
                SchedulingStatus.TIME_LIMIT_REACHED,
                false,
                explanation,
                totalRequested,
                0,
                solveDurationMs,
                false,
                Collections.emptyList()
        );
    }

    public static SchedulingServiceResult error(String message, int totalRequested, long solveDurationMs) {
        return new SchedulingServiceResult(
                SchedulingStatus.ERROR,
                false,
                message,
                totalRequested,
                0,
                solveDurationMs,
                false,
                Collections.emptyList()
        );
    }

    public SchedulingStatus getStatus() {
        return status;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getExplanation() {
        return explanation;
    }

    public int totalInspectionsRequested() {
        return totalInspectionsRequested;
    }

    public int totalInspectionsScheduled() {
        return totalInspectionsScheduled;
    }

    public long getSolveDurationMs() {
        return solveDurationMs;
    }

    public boolean isPersisted() {
        return persisted;
    }

    public List<ScheduledInspectionResultDto> getScheduledInspections() {
        return scheduledInspections;
    }

    @Override
    public String toString() {
        return "SchedulingServiceResult{" +
                "status=" + status +
                ", success=" + success +
                ", persisted=" + persisted +
                ", totalRequested=" + totalInspectionsRequested +
                ", totalScheduled=" + totalInspectionsScheduled +
                ", solveDurationMs=" + solveDurationMs +
                ", explanation='" + explanation + '\'' +
                '}';
    }
}

