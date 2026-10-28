-- ==============================================================================
-- Flyway Seed Data V2: Non-Production Synthetic Development Seed Data
-- Loaded ONLY when spring.profiles.active includes 'dev' or 'test'
-- ==============================================================================

-- 1. Insert Core Roles
INSERT INTO roles (id, code, name, description) VALUES
    ('00000000-0000-0000-0000-000000000001', 'ROLE_AICTE_ADMIN', 'AICTE Central Administrator', 'Full regulatory administrative authority'),
    ('00000000-0000-0000-0000-000000000002', 'ROLE_REGIONAL_OFFICER', 'AICTE Regional Officer', 'Regional jurisdiction approval oversight'),
    ('00000000-0000-0000-0000-000000000003', 'ROLE_SCRUTINY_OFFICER', 'Scrutiny Officer / Expert', 'Technical document and deficiency evaluation'),
    ('00000000-0000-0000-0000-000000000004', 'ROLE_EVC_MEMBER', 'EVC Committee Member', 'Physical and virtual inspection verification'),
    ('00000000-0000-0000-0000-000000000005', 'ROLE_INSTITUTION_OFFICER', 'Institution Authorized Signatory', 'College institutional regulatory representative');

-- 2. Insert Core Permissions
INSERT INTO permissions (id, code, description) VALUES
    ('00000000-0000-0000-0000-000000000010', 'INSTITUTION_READ', 'Read institution profiles'),
    ('00000000-0000-0000-0000-000000000011', 'INSTITUTION_WRITE', 'Modify institution profiles'),
    ('00000000-0000-0000-0000-000000000012', 'APPLICATION_READ', 'Read approval applications'),
    ('00000000-0000-0000-0000-000000000013', 'APPLICATION_SUBMIT', 'Submit approval applications'),
    ('00000000-0000-0000-0000-000000000014', 'SCRUTINY_EVALUATE', 'Conduct scrutiny evaluation'),
    ('00000000-0000-0000-0000-000000000015', 'AUDIT_READ', 'Read compliance audit logs');

-- 3. Bind Role-Permissions
INSERT INTO role_permissions (role_id, permission_id) VALUES
    ('00000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000010'),
    ('00000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000011'),
    ('00000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000012'),
    ('00000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000015'),
    ('00000000-0000-0000-0000-000000000005', '00000000-0000-0000-0000-000000000010'),
    ('00000000-0000-0000-0000-000000000005', '00000000-0000-0000-0000-000000000013');

-- 4. Insert Demonstration Institutions
INSERT INTO institutions (id, code, name, application_type, state, district, status) VALUES
    ('22222222-2222-2222-2222-222222222222', '1-500123498', 'Delhi Institute of Advanced Engineering & Technology', 'ENGINEERING_AND_TECH', 'Delhi', 'New Delhi', 'ACTIVE'),
    ('33333333-3333-3333-3333-333333333333', '1-500987654', 'Mumbai Academy of Pharmaceutical Sciences', 'PHARMACY', 'Maharashtra', 'Mumbai Suburban', 'ACTIVE'),
    ('44444444-4444-4444-4444-444444444444', '1-500456123', 'Bengaluru Polytechnic Institute', 'POLYTECHNIC', 'Karnataka', 'Bengaluru Urban', 'ACTIVE');

-- 5. Insert Synthetic Users (BCrypt hash for 'Password123!': $2a$10$9Zj422FbkPlFPBveLlulK.oJ5o4nuF6aownRmGmFFGad97I2fQ/Vi)
INSERT INTO users (id, username, email, password_hash, full_name, active) VALUES
    ('11111111-1111-1111-1111-111111111111', 'admin@aicte.gov.in', 'admin@aicte.gov.in', '$2a$10$9Zj422FbkPlFPBveLlulK.oJ5o4nuF6aownRmGmFFGad97I2fQ/Vi', 'AICTE Central Administrator', TRUE),
    ('11111111-1111-1111-1111-111111111112', 'regional@aicte.gov.in', 'regional@aicte.gov.in', '$2a$10$9Zj422FbkPlFPBveLlulK.oJ5o4nuF6aownRmGmFFGad97I2fQ/Vi', 'Northern Regional Officer', TRUE),
    ('11111111-1111-1111-1111-111111111113', 'officer@delhi-tech.edu', 'officer@delhi-tech.edu', '$2a$10$9Zj422FbkPlFPBveLlulK.oJ5o4nuF6aownRmGmFFGad97I2fQ/Vi', 'Prof. Ramesh Sharma (Delhi Tech)', TRUE),
    ('11111111-1111-1111-1111-111111111114', 'officer@mumbai-engg.edu', 'officer@mumbai-engg.edu', '$2a$10$9Zj422FbkPlFPBveLlulK.oJ5o4nuF6aownRmGmFFGad97I2fQ/Vi', 'Dr. Sunita Patil (Mumbai Pharma)', TRUE);

-- 6. Bind User Roles
INSERT INTO user_roles (user_id, role_id) VALUES
    ('11111111-1111-1111-1111-111111111111', '00000000-0000-0000-0000-000000000001'), -- admin -> ROLE_AICTE_ADMIN
    ('11111111-1111-1111-1111-111111111112', '00000000-0000-0000-0000-000000000002'), -- regional -> ROLE_REGIONAL_OFFICER
    ('11111111-1111-1111-1111-111111111113', '00000000-0000-0000-0000-000000000005'), -- delhi officer -> ROLE_INSTITUTION_OFFICER
    ('11111111-1111-1111-1111-111111111114', '00000000-0000-0000-0000-000000000005'); -- mumbai officer -> ROLE_INSTITUTION_OFFICER

-- 7. Bind Institutional Tenancy
INSERT INTO institution_users (id, institution_id, user_id, role_in_institution, is_primary_contact) VALUES
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '22222222-2222-2222-2222-222222222222', '11111111-1111-1111-1111-111111111113', 'HEAD_OF_INSTITUTION', TRUE),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '33333333-3333-3333-3333-333333333333', '11111111-1111-1111-1111-111111111114', 'HEAD_OF_INSTITUTION', TRUE);
