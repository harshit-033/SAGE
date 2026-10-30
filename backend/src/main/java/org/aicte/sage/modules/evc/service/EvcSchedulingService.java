package org.aicte.sage.modules.evc.service;

import org.aicte.sage.common.security.UserPrincipal;
import org.aicte.sage.modules.evc.dto.AssignedExpertDto;
import org.aicte.sage.modules.evc.dto.EvcInspectionDto;
import org.aicte.sage.modules.evc.dto.ExpertDto;
import org.aicte.sage.modules.evc.dto.SchedulingRequestDto;
import org.aicte.sage.modules.evc.dto.SchedulingResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface EvcSchedulingService {

    /**
     * Solves and commits an optimal EVC committee inspection schedule.
     */
    SchedulingResponseDto schedule(SchedulingRequestDto request, UserPrincipal principal);

    /**
     * Retrieves all pending inspections awaiting committee schedule generation.
     */
    List<EvcInspectionDto> getPendingInspections(UserPrincipal principal);

    /**
     * Retrieves all scheduled inspections.
     */
    List<EvcInspectionDto> getScheduledInspections(UserPrincipal principal);

    /**
     * Retrieves a single inspection profile by ID.
     */
    EvcInspectionDto getInspectionById(UUID id, UserPrincipal principal);

    /**
     * Retrieves committee assignments for an inspection.
     */
    List<AssignedExpertDto> getCommitteeAssignments(UUID inspectionId, UserPrincipal principal);

    /**
     * Retrieves paginated directory of peer experts and their upcoming availability.
     */
    Page<ExpertDto> getActiveExperts(Pageable pageable);

    /**
     * Cancels an inspection or its schedule.
     */
    void cancelInspection(UUID inspectionId, String reason, UserPrincipal principal);

    /**
     * Submits an EVC inspection report, records committee findings, and advances the application workflow to EVC_CONDUCTED.
     */
    EvcInspectionDto submitInspectionReport(UUID inspectionId, org.aicte.sage.modules.evc.dto.SubmitInspectionReportRequest request, UserPrincipal principal);
}
