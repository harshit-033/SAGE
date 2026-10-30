package org.aicte.sage.modules.evc.scheduler.model;

import org.aicte.sage.modules.evc.entity.EvcInspection;

import java.time.LocalDate;
import java.util.*;

/**
 * Immutable input model representing an EVC inspection request submitted to the scheduling solver.
 * Dynamic duration and committee size are configured per inspection by SAGE.
 * Inspection duration represents the number of actual inspection days, excluding Sundays.
 */
public final class InspectionRequest {

    private final String inspectionId;
    private final int durationInDays;
    private final int requiredMemberCount;
    private final LocalDate earliestStartDate;
    private final LocalDate latestStartDate;
    private final Set<String> eligibleExpertIds;

    public InspectionRequest(String inspectionId,
                             int durationInDays,
                             int requiredMemberCount,
                             LocalDate earliestStartDate,
                             LocalDate latestStartDate,
                             Collection<String> eligibleExpertIds) {
        if (inspectionId == null || inspectionId.isBlank()) {
            throw new IllegalArgumentException("Inspection ID must not be null or blank");
        }
        if (durationInDays <= 0) {
            throw new IllegalArgumentException("Inspection duration must be positive: " + durationInDays);
        }
        if (requiredMemberCount <= 0) {
            throw new IllegalArgumentException("Required committee member count must be positive: " + requiredMemberCount);
        }
        if (earliestStartDate == null) {
            throw new IllegalArgumentException("Earliest start date must not be null");
        }
        if (latestStartDate == null) {
            throw new IllegalArgumentException("Latest start date must not be null");
        }
        if (earliestStartDate.isAfter(latestStartDate)) {
            throw new IllegalArgumentException(String.format(
                    "Earliest start date (%s) must not be after latest start date (%s)",
                    earliestStartDate, latestStartDate));
        }

        this.inspectionId = inspectionId;
        this.durationInDays = durationInDays;
        this.requiredMemberCount = requiredMemberCount;
        this.earliestStartDate = earliestStartDate;
        this.latestStartDate = latestStartDate;
        this.eligibleExpertIds = eligibleExpertIds != null
                ? Collections.unmodifiableSet(new LinkedHashSet<>(eligibleExpertIds))
                : Collections.emptySet();
    }

    public String getInspectionId() {
        return inspectionId;
    }

    public int getDurationInDays() {
        return durationInDays;
    }

    public int getRequiredMemberCount() {
        return requiredMemberCount;
    }

    public LocalDate getEarliestStartDate() {
        return earliestStartDate;
    }

    public LocalDate getLatestStartDate() {
        return latestStartDate;
    }

    public Set<String> getEligibleExpertIds() {
        return eligibleExpertIds;
    }

    public LocalDate calculateEndDate(LocalDate startDate) {
        return EvcInspection.calculateInclusiveEndDate(startDate, durationInDays);
    }

    public List<LocalDate> calculateInspectionDays(LocalDate startDate) {
        return EvcInspection.calculateInspectionDays(startDate, durationInDays);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InspectionRequest that)) return false;
        return Objects.equals(inspectionId, that.inspectionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(inspectionId);
    }

    @Override
    public String toString() {
        return "InspectionRequest{" +
                "inspectionId='" + inspectionId + '\'' +
                ", durationInDays=" + durationInDays +
                ", requiredMemberCount=" + requiredMemberCount +
                ", earliestStartDate=" + earliestStartDate +
                ", latestStartDate=" + latestStartDate +
                ", eligibleExpertCount=" + eligibleExpertIds.size() +
                '}';
    }
}
