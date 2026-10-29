package org.aicte.sage.modules.jobs;

import org.aicte.sage.common.api.ApiResponse;
import org.aicte.sage.common.security.SecurityUtils;
import org.aicte.sage.common.security.UserPrincipal;
import org.aicte.sage.modules.compliance.dto.ComplianceCheckRunDto;
import org.aicte.sage.modules.compliance.service.ComplianceEngineService;
import org.aicte.sage.modules.deadlines.service.DeadlineService;
import org.aicte.sage.modules.notifications.service.AlertService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/jobs")
@PreAuthorize("hasRole('ROLE_AICTE_ADMIN')")
public class AdminJobController {

    private final DeadlineService deadlineService;
    private final AlertService alertService;
    private final ComplianceEngineService complianceService;

    public AdminJobController(
            DeadlineService deadlineService,
            AlertService alertService,
            ComplianceEngineService complianceService) {
        this.deadlineService = deadlineService;
        this.alertService = alertService;
        this.complianceService = complianceService;
    }

    private UserPrincipal getCurrentPrincipal() {
        return SecurityUtils.getCurrentUserPrincipal()
                .orElseThrow(() -> new IllegalStateException("Unauthenticated request"));
    }

    @PostMapping("/deadlines/evaluate")
    public ResponseEntity<ApiResponse<Map<String, Object>>> triggerDeadlineEvaluation() {
        int count = deadlineService.evaluateDeadlines();
        return ResponseEntity.ok(ApiResponse.ok(
                "Deadline evaluation completed: " + count + " deadlines updated",
                Map.of("processedCount", count, "status", "SUCCESS")
        ));
    }

    @PostMapping("/notifications/retry")
    public ResponseEntity<ApiResponse<Map<String, Object>>> triggerNotificationRetries() {
        int retried = alertService.retryFailedDeliveries();
        return ResponseEntity.ok(ApiResponse.ok(
                "Notification retries executed: " + retried + " deliveries retried",
                Map.of("retriedCount", retried, "status", "SUCCESS")
        ));
    }

    @PostMapping("/compliance/run")
    public ResponseEntity<ApiResponse<ComplianceCheckRunDto>> triggerComplianceScan(
            @RequestParam(required = false) UUID institutionId) {
        ComplianceCheckRunDto result = complianceService.executeComplianceCheckRun(
                "MANUAL",
                "Triggered by Admin: " + getCurrentPrincipal().getUsername(),
                institutionId,
                getCurrentPrincipal()
        );
        return ResponseEntity.ok(ApiResponse.ok("Compliance scan triggered successfully", result));
    }
}
