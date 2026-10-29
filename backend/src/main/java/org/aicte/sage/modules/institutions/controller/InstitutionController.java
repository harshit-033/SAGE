package org.aicte.sage.modules.institutions.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.aicte.sage.common.api.ApiResponse;
import org.aicte.sage.common.api.PageResponse;
import org.aicte.sage.common.security.SecurityUtils;
import org.aicte.sage.common.security.UserPrincipal;
import org.aicte.sage.modules.institutions.dto.*;
import org.aicte.sage.modules.institutions.service.InstitutionService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    private static final java.util.Set<String> ALLOWED_SORT_FIELDS = java.util.Set.of(
            "name", "code", "city", "state", "category", "status", "createdAt", "updatedAt"
    );

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Search and List Institutions", description = "Retrieves institutions with pagination and filtering, scoped to principal tenancy")
    public ResponseEntity<ApiResponse<PageResponse<InstitutionDto>>> listInstitutions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        UserPrincipal principal = SecurityUtils.getCurrentUserPrincipal()
                .orElseThrow(() -> new IllegalStateException("Unauthenticated request"));

        Pageable pageable = org.aicte.sage.common.api.PaginationUtils.validateAndBuildPageable(
                page, size, sortBy, sortDir, ALLOWED_SORT_FIELDS, "name");

        PageResponse<InstitutionDto> result = institutionService.getInstitutionsPaginated(
                query, state, category, active, pageable, principal);

        return ResponseEntity.ok(ApiResponse.ok(result));
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

    @PutMapping("/{id}")
    @PreAuthorize("@tenancyEvaluator.canUpdateInstitution(authentication, #id)")
    @Operation(summary = "Update Institution Profile", description = "Updates profile and contact information for an authorized institution")
    public ResponseEntity<ApiResponse<InstitutionDto>> updateInstitution(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateInstitutionRequest request,
            HttpServletRequest servletRequest) {
        UserPrincipal principal = SecurityUtils.getCurrentUserPrincipal()
                .orElseThrow(() -> new IllegalStateException("Unauthenticated request"));
        InstitutionDto updated = institutionService.updateInstitution(id, request, principal, servletRequest.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok("Institution profile updated successfully", updated));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('INSTITUTION_ACTIVATE') or hasRole('ROLE_AICTE_ADMIN')")
    @Operation(summary = "Activate or Suspend Institution", description = "Toggles institutional active status with regulatory audit trail")
    public ResponseEntity<ApiResponse<InstitutionDto>> updateInstitutionStatus(
            @PathVariable UUID id,
            @Valid @RequestBody InstitutionStatusRequest request,
            HttpServletRequest servletRequest) {
        UserPrincipal principal = SecurityUtils.getCurrentUserPrincipal()
                .orElseThrow(() -> new IllegalStateException("Unauthenticated request"));
        InstitutionDto updated = institutionService.setInstitutionActiveStatus(id, request, principal, servletRequest.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok("Institution status updated successfully", updated));
    }

    @GetMapping("/{id}/users")
    @PreAuthorize("@tenancyEvaluator.hasAccessToInstitution(authentication, #id)")
    @Operation(summary = "List Institution Affiliated Users", description = "Lists authorized representatives and signatories affiliated with an institution")
    public ResponseEntity<ApiResponse<List<InstitutionUserDto>>> getInstitutionUsers(@PathVariable UUID id) {
        UserPrincipal principal = SecurityUtils.getCurrentUserPrincipal()
                .orElseThrow(() -> new IllegalStateException("Unauthenticated request"));
        List<InstitutionUserDto> users = institutionService.getInstitutionUsers(id, principal);
        return ResponseEntity.ok(ApiResponse.ok(users));
    }

    @PostMapping("/{id}/users")
    @PreAuthorize("@tenancyEvaluator.canUpdateInstitution(authentication, #id)")
    @Operation(summary = "Affiliate User with Institution", description = "Affiliates a user account with an institution")
    public ResponseEntity<ApiResponse<InstitutionUserDto>> addInstitutionUser(
            @PathVariable UUID id,
            @Valid @RequestBody AddInstitutionUserRequest request,
            HttpServletRequest servletRequest) {
        UserPrincipal principal = SecurityUtils.getCurrentUserPrincipal()
                .orElseThrow(() -> new IllegalStateException("Unauthenticated request"));
        InstitutionUserDto created = institutionService.addInstitutionUser(id, request, principal, servletRequest.getRemoteAddr());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("User affiliated successfully", created));
    }

    @DeleteMapping("/{id}/users/{userId}")
    @PreAuthorize("@tenancyEvaluator.canUpdateInstitution(authentication, #id)")
    @Operation(summary = "Disaffiliate User from Institution", description = "Removes institutional association for a user")
    public ResponseEntity<ApiResponse<Void>> removeInstitutionUser(
            @PathVariable UUID id,
            @PathVariable UUID userId,
            HttpServletRequest servletRequest) {
        UserPrincipal principal = SecurityUtils.getCurrentUserPrincipal()
                .orElseThrow(() -> new IllegalStateException("Unauthenticated request"));
        institutionService.removeInstitutionUser(id, userId, principal, servletRequest.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok("User disaffiliated successfully", null));
    }
}
