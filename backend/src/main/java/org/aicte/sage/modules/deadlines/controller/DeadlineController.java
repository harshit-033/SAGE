package org.aicte.sage.modules.deadlines.controller;

import jakarta.validation.Valid;
import org.aicte.sage.common.api.ApiResponse;
import org.aicte.sage.common.api.PageResponse;
import org.aicte.sage.common.security.SecurityUtils;
import org.aicte.sage.common.security.UserPrincipal;
import org.aicte.sage.modules.deadlines.dto.*;
import org.aicte.sage.modules.deadlines.entity.DeadlineStatus;
import org.aicte.sage.modules.deadlines.service.DeadlineService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/deadlines")
public class DeadlineController {

    private final DeadlineService deadlineService;

    public DeadlineController(DeadlineService deadlineService) {
        this.deadlineService = deadlineService;
    }

    private UserPrincipal getCurrentPrincipal() {
        return SecurityUtils.getCurrentUserPrincipal()
                .orElseThrow(() -> new IllegalStateException("Unauthenticated request"));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('DEADLINE_READ') or hasRole('ROLE_AICTE_ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<DeadlineDto>>> getDeadlines(
            @RequestParam(required = false) UUID institutionId,
            @RequestParam(required = false) DeadlineStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "targetDate") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        PageRequest pageRequest = PageRequest.of(Math.max(0, page), Math.min(size, 100), Sort.by(direction, sortBy));

        Page<DeadlineDto> result = deadlineService.getDeadlines(institutionId, status, pageRequest, getCurrentPrincipal());
        return ResponseEntity.ok(ApiResponse.ok("Deadlines retrieved successfully", PageResponse.of(result)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('DEADLINE_READ') or hasRole('ROLE_AICTE_ADMIN')")
    public ResponseEntity<ApiResponse<DeadlineDto>> getDeadlineById(@PathVariable UUID id) {
        DeadlineDto result = deadlineService.getDeadlineById(id, getCurrentPrincipal());
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/{id}/history")
    @PreAuthorize("hasAuthority('DEADLINE_READ') or hasRole('ROLE_AICTE_ADMIN')")
    public ResponseEntity<ApiResponse<List<DeadlineHistoryDto>>> getDeadlineHistory(@PathVariable UUID id) {
        List<DeadlineHistoryDto> result = deadlineService.getDeadlineHistory(id, getCurrentPrincipal());
        return ResponseEntity.ok(ApiResponse.ok("Deadline history retrieved", result));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('DEADLINE_MANAGE') or hasRole('ROLE_AICTE_ADMIN')")
    public ResponseEntity<ApiResponse<DeadlineDto>> createDeadline(@Valid @RequestBody CreateDeadlineRequest req) {
        DeadlineDto result = deadlineService.createDeadline(req, getCurrentPrincipal());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Deadline created successfully", result));
    }

    @PostMapping("/{id}/recalculate")
    @PreAuthorize("hasAuthority('DEADLINE_MANAGE') or hasRole('ROLE_AICTE_ADMIN')")
    public ResponseEntity<ApiResponse<DeadlineDto>> recalculateDeadline(
            @PathVariable UUID id,
            @Valid @RequestBody RecalculateDeadlineRequest req) {
        DeadlineDto result = deadlineService.recalculateDeadline(id, req.newTargetDate(), req.reason(), getCurrentPrincipal());
        return ResponseEntity.ok(ApiResponse.ok("Deadline recalculated successfully", result));
    }

    @PostMapping("/{id}/resolve")
    @PreAuthorize("hasAuthority('DEADLINE_MANAGE') or hasRole('ROLE_AICTE_ADMIN')")
    public ResponseEntity<ApiResponse<DeadlineDto>> resolveDeadline(
            @PathVariable UUID id,
            @Valid @RequestBody ResolveDeadlineRequest req) {
        DeadlineDto result = deadlineService.resolveDeadline(id, req.resolutionNotes(), getCurrentPrincipal());
        return ResponseEntity.ok(ApiResponse.ok("Deadline resolved successfully", result));
    }
}
