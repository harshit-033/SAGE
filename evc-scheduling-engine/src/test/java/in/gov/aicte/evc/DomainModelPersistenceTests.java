package in.gov.aicte.evc;

import in.gov.aicte.evc.entity.*;
import in.gov.aicte.evc.repository.CommitteeAssignmentRepository;
import in.gov.aicte.evc.repository.EvcInspectionRepository;
import in.gov.aicte.evc.repository.ExpertRepository;
import in.gov.aicte.evc.repository.InstitutionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class DomainModelPersistenceTests {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private InstitutionRepository institutionRepository;

    @Autowired
    private ExpertRepository expertRepository;

    @Autowired
    private EvcInspectionRepository inspectionRepository;

    @Autowired
    private CommitteeAssignmentRepository assignmentRepository;

    @Test
    @DisplayName("Persist Institution and query by unique AICTE ID")
    void testInstitutionPersistenceAndQueries() {
        Institution inst = new Institution("1-1002345", "Govt Engineering College", "Bhopal", "Madhya Pradesh", "Central");
        Institution saved = institutionRepository.save(inst);
        entityManager.flush();
        entityManager.clear();

        Optional<Institution> found = institutionRepository.findByAicteId("1-1002345");
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Govt Engineering College");
        assertThat(found.get().getCity()).isEqualTo("Bhopal");
        assertThat(found.get().getCreatedAt()).isNotNull();
        assertThat(found.get().getUpdatedAt()).isNotNull();

        assertThat(institutionRepository.existsByAicteId("1-1002345")).isTrue();
        assertThat(institutionRepository.existsByAicteId("NON-EXISTENT")).isFalse();
    }

    @Test
    @DisplayName("Enforce unique AICTE ID at database level")
    void testInstitutionDuplicateAicteIdThrowsException() {
        Institution inst1 = new Institution("1-UNIQUE-01", "Institute Alpha", "Mumbai", "Maharashtra", "Western");
        institutionRepository.save(inst1);
        entityManager.flush();

        Institution inst2 = new Institution("1-UNIQUE-01", "Institute Beta", "Pune", "Maharashtra", "Western");
        assertThrows(DataIntegrityViolationException.class, () -> {
            institutionRepository.save(inst2);
            entityManager.flush();
        });
    }

    @Test
    @DisplayName("Persist Experts and query by active status and discipline")
    void testExpertPersistenceAndQueries() {
        Expert e1 = new Expert("Dr. A. Sharma", "sharma@univ.ac.in", "Computer Science", true);
        Expert e2 = new Expert("Dr. B. Verma", "verma@univ.ac.in", "Mechanical Engineering", true);
        Expert e3 = new Expert("Dr. C. Rao", "rao@univ.ac.in", "Computer Science", false);

        expertRepository.saveAll(List.of(e1, e2, e3));
        entityManager.flush();
        entityManager.clear();

        List<Expert> activeExperts = expertRepository.findByActiveTrue();
        assertThat(activeExperts).hasSize(2)
                .extracting(Expert::getName)
                .containsExactlyInAnyOrder("Dr. A. Sharma", "Dr. B. Verma");

        List<Expert> csExperts = expertRepository.findByDisciplineIgnoreCase("computer science");
        assertThat(csExperts).hasSize(2)
                .extracting(Expert::getName)
                .containsExactlyInAnyOrder("Dr. A. Sharma", "Dr. C. Rao");
    }

    @Test
    @DisplayName("Persist Inspections with dynamic durations and required member counts supplied per inspection")
    void testInspectionsWithDifferentDurationsAndMemberCounts() {
        Institution instA = institutionRepository.save(new Institution("1-INST-A", "College of Engineering", "Delhi", "Delhi", "North-Western"));
        Institution instB = institutionRepository.save(new Institution("1-INST-B", "National Tech Institute", "Bengaluru", "Karnataka", "South-Western"));
        entityManager.flush();

        // Inspection 1: Duration 3 days, 3 committee members
        EvcInspection insp1 = new EvcInspection(instA, 3, 3, LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 15));
        insp1.setScheduledStartDate(LocalDate.of(2026, 11, 5));
        insp1.setStatus(InspectionStatus.SCHEDULED);

        // Inspection 2: Duration 4 days, 4 committee members (different configuration from SAGE)
        EvcInspection insp2 = new EvcInspection(instB, 4, 4, LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 25));
        insp2.setScheduledStartDate(LocalDate.of(2026, 11, 12));
        insp2.setStatus(InspectionStatus.SCHEDULED);

        // Inspection 3: Pending inspection with 2-day duration and 2 members
        EvcInspection insp3 = new EvcInspection(instA, 2, 2, LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 10));

        inspectionRepository.saveAll(List.of(insp1, insp2, insp3));
        entityManager.flush();
        entityManager.clear();

        List<EvcInspection> scheduled = inspectionRepository.findByStatus(InspectionStatus.SCHEDULED);
        assertThat(scheduled).hasSize(2);

        EvcInspection loaded1 = inspectionRepository.findById(insp1.getId()).orElseThrow();
        assertThat(loaded1.getDurationInDays()).isEqualTo(3);
        assertThat(loaded1.getRequiredMemberCount()).isEqualTo(3);
        // Inclusive end date: Nov 5 + (3 - 1) days = Nov 7
        assertThat(loaded1.getScheduledEndDate()).isEqualTo(LocalDate.of(2026, 11, 7));

        EvcInspection loaded2 = inspectionRepository.findById(insp2.getId()).orElseThrow();
        assertThat(loaded2.getDurationInDays()).isEqualTo(4);
        assertThat(loaded2.getRequiredMemberCount()).isEqualTo(4);
        // Inclusive end date: starts Thu Nov 12, duration 4 inspection days (Thu 12, Fri 13, Sat 14, Sun 15 skipped, Mon 16) = Nov 16
        assertThat(loaded2.getScheduledEndDate()).isEqualTo(LocalDate.of(2026, 11, 16));

        List<EvcInspection> instAInspections = inspectionRepository.findByInstitutionId(instA.getId());
        assertThat(instAInspections).hasSize(2);
    }

    @Test
    @DisplayName("CommitteeAssignment connects inspections to selected experts with roles and statuses")
    void testCommitteeAssignmentsMultiExpertAndMultiInspection() {
        Institution inst = institutionRepository.save(new Institution("1-ASSIGN-INST", "Polytechnic Institute", "Chennai", "Tamil Nadu", "Southern"));
        Expert expert1 = expertRepository.save(new Expert("Dr. Chairperson", "chair@univ.ac.in", "Civil", true));
        Expert expert2 = expertRepository.save(new Expert("Dr. Specialist", "specialist@univ.ac.in", "Architecture", true));
        Expert expert3 = expertRepository.save(new Expert("Dr. Member", "member@univ.ac.in", "Electrical", true));

        // Inspection requiring 3 members
        EvcInspection insp1 = inspectionRepository.save(
                new EvcInspection(inst, 3, 3, LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 20))
        );

        // Assign 3 experts with different roles to Inspection 1
        CommitteeAssignment a1 = new CommitteeAssignment(insp1, expert1, AssignmentRole.CHAIRPERSON, AssignmentStatus.CONFIRMED);
        CommitteeAssignment a2 = new CommitteeAssignment(insp1, expert2, AssignmentRole.SPECIALIST, AssignmentStatus.PROPOSED);
        CommitteeAssignment a3 = new CommitteeAssignment(insp1, expert3, AssignmentRole.MEMBER, AssignmentStatus.PROPOSED);
        assignmentRepository.saveAll(List.of(a1, a2, a3));

        // Another inspection requiring 2 members, expert1 is also assigned to it
        EvcInspection insp2 = inspectionRepository.save(
                new EvcInspection(inst, 4, 2, LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 15))
        );
        CommitteeAssignment a4 = new CommitteeAssignment(insp2, expert1, AssignmentRole.CHAIRPERSON, AssignmentStatus.CONFIRMED);
        assignmentRepository.save(a4);

        entityManager.flush();
        entityManager.clear();

        // Verify Inspection 1 has 3 assignments
        List<CommitteeAssignment> insp1Assignments = assignmentRepository.findByInspectionId(insp1.getId());
        assertThat(insp1Assignments).hasSize(3)
                .extracting(CommitteeAssignment::getRole)
                .containsExactlyInAnyOrder(AssignmentRole.CHAIRPERSON, AssignmentRole.SPECIALIST, AssignmentRole.MEMBER);

        // Verify Inspection 2 has 1 assignment
        List<CommitteeAssignment> insp2Assignments = assignmentRepository.findByInspectionId(insp2.getId());
        assertThat(insp2Assignments).hasSize(1);

        // Verify expert1 participates in both inspections
        List<CommitteeAssignment> expert1Assignments = assignmentRepository.findByExpertId(expert1.getId());
        assertThat(expert1Assignments).hasSize(2);

        // Verify exists query
        assertThat(assignmentRepository.existsByInspectionIdAndExpertId(insp1.getId(), expert1.getId())).isTrue();
        assertThat(assignmentRepository.existsByInspectionIdAndExpertId(insp2.getId(), expert2.getId())).isFalse();
    }

    @Test
    @DisplayName("Prevent duplicate assignment of the same expert to the same inspection at database level")
    void testDuplicateAssignmentThrowsDataIntegrityViolation() {
        Institution inst = institutionRepository.save(new Institution("1-DUP-INST", "Tech College", "Jaipur", "Rajasthan", "North-Western"));
        Expert expert = expertRepository.save(new Expert("Dr. Repeat", "repeat@univ.ac.in", "IT", true));
        EvcInspection insp = inspectionRepository.save(
                new EvcInspection(inst, 3, 2, LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 10))
        );

        CommitteeAssignment assignment1 = new CommitteeAssignment(insp, expert, AssignmentRole.CHAIRPERSON, AssignmentStatus.CONFIRMED);
        assignmentRepository.save(assignment1);
        entityManager.flush();

        CommitteeAssignment duplicate = new CommitteeAssignment(insp, expert, AssignmentRole.MEMBER, AssignmentStatus.PROPOSED);
        assertThrows(DataIntegrityViolationException.class, () -> {
            assignmentRepository.save(duplicate);
            entityManager.flush();
        });
    }

    @Test
    @DisplayName("Validation or database constraint rejects non-positive inspection duration")
    void testDatabaseCheckConstraintRejectsNonPositiveDuration() {
        Institution inst = institutionRepository.save(new Institution("1-CHK-DUR", "Duration Test College", "Hyderabad", "Telangana", "South-Central"));
        entityManager.flush();

        EvcInspection invalidInsp = new EvcInspection(inst, 0, 3, LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 10));
        assertThrows(Exception.class, () -> {
            inspectionRepository.save(invalidInsp);
            entityManager.flush();
        });
    }

    @Test
    @DisplayName("Validation or database constraint rejects non-positive member count")
    void testDatabaseCheckConstraintRejectsNonPositiveMemberCount() {
        Institution inst = institutionRepository.save(new Institution("1-CHK-MEM", "Member Test College", "Hyderabad", "Telangana", "South-Central"));
        entityManager.flush();

        EvcInspection invalidInsp = new EvcInspection(inst, 3, 0, LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 10));
        assertThrows(Exception.class, () -> {
            inspectionRepository.save(invalidInsp);
            entityManager.flush();
        });
    }

    @Test
    @DisplayName("Validation or database constraint rejects earliest start date after latest start date")
    void testDatabaseCheckConstraintRejectsInvalidDateWindow() {
        Institution inst = institutionRepository.save(new Institution("1-CHK-DATE", "Date Test College", "Kolkata", "West Bengal", "Eastern"));
        entityManager.flush();

        // Earliest date (Nov 15) is after Latest date (Nov 10)
        EvcInspection invalidInsp = new EvcInspection(inst, 3, 3, LocalDate.of(2026, 11, 15), LocalDate.of(2026, 11, 10));
        assertThrows(Exception.class, () -> {
            inspectionRepository.save(invalidInsp);
            entityManager.flush();
        });
    }
}
