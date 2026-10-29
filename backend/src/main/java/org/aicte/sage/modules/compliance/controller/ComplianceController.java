package org.aicte.sage.modules.compliance.controller;

import jakarta.validation.Valid;
import org.aicte.sage.common.api.ApiResponse;
import org.aicte.sage.common.api.PageResponse;
import org.aicte.sage.common.security.SecurityUtils;
import org.aicte.sage.common.security.UserPrincipal;
import org.aicte.sage.modules.compliance.dto.ComplianceCheckRunDto;
import org.aicte.sage.modules.compliance.dto.ComplianceFindingDto;
import org.aicte.sage.modules.compliance.dto.ResolveComplianceFindingRequest;
import org.aicte.sage.modules.compliance.entity.ComplianceFindingStatus;
import org.aicte.sage.modules.compliance.service.ComplianceEngineService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/compliance")
public class ComplianceController {

    private final ComplianceEngineService complianceService;

    public ComplianceController(ComplianceEngineService complianceService) {
        this.complianceService = complianceService;
    }

    private UserPrincipal getCurrentPrincipal() {
        return SecurityUtils.getCurrentUserPrincipal()
                .orElseThrow(() -> new IllegalStateException("Unauthenticated request"));
    }

    @GetMapping("/findings")
    @PreAuthorize("hasAuthority('COMPLIANCE_READ') or hasRole('ROLE_AICTE_ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<ComplianceFindingDto>>> getFindings(
            @RequestParam(required = false) UUID institutionId,
            @RequestParam(required = false) ComplianceFindingStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        PageRequest pageRequest = PageRequest.of(Math.max(0, page), Math.min(size, 100), Sort.by(direction, sortBy));

        Page<ComplianceFindingDto> result = complianceService.getFindings(institutionId, status, pageRequest, getCurrentPrincipal());
        return ResponseEntity.ok(ApiResponse.ok("Compliance findings retrieved successfully", PageResponse.of(result)));
    }

    @PostMapping("/findings/{id}/resolve")
    @PreAuthorize("hasAuthority('SCRUTINY_REVIEW') or hasRole('ROLE_AICTE_ADMIN') or hasRole('ROLE_COMPLIANCE_OFFICER')")
    public ResponseEntity<ApiResponse<ComplianceFindingDto>> resolveFinding(
            @PathVariable UUID id,
            @Valid @RequestBody ResolveComplianceFindingRequest req) {

        ComplianceFindingDto result = complianceService.resolveFinding(id, req.resolutionNotes(), getCurrentPrincipal());
        return ResponseEntity.ok(ApiResponse.ok("Compliance finding resolved successfully", result));
    }

    @GetMapping("/runs")
    @PreAuthorize("hasAuthority('COMPLIANCE_READ') or hasRole('ROLE_AICTE_ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<ComplianceCheckRunDto>>> getCheckRuns(
            @RequestParam(required = false) UUID institutionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "startedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        PageRequest pageRequest = PageRequest.of(Math.max(0, page), Math.min(size, 100), Sort.by(direction, sortBy));

        Page<ComplianceCheckRunDto> result = complianceService.getCheckRuns(institutionId, pageRequest, getCurrentPrincipal());
        return ResponseEntity.ok(ApiResponse.ok("Compliance check runs retrieved successfully", PageResponse.of(result)));
    }

    @PostMapping("/scan")
    @PreAuthorize("hasAuthority('COMPLIANCE_EXECUTE') or hasRole('ROLE_AICTE_ADMIN') or hasRole('ROLE_COMPLIANCE_OFFICER')")
    public ResponseEntity<ApiResponse<ComplianceCheckRunDto>> triggerComplianceScan(
            @RequestParam(required = false) UUID institutionId) {

        ComplianceCheckRunDto result = complianceService.executeComplianceCheckRun(
                "MANUAL",
                "Triggered by " + getCurrentPrincipal().getUsername(),
                institutionId,
                getCurrentPrincipal()
        );
        return ResponseEntity.ok(ApiResponse.ok("Continuous compliance scan executed successfully", result));
    }
}
