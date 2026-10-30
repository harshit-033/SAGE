package org.aicte.sage.modules.evc.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.aicte.sage.modules.applications.model.Application;
import org.aicte.sage.modules.institutions.entity.Institution;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents an Expert Visit Committee inspection for an approval application.
 * Dynamically enforces duration, required committee member count, and Sunday non-inspection calendar rule.
 */
@Entity
@Table(
    name = "evc_inspections",
    indexes = {
        @Index(name = "idx_evc_inspections_institution", columnList = "institution_id"),
        @Index(name = "idx_evc_inspections_application", columnList = "application_id"),
        @Index(name = "idx_evc_inspections_status", columnList = "status")
    }
)
public class EvcInspection {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id")
    private Application application;

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

    @Column(name = "scheduled_end_date")
    private LocalDate scheduledEndDate;

    @NotNull(message = "Inspection status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private InspectionStatus status = InspectionStatus.PENDING;

    @Column(name = "special_instructions", length = 500)
    private String specialInstructions;

    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public EvcInspection() {
    }

    public EvcInspection(Institution institution, Application application, Integer durationInDays,
                         Integer requiredMemberCount, LocalDate earliestStartDate, LocalDate latestStartDate) {
        this.institution = institution;
        this.application = application;
        this.durationInDays = durationInDays;
        this.requiredMemberCount = requiredMemberCount;
        this.earliestStartDate = earliestStartDate;
        this.latestStartDate = latestStartDate;
        this.status = InspectionStatus.PENDING;
    }

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
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
     * Calculates the sequence of actual inspection days for a given start date and duration.
     * Sundays are strictly excluded from inspection activities per AICTE statutory guidelines.
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
     * Calculates the inclusive end date for an inspection based on its start date and duration,
     * skipping non-working Sundays.
     */
    public static LocalDate calculateInclusiveEndDate(LocalDate startDate, int durationInDays) {
        List<LocalDate> days = calculateInspectionDays(startDate, durationInDays);
        return days.get(days.size() - 1);
    }

    public LocalDate getDerivedInclusiveEndDate() {
        if (scheduledStartDate == null || durationInDays == null) {
            return null;
        }
        return calculateInclusiveEndDate(scheduledStartDate, durationInDays);
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
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
        if (scheduledStartDate != null && durationInDays != null) {
            this.scheduledEndDate = calculateInclusiveEndDate(scheduledStartDate, durationInDays);
        } else {
            this.scheduledEndDate = null;
        }
    }

    public LocalDate getScheduledEndDate() {
        return scheduledEndDate;
    }

    public void setScheduledEndDate(LocalDate scheduledEndDate) {
        this.scheduledEndDate = scheduledEndDate;
    }

    public InspectionStatus getStatus() {
        return status;
    }

    public void setStatus(InspectionStatus status) {
        this.status = status;
    }

    public String getSpecialInstructions() {
        return specialInstructions;
    }

    public void setSpecialInstructions(String specialInstructions) {
        this.specialInstructions = specialInstructions;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EvcInspection that = (EvcInspection) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
