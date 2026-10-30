package org.aicte.sage.modules.evc.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.aicte.sage.common.security.UserPrincipal;
import org.aicte.sage.modules.evc.dto.*;
import org.aicte.sage.modules.evc.service.EvcSchedulingService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/evc")
@Tag(name = "EVC Scheduling Engine", description = "AICTE Expert Visit Committee (EVC) scheduling and peer panel appointment API")
public class EvcScheduleController {

    private final EvcSchedulingService schedulingService;

    public EvcScheduleController(EvcSchedulingService schedulingService) {
        this.schedulingService = schedulingService;
    }

    @PostMapping("/schedules")
    @PreAuthorize("hasAnyRole('ROLE_AICTE_ADMIN', 'ROLE_REGIONAL_OFFICER', 'ROLE_EVC_COORDINATOR')")
    @Operation(summary = "Generate and persist an EVC inspection schedule using Google OR-Tools CP-SAT solver")
    public ResponseEntity<SchedulingResponseDto> generateSchedule(
            @Valid @RequestBody SchedulingRequestDto request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        SchedulingResponseDto response = schedulingService.schedule(request, principal);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/inspections/pending")
    @PreAuthorize("hasAnyRole('ROLE_AICTE_ADMIN', 'ROLE_REGIONAL_OFFICER', 'ROLE_EVC_COORDINATOR', 'ROLE_INSTITUTION_OFFICER', 'ROLE_AUDITOR')")
    @Operation(summary = "Retrieve pending inspections awaiting committee scheduling")
    public ResponseEntity<List<EvcInspectionDto>> getPendingInspections(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        List<EvcInspectionDto> inspections = schedulingService.getPendingInspections(principal);
        return ResponseEntity.ok(inspections);
    }

    @GetMapping("/inspections/scheduled")
    @PreAuthorize("hasAnyRole('ROLE_AICTE_ADMIN', 'ROLE_REGIONAL_OFFICER', 'ROLE_EVC_COORDINATOR', 'ROLE_INSTITUTION_OFFICER', 'ROLE_AUDITOR')")
    @Operation(summary = "Retrieve scheduled EVC inspections and committee allocations")
    public ResponseEntity<List<EvcInspectionDto>> getScheduledInspections(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        List<EvcInspectionDto> inspections = schedulingService.getScheduledInspections(principal);
        return ResponseEntity.ok(inspections);
    }

    @GetMapping("/inspections/{id}")
    @PreAuthorize("hasAnyRole('ROLE_AICTE_ADMIN', 'ROLE_REGIONAL_OFFICER', 'ROLE_EVC_COORDINATOR', 'ROLE_INSTITUTION_OFFICER', 'ROLE_AUDITOR')")
    @Operation(summary = "Retrieve inspection profile by ID")
    public ResponseEntity<EvcInspectionDto> getInspectionById(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        EvcInspectionDto inspection = schedulingService.getInspectionById(id, principal);
        return ResponseEntity.ok(inspection);
    }

    @GetMapping("/inspections/{id}/committee")
    @PreAuthorize("hasAnyRole('ROLE_AICTE_ADMIN', 'ROLE_REGIONAL_OFFICER', 'ROLE_EVC_COORDINATOR', 'ROLE_INSTITUTION_OFFICER', 'ROLE_AUDITOR')")
    @Operation(summary = "Retrieve assigned committee experts for an inspection")
    public ResponseEntity<List<AssignedExpertDto>> getCommitteeAssignments(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        List<AssignedExpertDto> committee = schedulingService.getCommitteeAssignments(id, principal);
        return ResponseEntity.ok(committee);
    }

    @GetMapping("/experts")
    @PreAuthorize("hasAnyRole('ROLE_AICTE_ADMIN', 'ROLE_REGIONAL_OFFICER', 'ROLE_EVC_COORDINATOR', 'ROLE_AUDITOR')")
    @Operation(summary = "Retrieve accredited peer review experts directory with calendar availability")
    public ResponseEntity<Page<ExpertDto>> getActiveExperts(Pageable pageable) {
        Page<ExpertDto> experts = schedulingService.getActiveExperts(pageable);
        return ResponseEntity.ok(experts);
    }

    @PostMapping("/inspections/{id}/cancel")
    @PreAuthorize("hasAnyRole('ROLE_AICTE_ADMIN', 'ROLE_REGIONAL_OFFICER', 'ROLE_EVC_COORDINATOR')")
    @Operation(summary = "Cancel an EVC inspection or schedule")
    public ResponseEntity<Void> cancelInspection(
            @PathVariable UUID id,
            @RequestParam(required = false) String reason,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        schedulingService.cancelInspection(id, reason, principal);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/inspections/{id}/report")
    @PreAuthorize("hasAnyRole('ROLE_AICTE_ADMIN', 'ROLE_REGIONAL_OFFICER', 'ROLE_EVC_COORDINATOR', 'ROLE_COMMITTEE_MEMBER', 'ROLE_EVC_MEMBER')")
    @Operation(summary = "Submit EVC inspection report, record committee findings, and advance workflow to EVC_CONDUCTED")
    public ResponseEntity<EvcInspectionDto> submitInspectionReport(
            @PathVariable UUID id,
            @Valid @RequestBody SubmitInspectionReportRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        EvcInspectionDto inspection = schedulingService.submitInspectionReport(id, request, principal);
        return ResponseEntity.ok(inspection);
    }
}
