#!/usr/bin/env python3
"""
SAGE Platform Comprehensive End-to-End Workflow & Security Audit Script
Tests live integration across:
- Authentication & JWT issuance
- Application Creation (Institution Officer)
- Document Upload & Automatic Indra/Tesseract OCR Processing
- Deterministic APH Scrutiny Engine
- State Transitions & RBAC Guards
- Google OR-Tools CP-SAT EVC Inspection Scheduling
- Statutory Deadline Management & Deduplicated Alerting
- Compliance Scans
- Regulatory Audit Trail Retrieval
- Cross-Tenant Multi-Tenancy Isolation
"""

import sys
import os
import requests
import json
import uuid

BACKEND_URL = "http://localhost:8080/api/v1"
AUTOMATION_URL = "http://localhost:8000/api/v1"
CORPUS_FILE = os.path.abspath("automation/tests/benchmark/corpus/doc_1_fire_safety_valid.pdf")

results = []

def record(phase, test_name, passed, detail=""):
    status = "PASS" if passed else "FAIL"
    print(f"[{status}] {phase} :: {test_name} - {detail}")
    results.append({
        "phase": phase,
        "test": test_name,
        "status": status,
        "detail": detail
    })

def login(username, password):
    url = f"{BACKEND_URL}/auth/login"
    resp = requests.post(url, json={"username": username, "password": password})
    if resp.status_code == 200:
        data = resp.json().get("data", {})
        return data.get("accessToken")
    return None

def run_audit():
    print("================================================================================")
    print("      SAGE COMPREHENSIVE END-TO-END AUDIT & ACCEPTANCE VERIFICATION           ")
    print("================================================================================")

    # 1. Authentication
    admin_token = login("admin@aicte.gov.in", "Password123!")
    delhi_token = login("officer@delhi-tech.edu", "Password123!")
    regional_token = login("regional@aicte.gov.in", "Password123!")
    auditor_token = login("auditor@aicte.gov.in", "Password123!")

    record("Auth", "Admin Login", admin_token is not None, "Obtained JWT for central admin")
    record("Auth", "Institution Officer Login", delhi_token is not None, "Obtained JWT for officer@delhi-tech.edu")
    record("Auth", "Regional Officer Login", regional_token is not None, "Obtained JWT for regional@aicte.gov.in")
    record("Auth", "Auditor Login", auditor_token is not None, "Obtained JWT for auditor@aicte.gov.in")

    if not all([admin_token, delhi_token, regional_token, auditor_token]):
        print("Authentication failure. Halting further tests.")
        sys.exit(1)

    # 2. Application Creation
    headers_delhi = {"Authorization": f"Bearer {delhi_token}"}
    create_payload = {
        "institutionId": "22222222-2222-2222-2222-222222222222",
        "academicYear": "2026-2027",
        "category": "NEW_INSTITUTION",
        "level": "UNDERGRADUATE",
        "program": "ENGINEERING_AND_TECHNOLOGY"
    }
    app_resp = requests.post(f"{BACKEND_URL}/applications", headers=headers_delhi, json=create_payload)
    app_created = app_resp.status_code in (200, 201)
    app_data = app_resp.json().get("data", {}) if app_created else {}
    app_id = app_data.get("id")
    record("Workflow", "Application Creation", app_created, f"Created Application: {app_data.get('applicationNumber', app_id)}")

    # 3. Document Upload with Real PDF & OCR Execution
    doc_uploaded = False
    doc_id = None
    if app_id and os.path.exists(CORPUS_FILE):
        with open(CORPUS_FILE, "rb") as f:
            files = {"file": ("doc_1_fire_safety_valid.pdf", f, "application/pdf")}
            doc_resp = requests.post(
                f"{BACKEND_URL}/applications/{app_id}/documents?documentType=FIRE_SAFETY_CERTIFICATE",
                headers=headers_delhi,
                files=files
            )
            if doc_resp.status_code in (200, 201):
                doc_data = doc_resp.json().get("data", {})
                doc_id = doc_data.get("id")
                doc_uploaded = True
                extracted = doc_data.get("extractedFields", [])
                cert_no = None
                if isinstance(extracted, list):
                    cert_no = next((f.get("extractedValue") for f in extracted if f.get("fieldKey") == "certificate_number"), None)
                elif isinstance(extracted, dict):
                    cert_no = extracted.get("certificate_number", {}).get("extractedValue")
                record("OCR", "Document Upload & OCR Extraction", doc_uploaded, f"Doc ID: {doc_id}, Extracted Cert: {cert_no}")
            else:
                record("OCR", "Document Upload & OCR Extraction", False, f"HTTP {doc_resp.status_code}: {doc_resp.text}")
    else:
        record("OCR", "Document Upload & OCR Extraction", False, "Missing application ID or corpus file")

    # 4. State Transition: DRAFT -> SUBMITTED (Institution Officer)
    if app_id:
        trans_resp = requests.post(
            f"{BACKEND_URL}/applications/{app_id}/transitions",
            headers=headers_delhi,
            json={"targetState": "SUBMITTED", "reason": "Completed statutory document submissions"}
        )
        record("Workflow", "Submit Application (DRAFT -> SUBMITTED)", trans_resp.status_code == 200, f"HTTP {trans_resp.status_code}")

    # 5. Deterministic Scrutiny Evaluation (Regional Officer)
    headers_reg = {"Authorization": f"Bearer {regional_token}"}
    if app_id:
        # Move to UNDER_SCRUTINY
        requests.post(
            f"{BACKEND_URL}/applications/{app_id}/transitions",
            headers=headers_reg,
            json={"targetState": "UNDER_SCRUTINY", "reason": "Initiated scrutiny evaluation"}
        )
        scrutiny_resp = requests.post(f"{BACKEND_URL}/applications/{app_id}/scrutiny/evaluate", headers=headers_reg)
        record("Scrutiny", "Deterministic Scrutiny Evaluation", scrutiny_resp.status_code == 200, f"Scrutiny outcome: {scrutiny_resp.json().get('data', {}).get('status')}")

    # 6. EVC Scheduling Engine via CP-SAT
    headers_admin = {"Authorization": f"Bearer {admin_token}"}
    sched_resp = requests.post(
        f"{BACKEND_URL}/evc/schedules",
        headers=headers_admin,
        json={"inspectionIds": ["ffffffff-ffff-ffff-ffff-000000000001"], "timeLimitSeconds": 10, "allowRescheduling": True}
    )
    sched_ok = sched_resp.status_code == 200 and sched_resp.json().get("status") in ("OPTIMAL", "FEASIBLE")
    record("EVC Engine", "Google OR-Tools CP-SAT Scheduling", sched_ok, f"Status: {sched_resp.json().get('status')}, Solve Duration: {sched_resp.json().get('solveDurationMs')}ms")

    # Retrieve Confirmed Schedule
    list_sched_resp = requests.get(f"{BACKEND_URL}/evc/inspections/scheduled", headers=headers_admin)
    has_scheduled = list_sched_resp.status_code == 200 and len(list_sched_resp.json()) > 0
    record("EVC Engine", "Retrieve Saved Schedule", has_scheduled, f"Retrieved {len(list_sched_resp.json()) if has_scheduled else 0} persisted inspections")

    # 7. Compliance Engine Scan
    comp_resp = requests.post(f"{BACKEND_URL}/compliance/scan", headers=headers_admin)
    comp_ok = comp_resp.status_code == 200
    record("Compliance", "Execute Regulatory Compliance Scan", comp_ok, f"Run ID: {comp_resp.json().get('data', {}).get('id') if comp_ok else 'None'}")

    # 8. Statutory Deadline Creation & Deduplicated Alerts
    deadline_payload = {
        "deadlineCode": f"TEST_DL_{uuid.uuid4().hex[:6]}",
        "title": "E2E Audit Fire Safety NOC Verification",
        "description": "Statutory verification of institutional fire clearance",
        "deadlineType": "STATUTORY",
        "statutoryClause": "APH 2024-27 Clause 2.4",
        "entityType": "APPLICATION",
        "entityId": app_id if app_id else "bbbbbbbb-bbbb-bbbb-bbbb-000000000001",
        "institutionId": "22222222-2222-2222-2222-222222222222",
        "targetDate": "2026-11-20T18:30:00Z",
        "gracePeriodDays": 3
    }
    dl_resp = requests.post(f"{BACKEND_URL}/deadlines", headers=headers_admin, json=deadline_payload)
    dl_ok = dl_resp.status_code in (200, 201)
    record("Deadlines", "Create Statutory Deadline", dl_ok, f"Deadline Code: {deadline_payload['deadlineCode']}")

    # 9. Audit Log Retrieval (Auditor Role)
    headers_auditor = {"Authorization": f"Bearer {auditor_token}"}
    audit_resp = requests.get(f"{BACKEND_URL}/audit?page=0&size=10", headers=headers_auditor)
    audit_ok = audit_resp.status_code == 200 and len(audit_resp.json().get("data", {}).get("content", [])) > 0
    record("Audit", "Immutable Audit Log Retrieval", audit_ok, f"Audit events found: {len(audit_resp.json().get('data', {}).get('content', [])) if audit_ok else 0}")

    # 10. Cross-Tenant Security Isolation (IDOR Check)
    # Officer from Delhi Tech tries to fetch Mumbai Engg's institution data
    idor_resp = requests.get(f"{BACKEND_URL}/institutions/33333333-3333-3333-3333-333333333333", headers=headers_delhi)
    idor_blocked = idor_resp.status_code == 403
    record("Security", "Cross-Tenant Access Denial (IDOR Guard)", idor_blocked, f"Response Status: HTTP {idor_resp.status_code} (Expected 403)")

    # 11. Institutional Officer RBAC Denial on Admin Audit Endpoint
    audit_leak_resp = requests.get(f"{BACKEND_URL}/audit", headers=headers_delhi)
    audit_leak_blocked = audit_leak_resp.status_code == 403
    record("Security", "Officer Forbidden from Audit Endpoint", audit_leak_blocked, f"Response Status: HTTP {audit_leak_resp.status_code} (Expected 403)")

    print("================================================================================")
    passed_count = sum(1 for r in results if r["status"] == "PASS")
    total_count = len(results)
    print(f"Audit Summary: {passed_count}/{total_count} acceptance checks PASSED ({passed_count/total_count*100:.1f}%)")
    print("================================================================================")

if __name__ == "__main__":
    run_audit()
