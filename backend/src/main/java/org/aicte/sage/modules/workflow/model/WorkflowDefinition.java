package org.aicte.sage.modules.workflow.model;

import jakarta.persistence.*;
import org.aicte.sage.modules.applications.model.ApplicationCategory;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "workflow_definitions")
public class WorkflowDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 64)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 64)
    private ApplicationCategory category;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(nullable = false, length = 32)
    private String version;

    @Column(name = "aph_reference", length = 128)
    private String aphReference;

    @Column(name = "requires_evc", nullable = false)
    private boolean requiresEvc = false;

    @Column(name = "requires_shc", nullable = false)
    private boolean requiresShc = false;

    @Column(name = "requires_sac", nullable = false)
    private boolean requiresSac = false;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public WorkflowDefinition() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public ApplicationCategory getCategory() { return category; }
    public void setCategory(ApplicationCategory category) { this.category = category; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getAphReference() { return aphReference; }
    public void setAphReference(String aphReference) { this.aphReference = aphReference; }

    public boolean isRequiresEvc() { return requiresEvc; }
    public void setRequiresEvc(boolean requiresEvc) { this.requiresEvc = requiresEvc; }

    public boolean isRequiresShc() { return requiresShc; }
    public void setRequiresShc(boolean requiresShc) { this.requiresShc = requiresShc; }

    public boolean isRequiresSac() { return requiresSac; }
    public void setRequiresSac(boolean requiresSac) { this.requiresSac = requiresSac; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
