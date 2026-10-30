package org.aicte.sage.modules.scrutiny.model;

import jakarta.persistence.*;
import org.aicte.sage.modules.applications.model.Application;
import org.aicte.sage.modules.users.entity.User;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "scrutiny_evaluations")
public class ScrutinyEvaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @Column(nullable = false, length = 32)
    private String status; // PASSED, DEFICIENT, REQUIRES_HUMAN_REVIEW

    @Column(columnDefinition = "TEXT")
    private String summary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluated_by_user_id")
    private User evaluatedByUser;

    @Column(name = "evaluated_at", nullable = false, updatable = false)
    private Instant evaluatedAt = Instant.now();

    @Column(name = "score")
    private Integer score;

    @Column(name = "recommendation", length = 64)
    private String recommendation;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "criteria_breakdown_json", columnDefinition = "TEXT")
    private String criteriaBreakdownJson;

    public ScrutinyEvaluation() {}

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public String getRecommendation() { return recommendation; }
    public void setRecommendation(String recommendation) { this.recommendation = recommendation; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public String getCriteriaBreakdownJson() { return criteriaBreakdownJson; }
    public void setCriteriaBreakdownJson(String criteriaBreakdownJson) { this.criteriaBreakdownJson = criteriaBreakdownJson; }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Application getApplication() { return application; }
    public void setApplication(Application application) { this.application = application; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public User getEvaluatedByUser() { return evaluatedByUser; }
    public void setEvaluatedByUser(User evaluatedByUser) { this.evaluatedByUser = evaluatedByUser; }

    public Instant getEvaluatedAt() { return evaluatedAt; }
    public void setEvaluatedAt(Instant evaluatedAt) { this.evaluatedAt = evaluatedAt; }
}
