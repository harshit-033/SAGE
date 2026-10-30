package org.aicte.sage.modules.evc.scheduler.model;

import java.time.LocalDate;
import java.util.*;

/**
 * Result representing a scheduled inspection produced by the CP-SAT engine.
 */
public final class ScheduledInspection {

    private final String inspectionId;
    private final LocalDate scheduledStartDate;
    private final LocalDate scheduledEndDate;
    private final Set<String> assignedExpertIds;

    public ScheduledInspection(String inspectionId,
                               LocalDate scheduledStartDate,
                               LocalDate scheduledEndDate,
                               Collection<String> assignedExpertIds) {
        if (inspectionId == null || inspectionId.isBlank()) {
            throw new IllegalArgumentException("Inspection ID must not be null or blank");
        }
        Objects.requireNonNull(scheduledStartDate, "Scheduled start date must not be null");
        Objects.requireNonNull(scheduledEndDate, "Scheduled end date must not be null");
        if (scheduledStartDate.isAfter(scheduledEndDate)) {
            throw new IllegalArgumentException("Scheduled start date must not be after scheduled end date");
        }

        this.inspectionId = inspectionId;
        this.scheduledStartDate = scheduledStartDate;
        this.scheduledEndDate = scheduledEndDate;
        this.assignedExpertIds = assignedExpertIds != null
                ? Collections.unmodifiableSet(new LinkedHashSet<>(assignedExpertIds))
                : Collections.emptySet();
    }

    public String getInspectionId() {
        return inspectionId;
    }

    public LocalDate getScheduledStartDate() {
        return scheduledStartDate;
    }

    public LocalDate getScheduledEndDate() {
        return scheduledEndDate;
    }

    public Set<String> getAssignedExpertIds() {
        return assignedExpertIds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ScheduledInspection that)) return false;
        return Objects.equals(inspectionId, that.inspectionId) &&
                Objects.equals(scheduledStartDate, that.scheduledStartDate) &&
                Objects.equals(scheduledEndDate, that.scheduledEndDate) &&
                Objects.equals(assignedExpertIds, that.assignedExpertIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(inspectionId, scheduledStartDate, scheduledEndDate, assignedExpertIds);
    }

    @Override
    public String toString() {
        return "ScheduledInspection{" +
                "inspectionId='" + inspectionId + '\'' +
                ", scheduledStartDate=" + scheduledStartDate +
                ", scheduledEndDate=" + scheduledEndDate +
                ", assignedExperts=" + assignedExpertIds +
                '}';
    }
}
