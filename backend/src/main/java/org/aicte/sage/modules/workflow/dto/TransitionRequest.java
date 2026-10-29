package org.aicte.sage.modules.workflow.dto;

import jakarta.validation.constraints.NotNull;
import org.aicte.sage.modules.applications.model.ApplicationState;

public class TransitionRequest {

    @NotNull(message = "Target state is required")
    private ApplicationState targetState;

    private String reason;
    private String evidenceSummary;

    public TransitionRequest() {}

    public TransitionRequest(ApplicationState targetState, String reason, String evidenceSummary) {
        this.targetState = targetState;
        this.reason = reason;
        this.evidenceSummary = evidenceSummary;
    }

    public ApplicationState getTargetState() { return targetState; }
    public void setTargetState(ApplicationState targetState) { this.targetState = targetState; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getEvidenceSummary() { return evidenceSummary; }
    public void setEvidenceSummary(String evidenceSummary) { this.evidenceSummary = evidenceSummary; }
}
