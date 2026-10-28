# Requirements Traceability Matrix — SIH Problem Statement 26262

**Problem Title**: Integrated AICTE Approval Workflow Automation, Compliance Monitoring and Alert Management System (PS 26262)

---

## 1. Traceability Matrix

| Requirement ID | Problem Statement Requirement | Phase 1 Architectural Realization | Module & Location | Status |
|---|---|---|---|---|
| **REQ-AUTH-01** | Secure authentication for AICTE officials and institutions | JWT authentication, BCrypt hashing, stateless security filters | `backend/src/main/java/org/aicte/sage/modules/auth/` | **Phase 1 Complete** |
| **REQ-AUTH-02** | Role-based access control (Admin, Regional, Scrutiny, EVC, College) | Strict Spring Security `@PreAuthorize` and role-permission mapping | `backend/src/main/java/org/aicte/sage/common/security/` | **Phase 1 Complete** |
| **REQ-TENANT-01** | Prevent unauthorized access to other institutions' records (IDOR/BOLA) | Custom `TenancyAuthorizationEvaluator` enforcing resource-level isolation | `backend/src/main/java/org/aicte/sage/common/security/TenancyAuthorizationEvaluator.java` | **Phase 1 Complete** |
| **REQ-AUDIT-01** | Tamper-evident audit trail of regulatory and security actions | Append-only `audit_events` table capturing principal, IP, correlation ID | `backend/src/main/java/org/aicte/sage/modules/audit/` | **Phase 1 Complete** |
| **REQ-DB-01** | Relational, version-controlled schema for institutions and credentials | PostgreSQL schema with Flyway migrations (`V1`, `V2`) | `backend/src/main/resources/db/migration/` | **Phase 1 Complete** |
| **REQ-API-01** | Standardized, traceable API contracts with error safety | RFC 7807 Problem Details, `X-Correlation-ID` header filter | `backend/src/main/java/org/aicte/sage/common/api/` | **Phase 1 Complete** |
| **REQ-AUTO-01** | Automation service foundation without direct DB corruption | FastAPI microservice with clean abstract worker interfaces | `automation/app/` | **Phase 1 Complete** |
| **REQ-UI-01** | Professional administrative frontend shell with role awareness | React 18/19 SPA with Vite, modern design system, health status view | `frontend/src/` | **Phase 1 Complete** |
| **REQ-WORKFLOW-01** | End-to-end multi-stage approval workflow state machine | Domain module boundary designed; state machine engine planned for Phase 2 | `backend/src/main/java/org/aicte/sage/modules/workflow/` | *Phase 2 Planned* |
| **REQ-SCRUTINY-01** | Automated deficiency calculation against APH norms | Scrutiny module boundary designed; Python rule scoring planned for Phase 2 | `backend/src/main/java/org/aicte/sage/modules/scrutiny/` | *Phase 2 Planned* |
| **REQ-EVC-01** | Expert Visit Committee scheduling and geo-optimization | Module boundary designed; optimization algorithms planned for Phase 3 | `automation/app/interfaces/evc_optimization.py` | *Phase 3 Planned* |
| **REQ-ALERT-01** | Regulatory deadline tracking and multi-channel alert escalation | Deadlines boundary designed; scheduler interfaces planned for Phase 4 | `automation/app/interfaces/deadline_monitoring.py` | *Phase 4 Planned* |
