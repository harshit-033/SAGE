package in.gov.aicte.evc.devdashboard;

import in.gov.aicte.evc.devdashboard.dto.*;
import in.gov.aicte.evc.entity.EvcInspection;
import in.gov.aicte.evc.scheduler.EvcCpSatScheduler;
import in.gov.aicte.evc.scheduler.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDate;
import java.util.*;

/**
 * Temporary development-only REST controller exposing scheduling endpoints
 * for the interactive Dev Dashboard.
 * Active exclusively under the 'dev-dashboard' Spring profile.
 */
@Profile("dev-dashboard")
@RestController
@RequestMapping("/dev/dashboard/api")
public class DevDashboardController {

    private static final Logger log = LoggerFactory.getLogger(DevDashboardController.class);
    private static final int MAX_INSPECTIONS = 50;
    private static final int MAX_TIME_LIMIT_SECONDS = 30;
    private static final int DEFAULT_TIME_LIMIT_SECONDS = 5;

    private final EvcCpSatScheduler scheduler;

    public DevDashboardController(EvcCpSatScheduler scheduler) {
        this.scheduler = scheduler;
    }

    /**
     * Executes the CP-SAT solver against developer-configured inspection and availability data.
     */
    @PostMapping("/schedule")
    public ResponseEntity<?> schedule(@RequestBody DashboardScheduleRequest request) {
        if (request == null || request.getInspections() == null || request.getInspections().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Request must contain at least one inspection",
                    "status", HttpStatus.BAD_REQUEST.value()
            ));
        }

        if (request.getInspections().size() > MAX_INSPECTIONS) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Exceeded maximum demonstration limit of " + MAX_INSPECTIONS + " inspections",
                    "status", HttpStatus.BAD_REQUEST.value()
            ));
        }

        // Validate individual inspections
        List<InspectionRequest> solverRequests = new ArrayList<>();
        Set<String> seenIds = new HashSet<>();
        int totalRequestedAssignments = 0;

        for (DashboardInspectionDto dto : request.getInspections()) {
            if (dto.getInspectionId() == null || dto.getInspectionId().isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Inspection ID must not be blank"));
            }
            if (!seenIds.add(dto.getInspectionId())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Duplicate inspection ID: " + dto.getInspectionId()));
            }
            if (dto.getDurationInDays() <= 0) {
                return ResponseEntity.badRequest().body(Map.of("error", "Duration must be positive for inspection " + dto.getInspectionId()));
            }
            if (dto.getRequiredMemberCount() <= 0) {
                return ResponseEntity.badRequest().body(Map.of("error", "Required member count must be positive for inspection " + dto.getInspectionId()));
            }
            if (dto.getEarliestStartDate() == null || dto.getLatestStartDate() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Start date window bounds cannot be null for inspection " + dto.getInspectionId()));
            }
            if (dto.getEarliestStartDate().isAfter(dto.getLatestStartDate())) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", String.format("Earliest start date (%s) is after latest start date (%s) for inspection %s",
                                dto.getEarliestStartDate(), dto.getLatestStartDate(), dto.getInspectionId())
                ));
            }

            Collection<String> eligible = dto.getEligibleExpertIds() != null ? dto.getEligibleExpertIds() : Collections.emptyList();
            totalRequestedAssignments += dto.getRequiredMemberCount();

            solverRequests.add(new InspectionRequest(
                    dto.getInspectionId(),
                    dto.getDurationInDays(),
                    dto.getRequiredMemberCount(),
                    dto.getEarliestStartDate(),
                    dto.getLatestStartDate(),
                    eligible
            ));
        }

        // Convert expert availabilities
        Map<String, ExpertAvailability> availabilityMap = new HashMap<>();
        if (request.getExpertAvailabilities() != null) {
            for (DashboardExpertAvailabilityDto expDto : request.getExpertAvailabilities()) {
                if (expDto.getExpertId() != null && !expDto.getExpertId().isBlank()) {
                    List<LocalDate> dates = expDto.getAvailableDates() != null ? expDto.getAvailableDates() : Collections.emptyList();
                    availabilityMap.put(expDto.getExpertId(), ExpertAvailability.of(expDto.getExpertId(), dates));
                }
            }
        }

        int timeLimitSec = (request.getTimeLimitSeconds() != null && request.getTimeLimitSeconds() > 0)
                ? Math.min(request.getTimeLimitSeconds(), MAX_TIME_LIMIT_SECONDS)
                : DEFAULT_TIME_LIMIT_SECONDS;

        SchedulingProblem problem = new SchedulingProblem(solverRequests, availabilityMap, Duration.ofSeconds(timeLimitSec));

        long startTime = System.currentTimeMillis();
        SchedulingResult result = scheduler.solve(problem);
        long solveTimeMs = System.currentTimeMillis() - startTime;

        // Build response DTO
        List<DashboardScheduledInspectionDto> scheduledDtos = new ArrayList<>();
        if (result.isSuccess()) {
            for (InspectionRequest req : solverRequests) {
                ScheduledInspection si = result.getScheduledInspection(req.getInspectionId());
                if (si != null) {
                    List<LocalDate> occupiedDates = EvcInspection.calculateInspectionDays(
                            si.getScheduledStartDate(), req.getDurationInDays());
                    scheduledDtos.add(new DashboardScheduledInspectionDto(
                            si.getInspectionId(),
                            si.getScheduledStartDate(),
                            si.getScheduledEndDate(),
                            req.getDurationInDays(),
                            req.getRequiredMemberCount(),
                            new ArrayList<>(si.getAssignedExpertIds()),
                            occupiedDates
                    ));
                }
            }
        }

        DashboardScheduleResponse response = new DashboardScheduleResponse(
                result.getStatus(),
                result.isSuccess(),
                result.getExplanation(),
                solverRequests.size(),
                totalRequestedAssignments,
                scheduledDtos.size(),
                solveTimeMs,
                scheduledDtos
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Returns pre-configured test scenarios for one-click demonstration.
     */
    @GetMapping("/scenarios")
    public List<DemoScenarioDto> getDemoScenarios() {
        return List.of(
                createScenario1(),
                createScenario2(),
                createScenario3(),
                createScenario4(),
                createScenario5()
        );
    }

    private DemoScenarioDto createScenario1() {
        // Feasible: Mixed Durations (3 vs 4 days) & Committee Sizes (2 vs 3 members)
        DashboardInspectionDto i1 = new DashboardInspectionDto(
                "INSP-ENGG-01",
                3,
                2,
                LocalDate.of(2026, 11, 10),
                LocalDate.of(2026, 11, 15),
                List.of("EXP-01", "EXP-02", "EXP-03")
        );
        DashboardInspectionDto i2 = new DashboardInspectionDto(
                "INSP-POLY-02",
                4,
                3,
                LocalDate.of(2026, 11, 10),
                LocalDate.of(2026, 11, 15),
                List.of("EXP-04", "EXP-05", "EXP-06", "EXP-07")
        );

        List<DashboardExpertAvailabilityDto> avails = new ArrayList<>();
        for (int i = 1; i <= 7; i++) {
            avails.add(createFullAvailability("EXP-0" + i, LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 25)));
        }

        DashboardScheduleRequest req = new DashboardScheduleRequest(List.of(i1, i2), avails, 5);
        return new DemoScenarioDto(
                "scenario-1",
                "1. Dynamic Durations & Committee Sizes",
                "Feasible",
                "Schedules two inspections with different durations (3 vs 4 days) and different committee sizes (2 vs 3 members).",
                req
        );
    }

    private DemoScenarioDto createScenario2() {
        // Shared Expert Conflict: forces date staggering
        DashboardInspectionDto i1 = new DashboardInspectionDto(
                "INSP-ALPHA",
                3,
                2,
                LocalDate.of(2026, 11, 10),
                LocalDate.of(2026, 11, 12),
                List.of("EXP-LEAD", "EXP-MEMBER-A")
        );
        DashboardInspectionDto i2 = new DashboardInspectionDto(
                "INSP-BETA",
                3,
                2,
                LocalDate.of(2026, 11, 10),
                LocalDate.of(2026, 11, 16),
                List.of("EXP-LEAD", "EXP-MEMBER-B")
        );

        List<DashboardExpertAvailabilityDto> avails = List.of(
                createFullAvailability("EXP-LEAD", LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 20)),
                createFullAvailability("EXP-MEMBER-A", LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 20)),
                createFullAvailability("EXP-MEMBER-B", LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 20))
        );

        DashboardScheduleRequest req = new DashboardScheduleRequest(List.of(i1, i2), avails, 5);
        return new DemoScenarioDto(
                "scenario-2",
                "2. Shared Expert Conflict (Date Staggering)",
                "Conflict Resolution",
                "Both inspections require common expert EXP-LEAD. The solver automatically staggers INSP-BETA to avoid overlap.",
                req
        );
    }

    private DemoScenarioDto createScenario3() {
        // Infeasible: Committee size exceeds pool
        DashboardInspectionDto i1 = new DashboardInspectionDto(
                "INSP-DEFICIENT",
                3,
                4, // Requires 4 members
                LocalDate.of(2026, 11, 10),
                LocalDate.of(2026, 11, 15),
                List.of("EXP-01", "EXP-02") // Only 2 experts
        );

        List<DashboardExpertAvailabilityDto> avails = List.of(
                createFullAvailability("EXP-01", LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 20)),
                createFullAvailability("EXP-02", LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 20))
        );

        DashboardScheduleRequest req = new DashboardScheduleRequest(List.of(i1), avails, 5);
        return new DemoScenarioDto(
                "scenario-3",
                "3. Infeasible: Pool Size Deficient",
                "Infeasible",
                "Requested committee size (4) exceeds the eligible expert pool (2). Mathematically proven infeasible with clear diagnostics.",
                req
        );
    }

    private DemoScenarioDto createScenario4() {
        // Infeasible: Expert Calendar Blackouts
        DashboardInspectionDto i1 = new DashboardInspectionDto(
                "INSP-BLACKOUT",
                3,
                2,
                LocalDate.of(2026, 11, 10),
                LocalDate.of(2026, 11, 12),
                List.of("EXP-01", "EXP-02")
        );

        // EXP-02 has blackouts on Nov 11 and 13, leaving no 3-day continuous span
        List<LocalDate> exp2Dates = List.of(
                LocalDate.of(2026, 11, 10),
                LocalDate.of(2026, 11, 12),
                LocalDate.of(2026, 11, 14)
        );

        List<DashboardExpertAvailabilityDto> avails = List.of(
                createFullAvailability("EXP-01", LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 20)),
                new DashboardExpertAvailabilityDto("EXP-02", exp2Dates)
        );

        DashboardScheduleRequest req = new DashboardScheduleRequest(List.of(i1), avails, 5);
        return new DemoScenarioDto(
                "scenario-4",
                "4. Infeasible: Expert Calendar Blackouts",
                "Infeasible",
                "Expert EXP-02 has calendar blackouts preventing any 3-consecutive-day assignment within the permissible window.",
                req
        );
    }

    private DemoScenarioDto createScenario5() {
        // Mixed duration: 4 days and 2 days
        DashboardInspectionDto i1 = new DashboardInspectionDto(
                "INSP-LONG-4DAYS",
                4,
                1,
                LocalDate.of(2026, 11, 10),
                LocalDate.of(2026, 11, 15),
                List.of("EXP-SHARED")
        );
        DashboardInspectionDto i2 = new DashboardInspectionDto(
                "INSP-SHORT-2DAYS",
                2,
                1,
                LocalDate.of(2026, 11, 10),
                LocalDate.of(2026, 11, 18),
                List.of("EXP-SHARED")
        );

        List<DashboardExpertAvailabilityDto> avails = List.of(
                createFullAvailability("EXP-SHARED", LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 25))
        );

        DashboardScheduleRequest req = new DashboardScheduleRequest(List.of(i1, i2), avails, 5);
        return new DemoScenarioDto(
                "scenario-5",
                "5. Mixed-Duration Consecutive Days",
                "Feasible",
                "Single expert scheduled across a 4-day and a 2-day inspection with consecutive-day occupancy and inclusive end-date math.",
                req
        );
    }

    private DashboardExpertAvailabilityDto createFullAvailability(String expertId, LocalDate start, LocalDate end) {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate curr = start;
        while (!curr.isAfter(end)) {
            dates.add(curr);
            curr = curr.plusDays(1);
        }
        return new DashboardExpertAvailabilityDto(expertId, dates);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception e) {
        log.error("Dev Dashboard error: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "An internal error occurred while processing the schedule request: " + e.getMessage(),
                "status", HttpStatus.INTERNAL_SERVER_ERROR.value()
        ));
    }
}

