# SAGE × EVC Scheduling Engine — Integration & Architecture Documentation

## 1. Executive Summary
Under the AICTE Approval Process Handbook (APH 2024–27), institutions seeking approval for new technical institutions, extension of approval (EoA), or introduction of new supernumerary seats require statutory on-site verification by an Expert Visit Committee (EVC). 

Previously, committee formation and scheduling were managed in an external standalone prototype (`evc-scheduling-engine`). This integration seamlessly embeds the genuine Google OR-Tools CP-SAT constraint optimization engine directly into SAGE's core Java 21 / Spring Boot 3.4.3 backend (`org.aicte.sage.modules.evc`) and React 19 / TypeScript enterprise frontend, maintaining canonical SAGE multi-tenancy, transactional persistence, strict audit logging, and role-based access control (RBAC).

---

## 2. Integrated Architecture

```
                    ┌────────────────────────────────────────┐
                    │       SAGE React 19 Frontend           │
                    │   (/evc/schedule & Role Dashboards)    │
                    └───────────────────┬────────────────────┘
                                        │ JWT Bearer (Correlation-ID)
                                        ▼
                    ┌────────────────────────────────────────┐
                    │      Spring Boot 3.4.3 Backend         │
                    │         EvcScheduleController          │
                    └───────────────────┬────────────────────┘
                                        │
           ┌────────────────────────────┴───────────────────────────┐
           ▼                                                        ▼
┌───────────────────────────────────────┐        ┌───────────────────────────────────┐
│ AuthoritativeAuthorizationService     │        │ AuditService                      │
│ - ROLE_AICTE_ADMIN                    │        │ - EVC_SCHEDULE_GENERATION_INIT    │
│ - ROLE_EVC_COORDINATOR                │        │ - EVC_SCHEDULE_PERSISTED          │
│ - ROLE_REGIONAL_OFFICER (Jurisdiction)│        │ - EVC_SCHEDULE_CANCELLED          │
└──────────────────┬────────────────────┘        └─────────────────┬─────────────────┘
                   │                                               │
                   ▼                                               ▼
┌────────────────────────────────────────────────────────────────────────────────────┐
│ DefaultEvcSchedulingService & SchedulePersistenceService                           │
│ - Retrieves canonical EvcInspection, Application, and Institution entities        │
│ - Filters eligible active experts with verified availability dates                │
│ - Invokes native Google OR-Tools CP-SAT solver                                     │
│ - Atomic transaction commit & application state transition to EVC_SCHEDULED        │
└──────────────────────────────────┬─────────────────────────────────────────────────┘
                                   │
                                   ▼
┌────────────────────────────────────────────────────────────────────────────────────┐
│ EvcCpSatScheduler (Google OR-Tools CP-SAT 9.11.4210)                               │
│ - Strict Sunday exclusion rule                                                     │
│ - Dynamic inspection duration & inclusive end-date calculation                    │
│ - Required committee size enforcement (exactly N experts)                          │
│ - Multi-day date overlap prevention per expert across concurrent inspections        │
│ - Date-offset minimization objective favoring earliest permissible dates          │
│ - Corrected Section 8.1 missing availability diagnosis constraint                  │
└──────────────────────────────────┬─────────────────────────────────────────────────┘
                                   │
                                   ▼
┌────────────────────────────────────────────────────────────────────────────────────┐
│ Canonical SAGE Database (PostgreSQL / Flyway Migrations V1–V13)                    │
│ - evc_inspections (references institutions, applications)                          │
│ - experts & expert_availabilities                                                  │
│ - committee_assignments (composite unique constraints, CHAIRPERSON/MEMBER roles)  │
│ - distributed_job_locks & audit_logs                                               │
└────────────────────────────────────────────────────────────────────────────────────┘
```

---

## 3. Data Authority & Model Resolution

| Concept | Canonical SAGE Source | Notes |
| :--- | :--- | :--- |
| **Institution** | `org.aicte.sage.modules.institution.entity.Institution` | Master tenant record with AICTE permanent ID, regional jurisdiction, and address. |
| **Application** | `org.aicte.sage.modules.application.entity.Application` | Authoritative approval workflow entity. Must be in `UNDER_SCRUTINY` or `SCRUTINY_PASSED` to be eligible for EVC. |
| **EVC Inspection** | `org.aicte.sage.modules.evc.entity.EvcInspection` | Holds inspection duration, member count, statutory window (`earliestStartDate` to `latestStartDate`), and scheduled dates. |
| **Expert** | `org.aicte.sage.modules.evc.entity.Expert` | Registry of verified academic and industrial experts with discipline, designation, and active status. |
| **Availability** | `org.aicte.sage.modules.evc.entity.ExpertAvailabilityRecord` | Authoritative record of confirmed available dates per expert. |
| **Committee Assignment** | `org.aicte.sage.modules.evc.entity.CommitteeAssignment` | Persisted relationship linking an expert to an inspection with assigned role (`CHAIRPERSON` or `MEMBER`) and status. |

---

## 4. Scheduling Model & Mathematical Constraints

The CP-SAT model enforces the following constraints:

### 4.1. Date Window & Sunday Exclusion
- For each inspection $i \in I$ with duration $D_i$ days, the candidate start date $s$ must satisfy:
  $$\text{earliestStartDate}_i \le s \le \text{latestStartDate}_i$$
- **Sunday Exclusion Rule**: If candidate date $d$ is a Sunday ($\text{DayOfWeek} = 7$), it is excluded from on-site inspection execution. The inspection span advances across working days (Monday–Saturday) until $D_i$ valid working days are accumulated.
- **Inclusive End Date**: The scheduled end date $e_i$ is the date of the $D_i$-th working inspection day.

### 4.2. Exact Committee Size
- For each inspection $i$ requiring $M_i$ committee members:
  $$\sum_{e \in E} x_{i,e} = M_i$$
  where $x_{i,e} \in \{0, 1\}$ indicates whether expert $e$ is assigned to inspection $i$.

### 4.3. Expert Eligibility & Availability
- An expert $e$ can only be assigned to inspection $i$ starting on date $s$ if $e$ is active and has verified calendar availability for **all** generated inspection days of $(i, s)$:
  $$x_{i,e} \le \text{isAvailable}(e, s, D_i)$$
- **Section 8.1 Bug Fix**: In previous standalone engine iterations, `diagnoseInfeasibility()` incorrectly treated missing availability records as available. In the integrated engine, missing availability records are strictly treated as unavailable, maintaining mathematical parity between the CP-SAT solver and diagnostic reporting.

### 4.4. Multi-Day Overlap Prevention
- An expert $e$ cannot be assigned to two inspections $i_1, i_2$ whose active inspection days overlap:
  $$\forall d, \sum_{i \in I : d \in \text{Days}(i)} x_{i,e} \le 1$$
- Conflict detection evaluates the full set of generated calendar dates, not merely start dates, correctly preventing collisions across multi-day visits.

### 4.5. Optimization Objective
- The objective minimizes the sum of start-date offsets from the earliest permissible statutory date:
  $$\text{Minimize} \sum_{i \in I} \sum_{s \in S_i} \text{offset}(s) \cdot y_{i,s}$$
  where $y_{i,s} \in \{0, 1\}$ indicates start date $s$ is chosen for inspection $i$. This favors earlier permissible inspection dates while satisfying all feasibility constraints.

---

## 5. Role-Based Access Control & Security Matrix

All EVC endpoints are protected under SAGE's JWT authentication filter and `AuthoritativeAuthorizationService`:

| Role | View Pending / Scheduled Inspections | View Expert Registry | Generate Schedule | Cancel Schedule | Reschedule |
| :--- | :---: | :---: | :---: | :---: | :---: |
| `ROLE_AICTE_ADMIN` | Full (All Regions) | Yes | Yes | Yes | Yes (explicit flag) |
| `ROLE_EVC_COORDINATOR` | Assigned Cases | Yes | Yes | Yes | Yes (explicit flag) |
| `ROLE_REGIONAL_OFFICER` | Regional Jurisdiction Only | Yes | Yes | Yes | Yes (explicit flag) |
| `ROLE_SCRUTINY_OFFICER` | Read-only | Read-only | No (403) | No (403) | No (403) |
| `ROLE_INSTITUTION_OFFICER` | Own Institution (Scheduled Only) | No (403) | No (403) | No (403) | No (403) |
| `ROLE_AUDITOR` | Read-only (All Regions) | Read-only | No (403) | No (403) | No (403) |

---

## 6. Database Migrations

### `V12__evc_scheduling_engine.sql`
Creates the schema for the EVC scheduling engine:
- `experts`: Master registry for accredited experts with unique email and active index.
- `expert_availabilities`: Date-level availability calendar with unique `(expert_id, available_date)`.
- `evc_inspections`: Inspection specifications tied to SAGE `institutions` and `applications`.
- `committee_assignments`: Normalized committee assignments with unique `(inspection_id, expert_id)`.

### `V13__evc_scheduling_seed_data.sql`
Seeds realistic test data for development and regression testing:
- 6 accredited engineering and technical experts across disciplines (Computer Science, Civil, Electrical, Mechanical, Chemical, Electronics).
- Verified date availabilities across November 2026.
- 2 test inspections:
  - `ffffffff-ffff-ffff-ffff-000000000001` (Delhi Institute of Advanced Engineering & Technology, 3 days, 3 members).
  - `ffffffff-ffff-ffff-ffff-000000000002` (Mumbai Academy of Pharmaceutical Sciences, 2 days, 2 members).
- Regional case assignment for `evc.coord@aicte.gov.in`.

---

## 7. REST API Endpoints

Base path: `/api/v1/evc`

| Method | Endpoint | Description | Permitted Roles |
| :--- | :--- | :--- | :--- |
| `GET` | `/inspections/pending` | List pending inspections eligible for scheduling | Admin, Coordinator, Regional |
| `GET` | `/inspections/scheduled` | List confirmed scheduled inspections with committees | Admin, Coordinator, Regional, Auditor, Institution (own) |
| `GET` | `/inspections/{id}` | Retrieve specific inspection details | Role & tenancy scoped |
| `GET` | `/inspections/{id}/committee` | Retrieve assigned committee members | Role & tenancy scoped |
| `GET` | `/experts` | Paginated list of active accredited experts | Admin, Coordinator, Regional, Auditor |
| `POST` | `/schedules` | Trigger Google OR-Tools CP-SAT scheduler | Admin, Coordinator, Regional |
| `POST` | `/inspections/{id}/cancel` | Cancel an inspection and release committee members | Admin, Coordinator, Regional |

---

## 8. Verification & Test Evidence

### Backend Test Execution
- Command: `mvn test` in `backend/`
- Result: **83 of 83 tests passing** (0 failures, 0 errors, 0 skipped).
- EVC Specific Suites:
  - `EvcCpSatSchedulerTest` (7/7 passing): Sunday exclusion, duration math, multi-day overlap, empty expert pools, time limits, and Section 8.1 missing availability diagnostic consistency.
  - `EvcPersistenceAndServiceTest` (3/3 passing): Atomic transaction rollback, persistent retrieval without re-optimization, and authorized rescheduling.
  - `EvcAuthorizationTest` (7/7 passing): Role isolation, institution tenant scoping, regional officer jurisdiction, and forbidden operations.

### Frontend Test Execution
- Command: `npm test -- --run` in `frontend/`
- Result: **42 of 42 tests passing across 13 test suites**.
- `EvcScheduling.test.tsx` (6/6 passing):
  - Header & KPI rendering.
  - Inspection selection & count tracking.
  - CP-SAT execution & optimal result banner display.
  - Infeasibility diagnostic handling.
  - Tab navigation (Pending, Scheduled, Experts).
  - Read-only restrictions for institution officers.
- Production Build: `npm run build` completed with code 0 (`tsc -b && vite build`).

### Python Automation Test Execution
- Command: `pytest tests/ -v` in `automation/`
- Result: **15 of 15 tests passing** (health, OCR validation, security tokens, correlation IDs).

---

## 9. Traceability Matrix

| Requirement | Implementation Component | Test Verification | Status |
| :--- | :--- | :--- | :---: |
| Google OR-Tools CP-SAT Integration | `EvcCpSatScheduler.java` | `EvcCpSatSchedulerTest.testFeasibleScheduleGeneration` | **PASS** |
| Section 8.1 Infeasibility Diagnosis Fix | `EvcCpSatScheduler.java:279` | `EvcCpSatSchedulerTest.testDiagnosticMatchesSolverUnavailableRule` | **PASS** |
| Sunday Exclusion Rule | `EvcCpSatScheduler.computeInspectionDays()` | `EvcCpSatSchedulerTest.testSundayExclusionAdvancesSchedule` | **PASS** |
| Multi-Day Conflict Prevention | `EvcCpSatScheduler.buildModel()` | `EvcCpSatSchedulerTest.testMultiDayOverlappingInspectionsPreventExpertConflict` | **PASS** |
| Canonical Model Tenancy | `EvcInspection`, `Institution`, `Application` | `EvcAuthorizationTest.testInstitutionOfficerCannotAccessOtherInstitutionSchedules` | **PASS** |
| Transactional Persistence | `SchedulePersistenceService.java` | `EvcPersistenceAndServiceTest.testPersistAndRetrieveSchedule` | **PASS** |
| Rescheduling Guard | `DefaultEvcSchedulingService.java` | `EvcPersistenceAndServiceTest.testRescheduleRejectionWithoutFlag` | **PASS** |
| Frontend Workflow & UI | `EvcScheduling.tsx`, `evcApi.ts` | `EvcScheduling.test.tsx` (6 tests) | **PASS** |
| Full Platform Regression Safety | Backend, Automation, Frontend | 83 backend, 42 frontend, 15 automation tests passing | **PASS** |
