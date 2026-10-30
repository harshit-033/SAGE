package in.gov.aicte.evc.entity;

/**
 * Designated role of an assigned expert within an EVC committee.
 */
public enum AssignmentRole {
    /**
     * Senior academician or designated lead directing the committee inspection.
     */
    CHAIRPERSON,

    /**
     * Peer academic member evaluating institutional criteria.
     */
    MEMBER,

    /**
     * Subject matter expert or industry specialist for domain-specific evaluation.
     */
    SPECIALIST
}
