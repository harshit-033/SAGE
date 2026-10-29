package org.aicte.sage.modules.scrutiny.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "scrutiny_rules")
public class ScrutinyRule {

    @Id
    @Column(length = 64)
    private String id;

    @Column(nullable = false, length = 32)
    private String version;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "aph_clause", nullable = false, length = 128)
    private String aphClause;

    @Column(name = "aph_page_number")
    private Integer aphPageNumber;

    @Column(name = "application_category", nullable = false, length = 64)
    private String applicationCategory = "ALL";

    @Column(nullable = false, length = 32)
    private String severity = "CRITICAL_DEFICIENCY";

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public ScrutinyRule() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAphClause() { return aphClause; }
    public void setAphClause(String aphClause) { this.aphClause = aphClause; }

    public Integer getAphPageNumber() { return aphPageNumber; }
    public void setAphPageNumber(Integer aphPageNumber) { this.aphPageNumber = aphPageNumber; }

    public String getApplicationCategory() { return applicationCategory; }
    public void setApplicationCategory(String applicationCategory) { this.applicationCategory = applicationCategory; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
