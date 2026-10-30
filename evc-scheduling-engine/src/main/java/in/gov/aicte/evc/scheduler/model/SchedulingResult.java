package in.gov.aicte.evc.scheduler.model;

import java.util.*;

/**
 * Result returned by the CP-SAT scheduling engine encapsulating status, scheduled assignments,
 * and diagnostic explanation when infeasible or in error.
 */
public final class SchedulingResult {

    private final SchedulingStatus status;
    private final Map<String, ScheduledInspection> scheduledInspections;
    private final String explanation;

    public SchedulingResult(SchedulingStatus status,
                            Map<String, ScheduledInspection> scheduledInspections,
                            String explanation) {
        this.status = Objects.requireNonNull(status, "Status must not be null");
        this.scheduledInspections = scheduledInspections != null
                ? Collections.unmodifiableMap(new LinkedHashMap<>(scheduledInspections))
                : Collections.emptyMap();
        this.explanation = explanation != null ? explanation : "";
    }

    public static SchedulingResult success(SchedulingStatus status, Map<String, ScheduledInspection> scheduledInspections) {
        if (status != SchedulingStatus.OPTIMAL && status != SchedulingStatus.FEASIBLE) {
            throw new IllegalArgumentException("Success status must be OPTIMAL or FEASIBLE, got: " + status);
        }
        return new SchedulingResult(status, scheduledInspections, "Schedule successfully generated");
    }

    public static SchedulingResult infeasible(String explanation) {
        return new SchedulingResult(SchedulingStatus.INFEASIBLE, Collections.emptyMap(), explanation);
    }

    public static SchedulingResult timeLimitReached(String explanation) {
        return new SchedulingResult(SchedulingStatus.TIME_LIMIT_REACHED, Collections.emptyMap(), explanation);
    }

    public static SchedulingResult invalidInput(String explanation) {
        return new SchedulingResult(SchedulingStatus.INVALID_INPUT, Collections.emptyMap(), explanation);
    }

    public static SchedulingResult error(String explanation) {
        return new SchedulingResult(SchedulingStatus.ERROR, Collections.emptyMap(), explanation);
    }

    public boolean isSuccess() {
        return status == SchedulingStatus.OPTIMAL || status == SchedulingStatus.FEASIBLE;
    }

    public SchedulingStatus getStatus() {
        return status;
    }

    public Map<String, ScheduledInspection> getScheduledInspections() {
        return scheduledInspections;
    }

    public ScheduledInspection getScheduledInspection(String inspectionId) {
        return scheduledInspections.get(inspectionId);
    }

    public String getExplanation() {
        return explanation;
    }

    @Override
    public String toString() {
        return "SchedulingResult{" +
                "status=" + status +
                ", scheduledCount=" + scheduledInspections.size() +
                ", explanation='" + explanation + '\'' +
                '}';
    }
}

