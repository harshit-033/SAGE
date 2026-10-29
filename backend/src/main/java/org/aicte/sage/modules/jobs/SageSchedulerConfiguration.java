package org.aicte.sage.modules.jobs;

import org.aicte.sage.modules.compliance.service.ComplianceEngineService;
import org.aicte.sage.modules.deadlines.service.DeadlineService;
import org.aicte.sage.modules.notifications.service.AlertService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.concurrent.atomic.AtomicBoolean;

@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "sage.scheduling.enabled", havingValue = "true", matchIfMissing = true)
public class SageSchedulerConfiguration {

    private static final Logger log = LoggerFactory.getLogger(SageSchedulerConfiguration.class);

    private final DeadlineService deadlineService;
    private final AlertService alertService;
    private final ComplianceEngineService complianceService;

    private final AtomicBoolean deadlineJobRunning = new AtomicBoolean(false);
    private final AtomicBoolean notificationRetryJobRunning = new AtomicBoolean(false);
    private final AtomicBoolean complianceJobRunning = new AtomicBoolean(false);

    public SageSchedulerConfiguration(
            DeadlineService deadlineService,
            AlertService alertService,
            ComplianceEngineService complianceService) {
        this.deadlineService = deadlineService;
        this.alertService = alertService;
        this.complianceService = complianceService;
    }

    /**
     * Evaluates active deadlines and triggers due/overdue alerts.
     * Prevents overlapping executions with atomic guard.
     */
    @Scheduled(fixedRateString = "${sage.scheduling.deadline-rate-ms:60000}", initialDelay = 10000)
    public void scheduleDeadlineEvaluation() {
        if (!deadlineJobRunning.compareAndSet(false, true)) {
            log.debug("Skipping deadline evaluation: previous run is still active.");
            return;
        }
        try {
            int count = deadlineService.evaluateDeadlines();
            if (count > 0) {
                log.info("Scheduled deadline evaluation processed: {} deadlines updated", count);
            }
        } catch (Exception e) {
            log.error("Error during scheduled deadline evaluation: {}", e.getMessage(), e);
        } finally {
            deadlineJobRunning.set(false);
        }
    }

    /**
     * Retries failed alert deliveries with exponential backoff.
     */
    @Scheduled(fixedRateString = "${sage.scheduling.notification-retry-rate-ms:60000}", initialDelay = 15000)
    public void scheduleNotificationRetries() {
        if (!notificationRetryJobRunning.compareAndSet(false, true)) {
            log.debug("Skipping notification retry: previous run is still active.");
            return;
        }
        try {
            int retried = alertService.retryFailedDeliveries();
            if (retried > 0) {
                log.info("Scheduled notification retry job processed: {} deliveries retried", retried);
            }
        } catch (Exception e) {
            log.error("Error during scheduled notification retries: {}", e.getMessage(), e);
        } finally {
            notificationRetryJobRunning.set(false);
        }
    }

    /**
     * Periodic continuous compliance scan.
     */
    @Scheduled(cron = "${sage.scheduling.compliance-cron:0 0 */6 * * *}")
    public void scheduleComplianceScan() {
        if (!complianceJobRunning.compareAndSet(false, true)) {
            log.debug("Skipping compliance scan: previous run is still active.");
            return;
        }
        try {
            log.info("Starting scheduled continuous compliance scan across all institutions...");
            complianceService.executeComplianceCheckRun("SCHEDULED", "Automated Periodic Compliance Cron", null, null);
        } catch (Exception e) {
            log.error("Error during scheduled compliance scan: {}", e.getMessage(), e);
        } finally {
            complianceJobRunning.set(false);
        }
    }
}
