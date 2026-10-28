package org.aicte.sage.modules.institutions.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class InstitutionStatusRequest {

    @NotNull(message = "Active status is required")
    private Boolean active;

    @Size(max = 255, message = "Reason must not exceed 255 characters")
    private String reason;

    public InstitutionStatusRequest() {}

    public InstitutionStatusRequest(Boolean active, String reason) {
        this.active = active;
        this.reason = reason;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
