-- ==============================================================================
-- Flyway Seed Data V6: Non-Production Demonstration Assignments & Appointments
-- Loaded ONLY when spring.profiles.active includes 'dev' or 'test'
-- ==============================================================================

-- 1. Regional Jurisdiction Assignment for regional@aicte.gov.in
-- Covers Northern Region (Delhi, Haryana, Punjab, etc.)
INSERT INTO jurisdiction_assignments (
    id, user_id, region, state, active, valid_from, valid_until
) VALUES (
    '77777777-7777-7777-7777-777777777771',
    '11111111-1111-1111-1111-111111111112',
    'NORTHERN',
    'Delhi',
    TRUE,
    CURRENT_TIMESTAMP - INTERVAL '30' DAY,
    CURRENT_TIMESTAMP + INTERVAL '365' DAY
);

-- 2. Explicit Case Assignment for compliance@aicte.gov.in on Delhi Tech
INSERT INTO case_assignments (
    id, user_id, institution_id, assignment_type, case_reference, active, assigned_at, valid_until
) VALUES (
    '88888888-8888-8888-8888-888888888881',
    '11111111-1111-1111-1111-111111111115',
    '22222222-2222-2222-2222-222222222222',
    'COMPLIANCE_MONITORING',
    'CASE-DEL-2026-COMPL-001',
    TRUE,
    CURRENT_TIMESTAMP - INTERVAL '15' DAY,
    CURRENT_TIMESTAMP + INTERVAL '180' DAY
);

-- 3. Explicit Case Assignment for evc.coord@aicte.gov.in on Delhi Tech
INSERT INTO case_assignments (
    id, user_id, institution_id, assignment_type, case_reference, active, assigned_at, valid_until
) VALUES (
    '88888888-8888-8888-8888-888888888882',
    '11111111-1111-1111-1111-111111111117',
    '22222222-2222-2222-2222-222222222222',
    'EVC_VISIT',
    'CASE-DEL-2026-EVC-001',
    TRUE,
    CURRENT_TIMESTAMP - INTERVAL '10' DAY,
    CURRENT_TIMESTAMP + INTERVAL '90' DAY
);

-- 4. Explicit Case Assignment for auditor@aicte.gov.in on Delhi Tech
INSERT INTO case_assignments (
    id, user_id, institution_id, assignment_type, case_reference, active, assigned_at, valid_until
) VALUES (
    '88888888-8888-8888-8888-888888888883',
    '11111111-1111-1111-1111-111111111116',
    '22222222-2222-2222-2222-222222222222',
    'AUDIT_REVIEW',
    'CASE-DEL-2026-AUDIT-001',
    TRUE,
    CURRENT_TIMESTAMP - INTERVAL '5' DAY,
    CURRENT_TIMESTAMP + INTERVAL '60' DAY
);

-- 5. Explicit Committee Appointment for Smt. Priya Nair on Standing Complaint Scrutiny Committee (SCSC)
INSERT INTO committee_appointments (
    id, user_id, committee_type, role_in_committee, qualification_details, active, valid_from, valid_until
) VALUES (
    '99999999-9999-9999-9999-999999999991',
    '11111111-1111-1111-1111-111111111116',
    'SCSC',
    'EXPERT_MEMBER',
    'Associate Professor in Technical Education per APH Annexure 16.7',
    TRUE,
    CURRENT_TIMESTAMP - INTERVAL '30' DAY,
    CURRENT_TIMESTAMP + INTERVAL '365' DAY
);
