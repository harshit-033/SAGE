package in.gov.aicte.evc.devdashboard.dto;

/**
 * Encapsulates pre-configured test scenarios for one-click demonstration.
 */
public class DemoScenarioDto {

    private String id;
    private String name;
    private String category;
    private String description;
    private DashboardScheduleRequest request;

    public DemoScenarioDto() {
    }

    public DemoScenarioDto(String id, String name, String category, String description, DashboardScheduleRequest request) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.description = description;
        this.request = request;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public DashboardScheduleRequest getRequest() {
        return request;
    }

    public void setRequest(DashboardScheduleRequest request) {
        this.request = request;
    }
}

