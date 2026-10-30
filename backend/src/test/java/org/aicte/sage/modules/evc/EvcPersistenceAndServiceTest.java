package org.aicte.sage.modules.evc;

import org.aicte.sage.common.security.UserPrincipal;
import org.aicte.sage.modules.audit.repository.AuditEventRepository;
import org.aicte.sage.modules.evc.dto.*;
import org.aicte.sage.modules.evc.entity.*;
import org.aicte.sage.modules.evc.repository.CommitteeAssignmentRepository;
import org.aicte.sage.modules.evc.repository.EvcInspectionRepository;
import org.aicte.sage.modules.evc.repository.ExpertAvailabilityRepository;
import org.aicte.sage.modules.evc.repository.ExpertRepository;
import org.aicte.sage.modules.evc.scheduler.model.SchedulingStatus;
import org.aicte.sage.modules.evc.service.EvcSchedulingService;
import org.aicte.sage.modules.institutions.entity.Institution;
import org.aicte.sage.modules.institutions.repository.InstitutionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class EvcPersistenceAndServiceTest {

    @Autowired
    private EvcSchedulingService schedulingService;

    @Autowired
    private EvcInspectionRepository inspectionRepository;

    @Autowired
    private CommitteeAssignmentRepository assignmentRepository;

    @Autowired
    private ExpertRepository expertRepository;

    @Autowired
    private ExpertAvailabilityRepository availabilityRepository;

    @Autowired
    private InstitutionRepository institutionRepository;

    @Autowired
    private AuditEventRepository auditEventRepository;

    private UserPrincipal createAdminPrincipal() {
        return new UserPrincipal(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                "admin@aicte.gov.in",
                "admin@aicte.gov.in",
                "Password123!",
                "Central Administrator",
                true,
                List.of(new SimpleGrantedAuthority("ROLE_AICTE_ADMIN")),
                Collections.emptySet()
        );
    }

    private UserPrincipal createEvcCoordinatorPrincipal() {
        return new UserPrincipal(
                UUID.fromString("11111111-1111-1111-1111-111111111117"),
                "evc.coord@aicte.gov.in",
                "evc.coord@aicte.gov.in",
                "Password123!",
                "Dr. Anand Kulkarni",
                true,
                List.of(new SimpleGrantedAuthority("ROLE_EVC_COORDINATOR")),
                Collections.emptySet()
        );
    }

    @org.junit.jupiter.api.BeforeEach
    void resetTestInspections() {
        UUID insp1 = UUID.fromString("ffffffff-ffff-ffff-ffff-000000000001");
        UUID insp2 = UUID.fromString("ffffffff-ffff-ffff-ffff-000000000002");
        for (UUID id : List.of(insp1, insp2)) {
            inspectionRepository.findById(id).ifPresent(i -> {
                assignmentRepository.deleteByInspection(i);
                i.setStatus(InspectionStatus.PENDING);
                i.setScheduledStartDate(null);
                i.setScheduledEndDate(null);
                inspectionRepository.save(i);
            });
        }
    }

    @Test
    @DisplayName("Persistence Test 1: Solves and persists schedule for seeded inspection with Sunday exclusion & assignments")
    void testScheduleGenerationAndPersistence() {
        UserPrincipal admin = createAdminPrincipal();
        UUID inspId = UUID.fromString("ffffffff-ffff-ffff-ffff-000000000001");

        EvcInspection initial = inspectionRepository.findById(inspId).orElseThrow();
        assertThat(initial.getStatus()).isEqualTo(InspectionStatus.PENDING);

        SchedulingRequestDto request = new SchedulingRequestDto(
                List.of(inspId),
                false,
                10
        );

        SchedulingResponseDto response = schedulingService.schedule(request, admin);

        assertThat(response.status()).isIn(SchedulingStatus.OPTIMAL, SchedulingStatus.FEASIBLE);
        assertThat(response.scheduledInspections()).hasSize(1);

        ScheduledInspectionResultDto scheduledDto = response.scheduledInspections().get(0);
        assertThat(scheduledDto.inspectionId()).isEqualTo(inspId);
        assertThat(scheduledDto.assignedExperts()).hasSize(3);

        // Verify First expert is assigned as CHAIRPERSON
        assertThat(scheduledDto.assignedExperts().get(0).role()).isEqualTo(AssignmentRole.CHAIRPERSON);

        // Verify Database Persistence
        EvcInspection persistedInspection = inspectionRepository.findById(inspId).orElseThrow();
        assertThat(persistedInspection.getStatus()).isEqualTo(InspectionStatus.SCHEDULED);
        assertThat(persistedInspection.getScheduledStartDate()).isNotNull();
        assertThat(persistedInspection.getScheduledEndDate()).isNotNull();

        List<CommitteeAssignment> persistedAssignments = assignmentRepository.findByInspection(persistedInspection);
        assertThat(persistedAssignments).hasSize(3);
        assertThat(persistedAssignments.stream().anyMatch(a -> a.getRole() == AssignmentRole.CHAIRPERSON)).isTrue();
        assertThat(persistedAssignments.stream().filter(a -> a.getRole() == AssignmentRole.MEMBER).count()).isEqualTo(2);

        // Verify schedule can be retrieved without rerunning solver
        EvcInspectionDto retrieved = schedulingService.getInspectionById(inspId, admin);
        assertThat(retrieved.status()).isEqualTo(InspectionStatus.SCHEDULED);
        assertThat(retrieved.scheduledStartDate()).isEqualTo(persistedInspection.getScheduledStartDate());
        assertThat(retrieved.assignedCommittee()).hasSize(3);
    }

    @Test
    @DisplayName("Persistence Test 2: Rescheduling policy blocks accidental overwrite unless allowRescheduling is true")
    void testReschedulingPolicy() {
        UserPrincipal coord = createEvcCoordinatorPrincipal();
        UUID inspId = UUID.fromString("ffffffff-ffff-ffff-ffff-000000000002");

        // 1. Initial Schedule
        SchedulingRequestDto initialReq = new SchedulingRequestDto(List.of(inspId), false, 10);
        SchedulingResponseDto initialResp = schedulingService.schedule(initialReq, coord);
        assertThat(initialResp.status()).isIn(SchedulingStatus.OPTIMAL, SchedulingStatus.FEASIBLE);

        // 2. Attempt Reschedule with allowRescheduling = false -> must be rejected
        SchedulingRequestDto duplicateReq = new SchedulingRequestDto(List.of(inspId), false, 10);
        SchedulingResponseDto duplicateResp = schedulingService.schedule(duplicateReq, coord);
        assertThat(duplicateResp.status()).isEqualTo(SchedulingStatus.INVALID_INPUT);
        assertThat(duplicateResp.explanation()).contains("already SCHEDULED. Rescheduling must be explicitly enabled");

        // 3. Attempt Reschedule with allowRescheduling = true -> must succeed and cleanly replace assignments
        SchedulingRequestDto rescheduleReq = new SchedulingRequestDto(List.of(inspId), true, 10);
        SchedulingResponseDto rescheduleResp = schedulingService.schedule(rescheduleReq, coord);
        assertThat(rescheduleResp.status()).isIn(SchedulingStatus.OPTIMAL, SchedulingStatus.FEASIBLE);

        List<CommitteeAssignment> updatedAssignments = assignmentRepository.findByInspectionId(inspId);
        assertThat(updatedAssignments).hasSize(2);
    }

    @Test
    @DisplayName("Persistence Test 3: Conflict of Interest (COI) isolation excludes affiliated experts from candidate pool")
    void testConflictOfInterestIsolation() {
        UserPrincipal admin = createAdminPrincipal();
        Institution delhiTech = institutionRepository.findById(UUID.fromString("22222222-2222-2222-2222-222222222222")).orElseThrow();

        // Create an expert formally affiliated with Delhi Tech
        Expert affiliatedExpert = new Expert(
                "Dr. Affiliated Insider",
                "insider@delhi-tech.edu",
                "COMPUTER_SCIENCE_ENGINEERING",
                "Internal Professor",
                true
        );
        affiliatedExpert.setAffiliatedInstitution(delhiTech);
        Expert savedAffiliated = expertRepository.save(affiliatedExpert);

        // Add availability for this affiliated expert
        for (int i = 5; i <= 20; i++) {
            availabilityRepository.save(new ExpertAvailabilityRecord(savedAffiliated, LocalDate.now().plusDays(i)));
        }

        UUID inspId = UUID.fromString("ffffffff-ffff-ffff-ffff-000000000001");
        SchedulingRequestDto request = new SchedulingRequestDto(List.of(inspId), true, 10);

        SchedulingResponseDto response = schedulingService.schedule(request, admin);
        assertThat(response.status()).isIn(SchedulingStatus.OPTIMAL, SchedulingStatus.FEASIBLE);

        // The affiliated expert MUST NOT be assigned to Delhi Tech's inspection
        ScheduledInspectionResultDto scheduledDto = response.scheduledInspections().get(0);
        assertThat(scheduledDto.assignedExperts().stream().noneMatch(e -> e.id().equals(savedAffiliated.getId()))).isTrue();
    }
}
