package org.aicte.sage.modules.evc.service;

import org.aicte.sage.modules.applications.model.Application;
import org.aicte.sage.modules.applications.model.ApplicationState;
import org.aicte.sage.modules.applications.repository.ApplicationRepository;
import org.aicte.sage.modules.evc.dto.AssignedExpertDto;
import org.aicte.sage.modules.evc.dto.ScheduledInspectionResultDto;
import org.aicte.sage.modules.evc.entity.*;
import org.aicte.sage.modules.evc.repository.CommitteeAssignmentRepository;
import org.aicte.sage.modules.evc.repository.EvcInspectionRepository;
import org.aicte.sage.modules.evc.scheduler.model.ScheduledInspection;
import org.aicte.sage.modules.evc.scheduler.model.SchedulingResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

/**
 * Transactional persistence service committing EVC scheduled outcomes into authoritative SAGE persistence.
 * Runs strictly isolated to ensure database transactions are open only during writes and never during CP-SAT solving.
 */
@Service
public class SchedulePersistenceService {

    private static final Logger log = LoggerFactory.getLogger(SchedulePersistenceService.class);

    private final EvcInspectionRepository inspectionRepository;
    private final CommitteeAssignmentRepository assignmentRepository;
    private final ApplicationRepository applicationRepository;

    public SchedulePersistenceService(
            EvcInspectionRepository inspectionRepository,
            CommitteeAssignmentRepository assignmentRepository,
            ApplicationRepository applicationRepository
    ) {
        this.inspectionRepository = inspectionRepository;
        this.assignmentRepository = assignmentRepository;
        this.applicationRepository = applicationRepository;
    }

    /**
     * Atomically persists a successful solver schedule into the database.
     * Maps scheduled start dates and derived inclusive end dates, clears previous assignments
     * if rescheduling is authorized, and persists committee member assignments.
     */
    @Transactional(rollbackFor = Exception.class)
    public List<ScheduledInspectionResultDto> persistScheduledSchedule(
            SchedulingResult solverResult,
            Map<UUID, EvcInspection> inspectionsById,
            Map<UUID, Expert> expertsById,
            boolean allowRescheduling
    ) {
        if (solverResult == null || !solverResult.isSuccess()) {
            throw new IllegalArgumentException("Cannot persist an unsuccessful solver result");
        }

        List<ScheduledInspectionResultDto> results = new ArrayList<>();

        for (ScheduledInspection si : solverResult.getScheduledInspections().values()) {
            UUID inspId = UUID.fromString(si.getInspectionId());
            EvcInspection inspection = inspectionsById.get(inspId);
            if (inspection == null) {
                throw new IllegalStateException("Inspection entity not found for ID: " + inspId);
            }

            // Rescheduling safety check
            if (inspection.getStatus() == InspectionStatus.SCHEDULED) {
                if (!allowRescheduling) {
                    throw new IllegalStateException(String.format(
                            "Inspection %s is already SCHEDULED. Rescheduling must be explicitly enabled.", inspId));
                }
                log.info("Rescheduling authorized for inspection {}. Clearing existing committee assignments.", inspId);
                List<CommitteeAssignment> existingAssignments = assignmentRepository.findByInspection(inspection);
                if (!existingAssignments.isEmpty()) {
                    assignmentRepository.deleteAll(existingAssignments);
                    assignmentRepository.flush();
                }
            }

            // 1. Update Inspection start date, end date, and status
            inspection.setScheduledStartDate(si.getScheduledStartDate());
            inspection.setScheduledEndDate(si.getScheduledEndDate());
            inspection.setStatus(InspectionStatus.SCHEDULED);
            EvcInspection savedInspection = inspectionRepository.save(inspection);

            // 2. Transition linked Application workflow state to EVC_SCHEDULED if applicable
            Application application = savedInspection.getApplication();
            if (application != null && application.getCurrentState() == ApplicationState.SCRUTINY_PASSED) {
                log.info("Transitioning application {} state from SCRUTINY_PASSED to EVC_SCHEDULED", application.getApplicationNumber());
                application.setCurrentState(ApplicationState.EVC_SCHEDULED);
                applicationRepository.save(application);
            }

            // 3. Persist Committee Assignments with deterministic role policy
            List<AssignedExpertDto> assignedExpertDtos = new ArrayList<>();
            List<String> assignedExpertIds = new ArrayList<>(si.getAssignedExpertIds());

            // Validate that assigned count matches required count
            if (assignedExpertIds.size() != inspection.getRequiredMemberCount()) {
                throw new IllegalStateException(String.format(
                        "Solver assigned %d experts to inspection %s, but required count is %d",
                        assignedExpertIds.size(), inspId, inspection.getRequiredMemberCount()));
            }

            // Sort assigned expert IDs deterministically to ensure auditable role distribution
            Collections.sort(assignedExpertIds);

            for (int i = 0; i < assignedExpertIds.size(); i++) {
                UUID expertId = UUID.fromString(assignedExpertIds.get(i));
                Expert expert = expertsById.get(expertId);
                if (expert == null) {
                    throw new IllegalStateException("Expert entity not found for ID: " + expertId);
                }

                // Deterministic policy: First assigned expert serves as Committee CHAIRPERSON; subsequent as MEMBERs
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

            String instName = (savedInspection.getInstitution() != null)
                    ? savedInspection.getInstitution().getName()
                    : "Unknown";
            UUID instId = (savedInspection.getInstitution() != null)
                    ? savedInspection.getInstitution().getId()
                    : null;
            UUID appId = (savedInspection.getApplication() != null)
                    ? savedInspection.getApplication().getId()
                    : null;
            String appNum = (savedInspection.getApplication() != null)
                    ? savedInspection.getApplication().getApplicationNumber()
                    : "";

            results.add(new ScheduledInspectionResultDto(
                    savedInspection.getId(),
                    instId,
                    instName,
                    appId,
                    appNum,
                    savedInspection.getScheduledStartDate(),
                    savedInspection.getScheduledEndDate(),
                    savedInspection.getDurationInDays(),
                    savedInspection.getRequiredMemberCount(),
                    assignedExpertDtos
            ));
        }

        // Flush all changes within the transaction
        assignmentRepository.flush();
        inspectionRepository.flush();

        log.info("Successfully persisted schedule for {} EVC inspections", results.size());
        return results;
    }
}
