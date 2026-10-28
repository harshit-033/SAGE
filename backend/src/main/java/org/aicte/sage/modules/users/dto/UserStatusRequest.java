package org.aicte.sage.modules.users.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class UserStatusRequest {

    @NotNull(message = "Active status is required")
    private Boolean active;

    @Size(max = 255)
    private String reason;

    public UserStatusRequest() {}

    public UserStatusRequest(Boolean active, String reason) {
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
