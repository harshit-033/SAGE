# SAGE — Demonstration Scenarios Script

**System:** SAGE (System for Approval Governance and Evaluation)  
**Hackathon:** Smart India Hackathon (SIH) 2026 — Problem Statement 26262  
**Audience:** Technical Evaluators, Hackathon Jury, and Regulatory Stakeholders  

---

## Scenario A — Fast-Track Extension of Approval (EoA)

### Objective
Demonstrate the end-to-end institutional submission and regulatory evaluation of an Extension of Approval (EoA) application, illustrating deterministic scrutiny and regulatory fast-track approval per APH Chapter II norms.

### Step-by-Step Script

1. **Institutional Login & Draft Initiation**:
   - URL: `http://localhost:5173/login`
   - Sign in as `officer@mumbai-engg.edu` / `Password123!` (Western Institute of Technology & Management, Mumbai).
   - Navigate to **Applications** (`/applications`).
   - Click **Initiate Application**.
   - Fill in:
     - Application Category: `Extension of Approval (EoA)`
     - Academic Session: `2025-26`
   - Click **Create Draft Application**.
   - An application with number `APP-2025-MUM-XXXX` is initialized in `DRAFT` state.

2. **Document Upload & Binary Validation**:
   - In the application details view, switch to the **Documents** tab.
   - Select document type: `Fire Safety Certificate`.
   - Choose a sample valid certificate (`sample-fire-safety.pdf`).
   - Click **Upload & Process Document**.
   - The backend validates magic bytes (`%PDF`), writes the file to sandboxed storage with an immutable SHA-256 hash, and calls the FastAPI OCR service.
   - Observe extracted metadata: Issuing Authority, Valid From, Expiry Date, and Concordance Confidence.

3. **Institutional Submission**:
   - Click **Submit Application**.
   - Confirm submission in the dialog.
   - Status changes to `SUBMITTED`. Audit entry `APPLICATION_SUBMITTED` is recorded.
   - Log out.

4. **Regional Regulatory Scrutiny & Approval**:
   - Sign in as `regional.west@aicte-demo.gov.in` / `Password123!`.
   - Navigate to **Applications**. Locate `APP-2025-MUM-XXXX` under Western Region applications.
   - Open the application.
   - Click **Evaluate Scrutiny Rules**.
   - The rule engine runs against APH norms:
     - `RULE_APH_MANDATORY_DOCS` -> PASSED
     - `RULE_APH_FIRE_SAFETY` -> PASSED (covers academic session)
     - `RULE_APH_OCCUPANCY` -> PASSED
     - `RULE_APH_INST_NAME` -> PASSED
   - Click **Advance Workflow** -> Target State: `SCRUTINY_PASSED`. Justification: `"Scrutiny rules evaluated successfully without deficiency."`
   - Click **Advance Workflow** -> Target State: `APPROVED`.
   - Because EoA does not require mandatory EVC inspection or hearing under APH Chapter II, the Regional Officer successfully grants approval.

---

## Scenario B — Cross-Tenant Isolation Enforcement (P0 Security Proof)

### Objective
Prove that SAGE enforces fail-closed multi-tenant data isolation, strictly preventing applicant colleges and regional officers from accessing unauthorized records.

### Step-by-Step Script

1. **Verify Single-Tenant Scoping for Institution Officer**:
   - Sign in as `officer@delhi-tech.edu` / `Password123!`.
   - Go to **Applications**.
   - Observe that only Delhi Institute of Technology records appear.
   - Note the ID of a Mumbai application (`bbbbbbbb-0002-0000-0000-000000000201`).
   - In browser address bar, enter: `http://localhost:5173/applications/bbbbbbbb-0002-0000-0000-000000000201`.
   - **System Response**:
     - The page rejects the load and renders an official statutory `Access Denied` notification modal.
     - The backend rejects the request with HTTP 403 Forbidden and problem URI `https://api.aicte.gov.in/errors/tenant-access-denied`.
     - An immutable `TENANCY_VIOLATION_ATTEMPT` audit event is logged.

2. **Verify Regional Territorial Jurisdiction Scoping**:
   - Sign in as `regional.north@aicte-demo.gov.in` / `Password123!`.
   - Navigate to **Applications**.
   - Notice that all listed applications belong to institutions in Delhi, Uttar Pradesh, Uttarakhand, and Haryana.
   - Try to filter by Western Region or open a Western college's document.
   - Access is denied; statutory territorial boundaries are strictly upheld.

3. **Verify Central Regulatory Oversight**:
   - Sign in as `admin@aicte-demo.gov.in` / `Password123!`.
   - Navigate to **Applications**.
   - Central Admin sees institutions and applications nationwide across all 8 regions.

---

## Scenario C — Deficiency Flagging, Document Replacement & Resubmission

### Objective
Demonstrate the governance workflow when an application has non-compliant or expired certificates: deficiency issuance, replacement with immutable versioning, and resubmission.

### Step-by-Step Script

1. **Inspect Seeded Deficient Application**:
   - Sign in as `regional.north@aicte-demo.gov.in` / `Password123!`.
   - Open application `APP-2025-DEL-0103` (Delhi Institute of Technology).
   - Observe current state: **`DEFICIENT`**.
   - Switch to the **Scrutiny Findings** tab.
   - Finding: `"RULE_APH_FIRE_SAFETY — Critical Deficiency: Fire Safety Certificate expired before academic session start."`
   - Log out.

2. **Institutional Rectification & Document Versioning**:
   - Sign in as `officer@delhi-tech.edu` / `Password123!`.
   - Open application `APP-2025-DEL-0103`.
   - Switch to the **Documents** tab. Locate the expired Fire Safety Certificate.
   - Click **Replace Document (New Version)**.
   - Upload the renewed certificate PDF and enter reason: `"Renewed Fire NOC issued by Delhi Fire Service on Oct 15, 2025"`.
   - The system creates Version 2 of the document linked to Version 1, updating expiry dates and audit records.
   - Click **Resubmit Rectified Application** on the header card.
   - Justification: `"Deficiencies rectified; renewed statutory Fire NOC uploaded."`
   - Status changes to **`RESUBMITTED`**.

3. **Regional Clearance**:
   - Sign in as `regional.north@aicte-demo.gov.in`.
   - Open `APP-2025-DEL-0103`.
   - In Scrutiny Findings, click **Resolve Finding** -> Enter verification note: `"Inspected renewed NOC valid through 2028. Deficiency resolved."`
   - Advance workflow state to **`UNDER_SCRUTINY`** -> **`SCRUTINY_PASSED`**.

---

## Scenario D — Document Compliance Hub & Surveillance Tracking

### Objective
Demonstrate automated certificate surveillance, expiry warnings, and statutory corrective action tracking.

### Step-by-Step Script

1. **Institutional Surveillance Metrics**:
   - Sign in as `compliance.officer1@aicte-demo.gov.in` / `Password123!`.
   - Navigate to **Compliance Tracking** (`/compliance`).
   - Review high-level metrics cards:
     - Total Registered Documents
     - Verified Documents
     - Expiring Soon (< 30 days)
     - Expired Documents
     - Pending Corrective Actions
2. **Filtering & Audit Review**:
   - Set filter to **`Expiring Soon`**.
   - Review institutions with certificates nearing renewal deadlines.
   - Open an expiring certificate -> Click **Preview** to view the document securely inline.
3. **Corrective Action Workflow**:
   - Switch to the **Corrective Actions** tab.
   - Inspect open corrective action `CA-2025-001` (Himalayan Institute of Technology, Dehradun: Slope Stability Certificate).
   - View institutional response and engineering justification notes.
   - Click **Verify & Close Action** to record regulatory compliance.

---

## Scenario E — Autonomous Committee Dispatch via Google OR-Tools CP-SAT

### Objective
Demonstrate automated Expert Visit Committee (EVC) allocation and scheduling enforcing complex regulatory constraints.

### Step-by-Step Script

1. **Access EVC Scheduling Hub**:
   - Sign in as `evc.coord1@aicte-demo.gov.in` / `Password123!`.
   - Navigate to **EVC Scheduling** (`/evc-scheduling`).
   - Inspect the candidate pool of peer evaluators (Professors, Subject Experts, Industry Experts) across disciplines (Computer Science, Mechanical, Civil, Electrical).
2. **Review Candidate Availability & Constraints**:
   - View expert availability calendar and discipline alignments.
   - View pending inspections requiring expert visits (e.g. New Institution or New Programme applications).
3. **Execute CP-SAT Discrete Constraint Solver**:
   - Click **Run Statutory Allocation Engine**.
   - The OR-Tools CP-SAT engine formulates and solves the constraint satisfaction problem:
     - Mandates exactly 3 experts per inspection panel.
     - Solves a 2-day consecutive inspection duration.
     - Strictly excludes Sundays from inspection schedules.
     - Excludes experts with conflicts of interest (e.g., home institution affiliation).
   - The UI displays the proposed panel assignments with chairpersons, dates, and member details.
4. **Persist & Lock Schedule**:
   - Click **Confirm & Lock Schedule**.
   - The schedules and committee assignments are persisted to PostgreSQL.
   - Attempting to re-run the solver without the explicit `reschedule=true` flag is rejected to protect locked commitments.

---

## Scenario F — Immutable Regulatory Audit Trail & Outbox

### Objective
Demonstrate complete, tamper-evident regulatory traceability for audit and anti-corruption oversight.

### Step-by-Step Script

1. **Access Audit Trail**:
   - Sign in as `auditor@aicte-demo.gov.in` / `Password123!`.
   - Navigate to **Audit Trail** (`/audit-trail`).
2. **Inspect Traceability Fields**:
   - Every system interaction is displayed in chronological order:
     - Timestamp (ISO 8601 with millisecond precision)
     - Event Type (`AUTH_LOGIN_SUCCESS`, `APPLICATION_CREATED`, `WORKFLOW_STATE_TRANSITION`, `TENANCY_VIOLATION_ATTEMPT`, `EVC_SCHEDULE_PERSISTED`)
     - Principal Email
     - Resource Type & Target Entity ID
     - Action & Client IP Address
     - Correlation ID (e.g. `c-app-init-16`)
3. **Trace Multi-Tenant Violation**:
   - Filter by Event Type: `TENANCY_VIOLATION_ATTEMPT`.
   - View the record created during Scenario B showing the exact time, principal, targeted foreign institution ID, and client IP.
   - Verify that audit logs cannot be edited or deleted through any API endpoint.
