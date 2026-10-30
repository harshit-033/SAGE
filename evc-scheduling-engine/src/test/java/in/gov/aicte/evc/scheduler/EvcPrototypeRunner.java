package in.gov.aicte.evc.scheduler;

import in.gov.aicte.evc.scheduler.model.*;

import java.time.Duration;
import java.time.LocalDate;
import java.util.*;

/**
 * Non-production runnable prototype entry point demonstrating the CP-SAT scheduling engine
 * across all Phase 3 required synthetic scenarios.
 */
public class EvcPrototypeRunner {

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("   EVC Scheduling Engine - Phase 3 CP-SAT Prototype Demonstration");
        System.out.println("================================================================================\n");

        EvcCpSatScheduler scheduler = new EvcCpSatScheduler();

        runScenario1(scheduler);
        runScenario2(scheduler);
        runScenario3(scheduler);
        runScenario4(scheduler);
        runScenario5(scheduler);
        runScenario6();

        System.out.println("================================================================================");
        System.out.println("   All Phase 3 Prototype Demonstrations Completed Successfully!");
        System.out.println("================================================================================");
    }

    /**
     * Scenario 1 & 2: Two inspections with different durations (3 days vs 4 days)
     * and different committee sizes (2 members vs 3 members).
     */
    public static void runScenario1(EvcCpSatScheduler scheduler) {
        System.out.println(">>> Scenario 1 & 2: Dynamic Durations (3 vs 4 days) & Committee Sizes (2 vs 3 members)");

        InspectionRequest insp1 = new InspectionRequest(
                "INSP-ENGG-01",
                3, // 3 days
                2, // 2 members
                LocalDate.of(2026, 11, 10),
                LocalDate.of(2026, 11, 15),
                Set.of("EXP-01", "EXP-02", "EXP-03")
        );

        InspectionRequest insp2 = new InspectionRequest(
                "INSP-POLY-02",
                4, // 4 days
                3, // 3 members
                LocalDate.of(2026, 11, 10),
                LocalDate.of(2026, 11, 15),
                Set.of("EXP-04", "EXP-05", "EXP-06", "EXP-07")
        );

        Map<String, ExpertAvailability> availabilities = new HashMap<>();
        for (int i = 1; i <= 7; i++) {
            String id = "EXP-0" + i;
            availabilities.put(id, ExpertAvailability.availableBetween(
                    id, LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 25), Collections.emptySet()
            ));
        }

        SchedulingProblem problem = new SchedulingProblem(
                List.of(insp1, insp2),
                availabilities,
                Duration.ofSeconds(5)
        );

        SchedulingResult result = scheduler.solve(problem);
        printResult(result);
    }

    /**
     * Scenario 3: Shared expert conflict forces solver to stagger inspection dates.
     */
    public static void runScenario2(EvcCpSatScheduler scheduler) {
        System.out.println(">>> Scenario 3: Expert Conflict Forcing Staggered Dates for Shared Expert");

        // Both require shared expert EXP-LEAD
        InspectionRequest inspA = new InspectionRequest(
                "INSP-ALPHA",
                3,
                2,
                LocalDate.of(2026, 11, 10),
                LocalDate.of(2026, 11, 12),
                Set.of("EXP-LEAD", "EXP-MEMBER-A")
        );

        InspectionRequest inspB = new InspectionRequest(
                "INSP-BETA",
                3,
                2,
                LocalDate.of(2026, 11, 10),
                LocalDate.of(2026, 11, 16),
                Set.of("EXP-LEAD", "EXP-MEMBER-B")
        );

        Map<String, ExpertAvailability> availabilities = new HashMap<>();
        for (String id : List.of("EXP-LEAD", "EXP-MEMBER-A", "EXP-MEMBER-B")) {
            availabilities.put(id, ExpertAvailability.availableBetween(
                    id, LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 20), Collections.emptySet()
            ));
        }

        SchedulingProblem problem = new SchedulingProblem(
                List.of(inspA, inspB),
                availabilities,
                Duration.ofSeconds(5)
        );

        SchedulingResult result = scheduler.solve(problem);
        printResult(result);
    }

    /**
     * Scenario 4: Infeasible case where required committee size exceeds eligible expert pool.
     */
    public static void runScenario3(EvcCpSatScheduler scheduler) {
        System.out.println(">>> Scenario 4: Infeasible Case (Committee Size > Eligible Pool)");

        InspectionRequest insp = new InspectionRequest(
                "INSP-DEFICIENT",
                3,
                4, // Requires 4 members
                LocalDate.of(2026, 11, 10),
                LocalDate.of(2026, 11, 15),
                Set.of("EXP-01", "EXP-02") // Only 2 experts provided
        );

        Map<String, ExpertAvailability> availabilities = Map.of(
                "EXP-01", ExpertAvailability.availableBetween("EXP-01", LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 20), Collections.emptySet()),
                "EXP-02", ExpertAvailability.availableBetween("EXP-02", LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 20), Collections.emptySet())
        );

        SchedulingResult result = scheduler.solve(SchedulingProblem.of(List.of(insp), availabilities));
        printResult(result);
    }

    /**
     * Scenario 5: Infeasible case where expert blackout prevents schedule.
     */
    public static void runScenario4(EvcCpSatScheduler scheduler) {
        System.out.println(">>> Scenario 5: Infeasible Case (Expert Calendar Blackout Prevents Schedule)");

        // 3-day inspection in window Nov 10-12 requiring 2 members from {EXP-01, EXP-02}
        // EXP-02 has blackouts on Nov 11 and 13
        InspectionRequest insp = new InspectionRequest(
                "INSP-BLACKOUT",
                3,
                2,
                LocalDate.of(2026, 11, 10),
                LocalDate.of(2026, 11, 12),
                Set.of("EXP-01", "EXP-02")
        );

        Map<String, ExpertAvailability> availabilities = Map.of(
                "EXP-01", ExpertAvailability.availableBetween("EXP-01", LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 20), Collections.emptySet()),
                "EXP-02", ExpertAvailability.availableBetween("EXP-02", LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 20),
                        Set.of(LocalDate.of(2026, 11, 11), LocalDate.of(2026, 11, 13)))
        );

        SchedulingResult result = scheduler.solve(SchedulingProblem.of(List.of(insp), availabilities));
        printResult(result);
    }

    /**
     * Scenario 6: Non-overlapping interval preservation across different durations.
     */
    public static void runScenario5(EvcCpSatScheduler scheduler) {
        System.out.println(">>> Scenario 6: Mixed-Duration Non-Overlapping Interlocking Schedule");

        // Expert EXP-SHARED must do both a 4-day inspection and a 2-day inspection
        InspectionRequest inspLong = new InspectionRequest(
                "INSP-LONG-4DAYS",
                4,
                1,
                LocalDate.of(2026, 11, 10),
                LocalDate.of(2026, 11, 15),
                Set.of("EXP-SHARED")
        );

        InspectionRequest inspShort = new InspectionRequest(
                "INSP-SHORT-2DAYS",
                2,
                1,
                LocalDate.of(2026, 11, 10),
                LocalDate.of(2026, 11, 18),
                Set.of("EXP-SHARED")
        );

        Map<String, ExpertAvailability> availabilities = Map.of(
                "EXP-SHARED", ExpertAvailability.availableBetween("EXP-SHARED", LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 25), Collections.emptySet())
        );

        SchedulingResult result = scheduler.solve(SchedulingProblem.of(List.of(inspLong, inspShort), availabilities));
        printResult(result);
    }

    /**
     * Scenario 7: Inclusive end-date calculation demonstration.
     */
    public static void runScenario6() {
        System.out.println(">>> Scenario 7: Inclusive Calendar End-Date Verification");
        LocalDate start = LocalDate.of(2026, 11, 10);
        for (int duration : List.of(2, 3, 4, 5)) {
            LocalDate end = start.plusDays(duration - 1);
            System.out.printf("   Start: %s | Duration: %d days | Inclusive End: %s (occupies %d calendar days)%n",
                    start, duration, end, duration);
        }
        System.out.println();
    }

    private static void printResult(SchedulingResult result) {
        System.out.println("   Status: " + result.getStatus());
        if (result.isSuccess()) {
            for (ScheduledInspection si : result.getScheduledInspections().values()) {
                System.out.printf("   - %s: Scheduled [%s to %s] | Experts: %s%n",
                        si.getInspectionId(), si.getScheduledStartDate(), si.getScheduledEndDate(), si.getAssignedExpertIds());
            }
        } else {
            System.out.println("   - Explanation: " + result.getExplanation());
        }
        System.out.println();
    }
}

