package org.aicte.sage.modules.audit.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.aicte.sage.common.api.ApiResponse;
import org.aicte.sage.common.api.PageResponse;
import org.aicte.sage.common.api.PaginationUtils;
import org.aicte.sage.common.security.SecurityUtils;
import org.aicte.sage.common.security.UserPrincipal;
import org.aicte.sage.modules.audit.dto.AuditEventDto;
import org.aicte.sage.modules.audit.entity.AuditEvent;
import org.aicte.sage.modules.audit.repository.AuditEventRepository;
import org.aicte.sage.modules.governance.repository.CaseAssignmentRepository;
import org.aicte.sage.modules.governance.repository.JurisdictionAssignmentRepository;
import org.aicte.sage.modules.institutions.entity.Institution;
import org.aicte.sage.modules.institutions.repository.InstitutionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/audit")
@Tag(name = "Audit", description = "Regulatory Audit Trail and Security Event Inspection")
public class AuditController {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "createdAt", "eventType", "principal", "action", "resourceType"
    );

    private final AuditEventRepository auditEventRepository;
    private final JurisdictionAssignmentRepository jurisdictionRepository;
    private final CaseAssignmentRepository caseAssignmentRepository;
    private final InstitutionRepository institutionRepository;

    public AuditController(AuditEventRepository auditEventRepository,
                           JurisdictionAssignmentRepository jurisdictionRepository,
                           CaseAssignmentRepository caseAssignmentRepository,
                           InstitutionRepository institutionRepository) {
        this.auditEventRepository = auditEventRepository;
        this.jurisdictionRepository = jurisdictionRepository;
        this.caseAssignmentRepository = caseAssignmentRepository;
        this.institutionRepository = institutionRepository;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_AICTE_ADMIN', 'ROLE_AUDITOR', 'ROLE_REGIONAL_OFFICER') or hasAuthority('AUDIT_READ')")
    @Operation(summary = "Search Audit Events", description = "Retrieves paginated regulatory audit logs scoped to caller authorization")
    public ResponseEntity<ApiResponse<PageResponse<AuditEventDto>>> listAuditEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(required = false) String principal,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String resourceType,
            @RequestParam(required = false) String correlationId,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        UserPrincipal userPrincipal = SecurityUtils.getCurrentUserPrincipal()
                .orElseThrow(() -> new IllegalStateException("Unauthenticated request"));

        Pageable pageable = PaginationUtils.validateAndBuildPageable(page, size, sortBy, sortDir, ALLOWED_SORT_FIELDS, "createdAt");

        Page<AuditEvent> eventPage;

        if (userPrincipal.hasRole("ROLE_AICTE_ADMIN")) {
            // Central Admin has global audit visibility
            eventPage = auditEventRepository.searchAuditEvents(principal, eventType, resourceType, correlationId, pageable);
        } else {
            // Scope audit access to authorized institutions
            Set<UUID> authorizedInstitutions = new HashSet<>(userPrincipal.getAuthorizedInstitutionIds());

            if (userPrincipal.hasRole("ROLE_REGIONAL_OFFICER")) {
                List<String> states = jurisdictionRepository.findActiveStatesByUserId(userPrincipal.getId());
                if (states.contains("*")) {
                    authorizedInstitutions.addAll(institutionRepository.findAll().stream().map(Institution::getId).toList());
                } else if (!states.isEmpty()) {
                    List<Institution> insts = institutionRepository.findAll().stream()
                            .filter(i -> i.getState() != null && states.stream().anyMatch(s -> s.equalsIgnoreCase(i.getState())))
                            .toList();
                    insts.forEach(i -> authorizedInstitutions.add(i.getId()));
                }
            }

            List<UUID> assignedIds = caseAssignmentRepository.findActiveInstitutionIdsByUserId(userPrincipal.getId());
            authorizedInstitutions.addAll(assignedIds);

            if (authorizedInstitutions.isEmpty()) {
                eventPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
            } else {
                eventPage = auditEventRepository.searchScopedAuditEvents(
                        authorizedInstitutions,
                        userPrincipal.getUsername(),
                        principal,
                        eventType,
                        resourceType,
                        correlationId,
                        pageable
                );
            }
        }

        List<AuditEventDto> dtos = eventPage.getContent().stream()
                .map(AuditEventDto::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(eventPage, dtos)));
    }
}
