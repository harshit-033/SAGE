package in.gov.aicte.evc.devdashboard.dto;

import java.util.List;

/**
 * Request payload for the development scheduling dashboard.
 */
public class DashboardScheduleRequest {

    private List<DashboardInspectionDto> inspections;
    private List<DashboardExpertAvailabilityDto> expertAvailabilities;
    private Integer timeLimitSeconds;

    public DashboardScheduleRequest() {
    }

    public DashboardScheduleRequest(List<DashboardInspectionDto> inspections,
                                   List<DashboardExpertAvailabilityDto> expertAvailabilities,
                                   Integer timeLimitSeconds) {
        this.inspections = inspections;
        this.expertAvailabilities = expertAvailabilities;
        this.timeLimitSeconds = timeLimitSeconds;
    }

    public List<DashboardInspectionDto> getInspections() {
        return inspections;
    }

    public void setInspections(List<DashboardInspectionDto> inspections) {
        this.inspections = inspections;
    }

    public List<DashboardExpertAvailabilityDto> getExpertAvailabilities() {
        return expertAvailabilities;
    }

    public void setExpertAvailabilities(List<DashboardExpertAvailabilityDto> expertAvailabilities) {
        this.expertAvailabilities = expertAvailabilities;
    }

    public Integer getTimeLimitSeconds() {
        return timeLimitSeconds;
    }

    public void setTimeLimitSeconds(Integer timeLimitSeconds) {
        this.timeLimitSeconds = timeLimitSeconds;
    }
}

