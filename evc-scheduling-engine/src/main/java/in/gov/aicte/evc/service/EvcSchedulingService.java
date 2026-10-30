package in.gov.aicte.evc.service;

import in.gov.aicte.evc.service.dto.SchedulingRequest;
import in.gov.aicte.evc.service.dto.SchedulingServiceResult;

/**
 * Application service interface for EVC inspection committee scheduling.
 * Orchestrates loading persisted domain models, resolving expert availability,
 * converting to detached CP-SAT solver inputs, invoking the solver, and safely
 * persisting successful schedules.
 */
public interface EvcSchedulingService {

    /**
     * Executes the scheduling workflow for the specified request.
     *
     * @param request the scheduling orchestration request
     * @return result encapsulating solver status, persistence outcome, and scheduled inspection details
     */
    SchedulingServiceResult schedule(SchedulingRequest request);
}

