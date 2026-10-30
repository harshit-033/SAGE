package in.gov.aicte.evc.scheduler;

import in.gov.aicte.evc.scheduler.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EvcCpSatSchedulerTests {

    private EvcCpSatScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new EvcCpSatScheduler();
    }

    @Test
    @DisplayName("Successfully schedules two inspections with different durations and committee sizes")
    void testDynamicDurationAndCommitteeSize() {
        // Inspection 1: duration 3 days, committee size 2
        InspectionRequest insp1 = new InspectionRequest(
                "INSP-01",
                3,
                2,
                LocalDate.of(2026, 11, 10),
                LocalDate.of(2026, 11, 15),
                Set.of("EXP-1", "EXP-2", "EXP-3")
        );

        // Inspection 2: duration 4 days, committee size 3 (different duration & size)
        InspectionRequest insp2 = new InspectionRequest(
                "INSP-02",
                4,
                3,
                LocalDate.of(2026, 11, 10),
                LocalDate.of(2026, 11, 15),
                Set.of("EXP-4", "EXP-5", "EXP-6", "EXP-7")
        );

        // Full availability across horizon Nov 10 to Nov 25
        Map<String, ExpertAvailability> availabilities = new HashMap<>();
        for (int i = 1; i <= 7; i++) {
            String expId = "EXP-" + i;
            availabilities.put(expId, ExpertAvailability.availableBetween(
                    expId, LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 25), Collections.emptySet()
            ));
        }

        SchedulingProblem problem = new SchedulingProblem(
                List.of(insp1, insp2),
                availabilities,
                Duration.ofSeconds(5)
        );

        SchedulingResult result = scheduler.solve(problem);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getStatus()).isIn(SchedulingStatus.OPTIMAL, SchedulingStatus.FEASIBLE);

        ScheduledInspection res1 = result.getScheduledInspection("INSP-01");
        assertThat(res1).isNotNull();
        assertThat(res1.getAssignedExpertIds()).hasSize(2);
        assertThat(res1.getScheduledStartDate()).isBetween(insp1.getEarliestStartDate(), insp1.getLatestStartDate());
        // 3 days inclusive: start + 2 days
        assertThat(res1.getScheduledEndDate()).isEqualTo(res1.getScheduledStartDate().plusDays(3 - 1));

        ScheduledInspection res2 = result.getScheduledInspection("INSP-02");
        assertThat(res2).isNotNull();
        assertThat(res2.getAssignedExpertIds()).hasSize(3);
        assertThat(res2.getScheduledStartDate()).isBetween(insp2.getEarliestStartDate(), insp2.getLatestStartDate());
        // 4 days inclusive: start + 3 days
        assertThat(res2.getScheduledEndDate()).isEqualTo(res2.getScheduledStartDate().plusDays(4 - 1));
    }

    @Test
    @DisplayName("Expert conflict resolution: forces solver to stagger dates when shared expert is required")
    void testExpertConflictForcesStaggeredDates() {
        // Both inspections require shared expert EXP-COMMON
        // Insp A: duration 3, window Nov 10 to Nov 12, requires 2 members from {EXP-COMMON, EXP-A}
        InspectionRequest inspA = new InspectionRequest(
                "INSP-A",
                3,
                2,
                LocalDate.of(2026, 11, 10),
                LocalDate.of(2026, 11, 12),
                Set.of("EXP-COMMON", "EXP-A")
        );

        // Insp B: duration 3, window Nov 10 to Nov 16, requires 2 members from {EXP-COMMON, EXP-B}
        InspectionRequest inspB = new InspectionRequest(
                "INSP-B",
                3,
                2,
                LocalDate.of(2026, 11, 10),
                LocalDate.of(2026, 11, 16),
                Set.of("EXP-COMMON", "EXP-B")
        );

        Map<String, ExpertAvailability> availabilities = new HashMap<>();
        for (String id : List.of("EXP-COMMON", "EXP-A", "EXP-B")) {
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

        assertThat(result.isSuccess()).isTrue();

        ScheduledInspection resA = result.getScheduledInspection("INSP-A");
        ScheduledInspection resB = result.getScheduledInspection("INSP-B");

        // Both require EXP-COMMON
        assertThat(resA.getAssignedExpertIds()).contains("EXP-COMMON");
        assertThat(resB.getAssignedExpertIds()).contains("EXP-COMMON");

        // Intervals must NOT overlap
        boolean aBeforeB = !resA.getScheduledEndDate().isAfter(resB.getScheduledStartDate().minusDays(1));
        boolean bBeforeA = !resB.getScheduledEndDate().isAfter(resA.getScheduledStartDate().minusDays(1));
        assertThat(aBeforeB || bBeforeA)
                .as("Intervals for shared expert must be non-overlapping: [%s, %s] vs [%s, %s]",
                        resA.getScheduledStartDate(), resA.getScheduledEndDate(),
                        resB.getScheduledStartDate(), resB.getScheduledEndDate())
                .isTrue();
    }

    @Test
    @DisplayName("Infeasible case: Required committee size exceeds eligible expert pool")
    void testInfeasibleWhenCommitteeSizeExceedsPool() {
        // Requires 4 members, but only 2 eligible experts are available
        InspectionRequest insp = new InspectionRequest(
                "INSP-DEFICIENT-POOL",
                3,
                4,
                LocalDate.of(2026, 11, 10),
                LocalDate.of(2026, 11, 15),
                Set.of("EXP-1", "EXP-2")
        );

        Map<String, ExpertAvailability> availabilities = Map.of(
                "EXP-1", ExpertAvailability.availableBetween("EXP-1", LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 20), Collections.emptySet()),
                "EXP-2", ExpertAvailability.availableBetween("EXP-2", LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 20), Collections.emptySet())
        );

        SchedulingProblem problem = SchedulingProblem.of(List.of(insp), availabilities);

        SchedulingResult result = scheduler.solve(problem);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getStatus()).isEqualTo(SchedulingStatus.INFEASIBLE);
        assertThat(result.getExplanation()).contains("requires 4 members, but only 2 eligible experts were provided");
    }

    @Test
    @DisplayName("Infeasible case: Expert availability blackout prevents meeting committee size")
    void testInfeasibleWhenAvailabilityPreventsSchedule() {
        // Window Nov 10 to Nov 12 (3 candidate start dates: 10, 11, 12)
        // Duration 3 days.
        // Requires 2 members from {EXP-1, EXP-2}.
        // EXP-1 is available, but EXP-2 has blackouts on Nov 11 and Nov 13,
        // making EXP-2 unavailable for ANY 3-consecutive-day span starting on 10, 11, or 12.
        InspectionRequest insp = new InspectionRequest(
                "INSP-BLACKOUT",
                3,
                2,
                LocalDate.of(2026, 11, 10),
                LocalDate.of(2026, 11, 12),
                Set.of("EXP-1", "EXP-2")
        );

        Map<String, ExpertAvailability> availabilities = Map.of(
                "EXP-1", ExpertAvailability.availableBetween("EXP-1", LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 20), Collections.emptySet()),
                "EXP-2", ExpertAvailability.availableBetween("EXP-2", LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 20),
                        Set.of(LocalDate.of(2026, 11, 11), LocalDate.of(2026, 11, 13)))
        );

        SchedulingProblem problem = SchedulingProblem.of(List.of(insp), availabilities);

        SchedulingResult result = scheduler.solve(problem);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getStatus()).isEqualTo(SchedulingStatus.INFEASIBLE);
        assertThat(result.getExplanation()).contains("cannot be scheduled: no start date in window");
    }

    @Test
    @DisplayName("Invalid input rejection: earliest start date after latest start date")
    void testInvalidInputRejectsInvertedDateWindow() {
        assertThrows(IllegalArgumentException.class, () -> new InspectionRequest(
                "INSP-INV",
                3,
                2,
                LocalDate.of(2026, 11, 20),
                LocalDate.of(2026, 11, 10),
                Set.of("EXP-1", "EXP-2")
        ));
    }

    @Test
    @DisplayName("Invalid input rejection: non-positive duration or member count")
    void testInvalidInputRejectsNonPositiveParameters() {
        assertThrows(IllegalArgumentException.class, () -> new InspectionRequest(
                "INSP-ZERO-DUR",
                0,
                2,
                LocalDate.of(2026, 11, 10),
                LocalDate.of(2026, 11, 15),
                Set.of("EXP-1")
        ));

        assertThrows(IllegalArgumentException.class, () -> new InspectionRequest(
                "INSP-ZERO-MEM",
                3,
                0,
                LocalDate.of(2026, 11, 10),
                LocalDate.of(2026, 11, 15),
                Set.of("EXP-1")
        ));
    }

    @Test
    @DisplayName("Distinct expert selection: all assigned experts in committee are distinct")
    void testDistinctExpertSelection() {
        InspectionRequest insp = new InspectionRequest(
                "INSP-DISTINCT",
                3,
                3,
                LocalDate.of(2026, 11, 10),
                LocalDate.of(2026, 11, 12),
                Set.of("EXP-A", "EXP-B", "EXP-C", "EXP-D")
        );

        Map<String, ExpertAvailability> availabilities = new HashMap<>();
        for (String id : List.of("EXP-A", "EXP-B", "EXP-C", "EXP-D")) {
            availabilities.put(id, ExpertAvailability.availableBetween(
                    id, LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 20), Collections.emptySet()
            ));
        }

        SchedulingResult result = scheduler.solve(SchedulingProblem.of(List.of(insp), availabilities));

        assertThat(result.isSuccess()).isTrue();
        ScheduledInspection scheduled = result.getScheduledInspection("INSP-DISTINCT");
        assertThat(scheduled.getAssignedExpertIds()).hasSize(3);
        // Set semantics ensure all elements are distinct
        Set<String> uniqueExperts = new HashSet<>(scheduled.getAssignedExpertIds());
        assertThat(uniqueExperts).hasSize(3);
    }

    @Test
    @DisplayName("Inclusive end date calculation for 3, 4, and 5 day durations")
    void testInclusiveEndDateCalculation() {
        InspectionRequest req3 = new InspectionRequest("I3", 3, 1, LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 10), Set.of("E1"));
        InspectionRequest req4 = new InspectionRequest("I4", 4, 1, LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 10), Set.of("E1"));
        InspectionRequest req5 = new InspectionRequest("I5", 5, 1, LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 10), Set.of("E1"));

        // 3 days: 10, 11, 12 -> 12 Nov
        assertThat(req3.calculateEndDate(LocalDate.of(2026, 11, 10))).isEqualTo(LocalDate.of(2026, 11, 12));
        // 4 days: 10, 11, 12, 13 -> 13 Nov
        assertThat(req4.calculateEndDate(LocalDate.of(2026, 11, 10))).isEqualTo(LocalDate.of(2026, 11, 13));
        // 5 days: 10, 11, 12, 13, 14 -> 14 Nov
        assertThat(req5.calculateEndDate(LocalDate.of(2026, 11, 10))).isEqualTo(LocalDate.of(2026, 11, 14));
    }

    // ==========================================
    // Phase 4A Tests: Sunday Exclusion Semantics
    // ==========================================

    @Test
    @DisplayName("Phase 4A.1: Four-day inspection starting Friday spans Sunday and completes Tuesday")
    void testFourDayInspectionStartingFridaySpansSunday() {
        // Friday Nov 6, 2026 to Tuesday Nov 10, 2026
        // Inspection days: Fri 6, Sat 7, Mon 9, Tue 10 (Sun 8 skipped)
        InspectionRequest req = new InspectionRequest(
                "INSP-FRI-4DAY",
                4,
                2,
                LocalDate.of(2026, 11, 6),
                LocalDate.of(2026, 11, 6),
                Set.of("EXP-1", "EXP-2")
        );

        // Experts available on Fri 6, Sat 7, Mon 9, Tue 10 (Sunday Nov 8 NOT in availability)
        Set<LocalDate> dates = Set.of(
                LocalDate.of(2026, 11, 6),
                LocalDate.of(2026, 11, 7),
                LocalDate.of(2026, 11, 9),
                LocalDate.of(2026, 11, 10)
        );
        Map<String, ExpertAvailability> avails = Map.of(
                "EXP-1", ExpertAvailability.of("EXP-1", dates),
                "EXP-2", ExpertAvailability.of("EXP-2", dates)
        );

        SchedulingResult result = scheduler.solve(SchedulingProblem.of(List.of(req), avails));

        assertThat(result.isSuccess()).isTrue();
        ScheduledInspection scheduled = result.getScheduledInspection("INSP-FRI-4DAY");
        assertThat(scheduled.getScheduledStartDate()).isEqualTo(LocalDate.of(2026, 11, 6));
        assertThat(scheduled.getScheduledEndDate()).isEqualTo(LocalDate.of(2026, 11, 10));
        assertThat(scheduled.getAssignedExpertIds()).containsExactlyInAnyOrder("EXP-1", "EXP-2");
        assertThat(req.calculateInspectionDays(scheduled.getScheduledStartDate()))
                .containsExactly(
                        LocalDate.of(2026, 11, 6),
                        LocalDate.of(2026, 11, 7),
                        LocalDate.of(2026, 11, 9),
                        LocalDate.of(2026, 11, 10)
                );
    }

    @Test
    @DisplayName("Phase 4A.2: Multi-day inspection spanning multiple Sundays completes required non-Sunday days")
    void testMultiDayInspectionSpanningMultipleSundays() {
        // 10 actual inspection days starting Friday Nov 6, 2026
        // Week 1: Fri 6, Sat 7 (2 days) -> Sun 8 skipped
        // Week 2: Mon 9, Tue 10, Wed 11, Thu 12, Fri 13, Sat 14 (6 days, total 8) -> Sun 15 skipped
        // Week 3: Mon 16, Tue 17 (2 days, total 10)
        // Inclusive end date: Tue Nov 17, 2026
        InspectionRequest req = new InspectionRequest(
                "INSP-10DAYS",
                10,
                1,
                LocalDate.of(2026, 11, 6),
                LocalDate.of(2026, 11, 6),
                Set.of("EXP-1")
        );

        ExpertAvailability avail = ExpertAvailability.availableBetween(
                "EXP-1", LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30), Collections.emptySet()
        );

        SchedulingResult result = scheduler.solve(SchedulingProblem.of(List.of(req), Map.of("EXP-1", avail)));

        assertThat(result.isSuccess()).isTrue();
        ScheduledInspection scheduled = result.getScheduledInspection("INSP-10DAYS");
        assertThat(scheduled.getScheduledStartDate()).isEqualTo(LocalDate.of(2026, 11, 6));
        assertThat(scheduled.getScheduledEndDate()).isEqualTo(LocalDate.of(2026, 11, 17));

        List<LocalDate> inspectionDays = req.calculateInspectionDays(scheduled.getScheduledStartDate());
        assertThat(inspectionDays).hasSize(10);
        assertThat(inspectionDays).doesNotContain(
                LocalDate.of(2026, 11, 8),
                LocalDate.of(2026, 11, 15)
        );
        assertThat(inspectionDays.get(0)).isEqualTo(LocalDate.of(2026, 11, 6));
        assertThat(inspectionDays.get(9)).isEqualTo(LocalDate.of(2026, 11, 17));
    }

    @Test
    @DisplayName("Phase 4A.3: Inspection starting on Sunday does not count Sunday towards duration")
    void testInspectionStartingOnSundayDoesNotCountSunday() {
        // Candidate start date Sunday Nov 8, 2026, duration 4 days
        // Sunday Nov 8 is skipped from inspection days
        // Days: Mon Nov 9, Tue Nov 10, Wed Nov 11, Thu Nov 12
        // Inclusive end date: Thu Nov 12, 2026
        InspectionRequest req = new InspectionRequest(
                "INSP-SUN-START",
                4,
                1,
                LocalDate.of(2026, 11, 8),
                LocalDate.of(2026, 11, 8),
                Set.of("EXP-1")
        );

        // Expert available only on Mon-Thu (NOT available on Sunday Nov 8)
        Set<LocalDate> dates = Set.of(
                LocalDate.of(2026, 11, 9),
                LocalDate.of(2026, 11, 10),
                LocalDate.of(2026, 11, 11),
                LocalDate.of(2026, 11, 12)
        );
        Map<String, ExpertAvailability> avails = Map.of(
                "EXP-1", ExpertAvailability.of("EXP-1", dates)
        );

        SchedulingResult result = scheduler.solve(SchedulingProblem.of(List.of(req), avails));

        assertThat(result.isSuccess()).isTrue();
        ScheduledInspection scheduled = result.getScheduledInspection("INSP-SUN-START");
        assertThat(scheduled.getScheduledStartDate()).isEqualTo(LocalDate.of(2026, 11, 8));
        assertThat(scheduled.getScheduledEndDate()).isEqualTo(LocalDate.of(2026, 11, 12));

        List<LocalDate> days = req.calculateInspectionDays(scheduled.getScheduledStartDate());
        assertThat(days).containsExactly(
                LocalDate.of(2026, 11, 9),
                LocalDate.of(2026, 11, 10),
                LocalDate.of(2026, 11, 11),
                LocalDate.of(2026, 11, 12)
        );
    }

    @Test
    @DisplayName("Phase 4A.4: Infeasible when date window cannot accommodate required non-Sunday inspection days")
    void testPermissibleWindowCannotAccommodateRequiredNonSundayInspectionDays() {
        // Window Nov 6 to Nov 6 (Friday). Duration: 3 inspection days.
        // Required inspection days: Fri Nov 6, Sat Nov 7, Mon Nov 9.
        // But the expert is ONLY available on Fri Nov 6, Sat Nov 7, and Sun Nov 8.
        // Since Sunday Nov 8 does not count, expert only has 2 valid inspection days (Fri 6, Sat 7), not 3.
        InspectionRequest req = new InspectionRequest(
                "INSP-DEFICIENT-DAYS",
                3,
                1,
                LocalDate.of(2026, 11, 6),
                LocalDate.of(2026, 11, 6),
                Set.of("EXP-1")
        );

        Set<LocalDate> dates = Set.of(
                LocalDate.of(2026, 11, 6),
                LocalDate.of(2026, 11, 7),
                LocalDate.of(2026, 11, 8) // Sunday!
        );
        Map<String, ExpertAvailability> avails = Map.of("EXP-1", ExpertAvailability.of("EXP-1", dates));

        SchedulingResult result = scheduler.solve(SchedulingProblem.of(List.of(req), avails));

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getStatus()).isEqualTo(SchedulingStatus.INFEASIBLE);
        assertThat(result.getExplanation()).contains("cannot be scheduled: no start date in window");
    }

    @Test
    @DisplayName("Phase 4A.5: Expert missing availability on required inspection day vs Sunday")
    void testExpertAvailabilityMissingOnRequiredInspectionDay() {
        // 4-day inspection starting Friday Nov 6: requires Fri 6, Sat 7, Mon 9, Tue 10
        InspectionRequest req = new InspectionRequest(
                "INSP-AVAIL-TEST",
                4,
                1,
                LocalDate.of(2026, 11, 6),
                LocalDate.of(2026, 11, 6),
                Set.of("EXP-DEFICIENT", "EXP-VALID")
        );

        // EXP-DEFICIENT: Available on Fri 6, Sat 7, Sun 8 (Sunday), Tue 10. Missing Mon 9!
        ExpertAvailability expDeficient = ExpertAvailability.of("EXP-DEFICIENT", Set.of(
                LocalDate.of(2026, 11, 6),
                LocalDate.of(2026, 11, 7),
                LocalDate.of(2026, 11, 8),
                LocalDate.of(2026, 11, 10)
        ));

        // Case A: Only EXP-DEFICIENT is provided -> INFEASIBLE
        SchedulingResult resultA = scheduler.solve(SchedulingProblem.of(
                List.of(new InspectionRequest("I1", 4, 1, LocalDate.of(2026, 11, 6), LocalDate.of(2026, 11, 6), Set.of("EXP-DEFICIENT"))),
                Map.of("EXP-DEFICIENT", expDeficient)
        ));
        assertThat(resultA.isSuccess()).isFalse();
        assertThat(resultA.getStatus()).isEqualTo(SchedulingStatus.INFEASIBLE);

        // Case B: EXP-VALID is available on Fri 6, Sat 7, Mon 9, Tue 10 (NOT on Sunday Nov 8) -> FEASIBLE!
        ExpertAvailability expValid = ExpertAvailability.of("EXP-VALID", Set.of(
                LocalDate.of(2026, 11, 6),
                LocalDate.of(2026, 11, 7),
                LocalDate.of(2026, 11, 9),
                LocalDate.of(2026, 11, 10)
        ));
        SchedulingResult resultB = scheduler.solve(SchedulingProblem.of(
                List.of(new InspectionRequest("I2", 4, 1, LocalDate.of(2026, 11, 6), LocalDate.of(2026, 11, 6), Set.of("EXP-VALID"))),
                Map.of("EXP-VALID", expValid)
        ));
        assertThat(resultB.isSuccess()).isTrue();
        ScheduledInspection si = resultB.getScheduledInspection("I2");
        assertThat(si.getAssignedExpertIds()).containsExactly("EXP-VALID");
    }

    @Test
    @DisplayName("Phase 4A.6: Two inspections competing for same expert across Sunday")
    void testTwoInspectionsCompetingForSameExpertAcrossSunday() {
        // Inspection 1: Fri Nov 6 to Sat Nov 7 (duration 2 days)
        InspectionRequest req1 = new InspectionRequest(
                "INSP-FRI-SAT",
                2,
                1,
                LocalDate.of(2026, 11, 6),
                LocalDate.of(2026, 11, 6),
                Set.of("EXP-SHARED")
        );

        // Inspection 2: Mon Nov 9 to Tue Nov 10 (duration 2 days)
        InspectionRequest req2 = new InspectionRequest(
                "INSP-MON-TUE",
                2,
                1,
                LocalDate.of(2026, 11, 9),
                LocalDate.of(2026, 11, 9),
                Set.of("EXP-SHARED")
        );

        // Case A: Non-overlapping across Sunday (Sunday Nov 8 is in between) -> FEASIBLE
        ExpertAvailability avail = ExpertAvailability.of("EXP-SHARED", Set.of(
                LocalDate.of(2026, 11, 6),
                LocalDate.of(2026, 11, 7),
                LocalDate.of(2026, 11, 9),
                LocalDate.of(2026, 11, 10)
        ));

        SchedulingResult resultA = scheduler.solve(SchedulingProblem.of(List.of(req1, req2), Map.of("EXP-SHARED", avail)));
        assertThat(resultA.isSuccess()).isTrue();
        assertThat(resultA.getScheduledInspection("INSP-FRI-SAT").getAssignedExpertIds()).contains("EXP-SHARED");
        assertThat(resultA.getScheduledInspection("INSP-MON-TUE").getAssignedExpertIds()).contains("EXP-SHARED");

        // Case B: Inspection 1 has duration 3 (Fri 6, Sat 7, Mon 9) -> conflicts on Monday Nov 9!
        InspectionRequest req1Overlap = new InspectionRequest(
                "INSP-OVERLAP",
                3,
                1,
                LocalDate.of(2026, 11, 6),
                LocalDate.of(2026, 11, 6),
                Set.of("EXP-SHARED")
        );

        SchedulingResult resultB = scheduler.solve(SchedulingProblem.of(List.of(req1Overlap, req2), Map.of("EXP-SHARED", avail)));
        assertThat(resultB.isSuccess()).isFalse();
        assertThat(resultB.getStatus()).isEqualTo(SchedulingStatus.INFEASIBLE);
    }
}

