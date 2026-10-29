package org.aicte.sage.modules.notifications.controller;

import jakarta.validation.Valid;
import org.aicte.sage.common.api.ApiResponse;
import org.aicte.sage.common.api.PageResponse;
import org.aicte.sage.common.security.SecurityUtils;
import org.aicte.sage.common.security.UserPrincipal;
import org.aicte.sage.modules.notifications.dto.AcknowledgeAlertRequest;
import org.aicte.sage.modules.notifications.dto.AlertDto;
import org.aicte.sage.modules.notifications.dto.ResolveAlertRequest;
import org.aicte.sage.modules.notifications.entity.AlertSeverity;
import org.aicte.sage.modules.notifications.entity.AlertStatus;
import org.aicte.sage.modules.notifications.service.AlertService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/alerts")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    private UserPrincipal getCurrentPrincipal() {
        return SecurityUtils.getCurrentUserPrincipal()
                .orElseThrow(() -> new IllegalStateException("Unauthenticated request"));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ALERT_READ') or hasRole('ROLE_AICTE_ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<AlertDto>>> getAlerts(
            @RequestParam(required = false) UUID institutionId,
            @RequestParam(required = false) AlertStatus status,
            @RequestParam(required = false) AlertSeverity severity,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        PageRequest pageRequest = PageRequest.of(Math.max(0, page), Math.min(size, 100), Sort.by(direction, sortBy));

        Page<AlertDto> result = alertService.getAlerts(institutionId, status, severity, pageRequest, getCurrentPrincipal());
        return ResponseEntity.ok(ApiResponse.ok("Alerts retrieved successfully", PageResponse.of(result)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ALERT_READ') or hasRole('ROLE_AICTE_ADMIN')")
    public ResponseEntity<ApiResponse<AlertDto>> getAlertById(@PathVariable UUID id) {
        AlertDto result = alertService.getAlertById(id, getCurrentPrincipal());
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @PostMapping("/{id}/acknowledge")
    @PreAuthorize("hasAuthority('ALERT_ACKNOWLEDGE') or hasRole('ROLE_AICTE_ADMIN')")
    public ResponseEntity<ApiResponse<AlertDto>> acknowledgeAlert(
            @PathVariable UUID id,
            @Valid @RequestBody AcknowledgeAlertRequest req) {
        AlertDto result = alertService.acknowledgeAlert(id, req.acknowledgementNotes(), getCurrentPrincipal());
        return ResponseEntity.ok(ApiResponse.ok("Alert acknowledged successfully", result));
    }

    @PostMapping("/{id}/resolve")
    @PreAuthorize("hasAuthority('ALERT_ACKNOWLEDGE') or hasRole('ROLE_AICTE_ADMIN')")
    public ResponseEntity<ApiResponse<AlertDto>> resolveAlert(
            @PathVariable UUID id,
            @Valid @RequestBody ResolveAlertRequest req) {
        AlertDto result = alertService.resolveAlert(id, req.resolutionNotes(), getCurrentPrincipal());
        return ResponseEntity.ok(ApiResponse.ok("Alert resolved successfully", result));
    }

    @PostMapping("/{id}/escalate")
    @PreAuthorize("hasRole('ROLE_AICTE_ADMIN') or hasRole('ROLE_REGIONAL_OFFICER')")
    public ResponseEntity<ApiResponse<AlertDto>> escalateAlert(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "ROLE_REGIONAL_OFFICER") String escalateToRole) {
        AlertDto result = alertService.escalateAlert(id, escalateToRole, getCurrentPrincipal());
        return ResponseEntity.ok(ApiResponse.ok("Alert escalated successfully", result));
    }
}
