package org.aicte.sage.modules.jobs;

import org.aicte.sage.modules.compliance.service.ComplianceEngineService;
import org.aicte.sage.modules.deadlines.service.DeadlineService;
import org.aicte.sage.modules.jobs.service.DistributedJobLockService;
import org.aicte.sage.modules.notifications.service.AlertService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Duration;

@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "sage.scheduling.enabled", havingValue = "true", matchIfMissing = true)
public class SageSchedulerConfiguration {

    private static final Logger log = LoggerFactory.getLogger(SageSchedulerConfiguration.class);

    private final DeadlineService deadlineService;
    private final AlertService alertService;
    private final ComplianceEngineService complianceService;
    private final DistributedJobLockService lockService;

    public SageSchedulerConfiguration(
            DeadlineService deadlineService,
            AlertService alertService,
            ComplianceEngineService complianceService,
            DistributedJobLockService lockService) {
        this.deadlineService = deadlineService;
        this.alertService = alertService;
        this.complianceService = complianceService;
        this.lockService = lockService;
    }

    /**
     * Evaluates active deadlines and triggers due/overdue alerts.
     * Prevents overlapping executions across clustered instances with a database-backed distributed lease.
     */
    @Scheduled(fixedRateString = "${sage.scheduling.deadline-rate-ms:60000}", initialDelay = 10000)
    public void scheduleDeadlineEvaluation() {
        boolean executed = lockService.runWithLock("DEADLINE_EVALUATION", Duration.ofMinutes(2), () -> {
            try {
                int count = deadlineService.evaluateDeadlines();
                if (count > 0) {
                    log.info("Scheduled deadline evaluation processed: {} deadlines updated", count);
                }
            } catch (Exception e) {
                log.error("Error during scheduled deadline evaluation: {}", e.getMessage(), e);
            }
        });
        if (!executed) {
            log.debug("Deadline evaluation skipped: lock held by another cluster instance.");
        }
    }

    /**
     * Retries failed alert deliveries with exponential backoff.
     * Prevents duplicate deliveries across multiple instances via distributed locking.
     */
    @Scheduled(fixedRateString = "${sage.scheduling.notification-retry-rate-ms:60000}", initialDelay = 15000)
    public void scheduleNotificationRetries() {
        boolean executed = lockService.runWithLock("NOTIFICATION_RETRY", Duration.ofMinutes(2), () -> {
            try {
                int retried = alertService.retryFailedDeliveries();
                if (retried > 0) {
                    log.info("Scheduled notification retry job processed: {} deliveries retried", retried);
                }
            } catch (Exception e) {
                log.error("Error during scheduled notification retries: {}", e.getMessage(), e);
            }
        });
        if (!executed) {
            log.debug("Notification retries skipped: lock held by another cluster instance.");
        }
    }

    /**
     * Periodic continuous compliance scan across all institutions.
     * Distributed lock guarantees a single cluster node conducts the scan.
     */
    @Scheduled(cron = "${sage.scheduling.compliance-cron:0 0 */6 * * *}")
    public void scheduleComplianceScan() {
        boolean executed = lockService.runWithLock("COMPLIANCE_SCAN", Duration.ofMinutes(15), () -> {
            try {
                log.info("Starting scheduled continuous compliance scan across all institutions...");
                complianceService.executeComplianceCheckRun("SCHEDULED", "Automated Periodic Compliance Cron", null, null);
            } catch (Exception e) {
                log.error("Error during scheduled compliance scan: {}", e.getMessage(), e);
            }
        });
        if (!executed) {
            log.debug("Compliance scan skipped: lock held by another cluster instance.");
        }
    }
}
