package in.gov.aicte.evc.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents an EVC inspection requiring scheduling for a given institution.
 * Inspection duration and required committee member count are dynamic and configured per inspection by SAGE.
 */
@Entity
@Table(
    name = "evc_inspections",
    indexes = {
        @Index(name = "idx_inspections_institution", columnList = "institution_id"),
        @Index(name = "idx_inspections_status", columnList = "status")
    }
)
public class EvcInspection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Institution is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "institution_id", nullable = false)
    private Institution institution;

    @NotNull(message = "Inspection duration is required")
    @Positive(message = "Inspection duration must be positive")
    @Column(name = "duration_in_days", nullable = false)
    private Integer durationInDays;

    @NotNull(message = "Required committee member count is required")
    @Positive(message = "Required committee member count must be positive")
    @Column(name = "required_member_count", nullable = false)
    private Integer requiredMemberCount;

    @NotNull(message = "Earliest start date is required")
    @Column(name = "earliest_start_date", nullable = false)
    private LocalDate earliestStartDate;

    @NotNull(message = "Latest start date is required")
    @Column(name = "latest_start_date", nullable = false)
    private LocalDate latestStartDate;

    @Column(name = "scheduled_start_date")
    private LocalDate scheduledStartDate;

    @NotNull(message = "Inspection status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private InspectionStatus status = InspectionStatus.PENDING;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public EvcInspection() {
    }

    public EvcInspection(Institution institution, Integer durationInDays, Integer requiredMemberCount,
                         LocalDate earliestStartDate, LocalDate latestStartDate) {
        this.institution = institution;
        this.durationInDays = durationInDays;
        this.requiredMemberCount = requiredMemberCount;
        this.earliestStartDate = earliestStartDate;
        this.latestStartDate = latestStartDate;
        this.status = InspectionStatus.PENDING;
    }

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    @AssertTrue(message = "Earliest start date must not be later than latest start date")
    public boolean isValidDateWindow() {
        if (earliestStartDate == null || latestStartDate == null) {
            return true;
        }
        return !earliestStartDate.isAfter(latestStartDate);
    }

    /**
     * Calculates the inclusive end date for the inspection based on its scheduled start date and duration.
     * Sunday is a mandatory non-inspection day and does not count towards the duration.
     * For example, a 4-day inspection starting on Friday, 6 November 2026 runs on Fri 6, Sat 7,
     * skips Sun 8, and completes on Mon 9 and Tue 10 November. Inclusive end date is 10 November.
     *
     * @return inclusive end date, or null if scheduled start date or duration is not set
     */
    public LocalDate getScheduledEndDate() {
        if (scheduledStartDate == null || durationInDays == null) {
            return null;
        }
        return calculateInclusiveEndDate(scheduledStartDate, durationInDays);
    }

    /**
     * Calculates the sequence of actual inspection days for a given start date and duration.
     * Sundays are strictly excluded from inspection activities.
     * If an inspection starts on a Sunday, Sunday does not count as an inspection day.
     *
     * @param startDate the start date of the inspection
     * @param durationInDays the number of actual inspection days required
     * @return list of actual inspection dates (never containing a Sunday)
     */
    public static List<LocalDate> calculateInspectionDays(LocalDate startDate, int durationInDays) {
        Objects.requireNonNull(startDate, "Start date must not be null");
        if (durationInDays <= 0) {
            throw new IllegalArgumentException("Inspection duration must be positive: " + durationInDays);
        }
        List<LocalDate> inspectionDays = new ArrayList<>(durationInDays);
        LocalDate current = startDate;
        while (inspectionDays.size() < durationInDays) {
            if (current.getDayOfWeek() != DayOfWeek.SUNDAY) {
                inspectionDays.add(current);
            }
            current = current.plusDays(1);
        }
        return inspectionDays;
    }

    /**
     * Calculates the inclusive end date for an inspection based on its start date and duration.
     * Sunday is a mandatory non-inspection day and does not count towards the duration.
     *
     * @param startDate the start date
     * @param durationInDays number of actual inspection days
     * @return inclusive end date
     */
    public static LocalDate calculateInclusiveEndDate(LocalDate startDate, int durationInDays) {
        List<LocalDate> days = calculateInspectionDays(startDate, durationInDays);
        return days.get(days.size() - 1);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public Integer getDurationInDays() {
        return durationInDays;
    }

    public void setDurationInDays(Integer durationInDays) {
        this.durationInDays = durationInDays;
    }

    public Integer getRequiredMemberCount() {
        return requiredMemberCount;
    }

    public void setRequiredMemberCount(Integer requiredMemberCount) {
        this.requiredMemberCount = requiredMemberCount;
    }

    public LocalDate getEarliestStartDate() {
        return earliestStartDate;
    }

    public void setEarliestStartDate(LocalDate earliestStartDate) {
        this.earliestStartDate = earliestStartDate;
    }

    public LocalDate getLatestStartDate() {
        return latestStartDate;
    }

    public void setLatestStartDate(LocalDate latestStartDate) {
        this.latestStartDate = latestStartDate;
    }

    public LocalDate getScheduledStartDate() {
        return scheduledStartDate;
    }

    public void setScheduledStartDate(LocalDate scheduledStartDate) {
        this.scheduledStartDate = scheduledStartDate;
    }

    public InspectionStatus getStatus() {
        return status;
    }

    public void setStatus(InspectionStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EvcInspection that)) return false;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "EvcInspection{" +
                "id=" + id +
                ", durationInDays=" + durationInDays +
                ", requiredMemberCount=" + requiredMemberCount +
                ", earliestStartDate=" + earliestStartDate +
                ", latestStartDate=" + latestStartDate +
                ", scheduledStartDate=" + scheduledStartDate +
                ", status=" + status +
                '}';
    }
}
