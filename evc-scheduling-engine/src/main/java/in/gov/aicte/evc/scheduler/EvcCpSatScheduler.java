package in.gov.aicte.evc.scheduler;

import com.google.ortools.Loader;
import com.google.ortools.sat.*;
import in.gov.aicte.evc.entity.EvcInspection;
import in.gov.aicte.evc.scheduler.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Core CP-SAT constraint programming solver for the EVC Scheduling Engine.
 * Formulates and solves inspection committee scheduling with dynamic durations,
 * dynamic committee sizes, Sunday exclusion (Sunday is a mandatory non-inspection day),
 * and expert availability.
 */
@Component
public class EvcCpSatScheduler {

    private static final Logger log = LoggerFactory.getLogger(EvcCpSatScheduler.class);

    static {
        Loader.loadNativeLibraries();
    }

    /**
     * Solves the scheduling problem instance using Google OR-Tools CP-SAT.
     *
     * @param problem the scheduling problem definition containing inspections and expert availability
     * @return result encapsulating status, scheduled inspections with dates and assigned experts, or diagnostic explanation
     */
    public SchedulingResult solve(SchedulingProblem problem) {
        if (problem == null) {
            return SchedulingResult.invalidInput("Scheduling problem instance must not be null");
        }
        if (problem.getInspections().isEmpty()) {
            return SchedulingResult.invalidInput("Scheduling problem must contain at least one inspection");
        }

        // Validate individual requests
        for (InspectionRequest req : problem.getInspections()) {
            if (req.getEarliestStartDate().isAfter(req.getLatestStartDate())) {
                return SchedulingResult.invalidInput(String.format(
                        "Inspection '%s' has earliest start date (%s) after latest start date (%s)",
                        req.getInspectionId(), req.getEarliestStartDate(), req.getLatestStartDate()));
            }
            if (req.getDurationInDays() <= 0) {
                return SchedulingResult.invalidInput(String.format(
                        "Inspection '%s' has non-positive duration (%d)",
                        req.getInspectionId(), req.getDurationInDays()));
            }
            if (req.getRequiredMemberCount() <= 0) {
                return SchedulingResult.invalidInput(String.format(
                        "Inspection '%s' has non-positive required member count (%d)",
                        req.getInspectionId(), req.getRequiredMemberCount()));
            }
        }

        try {
            return buildAndSolveModel(problem);
        } catch (Exception e) {
            log.error("Unexpected error during CP-SAT schedule formulation or execution", e);
            return SchedulingResult.error("Solver execution error: " + e.getMessage());
        }
    }

    private SchedulingResult buildAndSolveModel(SchedulingProblem problem) {
        CpModel model = new CpModel();

        // 1. Establish reference horizon
        LocalDate globalMinDate = null;
        for (InspectionRequest req : problem.getInspections()) {
            if (globalMinDate == null || req.getEarliestStartDate().isBefore(globalMinDate)) {
                globalMinDate = req.getEarliestStartDate();
            }
        }
        final LocalDate baseDate = globalMinDate;

        // Variables tracking
        Map<String, IntVar> startVars = new LinkedHashMap<>();
        Map<String, IntVar> endVars = new LinkedHashMap<>();
        Map<String, Map<LocalDate, BoolVar>> startAtDateVars = new LinkedHashMap<>();
        Map<String, Map<String, BoolVar>> assignmentVars = new LinkedHashMap<>();
        Map<String, Map<LocalDate, List<LocalDate>>> inspectionDaysByInsp = new LinkedHashMap<>();
        Map<String, List<InspectionRequest>> expertToInspections = new LinkedHashMap<>();

        // 2. Formulate each inspection
        for (InspectionRequest req : problem.getInspections()) {
            String inspId = req.getInspectionId();
            int duration = req.getDurationInDays();
            int eOffset = (int) ChronoUnit.DAYS.between(baseDate, req.getEarliestStartDate());
            int lOffset = (int) ChronoUnit.DAYS.between(baseDate, req.getLatestStartDate());

            IntVar startVar = model.newIntVar(eOffset, lOffset, "start_" + inspId);
            startVars.put(inspId, startVar);

            // Start date indicator booleans
            Map<LocalDate, BoolVar> dateIndicators = new LinkedHashMap<>();
            List<BoolVar> allDateVars = new ArrayList<>();
            List<Long> startDateOffsets = new ArrayList<>();
            List<Long> endDateOffsets = new ArrayList<>();

            LocalDate curr = req.getEarliestStartDate();
            while (!curr.isAfter(req.getLatestStartDate())) {
                BoolVar dateVar = model.newBoolVar("start_" + inspId + "_at_" + curr);
                dateIndicators.put(curr, dateVar);
                allDateVars.add(dateVar);
                startDateOffsets.add(ChronoUnit.DAYS.between(baseDate, curr));

                LocalDate inclusiveEndDate = EvcInspection.calculateInclusiveEndDate(curr, duration);
                endDateOffsets.add(ChronoUnit.DAYS.between(baseDate, inclusiveEndDate));

                List<LocalDate> days = EvcInspection.calculateInspectionDays(curr, duration);
                inspectionDaysByInsp.computeIfAbsent(inspId, k -> new LinkedHashMap<>()).put(curr, days);

                curr = curr.plusDays(1);
            }
            startAtDateVars.put(inspId, dateIndicators);

            // Constraint: Exactly one start date selected
            model.addEquality(LinearExpr.sum(allDateVars.toArray(new BoolVar[0])), 1);

            // Link startVar with selected start date indicator: startVar = sum(startOffset_t * dateVar_t)
            long[] startCoeffs = startDateOffsets.stream().mapToLong(Long::longValue).toArray();
            model.addEquality(startVar, LinearExpr.weightedSum(allDateVars.toArray(new BoolVar[0]), startCoeffs));

            // Link endVar with inclusive end date: endVar = sum(endOffset_t * dateVar_t)
            long minEndOffset = Collections.min(endDateOffsets);
            long maxEndOffset = Collections.max(endDateOffsets);
            IntVar endVar = model.newIntVar(minEndOffset, maxEndOffset, "end_" + inspId);
            long[] endCoeffs = endDateOffsets.stream().mapToLong(Long::longValue).toArray();
            model.addEquality(endVar, LinearExpr.weightedSum(allDateVars.toArray(new BoolVar[0]), endCoeffs));
            endVars.put(inspId, endVar);

            // Committee assignment variables
            Map<String, BoolVar> expertVarsForInsp = new LinkedHashMap<>();
            List<BoolVar> eligibleVars = new ArrayList<>();

            for (String expertId : req.getEligibleExpertIds()) {
                BoolVar y = model.newBoolVar("assign_" + inspId + "_" + expertId);
                expertVarsForInsp.put(expertId, y);
                eligibleVars.add(y);
                expertToInspections.computeIfAbsent(expertId, k -> new ArrayList<>()).add(req);

                // Constraint E: Expert Availability on Actual Non-Sunday Inspection Days
                // If expert is not available on every actual inspection day of candidate start date,
                // then choosing that start date implies expert cannot be assigned: (dateVar_t => not(y))
                ExpertAvailability availability = problem.getExpertAvailability(expertId);
                for (Map.Entry<LocalDate, BoolVar> entry : dateIndicators.entrySet()) {
                    LocalDate startDate = entry.getKey();
                    BoolVar dateVar = entry.getValue();
                    List<LocalDate> requiredDays = inspectionDaysByInsp.get(inspId).get(startDate);

                    if (availability == null || !availability.isAvailableForDates(requiredDays)) {
                        model.addImplication(dateVar, y.not());
                    }
                }
            }
            assignmentVars.put(inspId, expertVarsForInsp);

            // Constraint B: Dynamic committee size - exactly requiredMemberCount distinct experts
            model.addEquality(LinearExpr.sum(eligibleVars.toArray(new BoolVar[0])), req.getRequiredMemberCount());
        }

        // 3. Constraint F: Overlapping assignments prevention across actual inspection days
        // For each expert, no two active inspections may overlap on any actual non-Sunday inspection day.
        // Sunday is non-working, so inspections that only touch or span Sunday without sharing weekday/Saturday
        // inspection days do not conflict.
        for (Map.Entry<String, List<InspectionRequest>> entry : expertToInspections.entrySet()) {
            String expertId = entry.getKey();
            List<InspectionRequest> expertReqs = entry.getValue();
            int n = expertReqs.size();
            if (n <= 1) {
                continue;
            }

            for (int i = 0; i < n; i++) {
                InspectionRequest req1 = expertReqs.get(i);
                BoolVar y1 = assignmentVars.get(req1.getInspectionId()).get(expertId);
                Map<LocalDate, BoolVar> dates1 = startAtDateVars.get(req1.getInspectionId());

                for (int j = i + 1; j < n; j++) {
                    InspectionRequest req2 = expertReqs.get(j);
                    BoolVar y2 = assignmentVars.get(req2.getInspectionId()).get(expertId);
                    Map<LocalDate, BoolVar> dates2 = startAtDateVars.get(req2.getInspectionId());

                    for (Map.Entry<LocalDate, BoolVar> e1 : dates1.entrySet()) {
                        LocalDate d1 = e1.getKey();
                        BoolVar dateVar1 = e1.getValue();
                        List<LocalDate> days1 = inspectionDaysByInsp.get(req1.getInspectionId()).get(d1);

                        for (Map.Entry<LocalDate, BoolVar> e2 : dates2.entrySet()) {
                            LocalDate d2 = e2.getKey();
                            BoolVar dateVar2 = e2.getValue();
                            List<LocalDate> days2 = inspectionDaysByInsp.get(req2.getInspectionId()).get(d2);

                            if (!Collections.disjoint(days1, days2)) {
                                model.addBoolOr(new Literal[] { y1.not(), y2.not(), dateVar1.not(), dateVar2.not() });
                            }
                        }
                    }
                }
            }
        }

        // 4. Simple deterministic objective: minimize sum of start dates
        // Encourages earlier valid scheduling within permissible windows
        model.minimize(LinearExpr.sum(startVars.values().toArray(new IntVar[0])));

        // 5. Execute Solver
        CpSolver solver = new CpSolver();
        if (problem.getTimeLimit() != null && !problem.getTimeLimit().isZero()) {
            solver.getParameters().setMaxTimeInSeconds(problem.getTimeLimit().toSeconds());
        }
        solver.getParameters().setNumWorkers(1);
        solver.getParameters().setRandomSeed(42);

        CpSolverStatus cpStatus = solver.solve(model);

        // 6. Map results
        if (cpStatus == CpSolverStatus.OPTIMAL || cpStatus == CpSolverStatus.FEASIBLE) {
            Map<String, ScheduledInspection> scheduled = new LinkedHashMap<>();
            for (InspectionRequest req : problem.getInspections()) {
                String id = req.getInspectionId();
                int startOffset = (int) solver.value(startVars.get(id));
                LocalDate scheduledStart = baseDate.plusDays(startOffset);
                LocalDate scheduledEnd = EvcInspection.calculateInclusiveEndDate(scheduledStart, req.getDurationInDays());

                Set<String> assigned = new LinkedHashSet<>();
                Map<String, BoolVar> expVars = assignmentVars.get(id);
                if (expVars != null) {
                    for (Map.Entry<String, BoolVar> expEntry : expVars.entrySet()) {
                        if (solver.value(expEntry.getValue()) == 1) {
                            assigned.add(expEntry.getKey());
                        }
                    }
                }
                scheduled.put(id, new ScheduledInspection(id, scheduledStart, scheduledEnd, assigned));
            }
            SchedulingStatus outStatus = (cpStatus == CpSolverStatus.OPTIMAL)
                    ? SchedulingStatus.OPTIMAL
                    : SchedulingStatus.FEASIBLE;
            return SchedulingResult.success(outStatus, scheduled);
        } else if (cpStatus == CpSolverStatus.INFEASIBLE) {
            String diag = diagnoseInfeasibility(problem);
            return SchedulingResult.infeasible(diag);
        } else if (cpStatus == CpSolverStatus.MODEL_INVALID) {
            return SchedulingResult.invalidInput("CP-SAT reported the model specification as invalid");
        } else {
            return SchedulingResult.timeLimitReached(String.format(
                    "Solver stopped with status %s (time limit of %ds reached without proving feasibility or infeasibility)",
                    cpStatus, problem.getTimeLimit().toSeconds()));
        }
    }

    private String diagnoseInfeasibility(SchedulingProblem problem) {
        StringBuilder sb = new StringBuilder("The scheduling problem is mathematically infeasible under given constraints: ");
        List<String> specificIssues = new ArrayList<>();

        for (InspectionRequest req : problem.getInspections()) {
            int poolSize = req.getEligibleExpertIds().size();
            if (req.getRequiredMemberCount() > poolSize) {
                specificIssues.add(String.format(
                        "Inspection '%s' requires %d members, but only %d eligible experts were provided.",
                        req.getInspectionId(), req.getRequiredMemberCount(), poolSize));
            } else {
                // Check if any date in the window has enough available experts
                boolean anyDateFeasible = false;
                LocalDate d = req.getEarliestStartDate();
                while (!d.isAfter(req.getLatestStartDate())) {
                    int availableOnDate = 0;
                    List<LocalDate> inspectionDays = EvcInspection.calculateInspectionDays(d, req.getDurationInDays());
                    for (String expId : req.getEligibleExpertIds()) {
                        ExpertAvailability av = problem.getExpertAvailability(expId);
                        if (av == null || av.isAvailableForDates(inspectionDays)) {
                            availableOnDate++;
                        }
                    }
                    if (availableOnDate >= req.getRequiredMemberCount()) {
                        anyDateFeasible = true;
                        break;
                    }
                    d = d.plusDays(1);
                }
                if (!anyDateFeasible) {
                    specificIssues.add(String.format(
                            "Inspection '%s' cannot be scheduled: no start date in window [%s, %s] has at least %d available eligible experts.",
                            req.getInspectionId(), req.getEarliestStartDate(), req.getLatestStartDate(), req.getRequiredMemberCount()));
                }
            }
        }

        if (!specificIssues.isEmpty()) {
            sb.append(String.join(" ", specificIssues));
        } else {
            sb.append("Resource conflicts or mutually overlapping inspection intervals for shared experts prevent a valid non-overlapping schedule.");
        }
        return sb.toString();
    }
}

