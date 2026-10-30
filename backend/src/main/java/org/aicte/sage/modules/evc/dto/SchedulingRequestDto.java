package org.aicte.sage.modules.evc.dto;

import jakarta.validation.constraints.NotEmpty;

import java.time.LocalDate;
import java.util.*;

public class SchedulingRequestDto {

    @NotEmpty(message = "At least one inspection ID must be specified for scheduling")
    private List<UUID> inspectionIds = new ArrayList<>();

    private boolean allowRescheduling = false;

    private Integer timeLimitSeconds = 10;

    private Map<UUID, Set<UUID>> eligibleExpertIdsByInspectionId = new HashMap<>();

    private Map<UUID, Set<LocalDate>> expertAvailabilityOverrides = new HashMap<>();

    public SchedulingRequestDto() {
    }

    public SchedulingRequestDto(List<UUID> inspectionIds, boolean allowRescheduling, Integer timeLimitSeconds) {
        this.inspectionIds = inspectionIds;
        this.allowRescheduling = allowRescheduling;
        this.timeLimitSeconds = timeLimitSeconds;
    }

    public List<UUID> getInspectionIds() {
        return inspectionIds;
    }

    public void setInspectionIds(List<UUID> inspectionIds) {
        this.inspectionIds = inspectionIds;
    }

    public boolean isAllowRescheduling() {
        return allowRescheduling;
    }

    public void setAllowRescheduling(boolean allowRescheduling) {
        this.allowRescheduling = allowRescheduling;
    }

    public Integer getTimeLimitSeconds() {
        return timeLimitSeconds;
    }

    public void setTimeLimitSeconds(Integer timeLimitSeconds) {
        this.timeLimitSeconds = timeLimitSeconds;
    }

    public Map<UUID, Set<UUID>> getEligibleExpertIdsByInspectionId() {
        return eligibleExpertIdsByInspectionId;
    }

    public void setEligibleExpertIdsByInspectionId(Map<UUID, Set<UUID>> eligibleExpertIdsByInspectionId) {
        this.eligibleExpertIdsByInspectionId = eligibleExpertIdsByInspectionId;
    }

    public Map<UUID, Set<LocalDate>> getExpertAvailabilityOverrides() {
        return expertAvailabilityOverrides;
    }

    public void setExpertAvailabilityOverrides(Map<UUID, Set<LocalDate>> expertAvailabilityOverrides) {
        this.expertAvailabilityOverrides = expertAvailabilityOverrides;
    }
}
