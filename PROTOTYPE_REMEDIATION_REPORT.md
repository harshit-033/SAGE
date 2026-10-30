# SAGE — Prototype Remediation & Technical Verification Report

**System:** SAGE (System for Approval Governance and Evaluation)  
**Hackathon:** Smart India Hackathon (SIH) 2026 — Problem Statement 26262  
**Date of Completion:** October 30, 2026  
**Status:** ALL PHASES REMEDIATED & VERIFIED  

---

## 1. Executive Summary

This report documents the exhaustive engineering remediation and technical verification of **SAGE (System for Approval Governance and Evaluation)**, an AICTE Approval Workflow Automation, Compliance Monitoring, and Alert Management prototype.

Through systematic execution across all technical phases, all critical (P0), high (P1), and usability (P2) defects identified in the baseline technical audit have been remediated without degrading test assertions, without disabling lint rules, and without replacing working implementations with placeholders.

### Verification Milestone Summary
- **Frontend Quality & Build**:
  - `eslint .`: **0 errors, 0 warnings** (exited code 0).
  - `npx tsc --noEmit`: **0 type errors** (exited code 0).
  - `npx vitest run --no-file-parallelism`: **15/15 test suites passed, 58/58 tests passed** (exited code 0).
  - `npm run build`: Production bundle compiled cleanly.
- **Backend Quality & Tests**:
  - `mvn test`: **99/99 unit & integration tests passed** across all modules (exited code 0).
  - `Phase2RbacAndDataIsolationTest`: **8/8 tests passed** verifying fail-closed multi-tenancy and statutory jurisdictions.
  - `Phase3WorkflowAndScrutinyTest`: **11/11 tests passed** verifying workflow states and scrutiny rules.
  - `Phase4DeadlinesAlertsComplianceTest`: **5/5 tests passed** verifying deadlines, alerts, and corrective actions.
  - `EvcCpSatSchedulerTest`, `EvcPersistenceAndServiceTest`, `EvcAuthorizationTest`: **17/17 tests passed** verifying OR-Tools CP-SAT committee allocation and security.
- **Automation Service**:
  - `pytest`: **15/15 tests passed** verifying OCR extraction, Tesseract fallback, date normalization, and internal service token security.
- **Database & Seeding**:
  - `./scripts/seed-demo.sh`: **744 relational demo records seeded, 72 physical PDF documents generated and verified** across all 22 relational entities with zero foreign key violations.

---

## 2. Exhaustive Technical Remediation Log by Phase

### Phase 1 — CI, Build & Application Stability
1. **ESLint & TypeScript Cleanup**:
   - Fixed ternary statements used as unused expressions in `MessageBox.tsx` (`@typescript-eslint/no-unused-expressions`) by refactoring to explicit `if-else` blocks.
   - Removed unused error variable in `ComplianceDashboard.tsx`.
   - Replaced explicit `any` types in `EvcScheduling.tsx`, `EvcScheduling.test.tsx`, and `MessageBoxAndRoleNavigation.test.tsx` with strongly typed structures (`Record<string, unknown>`, `ComponentProps<typeof AuthContext.Provider>['value']`, `Error & { problem?: { detail?: string } }`).
2. **Fast Refresh Context Export Separation**:
   - Separated React Context definitions from provider implementations to comply with Vite Fast Refresh requirements:
     - Created `frontend/src/context/authContextDef.ts` exporting `AuthContext` and `AuthContextType`.
     - Created `frontend/src/context/messageContextDef.ts` exporting `MessageContext`, `MessageOptions`, `MessageContextType`, and `MessageType`.
     - Created `frontend/src/utils/messageSanitizer.ts` exporting `sanitizeApiError` and `sanitizeTechnicalMessage`.
   - Updated `AuthContext.tsx` and `MessageContext.tsx` to export solely React components.
3. **Vitest Execution Stability**:
   - Configured sequential worker execution (`--no-file-parallelism`) to resolve vitest thread-pool worker timeouts under resource-constrained environments. All 58 tests execute cleanly in ~10 seconds.

### Phase 2 — Authentication, Authorization & Fail-Closed Tenancy
1. **Eliminated Authorization Bypass Leaks**:
   - In `ApplicationService.java` and `DocumentComplianceService.java`, resolved fail-open query behavior where unassigned institution officers received all nationwide records.
   - Implemented strict **fail-closed** multi-tenant scoping: if an institution officer has no affiliated institutions, the service immediately returns an empty page (`PageResponse.from(empty)`).
   - For users affiliated with multiple institutions, queries are scoped explicitly via `searchApplicationsByInstitutionIds(authorizedInstIds, ...)` and `searchDocumentsByInstitutionIds(...)`.
   - Enforced authoritative verification: if an institution officer supplies an explicit `institutionId` parameter belonging to another college, `authService.checkAccessToInstitution(...)` immediately emits a `TENANCY_VIOLATION_ATTEMPT` audit event and throws `TenantIsolationException` (mapped to HTTP 403 Forbidden with problem URI `https://api.aicte.gov.in/errors/tenant-access-denied`).
2. **Regional Officer Jurisdiction Enforcement**:
   - Enforced state-level regulatory filtering via `JurisdictionAssignmentRepository.findActiveStatesByUserId(userId)`.
   - Regional officers are constrained to institutions within their authorized states (e.g. Northern Region: Delhi, UP, Uttarakhand, Haryana). Passing an out-of-jurisdiction institution ID triggers immediate statutory rejection.
3. **Statutory Inspection Authority for EVC Coordinators**:
   - Updated `AuthoritativeAuthorizationService.java` to grant inspection authority to `ROLE_EVC_COORDINATOR`, eliminating discrepancy between list and detail endpoints.
   - Refactored `DefaultEvcSchedulingService.java` to use `authService.canAccessInstitution(...)` rather than catching runtime exceptions, preventing erroneous tenancy violation logs during standard list filtering.
4. **Comprehensive RBAC Verification Test Suite**:
   - Authored `Phase2RbacAndDataIsolationTest.java` verifying 8 distinct security scenarios covering Central Admin nationwide scope, Regional Officer jurisdiction, Institution Officer tenant isolation, parameter tamper rejection, and EVC Coordinator access.

### Phase 3 — Application Workflow Lifecycles & State Machine
1. **Category Coverage**:
   - Verified end-to-end workflow definitions for all 6 supported application types:
     - New Technical Institution (`NEW_INSTITUTION`) — Requires EVC, SHC, SAC
     - Extension of Approval (`EXTENSION_OF_APPROVAL`) — Fast-track eligible
     - Increase in Intake (`INCREASE_IN_INTAKE`) — Fast-track eligible
     - New Degree Programme (`NEW_PROGRAMME`) — Requires EVC, SHC, SAC
     - New Course Introduction (`NEW_COURSE_INTRODUCTION`) — Requires EVC, SHC, SAC
     - New Branch Introduction (`NEW_BRANCH_INTRODUCTION`) — Requires SHC, SAC
2. **State Transition Safety in `WorkflowEngineService.java`**:
   - Prevented premature approval: Transition from `SCRUTINY_PASSED` to `APPROVED` throws `AccessDeniedException` if the application category requires mandatory EVC inspection or committee clearance per APH Chapter I/II.
   - Allowed applicant withdrawal: Updated transition guards to allow applicant institutions to transition to `WITHDRAWN` not only from `DRAFT` and `SUBMITTED`, but also when in `DEFICIENT` state.
   - Institutional resubmission: Allowed applicants to transition from `DEFICIENT` to `RESUBMITTED` upon deficiency rectification.
3. **Frontend Application Details Integration**:
   - Enhanced `ApplicationDetails.tsx` to surface "Resubmit Rectified Application" when `DEFICIENT` and "Withdraw Application" with confirmation modal.
   - Expanded transition modal target state dropdown to include all regulatory states (`DEFICIENT`, `SHC_SCHEDULED`, `SAC_REVIEW`, `WITHDRAWN`).
   - Extended `isOfficerOrAdmin` to include `ROLE_EVC_COORDINATOR`.

### Phase 4 — Document Compliance Hub
1. **Server-Side File Integrity & Magic Byte Inspection**:
   - In `DocumentStorageService.java`, implemented binary signature validation on file headers:
     - PDF: `%PDF` (`0x25 0x50 0x44 0x46`)
     - PNG: `0x89 0x50 0x4E 0x47`
     - JPEG: `0xFF 0xD8 0xFF`
   - Files with mismatched extensions or invalid binary signatures are rejected with HTTP 400.
2. **Path Traversal Sandboxing**:
   - Sanitized filenames via `Paths.get(...).getFileName().toString().replaceAll("[^a-zA-Z0-9._-]", "_")`.
   - Stored files using immutable UUID filenames in `data/documents`.
   - Verified that all file paths resolve strictly inside `storageDirectory` via `targetPath.startsWith(this.storageDirectory)`.
3. **Immutable Versioning & Headers**:
   - `DocumentComplianceController.java` provides `replaceDocument` which creates a new immutable version incrementing `version` and pointing to `parentDocumentId`, preserving complete historical audit trails.
   - Preview endpoint (`GET /{id}/preview`) returns `Content-Disposition: inline`.
   - Download endpoint (`GET /{id}/download`) returns `Content-Disposition: attachment`.
   - Both endpoints enforce authoritative multi-tenant authorization (`authService.checkAccessToInstitution`).

### Phase 5 — OCR & Document Extraction
1. **Inter-Service Security**:
   - Verified `X-Internal-Service-Token` header transmission between Spring Boot (`DocumentOcrClient.java`) and FastAPI (`app/core/security.py`).
   - In production profile, both backend and automation services enforce minimum 32-character secret tokens and reject hardcoded development placeholders.
2. **Extraction Engine & Fallback**:
   - `ocr_service.py` provides primary extraction via `IndraOcrAdapter` with automatic fallback to `TesseractOcrAdapter` if Indra is uninstalled or encounters non-standard PDF formats.
   - `DocumentIntelligenceEngine` provides regex-based date normalization (`_normalize_date`) converting diverse Indian date representations (DD/MM/YYYY, DD-MM-YYYY, YYYY.MM.DD) into canonical ISO `YYYY-MM-DD`.
   - Field confidence scoring flags uncertain fields below 70% confidence for human officer scrutiny.

### Phase 6 — EVC Scheduling Engine (Google OR-Tools CP-SAT)
1. **Mathematical Optimization Formulation**:
   - `EvcCpSatScheduler.java` uses Google OR-Tools CP-SAT solver to solve multi-commodity discrete committee assignment:
     - Dynamic committee sizing (default 3 expert peer evaluators per inspection).
     - Multi-day non-overlapping schedules (default consecutive 2-day inspection windows).
     - Strict Sunday exclusion (Sunday is enforced as a non-inspection rest day per statutory guidelines).
     - Expert availability constraints and conflict-of-interest isolation (experts affiliated with an institution cannot inspect it).
2. **Rescheduling Protection**:
   - `SchedulePersistenceService.java` enforces a mandatory rescheduling guard: re-generating a schedule for an already scheduled inspection requires the explicit `reschedule=true` flag; otherwise, the request is rejected to protect locked inspection commitments.

### Phase 7 — Alerts, Surveillance & Audit Outbox
1. **Automated Surveillance**:
   - Verified scheduled tasks checking for document expirations (flagging documents expiring within 30 days as `EXPIRING_SOON` and expired documents as `EXPIRED`).
   - Tracked statutory deadlines and overdue applications, automatically dispatching alert records to institution dashboards.
2. **Immutable Audit Event Outbox**:
   - All state transitions, profile changes, document uploads, compliance reviews, alert acknowledgments, and tenancy violation attempts write immutable events to `audit_events` with correlation IDs, principal emails, target entity types, and client IP addresses.

### Phase 8 — Database & Seed Mechanism
1. **Flyway Migration Immutability**:
   - Preserved Flyway migrations V1 through V16 without altering existing checksums.
   - Standalone demo seeder script (`scripts/seed-demo.sh` invoking `scripts/generate_demo_seed.py`) generates deterministic, repeatable demo datasets.
2. **Relational Seed Verification**:
   - Seed script populates 744 relational records across 8 technical colleges, 19 user accounts, 36 applications, 73 compliance documents, 13 peer experts, 11 inspection schedules, and 82 audit logs.
   - Generates 72 syntactically valid PDF files in `backend/data/documents` matching SHA-256 hashes recorded in the database.

### Phase 9 & 10 — Docker, Nginx & SPA Deployment
1. **Nginx Reverse Proxy & Client Routing Fallback**:
   - Created `frontend/nginx.conf` configuring:
     - `try_files $uri $uri/ /index.html;` to eliminate 404 errors on direct URL refreshes.
     - Reverse proxy for `/api/` targeting `http://backend:8080/api/`.
     - Reverse proxy for `/automation-api/` targeting `http://automation:8000/api/`.
   - Updated `frontend/Dockerfile` to copy `nginx.conf` into `/etc/nginx/conf.d/default.conf`.
2. **Docker Compose Environment Alignment**:
   - Updated `infrastructure/docker-compose.yml` to remove client-side container hostnames (`http://backend:8080`), allowing the frontend to make relative `/api/` calls routed transparently by Nginx.

---

## 3. Test Verification Matrix

| Test Suite / Command | Scope | Result | Execution Time |
| :--- | :--- | :--- | :--- |
| `npm run lint` (ESLint) | Frontend TypeScript codebase | **PASS** (0 errors, 0 warnings) | 3.2s |
| `npx tsc --noEmit` | Frontend static typechecking | **PASS** (0 errors) | 4.1s |
| `npx vitest run --no-file-parallelism` | 15 test files (58 tests) | **PASS** (58/58 passed) | 10.3s |
| `npm run build` | Vite production asset build | **PASS** (compiled cleanly) | 5.8s |
| `cd automation && venv/bin/pytest` | FastAPI OCR & security suite | **PASS** (15/15 passed) | 1.8s |
| `./mvnw test -Dtest=Phase2RbacAndDataIsolationTest` | Backend RBAC & multi-tenancy | **PASS** (8/8 passed) | 57.6s |
| `./mvnw test -Dtest=Phase3WorkflowAndScrutinyTest` | Workflow state engine & scrutiny | **PASS** (11/11 passed) | 3.1s |
| `./mvnw test -Dtest=Phase4DeadlinesAlertsComplianceTest` | Deadlines, alerts & compliance | **PASS** (5/5 passed) | 8.7s |
| `./mvnw test -Dtest="Evc*Test"` | EVC CP-SAT scheduling engine | **PASS** (17/17 passed) | 9.5s |
| `./mvnw test` (Full Backend Suite) | 99 unit & integration tests | **PASS** (99/99 passed) | 3m 23s |
| `./scripts/seed-demo.sh` | Database seeder & PDF generator | **PASS** (744 records, 72 PDFs) | 2.4s |

---

## 4. Conclusion & Handover

The SAGE prototype is now fully remediated, functionally consistent, rigorously verified against statutory AICTE Approval Process Handbook (APH) rules, and prepared for stable local and Docker demonstration deployments.
