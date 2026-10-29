package org.aicte.sage.modules.jobs.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Database-backed distributed lock and lease service for clustered multi-instance SAGE deployments.
 * Prevents concurrent execution of scheduled jobs across multiple backend instances.
 * Guarantees safe recovery if a node terminates or is interrupted mid-execution.
 */
@Service
public class DistributedJobLockService {

    private static final Logger log = LoggerFactory.getLogger(DistributedJobLockService.class);

    private final JdbcTemplate jdbcTemplate;
    private final String defaultNodeId;

    public DistributedJobLockService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.defaultNodeId = "sage-node-" + UUID.randomUUID().toString().substring(0, 8);
    }

    public String getDefaultNodeId() {
        return defaultNodeId;
    }

    /**
     * Attempts to acquire an atomic database-backed lease for a specific job name using the default node ID.
     */
    public boolean acquireLock(String jobName, Duration leaseDuration) {
        return acquireLock(jobName, this.defaultNodeId, leaseDuration);
    }

    /**
     * Attempts to acquire an atomic database-backed lease with a custom owner/node ID.
     * Uses REQUIRES_NEW propagation to isolate the lease commit immediately.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean acquireLock(String jobName, String ownerId, Duration leaseDuration) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(leaseDuration);
        Instant past = now.minusSeconds(3600);
        Timestamp nowTs = Timestamp.from(now);
        Timestamp expiresTs = Timestamp.from(expiresAt);
        Timestamp pastTs = Timestamp.from(past);

        // Ensure row exists idempotently with an already expired timestamp
        jdbcTemplate.update(
                "INSERT INTO distributed_job_locks (job_name, locked_by, locked_at, expires_at) " +
                "SELECT ?, 'INIT', ?, ? WHERE NOT EXISTS (SELECT 1 FROM distributed_job_locks WHERE job_name = ?)",
                jobName, pastTs, pastTs, jobName
        );

        // Atomic conditional lease update
        int updated = jdbcTemplate.update(
                "UPDATE distributed_job_locks " +
                "SET locked_by = ?, locked_at = ?, expires_at = ? " +
                "WHERE job_name = ? AND (expires_at <= ? OR locked_by = ?)",
                ownerId, nowTs, expiresTs, jobName, nowTs, ownerId
        );

        if (updated > 0) {
            log.debug("Distributed lock acquired for job '{}' by owner '{}' until {}", jobName, ownerId, expiresAt);
            return true;
        } else {
            log.debug("Distributed lock for job '{}' held by active peer or lease not expired. Skipped by '{}'.", jobName, ownerId);
            return false;
        }
    }

    /**
     * Releases the lock held by the default node ID.
     */
    public void releaseLock(String jobName) {
        releaseLock(jobName, this.defaultNodeId);
    }

    /**
     * Releases the lock held by a specific owner/node ID.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void releaseLock(String jobName, String ownerId) {
        Timestamp nowTs = Timestamp.from(Instant.now());
        jdbcTemplate.update(
                "UPDATE distributed_job_locks SET expires_at = ? WHERE job_name = ? AND locked_by = ?",
                nowTs, jobName, ownerId
        );
        log.debug("Distributed lock released for job '{}' by owner '{}'", jobName, ownerId);
    }

    /**
     * Executes the task under a distributed lock lease.
     * If lock acquisition fails, the task is skipped and this method returns false.
     */
    public boolean runWithLock(String jobName, Duration leaseDuration, Runnable task) {
        return runWithLock(jobName, this.defaultNodeId, leaseDuration, task);
    }

    /**
     * Executes the task under a distributed lock lease for a specific owner.
     */
    public boolean runWithLock(String jobName, String ownerId, Duration leaseDuration, Runnable task) {
        if (!acquireLock(jobName, ownerId, leaseDuration)) {
            return false;
        }
        try {
            task.run();
            return true;
        } finally {
            try {
                releaseLock(jobName, ownerId);
            } catch (Exception e) {
                log.warn("Failed to release lock cleanly for job '{}' by owner '{}': {}", jobName, ownerId, e.getMessage());
            }
        }
    }
}
