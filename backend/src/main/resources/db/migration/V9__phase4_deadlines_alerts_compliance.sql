-- ==============================================================================
-- Flyway Migration V9: Phase 4 Deadlines, Alerts & Continuous Compliance Engine
-- Compatible with PostgreSQL and H2 (PostgreSQL compatibility mode)
-- Reference: AICTE Approval Process Handbook (APH 2024-25 to 2026-27)
-- ==============================================================================

-- 1. Deadlines Table
CREATE TABLE IF NOT EXISTS deadlines (
    id UUID PRIMARY KEY,
    deadline_code VARCHAR(64) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    deadline_type VARCHAR(32) NOT NULL,
    statutory_clause VARCHAR(128),
    entity_type VARCHAR(64) NOT NULL,
    entity_id UUID NOT NULL,
    institution_id UUID NOT NULL REFERENCES institutions(id) ON DELETE CASCADE,
    target_date TIMESTAMP WITH TIME ZONE NOT NULL,
    grace_period_days INT NOT NULL DEFAULT 0,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    reminder_schedule_json TEXT,
    last_evaluated_at TIMESTAMP WITH TIME ZONE,
    resolved_at TIMESTAMP WITH TIME ZONE,
    resolved_by_user_id UUID REFERENCES users(id),
    resolution_notes TEXT,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_deadlines_inst_status ON deadlines(institution_id, status);
CREATE INDEX IF NOT EXISTS idx_deadlines_target_status ON deadlines(target_date, status);
CREATE INDEX IF NOT EXISTS idx_deadlines_entity ON deadlines(entity_type, entity_id);

-- 2. Deadline History Table (Audit Trail for Date Recalculations)
CREATE TABLE IF NOT EXISTS deadline_history (
    id UUID PRIMARY KEY,
    deadline_id UUID NOT NULL REFERENCES deadlines(id) ON DELETE CASCADE,
    previous_target_date TIMESTAMP WITH TIME ZONE NOT NULL,
    new_target_date TIMESTAMP WITH TIME ZONE NOT NULL,
    reason TEXT NOT NULL,
    changed_by_user_id UUID REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_deadline_hist_dl ON deadline_history(deadline_id, created_at);

-- 3. Alerts Table
CREATE TABLE IF NOT EXISTS alerts (
    id UUID PRIMARY KEY,
    alert_code VARCHAR(64) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    category VARCHAR(32) NOT NULL,
    severity VARCHAR(32) NOT NULL,
    priority VARCHAR(32) NOT NULL,
    source_entity_type VARCHAR(64) NOT NULL,
    source_entity_id UUID NOT NULL,
    institution_id UUID NOT NULL REFERENCES institutions(id) ON DELETE CASCADE,
    recipient_user_id UUID REFERENCES users(id),
    target_role VARCHAR(64),
    jurisdiction_state VARCHAR(64),
    status VARCHAR(32) NOT NULL DEFAULT 'UNREAD',
    acknowledged_at TIMESTAMP WITH TIME ZONE,
    acknowledged_by_user_id UUID REFERENCES users(id),
    acknowledgement_notes TEXT,
    resolved_at TIMESTAMP WITH TIME ZONE,
    resolved_by_user_id UUID REFERENCES users(id),
    resolution_notes TEXT,
    is_escalated BOOLEAN NOT NULL DEFAULT FALSE,
    escalated_to_role VARCHAR(64),
    escalated_at TIMESTAMP WITH TIME ZONE,
    deduplication_hash VARCHAR(128) NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_alerts_inst_status ON alerts(institution_id, status);
CREATE INDEX IF NOT EXISTS idx_alerts_role_status ON alerts(target_role, status);
CREATE INDEX IF NOT EXISTS idx_alerts_recipient_status ON alerts(recipient_user_id, status);
CREATE INDEX IF NOT EXISTS idx_alerts_created ON alerts(created_at DESC);

-- 4. Alert Deliveries Table (Multi-Channel Dispatch and Retries)
CREATE TABLE IF NOT EXISTS alert_deliveries (
    id UUID PRIMARY KEY,
    alert_id UUID NOT NULL REFERENCES alerts(id) ON DELETE CASCADE,
    channel VARCHAR(32) NOT NULL,
    recipient_address VARCHAR(255) NOT NULL,
    delivery_status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    attempt_count INT NOT NULL DEFAULT 0,
    last_attempt_at TIMESTAMP WITH TIME ZONE,
    next_retry_at TIMESTAMP WITH TIME ZONE,
    error_message TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_alert_deliv_alert ON alert_deliveries(alert_id);
CREATE INDEX IF NOT EXISTS idx_alert_deliv_status_retry ON alert_deliveries(delivery_status, next_retry_at);

-- 5. Compliance Check Runs Table
CREATE TABLE IF NOT EXISTS compliance_check_runs (
    id UUID PRIMARY KEY,
    run_type VARCHAR(32) NOT NULL,
    trigger_source VARCHAR(128) NOT NULL,
    institution_id UUID REFERENCES institutions(id),
    started_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP WITH TIME ZONE,
    status VARCHAR(32) NOT NULL DEFAULT 'RUNNING',
    institutions_checked INT NOT NULL DEFAULT 0,
    findings_generated INT NOT NULL DEFAULT 0,
    alerts_created INT NOT NULL DEFAULT 0,
    deadlines_created INT NOT NULL DEFAULT 0,
    summary_notes TEXT,
    created_by_user_id UUID REFERENCES users(id)
);

CREATE INDEX IF NOT EXISTS idx_cmp_runs_status ON compliance_check_runs(status, started_at DESC);

-- 6. Continuous Compliance Findings Table
CREATE TABLE IF NOT EXISTS compliance_findings (
    id UUID PRIMARY KEY,
    check_run_id UUID NOT NULL REFERENCES compliance_check_runs(id) ON DELETE CASCADE,
    institution_id UUID NOT NULL REFERENCES institutions(id) ON DELETE CASCADE,
    application_id UUID REFERENCES applications(id) ON DELETE CASCADE,
    document_id UUID REFERENCES documents(id) ON DELETE CASCADE,
    rule_code VARCHAR(64) NOT NULL,
    aph_clause VARCHAR(128) NOT NULL,
    finding_type VARCHAR(64) NOT NULL,
    severity VARCHAR(32) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    evidence_summary TEXT,
    status VARCHAR(32) NOT NULL DEFAULT 'OPEN',
    resolved_at TIMESTAMP WITH TIME ZONE,
    resolved_by_user_id UUID REFERENCES users(id),
    resolution_notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_cmp_findings_inst_status ON compliance_findings(institution_id, status);
CREATE INDEX IF NOT EXISTS idx_cmp_findings_app ON compliance_findings(application_id, status);
CREATE INDEX IF NOT EXISTS idx_cmp_findings_rule ON compliance_findings(rule_code, status);

-- 7. Phase 4 Permissions
INSERT INTO permissions (id, code, description) VALUES
    ('00000000-0000-0000-0000-000000000041', 'DEADLINE_READ', 'View statutory and operational deadlines'),
    ('00000000-0000-0000-0000-000000000042', 'DEADLINE_MANAGE', 'Create, recalculate, resolve, or cancel deadlines'),
    ('00000000-0000-0000-0000-000000000043', 'ALERT_READ', 'View regulatory and compliance alerts'),
    ('00000000-0000-0000-0000-000000000044', 'ALERT_ACKNOWLEDGE', 'Acknowledge, dismiss, or resolve alerts'),
    ('00000000-0000-0000-0000-000000000045', 'COMPLIANCE_READ', 'View compliance findings and monitoring history'),
    ('00000000-0000-0000-0000-000000000046', 'COMPLIANCE_EXECUTE', 'Trigger continuous compliance check scans');

-- 8. Role-Permission Mappings for Phase 4
-- Administrator (ROLE_AICTE_ADMIN) - Has all permissions
INSERT INTO role_permissions (role_id, permission_id) VALUES
    ('00000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000041'),
    ('00000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000042'),
    ('00000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000043'),
    ('00000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000044'),
    ('00000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000045'),
    ('00000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000046');

-- Regional Officer (ROLE_REGIONAL_OFFICER)
INSERT INTO role_permissions (role_id, permission_id) VALUES
    ('00000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000041'),
    ('00000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000042'),
    ('00000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000043'),
    ('00000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000044'),
    ('00000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000045'),
    ('00000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000046');

-- Compliance Officer (ROLE_COMPLIANCE_OFFICER)
INSERT INTO role_permissions (role_id, permission_id) VALUES
    ('00000000-0000-0000-0000-000000000004', '00000000-0000-0000-0000-000000000041'),
    ('00000000-0000-0000-0000-000000000004', '00000000-0000-0000-0000-000000000043'),
    ('00000000-0000-0000-0000-000000000004', '00000000-0000-0000-0000-000000000044'),
    ('00000000-0000-0000-0000-000000000004', '00000000-0000-0000-0000-000000000045'),
    ('00000000-0000-0000-0000-000000000004', '00000000-0000-0000-0000-000000000046');

-- Institution Officer (ROLE_INSTITUTION_OFFICER)
INSERT INTO role_permissions (role_id, permission_id) VALUES
    ('00000000-0000-0000-0000-000000000005', '00000000-0000-0000-0000-000000000041'),
    ('00000000-0000-0000-0000-000000000005', '00000000-0000-0000-0000-000000000043'),
    ('00000000-0000-0000-0000-000000000005', '00000000-0000-0000-0000-000000000044'),
    ('00000000-0000-0000-0000-000000000005', '00000000-0000-0000-0000-000000000045');

-- Auditor (ROLE_AUDITOR)
INSERT INTO role_permissions (role_id, permission_id) VALUES
    ('00000000-0000-0000-0000-000000000009', '00000000-0000-0000-0000-000000000041'),
    ('00000000-0000-0000-0000-000000000009', '00000000-0000-0000-0000-000000000043'),
    ('00000000-0000-0000-0000-000000000009', '00000000-0000-0000-0000-000000000045');
