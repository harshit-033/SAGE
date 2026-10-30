package in.gov.aicte.evc.scheduler.model;

/**
 * Outcome status reported by the CP-SAT scheduling engine.
 */
public enum SchedulingStatus {

    /**
     * An optimal valid schedule was found satisfying all constraints and objective criteria.
     */
    OPTIMAL,

    /**
     * A valid feasible schedule was found satisfying all hard constraints.
     */
    FEASIBLE,

    /**
     * The model is mathematically proven infeasible under the given constraints.
     */
    INFEASIBLE,

    /**
     * The search limit (e.g. wall-clock time limit) was reached without establishing feasibility or infeasibility.
     */
    TIME_LIMIT_REACHED,

    /**
     * The input instance was rejected due to malformed, inconsistent, or invalid problem data.
     */
    INVALID_INPUT,

    /**
     * An unexpected internal solver or runtime error occurred.
     */
    ERROR
}

