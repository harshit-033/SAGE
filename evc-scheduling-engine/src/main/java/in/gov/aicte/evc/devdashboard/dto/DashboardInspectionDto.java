package in.gov.aicte.evc.devdashboard.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO representing an EVC inspection request in the dev dashboard.
 */
public class DashboardInspectionDto {

    private String inspectionId;
    private int durationInDays;
    private int requiredMemberCount;
    private LocalDate earliestStartDate;
    private LocalDate latestStartDate;
    private List<String> eligibleExpertIds;

    public DashboardInspectionDto() {
    }

    public DashboardInspectionDto(String inspectionId,
                                  int durationInDays,
                                  int requiredMemberCount,
                                  LocalDate earliestStartDate,
                                  LocalDate latestStartDate,
                                  List<String> eligibleExpertIds) {
        this.inspectionId = inspectionId;
        this.durationInDays = durationInDays;
        this.requiredMemberCount = requiredMemberCount;
        this.earliestStartDate = earliestStartDate;
        this.latestStartDate = latestStartDate;
        this.eligibleExpertIds = eligibleExpertIds;
    }

    public String getInspectionId() {
        return inspectionId;
    }

    public void setInspectionId(String inspectionId) {
        this.inspectionId = inspectionId;
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

    public List<String> getEligibleExpertIds() {
        return eligibleExpertIds;
    }

    public void setEligibleExpertIds(List<String> eligibleExpertIds) {
        this.eligibleExpertIds = eligibleExpertIds;
    }
}

