# SAGE — Production Deployment Audit Report

> **Project**: SAGE (System for Approval Governance and Evaluation)  
> **Context**: Independent Prototype developed for Smart India Hackathon (SIH) 2026 — Problem Statement 26262  
> **Date**: October 31, 2026  
> **Auditor**: Senior DevOps Engineer & Cloud Architect  
> **Scope**: AWS EC2 Docker Compose Production Deployment Readiness  

---

## 1. Executive Summary & Current Architecture

SAGE is an end-to-end, multi-tier regulatory approval governance and institutional compliance evaluation engine designed for technical education regulatory workflows.

```
+-----------------------------------------------------------------------------------+
|                                INTERNET                                           |
+-----------------------------------------------------------------------------------+
                                         | (Ports 80 / 443)
                                         v
+-----------------------------------------------------------------------------------+
|                           EDGE: CADDY REVERSE PROXY                               |
|              - Automatic HTTPS / TLS termination via Let's Encrypt / ZeroSSL      |
|              - Security headers, gzip/zstd, and request rate-limiting             |
+-----------------------------------------------------------------------------------+
                                         | (Internal Docker Network)
                                         v
+-----------------------------------------------------------------------------------+
|                        INGRESS / FRONTEND: NGINX                                  |
|         - Serves optimized React 19 / Vite SPA client bundle                     |
|         - Routes /api/ -> Spring Boot Backend (:8080)                            |
|         - Routes /automation-api/ -> FastAPI Automation (:8000)                   |
|         - Enforces SPA fallback (try_files $uri $uri/ /index.html)                |
+-----------------------------------------------------------------------------------+
              |                                                   |
              v                                                   v
+-----------------------------------+   +-------------------------------------------+
|    SPRING BOOT BACKEND (:8080)    |   |     FASTAPI AUTOMATION ENGINE (:8000)     |
| - Java 21, Spring Boot 3.4.3      |   | - Python 3.12, Uvicorn, Pydantic v2       |
| - JPA / Hibernate, Flyway v18     |   | - Indra OCR & Tesseract fallback engine   |
| - Spring Security (Stateless JWT) |   | - Gemini Document Intelligence            |
| - Multi-tenant data isolation     |   | - SHA-256 integrity hashing               |
| - EVC CP-SAT scheduling engine    |   | - Path traversal protection               |
| - Actuator & OpenAPI / Swagger    |   | - Internal service authentication token   |
+-----------------------------------+   +-------------------------------------------+
              |                                                   |
              +-------------------+   +---------------------------+
                                  |   |
                                  v   v
+-----------------------------------------------------------------------------------+
|                         PERSISTENCE & SHARED STORAGE                              |
| - PostgreSQL 16 (Relational transactional database, schema 'public', 18 Flyway)   |
| - Shared Volume `sage_documents` mounted at `/app/data/documents`                 |
+-----------------------------------------------------------------------------------+
```

---

## 2. Existing Services & Features Inventory

| Service | Technology | Internal Port | External Port (Dev) | External Port (Prod) | Responsibilities |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Edge Proxy** | Caddy v2 | 80, 443 | N/A | **80, 443** (Public) | Automatic SSL/TLS, Let's Encrypt certificate lifecycle, HTTP->HTTPS redirect, reverse proxy to Nginx. |
| **Frontend** | Nginx Alpine + React 19 / Vite | 80 | 5173 | **None** (Internal) | Single-page application, client routing, static asset serving, unified upstream gateway. |
| **Backend** | Eclipse Temurin 21 + Spring Boot 3.4.3 | 8080 | 8080 | **None** (Internal) | Core REST API, JWT auth, RBAC, tenancy boundaries, application lifecycle, Flyway migrations, document metadata. |
| **Automation** | Python 3.12 + FastAPI + Uvicorn | 8000 | 8000 | **None** (Internal) | Asynchronous OCR extraction, document forensics, Gemini Flash Lite analysis, CP-SAT optimization. |
| **Database** | PostgreSQL 16 Alpine | 5432 | 5432 | **None** (Internal) | Persistent relational data, constraints, indexes, audit logging. |

---

## 3. Existing Docker Configuration Audit

### Current `infrastructure/docker-compose.yml`
- **Strengths**:
  - Declarative multi-service compose defining `db`, `backend`, `automation`, and `frontend`.
  - Uses named volumes `sage_pgdata` and `sage_documents`.
  - Implements Docker service networking via `sage_network` bridge.
  - Implements healthchecks for `db`, `backend`, and `automation`.
  - Mounts document volume read-only in automation (`ro`) and read-write in backend.
- **Identified Production Gaps**:
  - Exposes port `5432` (PostgreSQL), `8080` (Backend), and `8000` (Automation) directly to the host. In production, only Caddy (`80`/`443`) should be exposed.
  - Missing Caddy reverse proxy service for automated HTTPS.
  - Missing resource constraints (CPU/Memory limits) to prevent noisy neighbor / OOM crashes during heavy OCR or EVC batch scheduling.
  - Backend healthcheck `test: ["CMD-SHELL", "curl -f http://localhost:8080/api/v1/health || exit 1"]` uses `/api/v1/health` instead of `/api/v1/health/ready` (which tests DB connection).
  - Needs production compose file `infrastructure/docker-compose.prod.yml` leaving development compose intact.

---

## 4. Current Environment Configuration

### Environment Variables Flow
1. **PostgreSQL**:
   - `POSTGRES_DB`: Default `sage_db`
   - `POSTGRES_USER`: Default `sage_user`
   - `POSTGRES_PASSWORD`: Must be set via environment in production
2. **Spring Boot Backend**:
   - `SPRING_PROFILES_ACTIVE`: `prod` for production (currently defaults to `dev` with in-memory H2)
   - `SPRING_DATASOURCE_URL`: `jdbc:postgresql://db:5432/${POSTGRES_DB}`
   - `JWT_SECRET`: Minimum 32-character key for HMAC-SHA256
   - `JWT_EXPIRATION_MS`: Token lifetime (default 86400000ms / 24h)
   - `INTERNAL_SERVICE_TOKEN`: Shared token for backend-to-automation authorization
   - `AUTOMATION_SERVICE_URL`: `http://automation:8000`
   - `GEMINI_API_KEY`: API key for Gemini Document Intelligence
   - `GEMINI_MODEL`: `gemini-flash-lite-latest`
3. **FastAPI Automation**:
   - `BACKEND_SERVICE_URL`: `http://backend:8080`
   - `INTERNAL_SERVICE_TOKEN`: Matches backend token
   - `GEMINI_API_KEY`: Passed to automation engine
   - `GEMINI_MODEL`: `gemini-flash-lite-latest`
   - `ENVIRONMENT`: `prod`
4. **Caddy / Ingress**:
   - `DOMAIN_NAME`: Fully qualified domain name (or `:80` / public IP for staging testing)
   - `ACME_EMAIL`: Email for Let's Encrypt notifications

---

## 5. Database & Storage Architecture

1. **Relational Database**:
   - PostgreSQL 16 schema managed strictly through Flyway migrations:
     - `V1` to `V18` located in `backend/src/main/resources/db/migration/`.
     - Seed scripts in `backend/src/main/resources/db/seed/` are loaded strictly in `dev` profile. Production migrations run schema and baseline regulatory rules only.
   - Database volume: `sage_pgdata` mounted at `/var/lib/postgresql/data`.
2. **Document & Media Storage**:
   - Files are stored on a local persistent volume `sage_documents` mounted at `/app/data/documents`.
   - File naming: UUID-based with original filename and SHA-256 hash stored in PostgreSQL `documents` table.
   - Path traversal prevention implemented in both Java (`Path.normalize()`) and Python (`validate_document_path()`).

---

## 6. API Communication Paths & Security Boundaries

```
[Browser / Client]
       |
       | HTTPS (443) -> Let's Encrypt TLS
       v
   [Caddy Proxy]
       |
       | HTTP -> Reverse Proxy (proxy_pass)
       v
  [Nginx Frontend]
       |
       +---> /                    -> React SPA static files
       +---> /api/v1/auth/        -> Public authentication (JWT issuance)
       +---> /api/v1/health       -> Public unauthenticated health probes
       +---> /api/v1/health/ready -> Readiness probe verifying PostgreSQL connection
       +---> /api/v1/*            -> Protected by JWT Bearer token & role-based evaluation
       +---> /automation-api/     -> Routed to FastAPI (protected by INTERNAL_SERVICE_TOKEN)
```

Inter-service communication:
- Backend calls Automation: `http://automation:8000/api/v1/ocr/process` passing header `X-Internal-Service-Token`.
- Automation calls Backend: `http://backend:8080/api/v1/internal/*` passing header `X-Internal-Service-Token`.

---

## 7. Baseline Test Suite Verification Summary

Prior to any deployment modifications, all existing test suites were executed to establish our verification baseline:

- **Frontend Vitest Suite**: 15 test files, **59 tests passed**, 0 failed.
- **Frontend TypeScript & Build**: `npm run build` completed with 0 errors.
- **Frontend Lint**: `npm run lint` completed with 0 errors.
- **Backend Test Suite**: 19 test classes, **101 tests passed**, 0 failed.
- **Python Automation Pytest Suite**: 4 test modules, **15 tests passed**, 0 failed.
- **Total Existing Automated Tests**: **175 tests passed (100% pass rate)**.

---

## 8. Deployment Blockers & Resolution Plan

| Blocker / Risk | Root Cause | Resolution Strategy |
| :--- | :--- | :--- |
| **No Production Spring Profile** | Backend only had `dev` (H2 database) and `test` profiles configured. | Create `application-prod.yml` with PostgreSQL connection pooling, Hikari settings, graceful shutdown, and schema validation. |
| **Public Port Exposure** | `docker-compose.yml` maps PostgreSQL (5432), Backend (8080), and Automation (8000) to host. | Create `infrastructure/docker-compose.prod.yml` that exposes ONLY Caddy (80 and 443). All other services stay internal. |
| **Missing HTTPS Reverse Proxy** | No Caddyfile or production reverse proxy configuration existed. | Create `infrastructure/Caddyfile` with automatic HTTPS, TLS configuration, gzip/zstd, and upstream forwarding to Nginx. |
| **Missing Production Env Spec** | `.env.example` lacks production-specific configuration tokens (e.g. Caddy domain, ACME email, Hikari limits). | Create `infrastructure/.env.production.example` and documentation in `docs/PRODUCTION_ENVIRONMENT.md`. |
| **Missing Backup & Recovery Scripts** | No automated, non-destructive database or document backup scripts existed. | Create `scripts/backup-db.sh`, `scripts/restore-db.sh`, `scripts/backup-documents.sh`, and `docs/BACKUP_AND_RECOVERY.md`. |
| **Missing Production Deploy Scripts** | Manual docker compose invocation could lead to race conditions or missed health validations. | Implement `scripts/deploy-production.sh` and `scripts/deployment-smoke-test.sh`. |

---

## 9. Non-Negotiable Zero-Regression Commitments

1. No existing frontend component, route, or CSS style will be modified or removed.
2. No backend controller, service, repository, or entity will be simplified or altered.
3. All 18 Flyway migrations will be preserved intact.
4. Gemini Flash Lite document analysis and OCR pipelines will remain active.
5. All 175 baseline tests must continue to pass before and after deployment configuration.
