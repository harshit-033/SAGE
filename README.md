# SAGE — System for Approval Governance and Evaluation

<p align="center">
  <img src="frontend/public/sage-logo.png" width="150" alt="SAGE Logo" />
  <br/>
  <strong>System for Approval Governance and Evaluation (SAGE)</strong>
  <br/>
  <em>Independent Prototype developed for SIH 2026 Problem Statement 26262</em>
</p>

> **SIH 2026 Problem Statement 26262**:
> *“Integrated AICTE Approval Workflow Automation, Compliance Monitoring and Alert Management System.”*  
> **Primary Regulatory Source**: AICTE Approval Process Handbook (APH) 2024–25 to 2026–27

[![Build & Test Status](https://img.shields.io/badge/tests-175%2F175%20passing%20%7C%2015%2F15%20e2e-brightgreen.svg)]()
[![Backend](https://img.shields.io/badge/backend-Spring%20Boot%203.4%20%7C%20Java%2021-blue.svg)]()
[![EVC Engine](https://img.shields.io/badge/evc%20solver-Google%20OR--Tools%20CP--SAT%209.11-darkblue.svg)]()
[![Automation](https://img.shields.io/badge/automation-FastAPI%20%7C%20Indra%20OCR%20%7C%20Python%203.14-teal.svg)]()
[![Frontend](https://img.shields.io/badge/frontend-React%2019%20%7C%20TypeScript%20%7C%20Vite-purple.svg)]()
[![Design System](https://img.shields.io/badge/design-AICTE%20Inspired%20Gov%20System-orange.svg)]()

> **Final Project Report & Audit**: See [docs/SAGE-FINAL-PROJECT-REPORT.md](docs/SAGE-FINAL-PROJECT-REPORT.md), [docs/DEPLOYMENT_IMPLEMENTATION_REPORT.md](docs/DEPLOYMENT_IMPLEMENTATION_REPORT.md), and [docs/AWS_EC2_DEPLOYMENT.md](docs/AWS_EC2_DEPLOYMENT.md) for complete audits, architectural proofs, deployment runbooks, and verification matrices.

---

## 1. Project Overview

**SAGE** is an independent multi-tenant regulatory automation prototype engineered for Smart India Hackathon (SIH) 2026 Problem Statement 26262. It modernizes statutory approval workflows, document scrutiny, Expert Visit Committee (EVC) panel allocations, continuous compliance monitoring, and alert escalation based on the AICTE Approval Process Handbook (APH).

SAGE incorporates:
- **Core Authority Backend**: Spring Boot 3.4 on Java 21 LTS with Spring Security, Spring Data JPA, PostgreSQL 16+, and Flyway schema migrations (V1 through V18).
- **Integrated EVC Scheduling Engine**: Google OR-Tools CP-SAT 9.11.4210 constraint solver generating conflict-free inspection committees (3 distinct active experts, consecutive 2-day duration, Sunday exclusion, expert availability constraints, atomic persistence).
- **Application Workflow Engine**: Independent application lifecycle modeling, versioned workflow state machines (`WF_NEW_INSTITUTION`, `WF_EOA`, `WF_INCREASE_INTAKE`, `WF_NEW_COURSE_INTRODUCTION`, `WF_NEW_BRANCH_INTRODUCTION`), optimistic locking, role authorization, and state transition auditing across statutory states.
- **New Course & Branch Introduction Workflows**: End-to-end statutory specifications for course discipline, degree type, proposed intake, faculty cadre ratios (1:20 norm), dedicated laboratories, and emerging area branch approvals.
- **Enterprise Document Compliance & Required Actions Tracking**: Dedicated institutional document registry uncoupled from single applications, immutable version replacement (`v1` → `v2`), automated expiry surveillance (`EXPIRING_SOON`, `EXPIRED`), clarification requests, officer reviews, and consolidated corrective action management.
- **Local Indra OCR & Multi-Engine Intelligence**: High-speed local CPU document processing with `indra-ocr` (45–55 ms/page, 10x–13x faster than Tesseract on digital PDFs, 4° skew correction), line-level bounding box coordinates, and extraction confidence provenance with automatic Tesseract fallback.
- **Secure Document Management**: Magic byte inspection (`%PDF-`, PNG, JPEG), SHA-256 cryptographic hashing, UUID file isolation, and directory traversal defense.
- **Deterministic APH Scrutiny Engine**: Explainable compliance verification mapping to APH 2024–27 clauses (checklists, fire safety validity, land area, name concordance, faculty cadre ratios, dedicated labs, and 75% confidence threshold gate).
- **Continuous Compliance & Deadline Automation**: Surveillance sweeps (`ComplianceEngineService`) detecting expired safety certificates and unrectified deficiencies, automatic countdown calculation, grace periods, and severity-based escalation (`INFO` → `WARNING` → `CRITICAL`).
- **Distributed Scheduler Job Locks**: High-availability database-backed job locks (`distributed_job_locks` table) ensuring single-instance execution of scheduled surveillance sweeps across clustered backend nodes.
- **Authoritative Authorization & Governance**: [AuthoritativeAuthorizationService](backend/src/main/java/org/aicte/sage/common/security/AuthoritativeAuthorizationService.java) strictly eliminating blanket cross-institution access through regional jurisdictions (`jurisdiction_assignments`), explicit case assignments (`case_assignments`), and statutory committee appointments (`committee_appointments`).
- **Authentication Lifecycle & Server-Side Revocation**: Stateless Bearer JWT with server-side token invalidation (`token_revocations` table and memory cache) on logout; deactivated accounts are blocked.
- **Reliable Regulatory Audit Trail**: Consequential state changes are committed atomically with `AuditClassification.MANDATORY`; if audit persistence fails, the business transaction rolls back (`AuditPersistenceException`).
- **AICTE-Inspired Design System & Role Dashboards**: Accessible government aesthetic with high-contrast toggle, font scaling, WCAG 2.2 AA compliant modal dialogs, and 6 custom portals for Central Admin, Regional Officer, Institution Officer, Compliance Officer, Auditor, and EVC Coordinator.

---

## 2. Directory Structure

```text
SAGE/
  backend/           # Java 21 / Spring Boot 3.4 Core REST API & Security Engine
    src/main/resources/db/migration/  # Flyway versioned migrations (V1, V3, V5, V7, V9, V11)
    src/main/resources/db/seed/       # Non-production synthetic demonstration seeds (V2, V4, V6, V8, V10)
    run.sh           # Dedicated standalone Spring Boot runner script
  automation/        # Python 3.14 / FastAPI Automation & Intelligence Service (Indra OCR + Tesseract)
  frontend/          # React 19 + TypeScript + Vite Official AICTE Design System
  infrastructure/    # Docker Compose and container orchestration configs
  docs/              # Architectural specs, security models, database design, phase reports
  scripts/           # Service runner (dev.sh) and unified test runner (test.sh)
  run.sh             # Unified platform runner (orchestrates Python, Java, Vite with health checks)
  .env.example       # Template for environment configuration
  README.md          # Project guide and execution documentation
```

---

## 3. Quick Start (Running Locally)

All three services run seamlessly on local ports:
- **React Frontend Client**: `http://localhost:5173`
- **Spring Boot Core Backend**: `http://localhost:8080` (API Docs: `/swagger-ui.html`)
- **FastAPI Automation Service**: `http://localhost:8000` (API Docs: `/docs`)

### A. Start Complete Platform (All Services)
Run the all-in-one platform runner which starts the automation engine, core backend, and frontend together with automated dependency installation, health checks, and graceful shutdown on `Ctrl+C`:
```bash
./run.sh
```

### B. Start Core Backend Only
Run the dedicated Spring Boot runner script:
```bash
# Standard dev runner on port 8080
./backend/run.sh

# Optional flags:
./backend/run.sh --port 8081 --debug
```

### C. Individual Services (Dev Scripts)
```bash
# Terminal 1: Core Backend
./scripts/dev.sh backend

# Terminal 2: Automation Engine
./scripts/dev.sh automation

# Terminal 3: React Frontend
./scripts/dev.sh frontend
```

### D. Health Verification
```bash
# Backend Health
curl http://localhost:8080/api/v1/health

# Automation Engine Health
curl http://localhost:8000/api/v1/health

# Frontend Status
curl -I http://localhost:5173
```

---

## 4. Configuration Keys & Demonstration Credentials

> [!NOTE]
> Synthetic demonstration accounts are enabled **only** in `dev` and `test` profiles via Flyway seed migrations. In production (`prod`), local seed accounts are disabled and strong secrets are strictly enforced.
> For exhaustive technical details, see [docs/credentials-and-services-audit.md](docs/credentials-and-services-audit.md).

### Pre-Seeded Demonstration Accounts (Password: `Password123!`)

| Role | Username / Email | Password | Access Scope & Jurisdiction |
|---|---|---|---|
| **AICTE Central Admin** | `admin@aicte.gov.in` | `Password123!` | Full nationwide administration, user governance, audit trail |
| **Institution Officer (Delhi)** | `officer@delhi-tech.edu` | `Password123!` | Scoped to Delhi Institute of Advanced Engineering (`1-500123498`) |
| **Institution Officer (Mumbai)** | `officer@mumbai-engg.edu` | `Password123!` | Scoped to Mumbai Academy of Pharmaceutical Sciences (`1-500987654`) |
| **Regional Officer** | `regional@aicte.gov.in` | `Password123!` | Scoped to Northern Region institutions (Delhi, Punjab, Haryana) |
| **Compliance Officer** | `compliance@aicte.gov.in` | `Password123!` | Compliance surveillance, deficiency rectification monitoring |
| **Statutory Auditor** | `auditor@aicte.gov.in` | `Password123!` | Read-only inspection across institutions and immutable audit trail |
| **EVC Coordinator** | `evc.coord@aicte.gov.in` | `Password123!` | Scoped to assigned Expert Visit Committee cases |

### Key Environment Configuration

| Variable | Purpose | Default (Dev / Test) |
|---|---|---|
| `JWT_SECRET` | HMAC-SHA256 signature secret for login tokens | `sage_dev_jwt_secret_key_minimum_32_characters_for_hmac_sha256` |
| `INTERNAL_SERVICE_TOKEN` | Inter-service shared secret between Java and Python | `sage_internal_dev_service_secret_token_12345` |
| `POSTGRES_DB` / `USER` / `PASSWORD` | Database credentials (PostgreSQL) | `sage_db` / `sage_user` / `sage_secure_pass_dev_only` |

---

## 5. Automated Verification & Test Suite

Execute the unified test suite across all three subsystems:
```bash
./scripts/test.sh all
```

---

## 6. Service Operational Capability Matrix

| Service | Mode | Status & Behavior |
|---|---|---|
| **Core REST API (Spring Boot)** | Local / Docker | **100% Working**: Full RBAC, Multi-tenancy, Workflows, Scrutiny Rules, Distributed Scheduler Locks. |
| **Document Intelligence (FastAPI)** | Local / Docker | **100% Working**: Indra OCR + Tesseract Fallback, structured field extraction. |
| **Web Frontend (React Vite)** | Local / Nginx | **100% Working**: Accessible government UI, 6 tailored role dashboards, strict RBAC tab filtering, and WCAG AA modals. |
| **In-App Alerts** | Database | **100% Working**: Alert deduplication via SHA-256 fingerprint, read/dismiss tracking. |
| **Email Dispatch** | Simulated | **Simulated**: Transactional logs emitted; real delivery requires external SMTP gateway credentials. |
| **SMS / WhatsApp** | Stubbed | **Pending Gateway**: Schema-ready; requires telecom aggregator API key. |
| **Cloud Storage** | Local Disk | **Local Volume**: Files stored in `data/documents` with SHA-256 deduplication. S3 adapter ready. |

---

## 7. Production Deployment & Regulatory Documentation

### Production Deployment Runbooks (AWS EC2)
- [AWS EC2 Deployment Runbook](docs/AWS_EC2_DEPLOYMENT.md)
- [AWS Infrastructure Sizing & Cost Analysis](docs/AWS_INFRASTRUCTURE_AND_COST.md)
- [Domain & Automated HTTPS (Caddy)](docs/DOMAIN_AND_HTTPS_SETUP.md)
- [Production Environment Specification](docs/PRODUCTION_ENVIRONMENT.md)
- [Backup and Disaster Recovery](docs/BACKUP_AND_RECOVERY.md)
- [Deployment Troubleshooting Guide](docs/DEPLOYMENT_TROUBLESHOOTING.md)
- [Deployment Implementation Report](docs/DEPLOYMENT_IMPLEMENTATION_REPORT.md)

### Architectural Specs & Verification Reports
- [Credentials, Keys & Operational Services Audit](docs/credentials-and-services-audit.md)
- [Final Product Polish & System Audit Report](docs/final-product-polish-audit-report.md)
- [Phase 5 Completion & Deployment Readiness Report](docs/phase-5-completion-report.md)
- [Phase 4 Compliance & Deadline Automation Report](docs/phase-4-completion-report.md)
- [Phase 3 Completion & Scrutiny Engine Report](docs/phase-3-completion-report.md)
- [Architecture Overview](docs/architecture.md)
- [Security & Authorization Model](docs/security-model.md)
- [Database Design & Flyway Migrations](docs/database-design.md)
- [API Contracts & RFC 7807 Error Models](docs/api-contracts.md)
- [Requirements Traceability Matrix](docs/requirements-traceability.md)
