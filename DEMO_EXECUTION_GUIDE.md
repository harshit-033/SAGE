# SAGE — Demonstration Execution Guide

**System:** SAGE (System for Approval Governance and Evaluation)  
**Smart India Hackathon (SIH) 2026** — Problem Statement 26262  
**Purpose:** Operational walkthrough for running and demonstrating the SAGE prototype live.

---

## 1. Quick Start & Prerequisites

### System Requirements
- **Java**: OpenJDK 21 or higher
- **Node.js**: Node 20.x or higher with `npm`
- **Python**: Python 3.12 with `venv` and `pip`
- **Docker**: Docker Engine 24+ & Docker Compose v2 (optional for containerized demo)
- **Tesseract OCR** (optional, recommended for physical OCR): `tesseract-ocr` package

---

## 2. Launching SAGE

### Option A — Full Native Stack (Recommended for Development & Live Demos)

1. **Seed Demo Data & Sample PDFs**:
   ```bash
   ./scripts/seed-demo.sh
   ```
   *Seeds 744 relational records across 8 colleges, 19 users, 36 applications, and generates 72 valid physical PDF certificates in `backend/data/documents`.*

2. **Start All Services with One Command**:
   ```bash
   ./run.sh
   ```
   *Spawns PostgreSQL (or starts in-memory H2 in dev profile), FastAPI Python automation service on port 8000, Spring Boot backend on port 8080, and Vite React frontend on port 5173.*

3. **Or Run Services in Separate Terminals**:
   - **Terminal 1 (Backend)**:
     ```bash
     cd backend
     ./mvnw spring-boot:run
     ```
     *Runs Spring Boot 3.3 on `http://localhost:8080`.*
   - **Terminal 2 (Automation Service)**:
     ```bash
     cd automation
     source venv/bin/activate
     uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
     ```
     *Runs FastAPI on `http://localhost:8000`.*
   - **Terminal 3 (Frontend)**:
     ```bash
     cd frontend
     npm run dev
     ```
     *Runs Vite React SPA on `http://localhost:5173`.*

---

### Option B — Containerized Demonstration (Docker Compose)

To run the entire multi-container environment (PostgreSQL, Spring Boot backend, FastAPI automation, and Nginx React frontend):

```bash
cd infrastructure
docker compose up --build -d
```

- **Frontend Application**: `http://localhost:5173`
- **Backend API & Swagger**: `http://localhost:8080/swagger-ui/index.html`
- **FastAPI Automation Docs**: `http://localhost:8000/docs`

To stop the containers:
```bash
docker compose down -v
```

---

## 3. Demonstration Accounts & Role Credentials

All demonstration accounts use the common secure password:
```text
Password123!
```

### Institutional & Regulatory Roles Matrix

| Email | Role | Scope / Jurisdiction | Demonstration Focus |
| :--- | :--- | :--- | :--- |
| `admin@aicte-demo.gov.in` | **Central AICTE Admin** | Nationwide (All Institutions) | System health, full audit trail, nationwide institutional registry, global overrides |
| `regional.north@aicte-demo.gov.in` | **Regional Officer** | Northern Region (UP, UK, DL, HR) | Scrutiny review, deficiency issuance, fast-track EoA approvals, regional data isolation |
| `regional.west@aicte-demo.gov.in` | **Regional Officer** | Western Region (MH, GJ, RJ) | Regional isolation verification (cannot view Northern colleges) |
| `officer@delhi-tech.edu` | **Institution Officer** | Delhi Institute of Technology | New course applications, deficiency rectification, resubmission, voluntary withdrawal |
| `officer@mumbai-engg.edu` | **Institution Officer** | Mumbai Engineering Institute | Extension of Approval (EoA) proposal, document upload, institutional compliance dashboard |
| `compliance.officer1@aicte-demo.gov.in` | **Compliance Officer** | Nationwide Compliance Hub | Safety audits, fire safety certificates, building occupancy verification, corrective action review |
| `evc.coord1@aicte-demo.gov.in` | **EVC Coordinator** | Expert Visit Committee Hub | OR-Tools CP-SAT committee scheduling, Sunday exclusion, peer evaluator conflict resolution |
| `auditor@aicte-demo.gov.in` | **Statutory Auditor** | Nationwide Audit Trail | Read-only tamper-evident regulatory event logs, filterable by date, action, and correlation ID |

---

## 4. Step-by-Step Demonstration Workflows

### Scenario 1: Strict Multi-Tenancy & Data Isolation (P0 Proof)
1. Open `http://localhost:5173` and log in as `officer@delhi-tech.edu` (`Password123!`).
2. Navigate to **Applications**. Observe that ONLY Delhi Institute of Technology applications appear (no Mumbai or Rajasthan records).
3. Attempt to navigate directly to Mumbai's application URL (`/applications/bbbbbbbb-0002-0000-0000-000000000201`).
4. **Result**: The system immediately blocks access, displaying an official statutory `Access Denied` error modal. The backend records a `TENANCY_VIOLATION_ATTEMPT` audit event.
5. Log out and log in as `regional.north@aicte-demo.gov.in`.
6. Navigate to **Applications**. Observe applications from Delhi, UP, Uttarakhand, and Haryana.
7. Observe that Western Region institutions (Mumbai) are strictly excluded from the list.

### Scenario 2: End-to-End Application Workflow & Scrutiny
1. Log in as `officer@delhi-tech.edu`.
2. Navigate to **Applications** -> Click **Initiate Application**.
3. Select **New Course Introduction**, enter Academic Year `2025-26`, and submit.
4. Open the created draft. Upload a sample Fire Safety Certificate (`.pdf`).
5. Click **Submit Application**. The status transitions from `DRAFT` to `SUBMITTED`.
6. Log out and log in as `regional.north@aicte-demo.gov.in`.
7. Open the submitted application. Click **Evaluate Scrutiny Rules**.
8. The deterministic scrutiny engine executes against APH 2024-27 norms, verifying certificates, land area, and institutional concordance.
9. Click **Advance Workflow** -> select `UNDER_SCRUTINY` or `SCRUTINY_PASSED`.

### Scenario 3: Deficiency Rectification & Resubmission
1. As `regional.north@aicte-demo.gov.in`, open application `APP-2025-DEL-0103` (currently in `DEFICIENT` state).
2. Note the flagged deficiency regarding expired Fire Safety Certificate.
3. Log in as `officer@delhi-tech.edu`.
4. Open `APP-2025-DEL-0103`. Observe the prominent **DEFICIENT** badge and the available **Resubmit Rectified Application** action.
5. In the Documents tab, click **Replace** on the Fire Safety Certificate and upload a valid renewed PDF.
6. Click **Resubmit Rectified Application**.
7. The application transitions cleanly from `DEFICIENT` to `RESUBMITTED` with audit entry.

### Scenario 4: EVC CP-SAT Expert Panel Scheduling
1. Log in as `evc.coord1@aicte-demo.gov.in`.
2. Navigate to **EVC Scheduling** (`/evc-scheduling`).
3. Select pending inspections (e.g. Bengaluru / Chennai technical institutes).
4. Review the candidate peer experts registry, disciplines, and availability calendar.
5. Click **Run Statutory Allocation Engine**.
6. The Google OR-Tools CP-SAT solver computes an optimal schedule enforcing:
   - 3 conflict-free expert evaluators per committee.
   - Consecutive 2-day inspection windows.
   - Strict Sunday exclusions.
   - Institutional non-conflict isolation.
7. Click **Confirm & Lock Schedule** to persist the committee appointments.

### Scenario 5: Document Compliance Hub & Expiry Surveillance
1. Log in as `compliance.officer1@aicte-demo.gov.in`.
2. Navigate to **Compliance Tracking** (`/compliance`).
3. Review authoritative metrics: Total Registered, Verified, Pending Review, Expiring Soon, and Deficient.
4. Filter by status: `Expiring Soon` or `Expired`.
5. Open an expiring Fire Safety Certificate. Click **Preview** to view the document inline.
6. Open an active **Corrective Action** requiring building stability certification, review institutional justification notes, and verify resolution.

### Scenario 6: Immutable Regulatory Audit Trail
1. Log in as `auditor@aicte-demo.gov.in` (or `admin@aicte-demo.gov.in`).
2. Navigate to **Audit Trail** (`/audit-trail`).
3. Observe chronological immutable logs of all system events (`AUTH_LOGIN_SUCCESS`, `APPLICATION_CREATED`, `WORKFLOW_STATE_TRANSITION`, `TENANCY_VIOLATION_ATTEMPT`, `EVC_SCHEDULE_PERSISTED`).
4. Filter by Action type or search by Correlation ID.
5. Verify that audit events contain non-repudiable timestamps, user principal emails, and client IP addresses.

---

## 5. Verification Checklist for Evaluators

- [x] All 6 roles have dedicated dashboards with tailored operational widgets.
- [x] Multi-tenancy strictly prevents cross-college and cross-regional data leakage.
- [x] Document upload performs binary signature (magic byte) verification.
- [x] FastAPI OCR service provides automated field extraction with Tesseract fallback.
- [x] EVC Scheduling uses genuine OR-Tools CP-SAT discrete constraint solving.
- [x] Complete workflow state machine prevents premature approvals per APH norms.
- [x] 100% of frontend tests (58/58) and backend tests (99/99) pass.
