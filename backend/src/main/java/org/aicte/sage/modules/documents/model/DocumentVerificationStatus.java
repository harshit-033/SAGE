package org.aicte.sage.modules.documents.model;

/**
 * Verification state for regulatory documents reviewed by officers or automated engines.
 */
public enum DocumentVerificationStatus {
    PENDING_REVIEW,
    UNDER_REVIEW,
    VERIFIED,
    VERIFIED_WITH_WARNINGS,
    REJECTED,
    REQUIRES_CLARIFICATION,
    REPROCESSING
}
