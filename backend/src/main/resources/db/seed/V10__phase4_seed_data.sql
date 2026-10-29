-- ==============================================================================
-- Flyway Seed V10: Phase 4 Demonstration Deadlines, Alerts & Compliance Findings
-- Compatible with PostgreSQL and H2
-- Reference: AICTE Approval Process Handbook (APH 2024-25 to 2026-27)
-- ==============================================================================

-- 1. Demonstration Deadlines
INSERT INTO deadlines (
    id, deadline_code, title, description, deadline_type, statutory_clause,
    entity_type, entity_id, institution_id, target_date, grace_period_days,
    status, reminder_schedule_json, version
)
VALUES
    (
        'dddddddd-dddd-dddd-dddd-000000000001',
        'DL_SCRUTINY_DEFICIENCY_RECTIFICATION',
        'Scrutiny Deficiency Rectification & Compliance Upload',
        'Mandatory 10-day window to upload physical inspection proof and rectification documents for Scrutiny findings.',
        'STATUTORY',
        'APH 2024-27 Clause 1.6 & Clause 2.3',
        'APPLICATION',
        'bbbbbbbb-bbbb-bbbb-bbbb-000000000001',
        '22222222-2222-2222-2222-222222222222',
        CURRENT_TIMESTAMP + INTERVAL '5' DAY,
        0,
        'DUE_SOON',
        '[-7, -3, -1, 0]',
        0
    ),
    (
        'dddddddd-dddd-dddd-dddd-000000000002',
        'DL_FIRE_SAFETY_RENEWAL',
        'Fire Safety Certificate Mandatory Annual Renewal',
        'Local Fire Service NOC requires renewal prior to expiry to maintain continuous institutional safety compliance.',
        'STATUTORY',
        'APH 2024-27 Clause 2.1.2 & Annexure 16',
        'INSTITUTION',
        '33333333-3333-3333-3333-333333333333',
        '33333333-3333-3333-3333-333333333333',
        CURRENT_TIMESTAMP + INTERVAL '12' DAY,
        0,
        'DUE_SOON',
        '[-30, -15, -7, 0]',
        0
    ),
    (
        'dddddddd-dddd-dddd-dddd-000000000003',
        'DL_EOA_SUBMISSION_WINDOW',
        'Annual Extension of Approval (EoA) Window Closing',
        'Final portal cutoff for submitting online EoA proposals for academic session 2024-25.',
        'OFFICIALLY_NOTIFIED',
        'AICTE Approval Calendar 2024-25',
        'APPLICATION',
        'bbbbbbbb-bbbb-bbbb-bbbb-000000000002',
        '33333333-3333-3333-3333-333333333333',
        CURRENT_TIMESTAMP + INTERVAL '30' DAY,
        0,
        'PENDING',
        '[-30, -15, -7, -1, 0]',
        0
    );

-- 2. Demonstration Deadline History
INSERT INTO deadline_history (
    id, deadline_id, previous_target_date, new_target_date, reason, changed_by_user_id
)
VALUES
    (
        'eeeeeeee-eeee-eeee-eeee-000000000001',
        'dddddddd-dddd-dddd-dddd-000000000001',
        CURRENT_TIMESTAMP + INTERVAL '3' DAY,
        CURRENT_TIMESTAMP + INTERVAL '5' DAY,
        'Regional Scrutiny Committee extension granted for physical certificate verification sitting',
        '11111111-1111-1111-1111-111111111112'
    );

-- 3. Demonstration Regulatory Alerts
INSERT INTO alerts (
    id, alert_code, title, message, category, severity, priority,
    source_entity_type, source_entity_id, institution_id, recipient_user_id,
    target_role, jurisdiction_state, status, is_escalated, deduplication_hash
)
VALUES
    (
        'ffffffff-ffff-ffff-ffff-000000000001',
        'ALERT_SCRUTINY_DEFICIENCY',
        'Unresolved Scrutiny Findings Require Urgent Rectification',
        'Application APP-2024-DEL-001 has 4 unresolved scrutiny deficiencies. Deadline expires in 5 days per APH Clause 1.6.',
        'COMPLIANCE',
        'CRITICAL',
        'HIGH',
        'DEADLINE',
        'dddddddd-dddd-dddd-dddd-000000000001',
        '22222222-2222-2222-2222-222222222222',
        '11111111-1111-1111-1111-111111111113',
        'ROLE_INSTITUTION_OFFICER',
        'Delhi',
        'UNREAD',
        FALSE,
        'ALERT:SCRUTINY:DELHI:APP-2024-DEL-001:202425'
    ),
    (
        'ffffffff-ffff-ffff-ffff-000000000002',
        'ALERT_CERTIFICATE_EXPIRING',
        'Fire Safety Certificate Approaching Expiry (12 Days)',
        'Institutional Fire Safety NOC for Mumbai Academy of Pharmaceutical Sciences expires soon. Upload renewed NOC.',
        'DOCUMENT',
        'WARNING',
        'MEDIUM',
        'DEADLINE',
        'dddddddd-dddd-dddd-dddd-000000000002',
        '33333333-3333-3333-3333-333333333333',
        '11111111-1111-1111-1111-111111111114',
        'ROLE_INSTITUTION_OFFICER',
        'Maharashtra',
        'UNREAD',
        FALSE,
        'ALERT:FIRE_SAFETY_EXPIRY:MUMBAI:202425'
    ),
    (
        'ffffffff-ffff-ffff-ffff-000000000003',
        'ALERT_WORKFLOW_ACTION_REQUIRED',
        'Northern Regional Scrutiny Queue Awaiting Officer Review',
        'Application APP-2024-DEL-001 submitted and ready for regional scrutiny committee recommendation.',
        'WORKFLOW',
        'INFO',
        'MEDIUM',
        'APPLICATION',
        'bbbbbbbb-bbbb-bbbb-bbbb-000000000001',
        '22222222-2222-2222-2222-222222222222',
        '11111111-1111-1111-1111-111111111112',
        'ROLE_REGIONAL_OFFICER',
        'Delhi',
        'UNREAD',
        FALSE,
        'ALERT:WORKFLOW_SCRUTINY:NORTHERN_REGION:APP-2024-DEL-001'
    );

-- 4. Demonstration Alert Deliveries
INSERT INTO alert_deliveries (
    id, alert_id, channel, recipient_address, delivery_status, attempt_count, last_attempt_at
)
VALUES
    (
        '10101010-1010-1010-1010-000000000001',
        'ffffffff-ffff-ffff-ffff-000000000001',
        'IN_APP',
        'officer@delhi-tech.edu',
        'SENT',
        1,
        CURRENT_TIMESTAMP
    ),
    (
        '10101010-1010-1010-1010-000000000002',
        'ffffffff-ffff-ffff-ffff-000000000001',
        'EMAIL',
        'officer@delhi-tech.edu',
        'SENT',
        1,
        CURRENT_TIMESTAMP
    ),
    (
        '10101010-1010-1010-1010-000000000003',
        'ffffffff-ffff-ffff-ffff-000000000002',
        'IN_APP',
        'officer@mumbai-engg.edu',
        'SENT',
        1,
        CURRENT_TIMESTAMP
    );

-- 5. Demonstration Compliance Check Run
INSERT INTO compliance_check_runs (
    id, run_type, trigger_source, institution_id, started_at, completed_at,
    status, institutions_checked, findings_generated, alerts_created, deadlines_created,
    summary_notes, created_by_user_id
)
VALUES
    (
        '20202020-2020-2020-2020-000000000001',
        'SCHEDULED',
        'Daily Compliance Cron Job',
        NULL,
        CURRENT_TIMESTAMP - INTERVAL '1' HOUR,
        CURRENT_TIMESTAMP - INTERVAL '58' MINUTE,
        'SUCCESS',
        5,
        2,
        2,
        2,
        'Automated continuous compliance scan completed across all registered institutions.',
        '11111111-1111-1111-1111-111111111111'
    );

-- 6. Demonstration Continuous Compliance Findings
INSERT INTO compliance_findings (
    id, check_run_id, institution_id, application_id, document_id,
    rule_code, aph_clause, finding_type, severity, title, description,
    evidence_summary, status
)
VALUES
    (
        '30303030-3030-3030-3030-000000000001',
        '20202020-2020-2020-2020-000000000001',
        '22222222-2222-2222-2222-222222222222',
        'bbbbbbbb-bbbb-bbbb-bbbb-000000000001',
        NULL,
        'CMP_UNRESOLVED_SCRUTINY_DEFICIENCY',
        'APH 2024-27 Clause 1.6 & Clause 2.3',
        'CONFIRMED_FAILURE',
        'CRITICAL',
        'Unresolved Scrutiny Findings on Submitted Application',
        'Application APP-2024-DEL-001 has 4 unresolved scrutiny deficiencies pending institutional rectification.',
        'Scrutiny evaluation recorded 4 deficiencies during automated APH rule engine execution.',
        'OPEN'
    ),
    (
        '30303030-3030-3030-3030-000000000002',
        '20202020-2020-2020-2020-000000000001',
        '33333333-3333-3333-3333-333333333333',
        NULL,
        NULL,
        'CMP_FIRE_SAFETY_EXPIRING_SOON',
        'APH 2024-27 Clause 2.1.2 & Annexure 16',
        'POTENTIAL_ISSUE',
        'WARNING',
        'Fire Safety NOC Expiring in Less Than 15 Days',
        'Institutional Fire Safety Certificate requires timely renewal to prevent operational suspension.',
        'Extracted validity indicates expiration within current calendar month.',
        'OPEN'
    );
