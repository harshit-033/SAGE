package org.aicte.sage.modules.documents.model;

/**
 * Compliance state for institutional documents evaluated for expiry and regulatory validity.
 */
public enum DocumentComplianceStatus {
    COMPLIANT,
    EXPIRING_SOON,
    EXPIRED,
    DEFICIENT,
    ACTION_REQUIRED,
    NOT_APPLICABLE
}
