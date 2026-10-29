# SAGE Credentials, Configuration Keys & Service Operational Matrix

> **AICTE Smart Governance Engine (SAGE)**  
> **Problem Statement 26262 | SIH 2026**  
> *Regulatory Source: AICTE Approval Process Handbook (APH) 2024–27*

This document provides a comprehensive inventory of all cryptographic keys, passwords, database settings, seed accounts, and the operational capability matrix (working vs. simulated services) for the SAGE platform.

---

## 1. Cryptographic Keys & Inter-Service Security

| Key / Variable | Subsystem | Default (Dev / Test) | Production Requirement & Behavior |
|---|---|---|---|
| `JWT_SECRET` | Spring Boot (`sage-backend`) | `sage_dev_jwt_secret_key_minimum_32_characters_for_hmac_sha256` | **Mandatory:** Cryptographically random string of $\ge 256$ bits (32+ characters) using HMAC-SHA256 (`HS256`). Rejects known insecure dev defaults in `prod` profile during Spring Boot startup. |
| `JWT_EXPIRATION_MS` | Spring Boot (`sage-backend`) | `86400000` (24 hours) | Configurable integer in milliseconds. Token revocations are tracked server-side in `token_revocations` table. |
| `INTERNAL_SERVICE_TOKEN` | Spring Boot & FastAPI Automation | `sage_internal_dev_service_secret_token_12345` | **Mandatory:** Shared secret token transmitted in HTTP header `X-Internal-Service-Token`. Both Spring Boot (`DocumentOcrClient`) and FastAPI (`config.py`) validate this token. Rejects known defaults in `prod`. |

---

## 2. Database Credentials & Connectivity

| Variable | Dev / Test Profile (In-Memory H2) | Production / Docker Compose (PostgreSQL 16) | Notes |
|---|---|---|---|
| `POSTGRES_DB` | `sage_dev` | `sage_db` | PostgreSQL database name |
| `POSTGRES_USER` | `sa` | `sage_user` | Database master user |
| `POSTGRES_PASSWORD` | *(empty)* | `sage_secure_pass_dev_only` | Must be updated in `.env` before production deployment |
| `POSTGRES_HOST` | `localhost` | `localhost` (local) / `db` (Docker) | Hostname / network address |
| `POSTGRES_PORT` | N/A | `5432` | Standard PostgreSQL port |
| `SPRING_DATASOURCE_URL` | `jdbc:h2:mem:sage_dev;...` | `jdbc:postgresql://${POSTGRES_HOST}:${POSTGRES_PORT}/${POSTGRES_DB}` | Auto-configured via Spring Boot properties |

---

## 3. Pre-Seeded Demonstration User Accounts

All seeded accounts in the development profile use BCrypt hashed password:  
**`Password123!`** *(Hash: `$2a$10$9Zj422FbkPlFPBveLlulK.oJ5o4nuF6aownRmGmFFGad97I2fQ/Vi`)*

| Username / Email | Password | Primary Role | Scope & Jurisdiction |
|---|---|---|---|
| `admin@aicte.gov.in` | `Password123!` | `ROLE_AICTE_ADMIN` | AICTE Central Administrator (Full national regulatory authority) |
| `regional@aicte.gov.in` | `Password123!` | `ROLE_REGIONAL_OFFICER` | Northern Region (Delhi, Punjab, Haryana institutions) |
| `officer@delhi-tech.edu` | `Password123!` | `ROLE_INSTITUTION_OFFICER` | Delhi Institute of Advanced Engineering (`22222222-2222-2222-2222-222222222222`) |
| `officer@mumbai-engg.edu` | `Password123!` | `ROLE_INSTITUTION_OFFICER` | Mumbai Academy of Pharmaceutical Sciences (`33333333-3333-3333-3333-333333333333`) |
| `compliance@aicte.gov.in` | `Password123!` | `ROLE_COMPLIANCE_OFFICER` | Compliance monitoring, statutory deficiency remediation oversight |
| `auditor@aicte.gov.in` | `Password123!` | `ROLE_AUDITOR` | Immutable audit log inspection, read-only institutional review |
| `evc.coord@aicte.gov.in` | `Password123!` | `ROLE_EVC_COORDINATOR` | Expert Visit Committee (EVC) physical/virtual inspection management |

---

## 4. Service Operational Status Matrix

### A. Fully Operational Services (100% Functional Locally)

* **Spring Boot 3 Core REST API (Port 8080)**:
  * Stateless JWT authentication with server-side revocation tracking (`token_revocations`).
  * Role-Based Access Control (RBAC) with method security and tenant data isolation.
  * Institution Directory with multi-column filtering, pagination, and location geocoding.
  * Application Workflow State Machine (`DRAFT` through `APPROVED`/`REJECTED`) with optimistic locking.
  * Document upload, MIME-type magic byte validation, and SHA-256 cryptographic hashing.
  * Deterministic Scrutiny Rule Engine (Rules 101–105 for Fire Safety NOC, Land Norms, Built-up Area, Architect Certificates, Trust Resolution).
  * Continuous Surveillance sweeps (`ComplianceEngineService`) detecting expired certificates and compliance violations.
  * Regulatory Deadline Management with automated countdowns, grace periods, and reminder schedules.
  * In-App Alert Delivery & Deduplication via SHA-256 fingerprinting.
  * Distributed Job Locking via `distributed_job_locks` database table.
  * Complete test coverage: **66/66 unit and integration tests passing**.

* **FastAPI Automation & OCR Engine (Port 8000)**:
  * Document intelligence endpoint `/api/v1/ocr/process` with high-speed local processing.
  * Both `Indra OCR` and `Tesseract OCR` engines operational on the system.
  * Structured field extraction: certificate numbers, issue/expiry dates, land acreage, built-up area.
  * Defense against path traversal and unauthorized unauthenticated access.
  * Complete test coverage: **15/15 tests passing**.

* **React 18 + Vite Frontend (Port 5173)**:
  * Official AICTE Government Design System with high-contrast mode and font scaling.
  * 5 Role-Tailored Dashboards (Admin, Regional Officer, Institution, Compliance, Auditor).
  * Accessible `ConfirmModal` dialogs replacing all native browser popups.
  * Strict RBAC client route guards.
  * Complete test coverage: **36/36 tests passing** (12 suites).

---

### B. Simulated, Stubbed, or External-Dependent Services

| Service / Integration | Status | Operational Details |
|---|---|---|
| **Outbound SMTP Email Gateway** | **Simulated** | Implemented via `EmailNotificationChannelAdapter`. Configured with `sage.notifications.email.enabled: false`. Alerts produce structured transactional log entries rather than initiating live network socket connections. Real email dispatch requires an external SMTP host and credentials. |
| **Telecom SMS & WhatsApp Gateways** | **Stubbed / Schema-Ready** | The delivery enum and database schema support multi-channel dispatch (`EMAIL`, `IN_APP`, `WEBHOOK`). Integration with a live telecom gateway (e.g. CDAC/NIC Gov SMS gateway) requires external provider API keys. |
| **National Legacy AICTE ERP / DigiLocker Sync** | **Decoupled by Design** | SAGE operates as an autonomous multi-tenant platform with its own transactional database. It does not hit live central Government of India legacy ERP servers (standard design for prototype & hackathon evaluation). |
| **Cloud Object Storage (AWS S3 / MinIO)** | **Local Storage** | Documents are stored directly on the local filesystem (`data/documents` or Docker volume `sage_documents`) with SHA-256 deduplication and UUID paths. Cloud S3 adapter can be plugged in without database schema changes. |
| **Playwright Browser Runner (in Sandbox)** | **Blocked in Restricted Sandbox** | Automated browser binary download (`azureedge.net`) is restricted in the sandbox execution environment. All frontend components and integration flows are tested and validated via Vitest + JSDOM. |

---

## 5. Quick Verification Commands

```bash
# Verify backend unit & integration tests (66 tests)
cd backend && mvn test

# Verify Python automation engine tests (15 tests)
source automation/venv/bin/activate && pytest automation/tests -v

# Verify frontend test suites (36 tests)
cd frontend && npm test -- --run
```
