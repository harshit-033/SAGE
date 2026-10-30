#!/usr/bin/env python3
"""
SAGE — Comprehensive Relational Demonstration Database Generator
Generates:
1. backend/src/main/resources/db/seed/V16__comprehensive_relational_demo_seed.sql
2. Safe fictional demonstration PDF documents in backend/data/documents/
"""

import os
import hashlib
import uuid
from datetime import datetime, timedelta

ROOT_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
SEED_SQL_PATH = os.path.join(ROOT_DIR, "backend/src/main/resources/db/seed/V16__comprehensive_relational_demo_seed.sql")
DOCS_DIR = os.path.join(ROOT_DIR, "backend/data/documents")

os.makedirs(DOCS_DIR, exist_ok=True)
os.makedirs(os.path.dirname(SEED_SQL_PATH), exist_ok=True)

# Standard BCrypt hash for 'Password123!'
BCRYPT_HASH = "$2a$10$9Zj422FbkPlFPBveLlulK.oJ5o4nuF6aownRmGmFFGad97I2fQ/Vi"

def create_safe_pdf(filename, title, cert_id, inst_name, body_text):
    content = f"""%PDF-1.4
1 0 obj << /Type /Catalog /Pages 2 0 R >> endobj
2 0 obj << /Type /Pages /Kids [3 0 R] /Count 1 >> endobj
3 0 obj << /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Contents 4 0 R /Resources << /Font << /F1 5 0 R >> >> >> endobj
4 0 obj << /Length 380 >> stream
BT
/F1 16 Tf
50 730 Td
({title}) Tj
/F1 10 Tf
0 -25 Td
(Certificate Ref: {cert_id}) Tj
0 -20 Td
(Institution: {inst_name}) Tj
0 -25 Td
(STATUTORY NOTICE: Independent AICTE SAGE Demonstration Record - Fictional Prototype) Tj
0 -20 Td
({body_text[:90]}) Tj
0 -20 Td
({body_text[90:180] if len(body_text) > 90 else ""}) Tj
0 -30 Td
(Verified for AICTE Approval Process Handbook (APH 2024-27) Scrutiny Simulation) Tj
ET
endstream
endobj
5 0 obj << /Type /Font /Subtype /Type1 /BaseFont /Helvetica >> endobj
xref
0 6
0000000000 65535 f 
0000000009 00000 n 
0000000058 00000 n 
0000000115 00000 n 
0000000244 00000 n 
0000000675 00000 n 
trailer << /Size 6 /Root 1 0 R >>
startxref
745
%%EOF"""
    b = content.encode('utf-8')
    sha256 = hashlib.sha256(b).hexdigest()
    file_path = os.path.join(DOCS_DIR, filename)
    with open(file_path, 'wb') as f:
        f.write(b)
    return sha256, len(b)

sql_lines = []
def emit(line=""):
    sql_lines.append(line)

emit("-- ==============================================================================")
emit("-- Flyway Seed V16: Comprehensive Relational Demonstration Dataset for SAGE")
emit("-- Compatible with PostgreSQL 16+ and H2 (MODE=PostgreSQL)")
emit("-- Strictly aligned with AICTE Approval Process Handbook (APH 2024-27)")
emit("-- ==============================================================================")
emit("")

# 1. Standard Workflow Definitions
emit("-- 1. Standard Workflow Definitions for All Supported Categories")
emit("INSERT INTO workflow_definitions (id, code, category, name, version, aph_reference, requires_evc, requires_shc, requires_sac, is_active)")
emit("VALUES")
emit("    ('aaaaaaaa-aaaa-aaaa-aaaa-000000000006', 'WF_NEW_PROGRAMME', 'NEW_PROGRAMME', 'Grant of Approval for Introduction of New Degree Programme', 'APH_2024_27_V1', 'Chapter II, APH 2024-27 Clause 2.3', TRUE, TRUE, TRUE, TRUE),")
emit("    ('aaaaaaaa-aaaa-aaaa-aaaa-000000000007', 'WF_CLOSURE', 'CLOSURE', 'Statutory Closure of Technical Course or Institution', 'APH_2024_27_V1', 'Chapter II, APH 2024-27 Clause 2.11', FALSE, TRUE, FALSE, TRUE),")
emit("    ('aaaaaaaa-aaaa-aaaa-aaaa-000000000008', 'WF_CHANGE_OF_SITE', 'CHANGE_OF_SITE', 'Statutory Change of Site and Permanent Campus Location', 'APH_2024_27_V1', 'Chapter II, APH 2024-27 Clause 2.8', TRUE, TRUE, FALSE, TRUE);")
emit("")

# 2. Demonstration Institutions
emit("-- 2. Multi-Regional Technical Institutions (Fictional Demonstration Profiles)")
emit("INSERT INTO institutions (")
emit("    id, code, name, application_type, category, address_line1, address_line2,")
emit("    city, state, district, pincode, official_email, official_phone,")
emit("    website_url, establishment_year, latitude, longitude, status, active")
emit(") VALUES")
emit("    (")
emit("        '77777777-7777-7777-7777-222222222206', '1-500667788', 'Central Institute of Computing & Emerging Tech',")
emit("        'ENGINEERING_AND_TECH', 'ENGINEERING_AND_TECH', 'Bhadbhada Road, Suraj Nagar', 'Near IISER Campus',")
emit("        'Bhopal', 'Madhya Pradesh', 'Bhopal', '462044', 'director@bhopal-comp.edu', '+91-755-2678901',")
emit("        'https://www.bhopal-comp.edu', 2012, 23.2599, 77.4126, 'ACTIVE', TRUE")
emit("    ),")
emit("    (")
emit("        '77777777-7777-7777-7777-222222222207', '1-500889900', 'Rajasthan College of Engineering & Applied Science',")
emit("        'ENGINEERING_AND_TECH', 'ENGINEERING_AND_TECH', 'RIICO Institutional Area, Sitapura', 'Tonk Road',")
emit("        'Jaipur', 'Rajasthan', 'Jaipur', '302022', 'principal@jaipur-engg.edu', '+91-141-2770123',")
emit("        'https://www.jaipur-engg.edu', 2006, 26.9124, 75.7873, 'ACTIVE', TRUE")
emit("    ),")
emit("    (")
emit("        '77777777-7777-7777-7777-222222222208', '1-500112233', 'Himalayan Institute of Technology',")
emit("        'ENGINEERING_AND_TECH', 'ENGINEERING_AND_TECH', 'Chakrata Road, Selaqui Industrial Area', 'Vikasnagar Tehsil',")
emit("        'Dehradun', 'Uttarakhand', 'Dehradun', '248011', 'registrar@dehradun-hit.edu', '+91-135-2698100',")
emit("        'https://www.dehradun-hit.edu', 2010, 30.3165, 78.0322, 'ACTIVE', TRUE")
emit("    ),")
emit("    (")
emit("        '77777777-7777-7777-7777-222222222209', '1-500445566', 'Coastal Engineering Institute',")
emit("        'ENGINEERING_AND_TECH', 'ENGINEERING_AND_TECH', 'IT Park Road, Madhurawada', 'Rishikonda Hills',")
emit("        'Visakhapatnam', 'Andhra Pradesh', 'Visakhapatnam', '530048', 'principal@vizag-coast.edu', '+91-891-2856400',")
emit("        'https://www.vizag-coast.edu', 2005, 17.6868, 83.2185, 'ACTIVE', TRUE")
emit("    ),")
emit("    (")
emit("        '77777777-7777-7777-7777-222222222210', '1-500556677', 'Deccan Institute of Technology & AI Research',")
emit("        'ENGINEERING_AND_TECH', 'ENGINEERING_AND_TECH', 'Financial District, Nanakramguda', 'Gachibowli Outer Ring Road',")
emit("        'Hyderabad', 'Telangana', 'Hyderabad', '500032', 'dean@hyderabad-dit.edu', '+91-40-23007890',")
emit("        'https://www.hyderabad-dit.edu', 2014, 17.3850, 78.4867, 'ACTIVE', TRUE")
emit("    ),")
emit("    (")
emit("        '77777777-7777-7777-7777-222222222211', '1-500223344', 'Greenfield College of Engineering & Polytechnic',")
emit("        'ENGINEERING_AND_TECH', 'ENGINEERING_AND_TECH', 'Sector 88, Pataudi Road Knowledge Zone', 'Near Dwarka Expressway',")
emit("        'Gurugram', 'Haryana', 'Gurugram', '122505', 'director@gurugram-gce.edu', '+91-124-2890555',")
emit("        'https://www.gurugram-gce.edu', 2017, 28.4595, 77.0266, 'ACTIVE', TRUE")
emit("    ),")
emit("    (")
emit("        '77777777-7777-7777-7777-222222222212', '1-500334455', 'Eastern School of Technology & Applied Research',")
emit("        'ENGINEERING_AND_TECH', 'ENGINEERING_AND_TECH', 'Sector V, Salt Lake Electronics Complex', 'Bidhan Nagar',")
emit("        'Kolkata', 'West Bengal', 'North 24 Parganas', '700091', 'head@kolkata-tech.edu', '+91-33-23579000',")
emit("        'https://www.kolkata-tech.edu', 2002, 22.5726, 88.3639, 'ACTIVE', TRUE")
emit("    );")
emit("")

# 3. Demonstration Users & Roles
emit("-- 3. Multi-Role Demonstration Accounts (BCrypt Password: Password123!)")
emit("INSERT INTO users (id, username, email, password_hash, full_name, active) VALUES")
emit(f"    ('11111111-1111-1111-1111-200000000001', 'admin@aicte-demo.gov.in', 'admin@aicte-demo.gov.in', '{BCRYPT_HASH}', 'Dr. Surendra Prasad (Central Admin Lead)', TRUE),")
emit(f"    ('11111111-1111-1111-1111-200000000002', 'regional.north@aicte-demo.gov.in', 'regional.north@aicte-demo.gov.in', '{BCRYPT_HASH}', 'Dr. Arvind Saxena (Northern Regional Director)', TRUE),")
emit(f"    ('11111111-1111-1111-1111-200000000003', 'regional.west@aicte-demo.gov.in', 'regional.west@aicte-demo.gov.in', '{BCRYPT_HASH}', 'Dr. Vikram Deshmukh (Western Regional Director)', TRUE),")
emit(f"    ('11111111-1111-1111-1111-200000000004', 'compliance.officer1@aicte-demo.gov.in', 'compliance.officer1@aicte-demo.gov.in', '{BCRYPT_HASH}', 'Shri R. K. Mittal (Compliance Surveillance Lead)', TRUE),")
emit(f"    ('11111111-1111-1111-1111-200000000005', 'compliance.officer2@aicte-demo.gov.in', 'compliance.officer2@aicte-demo.gov.in', '{BCRYPT_HASH}', 'Smt. Ananya Sen (Regulatory Compliance Monitor)', TRUE),")
emit(f"    ('11111111-1111-1111-1111-200000000006', 'auditor@aicte-demo.gov.in', 'auditor@aicte-demo.gov.in', '{BCRYPT_HASH}', 'Shri Harish Chand (Senior Statutory Auditor)', TRUE),")
emit(f"    ('11111111-1111-1111-1111-200000000007', 'evc.coord1@aicte-demo.gov.in', 'evc.coord1@aicte-demo.gov.in', '{BCRYPT_HASH}', 'Dr. Anand Kulkarni (EVC Central Operations)', TRUE),")
emit(f"    ('11111111-1111-1111-1111-200000000008', 'evc.coord2@aicte-demo.gov.in', 'evc.coord2@aicte-demo.gov.in', '{BCRYPT_HASH}', 'Prof. Shalini Roy (EVC Panel Allocation Manager)', TRUE),")
emit(f"    ('11111111-1111-1111-1111-200000000013', 'officer@bengaluru-poly.edu', 'officer@bengaluru-poly.edu', '{BCRYPT_HASH}', 'Prof. K. Venkatesh (Bengaluru Polytechnic)', TRUE),")
emit(f"    ('11111111-1111-1111-1111-200000000014', 'officer@pune-nimr.ac.in', 'officer@pune-nimr.ac.in', '{BCRYPT_HASH}', 'Dr. Deepak Joshi (Pune NIMR)', TRUE),")
emit(f"    ('11111111-1111-1111-1111-200000000015', 'officer@csaup.edu.in', 'officer@csaup.edu.in', '{BCRYPT_HASH}', 'Prof. V. Soundararajan (Chennai Arch)', TRUE),")
emit(f"    ('11111111-1111-1111-1111-200000000016', 'officer@bhopal-comp.edu', 'officer@bhopal-comp.edu', '{BCRYPT_HASH}', 'Dr. Manoj Tiwari (Central Computing Bhopal)', TRUE),")
emit(f"    ('11111111-1111-1111-1111-200000000017', 'officer@jaipur-engg.edu', 'officer@jaipur-engg.edu', '{BCRYPT_HASH}', 'Dr. Rajendra Shekhawat (Rajasthan College)', TRUE),")
emit(f"    ('11111111-1111-1111-1111-200000000018', 'officer@dehradun-hit.edu', 'officer@dehradun-hit.edu', '{BCRYPT_HASH}', 'Prof. Alok Rawat (Himalayan Institute)', TRUE),")
emit(f"    ('11111111-1111-1111-1111-200000000019', 'officer@vizag-coast.edu', 'officer@vizag-coast.edu', '{BCRYPT_HASH}', 'Dr. S. R. Murthy (Coastal Engineering)', TRUE),")
emit(f"    ('11111111-1111-1111-1111-200000000020', 'officer@hyderabad-dit.edu', 'officer@hyderabad-dit.edu', '{BCRYPT_HASH}', 'Dr. P. V. Rao (Deccan Institute Hyderabad)', TRUE),")
emit(f"    ('11111111-1111-1111-1111-200000000021', 'officer@gurugram-gce.edu', 'officer@gurugram-gce.edu', '{BCRYPT_HASH}', 'Prof. Kuldeep Yadav (Greenfield College Gurugram)', TRUE),")
emit(f"    ('11111111-1111-1111-1111-200000000022', 'officer@kolkata-tech.edu', 'officer@kolkata-tech.edu', '{BCRYPT_HASH}', 'Dr. Subrata Mukherjee (Eastern School Kolkata)', TRUE);")
emit("")

# 4. Role Bindings
emit("-- 4. Assign Security Roles")
emit("INSERT INTO user_roles (user_id, role_id) VALUES")
emit("    ('11111111-1111-1111-1111-200000000001', '00000000-0000-0000-0000-000000000001'), -- admin")
emit("    ('11111111-1111-1111-1111-200000000002', '00000000-0000-0000-0000-000000000002'), -- regional.north")
emit("    ('11111111-1111-1111-1111-200000000003', '00000000-0000-0000-0000-000000000002'), -- regional.west")
emit("    ('11111111-1111-1111-1111-200000000004', '00000000-0000-0000-0000-000000000008'), -- compliance 1")
emit("    ('11111111-1111-1111-1111-200000000005', '00000000-0000-0000-0000-000000000008'), -- compliance 2")
emit("    ('11111111-1111-1111-1111-200000000006', '00000000-0000-0000-0000-000000000009'), -- auditor")
emit("    ('11111111-1111-1111-1111-200000000007', '00000000-0000-0000-0000-000000000006'), -- evc.coord 1")
emit("    ('11111111-1111-1111-1111-200000000008', '00000000-0000-0000-0000-000000000006'), -- evc.coord 2")
for i in range(13, 23):
    term = ";" if i == 22 else ","
    emit(f"    ('11111111-1111-1111-1111-2000000000{i:02d}', '00000000-0000-0000-0000-000000000005'){term}")
emit("")

# 5. Institutional Tenancy Affiliations
emit("-- 5. Institutional Tenancy Affiliations")
inst_map = [
    ('44444444-4444-4444-4444-444444444444', '11111111-1111-1111-1111-200000000013'),
    ('55555555-5555-5555-5555-555555555555', '11111111-1111-1111-1111-200000000014'),
    ('66666666-6666-6666-6666-666666666666', '11111111-1111-1111-1111-200000000015'),
    ('77777777-7777-7777-7777-222222222206', '11111111-1111-1111-1111-200000000016'),
    ('77777777-7777-7777-7777-222222222207', '11111111-1111-1111-1111-200000000017'),
    ('77777777-7777-7777-7777-222222222208', '11111111-1111-1111-1111-200000000018'),
    ('77777777-7777-7777-7777-222222222209', '11111111-1111-1111-1111-200000000019'),
    ('77777777-7777-7777-7777-222222222210', '11111111-1111-1111-1111-200000000020'),
    ('77777777-7777-7777-7777-222222222211', '11111111-1111-1111-1111-200000000021'),
    ('77777777-7777-7777-7777-222222222212', '11111111-1111-1111-1111-200000000022'),
]
emit("INSERT INTO institution_users (id, institution_id, user_id, role_in_institution, is_primary_contact) VALUES")
for idx, (inst_id, u_id) in enumerate(inst_map):
    affil_id = f"cccccccc-0000-0000-0000-0000000000{idx+1:02d}"
    term = ";" if idx == len(inst_map) - 1 else ","
    emit(f"    ('{affil_id}', '{inst_id}', '{u_id}', 'HEAD_OF_INSTITUTION', TRUE){term}")
emit("")

# 6. Regional Jurisdiction Governance
emit("-- 6. Regional Jurisdiction Governance")
emit("INSERT INTO jurisdiction_assignments (id, user_id, region, state, active, valid_from, valid_until) VALUES")
north_states = ['Delhi', 'Uttar Pradesh', 'Haryana', 'Punjab', 'Uttarakhand']
for idx, st in enumerate(north_states):
    jid = f"77777777-7777-7777-8888-0000000000{idx+1:02d}"
    emit(f"    ('{jid}', '11111111-1111-1111-1111-200000000002', 'NORTHERN', '{st}', TRUE, CURRENT_TIMESTAMP - INTERVAL '30' DAY, CURRENT_TIMESTAMP + INTERVAL '365' DAY),")
    if st != 'Delhi':
        jid_mirror = f"77777777-7777-7777-8888-1000000000{idx+1:02d}"
        emit(f"    ('{jid_mirror}', '11111111-1111-1111-1111-111111111112', 'NORTHERN', '{st}', TRUE, CURRENT_TIMESTAMP - INTERVAL '30' DAY, CURRENT_TIMESTAMP + INTERVAL '365' DAY),")

west_states = ['Maharashtra', 'Gujarat', 'Rajasthan', 'Goa']
for idx, st in enumerate(west_states):
    jid = f"77777777-7777-7777-9999-0000000000{idx+1:02d}"
    term = ";" if idx == len(west_states) - 1 else ","
    emit(f"    ('{jid}', '11111111-1111-1111-1111-200000000003', 'WESTERN', '{st}', TRUE, CURRENT_TIMESTAMP - INTERVAL '30' DAY, CURRENT_TIMESTAMP + INTERVAL '365' DAY){term}")
emit("")

# 7. Case Assignments
emit("-- 7. Case Assignments (Surveillance, Scrutiny, and Audit Oversight)")
emit("INSERT INTO case_assignments (id, user_id, institution_id, assignment_type, case_reference, active, assigned_at, valid_until) VALUES")
case_list = [
    ('11111111-1111-1111-1111-200000000004', '77777777-7777-7777-7777-222222222208', 'COMPLIANCE_MONITORING', 'CASE-HIT-2026-COMPL-001'),
    ('11111111-1111-1111-1111-200000000004', '77777777-7777-7777-7777-222222222211', 'COMPLIANCE_MONITORING', 'CASE-GCE-2026-COMPL-001'),
    ('11111111-1111-1111-1111-200000000004', '22222222-2222-2222-2222-222222222222', 'COMPLIANCE_MONITORING', 'CASE-DEL-2026-COMPL-002'),
    ('11111111-1111-1111-1111-200000000005', '33333333-3333-3333-3333-333333333333', 'COMPLIANCE_MONITORING', 'CASE-MUM-2026-COMPL-001'),
    ('11111111-1111-1111-1111-200000000005', '55555555-5555-5555-5555-555555555555', 'COMPLIANCE_MONITORING', 'CASE-PUN-2026-COMPL-001'),
    ('11111111-1111-1111-1111-200000000005', '77777777-7777-7777-7777-222222222206', 'COMPLIANCE_MONITORING', 'CASE-BHO-2026-COMPL-001'),
    ('11111111-1111-1111-1111-200000000006', '33333333-3333-3333-3333-333333333333', 'AUDIT_REVIEW', 'CASE-MUM-2026-AUDIT-001'),
    ('11111111-1111-1111-1111-200000000006', '77777777-7777-7777-7777-222222222207', 'AUDIT_REVIEW', 'CASE-JAI-2026-AUDIT-001'),
    ('11111111-1111-1111-1111-200000000006', '77777777-7777-7777-7777-222222222212', 'AUDIT_REVIEW', 'CASE-KOL-2026-AUDIT-001'),
    ('11111111-1111-1111-1111-200000000007', '77777777-7777-7777-7777-222222222211', 'EVC_VISIT', 'CASE-GCE-2026-EVC-001'),
    ('11111111-1111-1111-1111-200000000007', '77777777-7777-7777-7777-222222222208', 'EVC_VISIT', 'CASE-HIT-2026-EVC-001'),
    ('11111111-1111-1111-1111-200000000008', '77777777-7777-7777-7777-222222222207', 'EVC_VISIT', 'CASE-JAI-2026-EVC-001'),
]
for idx, (u_id, inst_id, a_type, ref) in enumerate(case_list):
    cid = f"88888888-8888-8888-0000-0000000000{idx+1:02d}"
    term = ";" if idx == len(case_list) - 1 else ","
    emit(f"    ('{cid}', '{u_id}', '{inst_id}', '{a_type}', '{ref}', TRUE, CURRENT_TIMESTAMP - INTERVAL '15' DAY, CURRENT_TIMESTAMP + INTERVAL '180' DAY){term}")
emit("")

# 8. Applications (35 records)
emit("-- 8. Statutory Applications across Supported Categories and Workflow States")
# Let's define the applications
applications_data = [
    # (app_id, number, inst_id, year, category, wf_id, state, assigned_user, created_by, submitted_offset, decision_offset)
    # Inst 1: Delhi Tech
    ('bbbbbbbb-0001-0000-0000-000000000101', 'APP-2025-DEL-0101', '22222222-2222-2222-2222-222222222222', '2025-26', 'EXTENSION_OF_APPROVAL', 'aaaaaaaa-aaaa-aaaa-aaaa-000000000002', 'SUBMITTED', '11111111-1111-1111-1111-200000000002', '11111111-1111-1111-1111-111111111113', 2, None),
    ('bbbbbbbb-0001-0000-0000-000000000102', 'APP-2025-DEL-0102', '22222222-2222-2222-2222-222222222222', '2025-26', 'NEW_COURSE_INTRODUCTION', 'aaaaaaaa-aaaa-aaaa-aaaa-000000000004', 'UNDER_SCRUTINY', '11111111-1111-1111-1111-200000000002', '11111111-1111-1111-1111-111111111113', 5, None),
    ('bbbbbbbb-0001-0000-0000-000000000103', 'APP-2025-DEL-0103', '22222222-2222-2222-2222-222222222222', '2025-26', 'NEW_BRANCH_INTRODUCTION', 'aaaaaaaa-aaaa-aaaa-aaaa-000000000005', 'DEFICIENT', '11111111-1111-1111-1111-200000000002', '11111111-1111-1111-1111-111111111113', 8, None),

    # Inst 2: Mumbai Academy
    ('bbbbbbbb-0002-0000-0000-000000000201', 'APP-2025-MUM-0201', '33333333-3333-3333-3333-333333333333', '2025-26', 'EXTENSION_OF_APPROVAL', 'aaaaaaaa-aaaa-aaaa-aaaa-000000000002', 'APPROVED', '11111111-1111-1111-1111-200000000003', '11111111-1111-1111-1111-111111111114', 15, 3),
    ('bbbbbbbb-0002-0000-0000-000000000202', 'APP-2025-MUM-0202', '33333333-3333-3333-3333-333333333333', '2025-26', 'INCREASE_IN_INTAKE', 'aaaaaaaa-aaaa-aaaa-aaaa-000000000003', 'DEFICIENT', '11111111-1111-1111-1111-200000000003', '11111111-1111-1111-1111-111111111114', 10, None),
    ('bbbbbbbb-0002-0000-0000-000000000203', 'APP-2025-MUM-0203', '33333333-3333-3333-3333-333333333333', '2025-26', 'NEW_COURSE_INTRODUCTION', 'aaaaaaaa-aaaa-aaaa-aaaa-000000000004', 'RESUBMITTED', '11111111-1111-1111-1111-200000000003', '11111111-1111-1111-1111-111111111114', 12, None),

    # Inst 3: Bengaluru Poly
    ('bbbbbbbb-0003-0000-0000-000000000301', 'APP-2025-BEN-0301', '44444444-4444-4444-4444-444444444444', '2025-26', 'EXTENSION_OF_APPROVAL', 'aaaaaaaa-aaaa-aaaa-aaaa-000000000002', 'APPROVED', None, '11111111-1111-1111-1111-200000000013', 20, 5),
    ('bbbbbbbb-0003-0000-0000-000000000302', 'APP-2025-BEN-0302', '44444444-4444-4444-4444-444444444444', '2025-26', 'NEW_PROGRAMME', 'aaaaaaaa-aaaa-aaaa-aaaa-000000000006', 'UNDER_SCRUTINY', None, '11111111-1111-1111-1111-200000000013', 6, None),
    ('bbbbbbbb-0003-0000-0000-000000000303', 'APP-2025-BEN-0303', '44444444-4444-4444-4444-444444444444', '2025-26', 'INCREASE_IN_INTAKE', 'aaaaaaaa-aaaa-aaaa-aaaa-000000000003', 'SUBMITTED', None, '11111111-1111-1111-1111-200000000013', 3, None),

    # Inst 4: Pune NIMR
    ('bbbbbbbb-0004-0000-0000-000000000401', 'APP-2025-PUN-0401', '55555555-5555-5555-5555-555555555555', '2025-26', 'EXTENSION_OF_APPROVAL', 'aaaaaaaa-aaaa-aaaa-aaaa-000000000002', 'SCRUTINY_PASSED', '11111111-1111-1111-1111-200000000003', '11111111-1111-1111-1111-200000000014', 14, None),
    ('bbbbbbbb-0004-0000-0000-000000000402', 'APP-2025-PUN-0402', '55555555-5555-5555-5555-555555555555', '2025-26', 'NEW_PROGRAMME', 'aaaaaaaa-aaaa-aaaa-aaaa-000000000006', 'DRAFT', None, '11111111-1111-1111-1111-200000000014', None, None),
    ('bbbbbbbb-0004-0000-0000-000000000403', 'APP-2025-PUN-0403', '55555555-5555-5555-5555-555555555555', '2025-26', 'INCREASE_IN_INTAKE', 'aaaaaaaa-aaaa-aaaa-aaaa-000000000003', 'APPROVED', '11111111-1111-1111-1111-200000000003', '11111111-1111-1111-1111-200000000014', 18, 4),

    # Inst 5: Chennai Arch
    ('bbbbbbbb-0005-0000-0000-000000000501', 'APP-2025-CHE-0501', '66666666-6666-6666-6666-666666666666', '2025-26', 'EXTENSION_OF_APPROVAL', 'aaaaaaaa-aaaa-aaaa-aaaa-000000000002', 'SUBMITTED', None, '11111111-1111-1111-1111-200000000015', 4, None),
    ('bbbbbbbb-0005-0000-0000-000000000502', 'APP-2025-CHE-0502', '66666666-6666-6666-6666-666666666666', '2025-26', 'CHANGE_OF_SITE', 'aaaaaaaa-aaaa-aaaa-aaaa-000000000008', 'EVC_SCHEDULED', '11111111-1111-1111-1111-200000000007', '11111111-1111-1111-1111-200000000015', 12, None),
    ('bbbbbbbb-0005-0000-0000-000000000503', 'APP-2025-CHE-0503', '66666666-6666-6666-6666-666666666666', '2025-26', 'INCREASE_IN_INTAKE', 'aaaaaaaa-aaaa-aaaa-aaaa-000000000003', 'DRAFT', None, '11111111-1111-1111-1111-200000000015', None, None),

    # Inst 6: Central Computing Bhopal
    ('bbbbbbbb-0006-0000-0000-000000000601', 'APP-2025-BHO-0601', '77777777-7777-7777-7777-222222222206', '2025-26', 'EXTENSION_OF_APPROVAL', 'aaaaaaaa-aaaa-aaaa-aaaa-000000000002', 'APPROVED', None, '11111111-1111-1111-1111-200000000016', 25, 7),
    ('bbbbbbbb-0006-0000-0000-000000000602', 'APP-2025-BHO-0602', '77777777-7777-7777-7777-222222222206', '2025-26', 'NEW_COURSE_INTRODUCTION', 'aaaaaaaa-aaaa-aaaa-aaaa-000000000004', 'EVC_CONDUCTED', '11111111-1111-1111-1111-200000000007', '11111111-1111-1111-1111-200000000016', 15, None),
    ('bbbbbbbb-0006-0000-0000-000000000603', 'APP-2025-BHO-0603', '77777777-7777-7777-7777-222222222206', '2025-26', 'NEW_BRANCH_INTRODUCTION', 'aaaaaaaa-aaaa-aaaa-aaaa-000000000005', 'UNDER_SCRUTINY', None, '11111111-1111-1111-1111-200000000016', 7, None),

    # Inst 7: Rajasthan College Jaipur
    ('bbbbbbbb-0007-0000-0000-000000000701', 'APP-2025-JAI-0701', '77777777-7777-7777-7777-222222222207', '2025-26', 'EXTENSION_OF_APPROVAL', 'aaaaaaaa-aaaa-aaaa-aaaa-000000000002', 'DEFICIENT', '11111111-1111-1111-1111-200000000003', '11111111-1111-1111-1111-200000000017', 9, None),
    ('bbbbbbbb-0007-0000-0000-000000000702', 'APP-2025-JAI-0702', '77777777-7777-7777-7777-222222222207', '2025-26', 'NEW_COURSE_INTRODUCTION', 'aaaaaaaa-aaaa-aaaa-aaaa-000000000004', 'SCRUTINY_PASSED', '11111111-1111-1111-1111-200000000003', '11111111-1111-1111-1111-200000000017', 16, None),
    ('bbbbbbbb-0007-0000-0000-000000000703', 'APP-2025-JAI-0703', '77777777-7777-7777-7777-222222222207', '2025-26', 'INCREASE_IN_INTAKE', 'aaaaaaaa-aaaa-aaaa-aaaa-000000000003', 'RESUBMITTED', '11111111-1111-1111-1111-200000000003', '11111111-1111-1111-1111-200000000017', 11, None),

    # Inst 8: Himalayan Institute Dehradun
    ('bbbbbbbb-0008-0000-0000-000000000801', 'APP-2025-DEH-0801', '77777777-7777-7777-7777-222222222208', '2025-26', 'NEW_INSTITUTION', 'aaaaaaaa-aaaa-aaaa-aaaa-000000000001', 'EVC_SCHEDULED', '11111111-1111-1111-1111-200000000007', '11111111-1111-1111-1111-200000000018', 14, None),
    ('bbbbbbbb-0008-0000-0000-000000000802', 'APP-2025-DEH-0802', '77777777-7777-7777-7777-222222222208', '2025-26', 'EXTENSION_OF_APPROVAL', 'aaaaaaaa-aaaa-aaaa-aaaa-000000000002', 'SUBMITTED', '11111111-1111-1111-1111-200000000002', '11111111-1111-1111-1111-200000000018', 3, None),
    ('bbbbbbbb-0008-0000-0000-000000000803', 'APP-2025-DEH-0803', '77777777-7777-7777-7777-222222222208', '2025-26', 'NEW_COURSE_INTRODUCTION', 'aaaaaaaa-aaaa-aaaa-aaaa-000000000004', 'DEFICIENT', '11111111-1111-1111-1111-200000000002', '11111111-1111-1111-1111-200000000018', 8, None),

    # Inst 9: Coastal Engineering Vizag
    ('bbbbbbbb-0009-0000-0000-000000000901', 'APP-2025-VIZ-0901', '77777777-7777-7777-7777-222222222209', '2025-26', 'EXTENSION_OF_APPROVAL', 'aaaaaaaa-aaaa-aaaa-aaaa-000000000002', 'APPROVED', None, '11111111-1111-1111-1111-200000000019', 22, 6),
    ('bbbbbbbb-0009-0000-0000-000000000902', 'APP-2025-VIZ-0902', '77777777-7777-7777-7777-222222222209', '2025-26', 'NEW_BRANCH_INTRODUCTION', 'aaaaaaaa-aaaa-aaaa-aaaa-000000000005', 'SCRUTINY_PASSED', None, '11111111-1111-1111-1111-200000000019', 13, None),
    ('bbbbbbbb-0009-0000-0000-000000000903', 'APP-2025-VIZ-0903', '77777777-7777-7777-7777-222222222209', '2025-26', 'CLOSURE', 'aaaaaaaa-aaaa-aaaa-aaaa-000000000007', 'UNDER_SCRUTINY', None, '11111111-1111-1111-1111-200000000019', 5, None),

    # Inst 10: Deccan Institute Hyderabad
    ('bbbbbbbb-0010-0000-0000-000000001001', 'APP-2025-HYD-1001', '77777777-7777-7777-7777-222222222210', '2025-26', 'EXTENSION_OF_APPROVAL', 'aaaaaaaa-aaaa-aaaa-aaaa-000000000002', 'SUBMITTED', None, '11111111-1111-1111-1111-200000000020', 4, None),
    ('bbbbbbbb-0010-0000-0000-000000001002', 'APP-2025-HYD-1002', '77777777-7777-7777-7777-222222222210', '2025-26', 'NEW_PROGRAMME', 'aaaaaaaa-aaaa-aaaa-aaaa-000000000006', 'UNDER_SCRUTINY', None, '11111111-1111-1111-1111-200000000020', 6, None),
    ('bbbbbbbb-0010-0000-0000-000000001003', 'APP-2025-HYD-1003', '77777777-7777-7777-7777-222222222210', '2025-26', 'NEW_BRANCH_INTRODUCTION', 'aaaaaaaa-aaaa-aaaa-aaaa-000000000005', 'DRAFT', None, '11111111-1111-1111-1111-200000000020', None, None),

    # Inst 11: Greenfield College Gurugram
    ('bbbbbbbb-0011-0000-0000-000000001101', 'APP-2025-GUR-1101', '77777777-7777-7777-7777-222222222211', '2025-26', 'NEW_INSTITUTION', 'aaaaaaaa-aaaa-aaaa-aaaa-000000000001', 'EVC_CONDUCTED', '11111111-1111-1111-1111-200000000007', '11111111-1111-1111-1111-200000000021', 19, None),
    ('bbbbbbbb-0011-0000-0000-000000001102', 'APP-2025-GUR-1102', '77777777-7777-7777-7777-222222222211', '2025-26', 'NEW_COURSE_INTRODUCTION', 'aaaaaaaa-aaaa-aaaa-aaaa-000000000004', 'RESUBMITTED', '11111111-1111-1111-1111-200000000002', '11111111-1111-1111-1111-200000000021', 11, None),
    ('bbbbbbbb-0011-0000-0000-000000001103', 'APP-2025-GUR-1103', '77777777-7777-7777-7777-222222222211', '2025-26', 'EXTENSION_OF_APPROVAL', 'aaaaaaaa-aaaa-aaaa-aaaa-000000000002', 'DRAFT', None, '11111111-1111-1111-1111-200000000021', None, None),

    # Inst 12: Eastern School Kolkata
    ('bbbbbbbb-0012-0000-0000-000000001201', 'APP-2025-KOL-1201', '77777777-7777-7777-7777-222222222212', '2025-26', 'EXTENSION_OF_APPROVAL', 'aaaaaaaa-aaaa-aaaa-aaaa-000000000002', 'REJECTED', None, '11111111-1111-1111-1111-200000000022', 25, 2),
    ('bbbbbbbb-0012-0000-0000-000000001202', 'APP-2025-KOL-1202', '77777777-7777-7777-7777-222222222212', '2025-26', 'CHANGE_OF_SITE', 'aaaaaaaa-aaaa-aaaa-aaaa-000000000008', 'EVC_SCHEDULED', '11111111-1111-1111-1111-200000000007', '11111111-1111-1111-1111-200000000022', 15, None),
]

emit("INSERT INTO applications (id, application_number, institution_id, academic_year, category, workflow_definition_id, current_state, assigned_to_user_id, created_by, submitted_at, final_decision_at, version) VALUES")
for idx, (app_id, num, inst_id, yr, cat, wf_id, st, assigned, created, sub_days, dec_days) in enumerate(applications_data):
    assigned_sql = f"'{assigned}'" if assigned else "NULL"
    sub_sql = f"CURRENT_TIMESTAMP - INTERVAL '{sub_days}' DAY" if sub_days else "NULL"
    dec_sql = f"CURRENT_TIMESTAMP - INTERVAL '{dec_days}' DAY" if dec_days else "NULL"
    term = ";" if idx == len(applications_data) - 1 else ","
    emit(f"    ('{app_id}', '{num}', '{inst_id}', '{yr}', '{cat}', '{wf_id}', '{st}', {assigned_sql}, '{created}', {sub_sql}, {dec_sql}, 0){term}")
emit("")

# 9. Workflow Transitions History
emit("-- 9. Authoritative Workflow Transitions History")
emit("INSERT INTO workflow_transitions (id, application_id, from_state, to_state, actor_id, actor_role, reason, evidence_summary, created_at) VALUES")
trans_list = []
trans_counter = 1
for app_id, num, inst_id, yr, cat, wf_id, st, assigned, created, sub_days, dec_days in applications_data:
    if st == 'DRAFT':
        continue
    # 1. Draft to Submitted
    tid1 = f"cccccccc-1000-0000-0000-{trans_counter:012d}"
    trans_counter += 1
    actor_id = created
    role = 'ROLE_INSTITUTION_OFFICER'
    c_time = f"CURRENT_TIMESTAMP - INTERVAL '{sub_days or 10}' DAY"
    trans_list.append((tid1, app_id, 'DRAFT', 'SUBMITTED', actor_id, role, 'Formal application proposal and institutional fee declaration submitted', 'Statutory digital signatures and document uploads verified', c_time))

    if st in ['UNDER_SCRUTINY', 'DEFICIENT', 'RESUBMITTED', 'SCRUTINY_PASSED', 'EVC_SCHEDULED', 'EVC_CONDUCTED', 'APPROVED', 'REJECTED']:
        tid2 = f"cccccccc-1000-0000-0000-{trans_counter:012d}"
        trans_counter += 1
        act = assigned or '11111111-1111-1111-1111-200000000002'
        c_time2 = f"CURRENT_TIMESTAMP - INTERVAL '{(sub_days or 10) - 1}' DAY"
        trans_list.append((tid2, app_id, 'SUBMITTED', 'UNDER_SCRUTINY', act, 'ROLE_REGIONAL_OFFICER', 'Initiated automated document extraction and committee scrutiny review', 'All mandatory certificates verified in OCR queue', c_time2))

    if st in ['DEFICIENT', 'RESUBMITTED']:
        tid3 = f"cccccccc-1000-0000-0000-{trans_counter:012d}"
        trans_counter += 1
        act = assigned or '11111111-1111-1111-1111-200000000002'
        c_time3 = f"CURRENT_TIMESTAMP - INTERVAL '{(sub_days or 10) - 2}' DAY"
        trans_list.append((tid3, app_id, 'UNDER_SCRUTINY', 'DEFICIENT', act, 'ROLE_REGIONAL_OFFICER', 'Deficiency notice issued: Certificate expiry or infrastructure shortfall identified', 'Official deficiency report generated per APH Chapter I/II norms', c_time3))

    if st == 'RESUBMITTED':
        tid4 = f"cccccccc-1000-0000-0000-{trans_counter:012d}"
        trans_counter += 1
        c_time4 = f"CURRENT_TIMESTAMP - INTERVAL '{(sub_days or 10) - 3}' DAY"
        trans_list.append((tid4, app_id, 'DEFICIENT', 'RESUBMITTED', created, 'ROLE_INSTITUTION_OFFICER', 'Clarification and replacement documentation submitted by institution', 'Updated compliance certificate uploaded for verification', c_time4))

    if st in ['SCRUTINY_PASSED', 'EVC_SCHEDULED', 'EVC_CONDUCTED', 'APPROVED']:
        tid_pass = f"cccccccc-1000-0000-0000-{trans_counter:012d}"
        trans_counter += 1
        act = assigned or '11111111-1111-1111-1111-200000000002'
        c_time_p = f"CURRENT_TIMESTAMP - INTERVAL '{(sub_days or 10) - 2}' DAY"
        trans_list.append((tid_pass, app_id, 'UNDER_SCRUTINY', 'SCRUTINY_PASSED', act, 'ROLE_REGIONAL_OFFICER', 'Scrutiny Committee verified all certificates, land, building, and faculty cadre ratios', 'Statutory compliance checklists verified 100% compliant', c_time_p))

    if st in ['EVC_SCHEDULED', 'EVC_CONDUCTED']:
        tid_evc = f"cccccccc-1000-0000-0000-{trans_counter:012d}"
        trans_counter += 1
        act = '11111111-1111-1111-1111-200000000007'
        c_time_e = f"CURRENT_TIMESTAMP - INTERVAL '{(sub_days or 10) - 3}' DAY"
        trans_list.append((tid_evc, app_id, 'SCRUTINY_PASSED', 'EVC_SCHEDULED', act, 'ROLE_EVC_COORDINATOR', 'Statutory inspection panel assigned and physical visit scheduled', 'Conflict-free 3-member committee confirmed via allocation solver', c_time_e))

    if st == 'EVC_CONDUCTED':
        tid_evcc = f"cccccccc-1000-0000-0000-{trans_counter:012d}"
        trans_counter += 1
        act = '11111111-1111-1111-1111-200000000007'
        c_time_ec = f"CURRENT_TIMESTAMP - INTERVAL '{(sub_days or 10) - 4}' DAY"
        trans_list.append((tid_evcc, app_id, 'EVC_SCHEDULED', 'EVC_CONDUCTED', act, 'ROLE_EVC_COORDINATOR', 'On-site physical inspection completed and signed committee report uploaded', 'Peer review team verified laboratories, classrooms, and computing infrastructure', c_time_ec))

    if st == 'APPROVED':
        tid_appr = f"cccccccc-1000-0000-0000-{trans_counter:012d}"
        trans_counter += 1
        act = assigned or '11111111-1111-1111-1111-200000000002'
        c_time_a = f"CURRENT_TIMESTAMP - INTERVAL '{dec_days or 1}' DAY"
        trans_list.append((tid_appr, app_id, 'SCRUTINY_PASSED', 'APPROVED', act, 'ROLE_REGIONAL_OFFICER', 'Statutory approval granted per AICTE Approval Process Handbook (APH 2024-27)', 'Executive Committee / Competent Authority final approval order recorded', c_time_a))

    if st == 'REJECTED':
        tid_rej = f"cccccccc-1000-0000-0000-{trans_counter:012d}"
        trans_counter += 1
        act = assigned or '11111111-1111-1111-1111-200000000002'
        c_time_r = f"CURRENT_TIMESTAMP - INTERVAL '{dec_days or 1}' DAY"
        trans_list.append((tid_rej, app_id, 'UNDER_SCRUTINY', 'REJECTED', act, 'ROLE_REGIONAL_OFFICER', 'Application rejected following unrectified statutory deficiencies and Standing Hearing review', 'Final rejection order communicated with appeal provisions', c_time_r))

for idx, (tid, aid, from_s, to_s, act_id, act_r, rsn, ev, c_time) in enumerate(trans_list):
    term = ";" if idx == len(trans_list) - 1 else ","
    emit(f"    ('{tid}', '{aid}', '{from_s}', '{to_s}', '{act_id}', '{act_r}', '{rsn}', '{ev}', {c_time}){term}")
emit("")

# 10. Application Course Details
emit("-- 10. Application Course Details (New Course Introductions)")
courses = [
    ('dddddddd-0001-0000-0000-000000000001', 'bbbbbbbb-0001-0000-0000-000000000102', 'B.Tech in Artificial Intelligence & Machine Learning', 'UNDER_GRADUATE', 'B_TECH', 'COMPUTER_SCIENCE_ENGINEERING', 120, 'Department of Computer Science & Engineering', 6, 4, 2500, 2, 4, 8, '1:15'),
    ('dddddddd-0002-0000-0000-000000000002', 'bbbbbbbb-0002-0000-0000-000000000203', 'B.Pharm in Pharmaceutical Biotechnology', 'UNDER_GRADUATE', 'B_PHARM', 'PHARMACY', 60, 'Department of Pharmaceutical Chemistry', 4, 6, 3200, 1, 2, 5, '1:15'),
    ('dddddddd-0006-0000-0000-000000000003', 'bbbbbbbb-0006-0000-0000-000000000602', 'B.Tech in Data Science & Business Analytics', 'UNDER_GRADUATE', 'B_TECH', 'COMPUTER_SCIENCE_ENGINEERING', 60, 'Department of Information Technology', 4, 3, 1900, 1, 2, 4, '1:20'),
    ('dddddddd-0007-0000-0000-000000000004', 'bbbbbbbb-0007-0000-0000-000000000702', 'B.Tech in Robotics & Industrial Automation', 'UNDER_GRADUATE', 'B_TECH', 'MECHANICAL_ENGINEERING', 60, 'Department of Mechanical & Mechatronics', 4, 4, 2100, 1, 2, 5, '1:20'),
    ('dddddddd-0008-0000-0000-000000000005', 'bbbbbbbb-0008-0000-0000-000000000803', 'M.Tech in Cyber Security & Digital Forensics', 'POST_GRADUATE', 'M_TECH', 'COMPUTER_SCIENCE_ENGINEERING', 30, 'Department of Computer Engineering', 2, 2, 1400, 1, 1, 2, '1:12'),
    ('dddddddd-0011-0000-0000-000000000006', 'bbbbbbbb-0011-0000-0000-000000001102', 'M.Tech in VLSI Design & Embedded Systems', 'POST_GRADUATE', 'M_TECH', 'ELECTRONICS_ENGINEERING', 30, 'Department of Electronics & Communication', 2, 3, 1600, 1, 1, 2, '1:12'),
]
emit("INSERT INTO application_course_details (")
emit("    id, application_id, course_name, programme_level, degree_type, course_discipline,")
emit("    proposed_intake, mode_of_delivery, duration_years, regulatory_classification,")
emit("    proposed_department, available_classrooms, laboratories_count, library_volumes,")
emit("    professors_count, associate_professors_count, assistant_professors_count, faculty_student_ratio")
emit(") VALUES")
for idx, (cid, aid, cname, lvl, deg, disc, intake, dept, crs, labs, vols, profs, assoc, asst, ratio) in enumerate(courses):
    term = ";" if idx == len(courses) - 1 else ","
    emit(f"    ('{cid}', '{aid}', '{cname}', '{lvl}', '{deg}', '{disc}', {intake}, 'REGULAR', 4, 'EMERGING_AREA', '{dept}', {crs}, {labs}, {vols}, {profs}, {assoc}, {asst}, '{ratio}'){term}")
emit("")

# 11. Application Branch Details
emit("-- 11. Application Branch Details (New Specializations / Branches)")
branches = [
    ('eeeeeeee-0001-0000-0000-000000000001', 'bbbbbbbb-0001-0000-0000-000000000103', 'B.Tech Computer Science & Engineering', 'CSE', 'Internet of Things & Cyber Security', 60, 180, 12, 4, 3, 3),
    ('eeeeeeee-0006-0000-0000-000000000002', 'bbbbbbbb-0006-0000-0000-000000000603', 'B.Tech Information Technology', 'IT', 'Cloud Computing & DevOps Architecture', 60, 120, 8, 3, 2, 2),
    ('eeeeeeee-0009-0000-0000-000000000003', 'bbbbbbbb-0009-0000-0000-000000000902', 'B.Tech Mechanical Engineering', 'ME', 'Smart Manufacturing & Digital Twin', 60, 120, 10, 4, 3, 3),
    ('eeeeeeee-0010-0000-0000-000000000004', 'bbbbbbbb-0010-0000-0000-000000001003', 'B.Tech Electronics & Communication', 'ECE', 'VLSI Design & Semiconductor Technology', 60, 120, 9, 3, 2, 2),
]
emit("INSERT INTO application_branch_details (")
emit("    id, application_id, existing_programme, existing_department, proposed_branch_name,")
emit("    proposed_intake, existing_approved_intake, faculty_count, staff_count, available_classrooms, laboratory_count")
emit(") VALUES")
for idx, (bid, aid, prog, dept, bname, intake, exist_intake, fcnt, scnt, crs, labs) in enumerate(branches):
    term = ";" if idx == len(branches) - 1 else ","
    emit(f"    ('{bid}', '{aid}', '{prog}', '{dept}', '{bname}', {intake}, {exist_intake}, {fcnt}, {scnt}, {crs}, {labs}){term}")
emit("")

# 12. Documents Registry (75 Documents with physical files)
emit("-- 12. Secure Document Registry & Compliance Hub")
emit("INSERT INTO documents (")
emit("    id, application_id, institution_id, document_type, original_filename, stored_filename,")
emit("    file_path, content_type, file_size_bytes, sha256_hash, ocr_status, ocr_engine,")
emit("    overall_confidence, is_uncertain, uploaded_by, document_name, document_category,")
emit("    valid_from, expiry_date, verification_status, compliance_status, version_number,")
emit("    previous_version_id, root_document_id, officer_comments, clarification_text")
emit(") VALUES")

# Document metadata definitions
doc_specs = [
    # inst_idx, app_id, type, cat, name, v_status, c_status, ver, is_replacement_for_idx, expiry_days_offset
    # Inst 1: Delhi Tech
    (1, 'bbbbbbbb-0001-0000-0000-000000000101', 'FIRE_SAFETY_CERTIFICATE', 'INFRASTRUCTURE', 'Delhi Fire Service Annual Clearance NOC', 'VERIFIED', 'COMPLIANT', 1, None, 180),
    (1, 'bbbbbbbb-0001-0000-0000-000000000101', 'OCCUPANCY_CERTIFICATE', 'INFRASTRUCTURE', 'MCD Institutional Campus Occupancy Certificate', 'VERIFIED', 'COMPLIANT', 1, None, 720),
    (1, 'bbbbbbbb-0001-0000-0000-000000000101', 'LAND_REGISTRATION', 'LEGAL', 'DDA Institutional Land Allotment & Registered Lease Deed', 'VERIFIED', 'COMPLIANT', 1, None, 3600),
    (1, 'bbbbbbbb-0001-0000-0000-000000000102', 'BUILDING_PLAN_APPROVAL', 'INFRASTRUCTURE', 'North Delhi Municipal Sanctioned Building Plan', 'VERIFIED', 'COMPLIANT', 1, None, 1200),
    (1, 'bbbbbbbb-0001-0000-0000-000000000102', 'FACULTY_LIST_QUALIFICATION', 'FACULTY', 'AICTE Cadre Roster & Faculty Qualifications (AI & ML)', 'UNDER_REVIEW', 'COMPLIANT', 1, None, 365),
    (1, 'bbbbbbbb-0001-0000-0000-000000000102', 'LABORATORY_INFRASTRUCTURE_REPORT', 'INFRASTRUCTURE', 'Specialized AI & Machine Learning GPU Lab Infrastructure', 'UNDER_REVIEW', 'COMPLIANT', 1, None, 365),
    (1, 'bbbbbbbb-0001-0000-0000-000000000103', 'EQUIPMENT_UTILIZATION_CERTIFICATE', 'INFRASTRUCTURE', 'IoT Embedded Lab Testbench & Utilization Report', 'REQUIRES_CLARIFICATION', 'DEFICIENT', 1, None, 200),
    # Inst 1: Replacement Document (v1 -> v2)
    (1, 'bbbbbbbb-0001-0000-0000-000000000103', 'EQUIPMENT_UTILIZATION_CERTIFICATE', 'INFRASTRUCTURE', 'IoT Embedded Lab Testbench & Utilization Report (Rectified)', 'VERIFIED', 'COMPLIANT', 2, 6, 365),

    # Inst 2: Mumbai Academy
    (2, 'bbbbbbbb-0002-0000-0000-000000000201', 'FIRE_SAFETY_CERTIFICATE', 'INFRASTRUCTURE', 'Mumbai Fire Brigade Compliance NOC', 'VERIFIED', 'COMPLIANT', 1, None, 240),
    (2, 'bbbbbbbb-0002-0000-0000-000000000201', 'OCCUPANCY_CERTIFICATE', 'INFRASTRUCTURE', 'BMC Building Completion Certificate (Kalina Campus)', 'VERIFIED', 'COMPLIANT', 1, None, 1000),
    (2, 'bbbbbbbb-0002-0000-0000-000000000202', 'FINANCIAL_AUDIT_REPORT', 'FINANCIAL', 'Audited Balance Sheet & Financial Utilization 2023-24', 'REQUIRES_CLARIFICATION', 'DEFICIENT', 1, None, 90),
    # Inst 2: Replacement (v1 -> v2)
    (2, 'bbbbbbbb-0002-0000-0000-000000000202', 'FINANCIAL_AUDIT_REPORT', 'FINANCIAL', 'Audited Balance Sheet 2023-24 with CA Attestation Certificate', 'VERIFIED', 'COMPLIANT', 2, 10, 300),
    (2, 'bbbbbbbb-0002-0000-0000-000000000203', 'SYLLABUS_CURRICULUM_APPROVAL', 'ACADEMIC', 'University of Mumbai Academic Council Curriculum Clearance', 'PENDING_REVIEW', 'COMPLIANT', 1, None, 500),
    (2, 'bbbbbbbb-0002-0000-0000-000000000203', 'FACULTY_LIST_QUALIFICATION', 'FACULTY', 'Pharmaceutical Biotechnology Proposed Cadre Appointments', 'PENDING_REVIEW', 'COMPLIANT', 1, None, 365),

    # Inst 3: Bengaluru Poly
    (3, 'bbbbbbbb-0003-0000-0000-000000000301', 'FIRE_SAFETY_CERTIFICATE', 'INFRASTRUCTURE', 'Karnataka State Fire & Emergency Services Clearance', 'VERIFIED', 'COMPLIANT', 1, None, 300),
    (3, 'bbbbbbbb-0003-0000-0000-000000000301', 'LAND_REGISTRATION', 'LEGAL', 'KIADB Industrial Area Allotment Lease & Title', 'VERIFIED', 'COMPLIANT', 1, None, 2500),
    (3, 'bbbbbbbb-0003-0000-0000-000000000302', 'BUILDING_PLAN_APPROVAL', 'INFRASTRUCTURE', 'BBMP Sanctioned Polytechnic Expansion Plan', 'VERIFIED', 'COMPLIANT', 1, None, 800),
    (3, 'bbbbbbbb-0003-0000-0000-000000000302', 'LABORATORY_INFRASTRUCTURE_REPORT', 'INFRASTRUCTURE', 'Advanced Computing Center & Network Lab Report', 'UNDER_REVIEW', 'COMPLIANT', 1, None, 365),
    (3, 'bbbbbbbb-0003-0000-0000-000000000303', 'AFFIDAVIT_FORMAT_1', 'LEGAL', 'Statutory AICTE Non-Capitation & Anti-Ragging Affidavit', 'VERIFIED', 'COMPLIANT', 1, None, 365),

    # Inst 4: Pune NIMR
    (4, 'bbbbbbbb-0004-0000-0000-000000000401', 'FIRE_SAFETY_CERTIFICATE', 'INFRASTRUCTURE', 'PMC Fire Department Safety Verification NOC', 'VERIFIED', 'COMPLIANT', 1, None, 150),
    (4, 'bbbbbbbb-0004-0000-0000-000000000401', 'OCCUPANCY_CERTIFICATE', 'INFRASTRUCTURE', 'Pune Municipal Corporation Full Occupancy Certificate', 'VERIFIED', 'COMPLIANT', 1, None, 950),
    (4, 'bbbbbbbb-0004-0000-0000-000000000403', 'BOARD_RESOLUTION', 'LEGAL', 'Governing Body Resolution for Management Programme Intake Expansion', 'VERIFIED', 'COMPLIANT', 1, None, 365),
    (4, 'bbbbbbbb-0004-0000-0000-000000000403', 'FINANCIAL_AUDIT_REPORT', 'FINANCIAL', 'Institutional Reserve Fund & Fixed Deposit Receipt Proof', 'VERIFIED', 'COMPLIANT', 1, None, 400),

    # Inst 5: Chennai Arch
    (5, 'bbbbbbbb-0005-0000-0000-000000000501', 'FIRE_SAFETY_CERTIFICATE', 'INFRASTRUCTURE', 'Tamil Nadu Fire & Rescue Services Statutory NOC', 'VERIFIED', 'COMPLIANT', 1, None, 210),
    (5, 'bbbbbbbb-0005-0000-0000-000000000502', 'LAND_REGISTRATION', 'LEGAL', 'Registered Title Deed for New Proposed Campus Site (OMR)', 'VERIFIED', 'COMPLIANT', 1, None, 3000),
    (5, 'bbbbbbbb-0005-0000-0000-000000000502', 'BUILDING_PLAN_APPROVAL', 'INFRASTRUCTURE', 'CMDA Approved Institutional Layout & Architecture Studios', 'VERIFIED', 'COMPLIANT', 1, None, 1500),
    (5, 'bbbbbbbb-0005-0000-0000-000000000502', 'LABORATORY_INFRASTRUCTURE_REPORT', 'INFRASTRUCTURE', 'Materials Testing & Model Making Workshop Survey', 'UNDER_REVIEW', 'COMPLIANT', 1, None, 365),

    # Inst 6: Central Computing Bhopal
    (6, 'bbbbbbbb-0006-0000-0000-000000000601', 'FIRE_SAFETY_CERTIFICATE', 'INFRASTRUCTURE', 'Bhopal Municipal Corporation Fire NOC', 'VERIFIED', 'COMPLIANT', 1, None, 330),
    (6, 'bbbbbbbb-0006-0000-0000-000000000601', 'OCCUPANCY_CERTIFICATE', 'INFRASTRUCTURE', 'MP Urban Development Directorate Campus Completion Certificate', 'VERIFIED', 'COMPLIANT', 1, None, 1100),
    (6, 'bbbbbbbb-0006-0000-0000-000000000602', 'SYLLABUS_CURRICULUM_APPROVAL', 'ACADEMIC', 'RGPV Affiliating University Degree Scheme Clearance', 'VERIFIED', 'COMPLIANT', 1, None, 400),
    (6, 'bbbbbbbb-0006-0000-0000-000000000602', 'LABORATORY_INFRASTRUCTURE_REPORT', 'INFRASTRUCTURE', 'Data Analytics & Cloud Computing Infrastructure Audit', 'VERIFIED', 'COMPLIANT', 1, None, 365),
    (6, 'bbbbbbbb-0006-0000-0000-000000000603', 'FACULTY_LIST_QUALIFICATION', 'FACULTY', 'Cloud Computing Proposed Faculty Cadre (1 Professor, 2 Assoc, 5 Asst)', 'UNDER_REVIEW', 'COMPLIANT', 1, None, 365),

    # Inst 7: Rajasthan College Jaipur
    (7, 'bbbbbbbb-0007-0000-0000-000000000701', 'FIRE_SAFETY_CERTIFICATE', 'INFRASTRUCTURE', 'Jaipur Nagar Nigam Fire Safety NOC', 'REQUIRES_CLARIFICATION', 'EXPIRING_SOON', 1, None, 10),
    # Inst 7: Replacement (v1 -> v2)
    (7, 'bbbbbbbb-0007-0000-0000-000000000701', 'FIRE_SAFETY_CERTIFICATE', 'INFRASTRUCTURE', 'Jaipur Nagar Nigam Fire Safety Renewal Certificate (2025-26)', 'VERIFIED', 'COMPLIANT', 2, 33, 365),
    (7, 'bbbbbbbb-0007-0000-0000-000000000702', 'BUILDING_PLAN_APPROVAL', 'INFRASTRUCTURE', 'JDA Approved Mechanical & Robotics Laboratory Block', 'VERIFIED', 'COMPLIANT', 1, None, 900),
    (7, 'bbbbbbbb-0007-0000-0000-000000000702', 'LABORATORY_INFRASTRUCTURE_REPORT', 'INFRASTRUCTURE', 'Industrial Robotics & Mechatronics Equipment Roster', 'VERIFIED', 'COMPLIANT', 1, None, 365),
    (7, 'bbbbbbbb-0007-0000-0000-000000000703', 'FACULTY_LIST_QUALIFICATION', 'FACULTY', 'Cadre Deficiency Clarification & Ph.D. Faculty Appointments', 'VERIFIED', 'COMPLIANT', 1, None, 365),

    # Inst 8: Himalayan Institute Dehradun
    (8, 'bbbbbbbb-0008-0000-0000-000000000801', 'LAND_REGISTRATION', 'LEGAL', 'Uttarakhand SIDCUL Land Lease Agreement (30 Acres Contiguous)', 'VERIFIED', 'COMPLIANT', 1, None, 3600),
    (8, 'bbbbbbbb-0008-0000-0000-000000000801', 'BUILDING_PLAN_APPROVAL', 'INFRASTRUCTURE', 'MDDA Approved Master Plan & Engineering Academic Complex', 'VERIFIED', 'COMPLIANT', 1, None, 1200),
    (8, 'bbbbbbbb-0008-0000-0000-000000000801', 'FIRE_SAFETY_CERTIFICATE', 'INFRASTRUCTURE', 'Uttarakhand Fire Service Clearance Certificate', 'VERIFIED', 'COMPLIANT', 1, None, 200),
    (8, 'bbbbbbbb-0008-0000-0000-000000000802', 'OCCUPANCY_CERTIFICATE', 'INFRASTRUCTURE', 'Dehradun Municipal Occupancy Verification Order', 'VERIFIED', 'COMPLIANT', 1, None, 850),
    (8, 'bbbbbbbb-0008-0000-0000-000000000803', 'LABORATORY_INFRASTRUCTURE_REPORT', 'INFRASTRUCTURE', 'Digital Forensics Hardware & Network Simulation Rig', 'REQUIRES_CLARIFICATION', 'DEFICIENT', 1, None, 180),
    # Inst 8: Replacement (v1 -> v2)
    (8, 'bbbbbbbb-0008-0000-0000-000000000803', 'LABORATORY_INFRASTRUCTURE_REPORT', 'INFRASTRUCTURE', 'Digital Forensics Lab Invoice & Equipment Calibration Certificate', 'PENDING_REVIEW', 'COMPLIANT', 2, 42, 365),

    # Inst 9: Coastal Engineering Vizag
    (9, 'bbbbbbbb-0009-0000-0000-000000000901', 'FIRE_SAFETY_CERTIFICATE', 'INFRASTRUCTURE', 'AP State Disaster Response & Fire Services NOC', 'VERIFIED', 'COMPLIANT', 1, None, 280),
    (9, 'bbbbbbbb-0009-0000-0000-000000000901', 'OCCUPANCY_CERTIFICATE', 'INFRASTRUCTURE', 'GVMC Building Completion & Occupancy Order', 'VERIFIED', 'COMPLIANT', 1, None, 1050),
    (9, 'bbbbbbbb-0009-0000-0000-000000000902', 'LABORATORY_INFRASTRUCTURE_REPORT', 'INFRASTRUCTURE', 'Smart Manufacturing CNC & 3D Printing Facility Report', 'VERIFIED', 'COMPLIANT', 1, None, 365),
    (9, 'bbbbbbbb-0009-0000-0000-000000000903', 'BOARD_RESOLUTION', 'LEGAL', 'Board Resolution for Phased Discontinuation of Civil Diploma', 'VERIFIED', 'COMPLIANT', 1, None, 365),

    # Inst 10: Deccan Institute Hyderabad
    (10, 'bbbbbbbb-0010-0000-0000-000000001001', 'FIRE_SAFETY_CERTIFICATE', 'INFRASTRUCTURE', 'Telangana State Disaster Response & Fire NOC', 'VERIFIED', 'COMPLIANT', 1, None, 220),
    (10, 'bbbbbbbb-0010-0000-0000-000000001001', 'OCCUPANCY_CERTIFICATE', 'INFRASTRUCTURE', 'GHMC Comprehensive Occupancy Certificate', 'VERIFIED', 'COMPLIANT', 1, None, 980),
    (10, 'bbbbbbbb-0010-0000-0000-000000001002', 'BUILDING_PLAN_APPROVAL', 'INFRASTRUCTURE', 'HMDA Approved Research & Emerging Tech Tower Layout', 'UNDER_REVIEW', 'COMPLIANT', 1, None, 750),
    (10, 'bbbbbbbb-0010-0000-0000-000000001002', 'SYLLABUS_CURRICULUM_APPROVAL', 'ACADEMIC', 'JNTUH Affiliation & New PG Programme Approval Letter', 'UNDER_REVIEW', 'COMPLIANT', 1, None, 365),

    # Inst 11: Greenfield College Gurugram
    (11, 'bbbbbbbb-0011-0000-0000-000000001101', 'LAND_REGISTRATION', 'LEGAL', 'Haryana Shahari Vikas Pradhikaran Institutional Allotment Deed', 'VERIFIED', 'COMPLIANT', 1, None, 3600),
    (11, 'bbbbbbbb-0011-0000-0000-000000001101', 'BUILDING_PLAN_APPROVAL', 'INFRASTRUCTURE', 'DTCP Haryana Approved Integrated Engineering Campus Plan', 'VERIFIED', 'COMPLIANT', 1, None, 1400),
    (11, 'bbbbbbbb-0011-0000-0000-000000001101', 'FIRE_SAFETY_CERTIFICATE', 'INFRASTRUCTURE', 'Gurugram Municipal Fire Safety Certificate', 'VERIFIED', 'COMPLIANT', 1, None, 300),
    (11, 'bbbbbbbb-0011-0000-0000-000000001102', 'LABORATORY_INFRASTRUCTURE_REPORT', 'INFRASTRUCTURE', 'VLSI Design & Cadence Tool Suite License Agreement', 'REQUIRES_CLARIFICATION', 'DEFICIENT', 1, None, 180),
    # Inst 11: Replacement (v1 -> v2)
    (11, 'bbbbbbbb-0011-0000-0000-000000001102', 'LABORATORY_INFRASTRUCTURE_REPORT', 'INFRASTRUCTURE', 'VLSI Design Cadence EDA 50-Seat Bundle Purchase & Invoices', 'PENDING_REVIEW', 'COMPLIANT', 2, 56, 365),

    # Inst 12: Eastern School Kolkata
    (12, 'bbbbbbbb-0012-0000-0000-000000001201', 'FIRE_SAFETY_CERTIFICATE', 'INFRASTRUCTURE', 'West Bengal Fire & Emergency Services NOC', 'REJECTED', 'EXPIRED', 1, None, -30),
    (12, 'bbbbbbbb-0012-0000-0000-000000001201', 'OCCUPANCY_CERTIFICATE', 'INFRASTRUCTURE', 'Bidhannagar Municipal Corporation Completion Certificate', 'VERIFIED', 'COMPLIANT', 1, None, 700),
    (12, 'bbbbbbbb-0012-0000-0000-000000001202', 'LAND_REGISTRATION', 'LEGAL', 'WBIDC Salt Lake Sector V 99-Year Registered Land Deed', 'VERIFIED', 'COMPLIANT', 1, None, 2500),
    (12, 'bbbbbbbb-0012-0000-0000-000000001202', 'BUILDING_PLAN_APPROVAL', 'INFRASTRUCTURE', 'KMDA Approved High-Rise Academic Expansion Scheme', 'VERIFIED', 'COMPLIANT', 1, None, 1100),

    # Additional institutional compliance certificates
    (1, None, 'FIRE_SAFETY_CERTIFICATE', 'INFRASTRUCTURE', 'Annual Campus Fire Safety Clearance (2024)', 'VERIFIED', 'EXPIRED', 1, None, -45),
    (2, None, 'FIRE_SAFETY_CERTIFICATE', 'INFRASTRUCTURE', 'BMC Fire Brigade Annual Inspection Certificate (2024)', 'VERIFIED', 'EXPIRED', 1, None, -60),
    (3, None, 'FIRE_SAFETY_CERTIFICATE', 'INFRASTRUCTURE', 'BBMP Fire Dept Compliance Renewal NOC', 'VERIFIED', 'EXPIRING_SOON', 1, None, 14),
    (4, None, 'FIRE_SAFETY_CERTIFICATE', 'INFRASTRUCTURE', 'PMC Fire Protection Statutory Roster', 'VERIFIED', 'EXPIRING_SOON', 1, None, 20),
    (5, None, 'FIRE_SAFETY_CERTIFICATE', 'INFRASTRUCTURE', 'Chennai Corporation Fire Prevention Certificate', 'VERIFIED', 'EXPIRING_SOON', 1, None, 25),
    (6, None, 'FIRE_SAFETY_CERTIFICATE', 'INFRASTRUCTURE', 'Bhopal City Fire Safety Certificate (2024)', 'VERIFIED', 'EXPIRED', 1, None, -90),
    (7, None, 'FIRE_SAFETY_CERTIFICATE', 'INFRASTRUCTURE', 'Jaipur Fire Services Regular Inspection Order', 'VERIFIED', 'EXPIRING_SOON', 1, None, 15),
    (8, None, 'FIRE_SAFETY_CERTIFICATE', 'INFRASTRUCTURE', 'Uttarakhand Selaqui Industrial Fire Clearance', 'VERIFIED', 'EXPIRED', 1, None, -15),
    (9, None, 'FIRE_SAFETY_CERTIFICATE', 'INFRASTRUCTURE', 'Vizag Beach Road Campus Fire NOC (2024)', 'VERIFIED', 'EXPIRED', 1, None, -35),
    (10, None, 'FIRE_SAFETY_CERTIFICATE', 'INFRASTRUCTURE', 'Hyderabad Gachibowli Fire Station Safety Certificate', 'VERIFIED', 'EXPIRING_SOON', 1, None, 22),
    (11, None, 'FIRE_SAFETY_CERTIFICATE', 'INFRASTRUCTURE', 'Gurugram Sector 88 Fire Inspection Order', 'VERIFIED', 'EXPIRING_SOON', 1, None, 18),
    (12, None, 'FIRE_SAFETY_CERTIFICATE', 'INFRASTRUCTURE', 'Kolkata Sector V Fire Safety Audit Certificate (2024)', 'VERIFIED', 'EXPIRED', 1, None, -80),
]

inst_id_lookup = {
    1: '22222222-2222-2222-2222-222222222222',
    2: '33333333-3333-3333-3333-333333333333',
    3: '44444444-4444-4444-4444-444444444444',
    4: '55555555-5555-5555-5555-555555555555',
    5: '66666666-6666-6666-6666-666666666666',
    6: '77777777-7777-7777-7777-222222222206',
    7: '77777777-7777-7777-7777-222222222207',
    8: '77777777-7777-7777-7777-222222222208',
    9: '77777777-7777-7777-7777-222222222209',
    10: '77777777-7777-7777-7777-222222222210',
    11: '77777777-7777-7777-7777-222222222211',
    12: '77777777-7777-7777-7777-222222222212',
}

inst_name_lookup = {
    1: 'Northern Institute of Technology',
    2: 'Western Institute of Engineering & Pharmacy',
    3: 'Southern Technical College',
    4: 'Pune National Institute of Management & Research',
    5: 'Chennai School of Architecture & Urban Planning',
    6: 'Central Institute of Computing & Emerging Tech',
    7: 'Rajasthan College of Engineering & Applied Science',
    8: 'Himalayan Institute of Technology',
    9: 'Coastal Engineering Institute',
    10: 'Deccan Institute of Technology & AI Research',
    11: 'Greenfield College of Engineering & Polytechnic',
    12: 'Eastern School of Technology & Applied Research',
}

inst_officer_lookup = {
    1: '11111111-1111-1111-1111-111111111113',
    2: '11111111-1111-1111-1111-111111111114',
    3: '11111111-1111-1111-1111-200000000013',
    4: '11111111-1111-1111-1111-200000000014',
    5: '11111111-1111-1111-1111-200000000015',
    6: '11111111-1111-1111-1111-200000000016',
    7: '11111111-1111-1111-1111-200000000017',
    8: '11111111-1111-1111-1111-200000000018',
    9: '11111111-1111-1111-1111-200000000019',
    10: '11111111-1111-1111-1111-200000000020',
    11: '11111111-1111-1111-1111-200000000021',
    12: '11111111-1111-1111-1111-200000000022',
}

doc_id_map = {} # spec_idx -> doc_id

for idx, spec in enumerate(doc_specs):
    inst_idx, app_id, doc_type, doc_cat, doc_name, v_status, c_status, ver, rep_idx, exp_days = spec
    doc_id = f"ffffffff-0000-0000-0000-{idx+1:012d}"
    doc_id_map[idx] = doc_id

    inst_id = inst_id_lookup[inst_idx]
    inst_name = inst_name_lookup[inst_idx]
    uploader_id = inst_officer_lookup[inst_idx]

    stored_fn = f"{doc_id}.pdf"
    orig_fn = f"{doc_name.replace(' ', '_').lower()[:40]}.pdf"
    file_path = f"data/documents/{stored_fn}"

    # Physical safe PDF creation
    sha256, size_bytes = create_safe_pdf(stored_fn, doc_name, f"CERT-2025-{idx+1:04d}", inst_name, f"Official statutory document for {doc_type} under AICTE APH Chapter I/II compliance.")

    prev_doc_id = "NULL"
    root_doc_id = f"'{doc_id}'"
    if rep_idx is not None and rep_idx in doc_id_map:
        prev_doc_id = f"'{doc_id_map[rep_idx]}'"
        root_doc_id = f"'{doc_id_map[rep_idx]}'"

    app_id_sql = f"'{app_id}'" if app_id else "NULL"
    val_from_sql = f"CURRENT_TIMESTAMP - INTERVAL '365' DAY"
    exp_sql = f"CURRENT_TIMESTAMP + INTERVAL '{exp_days}' DAY" if exp_days >= 0 else f"CURRENT_TIMESTAMP - INTERVAL '{abs(exp_days)}' DAY"
    officer_comm = f"'Verified per APH 2024-27 Clause norms by Scrutiny Committee'" if v_status == 'VERIFIED' else "NULL"
    clarif_txt = f"'Replacement document submitted addressing identified deficiency'" if ver > 1 else "NULL"

    term = ";" if idx == len(doc_specs) - 1 else ","
    emit(f"    ('{doc_id}', {app_id_sql}, '{inst_id}', '{doc_type}', '{orig_fn}', '{stored_fn}', '{file_path}', 'application/pdf', {size_bytes}, '{sha256}', 'COMPLETED', 'INDRA_OCR', 0.96, FALSE, '{uploader_id}', '{doc_name}', '{doc_cat}', {val_from_sql}, {exp_sql}, '{v_status}', '{c_status}', {ver}, {prev_doc_id}, {root_doc_id}, {officer_comm}, {clarif_txt}){term}")
emit("")

# 13. Scrutiny Evaluations & Findings
emit("-- 13. Scrutiny Committee Evaluations & Deficiency Findings")
evaluations = [
    ('55555555-1000-0000-0000-000000000001', 'bbbbbbbb-0001-0000-0000-000000000103', 'DEFICIENT', 'Deficiencies raised regarding laboratory utilization calibration', '11111111-1111-1111-1111-200000000002'),
    ('55555555-1000-0000-0000-000000000002', 'bbbbbbbb-0002-0000-0000-000000000202', 'DEFICIENT', 'Audited balance sheet missing CA seal and stamp', '11111111-1111-1111-1111-200000000003'),
    ('55555555-1000-0000-0000-000000000003', 'bbbbbbbb-0002-0000-0000-000000000203', 'PASSED', 'Clarification accepted and syllabus affiliation verified', '11111111-1111-1111-1111-200000000003'),
    ('55555555-1000-0000-0000-000000000004', 'bbbbbbbb-0007-0000-0000-000000000701', 'DEFICIENT', 'Fire Safety Certificate validity expired', '11111111-1111-1111-1111-200000000003'),
    ('55555555-1000-0000-0000-000000000005', 'bbbbbbbb-0008-0000-0000-000000000803', 'DEFICIENT', 'Digital Forensics lab equipment invoices incomplete', '11111111-1111-1111-1111-200000000002'),
    ('55555555-1000-0000-0000-000000000006', 'bbbbbbbb-0011-0000-0000-000000001102', 'PASSED', 'Cadence EDA tool license agreement verified and cleared', '11111111-1111-1111-1111-200000000002'),
]
emit("INSERT INTO scrutiny_evaluations (id, application_id, status, summary, evaluated_by_user_id, evaluated_at) VALUES")
for idx, (eid, aid, st, summ, u_id) in enumerate(evaluations):
    term = ";" if idx == len(evaluations) - 1 else ","
    emit(f"    ('{eid}', '{aid}', '{st}', '{summ}', '{u_id}', CURRENT_TIMESTAMP - INTERVAL '6' DAY){term}")
emit("")

findings = [
    # Open deficiency on Inst 1 (App 103)
    ('66666666-1000-0000-0000-000000000001', '55555555-1000-0000-0000-000000000001', 'bbbbbbbb-0001-0000-0000-000000000103', 'RULE_APH_COURSE_LABORATORY', doc_id_map[6], 'DEFICIENT', 'INADEQUATE_INFRASTRUCTURE', 'Testbench equipment utilization report lacks calibration seals per APH Annexure 4.', False, None, None, None),
    # Resolved deficiency on Inst 2 (App 202)
    ('66666666-1000-0000-0000-000000000002', '55555555-1000-0000-0000-000000000002', 'bbbbbbbb-0002-0000-0000-000000000202', 'RULE_APH_MANDATORY_DOCS', doc_id_map[10], 'COMPLIANT', 'FIELD_MISSING', 'Audited balance sheet attestation verified via replacement v2 document.', True, 'CA attestation and UDIN verified in revised upload.', '11111111-1111-1111-1111-200000000003', 'CURRENT_TIMESTAMP - INTERVAL \'2\' DAY'),
    # Open deficiency on Inst 7 (App 701)
    ('66666666-1000-0000-0000-000000000003', '55555555-1000-0000-0000-000000000004', 'bbbbbbbb-0007-0000-0000-000000000701', 'RULE_APH_FIRE_SAFETY', doc_id_map[33], 'DEFICIENT', 'EXPIRED_CERTIFICATE', 'Fire Safety Certificate validity expires in 10 days. Renewal NOC required.', False, None, None, None),
    # Open deficiency on Inst 8 (App 803)
    ('66666666-1000-0000-0000-000000000004', '55555555-1000-0000-0000-000000000005', 'bbbbbbbb-0008-0000-0000-000000000803', 'RULE_APH_COURSE_LABORATORY', doc_id_map[42], 'DEFICIENT', 'INSUFFICIENT_DATA', 'Hardware simulation rig purchase invoices not linked to institution asset register.', False, None, None, None),
    # Resolved deficiency on Inst 11 (App 1102)
    ('66666666-1000-0000-0000-000000000005', '55555555-1000-0000-0000-000000000006', 'bbbbbbbb-0011-0000-0000-000000001102', 'RULE_APH_COURSE_LABORATORY', doc_id_map[56], 'COMPLIANT', 'INSUFFICIENT_DATA', 'Cadence EDA tool license purchase and 50-user bundle verified in v2 replacement.', True, 'License agreement and university bundle invoices verified.', '11111111-1111-1111-1111-200000000002', 'CURRENT_TIMESTAMP - INTERVAL \'1\' DAY'),
]
emit("INSERT INTO scrutiny_findings (id, evaluation_id, application_id, rule_id, document_id, status, deficiency_type, explanation, is_resolved, resolution_notes, resolved_by, resolved_at) VALUES")
for idx, (fid, eid, aid, rid, did, st, dtype, expl, is_res, res_notes, res_by, res_at) in enumerate(findings):
    term = ";" if idx == len(findings) - 1 else ","
    res_notes_sql = f"'{res_notes}'" if res_notes else "NULL"
    res_by_sql = f"'{res_by}'" if res_by else "NULL"
    res_at_sql = res_at if res_at else "NULL"
    is_res_sql = "TRUE" if is_res else "FALSE"
    emit(f"    ('{fid}', '{eid}', '{aid}', '{rid}', '{did}', '{st}', '{dtype}', '{expl}', {is_res_sql}, {res_notes_sql}, {res_by_sql}, {res_at_sql}){term}")
emit("")

# 14. Evaluator / Peer Experts Registry (12 Experts)
emit("-- 14. Evaluator / Peer Expert Registry (Non-Conflicting Academic Leaders)")
experts_data = [
    ('eeeeeeee-eeee-eeee-eeee-000000000011', 'Prof. Arvind Kejriwal', 'arvind.k@iitd.ac.in', 'COMPUTER_SCIENCE_ENGINEERING', 'Senior Professor & Dean (Faculty)', '22222222-2222-2222-2222-222222222222'),
    ('eeeeeeee-eeee-eeee-eeee-000000000012', 'Dr. Manjula Natarajan', 'manjula.n@nitk.edu.in', 'CIVIL_ENGINEERING', 'Professor & Head of Structural Engineering', '44444444-4444-4444-4444-444444444444'),
    ('eeeeeeee-eeee-eeee-eeee-000000000013', 'Dr. Satish Chandra', 'satish.c@vjti.ac.in', 'ELECTRICAL_ENGINEERING', 'Professor & Dean (Research)', '33333333-3333-3333-3333-333333333333'),
    ('eeeeeeee-eeee-eeee-eeee-000000000014', 'Dr. Sunita Deshpande', 'sunita.d@coep.ac.in', 'MECHANICAL_ENGINEERING', 'Professor of Robotics & Automation', '55555555-5555-5555-5555-555555555555'),
    ('eeeeeeee-eeee-eeee-eeee-000000000015', 'Prof. B. K. Sengupta', 'bk.sengupta@iitkgp.ac.in', 'ARCHITECTURE', 'Professor of Architecture & Planning', None),
    ('eeeeeeee-eeee-eeee-eeee-000000000016', 'Dr. Meena Swaminathan', 'meena.s@ictmumbai.edu.in', 'PHARMACY', 'Professor of Pharmaceutical Chemistry', None),
    ('eeeeeeee-eeee-eeee-eeee-000000000017', 'Dr. R. K. Bhattacharya', 'rkb@iiitb.ac.in', 'INFORMATION_TECHNOLOGY', 'Dean (Academic Affairs)', None),
    ('eeeeeeee-eeee-eeee-eeee-000000000018', 'Prof. Sanjeev Kumar', 'sanjeev.k@mnit.ac.in', 'COMPUTER_SCIENCE_ENGINEERING', 'Professor of Artificial Intelligence', None),
    ('eeeeeeee-eeee-eeee-eeee-000000000019', 'Dr. Lakshmi Narayanan', 'lakshmi.n@nitw.ac.in', 'ELECTRONICS_ENGINEERING', 'Head of VLSI & Embedded Systems', None),
    ('eeeeeeee-eeee-eeee-eeee-000000000020', 'Prof. Amitabh Sen', 'amitabh.sen@jadavpuruniversity.in', 'ELECTRICAL_ENGINEERING', 'Professor of Power Systems', None),
    ('eeeeeeee-eeee-eeee-eeee-000000000021', 'Dr. Vandana Aggarwal', 'vandana.a@thapar.edu', 'COMPUTER_SCIENCE_ENGINEERING', 'Professor of Cyber Security', None),
    ('eeeeeeee-eeee-eeee-eeee-000000000022', 'Dr. Jayaprakash Rao', 'jp.rao@osmania.ac.in', 'MECHANICAL_ENGINEERING', 'Dean of Engineering', None),
]
emit("INSERT INTO experts (id, name, email, discipline, designation, affiliated_institution_id, active) VALUES")
for idx, (xid, xname, xemail, xdisc, xdesig, xaffil) in enumerate(experts_data):
    term = ";" if idx == len(experts_data) - 1 else ","
    affil_sql = f"'{xaffil}'" if xaffil else "NULL"
    emit(f"    ('{xid}', '{xname}', '{xemail}', '{xdisc}', '{xdesig}', {affil_sql}, TRUE){term}")
emit("")

# 15. Expert Calendar Availabilities (Days 5 through 30)
emit("-- 15. Expert Calendar Availability Matrix (Upcoming Inspection Windows)")
emit("INSERT INTO expert_availabilities (id, expert_id, available_date) VALUES")
avail_entries = []
for e_idx, (xid, _, _, _, _, _) in enumerate(experts_data):
    for day_off in range(5, 30):
        # Exclude Sundays (e.g. day_off % 7 != 0)
        if day_off % 7 != 0:
            aid = f"aaaaaaaa-{e_idx+10:04d}-0001-0001-{day_off:012d}"
            avail_entries.append(f"    ('{aid}', '{xid}', CURRENT_DATE + INTERVAL '{day_off}' DAY)")
emit(",\n".join(avail_entries) + ";")
emit("")

# 16. EVC Inspections (10 records: 3 SCHEDULED, 2 COMPLETED, 2 IN_PROGRESS, 3 PENDING)
emit("-- 16. Expert Visit Committee (EVC) Inspections")
inspections = [
    # 3 SCHEDULED
    ('99999999-1000-0000-0000-000000000001', 'bbbbbbbb-0005-0000-0000-000000000502', '66666666-6666-6666-6666-666666666666', 2, 3, 5, 20, 8, 9, 'SCHEDULED', 'Comprehensive physical inspection of proposed new campus site on OMR Road.'),
    ('99999999-1000-0000-0000-000000000002', 'bbbbbbbb-0008-0000-0000-000000000801', '77777777-7777-7777-7777-222222222208', 2, 3, 6, 22, 12, 13, 'SCHEDULED', 'New Institution physical infrastructure verification per APH Chapter I.'),
    ('99999999-1000-0000-0000-000000000003', 'bbbbbbbb-0012-0000-0000-000000001202', '77777777-7777-7777-7777-222222222212', 2, 3, 7, 25, 15, 16, 'SCHEDULED', 'Change of Site land boundary and physical building verification.'),
    # 2 COMPLETED
    ('99999999-1000-0000-0000-000000000004', 'bbbbbbbb-0006-0000-0000-000000000602', '77777777-7777-7777-7777-222222222206', 2, 3, -15, -5, -10, -9, 'COMPLETED', 'On-site laboratory and faculty cadre verification completed successfully.'),
    ('99999999-1000-0000-0000-000000000005', 'bbbbbbbb-0011-0000-0000-000000001101', '77777777-7777-7777-7777-222222222211', 2, 3, -20, -10, -14, -13, 'COMPLETED', 'New Institution campus facilities and workshop inspection report signed.'),
    # 2 IN_PROGRESS
    ('99999999-1000-0000-0000-000000000006', 'bbbbbbbb-0001-0000-0000-000000000102', '22222222-2222-2222-2222-222222222222', 2, 3, 0, 10, 0, 1, 'IN_PROGRESS', 'Inspection team currently conducting laboratory and computing facility walkthrough.'),
    ('99999999-1000-0000-0000-000000000007', 'bbbbbbbb-0007-0000-0000-000000000702', '77777777-7777-7777-7777-222222222207', 2, 3, 1, 12, 1, 2, 'IN_PROGRESS', 'Robotics and mechatronics lab equipment physical verification in progress.'),
    # 3 PENDING (Ready for allocation by EVC Coordinator)
    ('99999999-1000-0000-0000-000000000008', 'bbbbbbbb-0003-0000-0000-000000000302', '44444444-4444-4444-4444-444444444444', 2, 3, 10, 30, None, None, 'PENDING', 'Awaiting committee allocation for Polytechnic expansion.'),
    ('99999999-1000-0000-0000-000000000009', 'bbbbbbbb-0009-0000-0000-000000000903', '77777777-7777-7777-7777-222222222209', 2, 3, 12, 35, None, None, 'PENDING', 'Closure verification inspection pending panel assignment.'),
    ('99999999-1000-0000-0000-000000000010', 'bbbbbbbb-0010-0000-0000-000000001002', '77777777-7777-7777-7777-222222222210', 2, 3, 14, 40, None, None, 'PENDING', 'Emerging Technology Research Center physical verification pending.'),
]
emit("INSERT INTO evc_inspections (")
emit("    id, application_id, institution_id, duration_in_days, required_member_count,")
emit("    earliest_start_date, latest_start_date, scheduled_start_date, scheduled_end_date, status, special_instructions")
emit(") VALUES")
for idx, (iid, aid, inst_id, dur, req_cnt, e_days, l_days, s_days, end_days, st, instr) in enumerate(inspections):
    term = ";" if idx == len(inspections) - 1 else ","
    e_sql = f"CURRENT_DATE + INTERVAL '{e_days}' DAY" if e_days >= 0 else f"CURRENT_DATE - INTERVAL '{abs(e_days)}' DAY"
    l_sql = f"CURRENT_DATE + INTERVAL '{l_days}' DAY" if l_days >= 0 else f"CURRENT_DATE - INTERVAL '{abs(l_days)}' DAY"
    s_sql = f"CURRENT_DATE + INTERVAL '{s_days}' DAY" if s_days is not None and s_days >= 0 else (f"CURRENT_DATE - INTERVAL '{abs(s_days)}' DAY" if s_days is not None else "NULL")
    end_sql = f"CURRENT_DATE + INTERVAL '{end_days}' DAY" if end_days is not None and end_days >= 0 else (f"CURRENT_DATE - INTERVAL '{abs(end_days)}' DAY" if end_days is not None else "NULL")
    emit(f"    ('{iid}', '{aid}', '{inst_id}', {dur}, {req_cnt}, {e_sql}, {l_sql}, {s_sql}, {end_sql}, '{st}', '{instr}'){term}")
emit("")

# 17. Committee Assignments (3 per scheduled/completed/in-progress inspection = 7 * 3 = 21 assignments)
emit("-- 17. Statutory Committee Assignments (Conflict-Free 3-Member Peer Panels)")
assignments = [
    # Insp 1 (Chennai Arch, Tamil Nadu) -> Experts from non-conflicting states
    ('99999999-2000-0000-0000-000000000001', '99999999-1000-0000-0000-000000000001', 'eeeeeeee-eeee-eeee-eeee-000000000015', 'CHAIRPERSON', 'CONFIRMED'), # Prof. B.K. Sengupta (Arch)
    ('99999999-2000-0000-0000-000000000002', '99999999-1000-0000-0000-000000000001', 'eeeeeeee-eeee-eeee-eeee-000000000012', 'MEMBER', 'CONFIRMED'),      # Dr. Manjula (Civil)
    ('99999999-2000-0000-0000-000000000003', '99999999-1000-0000-0000-000000000001', 'eeeeeeee-eeee-eeee-eeee-000000000013', 'SPECIALIST', 'CONFIRMED'),  # Dr. Satish (Elec)

    # Insp 2 (Himalayan Inst, Uttarakhand)
    ('99999999-2000-0000-0000-000000000004', '99999999-1000-0000-0000-000000000002', 'eeeeeeee-eeee-eeee-eeee-000000000014', 'CHAIRPERSON', 'CONFIRMED'), # Dr. Sunita (Mech)
    ('99999999-2000-0000-0000-000000000005', '99999999-1000-0000-0000-000000000002', 'eeeeeeee-eeee-eeee-eeee-000000000017', 'MEMBER', 'CONFIRMED'),      # Dr. Bhattacharya (IT)
    ('99999999-2000-0000-0000-000000000006', '99999999-1000-0000-0000-000000000002', 'eeeeeeee-eeee-eeee-eeee-000000000020', 'SPECIALIST', 'CONFIRMED'),  # Prof. Amitabh (Elec)

    # Insp 3 (Eastern School, West Bengal)
    ('99999999-2000-0000-0000-000000000007', '99999999-1000-0000-0000-000000000003', 'eeeeeeee-eeee-eeee-eeee-000000000018', 'CHAIRPERSON', 'CONFIRMED'), # Prof. Sanjeev Kumar (CSE)
    ('99999999-2000-0000-0000-000000000008', '99999999-1000-0000-0000-000000000003', 'eeeeeeee-eeee-eeee-eeee-000000000019', 'MEMBER', 'CONFIRMED'),      # Dr. Lakshmi (ECE)
    ('99999999-2000-0000-0000-000000000009', '99999999-1000-0000-0000-000000000003', 'eeeeeeee-eeee-eeee-eeee-000000000022', 'SPECIALIST', 'CONFIRMED'),  # Dr. Jayaprakash (Mech)

    # Insp 4 (Central Computing Bhopal - Completed)
    ('99999999-2000-0000-0000-000000000010', '99999999-1000-0000-0000-000000000004', 'eeeeeeee-eeee-eeee-eeee-000000000011', 'CHAIRPERSON', 'CONFIRMED'), # Prof. Kejriwal (CSE)
    ('99999999-2000-0000-0000-000000000011', '99999999-1000-0000-0000-000000000004', 'eeeeeeee-eeee-eeee-eeee-000000000021', 'MEMBER', 'CONFIRMED'),      # Dr. Vandana (Cyber Sec)
    ('99999999-2000-0000-0000-000000000012', '99999999-1000-0000-0000-000000000004', 'eeeeeeee-eeee-eeee-eeee-000000000017', 'SPECIALIST', 'CONFIRMED'),  # Dr. Bhattacharya (IT)

    # Insp 5 (Greenfield College Gurugram - Completed)
    ('99999999-2000-0000-0000-000000000013', '99999999-1000-0000-0000-000000000005', 'eeeeeeee-eeee-eeee-eeee-000000000013', 'CHAIRPERSON', 'CONFIRMED'), # Dr. Satish (Elec)
    ('99999999-2000-0000-0000-000000000014', '99999999-1000-0000-0000-000000000005', 'eeeeeeee-eeee-eeee-eeee-000000000014', 'MEMBER', 'CONFIRMED'),      # Dr. Sunita (Mech)
    ('99999999-2000-0000-0000-000000000015', '99999999-1000-0000-0000-000000000005', 'eeeeeeee-eeee-eeee-eeee-000000000012', 'SPECIALIST', 'CONFIRMED'),  # Dr. Manjula (Civil)
]
emit("INSERT INTO committee_assignments (id, inspection_id, expert_id, role, status) VALUES")
for idx, (as_id, iid, xid, arole, ast) in enumerate(assignments):
    term = ";" if idx == len(assignments) - 1 else ","
    emit(f"    ('{as_id}', '{iid}', '{xid}', '{arole}', '{ast}'){term}")
emit("")

# 18. Document Corrective Actions (16 records)
emit("-- 18. Document & Compliance Corrective Actions")
corrective_actions = [
    # Inst 1
    ('77777777-3000-0000-0000-000000000001', inst_id_lookup[1], 'bbbbbbbb-0001-0000-0000-000000000103', doc_id_map[6], 'DOC_CLARIFICATION', 'Upload calibrated testbench equipment utilization certificate with department head seal.', 'HIGH', 15, 'OPEN', None, None, None, None),
    ('77777777-3000-0000-0000-000000000002', inst_id_lookup[1], None, doc_id_map[60], 'CERTIFICATE_RENEWAL', 'Fire safety annual inspection certificate expired. Upload current session NOC.', 'CRITICAL', -10, 'OPEN', None, None, None, None),
    # Inst 2
    ('77777777-3000-0000-0000-000000000003', inst_id_lookup[2], 'bbbbbbbb-0002-0000-0000-000000000202', doc_id_map[10], 'AUDIT_RECTIFICATION', 'Submit audited balance sheet with valid chartered accountant UDIN verification.', 'HIGH', 5, 'RESOLVED', 'Uploaded signed balance sheet with UDIN 240987654ABCD12.', 'Balance sheet verified against ICAI portal.', '11111111-1111-1111-1111-200000000003', 'CURRENT_TIMESTAMP - INTERVAL \'2\' DAY'),
    ('77777777-3000-0000-0000-000000000004', inst_id_lookup[2], None, doc_id_map[61], 'CERTIFICATE_RENEWAL', 'Mumbai Fire Brigade annual safety audit renewal required.', 'CRITICAL', 20, 'IN_PROGRESS', 'Renewal application submitted to BMC Fire Brigade; inspection scheduled.', None, None, None),
    # Inst 3
    ('77777777-3000-0000-0000-000000000005', inst_id_lookup[3], None, doc_id_map[62], 'CERTIFICATE_RENEWAL', 'Fire safety certificate expiring in 14 days. Submit renewal copy.', 'HIGH', 14, 'OPEN', None, None, None, None),
    # Inst 4
    ('77777777-3000-0000-0000-000000000006', inst_id_lookup[4], None, doc_id_map[63], 'CERTIFICATE_RENEWAL', 'PMC Fire Protection statutory renewal due in 20 days.', 'MEDIUM', 20, 'OPEN', None, None, None, None),
    # Inst 5
    ('77777777-3000-0000-0000-000000000007', inst_id_lookup[5], None, doc_id_map[64], 'CERTIFICATE_RENEWAL', 'Chennai Corporation fire safety renewal verification.', 'MEDIUM', 25, 'OPEN', None, None, None, None),
    # Inst 6
    ('77777777-3000-0000-0000-000000000008', inst_id_lookup[6], None, doc_id_map[65], 'CERTIFICATE_RENEWAL', 'Fire Safety Certificate expired 90 days ago. Immediate upload required.', 'CRITICAL', -30, 'OVERDUE', None, None, None, None),
    # Inst 7
    ('77777777-3000-0000-0000-000000000009', inst_id_lookup[7], 'bbbbbbbb-0007-0000-0000-000000000701', doc_id_map[33], 'DOC_REPLACEMENT', 'Replace Fire Safety Certificate nearing expiry with newly issued renewal NOC.', 'CRITICAL', 10, 'RESOLVED', 'Uploaded renewed Fire Safety NOC valid through 2026-27.', 'Verified with Jaipur Nagar Nigam portal.', '11111111-1111-1111-1111-200000000003', 'CURRENT_TIMESTAMP - INTERVAL \'1\' DAY'),
    ('77777777-3000-0000-0000-000000000010', inst_id_lookup[7], None, doc_id_map[66], 'CERTIFICATE_RENEWAL', 'Fire safety inspection order renewal pending.', 'HIGH', 15, 'IN_PROGRESS', 'Inspection fee deposited at Nagar Nigam.', None, None, None),
    # Inst 8
    ('77777777-3000-0000-0000-000000000011', inst_id_lookup[8], 'bbbbbbbb-0008-0000-0000-000000000803', doc_id_map[42], 'EQUIPMENT_INVOICE', 'Upload equipment calibration proof and invoices for digital forensics hardware.', 'HIGH', 12, 'SUBMITTED_FOR_REVIEW', 'Uploaded vendor purchase order and calibration report v2.', 'Pending review by Regional Scrutiny Officer.', None, None),
    ('77777777-3000-0000-0000-000000000012', inst_id_lookup[8], None, doc_id_map[67], 'CERTIFICATE_RENEWAL', 'Selaqui Industrial fire safety certificate expired. Upload renewal.', 'CRITICAL', -15, 'OPEN', None, None, None, None),
    # Inst 9
    ('77777777-3000-0000-0000-000000000013', inst_id_lookup[9], None, doc_id_map[68], 'CERTIFICATE_RENEWAL', 'Beach Road campus fire safety NOC renewal expired.', 'CRITICAL', -35, 'OPEN', None, None, None, None),
    # Inst 10
    ('77777777-3000-0000-0000-000000000014', inst_id_lookup[10], None, doc_id_map[69], 'CERTIFICATE_RENEWAL', 'Gachibowli Fire Station safety audit certificate expiring soon.', 'HIGH', 22, 'SUBMITTED_FOR_REVIEW', 'Fire audit completed; provisional clearance uploaded.', None, None, None),
    # Inst 11
    ('77777777-3000-0000-0000-000000000015', inst_id_lookup[11], 'bbbbbbbb-0011-0000-0000-000000001102', doc_id_map[56], 'DOC_REPLACEMENT', 'Upload Cadence EDA tool license purchase and 50-user bundle proof.', 'HIGH', 10, 'RESOLVED', 'Uploaded license agreement and purchase invoices v2.', 'Verified valid 50-seat educational bundle.', '11111111-1111-1111-1111-200000000002', 'CURRENT_TIMESTAMP - INTERVAL \'1\' DAY'),
    # Inst 12
    ('77777777-3000-0000-0000-000000000016', inst_id_lookup[12], 'bbbbbbbb-0012-0000-0000-000000001201', doc_id_map[58], 'CERTIFICATE_RENEWAL', 'Fire Safety Certificate expired. Submit renewed certificate from West Bengal Fire Services.', 'CRITICAL', -30, 'OPEN', None, None, None, None),
]
emit("INSERT INTO document_corrective_actions (")
emit("    id, institution_id, application_id, document_id, action_type, description,")
emit("    priority, responsible_party, due_date, status, institution_response, officer_notes, resolved_by, resolved_at")
emit(") VALUES")
for idx, (ca_id, inst_id, aid, did, atype, desc, prio, due_d, st, resp, off_n, res_by, res_at) in enumerate(corrective_actions):
    term = ";" if idx == len(corrective_actions) - 1 else ","
    aid_sql = f"'{aid}'" if aid else "NULL"
    did_sql = f"'{did}'" if did else "NULL"
    due_sql = f"CURRENT_TIMESTAMP + INTERVAL '{due_d}' DAY" if due_d >= 0 else f"CURRENT_TIMESTAMP - INTERVAL '{abs(due_d)}' DAY"
    resp_sql = f"'{resp}'" if resp else "NULL"
    off_n_sql = f"'{off_n}'" if off_n else "NULL"
    res_by_sql = f"'{res_by}'" if res_by else "NULL"
    res_at_sql = res_at if res_at else "NULL"
    emit(f"    ('{ca_id}', '{inst_id}', {aid_sql}, {did_sql}, '{atype}', '{desc}', '{prio}', 'INSTITUTION_OFFICER', {due_sql}, '{st}', {resp_sql}, {off_n_sql}, {res_by_sql}, {res_at_sql}){term}")
emit("")

# 19. Deadlines (14 records)
emit("-- 19. Statutory & Institutional Deadlines")
deadlines_data = [
    ('dddddddd-1000-0000-0000-000000000001', 'DL_SCRUTINY_RECT_DEL', 'Scrutiny Rectification Upload (Delhi Tech)', 'Mandatory 10-day window to rectify laboratory testbench deficiency findings.', 'STATUTORY', 'APH 2024-27 Clause 1.6', 'APPLICATION', 'bbbbbbbb-0001-0000-0000-000000000103', inst_id_lookup[1], 15, 'PENDING'),
    ('dddddddd-1000-0000-0000-000000000002', 'DL_FIRE_RENEWAL_DEL', 'Mandatory Annual Fire Safety NOC Renewal (Delhi Tech)', 'Local Fire Service NOC requires annual renewal to maintain compliance.', 'STATUTORY', 'APH Clause 2.1.2', 'INSTITUTION', inst_id_lookup[1], inst_id_lookup[1], -10, 'OVERDUE'),
    ('dddddddd-1000-0000-0000-000000000003', 'DL_AUDIT_RECT_MUM', 'Audit Balance Sheet Rectification (Mumbai Academy)', 'Submit chartered accountant attested balance sheet.', 'STATUTORY', 'APH Clause 2.1.2', 'APPLICATION', 'bbbbbbbb-0002-0000-0000-000000000202', inst_id_lookup[2], -2, 'RESOLVED'),
    ('dddddddd-1000-0000-0000-000000000004', 'DL_FIRE_RENEWAL_BEN', 'Fire Safety Renewal (Bengaluru Polytechnic)', 'Annual municipal fire safety NOC renewal.', 'STATUTORY', 'APH Clause 2.1.2', 'INSTITUTION', inst_id_lookup[3], inst_id_lookup[3], 14, 'DUE_SOON'),
    ('dddddddd-1000-0000-0000-000000000005', 'DL_FIRE_RENEWAL_PUN', 'Fire Safety Compliance (Pune NIMR)', 'Municipal fire clearance certificate renewal.', 'STATUTORY', 'APH Clause 2.1.2', 'INSTITUTION', inst_id_lookup[4], inst_id_lookup[4], 20, 'PENDING'),
    ('dddddddd-1000-0000-0000-000000000006', 'DL_FIRE_RENEWAL_CHE', 'Fire Safety Certificate Renewal (Chennai Arch)', 'State Disaster & Fire NOC annual renewal.', 'STATUTORY', 'APH Clause 2.1.2', 'INSTITUTION', inst_id_lookup[5], inst_id_lookup[5], 25, 'PENDING'),
    ('dddddddd-1000-0000-0000-000000000007', 'DL_FIRE_RENEWAL_BHO', 'Fire Safety Certificate Overdue Notice (Bhopal)', 'Fire Safety NOC expired 90 days ago.', 'STATUTORY', 'APH Clause 2.1.2', 'INSTITUTION', inst_id_lookup[6], inst_id_lookup[6], -30, 'OVERDUE'),
    ('dddddddd-1000-0000-0000-000000000008', 'DL_SCRUTINY_RECT_JAI', 'Fire Safety Rectification (Rajasthan College)', 'Upload renewed fire safety certificate.', 'STATUTORY', 'APH Clause 1.6', 'APPLICATION', 'bbbbbbbb-0007-0000-0000-000000000701', inst_id_lookup[7], -1, 'RESOLVED'),
    ('dddddddd-1000-0000-0000-000000000009', 'DL_LAB_INVOICES_DEH', 'Laboratory Equipment Invoices (Himalayan Inst)', 'Submit equipment purchase bills and calibration reports.', 'STATUTORY', 'APH Clause 2.4.3', 'APPLICATION', 'bbbbbbbb-0008-0000-0000-000000000803', inst_id_lookup[8], 12, 'DUE_SOON'),
    ('dddddddd-1000-0000-0000-000000000010', 'DL_FIRE_RENEWAL_DEH', 'Industrial Fire Safety Renewal (Himalayan Inst)', 'Fire clearance certificate expired 15 days ago.', 'STATUTORY', 'APH Clause 2.1.2', 'INSTITUTION', inst_id_lookup[8], inst_id_lookup[8], -15, 'OVERDUE'),
    ('dddddddd-1000-0000-0000-000000000011', 'DL_FIRE_RENEWAL_HYD', 'Campus Safety Renewal (Deccan Institute)', 'Gachibowli fire station safety audit renewal.', 'STATUTORY', 'APH Clause 2.1.2', 'INSTITUTION', inst_id_lookup[10], inst_id_lookup[10], 22, 'PENDING'),
    ('dddddddd-1000-0000-0000-000000000012', 'DL_LAB_EDA_GUR', 'EDA Tool License Agreement (Greenfield Gurugram)', 'Upload Cadence EDA software bundle purchase proof.', 'STATUTORY', 'APH Clause 2.4.3', 'APPLICATION', 'bbbbbbbb-0011-0000-0000-000000001102', inst_id_lookup[11], -1, 'RESOLVED'),
    ('dddddddd-1000-0000-0000-000000000013', 'DL_FIRE_RENEWAL_GUR', 'Fire Inspection Order Renewal (Greenfield Gurugram)', 'Municipal fire inspection order renewal.', 'STATUTORY', 'APH Clause 2.1.2', 'INSTITUTION', inst_id_lookup[11], inst_id_lookup[11], 18, 'DUE_SOON'),
    ('dddddddd-1000-0000-0000-000000000014', 'DL_FIRE_RENEWAL_KOL', 'Fire Safety Notice (Eastern School Kolkata)', 'Fire safety clearance certificate expired.', 'STATUTORY', 'APH Clause 2.1.2', 'INSTITUTION', inst_id_lookup[12], inst_id_lookup[12], -30, 'OVERDUE'),
]
emit("INSERT INTO deadlines (")
emit("    id, deadline_code, title, description, deadline_type, statutory_clause,")
emit("    entity_type, entity_id, institution_id, target_date, grace_period_days, status, reminder_schedule_json, version")
emit(") VALUES")
for idx, (dl_id, dcode, title, desc, dtype, cl, etype, eid, inst_id, t_days, st) in enumerate(deadlines_data):
    term = ";" if idx == len(deadlines_data) - 1 else ","
    t_sql = f"CURRENT_TIMESTAMP + INTERVAL '{t_days}' DAY" if t_days >= 0 else f"CURRENT_TIMESTAMP - INTERVAL '{abs(t_days)}' DAY"
    emit(f"    ('{dl_id}', '{dcode}', '{title}', '{desc}', '{dtype}', '{cl}', '{etype}', '{eid}', '{inst_id}', {t_sql}, 0, '{st}', '[-15, -7, -3, 0]', 0){term}")
emit("")

# 20. Alerts (32 records)
emit("-- 20. Institutional & Regulatory Alerts")
alerts_data = [
    # Inst 1
    ('aaaaaaaa-2000-0000-0000-000000000001', 'ALR_FIRE_EXP_DEL', 'Fire Safety Certificate Expired', 'Delhi Tech campus Fire Safety Certificate has expired. Immediate replacement upload required.', 'COMPLIANCE', 'CRITICAL', 'URGENT', 'DOCUMENT', doc_id_map[60], inst_id_lookup[1], inst_officer_lookup[1], 'ROLE_INSTITUTION_OFFICER', 'Delhi', 'UNREAD'),
    ('aaaaaaaa-2000-0000-0000-000000000002', 'ALR_SCRUT_DEF_DEL', 'Scrutiny Deficiency Issued', 'Application APP-2025-DEL-0103 has open scrutiny findings regarding lab testbench calibration.', 'WORKFLOW', 'WARNING', 'HIGH', 'APPLICATION', 'bbbbbbbb-0001-0000-0000-000000000103', inst_id_lookup[1], inst_officer_lookup[1], 'ROLE_INSTITUTION_OFFICER', 'Delhi', 'UNREAD'),
    ('aaaaaaaa-2000-0000-0000-000000000003', 'ALR_EVC_PROG_DEL', 'EVC Inspection In Progress', 'Physical visit committee is currently inspecting Delhi Tech campus for AI & ML course approval.', 'WORKFLOW', 'INFO', 'MEDIUM', 'APPLICATION', 'bbbbbbbb-0001-0000-0000-000000000102', inst_id_lookup[1], None, 'ROLE_REGIONAL_OFFICER', 'Delhi', 'READ'),

    # Inst 2
    ('aaaaaaaa-2000-0000-0000-000000000004', 'ALR_FIRE_EXP_MUM', 'Fire Safety Certificate Expired', 'Mumbai Academy campus fire certificate has expired. Corrective action in progress.', 'COMPLIANCE', 'CRITICAL', 'URGENT', 'DOCUMENT', doc_id_map[61], inst_id_lookup[2], inst_officer_lookup[2], 'ROLE_INSTITUTION_OFFICER', 'Maharashtra', 'ACKNOWLEDGED'),
    ('aaaaaaaa-2000-0000-0000-000000000005', 'ALR_EOA_APP_MUM', 'Extension of Approval Granted', 'Application APP-2025-MUM-0201 for Extension of Approval 2025-26 has been approved.', 'WORKFLOW', 'INFO', 'LOW', 'APPLICATION', 'bbbbbbbb-0002-0000-0000-000000000201', inst_id_lookup[2], inst_officer_lookup[2], 'ROLE_INSTITUTION_OFFICER', 'Maharashtra', 'READ'),

    # Inst 3
    ('aaaaaaaa-2000-0000-0000-000000000006', 'ALR_FIRE_SOON_BEN', 'Fire Safety Certificate Expiring in 14 Days', 'Bengaluru Polytechnic fire clearance requires renewal prior to expiry.', 'COMPLIANCE', 'WARNING', 'HIGH', 'DOCUMENT', doc_id_map[62], inst_id_lookup[3], inst_officer_lookup[3], 'ROLE_INSTITUTION_OFFICER', 'Karnataka', 'UNREAD'),
    ('aaaaaaaa-2000-0000-0000-000000000007', 'ALR_EVC_ALLOC_BEN', 'EVC Inspection Pending Panel Allocation', 'Application APP-2025-BEN-0302 requires Expert Visit Committee scheduling.', 'WORKFLOW', 'INFO', 'MEDIUM', 'APPLICATION', 'bbbbbbbb-0003-0000-0000-000000000302', inst_id_lookup[3], None, 'ROLE_EVC_COORDINATOR', 'Karnataka', 'UNREAD'),

    # Inst 4
    ('aaaaaaaa-2000-0000-0000-000000000008', 'ALR_FIRE_SOON_PUN', 'Fire Safety Certificate Expiring Soon', 'Pune NIMR fire protection statutory roster due for renewal in 20 days.', 'COMPLIANCE', 'WARNING', 'MEDIUM', 'DOCUMENT', doc_id_map[63], inst_id_lookup[4], inst_officer_lookup[4], 'ROLE_INSTITUTION_OFFICER', 'Maharashtra', 'UNREAD'),
    ('aaaaaaaa-2000-0000-0000-000000000009', 'ALR_SCRUT_PASS_PUN', 'Scrutiny Successfully Passed', 'Application APP-2025-PUN-0401 has cleared scrutiny checklists with 100% compliance.', 'WORKFLOW', 'INFO', 'LOW', 'APPLICATION', 'bbbbbbbb-0004-0000-0000-000000000401', inst_id_lookup[4], inst_officer_lookup[4], 'ROLE_INSTITUTION_OFFICER', 'Maharashtra', 'READ'),

    # Inst 5
    ('aaaaaaaa-2000-0000-0000-000000000010', 'ALR_EVC_SCHED_CHE', 'EVC Inspection Scheduled', 'Change of Site physical visit scheduled for Day 8-9 by Chairperson Prof. B.K. Sengupta.', 'WORKFLOW', 'INFO', 'HIGH', 'APPLICATION', 'bbbbbbbb-0005-0000-0000-000000000502', inst_id_lookup[5], inst_officer_lookup[5], 'ROLE_INSTITUTION_OFFICER', 'Tamil Nadu', 'UNREAD'),
    ('aaaaaaaa-2000-0000-0000-000000000011', 'ALR_FIRE_SOON_CHE', 'Fire Safety Renewal Notice', 'Chennai Arch campus fire certificate due for renewal in 25 days.', 'COMPLIANCE', 'WARNING', 'LOW', 'DOCUMENT', doc_id_map[64], inst_id_lookup[5], inst_officer_lookup[5], 'ROLE_INSTITUTION_OFFICER', 'Tamil Nadu', 'READ'),

    # Inst 6
    ('aaaaaaaa-2000-0000-0000-000000000012', 'ALR_FIRE_CRIT_BHO', 'Critical: Fire Safety Expired 90 Days Ago', 'Bhopal Central Computing campus fire certificate is severely overdue. Compliance action required.', 'COMPLIANCE', 'CRITICAL', 'URGENT', 'DOCUMENT', doc_id_map[65], inst_id_lookup[6], None, 'ROLE_COMPLIANCE_OFFICER', 'Madhya Pradesh', 'UNREAD'),
    ('aaaaaaaa-2000-0000-0000-000000000013', 'ALR_EVC_DONE_BHO', 'EVC Inspection Completed', 'Inspection report for Data Science course completed and signed by Prof. Kejriwal.', 'WORKFLOW', 'INFO', 'MEDIUM', 'APPLICATION', 'bbbbbbbb-0006-0000-0000-000000000602', inst_id_lookup[6], inst_officer_lookup[6], 'ROLE_INSTITUTION_OFFICER', 'Madhya Pradesh', 'READ'),

    # Inst 7
    ('aaaaaaaa-2000-0000-0000-000000000014', 'ALR_FIRE_RECT_JAI', 'Fire Safety Renewal Resolved', 'Replacement fire safety NOC v2 accepted and verified for Rajasthan College.', 'COMPLIANCE', 'INFO', 'LOW', 'APPLICATION', 'bbbbbbbb-0007-0000-0000-000000000701', inst_id_lookup[7], inst_officer_lookup[7], 'ROLE_INSTITUTION_OFFICER', 'Rajasthan', 'RESOLVED'),
    ('aaaaaaaa-2000-0000-0000-000000000015', 'ALR_EVC_PROG_JAI', 'EVC Inspection In Progress', 'Physical inspection in progress for Robotics & Automation degree introduction.', 'WORKFLOW', 'INFO', 'MEDIUM', 'APPLICATION', 'bbbbbbbb-0007-0000-0000-000000000702', inst_id_lookup[7], None, 'ROLE_REGIONAL_OFFICER', 'Rajasthan', 'READ'),

    # Inst 8
    ('aaaaaaaa-2000-0000-0000-000000000016', 'ALR_EVC_SCHED_DEH', 'New Institution EVC Visit Scheduled', '3-member inspection team confirmed for Himalayan Institute visit on Day 12-13.', 'WORKFLOW', 'INFO', 'HIGH', 'APPLICATION', 'bbbbbbbb-0008-0000-0000-000000000801', inst_id_lookup[8], inst_officer_lookup[8], 'ROLE_INSTITUTION_OFFICER', 'Uttarakhand', 'UNREAD'),
    ('aaaaaaaa-2000-0000-0000-000000000017', 'ALR_LAB_DEF_DEH', 'Equipment Invoice Clarification Required', 'Digital Forensics simulation rig purchase invoices pending validation.', 'WORKFLOW', 'WARNING', 'HIGH', 'APPLICATION', 'bbbbbbbb-0008-0000-0000-000000000803', inst_id_lookup[8], inst_officer_lookup[8], 'ROLE_INSTITUTION_OFFICER', 'Uttarakhand', 'UNREAD'),
    ('aaaaaaaa-2000-0000-0000-000000000018', 'ALR_FIRE_EXP_DEH', 'Fire Safety Certificate Expired', 'Selaqui Industrial fire certificate expired 15 days ago.', 'COMPLIANCE', 'CRITICAL', 'URGENT', 'DOCUMENT', doc_id_map[67], inst_id_lookup[8], None, 'ROLE_COMPLIANCE_OFFICER', 'Uttarakhand', 'UNREAD'),

    # Inst 9
    ('aaaaaaaa-2000-0000-0000-000000000019', 'ALR_FIRE_EXP_VIZ', 'Fire Safety Certificate Expired', 'Vizag campus fire safety certificate expired 35 days ago. Corrective action overdue.', 'COMPLIANCE', 'CRITICAL', 'URGENT', 'DOCUMENT', doc_id_map[68], inst_id_lookup[9], inst_officer_lookup[9], 'ROLE_INSTITUTION_OFFICER', 'Andhra Pradesh', 'UNREAD'),
    ('aaaaaaaa-2000-0000-0000-000000000020', 'ALR_EOA_APP_VIZ', 'Extension of Approval Granted', 'Application APP-2025-VIZ-0901 approved for academic session 2025-26.', 'WORKFLOW', 'INFO', 'LOW', 'APPLICATION', 'bbbbbbbb-0009-0000-0000-000000000901', inst_id_lookup[9], inst_officer_lookup[9], 'ROLE_INSTITUTION_OFFICER', 'Andhra Pradesh', 'READ'),

    # Inst 10
    ('aaaaaaaa-2000-0000-0000-000000000021', 'ALR_FIRE_SOON_HYD', 'Fire Safety Expiring in 22 Days', 'Hyderabad Gachibowli campus fire safety audit due for renewal.', 'COMPLIANCE', 'WARNING', 'HIGH', 'DOCUMENT', doc_id_map[69], inst_id_lookup[10], inst_officer_lookup[10], 'ROLE_INSTITUTION_OFFICER', 'Telangana', 'UNREAD'),
    ('aaaaaaaa-2000-0000-0000-000000000022', 'ALR_SCRUT_PEND_HYD', 'Scrutiny Verification in Queue', 'New Programme application APP-2025-HYD-1002 in regional scrutiny queue.', 'WORKFLOW', 'INFO', 'LOW', 'APPLICATION', 'bbbbbbbb-0010-0000-0000-000000001002', inst_id_lookup[10], None, 'ROLE_REGIONAL_OFFICER', 'Telangana', 'READ'),

    # Inst 11
    ('aaaaaaaa-2000-0000-0000-000000000023', 'ALR_EVC_DONE_GUR', 'New Institution Inspection Completed', 'Physical campus verification completed by Dr. Satish Chandra committee.', 'WORKFLOW', 'INFO', 'HIGH', 'APPLICATION', 'bbbbbbbb-0011-0000-0000-000000001101', inst_id_lookup[11], inst_officer_lookup[11], 'ROLE_INSTITUTION_OFFICER', 'Haryana', 'READ'),
    ('aaaaaaaa-2000-0000-0000-000000000024', 'ALR_EDA_RECT_GUR', 'EDA Tool License Resolved', 'Cadence EDA bundle verification completed. Course scrutiny cleared.', 'WORKFLOW', 'INFO', 'LOW', 'APPLICATION', 'bbbbbbbb-0011-0000-0000-000000001102', inst_id_lookup[11], inst_officer_lookup[11], 'ROLE_INSTITUTION_OFFICER', 'Haryana', 'RESOLVED'),
    ('aaaaaaaa-2000-0000-0000-000000000025', 'ALR_FIRE_SOON_GUR', 'Fire Inspection Order Expiring Soon', 'Gurugram Sector 88 fire inspection order due in 18 days.', 'COMPLIANCE', 'WARNING', 'MEDIUM', 'DOCUMENT', doc_id_map[70], inst_id_lookup[11], inst_officer_lookup[11], 'ROLE_INSTITUTION_OFFICER', 'Haryana', 'UNREAD'),

    # Inst 12
    ('aaaaaaaa-2000-0000-0000-000000000026', 'ALR_REJECT_KOL', 'Application Rejected following Hearing', 'Extension of Approval APP-2025-KOL-1201 rejected due to expired fire clearance.', 'WORKFLOW', 'CRITICAL', 'URGENT', 'APPLICATION', 'bbbbbbbb-0012-0000-0000-000000001201', inst_id_lookup[12], inst_officer_lookup[12], 'ROLE_INSTITUTION_OFFICER', 'West Bengal', 'UNREAD'),
    ('aaaaaaaa-2000-0000-0000-000000000027', 'ALR_EVC_SCHED_KOL', 'Change of Site EVC Inspection Scheduled', 'High-rise campus site inspection scheduled for Day 15-16 by Prof. Sanjeev Kumar.', 'WORKFLOW', 'INFO', 'HIGH', 'APPLICATION', 'bbbbbbbb-0012-0000-0000-000000001202', inst_id_lookup[12], inst_officer_lookup[12], 'ROLE_INSTITUTION_OFFICER', 'West Bengal', 'UNREAD'),
    ('aaaaaaaa-2000-0000-0000-000000000028', 'ALR_FIRE_EXP_KOL', 'Fire Safety Certificate Severely Overdue', 'Eastern School fire certificate expired 80 days ago.', 'COMPLIANCE', 'CRITICAL', 'URGENT', 'DOCUMENT', doc_id_map[71], inst_id_lookup[12], None, 'ROLE_COMPLIANCE_OFFICER', 'West Bengal', 'UNREAD'),

    # Regulatory oversight alerts
    ('aaaaaaaa-2000-0000-0000-000000000029', 'ALR_NAT_SURV_01', 'National Compliance Sweep Alert', 'Continuous compliance engine detected 6 expired safety certificates nationwide.', 'COMPLIANCE', 'WARNING', 'HIGH', 'INSTITUTION', inst_id_lookup[1], inst_id_lookup[1], None, 'ROLE_AICTE_ADMIN', None, 'UNREAD'),
    ('aaaaaaaa-2000-0000-0000-000000000030', 'ALR_NAT_EVC_01', 'Pending Inspection Quorum Deficit', '3 technical institutions in pending queue ready for statutory panel allocation.', 'WORKFLOW', 'INFO', 'MEDIUM', 'APPLICATION', 'bbbbbbbb-0003-0000-0000-000000000302', inst_id_lookup[3], None, 'ROLE_EVC_COORDINATOR', None, 'UNREAD'),
    ('aaaaaaaa-2000-0000-0000-000000000031', 'ALR_REG_NORTH_01', 'Northern Region Scrutiny Queue Notice', '5 applications in Northern Region awaiting scrutiny officer clearance.', 'WORKFLOW', 'INFO', 'MEDIUM', 'APPLICATION', 'bbbbbbbb-0001-0000-0000-000000000102', inst_id_lookup[1], None, 'ROLE_REGIONAL_OFFICER', 'Delhi', 'UNREAD'),
    ('aaaaaaaa-2000-0000-0000-000000000032', 'ALR_REG_WEST_01', 'Western Region Deficiency Notice Review', 'Rajasthan College replacement fire certificate awaiting regional confirmation.', 'WORKFLOW', 'INFO', 'MEDIUM', 'APPLICATION', 'bbbbbbbb-0007-0000-0000-000000000701', inst_id_lookup[7], None, 'ROLE_REGIONAL_OFFICER', 'Rajasthan', 'UNREAD'),
]

emit("INSERT INTO alerts (")
emit("    id, alert_code, title, message, category, severity, priority,")
emit("    source_entity_type, source_entity_id, institution_id, recipient_user_id,")
emit("    target_role, jurisdiction_state, status, is_escalated, deduplication_hash")
emit(") VALUES")
for idx, (alid, acode, atitle, amsg, acat, asev, aprio, stype, sid, inst_id, recip, trole, jstate, st) in enumerate(alerts_data):
    term = ";" if idx == len(alerts_data) - 1 else ","
    recip_sql = f"'{recip}'" if recip else "NULL"
    trole_sql = f"'{trole}'" if trole else "NULL"
    jstate_sql = f"'{jstate}'" if jstate else "NULL"
    dedup = f"HASH:{acode}:{sid}:{idx}"
    emit(f"    ('{alid}', '{acode}', '{atitle}', '{amsg}', '{acat}', '{asev}', '{aprio}', '{stype}', '{sid}', '{inst_id}', {recip_sql}, {trole_sql}, {jstate_sql}, '{st}', FALSE, '{dedup}'){term}")
emit("")

# 21. Audit Trail Events (50+ records)
emit("-- 21. Immutable Regulatory Audit Trail")
emit("INSERT INTO audit_events (")
emit("    id, event_type, principal, institution_id, resource_type, resource_id, action, client_ip, correlation_id, details, created_at")
emit(") VALUES")
audit_list = []
audit_counter = 1

principals = [
    ('admin@aicte-demo.gov.in', 'AICTE Central Administrator'),
    ('regional.north@aicte-demo.gov.in', 'Northern Regional Director'),
    ('regional.west@aicte-demo.gov.in', 'Western Regional Director'),
    ('compliance.officer1@aicte-demo.gov.in', 'Compliance Surveillance Officer'),
    ('auditor@aicte-demo.gov.in', 'Senior Statutory Auditor'),
    ('evc.coord1@aicte-demo.gov.in', 'EVC Operations Coordinator'),
]

# Generate audit events for applications
for app_id, num, inst_id, yr, cat, wf_id, st, assigned, created, sub_days, dec_days in applications_data:
    aid_evt = f"aaaaaaaa-3000-0000-0000-{audit_counter:012d}"
    audit_counter += 1
    c_time = f"CURRENT_TIMESTAMP - INTERVAL '{sub_days or 10}' DAY"
    audit_list.append((aid_evt, 'APPLICATION_CREATED', 'officer@institution.edu', inst_id, 'APPLICATION', app_id, 'CREATE', '10.0.1.15', f"c-app-init-{audit_counter}", f"Created approval application proposal {num} ({cat})", c_time))

    if sub_days:
        aid_evt2 = f"aaaaaaaa-3000-0000-0000-{audit_counter:012d}"
        audit_counter += 1
        audit_list.append((aid_evt2, 'APPLICATION_SUBMITTED', 'officer@institution.edu', inst_id, 'APPLICATION', app_id, 'SUBMIT', '10.0.1.15', f"c-app-sub-{audit_counter}", f"Submitted proposal {num} with statutory declaration", c_time))

# Generate audit events for documents
for idx in range(min(15, len(doc_specs))):
    aid_doc = f"aaaaaaaa-3000-0000-0000-{audit_counter:012d}"
    audit_counter += 1
    did = doc_id_map[idx]
    inst_idx = doc_specs[idx][0]
    c_time = f"CURRENT_TIMESTAMP - INTERVAL '{30 - idx}' DAY"
    audit_list.append((aid_doc, 'DOCUMENT_UPLOADED', 'officer@institution.edu', inst_id_lookup[inst_idx], 'DOCUMENT', did, 'UPLOAD', '10.0.1.20', f"c-doc-up-{audit_counter}", f"Uploaded statutory compliance document {doc_specs[idx][4]}", c_time))

for idx, (ea_id, ev_type, princ, i_id, rtype, rid, act, ip, cid, dtl, c_time) in enumerate(audit_list):
    term = ";" if idx == len(audit_list) - 1 else ","
    emit(f"    ('{ea_id}', '{ev_type}', '{princ}', '{i_id}', '{rtype}', '{rid}', '{act}', '{ip}', '{cid}', '{dtl}', {c_time}){term}")
emit("")

emit("-- End of Comprehensive Relational Demonstration Dataset")

with open(SEED_SQL_PATH, 'w', encoding='utf-8') as f:
    f.write("\n".join(sql_lines) + "\n")

print(f"Successfully generated {len(sql_lines)} SQL lines in {SEED_SQL_PATH}")
print(f"Generated {len(doc_specs)} physical PDF files in {DOCS_DIR}")
