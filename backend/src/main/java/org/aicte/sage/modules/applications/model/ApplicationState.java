package org.aicte.sage.modules.applications.model;

/**
 * Valid AICTE Application Workflow Lifecycle States per APH 2024-27.
 */
public enum ApplicationState {
    DRAFT,
    SUBMITTED,
    UNDER_SCRUTINY,
    DEFICIENT,
    RESUBMITTED,
    SCRUTINY_PASSED,
    SHC_SCHEDULED,
    SHC_REVIEW,
    EVC_SCHEDULED,
    EVC_CONDUCTED,
    SAC_REVIEW,
    APPROVED,
    REJECTED,
    WITHDRAWN
}
