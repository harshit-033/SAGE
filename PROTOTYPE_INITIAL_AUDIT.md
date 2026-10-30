# SAGE — Prototype Initial Technical Audit

**System:** SAGE (System for Approval Governance and Evaluation)  
**Hackathon:** Smart India Hackathon (SIH) 2026 — Problem Statement 26262  
**Audit Date:** October 30, 2026  
**Auditor:** SAGE Core Systems & Security Engineering  

---

## 1. Executive Summary

This initial technical audit documents the baseline architecture, operational status, test health, and identified defects of the SAGE prototype before remediation. SAGE is an AICTE Approval Workflow Automation, Compliance Monitoring, and Alert Management demonstration platform.

The core services (Spring Boot backend, FastAPI automation service, PostgreSQL database, and Vite React frontend) are structurally sound, but several critical (P0/P1) authorization fail-open vulnerabilities, CI linter errors, authorization inconsistencies, and database migration concerns were identified.

---

## 2. Existing Architecture

```
                    +------------------------------------------+
                    |           React 19 Frontend              |
                    |    TypeScript / Vite / React Router      |
                    |   (Port 5173 / Production Web Build)    |
                    +--------------------+---------------------+
                                         |
                       REST APIs / JWT   |  Bearer Auth
                                         v
                    +--------------------+---------------------+
                    |       Spring Boot 3.3 Backend            |
                    | Java 21 / Spring Security / Data JPA     |
                    |   Flyway Migrations / OR-Tools CP-SAT    |
                    |             (Port 8080)                  |
                    +---------+--------------------+-----------+
                              |                    |
        Internal HTTP Service |                    | PostgreSQL JDBC
         Token Authentication |                    | (Port 5432)
                              v                    v
+-----------------------------+-------+   +--------+------------------+
|    FastAPI Python Service           |   |   PostgreSQL Database     |
| Python 3.12 / Indra OCR / Tesseract |   | Relational Approval Store |
|    PyPDFium2 / OpenCV / Pillow      |   |  Audit Logs & Credentials |
|             (Port 8000)             |   +---------------------------+
+-------------------------------------+
```

---

## 3. Current Working Modules

| Component / Subsystem | Implementation Status | Functional Description |
|---|---|---|
| **Spring Boot Core & Security** | Working | JWT authentication, BCrypt encryption, Spring Security filters, centralized Problem Details exception handling. |
| **OR-Tools EVC Scheduling** | Working | Google OR-Tools CP-SAT constraint programming solver allocating active experts to inspection panels with conflict avoidance. |
| **FastAPI OCR & Extraction** | Working | Dual-engine OCR (Indra OCR with Tesseract fallback) for institutional PDF/image verification and field extraction. |
| **Frontend User Interface** | Working | Standardized typography, institutional color tokens, 6 role dashboards, approval application forms, document verification tabs. |
| **Audit Logging Subsystem** | Working | Immutable regulatory audit event logging capturing principal, institution, action, resource, and timestamp. |
| **Automated Scrutiny Engine** | Working | Rule-based document and application scrutiny detecting missing mandatory approvals and compliance deficiencies. |

---

## 4. Current Test Status

| Test Suite | Environment | Current Result | Notes |
|---|---|---|---|
| **Backend Suite** | Maven / JUnit 5 / MockMvc | **91 / 91 Passed** (100%) | Covers authentication, RBAC, workflows, EVC scheduling, audit logging. |
| **Automation Suite** | Pytest / AnyIO / FastAPI TestClient | **15 / 15 Passed** (100%) | Covers health, OCR upload, security token enforcement, schema validation. |
| **Frontend Unit Suite** | Vitest / Testing Library / JSDOM | **58 / 58 Passed** (100%) | Covers dashboards, navigation, message box, dialogs, status badges. |
| **Frontend TypeScript** | `tsc --noEmit` | **0 Errors** (100% clean) | Strict TypeScript compiler validation passes. |
| **Frontend ESLint** | `npm run lint` | **12 Errors** (Failed) | Fast Refresh context exports, unused variables, `no-unused-expressions`. |
| **Frontend Build** | `npm run build` | **Succeeded** (dist generated) | Minified production bundle generated in 350ms. |

---

## 5. Identified Defects & Priority Classification

Defects are categorized according to SIH prototype demonstration criteria:
- **P0**: Prevents startup, authentication, core workflows, or compromises data isolation/RBAC.
- **P1**: Breaks important demonstration functionality or CI pipelines.
- **P2**: Minor defects or usability improvements that do not block demonstration.
- **P3**: Production-oriented improvements that can be deferred.

### Priority P0 — Critical Security & Data Isolation Vulnerabilities

1. **[P0] Institution Officer Fail-Open in Application Listing**:
   - *Location*: `backend/src/main/java/org/aicte/sage/modules/applications/service/ApplicationService.java` (lines 274–282)
   - *Defect*: `principal.getAuthorizedInstitutionIds().stream().findFirst().orElse(null)` evaluates to `null` if the officer has no assigned institutions. In `searchApplications()`, passing `null` disables the institution filter, exposing applications of all institutions nationwide.
   - *Remediation*: Check if the authorized institution list is empty; if so, immediately return an empty page response or access denied. Support all authorized institution IDs rather than only the first one.

2. **[P0] Institution Officer Fail-Open in Document Listing**:
   - *Location*: `backend/src/main/java/org/aicte/sage/modules/documents/service/DocumentComplianceService.java` (lines 109–125)
   - *Defect*: Similar to application listing, an empty institution list causes `effectiveInstId = null`, querying documents across all technical institutions nationwide.
   - *Remediation*: Enforce authorized institution ID check; reject or return empty results when no institution is assigned.

3. **[P0] Missing Jurisdiction Scoping for Regional Officers in Application & Document Listing**:
   - *Location*: `ApplicationService.java` & `DocumentComplianceService.java`
   - *Defect*: `ROLE_REGIONAL_OFFICER` is not checked against `JurisdictionAssignmentRepository`. A regional officer currently sees applications and documents from unauthorized regions across the country.
   - *Remediation*: Query active regional jurisdictions for regional officers and filter applications/documents by the authorized states/regions.

4. **[P0] Inconsistent Authorization for EVC Coordinators**:
   - *Location*: `DefaultEvcSchedulingService.java` (lines 388–399 vs lines 320–375)
   - *Defect*: `filterAndMapInspections()` permits `ROLE_EVC_COORDINATOR` to view all inspections without checking `authService.checkAccessToInstitution()`. However, detail endpoints (`getInspectionById`, `getCommitteeAssignments`, `cancelInspection`) invoke `authService.checkAccessToInstitution()`, which rejects EVC Coordinators because `ROLE_EVC_COORDINATOR` is not recognized as having authority in `canAccessInstitution()`.
   - *Remediation*: Explicitly include `ROLE_EVC_COORDINATOR` in `canAccessInstitution()` for inspection oversight, ensuring list, detail, and scheduling operations behave consistently.

---

### Priority P1 — Demonstration & CI Blocking Issues

5. **[P1] Frontend ESLint Failures Blocking GitHub Actions CI**:
   - *Location*:
     - `frontend/src/components/common/MessageBox.tsx` (unused ternary expressions)
     - `frontend/src/components/dashboard/ComplianceDashboard.tsx` (unused `err` variable)
     - `frontend/src/context/AuthContext.tsx` (Fast Refresh non-component exports)
     - `frontend/src/context/MessageContext.tsx` (Fast Refresh non-component exports)
     - `frontend/src/pages/EvcScheduling.tsx` (explicit `any` types)
     - `frontend/src/tests/EvcScheduling.test.tsx` (explicit `any` types)
     - `frontend/src/tests/MessageBoxAndRoleNavigation.test.tsx` (explicit `any` types)
   - *Defect*: 12 ESLint errors cause the frontend CI job to fail, preventing deployment pipelines.
   - *Remediation*: Refactor code to eliminate unused expressions, properly type parameters, and structure context definitions to satisfy Fast Refresh rules.

6. **[P1] Flyway Migration Mutability vs Demo Seeding**:
   - *Location*: `scripts/generate_demo_seed.py` dynamically modified `V16__...sql` Flyway migration.
   - *Defect*: Flyway migrations must remain immutable once calculated; altering migration files invalidates checksums and breaks repeated deployments.
   - *Remediation*: Keep Flyway migrations fixed. Maintain repeatable demo seeding via dedicated non-Flyway seed scripts.

---

### Priority P2 — Usability & Demonstration Polish

7. **[P2] Hardcoded Docker Hostnames in Browser API Requests**:
   - *Location*: Docker Compose environment and frontend default configs.
   - *Defect*: Client browsers cannot resolve `http://backend:8080` or `http://automation:8000` because these hostnames only exist within the Docker bridge network.
   - *Remediation*: Ensure the frontend points to `http://localhost:8080` (or reverse-proxy relative path `/api`) when accessed from a host browser.

8. **[P2] Chairperson Qualification Logic in EVC Scheduling**:
   - *Location*: `SchedulePersistenceService.java` assigns the first sorted expert as Chairperson.
   - *Defect*: Statutory chairperson status requires explicit eligibility criteria (e.g. Professor cadre or explicit designation).
   - *Remediation*: Clearly identify chairperson assignment rules as configurable prototype criteria.

---

### Priority P3 — Deferred Production Architecture

9. **[P3] External Antivirus Scanner**:
   - *Assessment*: Scanning uploaded documents via an external ClamAV daemon is a production concern; server-side MIME type inspection, file signature validation, and size limiting are sufficient for the prototype.

10. **[P3] Advanced Secrets Management**:
    - *Assessment*: Production HashiCorp Vault or AWS KMS integration is out of scope for an SIH prototype; predictable environment variables and demo credentials are appropriate.

---

## 6. Audit Conclusion & Action Plan

The inspection confirms that SAGE has complete backend, ML, and frontend modules, but requires immediate P0/P1 remediation in:
1. **Frontend ESLint fixes** to restore CI pipeline pass status.
2. **Backend RBAC enforcement** for Institution Officer fail-closed scoping, Regional Officer state filtering, and EVC Coordinator access parity.
3. **Dedicated authorization tests** verifying positive and negative isolation scenarios.
4. **Comprehensive documentation** covering deployment, RBAC matrix, and demonstration scripts.
