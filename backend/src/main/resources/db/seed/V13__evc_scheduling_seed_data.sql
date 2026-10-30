-- ==============================================================================
-- Flyway Seed V13: EVC Peer Experts, Calendar Availability & Demonstration Inspections
-- Compatible with PostgreSQL and H2
-- Reference: AICTE Approval Process Handbook (APH 2024-25 to 2026-27)
-- ==============================================================================

-- 1. Insert Peer Review Evaluators / Experts
INSERT INTO experts (id, name, email, discipline, designation, affiliated_institution_id, active)
VALUES
    (
        'eeeeeeee-eeee-eeee-eeee-000000000001',
        'Prof. K. Radhakrishnan',
        'k.radhakrishnan@iitd.ac.in',
        'COMPUTER_SCIENCE_ENGINEERING',
        'Senior Professor & Former HoD',
        NULL,
        TRUE
    ),
    (
        'eeeeeeee-eeee-eeee-eeee-000000000002',
        'Dr. Manju Sharma',
        'manju.sharma@nitk.edu.in',
        'CIVIL_ENGINEERING',
        'Professor & Dean (Infrastructure)',
        NULL,
        TRUE
    ),
    (
        'eeeeeeee-eeee-eeee-eeee-000000000003',
        'Dr. Arvind Joshi',
        'arvind.joshi@vjti.ac.in',
        'ELECTRICAL_ENGINEERING',
        'Professor & Head of Electrical Engineering',
        NULL,
        TRUE
    ),
    (
        'eeeeeeee-eeee-eeee-eeee-000000000004',
        'Dr. Meenakshi Sundaram',
        'meenakshi.s@ictmumbai.edu.in',
        'PHARMACY',
        'Professor of Pharmaceutical Chemistry',
        NULL,
        TRUE
    ),
    (
        'eeeeeeee-eeee-eeee-eeee-000000000005',
        'Prof. V. Ramakrishnan',
        'v.ramakrishnan@spa.ac.in',
        'ARCHITECTURE',
        'Professor & Town Planning Specialist',
        NULL,
        TRUE
    ),
    (
        'eeeeeeee-eeee-eeee-eeee-000000000006',
        'Dr. Suresh N.',
        'suresh.n@iisc.ac.in',
        'MECHANICAL_ENGINEERING',
        'Chief Research Scientist',
        NULL,
        TRUE
    );

-- 2. Insert Calendar Availability Records for Upcoming Inspection Windows (Days 5 to 25)
-- Explicit portable inserts ensuring 100% compatibility across PostgreSQL and H2
INSERT INTO expert_availabilities (id, expert_id, available_date) VALUES
    ('aaaaaaaa-0001-0001-0001-000000000005', 'eeeeeeee-eeee-eeee-eeee-000000000001', CURRENT_DATE + INTERVAL '5' DAY),
    ('aaaaaaaa-0001-0001-0001-000000000006', 'eeeeeeee-eeee-eeee-eeee-000000000001', CURRENT_DATE + INTERVAL '6' DAY),
    ('aaaaaaaa-0001-0001-0001-000000000007', 'eeeeeeee-eeee-eeee-eeee-000000000001', CURRENT_DATE + INTERVAL '7' DAY),
    ('aaaaaaaa-0001-0001-0001-000000000008', 'eeeeeeee-eeee-eeee-eeee-000000000001', CURRENT_DATE + INTERVAL '8' DAY),
    ('aaaaaaaa-0001-0001-0001-000000000009', 'eeeeeeee-eeee-eeee-eeee-000000000001', CURRENT_DATE + INTERVAL '9' DAY),
    ('aaaaaaaa-0001-0001-0001-000000000010', 'eeeeeeee-eeee-eeee-eeee-000000000001', CURRENT_DATE + INTERVAL '10' DAY),
    ('aaaaaaaa-0001-0001-0001-000000000011', 'eeeeeeee-eeee-eeee-eeee-000000000001', CURRENT_DATE + INTERVAL '11' DAY),
    ('aaaaaaaa-0001-0001-0001-000000000012', 'eeeeeeee-eeee-eeee-eeee-000000000001', CURRENT_DATE + INTERVAL '12' DAY),
    ('aaaaaaaa-0001-0001-0001-000000000013', 'eeeeeeee-eeee-eeee-eeee-000000000001', CURRENT_DATE + INTERVAL '13' DAY),
    ('aaaaaaaa-0001-0001-0001-000000000014', 'eeeeeeee-eeee-eeee-eeee-000000000001', CURRENT_DATE + INTERVAL '14' DAY),
    ('aaaaaaaa-0001-0001-0001-000000000015', 'eeeeeeee-eeee-eeee-eeee-000000000001', CURRENT_DATE + INTERVAL '15' DAY),

    ('aaaaaaaa-0002-0001-0001-000000000005', 'eeeeeeee-eeee-eeee-eeee-000000000002', CURRENT_DATE + INTERVAL '5' DAY),
    ('aaaaaaaa-0002-0001-0001-000000000006', 'eeeeeeee-eeee-eeee-eeee-000000000002', CURRENT_DATE + INTERVAL '6' DAY),
    ('aaaaaaaa-0002-0001-0001-000000000007', 'eeeeeeee-eeee-eeee-eeee-000000000002', CURRENT_DATE + INTERVAL '7' DAY),
    ('aaaaaaaa-0002-0001-0001-000000000008', 'eeeeeeee-eeee-eeee-eeee-000000000002', CURRENT_DATE + INTERVAL '8' DAY),
    ('aaaaaaaa-0002-0001-0001-000000000009', 'eeeeeeee-eeee-eeee-eeee-000000000002', CURRENT_DATE + INTERVAL '9' DAY),
    ('aaaaaaaa-0002-0001-0001-000000000010', 'eeeeeeee-eeee-eeee-eeee-000000000002', CURRENT_DATE + INTERVAL '10' DAY),
    ('aaaaaaaa-0002-0001-0001-000000000011', 'eeeeeeee-eeee-eeee-eeee-000000000002', CURRENT_DATE + INTERVAL '11' DAY),
    ('aaaaaaaa-0002-0001-0001-000000000012', 'eeeeeeee-eeee-eeee-eeee-000000000002', CURRENT_DATE + INTERVAL '12' DAY),
    ('aaaaaaaa-0002-0001-0001-000000000013', 'eeeeeeee-eeee-eeee-eeee-000000000002', CURRENT_DATE + INTERVAL '13' DAY),
    ('aaaaaaaa-0002-0001-0001-000000000014', 'eeeeeeee-eeee-eeee-eeee-000000000002', CURRENT_DATE + INTERVAL '14' DAY),
    ('aaaaaaaa-0002-0001-0001-000000000015', 'eeeeeeee-eeee-eeee-eeee-000000000002', CURRENT_DATE + INTERVAL '15' DAY),

    ('aaaaaaaa-0003-0001-0001-000000000005', 'eeeeeeee-eeee-eeee-eeee-000000000003', CURRENT_DATE + INTERVAL '5' DAY),
    ('aaaaaaaa-0003-0001-0001-000000000006', 'eeeeeeee-eeee-eeee-eeee-000000000003', CURRENT_DATE + INTERVAL '6' DAY),
    ('aaaaaaaa-0003-0001-0001-000000000007', 'eeeeeeee-eeee-eeee-eeee-000000000003', CURRENT_DATE + INTERVAL '7' DAY),
    ('aaaaaaaa-0003-0001-0001-000000000008', 'eeeeeeee-eeee-eeee-eeee-000000000003', CURRENT_DATE + INTERVAL '8' DAY),
    ('aaaaaaaa-0003-0001-0001-000000000009', 'eeeeeeee-eeee-eeee-eeee-000000000003', CURRENT_DATE + INTERVAL '9' DAY),
    ('aaaaaaaa-0003-0001-0001-000000000010', 'eeeeeeee-eeee-eeee-eeee-000000000003', CURRENT_DATE + INTERVAL '10' DAY),
    ('aaaaaaaa-0003-0001-0001-000000000011', 'eeeeeeee-eeee-eeee-eeee-000000000003', CURRENT_DATE + INTERVAL '11' DAY),
    ('aaaaaaaa-0003-0001-0001-000000000012', 'eeeeeeee-eeee-eeee-eeee-000000000003', CURRENT_DATE + INTERVAL '12' DAY),
    ('aaaaaaaa-0003-0001-0001-000000000013', 'eeeeeeee-eeee-eeee-eeee-000000000003', CURRENT_DATE + INTERVAL '13' DAY),
    ('aaaaaaaa-0003-0001-0001-000000000014', 'eeeeeeee-eeee-eeee-eeee-000000000003', CURRENT_DATE + INTERVAL '14' DAY),
    ('aaaaaaaa-0003-0001-0001-000000000015', 'eeeeeeee-eeee-eeee-eeee-000000000003', CURRENT_DATE + INTERVAL '15' DAY),

    ('aaaaaaaa-0004-0001-0001-000000000007', 'eeeeeeee-eeee-eeee-eeee-000000000004', CURRENT_DATE + INTERVAL '7' DAY),
    ('aaaaaaaa-0004-0001-0001-000000000008', 'eeeeeeee-eeee-eeee-eeee-000000000004', CURRENT_DATE + INTERVAL '8' DAY),
    ('aaaaaaaa-0004-0001-0001-000000000009', 'eeeeeeee-eeee-eeee-eeee-000000000004', CURRENT_DATE + INTERVAL '9' DAY),
    ('aaaaaaaa-0004-0001-0001-000000000010', 'eeeeeeee-eeee-eeee-eeee-000000000004', CURRENT_DATE + INTERVAL '10' DAY),
    ('aaaaaaaa-0004-0001-0001-000000000011', 'eeeeeeee-eeee-eeee-eeee-000000000004', CURRENT_DATE + INTERVAL '11' DAY),
    ('aaaaaaaa-0004-0001-0001-000000000012', 'eeeeeeee-eeee-eeee-eeee-000000000004', CURRENT_DATE + INTERVAL '12' DAY),
    ('aaaaaaaa-0004-0001-0001-000000000013', 'eeeeeeee-eeee-eeee-eeee-000000000004', CURRENT_DATE + INTERVAL '13' DAY),
    ('aaaaaaaa-0004-0001-0001-000000000014', 'eeeeeeee-eeee-eeee-eeee-000000000004', CURRENT_DATE + INTERVAL '14' DAY),
    ('aaaaaaaa-0004-0001-0001-000000000015', 'eeeeeeee-eeee-eeee-eeee-000000000004', CURRENT_DATE + INTERVAL '15' DAY),
    ('aaaaaaaa-0004-0001-0001-000000000016', 'eeeeeeee-eeee-eeee-eeee-000000000004', CURRENT_DATE + INTERVAL '16' DAY),
    ('aaaaaaaa-0004-0001-0001-000000000017', 'eeeeeeee-eeee-eeee-eeee-000000000004', CURRENT_DATE + INTERVAL '17' DAY),

    ('aaaaaaaa-0005-0001-0001-000000000007', 'eeeeeeee-eeee-eeee-eeee-000000000005', CURRENT_DATE + INTERVAL '7' DAY),
    ('aaaaaaaa-0005-0001-0001-000000000008', 'eeeeeeee-eeee-eeee-eeee-000000000005', CURRENT_DATE + INTERVAL '8' DAY),
    ('aaaaaaaa-0005-0001-0001-000000000009', 'eeeeeeee-eeee-eeee-eeee-000000000005', CURRENT_DATE + INTERVAL '9' DAY),
    ('aaaaaaaa-0005-0001-0001-000000000010', 'eeeeeeee-eeee-eeee-eeee-000000000005', CURRENT_DATE + INTERVAL '10' DAY),
    ('aaaaaaaa-0005-0001-0001-000000000011', 'eeeeeeee-eeee-eeee-eeee-000000000005', CURRENT_DATE + INTERVAL '11' DAY),
    ('aaaaaaaa-0005-0001-0001-000000000012', 'eeeeeeee-eeee-eeee-eeee-000000000005', CURRENT_DATE + INTERVAL '12' DAY),
    ('aaaaaaaa-0005-0001-0001-000000000013', 'eeeeeeee-eeee-eeee-eeee-000000000005', CURRENT_DATE + INTERVAL '13' DAY),
    ('aaaaaaaa-0005-0001-0001-000000000014', 'eeeeeeee-eeee-eeee-eeee-000000000005', CURRENT_DATE + INTERVAL '14' DAY),
    ('aaaaaaaa-0005-0001-0001-000000000015', 'eeeeeeee-eeee-eeee-eeee-000000000005', CURRENT_DATE + INTERVAL '15' DAY),
    ('aaaaaaaa-0005-0001-0001-000000000016', 'eeeeeeee-eeee-eeee-eeee-000000000005', CURRENT_DATE + INTERVAL '16' DAY),
    ('aaaaaaaa-0005-0001-0001-000000000017', 'eeeeeeee-eeee-eeee-eeee-000000000005', CURRENT_DATE + INTERVAL '17' DAY),

    ('aaaaaaaa-0006-0001-0001-000000000005', 'eeeeeeee-eeee-eeee-eeee-000000000006', CURRENT_DATE + INTERVAL '5' DAY),
    ('aaaaaaaa-0006-0001-0001-000000000006', 'eeeeeeee-eeee-eeee-eeee-000000000006', CURRENT_DATE + INTERVAL '6' DAY),
    ('aaaaaaaa-0006-0001-0001-000000000007', 'eeeeeeee-eeee-eeee-eeee-000000000006', CURRENT_DATE + INTERVAL '7' DAY),
    ('aaaaaaaa-0006-0001-0001-000000000008', 'eeeeeeee-eeee-eeee-eeee-000000000006', CURRENT_DATE + INTERVAL '8' DAY),
    ('aaaaaaaa-0006-0001-0001-000000000009', 'eeeeeeee-eeee-eeee-eeee-000000000006', CURRENT_DATE + INTERVAL '9' DAY),
    ('aaaaaaaa-0006-0001-0001-000000000010', 'eeeeeeee-eeee-eeee-eeee-000000000006', CURRENT_DATE + INTERVAL '10' DAY),
    ('aaaaaaaa-0006-0001-0001-000000000011', 'eeeeeeee-eeee-eeee-eeee-000000000006', CURRENT_DATE + INTERVAL '11' DAY),
    ('aaaaaaaa-0006-0001-0001-000000000012', 'eeeeeeee-eeee-eeee-eeee-000000000006', CURRENT_DATE + INTERVAL '12' DAY),
    ('aaaaaaaa-0006-0001-0001-000000000013', 'eeeeeeee-eeee-eeee-eeee-000000000006', CURRENT_DATE + INTERVAL '13' DAY),
    ('aaaaaaaa-0006-0001-0001-000000000014', 'eeeeeeee-eeee-eeee-eeee-000000000006', CURRENT_DATE + INTERVAL '14' DAY),
    ('aaaaaaaa-0006-0001-0001-000000000015', 'eeeeeeee-eeee-eeee-eeee-000000000006', CURRENT_DATE + INTERVAL '15' DAY);

-- 3. Insert Demonstration EVC Inspections awaiting schedule generation
INSERT INTO evc_inspections (
    id, application_id, institution_id, duration_in_days, required_member_count,
    earliest_start_date, latest_start_date, status, special_instructions, version
)
VALUES
    (
        'ffffffff-ffff-ffff-ffff-000000000001',
        'bbbbbbbb-bbbb-bbbb-bbbb-000000000001',
        '22222222-2222-2222-2222-222222222222',
        3,
        3,
        CURRENT_DATE + INTERVAL '5' DAY,
        CURRENT_DATE + INTERVAL '15' DAY,
        'PENDING',
        'Physical verification of Laboratories, Built-up area, and AICTE compliance proofs per Chapter I norms.',
        0
    ),
    (
        'ffffffff-ffff-ffff-ffff-000000000002',
        'bbbbbbbb-bbbb-bbbb-bbbb-000000000002',
        '33333333-3333-3333-3333-333333333333',
        2,
        2,
        CURRENT_DATE + INTERVAL '7' DAY,
        CURRENT_DATE + INTERVAL '18' DAY,
        'PENDING',
        'Verification of Pharmacy animal house, specialized research labs, and fire safety systems.',
        0
    );

-- 4. Case Assignment for EVC Coordinator on Mumbai Pharma
INSERT INTO case_assignments (
    id, user_id, institution_id, assignment_type, case_reference, active, assigned_at, valid_until
) VALUES (
    '88888888-8888-8888-8888-000000000002',
    '11111111-1111-1111-1111-111111111117',
    '33333333-3333-3333-3333-333333333333',
    'EVC_VISIT',
    'CASE-MUM-2026-EVC-002',
    TRUE,
    CURRENT_TIMESTAMP - INTERVAL '5' DAY,
    CURRENT_TIMESTAMP + INTERVAL '90' DAY
);

