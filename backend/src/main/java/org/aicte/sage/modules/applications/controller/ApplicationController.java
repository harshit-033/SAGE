package org.aicte.sage.modules.applications.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.aicte.sage.common.api.ApiResponse;
import org.aicte.sage.common.api.PageResponse;
import org.aicte.sage.common.security.SecurityUtils;
import org.aicte.sage.common.security.UserPrincipal;
import org.aicte.sage.modules.applications.dto.ApplicationDto;
import org.aicte.sage.modules.applications.dto.CreateApplicationRequest;
import org.aicte.sage.modules.applications.model.ApplicationCategory;
import org.aicte.sage.modules.applications.model.ApplicationState;
import org.aicte.sage.modules.applications.service.ApplicationService;
import org.aicte.sage.modules.workflow.dto.TransitionRequest;
import org.aicte.sage.modules.workflow.dto.WorkflowTransitionDto;
import org.aicte.sage.modules.workflow.service.WorkflowEngineService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/applications")
@Tag(name = "Applications & Approval Workflows", description = "AICTE Approval applications and workflow engine lifecycle")
public class ApplicationController {

    private final ApplicationService applicationService;
    private final WorkflowEngineService workflowEngineService;

    public ApplicationController(
            ApplicationService applicationService,
            WorkflowEngineService workflowEngineService
    ) {
        this.applicationService = applicationService;
        this.workflowEngineService = workflowEngineService;
    }

    private UserPrincipal getCurrentPrincipal() {
        return SecurityUtils.getCurrentUserPrincipal()
                .orElseThrow(() -> new IllegalStateException("Unauthenticated request"));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('APPLICATION_CREATE')")
    @Operation(summary = "Initiate new approval application", description = "Creates a new application draft for an institution")
    public ResponseEntity<ApiResponse<ApplicationDto>> createApplication(
            @Valid @RequestBody CreateApplicationRequest request
    ) {
        ApplicationDto dto = applicationService.createApplication(request, getCurrentPrincipal());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Application created successfully", dto));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('APPLICATION_READ')")
    @Operation(summary = "Get application details", description = "Retrieves application status and workflow information")
    public ResponseEntity<ApiResponse<ApplicationDto>> getApplication(
            @PathVariable UUID id
    ) {
        ApplicationDto dto = applicationService.getApplication(id, getCurrentPrincipal());
        return ResponseEntity.ok(ApiResponse.ok(dto));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('APPLICATION_READ')")
    @Operation(summary = "List applications", description = "Search and paginate approval applications with tenant isolation")
    public ResponseEntity<ApiResponse<PageResponse<ApplicationDto>>> listApplications(
            @RequestParam(required = false) UUID institutionId,
            @RequestParam(required = false) ApplicationState state,
            @RequestParam(required = false) ApplicationCategory category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PageResponse<ApplicationDto> response = applicationService.listApplications(institutionId, state, category, page, size, getCurrentPrincipal());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAuthority('APPLICATION_SUBMIT')")
    @Operation(summary = "Submit application", description = "Transitions an application draft to SUBMITTED")
    public ResponseEntity<ApiResponse<WorkflowTransitionDto>> submitApplication(
            @PathVariable UUID id
    ) {
        WorkflowTransitionDto dto = applicationService.submitApplication(id, getCurrentPrincipal());
        return ResponseEntity.ok(ApiResponse.ok("Application submitted successfully", dto));
    }

    @PostMapping("/{id}/transitions")
    @PreAuthorize("hasAnyRole('ROLE_AICTE_ADMIN', 'ROLE_REGIONAL_OFFICER', 'ROLE_COMMITTEE_MEMBER') or hasAuthority('WORKFLOW_TRANSITION')")
    @Operation(summary = "Execute state transition", description = "Performs an authorized statutory workflow transition")
    public ResponseEntity<ApiResponse<WorkflowTransitionDto>> transitionState(
            @PathVariable UUID id,
            @Valid @RequestBody TransitionRequest request
    ) {
        WorkflowTransitionDto dto = workflowEngineService.transitionState(id, request, getCurrentPrincipal());
        return ResponseEntity.ok(ApiResponse.ok("Transition executed successfully", dto));
    }

    @GetMapping("/{id}/transitions")
    @PreAuthorize("hasAuthority('APPLICATION_READ')")
    @Operation(summary = "Get transition history", description = "Retrieves complete chronological audit trail of state transitions")
    public ResponseEntity<ApiResponse<List<WorkflowTransitionDto>>> getTransitions(
            @PathVariable UUID id
    ) {
        List<WorkflowTransitionDto> history = workflowEngineService.getTransitionsForApplication(id, getCurrentPrincipal());
        return ResponseEntity.ok(ApiResponse.ok(history));
    }

    @PostMapping("/{id}/withdraw")
    @PreAuthorize("hasAuthority('APPLICATION_SUBMIT')")
    @Operation(summary = "Withdraw application", description = "Allows applicant institution to withdraw application")
    public ResponseEntity<ApiResponse<WorkflowTransitionDto>> withdrawApplication(
            @PathVariable UUID id,
            @RequestParam(required = false) String reason
    ) {
        WorkflowTransitionDto dto = applicationService.withdrawApplication(id, reason, getCurrentPrincipal());
        return ResponseEntity.ok(ApiResponse.ok("Application withdrawn", dto));
    }
}
