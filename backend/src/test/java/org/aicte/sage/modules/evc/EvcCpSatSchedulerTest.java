package org.aicte.sage.modules.evc;

import org.aicte.sage.modules.evc.scheduler.EvcCpSatScheduler;
import org.aicte.sage.modules.evc.scheduler.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Rigorous algorithm verification tests for Google OR-Tools CP-SAT scheduler.
 * Enforces dynamic duration, dynamic committee size, Sunday exclusion, non-overlapping
 * intervals across actual inspection days, and diagnostic consistency for missing availability records.
 */
class EvcCpSatSchedulerTest {

    private EvcCpSatScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new EvcCpSatScheduler();
    }

    @Test
    @DisplayName("Algorithm Test 1: Solves dynamic durations and distinct committee sizes")
    void testDynamicDurationAndCommitteeSize() {
        InspectionRequest insp1 = new InspectionRequest(
                "INSP-01",
                3,
                2,
                LocalDate.of(2026, 11, 10),
                LocalDate.of(2026, 11, 15),
                Set.of("EXP-1", "EXP-2", "EXP-3")
        );

        InspectionRequest insp2 = new InspectionRequest(
                "INSP-02",
                4,
                3,
                LocalDate.of(2026, 11, 10),
                LocalDate.of(2026, 11, 15),
                Set.of("EXP-4", "EXP-5", "EXP-6", "EXP-7")
        );

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

        ScheduledInspection res2 = result.getScheduledInspection("INSP-02");
        assertThat(res2).isNotNull();
        assertThat(res2.getAssignedExpertIds()).hasSize(3);
    }

    @Test
    @DisplayName("Algorithm Test 2: Sunday Exclusion - 4-day inspection starting Friday skips Sunday and finishes Tuesday")
    void testSundayExclusionFourDayInspectionStartingFriday() {
        // Friday 6 Nov 2026
        // Inspection days: Fri 6, Sat 7, (Sun 8 skipped), Mon 9, Tue 10
        // Inclusive end date: Tue 10 Nov 2026
        InspectionRequest req = new InspectionRequest(
                "INSP-FRI-4DAY",
                4,
                2,
                LocalDate.of(2026, 11, 6),
                LocalDate.of(2026, 11, 6),
                Set.of("EXP-1", "EXP-2")
        );

        // Experts available on Fri 6, Sat 7, Mon 9, Tue 10 (Sunday 8 Nov NOT in availability)
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
    @DisplayName("Algorithm Test 3: Saturday is a valid working inspection day")
    void testSaturdayIsValidInspectionDay() {
        // Saturday 7 Nov 2026, 2-day inspection
        // Day 1: Sat 7, (Sun 8 skipped), Day 2: Mon 9 Nov
        InspectionRequest req = new InspectionRequest(
                "INSP-SAT-2DAY",
                2,
                1,
                LocalDate.of(2026, 11, 7),
                LocalDate.of(2026, 11, 7),
                Set.of("EXP-SAT")
        );

        Set<LocalDate> dates = Set.of(
                LocalDate.of(2026, 11, 7),
                LocalDate.of(2026, 11, 9)
        );
        Map<String, ExpertAvailability> avails = Map.of(
                "EXP-SAT", ExpertAvailability.of("EXP-SAT", dates)
        );

        SchedulingResult result = scheduler.solve(SchedulingProblem.of(List.of(req), avails));

        assertThat(result.isSuccess()).isTrue();
        ScheduledInspection scheduled = result.getScheduledInspection("INSP-SAT-2DAY");
        assertThat(scheduled.getScheduledStartDate()).isEqualTo(LocalDate.of(2026, 11, 7));
        assertThat(scheduled.getScheduledEndDate()).isEqualTo(LocalDate.of(2026, 11, 9));
    }

    @Test
    @DisplayName("Algorithm Test 4: Shared expert conflict forces staggered dates without date overlap")
    void testExpertConflictForcesStaggeredDates() {
        InspectionRequest inspA = new InspectionRequest(
                "INSP-A",
                3,
                2,
                LocalDate.of(2026, 11, 10),
                LocalDate.of(2026, 11, 12),
                Set.of("EXP-COMMON", "EXP-A")
        );

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

        assertThat(resA.getAssignedExpertIds()).contains("EXP-COMMON");
        assertThat(resB.getAssignedExpertIds()).contains("EXP-COMMON");

        // Intervals must NOT overlap
        boolean aBeforeB = !resA.getScheduledEndDate().isAfter(resB.getScheduledStartDate().minusDays(1));
        boolean bBeforeA = !resB.getScheduledEndDate().isAfter(resA.getScheduledStartDate().minusDays(1));
        assertThat(aBeforeB || bBeforeA).isTrue();
    }

    @Test
    @DisplayName("Algorithm Test 5: Section 8.1 Regression - Missing availability records treated as unavailable in solver and diagnostic")
    void testMissingAvailabilityRecordConsistency() {
        // Inspection requires 2 members from {EXP-1, EXP-2}
        // EXP-1 is available
        // EXP-2 has NO availability record at all (availability is null)
        InspectionRequest req = new InspectionRequest(
                "INSP-MISSING-AVAIL",
                3,
                2,
                LocalDate.of(2026, 11, 10),
                LocalDate.of(2026, 11, 12),
                Set.of("EXP-1", "EXP-2")
        );

        Map<String, ExpertAvailability> availabilities = new HashMap<>();
        // Only provide EXP-1; EXP-2 is omitted (null in map)
        availabilities.put("EXP-1", ExpertAvailability.availableBetween(
                "EXP-1", LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 20), Collections.emptySet()
        ));

        SchedulingProblem problem = SchedulingProblem.of(List.of(req), availabilities);

        SchedulingResult result = scheduler.solve(problem);

        // Solver must fail because EXP-2 is unavailable
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getStatus()).isEqualTo(SchedulingStatus.INFEASIBLE);

        // Critical Section 8.1 Verification:
        // Diagnostic MUST report that no start date has at least 2 available experts,
        // and must NOT falsely claim that EXP-2 is available!
        assertThat(result.getExplanation()).contains("cannot be scheduled: no start date in window");
        assertThat(result.getExplanation()).contains("has at least 2 available eligible experts");
    }

    @Test
    @DisplayName("Algorithm Test 6: Infeasible when required committee size exceeds eligible expert pool")
    void testInfeasibleWhenCommitteeSizeExceedsPool() {
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

        SchedulingResult result = scheduler.solve(SchedulingProblem.of(List.of(insp), availabilities));

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getStatus()).isEqualTo(SchedulingStatus.INFEASIBLE);
        assertThat(result.getExplanation()).contains("requires 4 members, but only 2 eligible experts were provided");
    }

    @Test
    @DisplayName("Algorithm Test 7: Invalid input rejection - inverted date window and non-positive duration")
    void testInvalidInputRejections() {
        assertThrows(IllegalArgumentException.class, () -> new InspectionRequest(
                "INSP-INV",
                3,
                2,
                LocalDate.of(2026, 11, 20),
                LocalDate.of(2026, 11, 10),
                Set.of("EXP-1", "EXP-2")
        ));

        assertThrows(IllegalArgumentException.class, () -> new InspectionRequest(
                "INSP-ZERO-DUR",
                0,
                2,
                LocalDate.of(2026, 11, 10),
                LocalDate.of(2026, 11, 15),
                Set.of("EXP-1")
        ));
    }
}
