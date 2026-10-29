-- ==============================================================================
-- Flyway Seed V8: Phase 3 Workflow Definitions, Scrutiny Rules & Dev Applications
-- Compatible with PostgreSQL and H2
-- ==============================================================================

-- 1. Standard Workflow Definitions
INSERT INTO workflow_definitions (id, code, category, name, version, aph_reference, requires_evc, requires_shc, requires_sac, is_active)
VALUES
    ('aaaaaaaa-aaaa-aaaa-aaaa-000000000001', 'WF_NEW_INSTITUTION', 'NEW_INSTITUTION', 'Grant of Approval for Setting up New Technical Institution', 'APH_2024_27_V1', 'Chapter I, Approval Process Handbook 2024-27', TRUE, TRUE, TRUE, TRUE),
    ('aaaaaaaa-aaaa-aaaa-aaaa-000000000002', 'WF_EXTENSION_OF_APPROVAL', 'EXTENSION_OF_APPROVAL', 'Extension of Approval (EoA) for Existing Institutions', 'APH_2024_27_V1', 'Chapter II, Approval Process Handbook 2024-27', FALSE, FALSE, FALSE, TRUE),
    ('aaaaaaaa-aaaa-aaaa-aaaa-000000000003', 'WF_INCREASE_INTAKE', 'INCREASE_IN_INTAKE', 'Increase in Intake / Additional Course Approval', 'APH_2024_27_V1', 'Chapter II, Approval Process Handbook 2024-27', FALSE, FALSE, FALSE, TRUE);

-- 2. Deterministic Regulatory Scrutiny Rules Catalog
INSERT INTO scrutiny_rules (id, version, name, description, aph_clause, aph_page_number, application_category, severity, is_active)
VALUES
    ('RULE_APH_MANDATORY_DOCS', 'APH_2024_27_V1', 'Mandatory Document Checklist Compliance', 'Verifies that all mandatory statutory documents required for the application category are uploaded.', 'APH 2024-27 Clause 1.4 & 2.1', 32, 'ALL', 'CRITICAL_DEFICIENCY', TRUE),
    ('RULE_APH_FIRE_SAFETY', 'APH_2024_27_V1', 'Fire Safety Certificate Validity & Expiry Norm', 'Verifies that a valid Fire Safety Certificate / NOC from the Competent Authority is present, covers the current academic session, and is not expired.', 'APH 2024-27 Clause 2.1.2 & Annexure 16', 177, 'ALL', 'CRITICAL_DEFICIENCY', TRUE),
    ('RULE_APH_OCCUPANCY', 'APH_2024_27_V1', 'Building Occupancy / Completion Certificate', 'Verifies that a Building Completion / Occupancy Certificate issued by the Local Competent Authority is present with valid built-up area.', 'APH 2024-27 Clause 2.1.2', 42, 'ALL', 'CRITICAL_DEFICIENCY', TRUE),
    ('RULE_APH_LAND_AREA', 'APH_2024_27_V1', 'Land Title & Contiguous Parcel Area Norm', 'Verifies that land registration title deed is registered in the name of the Trust/Institution and meets minimum acreage requirements.', 'APH 2024-27 Clause 1.4.3 & Chapter IV', 48, 'NEW_INSTITUTION', 'CRITICAL_DEFICIENCY', TRUE),
    ('RULE_APH_INST_NAME', 'APH_2024_27_V1', 'Statutory Institution Name Concordance', 'Verifies that the legal institution name extracted across all uploaded certificates matches the registered institution profile.', 'APH 2024-27 Clause 1.3', 18, 'ALL', 'MAJOR_DEFICIENCY', TRUE),
    ('RULE_APH_CONFIDENCE_GATE', 'APH_2024_27_V1', 'OCR Provenance & Confidence Gate', 'Flags documents where OCR field extraction confidence falls below 70% or key identifiers are unextracted, routing to human scrutiny officer.', 'APH 2024-27 Clause 1.6 Governance Integrity', 24, 'ALL', 'WARNING', TRUE);

-- 3. Development Seed Applications
INSERT INTO applications (
    id, application_number, institution_id, academic_year, category, workflow_definition_id,
    current_state, assigned_to_user_id, created_by, submitted_at, version
)
VALUES
    (
        'bbbbbbbb-bbbb-bbbb-bbbb-000000000001',
        'APP-2024-DEL-001',
        '22222222-2222-2222-2222-222222222222',
        '2024-25',
        'EXTENSION_OF_APPROVAL',
        'aaaaaaaa-aaaa-aaaa-aaaa-000000000002',
        'SUBMITTED',
        '11111111-1111-1111-1111-111111111112',
        '11111111-1111-1111-1111-111111111113',
        CURRENT_TIMESTAMP,
        0
    ),
    (
        'bbbbbbbb-bbbb-bbbb-bbbb-000000000002',
        'APP-2024-MUM-002',
        '33333333-3333-3333-3333-333333333333',
        '2024-25',
        'NEW_INSTITUTION',
        'aaaaaaaa-aaaa-aaaa-aaaa-000000000001',
        'DRAFT',
        NULL,
        '11111111-1111-1111-1111-111111111114',
        NULL,
        0
    );

-- 4. Initial Transition History for the submitted seed application
INSERT INTO workflow_transitions (
    id, application_id, from_state, to_state, actor_id, actor_role, reason, evidence_summary
)
VALUES
    (
        'cccccccc-cccc-cccc-cccc-000000000001',
        'bbbbbbbb-bbbb-bbbb-bbbb-000000000001',
        'DRAFT',
        'SUBMITTED',
        '11111111-1111-1111-1111-111111111113',
        'ROLE_INSTITUTION_OFFICER',
        'Initial submission for Extension of Approval 2024-25',
        'Application declaration and statutory fees completed'
    );
