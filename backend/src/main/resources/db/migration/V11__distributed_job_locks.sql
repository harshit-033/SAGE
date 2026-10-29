-- ==============================================================================
-- Flyway Migration V11: Database-Backed Distributed Job Locks for Multi-Instance SAGE Clustered Deployment
-- Replaces in-memory AtomicBoolean flags with database-backed leasing & atomic claim
-- ==============================================================================

CREATE TABLE IF NOT EXISTS distributed_job_locks (
    job_name VARCHAR(64) PRIMARY KEY,
    locked_by VARCHAR(128) NOT NULL,
    locked_at TIMESTAMP WITH TIME ZONE NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL
);

-- Pre-seed standard scheduled job entries in an ANSI-compliant, idempotent manner
INSERT INTO distributed_job_locks (job_name, locked_by, locked_at, expires_at)
SELECT 'DEADLINE_EVALUATION', 'SYSTEM_INIT', CURRENT_TIMESTAMP - INTERVAL '1' DAY, CURRENT_TIMESTAMP - INTERVAL '1' DAY
WHERE NOT EXISTS (SELECT 1 FROM distributed_job_locks WHERE job_name = 'DEADLINE_EVALUATION');

INSERT INTO distributed_job_locks (job_name, locked_by, locked_at, expires_at)
SELECT 'NOTIFICATION_RETRY', 'SYSTEM_INIT', CURRENT_TIMESTAMP - INTERVAL '1' DAY, CURRENT_TIMESTAMP - INTERVAL '1' DAY
WHERE NOT EXISTS (SELECT 1 FROM distributed_job_locks WHERE job_name = 'NOTIFICATION_RETRY');

INSERT INTO distributed_job_locks (job_name, locked_by, locked_at, expires_at)
SELECT 'COMPLIANCE_SCAN', 'SYSTEM_INIT', CURRENT_TIMESTAMP - INTERVAL '1' DAY, CURRENT_TIMESTAMP - INTERVAL '1' DAY
WHERE NOT EXISTS (SELECT 1 FROM distributed_job_locks WHERE job_name = 'COMPLIANCE_SCAN');
