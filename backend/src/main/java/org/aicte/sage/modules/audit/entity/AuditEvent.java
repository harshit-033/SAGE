package org.aicte.sage.modules.audit.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_events")
public class AuditEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "event_type", nullable = false, length = 64)
    private String eventType;

    @Column(nullable = false, length = 128)
    private String principal;

    @Column(name = "institution_id")
    private UUID institutionId;

    @Column(name = "resource_type", nullable = false, length = 64)
    private String resourceType;

    @Column(name = "resource_id", nullable = false, length = 128)
    private String resourceId;

    @Column(nullable = false, length = 32)
    private String action;

    @Column(name = "client_ip", length = 64)
    private String clientIp;

    @Column(name = "correlation_id", length = 64)
    private String correlationId;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @PreUpdate
    public void preUpdate() {
        throw new UnsupportedOperationException("Regulatory audit records are immutable and cannot be modified.");
    }

    @PreRemove
    public void preRemove() {
        throw new UnsupportedOperationException("Regulatory audit records are immutable and cannot be deleted.");
    }

    public AuditEvent() {}

    public AuditEvent(String eventType, String principal, UUID institutionId,
                      String resourceType, String resourceId, String action,
                      String clientIp, String correlationId, String details) {
        this.eventType = eventType;
        this.principal = principal;
        this.institutionId = institutionId;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.action = action != null && action.length() > 32 ? action.substring(0, 32) : action;
        this.clientIp = clientIp;
        this.correlationId = correlationId;
        this.details = details;
        this.createdAt = Instant.now();
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
