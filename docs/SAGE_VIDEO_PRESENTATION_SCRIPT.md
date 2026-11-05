# SAGE — System for Approval Governance and Evaluation
## Official Video Presentation, Director Cues & Tab Navigation Master Script
**Smart India Hackathon (SIH 2026) • Problem Statement 26262 • AICTE APH 2024–27 Automated Governance**

---

### Executive Studio & Presentation Parameters

| Parameter | Specification | Details |
| :--- | :--- | :--- |
| **Target Duration** | ~12 to 14 Minutes | 7 Synchronized Acts (Hook, Security, OCR, Scrutiny, EVC, Compliance, Architecture) |
| **Video Resolution** | 1080p 60fps | Full Screen Direct-to-Camera + Screen Share with PIP |
| **Facecam PIP Setup** | Top-Right Corner (22% width) | Eye-level lens framing, warm broadcast key lighting, no glare |
| **Audio Standard** | 48 kHz Crisp Broadcast Audio | Dedicated cardioid/condenser mic, zero room echo |
| **Spoken Cadence** | 130–145 words/minute | Authoritative, energetic, formal statutory terminology |
| **Statutory Baseline** | AICTE APH 2024–27 | Clauses 1.4, 2.1, 3.2, 4.1 & Chapter II Section 2.14 |

---

### Master Tab Pre-Staging Architecture (Open Before Recording)

To eliminate the need to log in and out repeatedly during your video presentation, pre-stage these six browser tabs in advance (using separate browser profiles, containers, or private windows):

| Tab # | Pre-Authenticated Persona | Target URL | Pre-Staged UI State & Key Demonstration Focus |
| :--- | :--- | :--- | :--- |
| **TAB 1** | **Unauthenticated / Public** | `http://localhost:5173/login` | National Accessibility Bar (WCAG 2.2 AA font scaling & high contrast), 2-column synthetic demo accounts grid. |
| **TAB 2** | **Institution Officer (Delhi)**<br>`officer@delhi-tech.edu` | `http://localhost:5173/dashboard`<br>&rarr; `/applications` | Delhi Tech dashboard (PID: 1-500123498), "New Approval Application" button, Course Proposal & OCR tabs, SAGE AI findings modal. |
| **TAB 3** | **Regional Officer (Northern)**<br>`regional@aicte.gov.in` | `http://localhost:5173/applications`<br>&rarr; `/applications/:id` | Scrutiny evaluation queue, "Evaluate Scrutiny Rules" button (Clauses 1.4, 2.1, 3.2, 4.1), Advance Workflow dropdown to `SCRUTINY_DEFICIENT`. |
| **TAB 4** | **EVC Coordinator**<br>`evc.coord@aicte.gov.in` | `http://localhost:5173/evc` | "Pending Inspections", "Confirmed Schedules", and "Accredited Experts" tabs, Google OR-Tools CP-SAT discrete constraint solver execution. |
| **TAB 5** | **Compliance Officer**<br>`compliance@aicte.gov.in` | `http://localhost:5173/compliance` | "Statutory Deadlines", "Compliance Findings", and "Surveillance Runs" tabs, automated continuous surveillance sweeps, severity ranking. |
| **TAB 6** | **Regulatory Auditor**<br>`auditor@aicte.gov.in` | `http://localhost:5173/admin/audit` | Immutable Regulatory Audit Ledger, Audit Outbox pattern, non-repudiation timestamps, Correlation ID tracing. |

---

## ACT 1: The Hook & Statutory Challenge
- **Duration**: `~0:00 – 1:30` (90 seconds)
- **Visual Mode**: `FACECAM FULL SCREEN`
- **Active Browser Tab**: None (Speaker Direct to Camera — No Browser Visible Yet)

### [Director & Screen Directions]
> **Camera Cue**: Eye-level framing, warm key lighting. Presenter speaks directly into the camera lens with high energy, confidence, and authoritative cadence. Do not share screen yet. Focus entirely on the human and statutory dimension of the challenge.

### Spoken Dialogue (Verbatim)
> **Presenter (Direct to Camera)**:
> *"Namaste, esteemed jury members and evaluators. Every single academic year, more than ten thousand technical and management institutions across India apply to the All India Council for Technical Education (AICTE) for new approvals, extension of approvals, intake expansions, and emerging branch introductions.*
>
> *Under the statutory AICTE Approval Process Handbook (APH 2024–27), this governance process has historically involved thousands of physical paper dossiers, months of manual verification, vulnerability to human error, and massive logistical gridlocks in scheduling Expert Visit Committees.*
>
> *Today, we are proud to introduce SAGE — the System for Approval Governance and Evaluation. SAGE is an end-to-end, multi-tenant regulatory automation platform engineered from the ground up to solve SIH 2026 Problem Statement 26262. It delivers explainable AI document intelligence, deterministic statutory scrutiny with zero human bias, mathematical committee panel optimization using Google OR-Tools CP-SAT discrete constraint solvers, continuous compliance surveillance, and an immutable regulatory audit trail. Let me take you inside the live system."*

### Key Jury Takeaways
- Addresses high-volume national statutory process across 10,000+ colleges.
- Directly solves SIH Problem Statement 26262 and enforces AICTE APH 2024–27 regulatory norms.
- Replaces weeks of manual paper scrutiny with automated mathematical guarantees.

---

## ACT 2: Portal Login & Multi-Tenant Personas
- **Duration**: `~1:30 – 3:30` (120 seconds)
- **Visual Mode**: `SCREEN SHARE + FACECAM PIP (Top-Right)`
- **Active Browser Tab**: `BROWSER TAB 1 • http://localhost:5173/login`

### [Director & Screen Directions]
> **Action on Screen**: Switch to screen share with Facecam in top-right PIP. Demonstrate accessibility features: click font scaling 'A+' then 'A-', then toggle 'High Contrast' mode on and off. Pan cursor across the synthetic demo accounts panel showing the 6 distinct statutory roles in a clean 2-column grid.
>
> **In-Page UI Tabs to Navigate**:
> - **Top Bar**: Accessibility Font Scaling (`A-`, `Normal`, `A+`) & High Contrast Toggle (`Alt+H`).
> - **Synthetic Demo Accounts Panel**: 1-click credentials for Central AICTE Admin, Regional Officer, Institution Officer (Delhi & Mumbai), Compliance Officer, EVC Coordinator, Regulatory Auditor.

### Spoken Dialogue (Verbatim)
> **Presenter**:
> *"Welcome to the official SAGE portal. Notice immediately our AICTE-inspired Government Design System. Across the top, we have an integrated National Accessibility Bar adhering to WCAG 2.2 AA standards: users can scale fonts on the fly or toggle High Contrast mode for visually impaired officers.*
>
> *Underneath, in our synthetic demonstration panel, you can see the six distinct statutory personas that power the AICTE governance ecosystem: The Central AICTE Administrator with nationwide regulatory jurisdiction; The Regional Officer, scoped strictly to their regional directorate; Institution Officers representing colleges like Delhi Tech or Mumbai Academy; The Compliance Officer, overseeing continuous fire safety NOCs; The EVC Coordinator, allocating peer review rosters; and The Independent Statutory Auditor.*
>
> *Every single persona has strict Role-Based Access Control. Institutional applicants can submit applications and upload documents, while regulatory officers can only evaluate, scrutinize, and audit. Let's switch to our authenticated institution tab."*

### Key Jury Takeaways
- WCAG 2.2 AA accessibility toolbar (font scaling & high contrast) built-in.
- Multi-tenant data isolation strictly enforced by Spring Boot security layer.
- All 6 statutory roles covered with instant 1-click demonstration credentials.

---

## ACT 3: Institution Dossier & SAGE AI OCR
- **Duration**: `~3:30 – 6:30` (180 seconds)
- **Visual Mode**: `SCREEN SHARE + FACECAM PIP (Top-Right)`
- **Active Browser Tab**: `BROWSER TAB 2 • http://localhost:5173/dashboard -> /applications`

### [Director & Screen Directions]
> **Action on Screen**: Switch smoothly to Tab 2 (logged in as Delhi Tech Officer). Point out Permanent Institute ID (PID: 1-500123498) and Active Standing banner. Navigate to 'Approval Applications' -> Click 'New Approval Application' -> Choose 'New Course Introduction'. Open application details. Click 'Compliance Documents & OCR' tab. Click the purple 'SAGE AI findings' button on Fire Safety Certificate to open the Google Gemini Flash advisory audit report modal. Point out the 45ms Indra OCR speed indicator.
>
> **In-Page UI Tabs to Navigate**:
> - **Application Detail Sub-Tabs**:
>   - `[Tab 1: Course Proposal]` — Curricular syllabus and laboratory infrastructure.
>   - `[Tab 2: Compliance Documents & OCR (ACTIVE)]` — Drag-and-drop PDF upload with SHA-256 fingerprint.
>   - `[Tab 3: Scrutiny Findings]` — Read-only for applicant before submission.
>   - `[Tab 4: Workflow History & State Transitions]` — Chronological lifecycle tracker.
> - **Modal**: `SAGE AI Advisory Findings (Google Gemini Flash)`.

### Spoken Dialogue (Verbatim)
> **Presenter**:
> *"We are now authenticated as the authorized signatory for Delhi Institute of Advanced Engineering. Notice how the interface adapts: the top banner shows our Permanent Institute ID, Category, and Active Standing. Because of our multi-tenant data isolation, this officer can only see Delhi Institute records. Attempting to access Mumbai records is blocked at both the React router and Spring Boot security layer with an automatic TENANCY_VIOLATION audit log.*
>
> *Let's click on Approval Applications. Notice this prominent orange button: 'New Approval Application'. This button is only visible to applicant colleges—regulators do not see it. SAGE supports all five versioned APH workflow types: EoA, Intake Increase, New Courses, Emerging Branches, and New Institutions.*
>
> *Now let's switch to the 'Compliance Documents & OCR' tab. When a college uploads a document, it passes through binary magic-byte validation, SHA-256 cryptographic hashing, and our local Indra OCR engine, which processes the PDF directly on the CPU in just 45 to 55 milliseconds per page—over 10 times faster than legacy Tesseract.*
>
> *Simultaneously, our integrated SAGE AI powered by Google Gemini Flash performs an automated advisory audit. Look at this modal: SAGE AI has verified Document Type Validity, confirmed Delhi Fire Service issuing authority, validated a 98% concordance match with institutional records, detected expiration dates through March 2027, and verified zero digital tampering. I will now click 'Submit Application'. The application immediately transitions from DRAFT to SUBMITTED."*

### Key Jury Takeaways
- CPU-optimized Indra OCR runs at 45–55 ms/page (10x faster than legacy Tesseract).
- Google Gemini Flash AI performs instant advisory validation on certificates.
- Cryptographic SHA-256 hashing and magic-byte validation prevent spoofed files.

---

## ACT 4: Regional Directorate & APH Scrutiny
- **Duration**: `~6:30 – 8:45` (135 seconds)
- **Visual Mode**: `SCREEN SHARE + FACECAM PIP (Top-Right)`
- **Active Browser Tab**: `BROWSER TAB 3 • http://localhost:5173/applications/:id`

### [Director & Screen Directions]
> **Action on Screen**: Switch smoothly to Tab 3 (logged in as Northern Regional Officer). Open the Delhi Tech application. Point out that the 'Upload Regulatory Document' form is completely hidden (demonstrating RBAC). Click 'Evaluate Scrutiny Rules' button. Switch to 'Automated Scrutiny Findings' tab. Review passed and deficient clauses. Click 'Advance Workflow' dropdown -> select 'SCRUTINY_DEFICIENT'. Explain the rectified document upload lifecycle (v1 to v2).
>
> **In-Page UI Tabs to Navigate**:
> - **Application Detail Sub-Tabs**:
>   - `[Tab 1: Course Proposal]`
>   - `[Tab 2: Compliance Documents & OCR]`
>   - `[Tab 3: Automated Scrutiny Findings (ACTIVE)]` — Pass/Deficient status for statutory clauses.
>   - `[Tab 4: Workflow History (v1 -> v2 State Machine)]`
> - **Header Action Controls**:
>   - Button: `Evaluate Scrutiny Rules`
>   - Dropdown & Button: `Advance Workflow` &rarr; `SCRUTINY_DEFICIENT`

### Spoken Dialogue (Verbatim)
> **Presenter**:
> *"We are now authenticated as the AICTE Regional Officer for the Northern Region. Observe how the portal transformed: the institutional creation button is gone because regional officers do not create applications; they scrutinize them.*
>
> *Notice the security boundaries: The 'Upload Regulatory Document' form is completely hidden from the Regional Officer. Instead, the officer has two powerful governance controls: 'Evaluate Scrutiny Rules' and 'Advance Workflow'.*
>
> *Let's click 'Evaluate Scrutiny Rules'. In less than 200 milliseconds, SAGE's Deterministic APH Scrutiny Engine evaluated the application against statutory clauses: Clause 1.4 for land area norms (minimum 2.5 acres); Clause 2.1 for fire safety NOC validity; Clause 3.2 for 1:20 faculty cadre ratios; and Clause 4.1 for title deed concordance.*
>
> *If an institution has an expired certificate, SAGE marks the rule as DEFICIENT and cites the exact clause. The Regional Officer can click 'Advance Workflow' and transition the state to SCRUTINY_DEFICIENT. When the college logs back in, they are instantly presented with a 'Resubmit Rectified Application' button. When they upload the renewed certificate, SAGE preserves the immutable version history from v1 to v2 and advances back to RESUBMITTED. Zero physical visits, zero lost paperwork, complete statutory transparency!"*

### Key Jury Takeaways
- Zero human bias: rule engine strictly maps to APH 2024–27 clauses.
- Automated deficiency notices with statutory clause-level citations.
- Immutable versioning (v1 &rarr; v2) for all rectified replacement documents.

---

## ACT 5: EVC Solver via Google OR-Tools CP-SAT
- **Duration**: `~8:45 – 10:45` (120 seconds)
- **Visual Mode**: `SCREEN SHARE + FACECAM PIP (Top-Right)`
- **Active Browser Tab**: `BROWSER TAB 4 • http://localhost:5173/evc`

### [Director & Screen Directions]
> **Action on Screen**: Switch to Tab 4 (logged in as EVC Coordinator). Point out the 3 horizontal navigation tabs: 'Pending Inspections', 'Confirmed Inspection Schedules', and 'Accredited Experts Registry'. Click the 'Accredited Experts Registry' tab to show professor specializations, home states, and conflict tracking. Return to 'Pending Inspections' and click 'Run Statutory Allocation Engine'. Point out the mathematical CP-SAT solver execution and conflict-free committee allocation.
>
> **In-Page UI Tabs to Navigate**:
> - `[Tab 1: Pending Inspections (ACTIVE)]` — Applications requiring campus peer review.
> - `[Tab 2: Confirmed Inspection Schedules]` — Formulated panels with assigned dates.
> - `[Tab 3: Accredited Experts Registry]` — Database of peer professors and home states.
> - **Action Button**: `Run Statutory Allocation Engine` / `Generate Conflict-Free Schedule`.

### Spoken Dialogue (Verbatim)
> **Presenter**:
> *"Now let's examine one of the most technologically advanced components of SAGE: The Expert Visit Committee (EVC) Allocation Engine. Under AICTE rules, whenever an institution applies for new approvals, an Expert Visit Committee must physically inspect the campus.*
>
> *Historically, assigning inspection committees was a monumental logistical headache: thousands of professors across India; strict conflict-of-interest rules where an expert cannot inspect a college in their home state or alma mater; inspections must run for exactly 2 consecutive working days; Sundays and holidays are strictly barred; and each committee must contain exactly 3 distinct active peer experts.*
>
> *Instead of manual guesswork, we integrated the Google OR-Tools CP-SAT constraint optimization solver directly into our Spring Boot backend. Let's click 'Generate Conflict-Free Schedule'.*
>
> *In under 400 milliseconds, the CP-SAT integer programming solver formulated a discrete constraint satisfaction problem with hundreds of Boolean variables and generated a mathematically proven, conflict-free inspection schedule: 3 distinct peer evaluators assigned per panel; consecutive two-day windows with all Sundays excluded; zero institutional conflicts of interest; and full availability validation. When confirmed, appointments are committed atomically and email notices are dispatched. This replaces weeks of manual phone calls with mathematical certainty!"*

### Key Jury Takeaways
- Google OR-Tools CP-SAT integer programming solver embedded directly in backend.
- Enforces 3-member active expert roster, 2-day consecutive window, and Sunday exclusion.
- Mathematically guarantees zero conflict of interest across all peer experts.

---

## ACT 6: Surveillance & Immutable Audit Ledger
- **Duration**: `~10:45 – 12:30` (105 seconds)
- **Visual Mode**: `SCREEN SHARE + FACECAM PIP (Top-Right)`
- **Active Browser Tabs**: `BROWSER TABS 5 & 6 • /compliance -> /admin/audit`

### [Director & Screen Directions]
> **Action on Screen**: First, show Tab 5 (Compliance Officer): click through the 3 tabs ('Statutory Deadlines', 'Compliance Findings', 'Surveillance Runs'). Click 'Trigger Compliance Sweep' button and highlight severity levels (INFO, WARNING, CRITICAL). Then smoothly switch to Tab 6 (Regulatory Auditor at `/admin/audit`): demonstrate the immutable ledger, filter by Correlation ID, and show the SHA-256 document fingerprint.
>
> **In-Page UI Tabs to Navigate**:
> - **Compliance Hub Tabs**:
>   - `[Tab 1: Statutory Deadlines & Expiries]`
>   - `[Tab 2: Compliance Findings & Risk Analysis]`
>   - `[Tab 3: Continuous Surveillance Runs & Execution History]`
> - **Audit Viewer Controls**:
>   - Filter by Principal (`officer@delhi-tech.edu`, `regional@aicte.gov.in`)
>   - Event Type Filter (`DOCUMENT_UPLOADED`, `SCRUTINY_EVALUATED`, `EVC_SCHEDULED`)
>   - Correlation ID Search bar (`corr-8f92-delhi-tech`)

### Spoken Dialogue (Verbatim)
> **Presenter**:
> *"AICTE governance doesn't end when an approval letter is issued. SAGE includes an Automated Continuous Compliance Engine. Our distributed background scheduler runs automated surveillance sweeps across all active institutions: tracking upcoming certificate expiration dates, detecting cadre ratio drift, and automatically classifying deficiencies into INFO, WARNING, and CRITICAL severity.*
>
> *Finally, let's look at statutory oversight. We are now in Tab 6, logged in as the Independent Regulatory Auditor. Notice this persona's portal: they cannot edit applications or change marks. They have an unrestricted view of the Immutable Regulatory Audit Ledger.*
>
> *Every single consequential action in SAGE—every login, every document upload with its SHA-256 hash, every scrutiny score, and every EVC committee assignment—is committed atomically in an Audit Outbox pattern. If the audit event fails to write to PostgreSQL, the business transaction rolls back! Each entry records the exact UTC timestamp, user principal, client IP address, action classification, and a unique Correlation ID that traces requests end-to-end. This ensures complete non-repudiation and anti-corruption integrity."*

### Key Jury Takeaways
- Continuous compliance sweeps run in background via distributed scheduler locks.
- Audit Outbox pattern: state changes roll back if audit persistence fails.
- Correlation IDs trace transactions across React, Spring Boot, and FastAPI.

---

## ACT 7: Technology Stack, Cloud & Closing
- **Duration**: `~12:30 – 14:00` (90 seconds)
- **Visual Mode**: `FACECAM FULL SCREEN`
- **Active Browser Tab**: None (Speaker Direct to Camera — Full Frame Return)

### [Director & Screen Directions]
> **Action on Screen**: Switch cleanly from Screen Share back to Facecam Full Screen. The presenter delivers an inspiring, high-impact concluding pitch. Recite the comprehensive test pass metrics (175/175 tests), cloud architecture readiness on Oracle Cloud Infrastructure ARM64 Ampere A1, and the final transformative vision of SAGE for national technical education governance.

### Spoken Dialogue (Verbatim)
> **Presenter (Closing to Camera)**:
> *"To summarize the engineering behind SAGE: Core Backend built on Java 21 LTS and Spring Boot 3.4 with 24 relational tables managed by Flyway migrations, stateless Bearer JWTs with server-side token revocation, and Google OR-Tools CP-SAT; Automation & OCR built on Python 3.12 and FastAPI utilizing high-speed CPU-optimized Indra OCR, Tesseract fallback, and Google Gemini Flash AI; Frontend built on React 19 and Vite 8 delivering an accessible, government-themed single-page application with responsive role-specific dashboards.*
>
> *We have verified 175 passing tests—101 Java unit and integration tests, 15 Python automation tests, and 59 Vitest frontend tests with 100% pass rate. Furthermore, SAGE is packaged via Docker Compose with automated Caddy HTTPS reverse proxying, automated daily database backup scripts, and is fully audited and optimized for Oracle Cloud Infrastructure (OCI) ARM64 Ampere A1 server deployment on the Always Free tier.*
>
> *SAGE transforms the AICTE Approval Process Handbook from a dense, 200-page regulatory manual into an automated, explainable, and tamper-resistant digital reality. It empowers institutions with speed and clarity, while giving regulators unprecedented transparency, fairness, and accountability. Thank you very much. We are now eager and ready to answer any questions from the esteemed evaluation panel!"*

### Key Jury Takeaways
- 175 / 175 passing tests across Java, Python, and TypeScript suites.
- OCI Always Free ARM64 (Ampere A1) production verified with Caddy HTTPS.
- Complete enterprise-grade solution ready for national regulatory staging.

---

## APPENDIX: Demonstration Credentials & Roles Cheatsheet

All demonstration accounts use password: `Password123!`

| Role Persona | Email / Username | Scope / Jurisdiction | Pre-Staged Tab & Key Demonstration Focus |
| :--- | :--- | :--- | :--- |
| **Central AICTE Admin** | `admin@aicte.gov.in` | Nationwide (All Tenancies) | Global diagnostics, user management, complete audit log |
| **Regional Officer** | `regional@aicte.gov.in` | Northern Region (DL, UP, HR) | **Tab 3:** Scrutiny evaluation, APH rule checks, advance workflow state |
| **Institution Officer (Delhi)** | `officer@delhi-tech.edu` | Delhi Tech (PID: 1-500123498) | **Tab 2:** Create application, upload PDF certificate, SAGE AI findings, submit |
| **Institution Officer (Mumbai)** | `officer@mumbai-engg.edu` | Mumbai Engg (PID: 1-500987654) | Demonstrates multi-tenancy & cross-institutional access denial |
| **Compliance Officer** | `compliance@aicte.gov.in` | Compliance Directorate | **Tab 5:** Continuous surveillance sweeps, NOC expiry tracking, cadre ratios |
| **EVC Coordinator** | `evc.coord@aicte.gov.in` | Inspection Operations Hub | **Tab 4:** OR-Tools CP-SAT discrete solver, 3-expert committee generation |
| **Regulatory Auditor** | `auditor@aicte.gov.in` | Statutory Oversight | **Tab 6:** Immutable tamper-evident audit trail, Correlation ID search |

---

### Core Technology & Architecture Matrix

| Core Technology | Version / Framework | Key Governance & Algorithmic Capabilities |
| :--- | :--- | :--- |
| **Backend Engine** | Java 21 LTS • Spring Boot 3.4 | 24 relational tables, Flyway migrations, stateless JWT with revocation, OR-Tools CP-SAT solver |
| **Document AI & OCR** | Python 3.12 • FastAPI | Indra OCR (45ms CPU inference), Tesseract fallback, Google Gemini Flash advisory audit |
| **Frontend Single Page App** | React 19 • Vite 8 • TypeScript | WCAG 2.2 AA accessibility toolbar, role-specific dynamic dashboards, zero information leakage |
| **Cloud & Infrastructure** | OCI Ampere A1 (ARM64) • Docker | Caddy HTTPS reverse proxy, daily automated PostgreSQL backup scripts, 175/175 tests verified |

---

### Key Statutory Citations & Emergency Recovery Cheatsheet

#### AICTE APH 2024–27 Statutory Citations
- **Clause 1.4**: Land & Built-up Area norms (Minimum 2.5 acres urban / 5.0 acres rural).
- **Clause 2.1**: Fire Safety & Structural Stability NOC validity verification.
- **Clause 3.2**: Faculty Cadre Ratio (1:20 UG Engineering; Professor : Associate : Assistant = 1 : 2 : 6).
- **Clause 4.1**: Title Deed, Non-encumbrance & Government Revenue concordance verification.
- **Chapter II Section 2.14**: EVC 3-member panel, 2 consecutive days, Sunday strictly barred.

#### Live Demo Port & Emergency Recovery Reference
- **Frontend App**: `http://localhost:5173` (React 19 / Vite 8)
- **Backend API**: `http://localhost:8080/swagger-ui.html` (Spring Boot 3.4 OpenAPI)
- **Automation & OCR**: `http://localhost:8000/docs` (FastAPI / Indra OCR)
- **Reset Demo Seed Data**: `./scripts/reset-demo.sh && ./scripts/seed-demo.sh`
- **Run Full Deployment Smoke Test**: `./scripts/deployment-smoke-test.sh`
