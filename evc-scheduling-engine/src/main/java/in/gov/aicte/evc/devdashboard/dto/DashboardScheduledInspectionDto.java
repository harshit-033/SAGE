package in.gov.aicte.evc.devdashboard.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO representing an individual scheduled inspection in the dev dashboard response.
 */
public class DashboardScheduledInspectionDto {

    private String inspectionId;
    private LocalDate scheduledStartDate;
    private LocalDate scheduledEndDate;
    private int durationInDays;
    private int requiredMemberCount;
    private List<String> assignedExpertIds;
    private List<LocalDate> occupiedDates;

    public DashboardScheduledInspectionDto() {
    }

    public DashboardScheduledInspectionDto(String inspectionId,
                                           LocalDate scheduledStartDate,
                                           LocalDate scheduledEndDate,
                                           int durationInDays,
                                           int requiredMemberCount,
                                           List<String> assignedExpertIds,
                                           List<LocalDate> occupiedDates) {
        this.inspectionId = inspectionId;
        this.scheduledStartDate = scheduledStartDate;
        this.scheduledEndDate = scheduledEndDate;
        this.durationInDays = durationInDays;
        this.requiredMemberCount = requiredMemberCount;
        this.assignedExpertIds = assignedExpertIds;
        this.occupiedDates = occupiedDates;
    }

    public String getInspectionId() {
        return inspectionId;
    }

    public void setInspectionId(String inspectionId) {
        this.inspectionId = inspectionId;
    }

    public LocalDate getScheduledStartDate() {
        return scheduledStartDate;
    }

    public void setScheduledStartDate(LocalDate scheduledStartDate) {
        this.scheduledStartDate = scheduledStartDate;
    }

    public LocalDate getScheduledEndDate() {
        return scheduledEndDate;
    }

    public void setScheduledEndDate(LocalDate scheduledEndDate) {
        this.scheduledEndDate = scheduledEndDate;
    }

    public int getDurationInDays() {
        return durationInDays;
    }

    public void setDurationInDays(int durationInDays) {
        this.durationInDays = durationInDays;
    }

    public int getRequiredMemberCount() {
        return requiredMemberCount;
    }

    public void setRequiredMemberCount(int requiredMemberCount) {
        this.requiredMemberCount = requiredMemberCount;
    }

    public List<String> getAssignedExpertIds() {
        return assignedExpertIds;
    }

    public void setAssignedExpertIds(List<String> assignedExpertIds) {
        this.assignedExpertIds = assignedExpertIds;
    }

    public List<LocalDate> getOccupiedDates() {
        return occupiedDates;
    }

    public void setOccupiedDates(List<LocalDate> occupiedDates) {
        this.occupiedDates = occupiedDates;
    }
}

