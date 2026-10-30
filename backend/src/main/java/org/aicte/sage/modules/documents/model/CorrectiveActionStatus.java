package org.aicte.sage.modules.documents.model;

/**
 * Status lifecycle for document and compliance corrective actions.
 */
public enum CorrectiveActionStatus {
    OPEN,
    IN_PROGRESS,
    SUBMITTED_FOR_REVIEW,
    RESOLVED,
    REJECTED,
    OVERDUE,
    CANCELLED
}
