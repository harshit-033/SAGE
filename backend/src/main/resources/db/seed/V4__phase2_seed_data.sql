-- ==============================================================================
-- Flyway Seed Data V4: Phase 2 Detailed Institution Records and Role Seed Users
-- Loaded ONLY when spring.profiles.active includes 'dev' or 'test'
-- ==============================================================================

-- 1. Enrich existing institutions with detailed address, location, and contacts
UPDATE institutions SET
    category = 'ENGINEERING_AND_TECH',
    address_line1 = 'Plot No. 14, Institutional Area',
    address_line2 = 'Sector 3, Rohini',
    city = 'New Delhi',
    pincode = '110085',
    official_email = 'director@delhi-tech.edu',
    official_phone = '+91-11-27891234',
    alternate_phone = '+91-11-27895678',
    website_url = 'https://www.delhi-tech.edu',
    establishment_year = 1998,
    latitude = 28.7041,
    longitude = 77.1025,
    active = TRUE
WHERE id = '22222222-2222-2222-2222-222222222222';

UPDATE institutions SET
    category = 'PHARMACY',
    address_line1 = 'Central Campus, CST Road',
    address_line2 = 'Kalina, Santacruz East',
    city = 'Mumbai',
    pincode = '400098',
    official_email = 'principal@mumbai-pharma.edu',
    official_phone = '+91-22-26543210',
    alternate_phone = '+91-22-26543211',
    website_url = 'https://www.mumbai-pharma.edu',
    establishment_year = 2004,
    latitude = 19.0760,
    longitude = 72.8777,
    active = TRUE
WHERE id = '33333333-3333-3333-3333-333333333333';

UPDATE institutions SET
    category = 'POLYTECHNIC',
    address_line1 = 'AICTE Knowledge Park',
    address_line2 = 'Whitefield Main Road',
    city = 'Bengaluru',
    pincode = '560066',
    official_email = 'contact@bengaluru-polytechnic.edu',
    official_phone = '+91-80-28450123',
    alternate_phone = '+91-80-28450124',
    website_url = 'https://www.bengaluru-polytechnic.edu',
    establishment_year = 2011,
    latitude = 12.9716,
    longitude = 77.5946,
    active = TRUE
WHERE id = '44444444-4444-4444-4444-444444444444';

-- 2. Insert additional demonstration institutions for pagination and filter testing
INSERT INTO institutions (
    id, code, name, application_type, category, address_line1, address_line2,
    city, state, district, pincode, official_email, official_phone,
    website_url, establishment_year, latitude, longitude, status, active
) VALUES
    (
        '55555555-5555-5555-5555-555555555555',
        '1-500778899',
        'Pune National Institute of Management & Research',
        'MANAGEMENT',
        'MANAGEMENT',
        'Survey No. 42, Hadaspar Industrial Area',
        'Near Magarpatta City',
        'Pune',
        'Maharashtra',
        'Pune',
        '411028',
        'dean@punenimr.ac.in',
        '+91-20-26890011',
        'https://www.punenimr.ac.in',
        2008,
        18.5204,
        73.8567,
        'ACTIVE',
        TRUE
    ),
    (
        '66666666-6666-6666-6666-666666666666',
        '1-500332211',
        'Chennai School of Architecture & Urban Planning',
        'ARCHITECTURE',
        'ARCHITECTURE',
        'Old Mahabalipuram Road (IT Corridor)',
        'Sholinganallur',
        'Chennai',
        'Tamil Nadu',
        'Chennai',
        '600119',
        'registrar@csaup.edu.in',
        '+91-44-24501122',
        'https://www.csaup.edu.in',
        2015,
        13.0827,
        80.2707,
        'ACTIVE',
        TRUE
    );

-- 3. Add Demonstration Role Seed Users (BCrypt hash for 'Password123!': $2a$10$9Zj422FbkPlFPBveLlulK.oJ5o4nuF6aownRmGmFFGad97I2fQ/Vi)
INSERT INTO users (id, username, email, password_hash, full_name, active) VALUES
    ('11111111-1111-1111-1111-111111111115', 'compliance@aicte.gov.in', 'compliance@aicte.gov.in', '$2a$10$9Zj422FbkPlFPBveLlulK.oJ5o4nuF6aownRmGmFFGad97I2fQ/Vi', 'Shri Rajesh Verma (Compliance Officer)', TRUE),
    ('11111111-1111-1111-1111-111111111116', 'auditor@aicte.gov.in', 'auditor@aicte.gov.in', '$2a$10$9Zj422FbkPlFPBveLlulK.oJ5o4nuF6aownRmGmFFGad97I2fQ/Vi', 'Smt. Priya Nair (Auditor & Reviewer)', TRUE),
    ('11111111-1111-1111-1111-111111111117', 'evc.coord@aicte.gov.in', 'evc.coord@aicte.gov.in', '$2a$10$9Zj422FbkPlFPBveLlulK.oJ5o4nuF6aownRmGmFFGad97I2fQ/Vi', 'Dr. Anand Kulkarni (EVC Coordinator)', TRUE);

-- 4. Bind User Roles
INSERT INTO user_roles (user_id, role_id) VALUES
    ('11111111-1111-1111-1111-111111111115', '00000000-0000-0000-0000-000000000008'), -- compliance -> ROLE_COMPLIANCE_OFFICER
    ('11111111-1111-1111-1111-111111111116', '00000000-0000-0000-0000-000000000009'), -- auditor -> ROLE_AUDITOR
    ('11111111-1111-1111-1111-111111111117', '00000000-0000-0000-0000-000000000006'); -- evc.coord -> ROLE_EVC_COORDINATOR
