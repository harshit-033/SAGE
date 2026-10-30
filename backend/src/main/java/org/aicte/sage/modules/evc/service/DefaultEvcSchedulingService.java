package org.aicte.sage.modules.evc.service;

import org.aicte.sage.common.audit.AuditService;
import org.aicte.sage.common.exception.ResourceNotFoundException;
import org.aicte.sage.common.security.AuthoritativeAuthorizationService;
import org.aicte.sage.common.security.UserPrincipal;
import org.aicte.sage.modules.evc.dto.*;
import org.aicte.sage.modules.evc.entity.*;
import org.aicte.sage.modules.evc.repository.CommitteeAssignmentRepository;
import org.aicte.sage.modules.evc.repository.EvcInspectionRepository;
import org.aicte.sage.modules.evc.repository.ExpertAvailabilityRepository;
import org.aicte.sage.modules.evc.repository.ExpertRepository;
import org.aicte.sage.modules.evc.scheduler.EvcCpSatScheduler;
import org.aicte.sage.modules.evc.scheduler.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;

@Service
public class DefaultEvcSchedulingService implements EvcSchedulingService {

    private static final Logger log = LoggerFactory.getLogger(DefaultEvcSchedulingService.class);

    private static final int MAX_BATCH_SIZE = 25;
    private static final int MAX_TIME_LIMIT_SECONDS = 60;
    private static final int MIN_TIME_LIMIT_SECONDS = 1;

    private final EvcInspectionRepository inspectionRepository;
    private final ExpertRepository expertRepository;
    private final ExpertAvailabilityRepository availabilityRepository;
    private final CommitteeAssignmentRepository assignmentRepository;
    private final EvcCpSatScheduler scheduler;
    private final SchedulePersistenceService persistenceService;
    private final AuthoritativeAuthorizationService authService;
    private final AuditService auditService;

    public DefaultEvcSchedulingService(
            EvcInspectionRepository inspectionRepository,
            ExpertRepository expertRepository,
            ExpertAvailabilityRepository availabilityRepository,
            CommitteeAssignmentRepository assignmentRepository,
            EvcCpSatScheduler scheduler,
            SchedulePersistenceService persistenceService,
            AuthoritativeAuthorizationService authService,
            AuditService auditService
    ) {
        this.inspectionRepository = inspectionRepository;
        this.expertRepository = expertRepository;
        this.availabilityRepository = availabilityRepository;
        this.assignmentRepository = assignmentRepository;
        this.scheduler = scheduler;
        this.persistenceService = persistenceService;
        this.authService = authService;
        this.auditService = auditService;
    }

    @Override
    public SchedulingResponseDto schedule(SchedulingRequestDto request, UserPrincipal principal) {
        if (request == null || request.getInspectionIds() == null || request.getInspectionIds().isEmpty()) {
            return SchedulingResponseDto.invalidInput("Scheduling request must contain at least one inspection ID");
        }

        if (request.getInspectionIds().size() > MAX_BATCH_SIZE) {
            return SchedulingResponseDto.invalidInput(String.format(
                    "Batch size exceeds maximum permitted limit (%d inspections per request)", MAX_BATCH_SIZE));
        }

        // Bounded solver time limit
        int boundedSeconds = 10;
        if (request.getTimeLimitSeconds() != null) {
            boundedSeconds = Math.max(MIN_TIME_LIMIT_SECONDS, Math.min(MAX_TIME_LIMIT_SECONDS, request.getTimeLimitSeconds()));
        }

        // 1. Load persisted inspection records
        List<EvcInspection> inspections = inspectionRepository.findAllById(request.getInspectionIds());
        Map<UUID, EvcInspection> inspectionsById = inspections.stream()
                .collect(Collectors.toMap(EvcInspection::getId, Function.identity()));

        List<UUID> missingIds = request.getInspectionIds().stream()
                .filter(id -> !inspectionsById.containsKey(id))
                .toList();

        if (!missingIds.isEmpty()) {
            return SchedulingResponseDto.invalidInput("The following inspection IDs do not exist: " + missingIds);
        }

        // 2. Authoritative authorization check for each inspection's institution
        for (EvcInspection inspection : inspections) {
            authService.checkAccessToInstitution(principal, inspection.getInstitution().getId(), "EVC_SCHEDULING");
        }

        // 3. Rescheduling Policy Check: Fail fast if any inspection is already SCHEDULED and allowRescheduling is false
        for (EvcInspection inspection : inspections) {
            if (inspection.getStatus() == InspectionStatus.SCHEDULED && !request.isAllowRescheduling()) {
                return SchedulingResponseDto.invalidInput(String.format(
                        "Inspection %s is already SCHEDULED. Rescheduling must be explicitly enabled.",
                        inspection.getId()));
            }
        }

        // 4. Resolve eligible experts with strict Conflict-of-Interest (COI) isolation
        List<Expert> activeExperts = expertRepository.findByActiveTrue();
        Map<UUID, Expert> activeExpertsById = activeExperts.stream()
                .collect(Collectors.toMap(Expert::getId, Function.identity()));

        Map<UUID, Set<UUID>> eligibleExpertIdsByInspection = new LinkedHashMap<>();
        Set<UUID> allEligibleExpertIds = new HashSet<>();

        for (EvcInspection inspection : inspections) {
            Set<UUID> specifiedEligible = request.getEligibleExpertIdsByInspectionId().get(inspection.getId());
            Set<UUID> eligible;

            if (specifiedEligible != null && !specifiedEligible.isEmpty()) {
                eligible = new HashSet<>(specifiedEligible);
            } else {
                eligible = new HashSet<>(activeExpertsById.keySet());
            }

            // Conflict of Interest (COI) Isolation:
            // Exclude any expert who is formally affiliated with the inspected institution
            UUID targetInstId = inspection.getInstitution().getId();
            eligible.removeIf(expId -> {
                Expert exp = activeExpertsById.get(expId);
                return exp != null && exp.getAffiliatedInstitution() != null &&
                        targetInstId.equals(exp.getAffiliatedInstitution().getId());
            });

            eligibleExpertIdsByInspection.put(inspection.getId(), eligible);
            allEligibleExpertIds.addAll(eligible);
        }

        // Load any expert entities that were explicitly requested but not in active pool
        Set<UUID> expertsToFetch = allEligibleExpertIds.stream()
                .filter(id -> !activeExpertsById.containsKey(id))
                .collect(Collectors.toSet());

        Map<UUID, Expert> allEligibleExpertsById = new HashMap<>(activeExpertsById);
        if (!expertsToFetch.isEmpty()) {
            List<Expert> additionalExperts = expertRepository.findAllById(expertsToFetch);
            for (Expert exp : additionalExperts) {
                allEligibleExpertsById.put(exp.getId(), exp);
            }
        }

        // 5. Resolve Expert Availability across the scheduling horizon
        LocalDate horizonStart = inspections.stream()
                .map(EvcInspection::getEarliestStartDate)
                .min(LocalDate::compareTo)
                .orElse(LocalDate.now());

        LocalDate horizonEnd = inspections.stream()
                .map(i -> EvcInspection.calculateInclusiveEndDate(i.getLatestStartDate(), i.getDurationInDays()))
                .max(LocalDate::compareTo)
                .orElse(LocalDate.now().plusMonths(1));

        Set<UUID> expertsToQueryAvailability = allEligibleExpertIds.stream()
                .filter(id -> !request.getExpertAvailabilityOverrides().containsKey(id))
                .collect(Collectors.toSet());

        Map<UUID, Set<LocalDate>> loadedAvailabilityByExpertId = new HashMap<>();
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
        // If an expert has no availability records, they are treated as strictly unavailable.
        Map<String, ExpertAvailability> solverAvailabilityMap = new HashMap<>();
        for (UUID expertId : allEligibleExpertIds) {
            Set<LocalDate> availableDates;
            if (request.getExpertAvailabilityOverrides().containsKey(expertId)) {
                availableDates = request.getExpertAvailabilityOverrides().get(expertId);
            } else {
                availableDates = loadedAvailabilityByExpertId.getOrDefault(expertId, Collections.emptySet());
            }

            String expIdStr = expertId.toString();
            solverAvailabilityMap.put(expIdStr, ExpertAvailability.of(expIdStr, availableDates));
        }

        // 6. Convert to detached solver InspectionRequest models
        List<InspectionRequest> solverRequests = new ArrayList<>();
        for (EvcInspection inspection : inspections) {
            Set<String> eligibleExpertIdStrs = eligibleExpertIdsByInspection.get(inspection.getId()).stream()
                    .map(UUID::toString)
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

        // 7. Formulate detached SchedulingProblem
        SchedulingProblem problem = new SchedulingProblem(
                solverRequests,
                solverAvailabilityMap,
                Duration.ofSeconds(boundedSeconds)
        );

        // 8. Invoke CP-SAT Solver outside of any DB transaction
        log.info("Starting CP-SAT solver for {} inspections and {} candidate experts (time limit: {}s)...",
                solverRequests.size(), allEligibleExpertIds.size(), boundedSeconds);

        auditService.recordEvent(
                "EVC_SCHEDULE_GENERATION_INITIATED",
                principal.getUsername(),
                inspections.get(0).getInstitution().getId(),
                "EVC_INSPECTION",
                request.getInspectionIds().toString(),
                "SCHEDULE_COMPUTE",
                null,
                String.format("EVC scheduling solver initiated for %d inspection(s) with %ds time limit.",
                        inspections.size(), boundedSeconds)
        );

        long startTime = System.currentTimeMillis();
        SchedulingResult solverResult = scheduler.solve(problem);
        long solveDurationMs = System.currentTimeMillis() - startTime;

        log.info("CP-SAT solver finished with status {} in {} ms", solverResult.getStatus(), solveDurationMs);

        // 9. Handle Infeasible or Error Outcomes
        if (!solverResult.isSuccess()) {
            auditService.recordEvent(
                    "EVC_SCHEDULE_SOLVER_UNSUCCESSFUL",
                    principal.getUsername(),
                    inspections.get(0).getInstitution().getId(),
                    "EVC_INSPECTION",
                    request.getInspectionIds().toString(),
                    "SCHEDULE_FAILED",
                    null,
                    String.format("EVC scheduling solver outcome: %s. Details: %s",
                            solverResult.getStatus(), solverResult.getExplanation())
            );

            return switch (solverResult.getStatus()) {
                case INFEASIBLE -> SchedulingResponseDto.infeasible(
                        inspections.size(), solveDurationMs, solverResult.getExplanation());
                case TIME_LIMIT_REACHED -> SchedulingResponseDto.timeLimitReached(
                        inspections.size(), solveDurationMs, solverResult.getExplanation());
                case INVALID_INPUT -> SchedulingResponseDto.invalidInput(
                        solverResult.getExplanation());
                default -> SchedulingResponseDto.error(
                        solverResult.getExplanation());
            };
        }

        // 10. Persist Successful Schedule atomically within a transaction
        try {
            List<ScheduledInspectionResultDto> scheduledDtos = persistenceService.persistScheduledSchedule(
                    solverResult,
                    inspectionsById,
                    allEligibleExpertsById,
                    request.isAllowRescheduling()
            );

            auditService.recordEvent(
                    "EVC_SCHEDULE_PERSISTED",
                    principal.getUsername(),
                    inspections.get(0).getInstitution().getId(),
                    "EVC_INSPECTION",
                    request.getInspectionIds().toString(),
                    "SCHEDULE_COMMITTED",
                    null,
                    String.format("Successfully scheduled and committed committee assignments for %d inspection(s).",
                            scheduledDtos.size())
            );

            return SchedulingResponseDto.success(
                    solverResult.getStatus(),
                    inspections.size(),
                    solveDurationMs,
                    scheduledDtos
            );
        } catch (Exception e) {
            log.error("Failed to persist scheduled results", e);
            return SchedulingResponseDto.error(
                    "Database error while persisting schedule: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<EvcInspectionDto> getPendingInspections(UserPrincipal principal) {
        List<EvcInspection> inspections = inspectionRepository.findByStatus(InspectionStatus.PENDING);
        return filterAndMapInspections(inspections, principal);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EvcInspectionDto> getScheduledInspections(UserPrincipal principal) {
        List<EvcInspection> inspections = inspectionRepository.findByStatus(InspectionStatus.SCHEDULED);
        return filterAndMapInspections(inspections, principal);
    }

    @Override
    @Transactional(readOnly = true)
    public EvcInspectionDto getInspectionById(UUID id, UserPrincipal principal) {
        EvcInspection inspection = inspectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EVC inspection with ID '" + id + "' not found"));

        authService.checkAccessToInstitution(principal, inspection.getInstitution().getId(), "EVC_READ");

        List<AssignedExpertDto> committee = loadCommitteeDto(inspection);
        return EvcInspectionDto.fromEntity(inspection, committee);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssignedExpertDto> getCommitteeAssignments(UUID inspectionId, UserPrincipal principal) {
        EvcInspection inspection = inspectionRepository.findById(inspectionId)
                .orElseThrow(() -> new ResourceNotFoundException("EVC inspection with ID '" + inspectionId + "' not found"));

        authService.checkAccessToInstitution(principal, inspection.getInstitution().getId(), "EVC_READ");

        return loadCommitteeDto(inspection);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ExpertDto> getActiveExperts(Pageable pageable) {
        Page<Expert> expertsPage = expertRepository.findByActiveTrue(pageable);
        List<UUID> expertIds = expertsPage.getContent().stream().map(Expert::getId).toList();

        LocalDate now = LocalDate.now();
        List<ExpertAvailabilityRecord> records = availabilityRepository
                .findByExpertIdInAndAvailableDateBetween(expertIds, now, now.plusMonths(2));

        Map<UUID, Set<LocalDate>> availMap = new HashMap<>();
        for (ExpertAvailabilityRecord r : records) {
            availMap.computeIfAbsent(r.getExpert().getId(), k -> new HashSet<>()).add(r.getAvailableDate());
        }

        List<ExpertDto> dtos = expertsPage.getContent().stream()
                .map(exp -> ExpertDto.fromEntity(exp, availMap.get(exp.getId())))
                .toList();

        return new PageImpl<>(dtos, pageable, expertsPage.getTotalElements());
    }

    @Override
    @Transactional
    public void cancelInspection(UUID inspectionId, String reason, UserPrincipal principal) {
        EvcInspection inspection = inspectionRepository.findById(inspectionId)
                .orElseThrow(() -> new ResourceNotFoundException("EVC inspection with ID '" + inspectionId + "' not found"));

        authService.checkAccessToInstitution(principal, inspection.getInstitution().getId(), "EVC_CANCEL");

        inspection.setStatus(InspectionStatus.CANCELLED);
        inspection.setSpecialInstructions("Cancellation: " + (reason != null ? reason : "Administrative cancellation"));
        inspectionRepository.save(inspection);

        auditService.recordEvent(
                "EVC_INSPECTION_CANCELLED",
                principal.getUsername(),
                inspection.getInstitution().getId(),
                "EVC_INSPECTION",
                inspectionId.toString(),
                "INSPECTION_CANCEL",
                null,
                "EVC inspection cancelled. Reason: " + reason
        );
    }

    private List<EvcInspectionDto> filterAndMapInspections(List<EvcInspection> inspections, UserPrincipal principal) {
        boolean isAdmin = principal.hasRole("ROLE_AICTE_ADMIN") || principal.hasRole("ROLE_EVC_COORDINATOR");

        return inspections.stream()
                .filter(i -> isAdmin || authService.canAccessInstitution(principal, i.getInstitution().getId()))
                .map(i -> EvcInspectionDto.fromEntity(i, loadCommitteeDto(i)))
                .toList();
    }

    private List<AssignedExpertDto> loadCommitteeDto(EvcInspection inspection) {
        List<CommitteeAssignment> assignments = assignmentRepository.findByInspection(inspection);
        return assignments.stream()
                .map(a -> new AssignedExpertDto(
                        a.getExpert().getId(),
                        a.getExpert().getName(),
                        a.getExpert().getDiscipline(),
                        a.getRole(),
                        a.getStatus()
                ))
                .toList();
    }
}
