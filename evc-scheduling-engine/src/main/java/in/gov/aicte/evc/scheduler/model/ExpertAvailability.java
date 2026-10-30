package in.gov.aicte.evc.scheduler.model;

import in.gov.aicte.evc.entity.EvcInspection;

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

    /**
     * Creates an availability profile where the expert is available on all dates between
     * {@code horizonStart} and {@code horizonEnd} inclusive, except for specified blackout dates.
     */
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

    /**
     * Checks if the expert is available for all specified inspection dates.
     *
     * @param dates collection of required inspection dates
     * @return true if expert is available on all specified dates, false otherwise
     */
    public boolean isAvailableForDates(Collection<LocalDate> dates) {
        if (dates == null || dates.isEmpty()) {
            return false;
        }
        return availableDates.containsAll(dates);
    }

    /**
     * Checks if the expert is available for every actual inspection day of an inspection
     * starting on {@code startDate} with {@code durationInDays} actual non-Sunday inspection days.
     * Sunday is excluded from inspection activities and does not require availability.
     *
     * @param startDate start date of the inspection
     * @param durationInDays duration of the inspection in actual non-Sunday inspection days
     * @return true if available for all actual inspection days, false otherwise
     */
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

