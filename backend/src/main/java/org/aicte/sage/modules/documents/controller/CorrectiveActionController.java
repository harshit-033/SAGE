package org.aicte.sage.modules.documents.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.aicte.sage.common.api.ApiResponse;
import org.aicte.sage.common.api.PageResponse;
import org.aicte.sage.common.security.SecurityUtils;
import org.aicte.sage.common.security.UserPrincipal;
import org.aicte.sage.modules.documents.dto.DocumentCorrectiveActionDto;
import org.aicte.sage.modules.documents.dto.ResolveCorrectiveActionRequest;
import org.aicte.sage.modules.documents.dto.RespondCorrectiveActionRequest;
import org.aicte.sage.modules.documents.model.CorrectiveActionStatus;
import org.aicte.sage.modules.documents.service.DocumentComplianceService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/corrective-actions")
@Tag(name = "Corrective Actions & Deficiency Resolution", description = "Lifecycle management of document findings, statutory deficiencies, and institutional responses")
public class CorrectiveActionController {

    private final DocumentComplianceService complianceService;

    public CorrectiveActionController(DocumentComplianceService complianceService) {
        this.complianceService = complianceService;
    }

    private UserPrincipal getCurrentPrincipal() {
        return SecurityUtils.getCurrentUserPrincipal()
                .orElseThrow(() -> new IllegalStateException("Unauthenticated request"));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('DOCUMENT_READ')")
    @Operation(summary = "List corrective actions", description = "Search and filter pending required corrective actions across applications and documents")
    public ResponseEntity<ApiResponse<PageResponse<DocumentCorrectiveActionDto>>> listActions(
            @RequestParam(required = false) UUID institutionId,
            @RequestParam(required = false) CorrectiveActionStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PageResponse<DocumentCorrectiveActionDto> response = complianceService.listCorrectiveActions(
                institutionId, status, page, size, getCurrentPrincipal()
        );
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('DOCUMENT_READ')")
    @Operation(summary = "Get corrective action detail", description = "Retrieves full details of a specific corrective action and linked evidence")
    public ResponseEntity<ApiResponse<DocumentCorrectiveActionDto>> getAction(
            @PathVariable UUID id
    ) {
        DocumentCorrectiveActionDto dto = complianceService.getCorrectiveAction(id, getCurrentPrincipal());
        return ResponseEntity.ok(ApiResponse.ok(dto));
    }

    @PostMapping("/{id}/respond")
    @PreAuthorize("hasAuthority('DOCUMENT_UPLOAD')")
    @Operation(summary = "Submit response to corrective action", description = "Institution provides explanation and/or references replacement evidence document")
    public ResponseEntity<ApiResponse<DocumentCorrectiveActionDto>> respondAction(
            @PathVariable UUID id,
            @Valid @RequestBody RespondCorrectiveActionRequest request
    ) {
        DocumentCorrectiveActionDto dto = complianceService.respondCorrectiveAction(id, request, getCurrentPrincipal());
        return ResponseEntity.ok(ApiResponse.ok("Response submitted successfully", dto));
    }

    @PostMapping("/{id}/resolve")
    @PreAuthorize("hasAnyRole('ROLE_AICTE_ADMIN', 'ROLE_REGIONAL_OFFICER', 'ROLE_COMPLIANCE_OFFICER')")
    @Operation(summary = "Resolve or reject corrective action", description = "Officer reviews response and transitions status to RESOLVED or REJECTED")
    public ResponseEntity<ApiResponse<DocumentCorrectiveActionDto>> resolveAction(
            @PathVariable UUID id,
            @Valid @RequestBody ResolveCorrectiveActionRequest request
    ) {
        DocumentCorrectiveActionDto dto = complianceService.resolveCorrectiveAction(id, request, getCurrentPrincipal());
        return ResponseEntity.ok(ApiResponse.ok("Corrective action updated successfully", dto));
    }
}
