package org.aicte.sage.modules.evc.scheduler.model;

import java.time.Duration;
import java.util.*;

/**
 * Encapsulates the complete scheduling problem instance submitted to the CP-SAT engine.
 */
public final class SchedulingProblem {

    private final List<InspectionRequest> inspections;
    private final Map<String, ExpertAvailability> expertAvailabilities;
    private final Duration timeLimit;

    public SchedulingProblem(Collection<InspectionRequest> inspections,
                             Map<String, ExpertAvailability> expertAvailabilities,
                             Duration timeLimit) {
        if (inspections == null || inspections.isEmpty()) {
            throw new IllegalArgumentException("Inspections list must not be null or empty");
        }

        // Validate unique inspection IDs
        Set<String> seenIds = new HashSet<>();
        for (InspectionRequest req : inspections) {
            if (!seenIds.add(req.getInspectionId())) {
                throw new IllegalArgumentException("Duplicate inspection ID detected: " + req.getInspectionId());
            }
        }

        this.inspections = Collections.unmodifiableList(new ArrayList<>(inspections));
        this.expertAvailabilities = expertAvailabilities != null
                ? Collections.unmodifiableMap(new LinkedHashMap<>(expertAvailabilities))
                : Collections.emptyMap();
        this.timeLimit = timeLimit != null ? timeLimit : Duration.ofSeconds(10);
    }

    public static SchedulingProblem of(Collection<InspectionRequest> inspections,
                                       Map<String, ExpertAvailability> expertAvailabilities) {
        return new SchedulingProblem(inspections, expertAvailabilities, Duration.ofSeconds(10));
    }

    public List<InspectionRequest> getInspections() {
        return inspections;
    }

    public Map<String, ExpertAvailability> getExpertAvailabilities() {
        return expertAvailabilities;
    }

    public ExpertAvailability getExpertAvailability(String expertId) {
        return expertAvailabilities.get(expertId);
    }

    public Duration getTimeLimit() {
        return timeLimit;
    }

    @Override
    public String toString() {
        return "SchedulingProblem{" +
                "inspectionsCount=" + inspections.size() +
                ", expertsCount=" + expertAvailabilities.size() +
                ", timeLimit=" + timeLimit +
                '}';
    }
}
