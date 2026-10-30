package in.gov.aicte.evc.service.dto;

import java.time.Duration;
import java.time.LocalDate;
import java.util.*;

/**
 * Encapsulates an application-level scheduling request.
 * Identifies the persisted inspections to schedule and optional constraints or overrides.
 */
public class SchedulingRequest {

    private final List<Long> inspectionIds;
    private final Map<Long, Set<Long>> eligibleExpertIdsByInspectionId;
    private final Map<Long, Set<LocalDate>> expertAvailabilityOverrides;
    private final Duration timeLimit;
    private final boolean allowRescheduling;

    public SchedulingRequest(List<Long> inspectionIds) {
        this(inspectionIds, Collections.emptyMap(), Collections.emptyMap(), Duration.ofSeconds(5), false);
    }

    public SchedulingRequest(List<Long> inspectionIds,
                             Map<Long, Set<Long>> eligibleExpertIdsByInspectionId,
                             Map<Long, Set<LocalDate>> expertAvailabilityOverrides,
                             Duration timeLimit,
                             boolean allowRescheduling) {
        if (inspectionIds == null || inspectionIds.isEmpty()) {
            throw new IllegalArgumentException("Inspection IDs must not be null or empty");
        }
        this.inspectionIds = Collections.unmodifiableList(new ArrayList<>(inspectionIds));
        this.eligibleExpertIdsByInspectionId = eligibleExpertIdsByInspectionId != null
                ? Collections.unmodifiableMap(new HashMap<>(eligibleExpertIdsByInspectionId))
                : Collections.emptyMap();
        this.expertAvailabilityOverrides = expertAvailabilityOverrides != null
                ? Collections.unmodifiableMap(new HashMap<>(expertAvailabilityOverrides))
                : Collections.emptyMap();
        this.timeLimit = (timeLimit != null && !timeLimit.isZero() && !timeLimit.isNegative())
                ? timeLimit
                : Duration.ofSeconds(5);
        this.allowRescheduling = allowRescheduling;
    }

    public static Builder builder() {
        return new Builder();
    }

    public List<Long> getInspectionIds() {
        return inspectionIds;
    }

    public Map<Long, Set<Long>> getEligibleExpertIdsByInspectionId() {
        return eligibleExpertIdsByInspectionId;
    }

    public Map<Long, Set<LocalDate>> getExpertAvailabilityOverrides() {
        return expertAvailabilityOverrides;
    }

    public Duration getTimeLimit() {
        return timeLimit;
    }

    public boolean isAllowRescheduling() {
        return allowRescheduling;
    }

    public static class Builder {
        private List<Long> inspectionIds = new ArrayList<>();
        private Map<Long, Set<Long>> eligibleExpertIdsByInspectionId = new HashMap<>();
        private Map<Long, Set<LocalDate>> expertAvailabilityOverrides = new HashMap<>();
        private Duration timeLimit = Duration.ofSeconds(5);
        private boolean allowRescheduling = false;

        public Builder inspectionIds(Collection<Long> ids) {
            if (ids != null) {
                this.inspectionIds = new ArrayList<>(ids);
            }
            return this;
        }

        public Builder addInspectionId(Long id) {
            if (id != null) {
                this.inspectionIds.add(id);
            }
            return this;
        }

        public Builder eligibleExperts(Long inspectionId, Collection<Long> expertIds) {
            if (inspectionId != null && expertIds != null) {
                this.eligibleExpertIdsByInspectionId.put(inspectionId, new HashSet<>(expertIds));
            }
            return this;
        }

        public Builder expertAvailability(Long expertId, Collection<LocalDate> dates) {
            if (expertId != null && dates != null) {
                this.expertAvailabilityOverrides.put(expertId, new HashSet<>(dates));
            }
            return this;
        }

        public Builder timeLimit(Duration timeLimit) {
            if (timeLimit != null) {
                this.timeLimit = timeLimit;
            }
            return this;
        }

        public Builder allowRescheduling(boolean allow) {
            this.allowRescheduling = allow;
            return this;
        }

        public SchedulingRequest build() {
            return new SchedulingRequest(
                    inspectionIds,
                    eligibleExpertIdsByInspectionId,
                    expertAvailabilityOverrides,
                    timeLimit,
                    allowRescheduling
            );
        }
    }
}

