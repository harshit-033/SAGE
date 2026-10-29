package org.aicte.sage.modules.workflow.dto;

import org.aicte.sage.modules.applications.model.ApplicationState;
import org.aicte.sage.modules.workflow.model.WorkflowTransition;

import java.time.Instant;
import java.util.UUID;

public class WorkflowTransitionDto {

    private UUID id;
    private UUID applicationId;
    private ApplicationState fromState;
    private ApplicationState toState;
    private UUID actorId;
    private String actorName;
    private String actorRole;
    private String reason;
    private String evidenceSummary;
    private Instant createdAt;

    public WorkflowTransitionDto() {}

    public static WorkflowTransitionDto fromEntity(WorkflowTransition t) {
        WorkflowTransitionDto dto = new WorkflowTransitionDto();
        dto.setId(t.getId());
        if (t.getApplication() != null) {
            dto.setApplicationId(t.getApplication().getId());
        }
        dto.setFromState(t.getFromState());
        dto.setToState(t.getToState());
        if (t.getActor() != null) {
            dto.setActorId(t.getActor().getId());
            dto.setActorName(t.getActor().getFullName());
        }
        dto.setActorRole(t.getActorRole());
        dto.setReason(t.getReason());
        dto.setEvidenceSummary(t.getEvidenceSummary());
        dto.setCreatedAt(t.getCreatedAt());
        return dto;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getApplicationId() { return applicationId; }
    public void setApplicationId(UUID applicationId) { this.applicationId = applicationId; }

    public ApplicationState getFromState() { return fromState; }
    public void setFromState(ApplicationState fromState) { this.fromState = fromState; }

    public ApplicationState getToState() { return toState; }
    public void setToState(ApplicationState toState) { this.toState = toState; }

    public UUID getActorId() { return actorId; }
    public void setActorId(UUID actorId) { this.actorId = actorId; }

    public String getActorName() { return actorName; }
    public void setActorName(String actorName) { this.actorName = actorName; }

    public String getActorRole() { return actorRole; }
    public void setActorRole(String actorRole) { this.actorRole = actorRole; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getEvidenceSummary() { return evidenceSummary; }
    public void setEvidenceSummary(String evidenceSummary) { this.evidenceSummary = evidenceSummary; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
