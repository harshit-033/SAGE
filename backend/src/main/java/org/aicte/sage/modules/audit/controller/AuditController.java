package org.aicte.sage.modules.audit.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.aicte.sage.common.api.ApiResponse;
import org.aicte.sage.common.api.PageResponse;
import org.aicte.sage.modules.audit.dto.AuditEventDto;
import org.aicte.sage.modules.audit.entity.AuditEvent;
import org.aicte.sage.modules.audit.repository.AuditEventRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/audit")
@Tag(name = "Audit", description = "Regulatory Audit Trail and Security Event Inspection")
public class AuditController {

    private final AuditEventRepository auditEventRepository;

    public AuditController(AuditEventRepository auditEventRepository) {
        this.auditEventRepository = auditEventRepository;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_AICTE_ADMIN', 'ROLE_AUDITOR') or hasAuthority('AUDIT_READ')")
    @Operation(summary = "Search Audit Events", description = "Retrieves paginated regulatory audit logs and consequential system actions")
    public ResponseEntity<ApiResponse<PageResponse<AuditEventDto>>> listAuditEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(required = false) String principal,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String resourceType,
            @RequestParam(required = false) String correlationId,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<AuditEvent> eventPage = auditEventRepository.searchAuditEvents(principal, eventType, resourceType, correlationId, pageable);
        List<AuditEventDto> dtos = eventPage.getContent().stream()
                .map(AuditEventDto::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(eventPage, dtos)));
    }
}
