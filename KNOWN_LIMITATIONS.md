# SAGE — Known Limitations & Prototype Boundaries

**System:** SAGE (System for Approval Governance and Evaluation)  
**Hackathon:** Smart India Hackathon (SIH) 2026 — Problem Statement 26262  
**Document Purpose:** Transparent engineering disclosure of prototype design boundaries, assumptions, and future production enhancements.

---

## 1. Prototype Scope Clarification

SAGE is developed as an advanced demonstration prototype for the Smart India Hackathon (SIH) 2026. Its primary objective is to prove the viability, algorithmic correctness, strict regulatory governance, and operational usability of automated approval workflows, multi-tenant RBAC, deterministic scrutiny, and mathematical committee scheduling.

It is **NOT** currently connected to live government production networks (such as actual NIC servers, live DigiLocker production gateways, or live State Fire Department APIs).

---

## 2. Identified Limitations by Subsystem

### 1. Document Storage & Malware Inspection
- **Current Implementation**:
  - Validates file size limits (15 MB maximum).
  - Sanitizes filenames against directory traversal (`Paths.get().getFileName()`).
  - Enforces strict binary signature / magic byte inspection (`%PDF`, PNG `0x89 0x50 0x4E 0x47`, JPEG `0xFF 0xD8 0xFF`).
  - Stores files on local container filesystem (`backend/data/documents`) referenced by unique UUIDs.
- **Prototype Boundary**:
  - Live ClamAV daemon antivirus scanning was deferred to prevent excessive memory and CPU consumption during demonstration deployments.
  - Files are stored on a local persistent volume rather than an AWS S3 / MinIO distributed object store with cross-region replication.
- **Production Path**: Integrate ClamAV daemon via streaming socket and configure AWS S3 / Azure Blob Storage adapter with server-side KMS encryption.

### 2. Document Intelligence & OCR Engine
- **Current Implementation**:
  - FastAPI automation microservice utilizes `IndraOcrAdapter` with automatic fallback to `TesseractOcrAdapter` (`pytesseract` + `pypdfium2`).
  - Performs regex-based date normalization and certificate number extraction.
  - Scores field extraction confidence and flags uncertainty for human scrutiny review.
- **Prototype Boundary**:
  - The prototype OCR models are optimized for standard certificates (Fire Safety, Building Occupancy, Land Deeds). Complex non-standard handwritten regional certificates or degraded historical photocopies may exhibit lower confidence scores and will trigger the human scrutiny fallback gate.
  - Test suites execute against representative benchmark corpus PDFs rather than millions of historical AICTE records.
- **Production Path**: Train fine-tuned TrOCR or LayoutLMv3 models on historical AICTE document archives for improved handwritten and tabular extraction.

### 3. Database Architecture & Multi-Tenancy Model
- **Current Implementation**:
  - Relational schema with Flyway migrations V1 through V16.
  - Single-database shared-schema architecture with row-level discriminator columns (`institution_id`, `state`).
  - Fail-closed authoritative application-level enforcement (`AuthoritativeAuthorizationService`).
  - In-memory H2 database is used for rapid, zero-dependency unit and integration testing (`mvn test`), while PostgreSQL 16 is used in Docker and staging environments.
- **Prototype Boundary**:
  - PostgreSQL Row-Level Security (RLS) policies at the database engine level are simulated via Spring Data JPA specifications and authoritative interceptors rather than native Postgres RLS session variables (`SET LOCAL app.current_tenant = ...`).
- **Production Path**: Enable PostgreSQL native Row-Level Security (RLS) policies combined with HikariCP tenant-aware connection pooling.

### 4. External Integrations & Notifications
- **Current Implementation**:
  - Internal notification and alert engine dispatches in-app alerts and surveillance warnings to role dashboards.
  - External notifications (SMS via CDAC/NIC gateway and Email via NIC SMTP) are logged in the audit trail as structured dispatch events.
- **Prototype Boundary**:
  - Live telecom SMS gateways (CDAC DLT) and official NIC email relays are mocked to ensure offline demonstration reliability without requiring live government API keys.
- **Production Path**: Connect to CDAC SMS Gateway API and NIC GovMail SMTP server using production credentials.

### 5. High Availability & Distributed Locks
- **Current Implementation**:
  - Single-instance Spring Boot and FastAPI services.
  - Flyway table `distributed_job_locks` manages cooperative locking for scheduled tasks (e.g. daily document expiry checks).
- **Prototype Boundary**:
  - Does not include Redis Sentinel or Hazelcast cluster topologies for distributed caching or session sharing.
  - In-memory token authentication relies on stateless JWTs, which allows horizontal scaling of the backend, but token revocation is handled via user status checks rather than a distributed Redis blocklist.
- **Production Path**: Introduce Redis cluster for distributed token blocklisting, query caching, and Redlock distributed scheduling.

---

## 3. Risk Assessment & Mitigations

| Risk | Impact | Prototype Mitigation | Production Mitigation |
| :--- | :--- | :--- | :--- |
| File Traversal in Document Upload | High | Canonical path verification against base directory; random UUID storage filenames | S3 bucket isolation with IAM roles |
| Premature Application Approval | Critical | Strict state machine guards rejecting approval without mandatory EVC / SHC clearance | Four-eyes cryptographic digital signatures on approval letters |
| Cross-College Data Leakage | Critical | Fail-closed tenant scoping in repository queries; explicit parameter rejection | Postgres Row-Level Security (RLS) + database-level encryption |
| OR-Tools CP-SAT Infeasibility | Medium | Informative diagnostic result (`INFEASIBLE`) returned to UI prompting coordinator to adjust date window or expand candidate pool | Automated relaxation heuristics and waitlist queuing |
