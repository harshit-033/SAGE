package in.gov.aicte.evc.service;

import in.gov.aicte.evc.entity.*;
import in.gov.aicte.evc.repository.*;
import in.gov.aicte.evc.scheduler.EvcCpSatScheduler;
import in.gov.aicte.evc.scheduler.model.SchedulingStatus;
import in.gov.aicte.evc.service.dto.AssignedExpertDto;
import in.gov.aicte.evc.service.dto.ScheduledInspectionResultDto;
import in.gov.aicte.evc.service.dto.SchedulingRequest;
import in.gov.aicte.evc.service.dto.SchedulingServiceResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class EvcSchedulingServiceTests {

    @Autowired
    private EvcSchedulingService schedulingService;

    @Autowired
    private EvcInspectionRepository inspectionRepository;

    @Autowired
    private InstitutionRepository institutionRepository;

    @Autowired
    private ExpertRepository expertRepository;

    @Autowired
    private ExpertAvailabilityRepository availabilityRepository;

    @Autowired
    private CommitteeAssignmentRepository assignmentRepository;

    @Autowired
    private SchedulePersistenceService persistenceService;

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    @BeforeEach
    void setUp() {
        cleanup();
    }

    @AfterEach
    void tearDown() {
        cleanup();
    }

    private void cleanup() {
        assignmentRepository.deleteAll();
        availabilityRepository.deleteAll();
        inspectionRepository.deleteAll();
        institutionRepository.deleteAll();
        expertRepository.deleteAll();
    }

    private Institution createInstitution(String aicteId, String name) {
        return institutionRepository.save(new Institution(aicteId, name, "New Delhi", "Delhi", "Northern"));
    }

    private Expert createExpert(String name, String discipline, boolean active) {
        return expertRepository.save(new Expert(name, name.toLowerCase() + "@test.aicte.gov.in", discipline, active));
    }

    private void addAvailability(Expert expert, LocalDate start, LocalDate end) {
        List<ExpertAvailabilityRecord> records = new ArrayList<>();
        LocalDate curr = start;
        while (!curr.isAfter(end)) {
            records.add(new ExpertAvailabilityRecord(expert, curr));
            curr = curr.plusDays(1);
        }
        availabilityRepository.saveAll(records);
    }

    @Test
    @DisplayName("End-to-end integration: Maps persisted inspections, solves via CP-SAT, and persists results with inclusive ends")
    void testSuccessfulSchedulePersistenceWithDynamicDurationsAndMemberCounts() {
        Institution inst1 = createInstitution("AICTE-ENGG-101", "Institute of Advanced Technology");
        Institution inst2 = createInstitution("AICTE-POLY-202", "State Polytechnic College");

        // Dynamic inspection 1: 3 days, 2 committee members
        EvcInspection insp1 = inspectionRepository.save(new EvcInspection(
                inst1, 3, 2, LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 15)
        ));

        // Dynamic inspection 2: 4 days, 3 committee members
        EvcInspection insp2 = inspectionRepository.save(new EvcInspection(
                inst2, 4, 3, LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 15)
        ));

        // 6 active experts with full availability Nov 10 to Nov 25
        List<Expert> experts = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            Expert exp = createExpert("Dr. Expert " + i, "Computer Science", true);
            experts.add(exp);
            addAvailability(exp, LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 25));
        }

        SchedulingRequest request = new SchedulingRequest(List.of(insp1.getId(), insp2.getId()));

        SchedulingServiceResult result = schedulingService.schedule(request);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.isPersisted()).isTrue();
        assertThat(result.getStatus()).isIn(SchedulingStatus.OPTIMAL, SchedulingStatus.FEASIBLE);
        assertThat(result.totalInspectionsRequested()).isEqualTo(2);
        assertThat(result.totalInspectionsScheduled()).isEqualTo(2);

        // Verify persisted inspection 1
        EvcInspection persisted1 = inspectionRepository.findById(insp1.getId()).orElseThrow();
        assertThat(persisted1.getStatus()).isEqualTo(InspectionStatus.SCHEDULED);
        assertThat(persisted1.getScheduledStartDate()).isNotNull();
        assertThat(persisted1.getScheduledStartDate()).isBetween(insp1.getEarliestStartDate(), insp1.getLatestStartDate());
        // Verify inclusive end date: scheduledStart + 3 - 1 = scheduledStart + 2
        assertThat(persisted1.getScheduledEndDate()).isEqualTo(persisted1.getScheduledStartDate().plusDays(2));

        List<CommitteeAssignment> assignments1 = assignmentRepository.findByInspection(persisted1);
        assertThat(assignments1).hasSize(2);
        assertThat(assignments1).extracting(CommitteeAssignment::getStatus).containsOnly(AssignmentStatus.CONFIRMED);
        assertThat(assignments1).extracting(CommitteeAssignment::getRole)
                .containsExactlyInAnyOrder(AssignmentRole.CHAIRPERSON, AssignmentRole.MEMBER);

        // Verify persisted inspection 2
        EvcInspection persisted2 = inspectionRepository.findById(insp2.getId()).orElseThrow();
        assertThat(persisted2.getStatus()).isEqualTo(InspectionStatus.SCHEDULED);
        assertThat(persisted2.getScheduledStartDate()).isNotNull();
        assertThat(persisted2.getScheduledStartDate()).isBetween(insp2.getEarliestStartDate(), insp2.getLatestStartDate());
        // Verify inclusive end date: scheduledStart + 4 - 1 = scheduledStart + 3
        assertThat(persisted2.getScheduledEndDate()).isEqualTo(persisted2.getScheduledStartDate().plusDays(3));

        List<CommitteeAssignment> assignments2 = assignmentRepository.findByInspection(persisted2);
        assertThat(assignments2).hasSize(3);
        assertThat(assignments2).extracting(CommitteeAssignment::getStatus).containsOnly(AssignmentStatus.CONFIRMED);
        assertThat(assignments2).extracting(CommitteeAssignment::getRole)
                .contains(AssignmentRole.CHAIRPERSON, AssignmentRole.MEMBER);

        // Ensure no expert is assigned twice across either inspection if intervals overlap
        Set<Long> assignedExpIds1 = new HashSet<>(assignments1.stream().map(a -> a.getExpert().getId()).toList());
        Set<Long> assignedExpIds2 = new HashSet<>(assignments2.stream().map(a -> a.getExpert().getId()).toList());
        assertThat(assignedExpIds1).hasSize(2);
        assertThat(assignedExpIds2).hasSize(3);
    }

    @Test
    @DisplayName("Expert availability rule: Missing availability data is strictly treated as unavailable")
    void testExpertAvailabilityHandling_MissingTreatedAsUnavailable() {
        Institution inst = createInstitution("AICTE-TEST-01", "College of Engineering");
        // Requires 2 members
        EvcInspection insp = inspectionRepository.save(new EvcInspection(
                inst, 3, 2, LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 15)
        ));

        // Create 2 experts: EXP-1 has availability, EXP-2 has NO availability records
        Expert exp1 = createExpert("Dr. Available", "Electrical", true);
        addAvailability(exp1, LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 20));

        Expert exp2 = createExpert("Dr. MissingAvailability", "Electrical", true);
        // Do NOT add any availability records for exp2!

        SchedulingRequest request = SchedulingRequest.builder()
                .inspectionIds(List.of(insp.getId()))
                .eligibleExperts(insp.getId(), List.of(exp1.getId(), exp2.getId()))
                .build();

        SchedulingServiceResult result = schedulingService.schedule(request);

        // Must fail because inspection needs 2 members, but only 1 has availability data!
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.isPersisted()).isFalse();
        assertThat(result.getStatus()).isEqualTo(SchedulingStatus.INFEASIBLE);
        assertThat(result.getExplanation()).contains("mathematically infeasible");

        // Database must remain completely untouched
        EvcInspection untouched = inspectionRepository.findById(insp.getId()).orElseThrow();
        assertThat(untouched.getStatus()).isEqualTo(InspectionStatus.PENDING);
        assertThat(untouched.getScheduledStartDate()).isNull();
        assertThat(assignmentRepository.findByInspection(untouched)).isEmpty();
    }

    @Test
    @DisplayName("Expert availability rule: Calendar blackout mid-week prevents consecutive-day scheduling")
    void testExpertAvailabilityHandling_BlackoutDatePreventsBlock() {
        Institution inst = createInstitution("AICTE-BLACKOUT-01", "National Technology Institute");
        // Window is exactly Nov 10 to Nov 10, duration 3 days (needs Nov 10, 11, 12)
        EvcInspection insp = inspectionRepository.save(new EvcInspection(
                inst, 3, 1, LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 10)
        ));

        Expert exp = createExpert("Dr. Blackout", "Mechanical", true);
        // Expert available on Nov 10 and Nov 12, but BLACKOUT on Nov 11
        availabilityRepository.save(new ExpertAvailabilityRecord(exp, LocalDate.of(2026, 11, 10)));
        availabilityRepository.save(new ExpertAvailabilityRecord(exp, LocalDate.of(2026, 11, 12)));

        SchedulingRequest request = SchedulingRequest.builder()
                .inspectionIds(List.of(insp.getId()))
                .eligibleExperts(insp.getId(), List.of(exp.getId()))
                .build();

        SchedulingServiceResult result = schedulingService.schedule(request);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getStatus()).isEqualTo(SchedulingStatus.INFEASIBLE);
        assertThat(result.isPersisted()).isFalse();

        EvcInspection persisted = inspectionRepository.findById(insp.getId()).orElseThrow();
        assertThat(persisted.getStatus()).isEqualTo(InspectionStatus.PENDING);
        assertThat(assignmentRepository.findByInspection(persisted)).isEmpty();
    }

    @Test
    @DisplayName("Rescheduling policy: Rejects scheduling previously SCHEDULED inspection when allowRescheduling is false")
    void testReschedulingPolicy_RejectionWhenDisallowed() {
        Institution inst = createInstitution("AICTE-RE-01", "Institute of Science");
        EvcInspection insp = new EvcInspection(
                inst, 3, 1, LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 15)
        );
        insp.setStatus(InspectionStatus.SCHEDULED);
        insp.setScheduledStartDate(LocalDate.of(2026, 11, 10));
        EvcInspection savedInsp = inspectionRepository.save(insp);

        Expert exp = createExpert("Dr. Existing", "Physics", true);
        assignmentRepository.save(new CommitteeAssignment(savedInsp, exp, AssignmentRole.CHAIRPERSON, AssignmentStatus.CONFIRMED));

        // Submit request without allowRescheduling
        SchedulingRequest request = new SchedulingRequest(List.of(savedInsp.getId()));

        SchedulingServiceResult result = schedulingService.schedule(request);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getStatus()).isEqualTo(SchedulingStatus.INVALID_INPUT);
        assertThat(result.isPersisted()).isFalse();
        assertThat(result.getExplanation()).contains("already SCHEDULED");

        // Verify original schedule remains completely intact
        EvcInspection unchanged = inspectionRepository.findById(savedInsp.getId()).orElseThrow();
        assertThat(unchanged.getStatus()).isEqualTo(InspectionStatus.SCHEDULED);
        assertThat(unchanged.getScheduledStartDate()).isEqualTo(LocalDate.of(2026, 11, 10));
        assertThat(assignmentRepository.findByInspection(unchanged)).hasSize(1);
    }

    @Test
    @DisplayName("Rescheduling policy: Successfully updates schedule when allowRescheduling is explicitly true")
    void testReschedulingPolicy_SuccessWhenAllowed() {
        Institution inst = createInstitution("AICTE-RE-02", "School of Technology");
        EvcInspection insp = new EvcInspection(
                inst, 3, 1, LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 15)
        );
        insp.setStatus(InspectionStatus.SCHEDULED);
        insp.setScheduledStartDate(LocalDate.of(2026, 11, 10));
        EvcInspection savedInsp = inspectionRepository.save(insp);

        Expert oldExp = createExpert("Dr. Old", "Civil", true);
        assignmentRepository.save(new CommitteeAssignment(savedInsp, oldExp, AssignmentRole.CHAIRPERSON, AssignmentStatus.CONFIRMED));

        Expert newExp = createExpert("Dr. New", "Civil", true);
        addAvailability(newExp, LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 20));

        SchedulingRequest request = SchedulingRequest.builder()
                .inspectionIds(List.of(savedInsp.getId()))
                .eligibleExperts(savedInsp.getId(), List.of(newExp.getId()))
                .allowRescheduling(true)
                .build();

        SchedulingServiceResult result = schedulingService.schedule(request);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.isPersisted()).isTrue();

        // Old assignment should have been removed and new assignment created
        List<CommitteeAssignment> currentAssignments = assignmentRepository.findByInspection(savedInsp);
        assertThat(currentAssignments).hasSize(1);
        assertThat(currentAssignments.get(0).getExpert().getId()).isEqualTo(newExp.getId());
    }

    @Test
    @DisplayName("Rescheduling policy: Existing valid schedule is preserved untouched if a new rescheduling attempt fails")
    void testExistingSchedulePreservationOnFailedRescheduling() {
        Institution inst = createInstitution("AICTE-SAFE-01", "Technical Campus");
        EvcInspection insp = new EvcInspection(
                inst, 3, 1, LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 15)
        );
        insp.setStatus(InspectionStatus.SCHEDULED);
        insp.setScheduledStartDate(LocalDate.of(2026, 11, 10));
        EvcInspection originalSaved = inspectionRepository.save(insp);

        Expert originalExpert = createExpert("Dr. Original", "Architecture", true);
        assignmentRepository.save(new CommitteeAssignment(originalSaved, originalExpert, AssignmentRole.CHAIRPERSON, AssignmentStatus.CONFIRMED));

        // Attempt rescheduling with an expert that has NO availability (making it infeasible)
        Expert unavailableExpert = createExpert("Dr. Unavailable", "Architecture", true);
        // zero availability

        SchedulingRequest infeasibleRequest = SchedulingRequest.builder()
                .inspectionIds(List.of(originalSaved.getId()))
                .eligibleExperts(originalSaved.getId(), List.of(unavailableExpert.getId()))
                .allowRescheduling(true)
                .build();

        SchedulingServiceResult result = schedulingService.schedule(infeasibleRequest);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getStatus()).isEqualTo(SchedulingStatus.INFEASIBLE);
        assertThat(result.isPersisted()).isFalse();

        // Check that the original valid schedule and assignments were NOT erased or modified!
        EvcInspection preserved = inspectionRepository.findById(originalSaved.getId()).orElseThrow();
        assertThat(preserved.getStatus()).isEqualTo(InspectionStatus.SCHEDULED);
        assertThat(preserved.getScheduledStartDate()).isEqualTo(LocalDate.of(2026, 11, 10));

        List<CommitteeAssignment> preservedAssignments = assignmentRepository.findByInspection(preserved);
        assertThat(preservedAssignments).hasSize(1);
        assertThat(preservedAssignments.get(0).getExpert().getId()).isEqualTo(originalExpert.getId());
    }

    @Test
    @DisplayName("Duplicate assignment prevention: Enforces unique constraint uk_assignment_inspection_expert")
    void testDuplicateAssignmentPrevention() {
        Institution inst = createInstitution("AICTE-DUP-01", "Polytechnic Institute");
        EvcInspection insp = inspectionRepository.save(new EvcInspection(
                inst, 3, 2, LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 15)
        ));

        Expert exp = createExpert("Dr. Unique", "Pharmacy", true);

        // First assignment succeeds
        assignmentRepository.save(new CommitteeAssignment(insp, exp, AssignmentRole.CHAIRPERSON, AssignmentStatus.CONFIRMED));

        // Attempt duplicate assignment to same inspection & expert must throw DataIntegrityViolationException
        CommitteeAssignment duplicate = new CommitteeAssignment(insp, exp, AssignmentRole.MEMBER, AssignmentStatus.CONFIRMED);
        assertThrows(DataIntegrityViolationException.class, () -> {
            assignmentRepository.save(duplicate);
            assignmentRepository.flush();
        });
    }

    @Test
    @DisplayName("Transaction rollback: In-memory availability overrides work seamlessly with the service")
    void testExternalAvailabilityOverrides() {
        Institution inst = createInstitution("AICTE-OVERRIDE-01", "Institute of Information Technology");
        EvcInspection insp = inspectionRepository.save(new EvcInspection(
                inst, 3, 1, LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 15)
        ));

        Expert exp = createExpert("Dr. Override", "IT", true);
        // No DB availability records! But override passed directly in request:
        Set<LocalDate> dates = Set.of(
                LocalDate.of(2026, 11, 12),
                LocalDate.of(2026, 11, 13),
                LocalDate.of(2026, 11, 14)
        );

        SchedulingRequest request = SchedulingRequest.builder()
                .inspectionIds(List.of(insp.getId()))
                .eligibleExperts(insp.getId(), List.of(exp.getId()))
                .expertAvailability(exp.getId(), dates)
                .build();

        SchedulingServiceResult result = schedulingService.schedule(request);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.isPersisted()).isTrue();

        EvcInspection scheduled = inspectionRepository.findById(insp.getId()).orElseThrow();
        assertThat(scheduled.getStatus()).isEqualTo(InspectionStatus.SCHEDULED);
        assertThat(scheduled.getScheduledStartDate()).isEqualTo(LocalDate.of(2026, 11, 12));
        assertThat(scheduled.getScheduledEndDate()).isEqualTo(LocalDate.of(2026, 11, 14));
    }

    @Test
    @DisplayName("Solver status handling: Time limit reached is cleanly surfaced and does not touch the database")
    void testSolverTimeLimitHandling() {
        Institution inst = createInstitution("AICTE-TL-01", "College of Science");
        EvcInspection insp = inspectionRepository.save(new EvcInspection(
                inst, 3, 1, LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 15)
        ));

        EvcCpSatScheduler mockScheduler = org.mockito.Mockito.mock(EvcCpSatScheduler.class);
        org.mockito.Mockito.when(mockScheduler.solve(org.mockito.ArgumentMatchers.any()))
                .thenReturn(in.gov.aicte.evc.scheduler.model.SchedulingResult.timeLimitReached("Search timed out after 5s"));

        SchedulePersistenceService persistence = new SchedulePersistenceService(inspectionRepository, assignmentRepository);
        EvcSchedulingService customService = new DefaultEvcSchedulingService(
                inspectionRepository, expertRepository, availabilityRepository, mockScheduler, persistence
        );

        SchedulingRequest request = new SchedulingRequest(List.of(insp.getId()));
        SchedulingServiceResult result = customService.schedule(request);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getStatus()).isEqualTo(SchedulingStatus.TIME_LIMIT_REACHED);
        assertThat(result.isPersisted()).isFalse();
        assertThat(result.getExplanation()).contains("Search timed out");

        EvcInspection unchanged = inspectionRepository.findById(insp.getId()).orElseThrow();
        assertThat(unchanged.getStatus()).isEqualTo(InspectionStatus.PENDING);
    }

    @Test
    @DisplayName("Solver status handling: Solver error is cleanly captured and leaves database untouched")
    void testSolverErrorHandling() {
        Institution inst = createInstitution("AICTE-ERR-01", "College of Engineering");
        EvcInspection insp = inspectionRepository.save(new EvcInspection(
                inst, 3, 1, LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 15)
        ));

        EvcCpSatScheduler mockScheduler = org.mockito.Mockito.mock(EvcCpSatScheduler.class);
        org.mockito.Mockito.when(mockScheduler.solve(org.mockito.ArgumentMatchers.any()))
                .thenReturn(in.gov.aicte.evc.scheduler.model.SchedulingResult.error("Native solver initialization failure"));

        SchedulePersistenceService persistence = new SchedulePersistenceService(inspectionRepository, assignmentRepository);
        EvcSchedulingService customService = new DefaultEvcSchedulingService(
                inspectionRepository, expertRepository, availabilityRepository, mockScheduler, persistence
        );

        SchedulingRequest request = new SchedulingRequest(List.of(insp.getId()));
        SchedulingServiceResult result = customService.schedule(request);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getStatus()).isEqualTo(SchedulingStatus.ERROR);
        assertThat(result.isPersisted()).isFalse();
        assertThat(result.getExplanation()).contains("Native solver initialization failure");

        EvcInspection unchanged = inspectionRepository.findById(insp.getId()).orElseThrow();
        assertThat(unchanged.getStatus()).isEqualTo(InspectionStatus.PENDING);
    }

    @Test
    @DisplayName("Transaction rollback: Database failure rolls back inspection status change and assignments")
    void testTransactionRollbackOnDatabaseFailure() {
        Institution inst = createInstitution("AICTE-ROLLBACK-01", "Institute of Management");
        EvcInspection insp = inspectionRepository.save(new EvcInspection(
                inst, 3, 1, LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 15)
        ));

        // Create a solver result with an expert ID that does not exist in the DB
        Map<String, in.gov.aicte.evc.scheduler.model.ScheduledInspection> scheduledMap = Map.of(
                insp.getId().toString(),
                new in.gov.aicte.evc.scheduler.model.ScheduledInspection(
                        insp.getId().toString(),
                        LocalDate.of(2026, 11, 10),
                        LocalDate.of(2026, 11, 12),
                        Set.of("999999") // Non-existent expert ID!
                )
        );
        in.gov.aicte.evc.scheduler.model.SchedulingResult dummyResult =
                in.gov.aicte.evc.scheduler.model.SchedulingResult.success(SchedulingStatus.OPTIMAL, scheduledMap);

        // Invoking persistence directly via Spring-managed bean with missing expert must throw exception and roll back
        assertThrows(IllegalStateException.class, () -> {
            persistenceService.persistScheduledSchedule(
                    dummyResult,
                    Map.of(insp.getId(), insp),
                    Collections.emptyMap(),
                    false
            );
        });

        // Clear persistence context to ensure we read fresh state from database
        entityManager.clear();

        // Verify inspection status in the database remained PENDING and no assignments persisted
        EvcInspection rolledBack = inspectionRepository.findById(insp.getId()).orElseThrow();
        assertThat(rolledBack.getStatus()).isEqualTo(InspectionStatus.PENDING);
        assertThat(rolledBack.getScheduledStartDate()).isNull();
        assertThat(assignmentRepository.findByInspection(rolledBack)).isEmpty();
    }

    // ==========================================
    // Phase 4A Tests: Sunday Exclusion Semantics
    // ==========================================

    @Test
    @DisplayName("Phase 4A.7: Correct persisted start and inclusive end dates spanning Sunday")
    void testPersistedStartAndInclusiveEndDateWithSundayExclusion() {
        Institution inst = createInstitution("AICTE-SUN-01", "Institute of Technology");

        // Duration 4 days starting Thursday Nov 12, 2026
        // Inspection days: Thu 12, Fri 13, Sat 14, (skip Sun 15), Mon 16
        // Expected inclusive end date: Monday Nov 16, 2026
        EvcInspection insp = inspectionRepository.save(new EvcInspection(
                inst, 4, 2, LocalDate.of(2026, 11, 12), LocalDate.of(2026, 11, 12)
        ));

        Expert exp1 = createExpert("Dr. Thursday Chairperson", "Architecture", true);
        Expert exp2 = createExpert("Dr. Thursday Member", "Civil", true);

        // Availability on Thu 12, Fri 13, Sat 14, Mon 16 (Sunday Nov 15 not present)
        addAvailability(exp1, LocalDate.of(2026, 11, 12), LocalDate.of(2026, 11, 14));
        availabilityRepository.save(new ExpertAvailabilityRecord(exp1, LocalDate.of(2026, 11, 16)));

        addAvailability(exp2, LocalDate.of(2026, 11, 12), LocalDate.of(2026, 11, 14));
        availabilityRepository.save(new ExpertAvailabilityRecord(exp2, LocalDate.of(2026, 11, 16)));

        SchedulingRequest request = new SchedulingRequest(List.of(insp.getId()));
        SchedulingServiceResult result = schedulingService.schedule(request);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.isPersisted()).isTrue();

        ScheduledInspectionResultDto dto = result.getScheduledInspections().get(0);
        assertThat(dto.scheduledStartDate()).isEqualTo(LocalDate.of(2026, 11, 12));
        assertThat(dto.scheduledEndDate()).isEqualTo(LocalDate.of(2026, 11, 16));
        assertThat(dto.durationInDays()).isEqualTo(4);

        // Verify entity in the database
        EvcInspection persisted = inspectionRepository.findById(insp.getId()).orElseThrow();
        assertThat(persisted.getStatus()).isEqualTo(InspectionStatus.SCHEDULED);
        assertThat(persisted.getScheduledStartDate()).isEqualTo(LocalDate.of(2026, 11, 12));
        assertThat(persisted.getScheduledEndDate()).isEqualTo(LocalDate.of(2026, 11, 16));

        // Verify committee assignments in database
        List<CommitteeAssignment> assignments = assignmentRepository.findByInspection(persisted);
        assertThat(assignments).hasSize(2);
        assertThat(assignments.get(0).getRole()).isIn(AssignmentRole.CHAIRPERSON, AssignmentRole.MEMBER);
    }

    @Test
    @DisplayName("Phase 4A.8: Rescheduling failure preserves the previously persisted schedule and assignments")
    void testReschedulingFailurePreservesPreviouslyPersistedSchedule() {
        Institution inst = createInstitution("AICTE-RETRY-01", "College of Engineering");

        // 1. Initial successful schedule: 4 days starting Friday Nov 6, 2026
        EvcInspection insp = inspectionRepository.save(new EvcInspection(
                inst, 4, 1, LocalDate.of(2026, 11, 6), LocalDate.of(2026, 11, 6)
        ));
        Expert exp = createExpert("Dr. Solid Expert", "Mechanical", true);

        // Available on Fri 6, Sat 7, Mon 9, Tue 10
        addAvailability(exp, LocalDate.of(2026, 11, 6), LocalDate.of(2026, 11, 7));
        availabilityRepository.save(new ExpertAvailabilityRecord(exp, LocalDate.of(2026, 11, 9)));
        availabilityRepository.save(new ExpertAvailabilityRecord(exp, LocalDate.of(2026, 11, 10)));

        SchedulingRequest initialRequest = new SchedulingRequest(List.of(insp.getId()));
        SchedulingServiceResult initialResult = schedulingService.schedule(initialRequest);
        assertThat(initialResult.isSuccess()).isTrue();

        EvcInspection initialScheduled = inspectionRepository.findById(insp.getId()).orElseThrow();
        assertThat(initialScheduled.getStatus()).isEqualTo(InspectionStatus.SCHEDULED);
        assertThat(initialScheduled.getScheduledStartDate()).isEqualTo(LocalDate.of(2026, 11, 6));
        assertThat(initialScheduled.getScheduledEndDate()).isEqualTo(LocalDate.of(2026, 11, 10));
        List<CommitteeAssignment> initialAssignments = assignmentRepository.findByInspection(initialScheduled);
        assertThat(initialAssignments).hasSize(1);
        Long originalAssignmentId = initialAssignments.get(0).getId();

        // 2. Trigger a rescheduling solve that fails (e.g. expert has NO availability via override)
        SchedulingRequest failingRescheduleRequest = SchedulingRequest.builder()
                .inspectionIds(List.of(insp.getId()))
                .allowRescheduling(true)
                .expertAvailability(exp.getId(), Collections.emptySet()) // Impossible: expert unavailable!
                .build();

        SchedulingServiceResult failResult = schedulingService.schedule(failingRescheduleRequest);

        assertThat(failResult.isSuccess()).isFalse();
        assertThat(failResult.getStatus()).isEqualTo(SchedulingStatus.INFEASIBLE);
        assertThat(failResult.isPersisted()).isFalse();

        // 3. Verify the database was untouched and previous schedule is intact
        entityManager.clear();
        EvcInspection preserved = inspectionRepository.findById(insp.getId()).orElseThrow();
        assertThat(preserved.getStatus()).isEqualTo(InspectionStatus.SCHEDULED);
        assertThat(preserved.getScheduledStartDate()).isEqualTo(LocalDate.of(2026, 11, 6));
        assertThat(preserved.getScheduledEndDate()).isEqualTo(LocalDate.of(2026, 11, 10));

        List<CommitteeAssignment> preservedAssignments = assignmentRepository.findByInspection(preserved);
        assertThat(preservedAssignments).hasSize(1);
        assertThat(preservedAssignments.get(0).getId()).isEqualTo(originalAssignmentId);
        assertThat(preservedAssignments.get(0).getExpert().getId()).isEqualTo(exp.getId());
    }

    @Test
    @DisplayName("Phase 4A.9: Regression test with variable durations and variable committee sizes spanning Sundays")
    void testVariableDurationsAndCommitteeSizesWithSundayExclusionRegression() {
        Institution inst1 = createInstitution("AICTE-REG-01", "Institute Alpha");
        Institution inst2 = createInstitution("AICTE-REG-02", "Institute Beta");
        Institution inst3 = createInstitution("AICTE-REG-03", "Institute Gamma");

        // Insp 1: duration 2 days, 1 member, window [Nov 6, Nov 6] (Fri, Sat -> Nov 7)
        EvcInspection insp1 = inspectionRepository.save(new EvcInspection(
                inst1, 2, 1, LocalDate.of(2026, 11, 6), LocalDate.of(2026, 11, 6)
        ));

        // Insp 2: duration 4 days, 2 members, window [Nov 6, Nov 6] (Fri, Sat, Mon, Tue -> Nov 10)
        EvcInspection insp2 = inspectionRepository.save(new EvcInspection(
                inst2, 4, 2, LocalDate.of(2026, 11, 6), LocalDate.of(2026, 11, 6)
        ));

        // Insp 3: duration 5 days, 3 members, window [Nov 12, Nov 12] (Thu, Fri, Sat, Mon, Tue -> Nov 17)
        EvcInspection insp3 = inspectionRepository.save(new EvcInspection(
                inst3, 5, 3, LocalDate.of(2026, 11, 12), LocalDate.of(2026, 11, 12)
        ));

        // 6 distinct experts with full availability Nov 1 to Nov 25
        for (int i = 1; i <= 6; i++) {
            Expert exp = createExpert("Dr. Multi " + i, "Electrical", true);
            addAvailability(exp, LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 25));
        }

        SchedulingRequest request = new SchedulingRequest(List.of(insp1.getId(), insp2.getId(), insp3.getId()));
        SchedulingServiceResult result = schedulingService.schedule(request);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getScheduledInspections()).hasSize(3);

        ScheduledInspectionResultDto r1 = result.getScheduledInspections().stream()
                .filter(r -> r.inspectionId().equals(insp1.getId())).findFirst().orElseThrow();
        assertThat(r1.scheduledStartDate()).isEqualTo(LocalDate.of(2026, 11, 6));
        assertThat(r1.scheduledEndDate()).isEqualTo(LocalDate.of(2026, 11, 7));
        assertThat(r1.assignedExperts()).hasSize(1);

        ScheduledInspectionResultDto r2 = result.getScheduledInspections().stream()
                .filter(r -> r.inspectionId().equals(insp2.getId())).findFirst().orElseThrow();
        assertThat(r2.scheduledStartDate()).isEqualTo(LocalDate.of(2026, 11, 6));
        assertThat(r2.scheduledEndDate()).isEqualTo(LocalDate.of(2026, 11, 10));
        assertThat(r2.assignedExperts()).hasSize(2);

        ScheduledInspectionResultDto r3 = result.getScheduledInspections().stream()
                .filter(r -> r.inspectionId().equals(insp3.getId())).findFirst().orElseThrow();
        assertThat(r3.scheduledStartDate()).isEqualTo(LocalDate.of(2026, 11, 12));
        assertThat(r3.scheduledEndDate()).isEqualTo(LocalDate.of(2026, 11, 17));
        assertThat(r3.assignedExperts()).hasSize(3);
    }
}
