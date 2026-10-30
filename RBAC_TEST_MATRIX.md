# SAGE — Role-Based Access Control (RBAC) & Tenancy Test Matrix

**System:** SAGE (System for Approval Governance and Evaluation)  
**Hackathon:** Smart India Hackathon (SIH) 2026 — Problem Statement 26262  
**Standard:** AICTE Approval Process Handbook (APH 2024-27) & Statutory Security Norms  

---

## 1. Statutory Roles Overview

SAGE implements authoritative, least-privilege Role-Based Access Control (RBAC) across six primary regulatory and institutional roles:

1. **`ROLE_AICTE_ADMIN` (Central Admin)**: Central AICTE regulatory governance with nationwide administrative visibility, system health monitoring, user account provisioning, and statutory override authority.
2. **`ROLE_REGIONAL_OFFICER` (Regional Officer)**: Territorial jurisdiction over designated states (e.g., Northern, Western, Southern, Eastern). Authoritative scrutiny, deficiency notifications, fast-track EoA approval, and regional hearing management.
3. **`ROLE_INSTITUTION_OFFICER` (Institution Officer)**: Strict single-tenant self-service applicant. Manages draft creation, specification entry, document uploads, deficiency rectifications, and voluntary withdrawals for own affiliated institution only.
4. **`ROLE_COMPLIANCE_OFFICER` (Compliance Officer)**: Specialized statutory surveillance role. Oversees building safety, fire NOCs, environmental clearances, expiry monitoring, and corrective action workflows nationwide.
5. **`ROLE_EVC_COORDINATOR` (EVC Coordinator)**: Specialized inspection management role. Executes automated OR-Tools CP-SAT committee allocation, manages the peer evaluators registry, and coordinates inspection logistics.
6. **`ROLE_AUDITOR` (Statutory Auditor)**: Independent, read-only oversight authority. Accesses immutable system audit trails, tracking state transitions, administrative actions, and security violation attempts without write permissions.

---

## 2. Resource-Level Permission & Tenancy Matrix

| Resource Domain | Operation | Central Admin | Regional Officer | Institution Officer | Compliance Officer | EVC Coordinator | Statutory Auditor | Enforcing Security Component | Verified Test Class |
| :--- | :--- | :---: | :---: | :---: | :---: | :---: | :---: | :--- | :--- |
| **Institutions** | List / Search | Nationwide | Regional States | Own College Only | Nationwide | Nationwide | Nationwide | `AuthoritativeAuthorizationService` | `Phase2RbacAndDataIsolationTest` |
| | Get Details | Allow | Regional Only | Own College Only | Allow | Allow | Allow | `checkAccessToInstitution` | `Phase2RbacAndDataIsolationTest` |
| | Update Profile | Allow | Deny | Own College (if `INSTITUTION_WRITE`) | Deny | Deny | Deny | `canUpdateInstitution` | `TenancyIsolationTest` |
| **Applications** | Create Draft | Allow | Deny | Own College Only | Deny | Deny | Deny | `@PreAuthorize('APPLICATION_CREATE')` | `Phase3WorkflowAndScrutinyTest` |
| | List / Search | Nationwide | Regional States | Own College Only (Fail-Closed) | Assigned Cases | Inspection Scope | Nationwide (Read-Only) | `ApplicationService.listApplications` | `Phase2RbacAndDataIsolationTest` |
| | Get Details | Allow | Regional States | Own College Only | Assigned Cases | Allow (Inspection) | Allow | `checkAccessToInstitution` | `Phase2RbacAndDataIsolationTest` |
| | Submit Draft | Allow | Deny | Own College Only | Deny | Deny | Deny | `WorkflowEngineService.validateTransitionAuthority` | `Phase3WorkflowAndScrutinyTest` |
| | Resubmit Deficient | Allow | Deny | Own College Only | Deny | Deny | Deny | `validateTransitionAuthority` | `Phase3WorkflowAndScrutinyTest` |
| | Advance Scrutiny | Allow | Regional States | Deny (403) | Deny (403) | Deny (403) | Deny (403) | `validateTransitionAuthority` | `Phase3WorkflowAndScrutinyTest` |
| | Schedule EVC | Allow | Regional States | Deny (403) | Deny (403) | Allow | Deny (403) | `validateTransitionAuthority` | `EvcAuthorizationTest` |
| | Grant Approval | Allow | Regional States (EoA fast-track or post-EVC) | Deny (403) | Deny (403) | Deny (403) | Deny (403) | `validateTransitionAuthority` | `Phase3WorkflowAndScrutinyTest` |
| | Withdraw Application | Allow | Allow | Own College (Draft/Deficient) | Deny (403) | Deny (403) | Deny (403) | `validateTransitionAuthority` | `Phase3WorkflowAndScrutinyTest` |
| **Documents** | Upload Document | Allow | Deny | Own College Only | Deny | Deny | Deny | `@PreAuthorize('DOCUMENT_UPLOAD')` | `DocumentSecurityTest` |
| | List Documents | Nationwide | Regional States | Own College Only | Nationwide | Inspection Scope | Nationwide | `DocumentComplianceService.listDocuments` | `Phase2RbacAndDataIsolationTest` |
| | Preview / Download | Allow | Regional States | Own College Only | Allow | Allow | Allow | `loadFileAsResource` + `checkAccessToInstitution` | `Phase2RbacAndDataIsolationTest` |
| | Replace (New Version)| Allow | Deny | Own College Only | Deny | Deny | Deny | `replaceDocument` | `Phase4DeadlinesAlertsComplianceTest`|
| | Review / Verify | Allow | Regional States | Deny (403) | Allow (Safety Docs) | Deny (403) | Deny (403) | `reviewDocument` | `Phase4DeadlinesAlertsComplianceTest`|
| | Submit Clarification| Allow | Deny | Own College Only | Deny | Deny | Deny | `submitClarification` | `Phase4DeadlinesAlertsComplianceTest`|
| **EVC Scheduling**| List Inspections | Nationwide | Regional States | Own College Only | Deny (403) | Nationwide | Nationwide (Read-Only) | `DefaultEvcSchedulingService` | `EvcAuthorizationTest` |
| | Run CP-SAT Solver | Allow | Regional States | Deny (403) | Deny (403) | Allow | Deny (403) | `EvcSchedulingService.generateSchedule` | `EvcAuthorizationTest` |
| | Persist Schedule | Allow | Regional States | Deny (403) | Deny (403) | Allow | Deny (403) | `SchedulePersistenceService` | `EvcPersistenceAndServiceTest` |
| | Cancel Inspection | Allow | Regional States | Deny (403) | Deny (403) | Allow | Deny (403) | `cancelInspection` | `EvcAuthorizationTest` |
| **Alerts & Deadlines**| List Alerts | Nationwide | Regional States | Own College Only | Nationwide | Deny (403) | Nationwide (Read-Only) | `AlertService.listAlerts` | `Phase4DeadlinesAlertsComplianceTest`|
| | Acknowledge Alert | Allow | Regional States | Own College Only | Allow | Deny (403) | Deny (403) | `AlertService.acknowledgeAlert` | `Phase4DeadlinesAlertsComplianceTest`|
| | Escalate Alert | Allow | Regional States | Deny (403) | Allow | Deny (403) | Deny (403) | `AlertService.escalateAlert` | `Phase4DeadlinesAlertsComplianceTest`|
| | Resolve Alert | Allow | Regional States | Own College Only | Allow | Deny (403) | Deny (403) | `AlertService.resolveAlert` | `Phase4DeadlinesAlertsComplianceTest`|
| **Audit Outbox** | View Audit Trail | Allow | Deny (403) | Deny (403) | Deny (403) | Deny (403) | Allow | `@PreAuthorize('AUDIT_READ')` | `Phase2GovernanceTest` |
| | Mutate Audit Log | Deny (System) | Deny (System) | Deny (System) | Deny (System) | Deny (System) | Deny (System) | Append-only database constraints | `Phase1AuthTest` |
| **User Mgmt** | Create / Update User| Allow | Deny (403) | Deny (403) | Deny (403) | Deny (403) | Deny (403) | `@PreAuthorize('USER_WRITE')` | `UserManagementTest` |

---

## 3. Automated Test Verification Results

### Suite 1: Multi-Tenancy & Data Scoping (`Phase2RbacAndDataIsolationTest.java`)
- `testUnassignedInstitutionOfficerReceivesEmptyListFailClosed()`: **PASS** (Zero leakage for unassigned accounts)
- `testInstitutionOfficerAccessOwnInstitutionApplications()`: **PASS** (Access limited strictly to affiliated college)
- `testInstitutionOfficerCannotAccessOtherInstitutionApplicationsViaParameter()`: **PASS** (Parameter injection triggers HTTP 403 `tenant-access-denied`)
- `testInstitutionOfficerCannotViewOtherInstitutionApplicationDetails()`: **PASS** (Direct UUID navigation triggers HTTP 403 `tenant-access-denied`)
- `testRegionalOfficerSeesOnlyJurisdictionStates()`: **PASS** (Northern Region strictly constrained to DL, UP, UK, HR)
- `testRegionalOfficerCannotAccessOtherRegionViaParameter()`: **PASS** (Western region request by Northern officer rejected)
- `testCentralAdminSeesNationwideApplications()`: **PASS** (Global nationwide visibility verified)
- `testEvcCoordinatorCanAccessApplicationsForInspectionManagement()`: **PASS** (Statutory inspection authority verified)

### Suite 2: State Transitions & Scrutiny Engine (`Phase3WorkflowAndScrutinyTest.java`)
- `testInstitutionOfficerCanSubmitDraft()`: **PASS** (DRAFT -> SUBMITTED)
- `testInstitutionOfficerCannotDirectlyApprove()`: **PASS** (Applicant cannot approve own application)
- `testScrutinyOfficerCanEvaluateAndAdvanceWorkflow()`: **PASS** (SUBMITTED -> UNDER_SCRUTINY -> SCRUTINY_PASSED)
- `testPrematureApprovalRejectionWithoutMandatoryEvc()`: **PASS** (Rejects approval of New Institution without EVC inspection)
- `testDeficiencyResolutionFlow()`: **PASS** (DEFICIENT -> Correction -> RESUBMITTED -> Scrutiny Resolution)

### Suite 3: EVC Solver & Committee Access (`EvcAuthorizationTest.java`, `EvcPersistenceAndServiceTest.java`)
- `testCoordinatorCanTriggerSolver()`: **PASS** (EVC Coordinator executes CP-SAT solver)
- `testInstitutionOfficerCannotTriggerSolver()`: **PASS** (Applicant denied access to panel dispatch engine)
- `testRescheduleRejectionWithoutFlag()`: **PASS** (Prevents accidental override of confirmed inspection schedules)
- `testSundayExclusionConstraint()`: **PASS** (Solver never schedules inspections on Sundays)
- `testConflictOfInterestExclusion()`: **PASS** (Affiliated experts never assigned to home institution)

---

## 4. Tenancy Violation Logging & Audit Invariants

When an unauthorized cross-tenant request is detected:
1. `AuthoritativeAuthorizationService` immediately records an audit event:
   - Event Type: `TENANCY_VIOLATION_ATTEMPT`
   - Principal: Authenticated username
   - Target Resource: `INSTITUTION:<UUID>`
   - Correlation ID: Unique request trace UUID
2. `TenantIsolationException` is thrown.
3. `GlobalExceptionHandler` converts the exception to RFC 7807 Problem Details:
   - HTTP Status: `403 Forbidden`
   - Type URI: `https://api.aicte.gov.in/errors/tenant-access-denied`
   - Detail: `"Cross-tenant access violation: Principal '[email]' is not authorized to access Institution ID: '[UUID]'"`
