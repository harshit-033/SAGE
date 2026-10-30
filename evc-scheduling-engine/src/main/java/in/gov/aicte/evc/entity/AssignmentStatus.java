package in.gov.aicte.evc.entity;

/**
 * Status of an expert's committee assignment.
 */
public enum AssignmentStatus {
    /**
     * Proposed by scheduler, awaiting confirmation.
     */
    PROPOSED,

    /**
     * Confirmed by the expert or regional coordinator.
     */
    CONFIRMED,

    /**
     * Declined by the expert or withdrawn.
     */
    DECLINED
}
