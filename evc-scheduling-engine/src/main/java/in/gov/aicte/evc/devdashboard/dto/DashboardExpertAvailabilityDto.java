package in.gov.aicte.evc.devdashboard.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO representing an expert's calendar availability in the dev dashboard.
 */
public class DashboardExpertAvailabilityDto {

    private String expertId;
    private List<LocalDate> availableDates;

    public DashboardExpertAvailabilityDto() {
    }

    public DashboardExpertAvailabilityDto(String expertId, List<LocalDate> availableDates) {
        this.expertId = expertId;
        this.availableDates = availableDates;
    }

    public String getExpertId() {
        return expertId;
    }

    public void setExpertId(String expertId) {
        this.expertId = expertId;
    }

    public List<LocalDate> getAvailableDates() {
        return availableDates;
    }

    public void setAvailableDates(List<LocalDate> availableDates) {
        this.availableDates = availableDates;
    }
}

