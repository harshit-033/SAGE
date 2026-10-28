# Requirements Traceability Matrix — SIH Problem Statement 26262

**Problem Title**: Integrated AICTE Approval Workflow Automation, Compliance Monitoring and Alert Management System (PS 26262)

---

## 1. Traceability Matrix

| Requirement ID | Problem Statement Requirement | Phase Realization & Capabilities | Module & Location | Status |
|---|---|---|---|---|
| **REQ-AUTH-01** | Secure authentication for AICTE officials and institutions | JWT authentication, BCrypt cryptographic hashing (`$2a$10$`), stateless security filters, logout audit logging, rate limiting considerations | `backend/src/main/java/org/aicte/sage/modules/auth/` | **Phase 2 Verified** |
| **REQ-AUTH-02** | Granular Role-based access control (Admin, Regional, Scrutiny, EVC, Committee, Compliance, Auditor, College) | Normalized roles (`ROLE_AICTE_ADMIN`, `ROLE_REGIONAL_OFFICER`, `ROLE_SCRUTINY_OFFICER`, `ROLE_EVC_COORDINATOR`, `ROLE_EVC_MEMBER`, `ROLE_COMMITTEE_MEMBER`, `ROLE_COMPLIANCE_OFFICER`, `ROLE_AUDITOR`, `ROLE_INSTITUTION_OFFICER`) and granular statutory permissions | `backend/src/main/java/org/aicte/sage/common/security/`, `V3__phase2_institution_details_and_roles.sql` | **Phase 2 Verified** |
| **REQ-TENANT-01** | Multi-tenant isolation preventing cross-institution data leakage (IDOR / BOLA) | Declarative method-level `@tenancyEvaluator.canUpdateInstitution(authentication, #id)` and `@tenancyEvaluator.hasAccessToInstitution`. Violations rejected with HTTP 403 Problem Details and logged as `TENANCY_VIOLATION_ATTEMPT` | `backend/src/main/java/org/aicte/sage/common/security/TenancyAuthorizationEvaluator.java`, `InstitutionControllerTenancyTest.java` | **Phase 2 Verified** |
| **REQ-INST-01** | Technical Institution profile & lifecycle management | Full institutional profile (PID, category, GIS coordinates, address, contacts, website, establishment year, active status), pagination, search, status toggles, user affiliations | `backend/src/main/java/org/aicte/sage/modules/institutions/`, `frontend/src/pages/Institutions.tsx`, `InstitutionProfile.tsx` | **Phase 2 Verified** |
| **REQ-USER-01** | Identity and user account governance | User CRUD, role assignment modal, account suspension/activation (self-suspension prevented), BCrypt hashing, duplicate protection | `backend/src/main/java/org/aicte/sage/modules/users/`, `frontend/src/pages/UserManagement.tsx` | **Phase 2 Verified** |
| **REQ-AUDIT-01** | Immutable regulatory audit trail of consequential system actions | Consequential actions (login, logout, user creation, role assignment, status change, cross-tenant violations) logged with actor, client IP, correlation ID, and searchable in UI | `backend/src/main/java/org/aicte/sage/modules/audit/`, `frontend/src/pages/AuditLogViewer.tsx` | **Phase 2 Verified** |
| **REQ-DB-01** | Relational, version-controlled schema governance | Versioned Flyway migrations `V1`, `V2`, `V3`, `V4` executing ANSI SQL identically on PostgreSQL and test H2 harness | `backend/src/main/resources/db/migration/`, `db/seed/` | **Phase 2 Verified** |
| **REQ-API-01** | Standardized, traceable API contracts with error safety | RFC 7807 Problem Details, `X-Correlation-ID` header filter, pagination DTOs, Bean Validation | `backend/src/main/java/org/aicte/sage/common/api/` | **Phase 2 Verified** |
| **REQ-AUTO-01** | Automation service foundation without direct DB corruption | FastAPI microservice with clean abstract worker interfaces, internal token authentication | `automation/app/` | **Phase 2 Verified** |
| **REQ-UI-01** | Official AICTE-inspired government design system & accessible shell | Restrained palette (midnight navy `#003366`, government blue `#005aaa`, saffron `#f75700`, light surfaces `#f8fafc`), A-/A/A+ font scaling, high contrast mode, WCAG 2.2 AA focus rings, breadcrumbs, structured tables | `frontend/src/index.css`, `components/layout/`, `TopGovBar.tsx`, `Breadcrumbs.tsx` | **Phase 2 Verified** |
| **REQ-WORKFLOW-01** | End-to-end multi-stage approval workflow state machine | Approval workflow engine and state machine transitions | `backend/src/main/java/org/aicte/sage/modules/workflow/` | *Phase 3 Planned* |
| **REQ-SCRUTINY-01** | Automated deficiency calculation against APH norms | Scrutiny rule engine and OCR document analysis | `backend/src/main/java/org/aicte/sage/modules/scrutiny/`, `automation/` | *Phase 3 Planned* |
| **REQ-EVC-01** | Expert Visit Committee scheduling and geo-optimization | EVC panel allocations, conflict-of-interest prevention, geographic clustering | `automation/app/interfaces/evc_optimization.py` | *Phase 3 Planned* |
| **REQ-ALERT-01** | Regulatory deadline tracking and multi-channel alert escalation | Deadlines monitoring and notification dispatch | `automation/app/interfaces/deadline_monitoring.py` | *Phase 4 Planned* |
