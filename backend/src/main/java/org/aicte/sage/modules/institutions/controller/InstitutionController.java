package org.aicte.sage.modules.institutions.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.aicte.sage.common.api.ApiResponse;
import org.aicte.sage.common.security.SecurityUtils;
import org.aicte.sage.common.security.UserPrincipal;
import org.aicte.sage.modules.institutions.dto.CreateInstitutionRequest;
import org.aicte.sage.modules.institutions.dto.InstitutionDto;
import org.aicte.sage.modules.institutions.service.InstitutionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/institutions")
@Tag(name = "Institutions", description = "Multi-Tenant Technical Institution Management")
public class InstitutionController {

    private final InstitutionService institutionService;

    public InstitutionController(InstitutionService institutionService) {
        this.institutionService = institutionService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_AICTE_ADMIN', 'ROLE_REGIONAL_OFFICER', 'ROLE_INSTITUTION_OFFICER')")
    @Operation(summary = "List Institutions", description = "Retrieves institutions scoped strictly to the authenticated principal's tenancy")
    public ResponseEntity<ApiResponse<List<InstitutionDto>>> listInstitutions() {
        UserPrincipal principal = SecurityUtils.getCurrentUserPrincipal()
                .orElseThrow(() -> new IllegalStateException("Unauthenticated request"));
        List<InstitutionDto> institutions = institutionService.getInstitutionsForPrincipal(principal);
        return ResponseEntity.ok(ApiResponse.ok(institutions));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@tenancyEvaluator.hasAccessToInstitution(authentication, #id)")
    @Operation(summary = "Get Institution Details", description = "Retrieves detailed institution profile with tenancy boundary enforcement")
    public ResponseEntity<ApiResponse<InstitutionDto>> getInstitutionById(@PathVariable UUID id) {
        UserPrincipal principal = SecurityUtils.getCurrentUserPrincipal()
                .orElseThrow(() -> new IllegalStateException("Unauthenticated request"));
        InstitutionDto dto = institutionService.getInstitutionById(id, principal);
        return ResponseEntity.ok(ApiResponse.ok(dto));
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_AICTE_ADMIN')")
    @Operation(summary = "Create Institution", description = "Registers a new technical institution under AICTE jurisdiction")
    public ResponseEntity<ApiResponse<InstitutionDto>> createInstitution(
            @Valid @RequestBody CreateInstitutionRequest request,
            HttpServletRequest servletRequest) {
        UserPrincipal principal = SecurityUtils.getCurrentUserPrincipal()
                .orElseThrow(() -> new IllegalStateException("Unauthenticated request"));
        InstitutionDto created = institutionService.createInstitution(request, principal, servletRequest.getRemoteAddr());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Institution created successfully", created));
    }
}
