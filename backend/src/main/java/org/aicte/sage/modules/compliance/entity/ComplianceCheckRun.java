package org.aicte.sage.modules.compliance.entity;

import jakarta.persistence.*;
import org.aicte.sage.modules.institutions.entity.Institution;
import org.aicte.sage.modules.users.entity.User;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "compliance_check_runs")
public class ComplianceCheckRun {

    @Id
    private UUID id;

    @Column(name = "run_type", nullable = false, length = 32)
    private String runType; // SCHEDULED, EVENT_DRIVEN, MANUAL

    @Column(name = "trigger_source", nullable = false, length = 128)
    private String triggerSource;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id")
    private Institution institution;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt = Instant.now();

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(nullable = false, length = 32)
    private String status = "RUNNING"; // RUNNING, SUCCESS, PARTIAL_FAILURE, FAILED

    @Column(name = "institutions_checked", nullable = false)
    private int institutionsChecked = 0;

    @Column(name = "findings_generated", nullable = false)
    private int findingsGenerated = 0;

    @Column(name = "alerts_created", nullable = false)
    private int alertsCreated = 0;

    @Column(name = "deadlines_created", nullable = false)
    private int deadlinesCreated = 0;

    @Column(name = "summary_notes", columnDefinition = "TEXT")
    private String summaryNotes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdByUser;

    public ComplianceCheckRun() {
        this.id = UUID.randomUUID();
    }

    public ComplianceCheckRun(String runType, String triggerSource, Institution institution, User createdByUser) {
        this();
        this.runType = runType;
        this.triggerSource = triggerSource;
        this.institution = institution;
        this.createdByUser = createdByUser;
        this.startedAt = Instant.now();
        this.status = "RUNNING";
    }

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        if (this.startedAt == null) {
            this.startedAt = Instant.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getRunType() {
        return runType;
    }

    public void setRunType(String runType) {
        this.runType = runType;
    }

    public String getTriggerSource() {
        return triggerSource;
    }

    public void setTriggerSource(String triggerSource) {
        this.triggerSource = triggerSource;
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getInstitutionsChecked() {
        return institutionsChecked;
    }

    public void setInstitutionsChecked(int institutionsChecked) {
        this.institutionsChecked = institutionsChecked;
    }

    public int getFindingsGenerated() {
        return findingsGenerated;
    }

    public void setFindingsGenerated(int findingsGenerated) {
        this.findingsGenerated = findingsGenerated;
    }

    public int getAlertsCreated() {
        return alertsCreated;
    }

    public void setAlertsCreated(int alertsCreated) {
        this.alertsCreated = alertsCreated;
    }

    public int getDeadlinesCreated() {
        return deadlinesCreated;
    }

    public void setDeadlinesCreated(int deadlinesCreated) {
        this.deadlinesCreated = deadlinesCreated;
    }

    public String getSummaryNotes() {
        return summaryNotes;
    }

    public void setSummaryNotes(String summaryNotes) {
        this.summaryNotes = summaryNotes;
    }

    public User getCreatedByUser() {
        return createdByUser;
    }

    public void setCreatedByUser(User createdByUser) {
        this.createdByUser = createdByUser;
    }
}
