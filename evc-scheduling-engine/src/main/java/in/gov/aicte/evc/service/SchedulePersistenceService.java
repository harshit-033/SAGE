package in.gov.aicte.evc.service;

import in.gov.aicte.evc.entity.*;
import in.gov.aicte.evc.repository.CommitteeAssignmentRepository;
import in.gov.aicte.evc.repository.EvcInspectionRepository;
import in.gov.aicte.evc.scheduler.model.ScheduledInspection;
import in.gov.aicte.evc.scheduler.model.SchedulingResult;
import in.gov.aicte.evc.service.dto.AssignedExpertDto;
import in.gov.aicte.evc.service.dto.ScheduledInspectionResultDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

/**
 * Dedicated persistence service responsible for transactional commitment of scheduled results.
 * Isolated in a separate service to ensure database transactions are only opened during persistence
 * and never held open across solver computation.
 */
@Service
public class SchedulePersistenceService {

    private static final Logger log = LoggerFactory.getLogger(SchedulePersistenceService.class);

    private final EvcInspectionRepository inspectionRepository;
    private final CommitteeAssignmentRepository assignmentRepository;

    public SchedulePersistenceService(EvcInspectionRepository inspectionRepository,
                                    CommitteeAssignmentRepository assignmentRepository) {
        this.inspectionRepository = inspectionRepository;
        this.assignmentRepository = assignmentRepository;
    }

    /**
     * Atomically persists a successful solver schedule into the database.
     * Maps scheduled start dates and derived inclusive end dates, clears previous assignments
     * if rescheduling is authorized, and persists committee member assignments.
     *
     * @param solverResult the successful CP-SAT solver result
     * @param inspectionsById map of managed or loaded inspection entities
     * @param expertsById map of expert entities
     * @param allowRescheduling whether overwriting previously scheduled inspections is permitted
     * @return list of application-level scheduled inspection DTOs
     */
    @Transactional(rollbackFor = Exception.class)
    public List<ScheduledInspectionResultDto> persistScheduledSchedule(SchedulingResult solverResult,
                                                                      Map<Long, EvcInspection> inspectionsById,
                                                                      Map<Long, Expert> expertsById,
                                                                      boolean allowRescheduling) {
        if (solverResult == null || !solverResult.isSuccess()) {
            throw new IllegalArgumentException("Cannot persist an unsuccessful solver result");
        }

        List<ScheduledInspectionResultDto> results = new ArrayList<>();

        for (ScheduledInspection si : solverResult.getScheduledInspections().values()) {
            Long inspId = Long.parseLong(si.getInspectionId());
            EvcInspection inspection = inspectionsById.get(inspId);
            if (inspection == null) {
                throw new IllegalStateException("Inspection entity not found for ID: " + inspId);
            }

            // Rescheduling safety check
            if (inspection.getStatus() == InspectionStatus.SCHEDULED) {
                if (!allowRescheduling) {
                    throw new IllegalStateException(String.format(
                            "Inspection %d is already SCHEDULED. Rescheduling must be explicitly enabled.", inspId));
                }
                log.info("Rescheduling authorized for inspection {}. Clearing existing committee assignments.", inspId);
                List<CommitteeAssignment> existingAssignments = assignmentRepository.findByInspection(inspection);
                if (!existingAssignments.isEmpty()) {
                    assignmentRepository.deleteAll(existingAssignments);
                    assignmentRepository.flush();
                }
            }

            // 1. Update Inspection start date and status
            inspection.setScheduledStartDate(si.getScheduledStartDate());
            inspection.setStatus(InspectionStatus.SCHEDULED);
            EvcInspection savedInspection = inspectionRepository.save(inspection);

            // Inclusive end date is derived as scheduledStartDate + durationInDays - 1
            LocalDate inclusiveEndDate = savedInspection.getScheduledEndDate();

            // 2. Persist Committee Assignments
            List<AssignedExpertDto> assignedExpertDtos = new ArrayList<>();
            List<String> assignedExpertIds = new ArrayList<>(si.getAssignedExpertIds());

            // Validate that assigned count matches required count
            if (assignedExpertIds.size() != inspection.getRequiredMemberCount()) {
                throw new IllegalStateException(String.format(
                        "Solver assigned %d experts to inspection %d, but required count is %d",
                        assignedExpertIds.size(), inspId, inspection.getRequiredMemberCount()));
            }

            for (int i = 0; i < assignedExpertIds.size(); i++) {
                Long expertId = Long.parseLong(assignedExpertIds.get(i));
                Expert expert = expertsById.get(expertId);
                if (expert == null) {
                    throw new IllegalStateException("Expert entity not found for ID: " + expertId);
                }

                // First assigned committee member serves as Chairperson; others as Members
                AssignmentRole role = (i == 0) ? AssignmentRole.CHAIRPERSON : AssignmentRole.MEMBER;
                AssignmentStatus status = AssignmentStatus.CONFIRMED;

                CommitteeAssignment assignment = new CommitteeAssignment(savedInspection, expert, role, status);
                CommitteeAssignment savedAssignment = assignmentRepository.save(assignment);

                assignedExpertDtos.add(new AssignedExpertDto(
                        expert.getId(),
                        expert.getName(),
                        expert.getDiscipline(),
                        savedAssignment.getRole(),
                        savedAssignment.getStatus()
                ));
            }

            String institutionName = (savedInspection.getInstitution() != null)
                    ? savedInspection.getInstitution().getName()
                    : "Unknown";
            Long institutionId = (savedInspection.getInstitution() != null)
                    ? savedInspection.getInstitution().getId()
                    : null;

            results.add(new ScheduledInspectionResultDto(
                    savedInspection.getId(),
                    institutionId,
                    institutionName,
                    savedInspection.getScheduledStartDate(),
                    inclusiveEndDate,
                    savedInspection.getDurationInDays(),
                    savedInspection.getRequiredMemberCount(),
                    assignedExpertDtos
            ));
        }

        // Flush all changes within the transaction
        assignmentRepository.flush();
        inspectionRepository.flush();

        log.info("Successfully persisted schedule for {} inspections", results.size());
        return results;
    }
}
