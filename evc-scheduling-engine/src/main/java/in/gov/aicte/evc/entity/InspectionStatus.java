package in.gov.aicte.evc.entity;

/**
 * Lifecycle status of an EVC inspection.
 */
public enum InspectionStatus {
    /**
     * Initial state: Inspection requested and awaiting date/expert scheduling.
     */
    PENDING,

    /**
     * Scheduled: Start date and committee members have been assigned.
     */
    SCHEDULED,

    /**
     * Completed: The inspection visit has taken place and reports submitted.
     */
    COMPLETED,

    /**
     * Cancelled: The inspection was cancelled or superseded.
     */
    CANCELLED
}
