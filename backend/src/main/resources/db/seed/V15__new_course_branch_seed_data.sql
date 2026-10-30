-- ==============================================================================
-- Flyway Seed V15: Workflow Definitions & Scrutiny Rules for New Course/Branch
-- Compatible with PostgreSQL and H2
-- Reference: AICTE Approval Process Handbook (APH 2024-25 to 2026-27 Chapter II)
-- ==============================================================================

-- 1. Workflow Definitions for New Course and New Branch
INSERT INTO workflow_definitions (id, code, category, name, version, aph_reference, requires_evc, requires_shc, requires_sac, is_active)
VALUES
    (
        'aaaaaaaa-aaaa-aaaa-aaaa-000000000004',
        'WF_NEW_COURSE_INTRODUCTION',
        'NEW_COURSE_INTRODUCTION',
        'Introduction of New Course / Discipline in Existing Institutions',
        'APH_2024_27_V1',
        'Chapter II, Approval Process Handbook 2024-27 Clause 2.4',
        TRUE,
        TRUE,
        TRUE,
        TRUE
    ),
    (
        'aaaaaaaa-aaaa-aaaa-aaaa-000000000005',
        'WF_NEW_BRANCH_INTRODUCTION',
        'NEW_BRANCH_INTRODUCTION',
        'Introduction of New Branch / Specialization under Approved Course',
        'APH_2024_27_V1',
        'Chapter II, Approval Process Handbook 2024-27 Clause 2.5',
        FALSE,
        TRUE,
        TRUE,
        TRUE
    );

-- 2. Scrutiny Rules for New Course Introduction & New Branch
INSERT INTO scrutiny_rules (id, version, name, description, aph_clause, aph_page_number, application_category, severity, is_active)
VALUES
    (
        'RULE_APH_COURSE_FACULTY_RATIO',
        'APH_2024_27_V1',
        'Course Faculty Cadre & Student-to-Faculty Ratio',
        'Verifies that adequate qualified faculty (Professor, Associate Professor, Assistant Professor) are proposed meeting mandatory 1:15 / 1:20 cadre ratio.',
        'APH 2024-27 Clause 2.4.2 & Chapter VII',
        64,
        'NEW_COURSE_INTRODUCTION',
        'CRITICAL_DEFICIENCY',
        TRUE
    ),
    (
        'RULE_APH_COURSE_LABORATORY',
        'APH_2024_27_V1',
        'Specialized Laboratory & Equipment Adequacy',
        'Verifies dedicated specialized laboratories and essential modern equipment are allocated for the proposed new course curriculum.',
        'APH 2024-27 Clause 2.4.3 & Annexure 4',
        112,
        'NEW_COURSE_INTRODUCTION',
        'CRITICAL_DEFICIENCY',
        TRUE
    ),
    (
        'RULE_APH_COURSE_CURRICULUM_AFFILIATION',
        'APH_2024_27_V1',
        'Affiliating University NOC & Curriculum Approval',
        'Verifies that affiliating University No Objection Certificate (NOC) and approved syllabus scheme are submitted.',
        'APH 2024-27 Clause 2.4.4',
        68,
        'NEW_COURSE_INTRODUCTION',
        'MAJOR_DEFICIENCY',
        TRUE
    ),
    (
        'RULE_APH_BRANCH_INTAKE_CAP',
        'APH_2024_27_V1',
        'Proposed Branch Intake Campus Capacity Threshold',
        'Verifies that the proposed specialization intake does not breach aggregate institutional built-up and infrastructure caps.',
        'APH 2024-27 Clause 2.5.1',
        72,
        'NEW_BRANCH_INTRODUCTION',
        'CRITICAL_DEFICIENCY',
        TRUE
    ),
    (
        'RULE_APH_BRANCH_CORE_FACULTY',
        'APH_2024_27_V1',
        'Specialization Core Faculty Availability',
        'Verifies that dedicated specialized subject matter faculty are assigned to the proposed specialization.',
        'APH 2024-27 Clause 2.5.2',
        74,
        'NEW_BRANCH_INTRODUCTION',
        'MAJOR_DEFICIENCY',
        TRUE
    );
