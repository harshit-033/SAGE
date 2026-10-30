# SAGE — Production & Demonstration Deployment Checklist

**System:** SAGE (System for Approval Governance and Evaluation)  
**Hackathon:** Smart India Hackathon (SIH) 2026 — Problem Statement 26262  
**Standard:** AICTE Technical Architecture & Security Deployment Norms  

---

## 1. Pre-Deployment Configuration & Secrets Audit

Before launching SAGE in a demonstration or production environment, verify all required configuration properties:

- [ ] **`SPRING_PROFILES_ACTIVE`**: Set to `dev` for local demonstration, or `prod` for production.
- [ ] **`JWT_SECRET`**:
  - In `dev`: Defaults to development secret (`dev_jwt_secret_key_minimum_32_characters_long_for_security_testing`).
  - In `prod`: MUST be provided as an external environment variable containing at least 32 high-entropy characters. Startup will fail if blank or using development default.
- [ ] **`INTERNAL_SERVICE_TOKEN`**:
  - Shared secret between Spring Boot backend (`DocumentOcrClient`) and FastAPI automation service (`verify_internal_service_token`).
  - In `prod`: MUST be explicitly configured (minimum 32 characters) and cannot match known development defaults.
- [ ] **`DATABASE_URL`**:
  - In `dev`: Uses H2 memory database or local PostgreSQL (`jdbc:postgresql://localhost:5432/sage_db`).
  - In Docker: Uses `jdbc:postgresql://db:5432/sage_db`.
- [ ] **`STORAGE_DIR`**:
  - Verified write permissions on `/app/data/documents` (or local `backend/data/documents`).

---

## 2. Docker & Container Health Checklist

When deploying via `infrastructure/docker-compose.yml`:

- [ ] **PostgreSQL Container (`sage_postgres`)**:
  - Image: `postgres:16-alpine`
  - Healthcheck command: `pg_isready -U sage_user -d sage_db`
  - Volume: `sage_pgdata` mounted and persistent.
- [ ] **Backend Service Container (`sage_backend`)**:
  - Image: Built from `backend/Dockerfile` (OpenJDK 21 Eclipse Temurin).
  - Healthcheck endpoint: `curl -f http://localhost:8080/api/v1/health`
  - Port mapping: `8080:8080`.
  - Dependent on `db: condition: service_healthy`.
- [ ] **FastAPI Automation Container (`sage_automation`)**:
  - Image: Built from `automation/Dockerfile` (Python 3.12 slim with Tesseract OCR).
  - Healthcheck endpoint: `curl -f http://localhost:8000/api/v1/health`
  - Port mapping: `8000:8000`.
  - Dependent on `backend: condition: service_healthy`.
- [ ] **Frontend Web Container (`sage_frontend`)**:
  - Image: Built from `frontend/Dockerfile` (Node 22 multi-stage build to Nginx Alpine).
  - Nginx configuration: `frontend/nginx.conf` copied to `/etc/nginx/conf.d/default.conf`.
  - Reverse proxy: `/api/` routed to `http://backend:8080/api/` and `/automation-api/` routed to `http://automation:8000/api/`.
  - Client-side routing: `try_files $uri $uri/ /index.html;` active.
  - Port mapping: `5173:80` (accessible at `http://localhost:5173`).

---

## 3. Database Migration & Seeding Verification

- [ ] **Flyway Migration Status**:
  - Run `./mvnw flyway:info` or inspect backend startup logs.
  - Verify all 16 migrations (V1 through V16) have state `SUCCESS`.
  - Verify schema checksum matches baseline; no manual schema drift.
- [ ] **Demonstration Dataset Seeding**:
  - Run `./scripts/seed-demo.sh`.
  - Verify output confirms 744 relational records across all 22 entities.
  - Verify 72 physical PDF documents generated in `backend/data/documents`.
  - Confirm zero foreign key constraint violations or duplicate key exceptions.

---

## 4. Post-Deployment Smoke Test Runbook

Perform the following smoke tests immediately following deployment:

1. **System Health Endpoints**:
   ```bash
   curl -i http://localhost:8080/api/v1/health
   # Expected: HTTP 200 {"status":"UP","services":{"database":"UP","diskSpace":"UP"}}
   ```
   ```bash
   curl -i http://localhost:8000/api/v1/health
   # Expected: HTTP 200 {"status":"healthy","ocr_engine":"ready"}
   ```

2. **Authentication Smoke Test**:
   ```bash
   curl -s -X POST http://localhost:8080/api/v1/auth/login \
     -H "Content-Type: application/json" \
     -d '{"username":"admin@aicte-demo.gov.in","password":"Password123!"}' | grep "token"
   # Expected: Non-empty JSON Web Token returned
   ```

3. **Multi-Tenancy Isolation Smoke Test**:
   - Log in as `officer@delhi-tech.edu`.
   - Access `GET /api/v1/applications?institutionId=22222222-2222-2222-2222-222222222222` (Mumbai College).
   - Expected response: `HTTP 403 Forbidden` (`tenant-access-denied`).

4. **Frontend Navigation Smoke Test**:
   - Open `http://localhost:5173`.
   - Log in as `admin@aicte-demo.gov.in` / `Password123!`.
   - Verify dashboard loads with KPI cards and application metrics.
   - Click navigation links: `Applications`, `Institutions`, `Compliance`, `EVC Scheduling`, `Audit Trail`.
   - Refresh on `/applications`. Verify page reloads cleanly without 404.

---

## 5. Rollback & Emergency Recovery Procedures

If deployment issues occur:

1. **Stop Services Cleanly**:
   ```bash
   cd infrastructure
   docker compose stop
   ```
2. **Database Reset (Demo Mode Only)**:
   ```bash
   # Caution: Destroys container volume data in demo mode
   docker compose down -v
   docker compose up -d db
   ./scripts/seed-demo.sh
   docker compose up -d
   ```
3. **Local Dev Reset**:
   ```bash
   killall -9 java uvicorn node 2>/dev/null || true
   ./scripts/seed-demo.sh
   ./run.sh
   ```
