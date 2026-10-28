-- ==============================================================================
-- Flyway Migration V3: Phase 2 Institution Details, Normalized Roles & Permissions
-- Compatible with PostgreSQL and H2 (PostgreSQL mode)
-- ==============================================================================

-- 1. Extend Institutions table with detailed profile, location and contact fields
ALTER TABLE institutions ADD COLUMN category VARCHAR(64) DEFAULT 'ENGINEERING_AND_TECH';
ALTER TABLE institutions ADD COLUMN address_line1 VARCHAR(255);
ALTER TABLE institutions ADD COLUMN address_line2 VARCHAR(255);
ALTER TABLE institutions ADD COLUMN city VARCHAR(100);
ALTER TABLE institutions ADD COLUMN pincode VARCHAR(10);
ALTER TABLE institutions ADD COLUMN official_email VARCHAR(128);
ALTER TABLE institutions ADD COLUMN official_phone VARCHAR(20);
ALTER TABLE institutions ADD COLUMN alternate_phone VARCHAR(20);
ALTER TABLE institutions ADD COLUMN website_url VARCHAR(255);
ALTER TABLE institutions ADD COLUMN establishment_year INT;
ALTER TABLE institutions ADD COLUMN latitude DOUBLE PRECISION;
ALTER TABLE institutions ADD COLUMN longitude DOUBLE PRECISION;
ALTER TABLE institutions ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE;

CREATE INDEX idx_institutions_category ON institutions(category);
CREATE INDEX idx_institutions_city ON institutions(city);
CREATE INDEX idx_institutions_active ON institutions(active);

-- 2. Add Phase 2 Specific Roles
INSERT INTO roles (id, code, name, description) VALUES
    ('00000000-0000-0000-0000-000000000006', 'ROLE_EVC_COORDINATOR', 'EVC Coordinator', 'Expert Visit Committee allocation and scheduling manager'),
    ('00000000-0000-0000-0000-000000000007', 'ROLE_COMMITTEE_MEMBER', 'Standing Appellate / Hearing Committee Member', 'High-level regulatory decision maker and appeal reviewer'),
    ('00000000-0000-0000-0000-000000000008', 'ROLE_COMPLIANCE_OFFICER', 'Compliance Officer', 'Continuous faculty, cadre, and infrastructure compliance monitor'),
    ('00000000-0000-0000-0000-000000000009', 'ROLE_AUDITOR', 'Regulatory Reviewer / Auditor', 'Read-only compliance and audit log reviewer');

-- 3. Add Phase 2 Specific Granular Permissions
INSERT INTO permissions (id, code, description) VALUES
    ('00000000-0000-0000-0000-000000000020', 'INSTITUTION_ACTIVATE', 'Activate or deactivate institution accounts'),
    ('00000000-0000-0000-0000-000000000021', 'USER_READ', 'View administrative user lists and profiles'),
    ('00000000-0000-0000-0000-000000000022', 'USER_WRITE', 'Create and modify administrative user accounts'),
    ('00000000-0000-0000-0000-000000000023', 'USER_ACTIVATE', 'Activate or suspend user accounts'),
    ('00000000-0000-0000-0000-000000000024', 'ROLE_ASSIGN', 'Assign and update security roles'),
    ('00000000-0000-0000-0000-000000000025', 'AUDIT_EXPORT', 'Export regulatory audit trails'),
    ('00000000-0000-0000-0000-000000000026', 'WORKFLOW_TRANSITION', 'Execute statutory workflow state transitions'),
    ('00000000-0000-0000-0000-000000000027', 'COMPLIANCE_VERIFY', 'Verify compliance disclosure documents'),
    ('00000000-0000-0000-0000-000000000028', 'EVC_MANAGE', 'Coordinate and assign inspection panels');

-- 4. Assign Permissions to Roles
-- Administrator has all permissions
INSERT INTO role_permissions (role_id, permission_id) VALUES
    ('00000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000020'),
    ('00000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000021'),
    ('00000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000022'),
    ('00000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000023'),
    ('00000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000024'),
    ('00000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000025'),
    ('00000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000026'),
    ('00000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000027'),
    ('00000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000028');

-- Regional Officer permissions
INSERT INTO role_permissions (role_id, permission_id) VALUES
    ('00000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000010'),
    ('00000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000021'),
    ('00000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000026');

-- EVC Coordinator permissions
INSERT INTO role_permissions (role_id, permission_id) VALUES
    ('00000000-0000-0000-0000-000000000006', '00000000-0000-0000-0000-000000000010'),
    ('00000000-0000-0000-0000-000000000006', '00000000-0000-0000-0000-000000000028');

-- Compliance Officer permissions
INSERT INTO role_permissions (role_id, permission_id) VALUES
    ('00000000-0000-0000-0000-000000000008', '00000000-0000-0000-0000-000000000010'),
    ('00000000-0000-0000-0000-000000000008', '00000000-0000-0000-0000-000000000027');

-- Auditor permissions
INSERT INTO role_permissions (role_id, permission_id) VALUES
    ('00000000-0000-0000-0000-000000000009', '00000000-0000-0000-0000-000000000010'),
    ('00000000-0000-0000-0000-000000000009', '00000000-0000-0000-0000-000000000015'),
    ('00000000-0000-0000-0000-000000000009', '00000000-0000-0000-0000-000000000025');

-- Institution Representative permissions (can update institution profile)
INSERT INTO role_permissions (role_id, permission_id) VALUES
    ('00000000-0000-0000-0000-000000000005', '00000000-0000-0000-0000-000000000011');

