# SAGE — System for Approval Governance and Evaluation

<p align="center">
  <img src="frontend/public/sage-logo.png" width="140" alt="SAGE Logo" />
  <br/>
  <strong>System for Approval Governance and Evaluation (SAGE)</strong>
  <br/>
  <em>Integrated AICTE Approval Workflow Automation, Multi-Modal Document Intelligence, Continuous Compliance Monitoring &amp; EVC Scheduling</em>
  <br/>
  <strong>Smart India Hackathon (SIH 2026) • Problem Statement 26262</strong>
</p>

<p align="center">
  <a href="#system-architecture"><img src="https://img.shields.io/badge/architecture-Microservices%20Perimeter-0f172a.svg" alt="Architecture" /></a>
  <a href="#testing-suite"><img src="https://img.shields.io/badge/tests-140%2B%20passing%20%7C%20100%25%20verified-brightgreen.svg" alt="Test Status" /></a>
  <a href="#system-requirements"><img src="https://img.shields.io/badge/backend-Spring%20Boot%203.4.3%20%7C%20Java%2021%20LTS-1e3a8a.svg" alt="Backend" /></a>
  <a href="#feature-catalog"><img src="https://img.shields.io/badge/evc%20solver-Google%20OR--Tools%20CP--SAT%209.11-darkblue.svg" alt="Solver" /></a>
  <a href="#feature-catalog"><img src="https://img.shields.io/badge/ocr%20engine-Indra%20OCR%20(45ms%2Fp)%20%7C%20FastAPI-0284c7.svg" alt="OCR" /></a>
  <a href="#system-requirements"><img src="https://img.shields.io/badge/frontend-React%2019%20%7C%20TypeScript%20%7C%20Vite-7c3aed.svg" alt="Frontend" /></a>
  <a href="#feature-catalog"><img src="https://img.shields.io/badge/accessibility-WCAG%202.2%20AA%20Compliant-ea580c.svg" alt="Accessibility" /></a>
  <a href="docs/SAGE_FINAL_MASTER_DEMONSTRATION_GUIDE.pdf"><img src="https://img.shields.io/badge/documentation-Master%20Guide%20PDF-15803d.svg" alt="PDF Guide" /></a>
</p>

---

## Table of Contents

1. [Executive Summary &amp; Regulatory Context](#executive-summary)
2. [System Architecture &amp; Subsystems](#system-architecture)
3. [Feature Catalog &amp; Deep-Dive](#feature-catalog)
   - [1. Intelligent Application Lifecycle Management (5 Tailored Pathways)](#feature-lifecycle-pathways)
   - [2. Statutory Pre-Submission Compliance Gate](#feature-compliance-gate)
   - [3. Side-by-Side Dual-Pane Document &amp; AI Inspector](#feature-dual-pane-inspector)
   - [4. SAGE AI 4-Pillar Document Review &amp; Anomaly Detection](#feature-sage-ai-pillars)
   - [5. Ultra-Fast Local Indra OCR &amp; Multi-Engine Intelligence](#feature-indra-ocr)
   - [6. Document Version Control &amp; Anti-Tampering Chain of Custody](#feature-version-control)
   - [7. Continuous Compliance Monitoring &amp; Expiry Sweeps](#feature-compliance-monitoring)
   - [8. Expert Visit Committee (EVC) Scheduling Engine (Google OR-Tools CP-SAT)](#feature-evc-scheduling)
   - [9. Authoritative Multi-Tenancy &amp; 6 Role-Based Portals](#feature-multi-tenancy-portals)
   - [10. Immutable Regulatory Audit Trail &amp; Correlation IDs](#feature-audit-trail)
   - [11. National Accessibility &amp; Inclusive Government Design System](#feature-accessibility)
4. [System Requirements &amp; Prerequisites](#system-requirements)
5. [Complete Local Setup &amp; Execution Guide](#local-setup-guide)
   - [Method 1: One-Click Unified Platform Runner (Recommended)](#method-1-runner)
   - [Method 2: Manual Terminal-by-Terminal Execution](#method-2-manual)
   - [Method 3: Containerized Deployment via Docker Compose](#method-3-docker)
   - [OS-Specific Setup Instructions (Linux, macOS, Windows)](#os-specific-setup-instructions)
6. [Configuration Properties &amp; Environment Variables](#configuration-properties)
7. [Pre-Seeded Demonstration Accounts &amp; Persona Matrix](#demo-accounts)
8. [Automated Testing &amp; Verification Suite](#testing-suite)
9. [Interactive API Documentation](#api-documentation)
10. [Regulatory Alignment &amp; APH 2024–27 Traceability](#regulatory-traceability)
11. [Troubleshooting &amp; Frequently Asked Questions](#troubleshooting-faq)
12. [Repository Directory Layout](#directory-layout)
13. [Project Documentation &amp; Evaluation Artifacts](#project-documentation)

---

## <a id="executive-summary"></a>🏛 Executive Summary & Regulatory Context

Every year, over **10,000 technical, engineering, and management institutions** across India submit applications to the **All India Council for Technical Education (AICTE)** for institutional approvals, Extensions of Approval (EoA), course additions, intake variations, and branch introductions.

### The Historical Bottlenecks
- **Physical Paperwork &amp; Lost Files**: Massive physical dossiers moving between applicant colleges and AICTE Regional Offices.
- **Unnoticed Certificate Expirations**: Critical fire safety clearances and land occupancy certificates expiring mid-academic-year without automated surveillance.
- **Combinatorial Scheduling Gridlock**: Manually assigning 3-member Expert Visit Committees (EVC) across thousands of institutions while attempting to avoid home-state conflicts of interest, match academic specializations, and exclude Sundays consumed weeks of manual phone calls and spreadsheets.
- **Vulnerability to Forgery &amp; AI-Generated Affidavits**: Unscrupulous entities fabricating statutory affidavits with generative AI tools (e.g., ChatGPT) or splicing municipal seals onto substandard stamp paper without detection.

### The SAGE Solution
**SAGE (System for Approval Governance and Evaluation)** transforms the statutory mandate of the **AICTE Approval Process Handbook (APH 2024–25 to 2026–27)** into an intelligent, transparent, accessible, and automated digital platform. 

SAGE combines:
1. **CPU-optimized Indra OCR** (45–55 ms/page) and **SAGE AI 4-Pillar Document Review** to extract and verify institutional evidence in seconds.
2. A **Pre-Submission Compliance Gate** that locks application submission until 100% of required statutory documents are verified clean.
3. A **Side-by-Side Dual-Pane Inspector** pairing the genuine PDF stream directly with extracted OCR data and AI fraud alerts.
4. **Google OR-Tools CP-SAT discrete constraint optimization** to compute conflict-free, 3-expert inspection panels in under 400 milliseconds.
5. **Continuous midnight compliance sweeps** with dynamic 30-day and 14-day expiry countdown clocks.
6. An **Immutable Transactional Outbox Audit Ledger** capturing SHA-256 digital document fingerprints and Request Correlation IDs.
7. A strict **Human-in-the-Loop Statutory Mandate**: AI and automation assist with extraction, validation, and scheduling; authorized human officers make all final regulatory determinations.

---

## <a id="system-architecture"></a>🏗 System Architecture & Subsystems

SAGE implements an isolated, micro-service perimeter architecture with strict role boundaries, database-level multi-tenancy, and secure inter-service communication:

```text
+---------------------------------------------------------------------------------------------------+
|                                     BROWSER CLIENT / PRESENTATION                                 |
|   React 19 + TypeScript 5.7 + Vite 8.3 • AICTE Design System • WCAG 2.2 AA Accessibility Toolbar   |
|   Port: 5173 (Dev) | 6 Tailored Role Dashboards | Side-by-Side Dual-Pane Document Inspector       |
+---------------------------------------------------------------------------------------------------+
                                            |
                                  HTTP REST / Bearer JWT
                                            v
+---------------------------------------------------------------------------------------------------+
|                                  CORE AUTHORITY BACKEND LAYER                                     |
|   Java 21 LTS • Spring Boot 3.4.3 • Spring Security 6.4 • Spring Data JPA • Hibernate 6.6         |
|   Port: 8080 | Swagger UI: /swagger-ui.html | Actuator Health: /actuator/health                   |
|                                                                                                   |
|   +--------------------------+  +--------------------------+  +-------------------------------+   |
|   |  Authoritative Security  |  | Statutory State Machine  |  |  Google OR-Tools CP-SAT 9.11  |   |
|   |  RBAC + Multi-Tenancy    |  | 5 Application Workflows  |  |  EVC Optimization Solver      |   |
|   +--------------------------+  +--------------------------+  +-------------------------------+   |
|   +--------------------------+  +--------------------------+  +-------------------------------+   |
|   | Pre-Submission Gate Svc  |  | Continuous Compliance    |  | Immutable Audit Outbox        |   |
|   | Mandatory Checklist Eval |  | Expiry Countdown Sweeps  |  | Atomic State & Correlation IDs|   |
|   +--------------------------+  +--------------------------+  +-------------------------------+   |
|   +--------------------------+  +--------------------------+                                      |
|   | Gemini AI Review Client  |  | Distributed Job Lock     |                                      |
|   | 4-Pillar Document Checks |  | Clustered Node Safety    |                                      |
|   +--------------------------+  +--------------------------+                                      |
+---------------------------------------------------------------------------------------------------+
             |                                                         |
   HTTP REST / X-Internal-Token                              JDBC / PostgreSQL 16+
   (Fail-Closed Perimeter Guard)                             (or In-Memory H2 for Dev)
             v                                                         v
+--------------------------------------------+    +-------------------------------------------------+
|     AUTOMATION & OCR INTELLIGENCE ENGINE   |    |                 PERSISTENCE LAYER               |
|   Python 3.11+ / FastAPI 0.115 • Uvicorn   |    |   PostgreSQL 16+ (Production / Docker)          |
|   Port: 8000 | API Docs: /docs             |    |   H2 In-Memory DB (PostgreSQL Mode for Dev)     |
|   • Indra OCR Engine (45–55 ms/page)       |    |   Flyway Versioned Migrations (V1 to V18)       |
|   • Tesseract OCR 5.3 Fallback             |    |   Document Storage Vault (data/documents)      |
|   • Line Bounding Boxes & Confidence       |    |   SHA-256 Cryptographic Deduplication           |
+--------------------------------------------+    +-------------------------------------------------+
```

### Subsystems Breakdown

| Subsystem | Primary Technologies | Operational Responsibilities |
| :--- | :--- | :--- |
| **Frontend Web Portal** | React 19, TypeScript, Vite, TanStack React Query v5, Lucide React | High-performance SPA with 6 persona dashboards, Dual-Pane Inspector modal, accessibility toolbar, and real-time reactive state management. |
| **Core Authority Backend** | Java 21, Spring Boot 3.4.3, Spring Security, Flyway, Google OR-Tools | Master authority handling statutory workflows, multi-tenancy scoping, APH checklist rules, CP-SAT panel scheduling, and immutable audit logging. |
| **Automation &amp; OCR Engine** | Python 3.11+, FastAPI, Indra OCR, Pypdfium2, Pillow, Pydantic | High-throughput local CPU text extraction, 4° deskew, line-level bounding box spatial mapping, and date/field normalization. |
| **SAGE AI Engine** | Google Gemini Integration (`gemini-flash-lite-latest`) | Pre-upload and in-inspector 4-pillar analysis: AI text detection, typography checking, APH seal verification, and date discrepancy checks. |
| **Persistence &amp; Storage** | PostgreSQL 16+ (Prod) / H2 (Dev), Local Vault | 24 relational tables, optimistic locking (`@Version`), Flyway schema migrations, and SHA-256 isolated file storage. |

---

## <a id="feature-catalog"></a>⚡ Feature Catalog & Deep-Dive

### <a id="feature-lifecycle-pathways"></a>1. Intelligent Application Lifecycle Management (5 Tailored Pathways)
Instead of forcing every college through a single generic, 200-field form, SAGE implements five category-specific workflows conforming to the AICTE Approval Process Handbook (APH):
1. **Introduction of New Course / Discipline (Clause 2.4)**: Enforces curriculum syllabus submission, laboratory readiness, and faculty cadre compliance.
2. **Extension of Approval (EoA)**: Streamlined annual renewal verifying active fire safety, building occupancy, and non-encumbrance status.
3. **Increase in Approved Intake**: Checks built-up classroom capacity, computer laboratory norms, and 1:20 teacher-to-student cadre ratios.
4. **Establishment of New Technical Institution**: Rigorous multi-stage scrutiny covering land title ownership, government revenue concordance, and trust deed verification.
5. **Introduction of New Branch / Specialization**: Fast-track pathway for emerging technology areas (AI, Data Science, Robotics, Cyber Security).

**Statutory State Progression Machine:**
```text
[DRAFT] ──► [SUBMITTED] ──► [UNDER_SCRUTINY] ──► [SCRUTINY_PASSED] ──► [AWAITING_INSPECTION] ──► [APPROVED]
                                   │                                                                   ▲
                                   ▼                                                                   │
                              [DEFICIENT] ──► [RESUBMITTED] ───────────────────────────────────────────┘
```
- Every transition is strictly authorized server-side and protected by optimistic locking (`@Version`).
- Illegal cross-state jumps (e.g. attempting to approve directly from `DRAFT`) are rejected with descriptive RFC 7807 problem details.

---

### <a id="feature-compliance-gate"></a>2. Statutory Pre-Submission Compliance Gate
Historically, thousands of applications were submitted with missing certificates or corrupt files, creating massive backlogs in AICTE regional offices.
- **How It Works**: SAGE evaluates the uploaded document portfolio against the application's category rules *before* allowing submission.
- **The Visual Gate Banner**: Displays a green progress tracker: `Ready for Statutory Submission (3/3) - 3 of 3 Statutory Documents (100%)`.
- **Enforcement**: If any mandatory certificate (such as Fire Safety NOC, Occupancy Certificate, or Statutory Compliance Affidavit) is missing, expired, or flagged with a high forgery risk, the **"Submit Application"** button remains locked server-side, preventing incomplete files from entering the regulatory queue.

---

### <a id="feature-dual-pane-inspector"></a>3. Side-by-Side Dual-Pane Document & AI Inspector
Scrutiny officers previously had to download PDFs, open them in separate desktop viewers, and manually cross-reference numbers against paper forms.
- **Left Pane (52% Viewport)**: Renders the actual uploaded PDF certificate using a secure, multi-tenant authorized browser stream with embedded zoom controls (`+`, `-`, `100%`, `Reset`), full-tab expansion, secure download, and file metadata (file size, MIME type, SHA-256 fingerprint, upload date).
- **Right Pane (48% Viewport)**: A three-tabbed scrutiny console:
  - **Tab 1: SAGE AI Review &amp; Integrity**: 4-pillar algorithmic check with bold red alert badges on anomalies.
  - **Tab 2: Current OCR Extraction**: Extracted statutory data table with 100% confidence badges.
  - **Tab 3: Officer Scrutiny Console**: Official remarks entry box and one-click determination buttons.

---

### <a id="feature-sage-ai-pillars"></a>4. SAGE AI 4-Pillar Document Review & Anomaly Detection
Every uploaded document is evaluated against the statutory standards of APH 2024–27 across four comprehensive pillars:

| Pillar | Algorithmic Evaluation | Clean Output | Tampered / Flagged Output |
| :--- | :--- | :--- | :--- |
| **1. AI Text Generation** | Evaluates token regularity, synthetic cadence, and LLM prompt leakage (e.g. ChatGPT / Claude markers) in legal affidavits. | `CLEAN (0% AI)` (Green badge) | `SUSPECTED_AI` (Red badge) &bull; *88.4% AI-generated text detected* |
| **2. AICTE Font Norms** | Enforces official statutory typography: Times New Roman, Arial, or Calibri in 10–12pt uniform baseline. Flags decorative display fonts. | `COMPLIANT` (Green badge) | `NON_COMPLIANT_FONTS` (Red badge) |
| **3. APH Format &amp; Seal** | Checks for official municipal/state letterhead, circular embossed seal impressions, dispatch numbers, and mandatory ₹100 stamp paper valuation. | `APH VERIFIED` (Green badge) | `DEFICIENT / SPLICED` (Red badge) &bull; *Missing ₹100 stamp paper denomination* |
| **4. Discrepancy Checks** | Cross-references validity dates against Academic Year 2024–25 and verifies institution names against the master institutional profile. | `0 DISCREPANCIES` (Green badge) | `FLAGGED (1+ MISMATCH)` (Red badge) |

> **Statutory Human-in-the-Loop Principle**: SAGE AI acts strictly in an advisory capacity. In compliance with AICTE regulatory guidelines, AI never rubber-stamps approvals or rejections; legal authority rests strictly with authorized human scrutiny officers.

---

### <a id="feature-indra-ocr"></a>5. Ultra-Fast Local Indra OCR & Multi-Engine Intelligence
- **High-Throughput CPU Performance**: Operates at **45–55 ms per page** on standard server CPUs without requiring expensive GPU infrastructure.
- **10x–13x Speedup**: Dramatically outperforms older Tesseract setups on digital government PDFs.
- **Spatial Precision**: Extracts bounding box coordinates `[ymin, xmin, ymax, xmax]` for every text token.
- **Automatic Skew Correction**: Corrects image rotation up to 4° before text analysis.
- **Normalized Data Fields**: Converts raw unstructured text into ISO-8601 standardized dates and normalized certificate numbers:
  - `Certificate Number`: e.g. `DFS/MS/2024/SZ/8891` (100% confidence)
  - `Issuing Authority`: `HEADQUARTERS: DELHI FIRE SERVICE: NEW DELHI` (100% confidence)
  - `Issue Date`: `2024-05-15` (100% confidence)
  - `Expiry Date`: `2027-05-14` (100% confidence)
  - `Institution Name`: `Delhi Institute of Technology` (100% confidence)
- **Automatic Fallback**: If an image is severely degraded, the service seamlessly cascades to Tesseract OCR 5.3 to maximize extraction success.

---

### <a id="feature-version-control"></a>6. Document Version Control & Anti-Tampering Chain of Custody
- **Magic-Byte Binary Inspection**: Verifies raw binary file signatures (`%PDF-`, PNG, JPEG); prevents executable spoofing or disguised malware.
- **SHA-256 Cryptographic Fingerprints**: Every file is fingerprinted at upload time; any silent file alteration is instantly detected.
- **Immutable Version History Drawer**: When a college rectifies a deficient certificate, the original **Version 1 (`v1`)** is permanently archived with its original timestamp, uploader email, and SHA-256 hash. **Version 2 (`v2`)** becomes the active document linked to Corrective Action (`CA-2025-001`). No submitted evidence is ever overwritten or hidden.

---

### <a id="feature-compliance-monitoring"></a>7. Continuous Compliance Monitoring & Expiry Sweeps
Regulatory governance does not end when approval is granted; certificates expire during the academic year.
- **Surveillance Engine (`ComplianceEngineService`)**: Runs automated background sweeps across all active campuses.
- **Dynamic Countdown Clocks**: Calculates days remaining in real-time (`30d left`, `14d left`, `OVERDUE 30d ago`).
- **Three Severity Tiers**:
  - `INFO`: Certificates expiring within 60 days.
  - `WARNING`: Certificates expiring within 30 days.
  - `CRITICAL`: Certificates already expired or unrectified deficiencies.
- **On-Demand Sweeps**: The **"Trigger Compliance Scan"** button allows compliance officers to trigger on-demand scans across all institutions in under 2 seconds.
- **In-App Alerts &amp; Deduplication**: Alerts are fingerprinted by SHA-256 to prevent alert spamming while tracking read and acknowledgment timestamps.

---

### <a id="feature-evc-scheduling"></a>8. Expert Visit Committee (EVC) Scheduling Engine (Google OR-Tools CP-SAT)
Assigning inspection committees across 10,000+ technical campuses historically required weeks of phone calls and spreadsheet coordination. SAGE embeds the **Google OR-Tools CP-SAT discrete constraint solver** directly into the Spring Boot backend, formulating peer review as a discrete integer satisfaction problem solved in **under 400 milliseconds**.

**5 Hard Statutory Constraints Enforced:**
1. **Exact Committee Size**: Exactly three distinct accredited peer evaluators per panel (Chairperson + Subject Experts).
2. **Discipline &amp; Specialization Concordance**: Evaluators must match the specific engineering or technical branch of the application.
3. **Availability Verification**: Experts must be confirmed available on both working inspection days.
4. **Sunday Exclusion Rule**: Physical inspections on Sundays are strictly prohibited. If a visit begins on Saturday, Sunday is non-working and Monday becomes Day 2.
5. **Zero Conflict of Interest (COI)**: An expert cannot inspect an institution located in their home state, their alma mater, or their prior employer.

---

### <a id="feature-multi-tenancy-portals"></a>9. Authoritative Multi-Tenancy & 6 Role-Based Portals
Multi-tenancy is enforced at the database query layer, not merely hidden in the frontend navigation:
- **Tenant Isolation**: Institution users are strictly bound to their `institution_id` extracted from signed, stateless JWT tokens. Attempting to query another college's ID triggers an immediate `HTTP 403 Forbidden` and logs a `TENANCY_VIOLATION_ATTEMPT` event.
- **Six Distinct Role Dashboards**:
  1. **Central Administrator**: Full global oversight, user management, policy rules, and system diagnostics.
  2. **Regional Scrutiny Officer**: Northern, Western, Southern regional queues, APH checklist evaluation, and deficiency notices.
  3. **Institution Officer**: Application submission, document uploads, compliance gate tracking, and versioned rectifications.
  4. **Compliance Officer**: Continuous surveillance dashboard, deadline countdowns, and scheduled sweep triggers.
  5. **EVC Coordinator**: Accredited experts registry, availability calendars, and CP-SAT panel generation.
  6. **Statutory Auditor**: Read-only forensic view of the immutable audit ledger across all institutions.
- **Server-Side Token Revocation**: Stateless Bearer JWT with server-side token revocation table and cache on logout.

---

### <a id="feature-audit-trail"></a>10. Immutable Regulatory Audit Trail & Correlation IDs
- **Transactional Outbox Pattern**: Consequential state changes (document uploads, scrutiny decisions, approvals, EVC runs) are saved atomically in the same database transaction as the audit record. If audit logging fails, the business transaction rolls back (`AuditPersistenceException`).
- **Forensic Attributes Captured**:
  - Exact UTC Timestamp
  - Actor Principal (Email)
  - Client IP Address
  - Action Classification (`DOCUMENT_UPLOAD`, `SCRUTINY_PASS`, `DEFICIENCY_FLAGGED`, `EVC_ALLOCATION`, `APPROVED`)
  - Target Resource UUID
  - SHA-256 Document Fingerprint
  - Unique Request Correlation ID (e.g. `4ee01b2a`)
- **Searchable Ledger**: Auditors can filter logs by Actor, Event Type, Resource Type, or Correlation ID to trace entire requests end-to-end.

---

### <a id="feature-accessibility"></a>11. National Accessibility & Inclusive Government Design System
- **WCAG 2.2 AA Conformance**: High-contrast ratios, semantic HTML landmarks, and accessible focus outlines.
- **Accessibility Toolbar**: Located prominently on the portal header:
  - `A-`: Reduces font size.
  - `Normal`: Standard typography.
  - `A+`: Enlarges all typography across the interface for low-vision users.
  - `High Contrast` (`Alt+H`): Toggles bold black-and-gold high-contrast styling conforming to national accessibility guidelines.
- **One-Click Synthetic Demo Presets**: Login screen features one-click buttons for all 7 statutory roles, enabling zero-friction demonstrations without manual password typing.

---

## <a id="system-requirements"></a>💻 System Requirements & Prerequisites

SAGE is engineered to be lightweight, efficient, and capable of running smoothly on developer laptops, on-premise servers, and cloud VMs (x86_64 or ARM64).

### Hardware Specifications

| Component | Minimum Specification | Recommended Specification |
| :--- | :--- | :--- |
| **Processor (CPU)** | 2 Cores (Intel / AMD x86_64 or Apple Silicon ARM64) | 4+ Cores (Intel i5/i7/i9, AMD Ryzen, Apple M1/M2/M3/M4) |
| **System Memory (RAM)** | 4 GB RAM | 8 GB or 16 GB RAM |
| **Disk Storage** | 5 GB free disk space | 10 GB+ SSD free space |
| **Network** | Offline capable for local demo | Internet access for initial package downloads |

### Software Prerequisites

| Tool | Required Version | Verification Command | Purpose |
| :--- | :--- | :--- | :--- |
| **Java JDK** | **Java 21 LTS** (OpenJDK / Temurin / Corretto) | `java -version` | Core Backend &amp; Google OR-Tools CP-SAT |
| **Python** | **Python 3.11, 3.12, 3.13, or 3.14** | `python3 --version` | FastAPI Automation &amp; Indra OCR Service |
| **Node.js &amp; npm** | **Node.js 18 LTS or 20+ LTS** (npm 9+) | `node -v && npm -v` | React 19 Frontend Web Portal |
| **Git** | **Git 2.30+** | `git --version` | Version control &amp; repository cloning |
| **Docker** *(Optional)* | Docker 24+ &amp; Docker Compose v2 | `docker compose version` | Containerized deployment mode |
| **PostgreSQL** *(Optional)* | PostgreSQL 16+ | `psql --version` | Optional (Embedded H2 in-memory mode active by default in dev) |

---

## <a id="local-setup-guide"></a>🚀 Complete Local Setup & Execution Guide

Follow these instructions to download, configure, and launch SAGE locally on any system.

### Step 1: Clone the Repository
Open your terminal and clone the SAGE repository:
```bash
git clone https://github.com/your-org/SAGE.git
cd SAGE
```

---

### Step 2: Environment Configuration
Copy the provided environment template:
```bash
cp .env.example .env
```
> [!NOTE]
> For standard local development and evaluation, the default values in `.env.example` are pre-configured to work immediately with the embedded in-memory database and local ports. No manual edits are required to get started.

---

### Step 3: Launch SAGE (Choose One Method)

#### <a id="method-1-runner"></a>Method 1: One-Click Unified Platform Runner (`./run.sh` - Recommended)
The all-in-one runner automatically checks your environment, sets up Python virtual environments, installs frontend dependencies, runs database migrations, launches all three services in parallel, polls health endpoints, and displays a live operational dashboard.

```bash
chmod +x run.sh
./run.sh
```

**What `./run.sh` does automatically:**
1. Validates Java 21, Python 3.11+, and Node.js prerequisites.
2. Checks ports `5173`, `8080`, and `8000` (auto-frees occupied ports if needed).
3. Creates `automation/venv` and installs Python OCR requirements (`pip install -r requirements.txt`).
4. Runs `npm install` in `frontend/` if `node_modules` is absent.
5. Launches FastAPI OCR Engine (port 8000), Spring Boot Backend (port 8080), and Vite Web Portal (port 5173).
6. Polls health endpoints until all services respond with `HTTP 200 OK`.
7. Displays URLs, pre-seeded accounts, and log locations.
8. Provides clean, graceful shutdown on `Ctrl+C`.

---

#### <a id="method-2-manual"></a>Method 2: Manual Terminal-by-Terminal Execution
If you prefer running each subsystem in its own terminal window:

##### Terminal 1: Python Automation & OCR Engine (Port 8000)
```bash
cd automation
python3 -m venv venv
source venv/bin/activate        # On Windows: venv\Scripts\activate
pip install --upgrade pip
pip install -r requirements.txt
uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```
*Health Check*: `http://localhost:8000/docs`

##### Terminal 2: Java Spring Boot Backend (Port 8080)
```bash
cd backend
./mvnw clean spring-boot:run -Dspring-boot.run.profiles=dev
# On Windows: mvnw.cmd clean spring-boot:run -Dspring-boot.run.profiles=dev
```
*Health Check*: `http://localhost:8080/actuator/health`

##### Terminal 3: React Vite Frontend (Port 5173)
```bash
cd frontend
npm install
npm run dev
```
*Web Portal*: `http://localhost:5173`

---

#### <a id="method-3-docker"></a>Method 3: Containerized Deployment via Docker Compose
To run SAGE in fully isolated Docker containers with PostgreSQL 16:
```bash
cd infrastructure
docker compose up --build -d
```
To check container status:
```bash
docker compose ps
docker compose logs -f
```
To shut down containers:
```bash
docker compose down
```

---

### OS-Specific Setup Instructions

#### <a id="setup-ubuntu-debian"></a>Ubuntu / Debian Linux
```bash
# Update package index
sudo apt update

# Install Java 21 LTS
sudo apt install -y openjdk-21-jdk

# Install Python 3 & venv
sudo apt install -y python3 python3-venv python3-pip

# Install Node.js 20 LTS
curl -fsSL https://deb.nodesource.com/setup_20.x | sudo -E bash -
sudo apt install -y nodejs

# Verify tools
java -version && python3 --version && node -v && npm -v
```

#### <a id="setup-macos"></a>macOS (Apple Silicon or Intel via Homebrew)
```bash
# Install Homebrew if not installed
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# Install Java 21, Python 3, and Node.js
brew install openjdk@21 python@3.12 node

# Link Java 21 to system wrapper
sudo ln -sfn /opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-21.jdk

# Verify tools
java -version && python3 --version && node -v && npm -v
```

#### <a id="setup-windows-wsl"></a>Windows 10 / 11 (via WSL2 - Recommended)
1. Install WSL2 (Ubuntu): `wsl --install -d Ubuntu`
2. Open Ubuntu terminal and follow the [Ubuntu / Debian Linux](#setup-ubuntu-debian) commands above.
3. Clone the repo inside the WSL filesystem: `cd ~ && git clone <repo_url> && cd SAGE`
4. Run `./run.sh`. The browser on Windows can access `http://localhost:5173` directly.

#### <a id="setup-windows-native"></a>Windows Native (PowerShell / Git Bash)
1. Install **Eclipse Temurin JDK 21** via [Adoptium](https://adoptium.net/).
2. Install **Python 3.12+** from [python.org](https://www.python.org/) (ensure *"Add Python to PATH"* is checked).
3. Install **Node.js 20 LTS** from [nodejs.org](https://nodejs.org/).
4. Use Git Bash to run `./run.sh`, or use separate PowerShell terminals following Method 2.

---

## <a id="configuration-properties"></a>⚙️ Configuration Properties & Environment Variables

| Variable Name | Subsystem | Default (Dev) | Description |
| :--- | :--- | :--- | :--- |
| `SERVER_PORT` | Backend | `8080` | Port for Spring Boot Core REST API |
| `SPRING_PROFILES_ACTIVE` | Backend | `dev` | Active Spring profile (`dev`, `test`, `prod`) |
| `SPRING_DATASOURCE_URL` | Backend | `jdbc:h2:mem:sage_dev;...` | Database connection JDBC URL (PostgreSQL in prod, H2 in dev) |
| `SPRING_DATASOURCE_USERNAME` | Backend | `sa` (dev) / `sage_user` | Database connection username |
| `SPRING_DATASOURCE_PASSWORD` | Backend | *empty* (dev) / secret | Database connection password |
| `JWT_SECRET` | Backend | `sage_dev_jwt_secret_key_...` | HMAC-SHA256 signing secret for authentication tokens (min 32 chars) |
| `JWT_EXPIRATION_MS` | Backend | `86400000` (24h) | JWT access token lifespan in milliseconds |
| `INTERNAL_SERVICE_TOKEN` | Backend + Python | `sage_internal_dev_service_...` | Shared secret for inter-service communication guard |
| `AUTOMATION_SERVICE_URL` | Backend | `http://localhost:8000` | URL where Spring Boot reaches the Python FastAPI service |
| `ENFORCE_DOC_COMPLETENESS` | Backend | `true` | Enforces pre-submission compliance gate on application submit |
| `GEMINI_API_KEY` | Backend | *empty* (optional) | Google Gemini API key for live SAGE AI 4-pillar analysis |
| `GEMINI_MODEL` | Backend | `gemini-flash-lite-latest` | Model used for document review and anomaly detection |
| `AUTOMATION_PORT` | Automation | `8000` | Port for FastAPI Python Automation Service |
| `AUTOMATION_HOST` | Automation | `0.0.0.0` | Bind host for FastAPI Uvicorn server |
| `BACKEND_SERVICE_URL` | Automation | `http://localhost:8080` | URL where Python reaches Spring Boot |
| `VITE_API_BASE_URL` | Frontend | `http://localhost:8080` | Base URL used by browser for Core REST API calls |

---

## <a id="demo-accounts"></a>👥 Pre-Seeded Demonstration Accounts & Persona Matrix

All demo accounts share the uniform password: **`Password123!`**  
These accounts can also be activated with a single click using the **Synthetic Demo Accounts Panel** on the login page (`/login`):

| Persona &amp; Role | Login Email | Default Password | Jurisdictional Scope &amp; Target Records |
| :--- | :--- | :--- | :--- |
| **AICTE Central Administrator** | `admin@aicte.gov.in` | `Password123!` | Full nationwide authority, user management, policy rules, and system health diagnostics. |
| **Northern Regional Officer** | `regional@aicte.gov.in` | `Password123!` | Northern Region (Delhi, Punjab, Haryana, UP). Scrutiny queue, clause checklist evaluation, deficiency notices. |
| **Institution Officer (Delhi Tech)** | `officer@delhi-tech.edu` | `Password123!` | Scoped to Delhi Institute of Advanced Engineering (PID: `1-500123498`). Application `APP-202425-1-500123498-8769`. |
| **Institution Officer (Mumbai)** | `officer@mumbai-engg.edu` | `Password123!` | Scoped to Mumbai Academy of Pharmaceutical Sciences (PID: `1-500987654`). Application `APP-2024-MUM-002`. |
| **Compliance Officer** | `compliance@aicte.gov.in` | `Password123!` | National safety surveillance, certificate countdown timers, and on-demand automated sweeps. |
| **EVC Coordinator** | `evc.coord@aicte.gov.in` | `Password123!` | Expert Visit Committee operations hub, expert roster, and CP-SAT committee allocation. |
| **Statutory Regulatory Auditor** | `auditor@aicte.gov.in` | `Password123!` | Read-only forensic inspection across institutions, SHA-256 fingerprint checks, and correlation ID search. |

---

## <a id="testing-suite"></a>🧪 Automated Testing & Verification Suite

SAGE includes a comprehensive, multi-layer automated test suite across all three subsystems:

```text
========================================================================================
SAGE VERIFIED TEST MATRIX
========================================================================================
Subsystem            Test Framework                Tests Executed   Pass Rate   Status
----------------------------------------------------------------------------------------
Core Backend         JUnit 5 + MockMvc + Mockito   83 tests         100%        PASSING
Automation Engine    Pytest + AnyIO + FastAPI      15 tests         100%        PASSING
Frontend Web Portal  Vitest + Testing Library      42 tests         100%        PASSING
Visual E2E Suite     Chrome DevTools Protocol      11 steps         100%        VERIFIED
----------------------------------------------------------------------------------------
TOTAL                Unified Test Matrix           140+ Tests       100%        ALL GREEN
========================================================================================
```

### Running Tests

#### Run All Test Suites (One Command)
```bash
./scripts/test.sh all
```

#### Run Backend Tests Only
```bash
./scripts/test.sh backend
# Or natively with Maven:
cd backend && ./mvnw test
```

#### Run Python Automation Tests Only
```bash
./scripts/test.sh automation
# Or natively with Pytest:
cd automation && source venv/bin/activate && pytest -v
```

#### Run Frontend Tests Only
```bash
./scripts/test.sh frontend
# Or natively with Vitest:
cd frontend && npm test -- --run
```

#### Run Visual End-to-End Verification Across All 9 Steps
```bash
automation/venv/bin/python3 scripts/verify_all_steps_visually.py
```
*Captures high-resolution screenshots for all 9 steps in `docs/screenshots/` and validates UI flows.*

---

## <a id="api-documentation"></a>📖 Interactive API Documentation

When SAGE is running locally, access full interactive API documentation and health endpoints:

- **Spring Boot Core REST API (Swagger 3.0 / OpenAPI)**:  
  [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)  
  *OpenAPI JSON Spec*: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

- **Python Automation &amp; OCR Engine (FastAPI Swagger)**:  
  [http://localhost:8000/docs](http://localhost:8000/docs)  
  *ReDoc Alternative*: [http://localhost:8000/redoc](http://localhost:8000/redoc)

- **Backend Health &amp; Diagnostic Metrics (Spring Boot Actuator)**:  
  [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)  
  [http://localhost:8080/actuator/info](http://localhost:8080/actuator/info)

---

## <a id="regulatory-traceability"></a>📜 Regulatory Alignment & APH 2024–27 Traceability

SAGE directly implements and enforces specific clauses from the **AICTE Approval Process Handbook (APH 2024–25 to 2026–27)**:

| APH Clause | Regulatory Mandate | SAGE Feature Enforcement |
| :--- | :--- | :--- |
| **Clause 1.4** | Land &amp; Built-up Area Norms (Min 2.5 acres urban / 5.0 acres rural) | Automated Scrutiny Engine validates land survey numbers, title deeds, and area norms. |
| **Clause 2.1** | Fire Safety &amp; Structural Stability Certificate Validity | OCR extracts issue and expiry dates; continuous surveillance triggers 30-day alerts. |
| **Clause 2.4** | Introduction of New Courses / Disciplines | Structured 6-document checklist, curriculum review, and Pre-Submission Gate locking. |
| **Clause 2.4(c)**| Faculty Cadre Ratios (1:20 UG Engineering; Prof : Assoc : Asst = 1 : 2 : 6) | Deterministic scrutiny evaluator checks faculty matrices against student intake proposals. |
| **Clause 2.4(d)**| Dedicated Laboratory &amp; Infrastructure Readiness | Verifies hardware/software inventory and equipment lists before EVC scheduling. |
| **Chapter III** | Expert Visit Committee (EVC) Composition &amp; Physical Visit Norms | Google OR-Tools CP-SAT solver enforces 3-expert panels, no Sundays, and zero home-state COI. |
| **Annexure 4** | Legal Affidavit Formats on Non-Judicial Stamp Paper (Min ₹100) | SAGE AI Pillar 3 checks for official municipal seals and verifies stamp paper valuation. |
| **APH 2024–27** | Anti-Tampering &amp; Document Chain of Custody | SHA-256 document hashing, magic byte verification, and immutable version preservation (`v1` &rarr; `v2`). |

---

## <a id="troubleshooting-faq"></a>🔧 Troubleshooting & Frequently Asked Questions

### 1. Port Conflict (`Port 8080 / 8000 / 5173 currently occupied`)
- **Fix**: The `./run.sh` script automatically detects and frees occupied ports. If running manually:
  ```bash
  # Linux / macOS:
  npx kill-port 8080 8000 5173
  # Or:
  fuser -k -9 8080/tcp 8000/tcp 5173/tcp
  ```

### 2. `JAVA_HOME` points to an older Java version (e.g. Java 17 or 11)
- **Fix**: SAGE requires **Java 21 LTS**. Point `JAVA_HOME` to your Java 21 installation:
  ```bash
  # Linux:
  export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
  export PATH=$JAVA_HOME/bin:$PATH

  # macOS:
  export JAVA_HOME=$(/usr/libexec/java_home -v 21)
  export PATH=$JAVA_HOME/bin:$PATH
  ```

### 3. Python OCR Service: `ModuleNotFoundError: No module named 'indra_ocr'`
- **Fix**: Ensure the Python virtual environment is activated before installing dependencies:
  ```bash
  cd automation
  python3 -m venv venv
  source venv/bin/activate
  pip install --upgrade pip
  pip install -r requirements.txt
  ```

### 4. Database: `Connection refused: localhost:5432`
- **Fix**: By default, SAGE runs in `dev` profile using an embedded in-memory H2 database in PostgreSQL compatibility mode, meaning **no external database is required**. If you explicitly set `SPRING_PROFILES_ACTIVE=prod`, ensure your local PostgreSQL 16 server is running or use `docker compose up db`.

### 5. Frontend: `Vite development server not reachable`
- **Fix**: Ensure Node.js 18+ is installed (`node -v`). Clean and reinstall `node_modules`:
  ```bash
  cd frontend
  rm -rf node_modules package-lock.json
  npm install
  npm run dev
  ```

---

## <a id="directory-layout"></a>📁 Repository Directory Layout

```text
SAGE/
├── backend/                       # Spring Boot 3.4.3 / Java 21 Core Backend
│   ├── src/main/java/org/aicte/sage/
│   │   ├── config/                # OpenAPI, Security, Async, and Scheduling configs
│   │   ├── modules/
│   │   │   ├── applications/      # Application entity, 5 pathways, SubmissionReadinessService
│   │   │   ├── audit/             # Immutable Audit Outbox pattern, AuditEvent entity
│   │   │   ├── auth/              # JWT provider, token revocation cache, AuthController
│   │   │   ├── automation/        # Internal HTTP client for Python OCR service
│   │   │   ├── compliance/        # ComplianceEngineService, sweep findings, risk grading
│   │   │   ├── deadlines/         # Statutory certificate expiry countdown calculations
│   │   │   ├── documents/         # Upload, SHA-256 hash, Gemini 4-pillar analysis
│   │   │   ├── evc/               # Google OR-Tools CP-SAT discrete scheduler & registry
│   │   │   ├── governance/        # Regional jurisdictions, committee appointments
│   │   │   ├── institutions/      # Institution profiles, Permanent Institute IDs (PID)
│   │   │   ├── jobs/              # Distributed scheduler locks (distributed_job_locks)
│   │   │   ├── notifications/     # In-app alerts, SHA-256 deduplication, dispatchers
│   │   │   ├── scrutiny/          # APH clause checklist evaluator (1.4, 2.1, 3.2, 4.1)
│   │   │   ├── users/             # User management, role mappings
│   │   │   └── workflow/          # Statutory state machine, optimistic locking
│   │   └── common/                # Exceptions, RFC 7807 problem details, security filters
│   ├── src/main/resources/
│   │   ├── db/migration/          # Flyway versioned migrations (V1 through V18)
│   │   ├── db/seed/               # Synthetic demonstration accounts and records
│   │   ├── application.yml        # Base configuration
│   │   └── application-dev.yml    # Development profile (H2 in-memory mode)
│   ├── pom.xml                    # Maven dependency definitions
│   └── run.sh                     # Dedicated backend runner
├── automation/                    # Python 3.11+ / FastAPI OCR & Automation Service
│   ├── app/
│   │   ├── api/v1/                # OCR, health, and internal audit endpoints
│   │   ├── core/                  # Security token validator, settings
│   │   └── services/              # Indra OCR engine (45ms/page) + Tesseract fallback
│   ├── requirements.txt           # Python library dependencies
│   └── Dockerfile                 # Container specification
├── frontend/                      # React 19 + TypeScript + Vite Web Portal
│   ├── src/
│   │   ├── api/                   # Typed API clients (applications, documents, evc, etc.)
│   │   ├── components/
│   │   │   ├── common/            # Dual-Pane Inspector modal, TopGovBar, StatusBadge
│   │   │   ├── dashboard/         # 6 tailored persona dashboards
│   │   │   └── layout/            # Sidebar, breadcrumbs, navigation
│   │   ├── context/               # AuthContext, ThemeContext, AccessibilityContext
│   │   ├── pages/                 # Applications, Compliance, EVC, AuditLogViewer, etc.
│   │   └── styles/                # AICTE government design system, high contrast CSS
│   ├── package.json               # Node.js dependencies and scripts
│   └── vite.config.ts             # Vite configuration with proxy rules
├── docs/                          # Architectural specifications, reports, and manuals
│   ├── SAGE_FINAL_MASTER_DEMONSTRATION_GUIDE.pdf # Printable 6-page Master PDF (2.92 MB)
│   ├── SAGE_COMPLETE_DEMONSTRATION_MASTER_GUIDE.md # 9-step feature-focused demonstration guide
│   ├── screenshots/               # Verified live screenshots of all 9 steps
│   ├── AWS_EC2_DEPLOYMENT.md      # Production AWS EC2 deployment runbook
│   └── SAGE-FINAL-PROJECT-REPORT.md # Comprehensive final technical audit report
├── infrastructure/                # Container orchestration & reverse proxy configs
│   ├── docker-compose.yml         # Local multi-service development compose
│   ├── docker-compose.prod.yml    # Production container stack
│   └── Caddyfile                  # Automated TLS / HTTPS reverse proxy config
├── scripts/                       # Operational & verification scripts
│   ├── dev.sh                     # Individual service runner
│   ├── test.sh                    # Unified test runner
│   ├── verify_all_steps_visually.py # Automated 9-step CDP visual verification runner
│   └── generate_final_master_pdf.py # ReportLab PDF generation script
├── run.sh                         # Unified one-click platform runner script
├── .env.example                   # Environment configuration template
└── README.md                      # Primary project documentation
```

---

## <a id="project-documentation"></a>📚 Project Documentation & Evaluation Artifacts

Comprehensive operational manuals, architectural audits, and visual verification artifacts are included in this repository:

1. **Master Printable Demonstration Guide (PDF)**:  
   [docs/SAGE_FINAL_MASTER_DEMONSTRATION_GUIDE.pdf](docs/SAGE_FINAL_MASTER_DEMONSTRATION_GUIDE.pdf)  
   *(6 Pages, 2.92 MB, High-Resolution Live Screenshots embedded, Feature-Focused Pitch Script, Jury Q&amp;A Cheat Sheet)*
2. **Feature-Focused Demonstration Script &amp; Screen Breakdown**:  
   [docs/SAGE_COMPLETE_DEMONSTRATION_MASTER_GUIDE.md](docs/SAGE_COMPLETE_DEMONSTRATION_MASTER_GUIDE.md)
3. **Verified Live Screenshots Gallery**:  
   [docs/screenshots/](docs/screenshots/)  
   *(Contains high-resolution captures for Steps 1 through 9, including Dual-Pane Inspector, OCR extraction, SAGE AI fraud alerts, and CP-SAT scheduler)*
4. **AWS EC2 Production Deployment Runbook**:  
   [docs/AWS_EC2_DEPLOYMENT.md](docs/AWS_EC2_DEPLOYMENT.md)
5. **System Architecture &amp; Security Model**:  
   [docs/architecture.md](docs/architecture.md) &bull; [docs/security-model.md](docs/security-model.md)
6. **Database Schema &amp; Flyway Migration Design**:  
   [docs/database-design.md](docs/database-design.md)
7. **Comprehensive Final Project Report**:  
   [docs/SAGE-FINAL-PROJECT-REPORT.md](docs/SAGE-FINAL-PROJECT-REPORT.md)

---

<p align="center">
  <strong>SAGE — System for Approval Governance and Evaluation</strong>
  <br/>
  <em>Smart India Hackathon (SIH 2026) • Problem Statement 26262</em>
  <br/>
  Ministry of Education • All India Council for Technical Education (AICTE)
</p>
