package org.aicte.sage.modules.evc.scheduler.model;

import org.aicte.sage.modules.evc.entity.EvcInspection;

import java.time.LocalDate;
import java.util.*;

/**
 * Encapsulates calendar availability for an individual expert over a scheduling horizon.
 * Sunday is a mandatory non-inspection day; experts do not need availability on Sundays.
 */
public final class ExpertAvailability {

    private final String expertId;
    private final Set<LocalDate> availableDates;

    public ExpertAvailability(String expertId, Collection<LocalDate> availableDates) {
        if (expertId == null || expertId.isBlank()) {
            throw new IllegalArgumentException("Expert ID must not be null or blank");
        }
        this.expertId = expertId;
        this.availableDates = availableDates != null
                ? Collections.unmodifiableSet(new HashSet<>(availableDates))
                : Collections.emptySet();
    }

    public static ExpertAvailability of(String expertId, Collection<LocalDate> availableDates) {
        return new ExpertAvailability(expertId, availableDates);
    }

    public static ExpertAvailability availableBetween(String expertId,
                                                      LocalDate horizonStart,
                                                      LocalDate horizonEnd,
                                                      Collection<LocalDate> blackoutDates) {
        Objects.requireNonNull(horizonStart, "Horizon start must not be null");
        Objects.requireNonNull(horizonEnd, "Horizon end must not be null");
        if (horizonStart.isAfter(horizonEnd)) {
            throw new IllegalArgumentException("Horizon start must not be after horizon end");
        }

        Set<LocalDate> blackouts = blackoutDates != null ? new HashSet<>(blackoutDates) : Collections.emptySet();
        Set<LocalDate> available = new HashSet<>();
        LocalDate curr = horizonStart;
        while (!curr.isAfter(horizonEnd)) {
            if (!blackouts.contains(curr)) {
                available.add(curr);
            }
            curr = curr.plusDays(1);
        }
        return new ExpertAvailability(expertId, available);
    }

    public String getExpertId() {
        return expertId;
    }

    public Set<LocalDate> getAvailableDates() {
        return availableDates;
    }

    public boolean isAvailableOn(LocalDate date) {
        return date != null && availableDates.contains(date);
    }

    public boolean isAvailableForDates(Collection<LocalDate> dates) {
        if (dates == null || dates.isEmpty()) {
            return false;
        }
        return availableDates.containsAll(dates);
    }

    public boolean isAvailableForRange(LocalDate startDate, int durationInDays) {
        if (startDate == null || durationInDays <= 0) {
            return false;
        }
        List<LocalDate> inspectionDays = EvcInspection.calculateInspectionDays(startDate, durationInDays);
        return availableDates.containsAll(inspectionDays);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExpertAvailability that)) return false;
        return Objects.equals(expertId, that.expertId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expertId);
    }

    @Override
    public String toString() {
        return "ExpertAvailability{" +
                "expertId='" + expertId + '\'' +
                ", availableDatesCount=" + availableDates.size() +
                '}';
    }
}
