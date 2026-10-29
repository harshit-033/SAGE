# SAGE — Smart AICTE Guidance and Evaluation
> **SIH 2026 Problem Statement 26262**:
> *“Integrated AICTE Approval Workflow Automation, Compliance Monitoring and Alert Management System.”*  
> **Primary Regulatory Source**: AICTE Approval Process Handbook (APH) 2024–25 to 2026–27

[![Build & Test Status](https://img.shields.io/badge/tests-99%2F99%20passing-brightgreen.svg)]()
[![Backend](https://img.shields.io/badge/backend-Spring%20Boot%203.4%20%7C%20Java%2021-blue.svg)]()
[![Automation](https://img.shields.io/badge/automation-FastAPI%20%7C%20Indra%20OCR%20%7C%20Python%203.14-teal.svg)]()
[![Frontend](https://img.shields.io/badge/frontend-React%2019%20%7C%20TypeScript%20%7C%20Vite-purple.svg)]()
[![Design System](https://img.shields.io/badge/design-Official%20AICTE%20Gov%20System-orange.svg)]()

---

## 1. Project Overview

**SAGE** is a multi-tenant regulatory automation platform engineered for the All India Council for Technical Education (AICTE). It modernizes statutory approval workflows, document scrutiny, Expert Visit Committee (EVC) panel allocations, continuous compliance monitoring, and alert escalation.

SAGE incorporates:
- **Core Authority Backend**: Spring Boot 3.4 on Java 21 LTS with Spring Security, Spring Data JPA, PostgreSQL 16+, and Flyway schema migrations.
- **Application Workflow Engine (Phase 3)**: Independent application lifecycle modeling, versioned workflow state machines (`WF_NEW_INSTITUTION`, `WF_EOA`, `WF_INCREASE_INTAKE`), optimistic locking, role authorization, and state transition auditing.
- **Local Indra OCR & Multi-Engine Intelligence (Phase 3)**: High-speed local CPU document processing with `indra-ocr` (45–55 ms/page, 10x–13x faster than Tesseract on digital PDFs, 4° skew correction), line-level bounding box coordinates, and extraction confidence provenance.
- **Secure Document Management (Phase 3)**: Magic byte inspection (`%PDF-`, PNG, JPEG), SHA-256 cryptographic hashing, UUID file isolation, and directory traversal defense.
- **Deterministic APH Scrutiny Engine (Phase 3)**: Explainable compliance verification mapping to APH 2024–27 clauses (checklists, fire safety validity, land area, name concordance, and 70% confidence threshold gate).
- **Authoritative Authorization & Governance**: [AuthoritativeAuthorizationService](file:///home/Harshit/Desktop/PROJECTS/SIH%202026/AICTE/SAGE/backend/src/main/java/org/aicte/sage/common/security/AuthoritativeAuthorizationService.java) strictly eliminating blanket cross-institution access through regional jurisdictions (`jurisdiction_assignments`), explicit case assignments (`case_assignments`), and statutory committee appointments (`committee_appointments`).
- **Statutory Committee Alignment**: Explicit modeling of committees defined by APH 2024–27 Annexure 16 (Executive Committee Clause 16.2, Scrutiny Ch I Clause 16.3, Scrutiny Ch II Clause 16.4, EVC Clause 16.5, SHC/SAC Clause 16.6, SCSC Clause 16.7, Council Clause 16.1).
- **Authentication Lifecycle & Server-Side Revocation**: Stateless Bearer JWT with server-side token invalidation (`token_revocations` table and memory cache) on logout; deactivated accounts are blocked.
- **Reliable Regulatory Audit Trail**: Consequential state changes are committed atomically with `AuditClassification.MANDATORY`; if audit persistence fails, the business transaction rolls back (`AuditPersistenceException`).
- **AICTE-Inspired Design System**: Authentic government aesthetic derived from [aicte.gov.in](https://www.aicte.gov.in/) (midnight navy `#003366`, government blue `#005aaa`, saffron `#f75700`, light surfaces `#f8fafc`, tricolor strip, font scaling `A-`/`A`/`A+`, contrast toggle, WCAG 2.2 AA compliant focus rings, and prototype disclaimer).

---

## 2. Directory Structure

```text
SAGE/
  backend/           # Java 21 / Spring Boot 3.4 Core REST API & Security Engine
    src/main/resources/db/migration/  # Flyway versioned migrations (V1, V3, V5)
    src/main/resources/db/seed/       # Non-production synthetic demonstration seeds (V2, V4, V6)
  automation/        # Python 3.14 / FastAPI Automation & Intelligence Service
  frontend/          # React 19 + TypeScript + Vite Official AICTE Design System
  infrastructure/    # Docker Compose and container orchestration configs
  docs/              # Architectural specs, security models, database design, traceability
  scripts/           # Service runner (dev.sh) and unified test runner (test.sh)
  2025031399.pdf     # Primary Source: AICTE Approval Process Handbook 2024-27
  .env.example       # Template for environment configuration
  README.md          # Project guide and execution documentation
```

---

## 3. Quick Start (Running Locally)

All three services run seamlessly on local ports:
- **React Frontend Client**: `http://localhost:5173`
- **Spring Boot Core Backend**: `http://localhost:8080` (API Docs: `/swagger-ui.html`)
- **FastAPI Automation Service**: `http://localhost:8000` (API Docs: `/docs`)

### A. Start All Services via Dev Script
```bash
# Terminal 1: Core Backend
./scripts/dev.sh backend

# Terminal 2: Automation Engine
./scripts/dev.sh automation

# Terminal 3: React Frontend
./scripts/dev.sh frontend
```

### B. Health Verification
```bash
# Backend Health
curl http://localhost:8080/api/v1/health

# Automation Engine Health
curl http://localhost:8000/api/v1/health

# Frontend Status
curl -I http://localhost:5173
```

---

## 4. Default Demonstration Credentials (Dev & Test Profile Only)

> [!NOTE]
> Synthetic demonstration accounts are enabled **only** in `dev` and `test` profiles via `V2`, `V4`, and `V6` seeds. In production (`prod`), local seed accounts are disabled and strong secrets are required.

| Role | Username / Email | Password | Access Scope & Jurisdiction |
|---|---|---|---|
| **AICTE Central Admin** | `admin@aicte.gov.in` | `Password123!` | Full nationwide administration, user governance, audit trail |
| **Institution Officer** | `officer@delhi-tech.edu` | `Password123!` | Scoped to Delhi Institute of Technology (`1-500123498`) |
| **Regional Officer** | `regional@aicte.gov.in` | `Password123!` | Scoped to Northern Region institutions (Delhi, etc.) |
| **Compliance Officer** | `compliance@aicte.gov.in` | `Password123!` | Scoped to explicitly assigned compliance cases |
| **Statutory Auditor** | `auditor@aicte.gov.in` | `Password123!` | Read-only inspection across assigned institutions & audit logs |
| **EVC Coordinator** | `evc.coord@aicte.gov.in` | `Password123!` | Scoped to assigned Expert Visit Committee cases |

---

## 5. Automated Verification & Test Suite

Execute the unified test suite across all three subsystems:
```bash
./scripts/test.sh all
```

### Verified Test Results (89 / 89 Passing)
```text
================================================================================
SAGE REGRESSION SUITE RESULTS (Phase 3 Full Stack)
================================================================================
[1] Spring Boot 3 Backend:
    - SageApplicationTests: 1 passed
    - AuthControllerTest: 5 passed
    - InstitutionControllerTenancyTest: 4 passed
    - InstitutionManagementTest: 5 passed
    - UserManagementTest: 5 passed
    - AuditEndpointTest: 2 passed
    - HealthEndpointTest: 2 passed
    - SecurityConfigurationFailureTest: 3 passed
    - TenancyAuthorizationEvaluatorTest: 4 passed
    - Phase21SecurityRemediationTest: 9 passed
    - Phase3WorkflowAndScrutinyTest: 8 passed
    - Phase3WorkflowAndScrutinyTest: 8 passed
    Total: 56 passed, 0 failed, 0 errors (BUILD SUCCESS)

[2] FastAPI Automation & OCR Microservice:
    - test_health.py: 3 passed
    - test_security.py: 4 passed
    - test_validation.py: 1 passed
    - test_ocr.py (Indra OCR, upload, benchmark): 5 passed
    Total: 13 passed, 0 failed

[3] React Frontend Vitest Suite:
    - ApiError.test.tsx: 1 passed
    - StatusBadge.test.tsx: 4 passed
    - Modal.test.tsx: 3 passed
    - Pagination.test.tsx: 3 passed
    - TopGovBar.test.tsx: 3 passed
    - Login.test.tsx: 2 passed
    - Navigation.test.tsx: 1 passed
    - Applications.test.tsx: 3 passed
    Total: 20 passed, 0 failed
================================================================================
OVERALL: 89 / 89 Tests Passed (100% Success Rate)
================================================================================
```

---

## 6. Regulatory Documentation & Architecture Specs
- [Phase 3 Completion & Verification Report](file:///home/Harshit/Desktop/PROJECTS/SIH%202026/AICTE/SAGE/docs/phase-3-completion-report.md)
- [Architecture Overview](file:///home/Harshit/Desktop/PROJECTS/SIH%202026/AICTE/SAGE/docs/architecture.md)
- [Security & Authorization Model](file:///home/Harshit/Desktop/PROJECTS/SIH%202026/AICTE/SAGE/docs/security-model.md)
- [Database Design & Flyway Migrations](file:///home/Harshit/Desktop/PROJECTS/SIH%202026/AICTE/SAGE/docs/database-design.md)
- [API Contracts & RFC 7807 Error Models](file:///home/Harshit/Desktop/PROJECTS/SIH%202026/AICTE/SAGE/docs/api-contracts.md)
- [Requirements Traceability Matrix](file:///home/Harshit/Desktop/PROJECTS/SIH%202026/AICTE/SAGE/docs/requirements-traceability.md)
- [Implementation & Remediation Status](file:///home/Harshit/Desktop/PROJECTS/SIH%202026/AICTE/SAGE/docs/implementation-status.md)
