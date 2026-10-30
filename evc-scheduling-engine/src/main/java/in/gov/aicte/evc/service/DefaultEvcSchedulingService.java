package in.gov.aicte.evc.service;

import in.gov.aicte.evc.entity.*;
import in.gov.aicte.evc.repository.CommitteeAssignmentRepository;
import in.gov.aicte.evc.repository.EvcInspectionRepository;
import in.gov.aicte.evc.repository.ExpertAvailabilityRepository;
import in.gov.aicte.evc.repository.ExpertRepository;
import in.gov.aicte.evc.scheduler.EvcCpSatScheduler;
import in.gov.aicte.evc.scheduler.model.ExpertAvailability;
import in.gov.aicte.evc.scheduler.model.InspectionRequest;
import in.gov.aicte.evc.scheduler.model.SchedulingProblem;
import in.gov.aicte.evc.scheduler.model.SchedulingResult;
import in.gov.aicte.evc.service.dto.ScheduledInspectionResultDto;
import in.gov.aicte.evc.service.dto.SchedulingRequest;
import in.gov.aicte.evc.service.dto.SchedulingServiceResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Default implementation of {@link EvcSchedulingService}.
 * Orchestrates scheduling workflow across persistence and Google OR-Tools CP-SAT solver.
 * Ensures the solver executes outside of any database transaction, preventing connection starvation.
 */
@Service
public class DefaultEvcSchedulingService implements EvcSchedulingService {

    private static final Logger log = LoggerFactory.getLogger(DefaultEvcSchedulingService.class);

    private final EvcInspectionRepository inspectionRepository;
    private final ExpertRepository expertRepository;
    private final ExpertAvailabilityRepository availabilityRepository;
    private final EvcCpSatScheduler scheduler;
    private final SchedulePersistenceService persistenceService;

    public DefaultEvcSchedulingService(EvcInspectionRepository inspectionRepository,
                                      ExpertRepository expertRepository,
                                      ExpertAvailabilityRepository availabilityRepository,
                                      EvcCpSatScheduler scheduler,
                                      SchedulePersistenceService persistenceService) {
        this.inspectionRepository = inspectionRepository;
        this.expertRepository = expertRepository;
        this.availabilityRepository = availabilityRepository;
        this.scheduler = scheduler;
        this.persistenceService = persistenceService;
    }

    @Override
    public SchedulingServiceResult schedule(SchedulingRequest request) {
        if (request == null || request.getInspectionIds() == null || request.getInspectionIds().isEmpty()) {
            return SchedulingServiceResult.invalidInput("Scheduling request must contain at least one inspection ID");
        }

        // 1. Load persisted inspection records
        List<EvcInspection> inspections = inspectionRepository.findAllById(request.getInspectionIds());
        Map<Long, EvcInspection> inspectionsById = inspections.stream()
                .collect(Collectors.toMap(EvcInspection::getId, Function.identity()));

        List<Long> missingIds = request.getInspectionIds().stream()
                .filter(id -> !inspectionsById.containsKey(id))
                .toList();

        if (!missingIds.isEmpty()) {
            return SchedulingServiceResult.invalidInput("The following inspection IDs do not exist: " + missingIds);
        }

        // 2. Rescheduling Policy Check: Fail fast if any inspection is already SCHEDULED and allowRescheduling is false
        for (EvcInspection inspection : inspections) {
            if (inspection.getStatus() == InspectionStatus.SCHEDULED && !request.isAllowRescheduling()) {
                return SchedulingServiceResult.invalidInput(String.format(
                        "Inspection %d is already SCHEDULED. Rescheduling must be explicitly enabled.",
                        inspection.getId()));
            }
        }

        // 3. Resolve eligible experts for each inspection
        // Preload active experts as fallback candidate pool
        List<Expert> activeExperts = expertRepository.findByActiveTrue();
        Map<Long, Expert> activeExpertsById = activeExperts.stream()
                .collect(Collectors.toMap(Expert::getId, Function.identity()));

        Map<Long, Set<Long>> eligibleExpertIdsByInspection = new LinkedHashMap<>();
        Set<Long> allEligibleExpertIds = new HashSet<>();

        for (EvcInspection inspection : inspections) {
            Set<Long> specifiedEligible = request.getEligibleExpertIdsByInspectionId().get(inspection.getId());
            Set<Long> eligible;

            if (specifiedEligible != null && !specifiedEligible.isEmpty()) {
                // Use explicit eligible pool provided in the request
                eligible = new HashSet<>(specifiedEligible);
            } else {
                // Default to all active experts
                eligible = new HashSet<>(activeExpertsById.keySet());
            }

            eligibleExpertIdsByInspection.put(inspection.getId(), eligible);
            allEligibleExpertIds.addAll(eligible);
        }

        // Load any expert entities that were explicitly requested but not in active pool
        Set<Long> expertsToFetch = allEligibleExpertIds.stream()
                .filter(id -> !activeExpertsById.containsKey(id))
                .collect(Collectors.toSet());

        Map<Long, Expert> allEligibleExpertsById = new HashMap<>(activeExpertsById);
        if (!expertsToFetch.isEmpty()) {
            List<Expert> additionalExperts = expertRepository.findAllById(expertsToFetch);
            for (Expert exp : additionalExperts) {
                allEligibleExpertsById.put(exp.getId(), exp);
            }
        }

        // 4. Resolve Expert Availability across the scheduling horizon
        // Determine earliest and latest dates across all inspections
        LocalDate horizonStart = inspections.stream()
                .map(EvcInspection::getEarliestStartDate)
                .min(LocalDate::compareTo)
                .orElse(LocalDate.now());

        LocalDate horizonEnd = inspections.stream()
                .map(i -> EvcInspection.calculateInclusiveEndDate(i.getLatestStartDate(), i.getDurationInDays()))
                .max(LocalDate::compareTo)
                .orElse(LocalDate.now().plusMonths(1));

        // Batch load persisted availability for all eligible experts not overridden by request
        Set<Long> expertsToQueryAvailability = allEligibleExpertIds.stream()
                .filter(id -> !request.getExpertAvailabilityOverrides().containsKey(id))
                .collect(Collectors.toSet());

        Map<Long, Set<LocalDate>> loadedAvailabilityByExpertId = new HashMap<>();
        if (!expertsToQueryAvailability.isEmpty()) {
            List<ExpertAvailabilityRecord> records = availabilityRepository
                    .findByExpertIdInAndAvailableDateBetween(expertsToQueryAvailability, horizonStart, horizonEnd);
            for (ExpertAvailabilityRecord record : records) {
                loadedAvailabilityByExpertId
                        .computeIfAbsent(record.getExpert().getId(), k -> new HashSet<>())
                        .add(record.getAvailableDate());
            }
        }

        // Build detached solver ExpertAvailability objects
        // Rule: If an expert has no availability data, their available set is empty (unavailable).
        Map<String, ExpertAvailability> solverAvailabilityMap = new HashMap<>();
        for (Long expertId : allEligibleExpertIds) {
            Set<LocalDate> availableDates;
            if (request.getExpertAvailabilityOverrides().containsKey(expertId)) {
                availableDates = request.getExpertAvailabilityOverrides().get(expertId);
            } else {
                availableDates = loadedAvailabilityByExpertId.getOrDefault(expertId, Collections.emptySet());
            }

            String expIdStr = expertId.toString();
            solverAvailabilityMap.put(expIdStr, ExpertAvailability.of(expIdStr, availableDates));
        }

        // 5. Convert to detached solver InspectionRequest models
        List<InspectionRequest> solverRequests = new ArrayList<>();
        for (EvcInspection inspection : inspections) {
            Set<String> eligibleExpertIdStrs = eligibleExpertIdsByInspection.get(inspection.getId()).stream()
                    .map(Object::toString)
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            solverRequests.add(new InspectionRequest(
                    inspection.getId().toString(),
                    inspection.getDurationInDays(),
                    inspection.getRequiredMemberCount(),
                    inspection.getEarliestStartDate(),
                    inspection.getLatestStartDate(),
                    eligibleExpertIdStrs
            ));
        }

        // 6. Formulate detached SchedulingProblem
        SchedulingProblem problem = new SchedulingProblem(
                solverRequests,
                solverAvailabilityMap,
                request.getTimeLimit()
        );

        // 7. Invoke CP-SAT Solver outside of any DB transaction
        log.info("Starting CP-SAT solver for {} inspections and {} eligible experts...",
                solverRequests.size(), allEligibleExpertIds.size());

        long startTime = System.currentTimeMillis();
        SchedulingResult solverResult = scheduler.solve(problem);
        long solveDurationMs = System.currentTimeMillis() - startTime;

        log.info("CP-SAT solver finished with status {} in {} ms", solverResult.getStatus(), solveDurationMs);

        // 8. Handle Solver Outcomes
        if (!solverResult.isSuccess()) {
            // For INFEASIBLE, TIME_LIMIT_REACHED, MODEL_INVALID, or ERROR:
            // Do NOT touch the database; do not alter existing valid assignments or statuses.
            return switch (solverResult.getStatus()) {
                case INFEASIBLE -> SchedulingServiceResult.infeasible(
                        solverResult.getExplanation(), inspections.size(), solveDurationMs);
                case TIME_LIMIT_REACHED -> SchedulingServiceResult.timeLimitReached(
                        solverResult.getExplanation(), inspections.size(), solveDurationMs);
                case INVALID_INPUT -> SchedulingServiceResult.invalidInput(
                        solverResult.getExplanation());
                default -> SchedulingServiceResult.error(
                        solverResult.getExplanation(), inspections.size(), solveDurationMs);
            };
        }

        // 9. Persist Successful Schedule atomically within a transaction
        try {
            List<ScheduledInspectionResultDto> scheduledDtos = persistenceService.persistScheduledSchedule(
                    solverResult,
                    inspectionsById,
                    allEligibleExpertsById,
                    request.isAllowRescheduling()
            );

            return SchedulingServiceResult.success(
                    solverResult.getStatus(),
                    inspections.size(),
                    solveDurationMs,
                    scheduledDtos
            );
        } catch (Exception e) {
            log.error("Failed to persist scheduled results", e);
            return SchedulingServiceResult.error(
                    "Database error while persisting schedule: " + e.getMessage(),
                    inspections.size(),
                    solveDurationMs
            );
        }
    }
}

