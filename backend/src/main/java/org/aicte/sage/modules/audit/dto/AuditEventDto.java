package org.aicte.sage.modules.audit.dto;

import org.aicte.sage.modules.audit.entity.AuditEvent;

import java.time.Instant;
import java.util.UUID;

public class AuditEventDto {
    private UUID id;
    private String eventType;
    private String principal;
    private UUID institutionId;
    private String resourceType;
    private String resourceId;
    private String action;
    private String clientIp;
    private String correlationId;
    private String details;
    private Instant createdAt;

    public AuditEventDto() {}

    public AuditEventDto(UUID id, String eventType, String principal, UUID institutionId,
                         String resourceType, String resourceId, String action,
                         String clientIp, String correlationId, String details, Instant createdAt) {
        this.id = id;
        this.eventType = eventType;
        this.principal = principal;
        this.institutionId = institutionId;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.action = action;
        this.clientIp = clientIp;
        this.correlationId = correlationId;
        this.details = details;
        this.createdAt = createdAt;
    }

    public static AuditEventDto fromEntity(AuditEvent event) {
        return new AuditEventDto(
                event.getId(),
                event.getEventType(),
                event.getPrincipal(),
                event.getInstitutionId(),
                event.getResourceType(),
                event.getResourceId(),
                event.getAction(),
                event.getClientIp(),
                event.getCorrelationId(),
                event.getDetails(),
                event.getCreatedAt()
        );
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getPrincipal() {
        return principal;
    }

    public void setPrincipal(String principal) {
        this.principal = principal;
    }

    public UUID getInstitutionId() {
        return institutionId;
    }

    public void setInstitutionId(UUID institutionId) {
        this.institutionId = institutionId;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
