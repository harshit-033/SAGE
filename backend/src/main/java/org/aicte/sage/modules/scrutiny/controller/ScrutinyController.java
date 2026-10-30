package org.aicte.sage.modules.scrutiny.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.aicte.sage.common.api.ApiResponse;
import org.aicte.sage.common.security.SecurityUtils;
import org.aicte.sage.common.security.UserPrincipal;
import org.aicte.sage.modules.scrutiny.dto.ResolveFindingRequest;
import org.aicte.sage.modules.scrutiny.dto.ScrutinyEvaluationDto;
import org.aicte.sage.modules.scrutiny.dto.ScrutinyFindingDto;
import org.aicte.sage.modules.scrutiny.service.ScrutinyEngineService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/applications/{applicationId}/scrutiny")
@Tag(name = "Document Scrutiny & Rule Engine", description = "Deterministic APH 2024-27 statutory document rule scrutiny")
public class ScrutinyController {

    private final ScrutinyEngineService scrutinyEngineService;

    public ScrutinyController(ScrutinyEngineService scrutinyEngineService) {
        this.scrutinyEngineService = scrutinyEngineService;
    }

    private UserPrincipal getCurrentPrincipal() {
        return SecurityUtils.getCurrentUserPrincipal()
                .orElseThrow(() -> new IllegalStateException("Unauthenticated request"));
    }

    @PostMapping("/evaluate")
    @PreAuthorize("hasAuthority('SCRUTINY_EXECUTE')")
    @Operation(summary = "Execute deterministic scrutiny", description = "Runs rule engine against uploaded documents and extracted OCR fields")
    public ResponseEntity<ApiResponse<ScrutinyEvaluationDto>> evaluateApplication(
            @PathVariable UUID applicationId
    ) {
        ScrutinyEvaluationDto evaluation = scrutinyEngineService.evaluateApplication(applicationId, getCurrentPrincipal());
        return ResponseEntity.ok(ApiResponse.ok("Scrutiny evaluation completed", evaluation));
    }

    @GetMapping("/findings")
    @PreAuthorize("hasAuthority('APPLICATION_READ')")
    @Operation(summary = "Get scrutiny findings", description = "Returns active deficiency and compliance findings for an application")
    public ResponseEntity<ApiResponse<List<ScrutinyFindingDto>>> getFindings(
            @PathVariable UUID applicationId
    ) {
        List<ScrutinyFindingDto> findings = scrutinyEngineService.getFindingsForApplication(applicationId, getCurrentPrincipal());
        return ResponseEntity.ok(ApiResponse.ok(findings));
    }

    @GetMapping("/evaluations")
    @PreAuthorize("hasAuthority('APPLICATION_READ')")
    @Operation(summary = "Get evaluation history", description = "Returns chronological scrutiny evaluation runs")
    public ResponseEntity<ApiResponse<List<ScrutinyEvaluationDto>>> getEvaluations(
            @PathVariable UUID applicationId
    ) {
        List<ScrutinyEvaluationDto> evaluations = scrutinyEngineService.getEvaluationsForApplication(applicationId, getCurrentPrincipal());
        return ResponseEntity.ok(ApiResponse.ok(evaluations));
    }

    @PostMapping("/findings/{findingId}/resolve")
    @PreAuthorize("hasAuthority('SCRUTINY_REVIEW')")
    @Operation(summary = "Resolve deficiency finding", description = "Allows authorized officer to review and resolve deficiency with notes")
    public ResponseEntity<ApiResponse<ScrutinyFindingDto>> resolveFinding(
            @PathVariable UUID applicationId,
            @PathVariable UUID findingId,
            @Valid @RequestBody ResolveFindingRequest request
    ) {
        ScrutinyFindingDto resolved = scrutinyEngineService.resolveFinding(findingId, request, getCurrentPrincipal());
        return ResponseEntity.ok(ApiResponse.ok("Deficiency finding resolved successfully", resolved));
    }

    @PostMapping("/assess")
    @PreAuthorize("hasAnyRole('ROLE_AICTE_ADMIN', 'ROLE_REGIONAL_OFFICER', 'ROLE_SCRUTINY_OFFICER', 'ROLE_COMMITTEE_MEMBER')")
    @Operation(summary = "Submit structured evaluator assessment", description = "Records evaluator criteria scores, remarks, and recommendation")
    public ResponseEntity<ApiResponse<ScrutinyEvaluationDto>> submitEvaluatorAssessment(
            @PathVariable UUID applicationId,
            @Valid @RequestBody org.aicte.sage.modules.scrutiny.dto.SubmitEvaluatorAssessmentRequest request
    ) {
        ScrutinyEvaluationDto evaluation = scrutinyEngineService.submitEvaluatorAssessment(applicationId, request, getCurrentPrincipal());
        return ResponseEntity.ok(ApiResponse.ok("Evaluator assessment recorded successfully", evaluation));
    }
}
