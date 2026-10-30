# SAGE — Local Production Docker Validation & Deployment Script Audit Report

> **Project**: SAGE (System for Approval Governance and Evaluation)  
> **Environment**: Local Validation & AWS EC2 Production Target  
> **Repository**: [https://github.com/harshit-033/SAGE](https://github.com/harshit-033/SAGE)  
> **Branch**: `main`  
> **Audit Date**: October 31, 2026  
> **Classification**: **READY FOR AWS DEPLOYMENT**

---

## 1. Executive Summary

This report documents the exhaustive local audit, configuration consistency check, deployment script remediation, and test validation of the SAGE production stack.

All tasks requested in the directive have been completed:
1. **Current Configuration Reviewed**: Audited development Compose, production Compose, Caddyfile, Nginx config, Spring Boot production profile, and FastAPI configuration.
2. **Deployment Script Issues Resolved**:
   - Fixed the shared health-check timeout counter in `deploy-production.sh`. Each service now receives its own independent timeout window (120s for Spring Boot with Flyway migrations V1–V18; 60s for other services).
   - Corrected the inaccurate "zero-downtime" deployment claim to reflect in-place container recreation.
   - Added robust command-line argument parsing (`--url <URL>`, `--skip-smoke-test`) to `deploy-production.sh` and propagated the target URL directly to the smoke-test suite.
   - Enhanced `deployment-smoke-test.sh` with `-L` redirect following (for Caddy HTTP-to-HTTPS redirects), domain resolution checks with local fallback, and detailed error snippet reporting.
   - Confirmed real authentication endpoint status codes (`401 Unauthorized` for both unauthenticated filter rejection on `/api/v1/auth/me` and invalid credential rejection on `/api/v1/auth/login`).
   - Hardened `.gitignore` to prevent any untracked or accidental commits of `.env.production`.
3. **Service Health Endpoints Verified**: Validated authentic application endpoints across PostgreSQL, Spring Boot, FastAPI, Nginx, and Caddy.
4. **Networking Architecture Verified**: Confirmed fail-closed architecture where only Caddy exposes ports 80/443; internal services remain private.
5. **Zero Feature Regressions**: 175 out of 175 tests passed with zero failures across the entire stack.

---

## 2. Issues Identified and Remediations Applied

### Issue 1: Shared Timeout Counter in Health Polling Loop
- **Finding**: In `scripts/deploy-production.sh`, `ELAPSED=0` was declared outside the service iteration loop (`for SVC in "${SERVICES[@]}"`). If the database or backend took 40–50 seconds to initialize, subsequent services (automation, frontend, Caddy) inherited the accumulated elapsed time and could trigger premature timeouts.
- **Fix**: Initialized `ELAPSED=0` inside the loop per service and configured dedicated timeout thresholds (`SVC_TIMEOUT=120` for Spring Boot backend; `60` for others). If a service fails or times out, the last 100 lines of container logs for that specific service are printed to `stderr`.

### Issue 2: Inaccurate Zero-Downtime Deployment Claim
- **Finding**: The deployment banner claimed "zero-downtime container recreation". Standard Docker Compose `up -d` stops and replaces existing containers sequentially, which causes a brief service interruption unless fronted by a blue-green load balancer.
- **Fix**: Updated log output to accurately reflect "in-place container recreation".

### Issue 3: Smoke-Test URL Selection and Propagation
- **Finding**: `scripts/deploy-production.sh` did not accept a custom target URL or pass target parameters to `scripts/deployment-smoke-test.sh`. Additionally, if `.env.production` defined a production domain name (e.g. `sage.yourdomain.gov.in`) that is not yet mapped in local `/etc/hosts` or DNS, the smoke tests failed to connect.
- **Fix**:
  - Implemented argument parsing in `deploy-production.sh` supporting `--url <URL>`, positional URLs, and `--skip-smoke-test`.
  - Updated `deployment-smoke-test.sh` to check if `DOMAIN_NAME` is resolvable locally; if unresolvable, it gracefully falls back to `http://localhost` for local smoke testing while printing an informative notice.

### Issue 4: HTTP-to-HTTPS Redirect Handling in Smoke Tests
- **Finding**: Caddy by default issues an HTTP 308 redirect from port 80 to port 443. `curl` in `deployment-smoke-test.sh` lacked the `-L` (location follow) flag, causing checks against `http://localhost/` to report `308` rather than the final `200 OK`.
- **Fix**: Added `-L` to `curl` commands in `deployment-smoke-test.sh`, allowing redirects to be followed and evaluating the final response status. Added capture of response snippets on failure for rapid triage.

### Issue 5: Authentication Status Code Verification
- **Finding**: Smoke-test assertions needed verification against actual Spring Security and controller behavior.
- **Verification**:
  - `GET /api/v1/auth/me` without Authorization header: Intercepted by `SecurityConfig.authenticationEntryPoint` -> returns `401 Unauthorized` with `ProblemDetails` payload.
  - `POST /api/v1/auth/login` with `{"username":"invalid@test.gov.in","password":"wrong"}`: Validated by `AuthService.authenticate` -> throws `BadCredentialsException` / `InvalidCredentialsException` -> handled by `GlobalExceptionHandler` -> returns `401 Unauthorized`.
  - Added both checks to `deployment-smoke-test.sh` as distinct assertions.

### Issue 6: Repository Protection for `.env.production`
- **Finding**: `.gitignore` contained `.env` and `*.env`, but `.env.production` was not matched, leaving local production secrets vulnerable to accidental tracking.
- **Fix**: Added `.env.*` to `.gitignore` with explicit negative lookaheads (`!.env.example`, `!infrastructure/.env.production.example`) to ensure templates remain committed while actual secret files are strictly ignored.

---

## 3. Service Health Endpoint Verification Matrix

| Component | Verified Health Check Mechanism | Actual Endpoint / Probe | HTTP / Return Code |
| :--- | :--- | :--- | :--- |
| **PostgreSQL 16** | Docker native `CMD-SHELL` | `pg_isready -U sage_user -d sage_db` | `0` (Ready) |
| **Spring Boot Backend** | `curl -f` probe | `http://localhost:8080/api/v1/health/ready` | `200 OK` (Validates JDBC connection) |
| **Spring Boot Liveness** | `curl -f` probe | `http://localhost:8080/api/v1/health` | `200 OK` (Process active) |
| **FastAPI Automation** | `curl -f` probe | `http://localhost:8000/api/v1/health` | `200 OK` (`{"status":"UP"}`) |
| **FastAPI Readiness** | `curl -f` probe | `http://localhost:8000/api/v1/health/ready` | `200 OK` (Validates backend reachability) |
| **Nginx Frontend** | `wget --spider` | `http://localhost:80/` | `200 OK` (Serves SPA `index.html`) |
| **Caddy Reverse Proxy** | `wget --spider` | `http://localhost:80/` | `200 OK` / `308 Redirect` |

---

## 4. Networking Architecture & Path Routing

```
                         [ PUBLIC INTERNET ]
                                  |
                         Ports 80 / 443 (Caddy)
                                  v
+--------------------------------------------------------------------+
|                      CADDY REVERSE PROXY                           |
|  - Ingress: Listens on :80 and :443                                |
|  - Egress : Proxies internal traffic to `frontend:80`              |
|  - Upload limit: 50MB                                              |
+--------------------------------------------------------------------+
                                  |
                                  v (Docker Network: `sage_network`)
+--------------------------------------------------------------------+
|                         NGINX FRONTEND                             |
|  - `location /`                -> Serves React SPA (try_files fallback)
|  - `location /assets/`         -> Immutable static cache (1 year)  |
|  - `location /api/`            -> Proxies to `http://backend:8080/api/`
|  - `location /automation-api/` -> Proxies to `http://automation:8000/api/`
+--------------------------------------------------------------------+
              |                                        |
              v                                        v
+---------------------------+            +---------------------------+
|    SPRING BOOT BACKEND    |            |     FASTAPI AUTOMATION    |
| Port: 8080 (Internal)     |            | Port: 8000 (Internal)     |
+---------------------------+            +---------------------------+
              |                                        |
              +-------------------+--------------------+
                                  v
+--------------------------------------------------------------------+
|                     POSTGRESQL 16 DATABASE                         |
| Port: 5432 (Internal to `sage_network` only)                       |
+--------------------------------------------------------------------+
```

- **Port Isolation**: PostgreSQL (5432), Backend (8080), Automation (8000), and Frontend (80) have **no public host port mappings** in `docker-compose.prod.yml`.
- **Inter-service Communication**:
  - Frontend to Backend: via Nginx `/api/` -> `http://backend:8080/api/`
  - Frontend to Automation: via Nginx `/automation-api/` -> `http://automation:8000/api/`
  - Backend to Automation: `http://automation:8000`
  - Automation to Backend: `http://backend:8080`
  - Backend to Database: `jdbc:postgresql://db:5432/sage_db`

---

## 5. Specification & Configuration Validation

### Command 1: Development Docker Compose Validation
```bash
docker compose -f infrastructure/docker-compose.yml config
```
- **Result**: Valid specification (Exit 0).

### Command 2: Production Docker Compose Validation
```bash
docker compose --env-file infrastructure/.env.production -f infrastructure/docker-compose.prod.yml config
```
- **Result**: Valid specification (Exit 0). All environment variables (`POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`, `JWT_SECRET`, `INTERNAL_SERVICE_TOKEN`, `GEMINI_API_KEY`) substituted correctly.

---

## 6. Regression Testing Results (175 / 175 Tests Passing)

### A. Spring Boot Backend Test Suite
- **Command**: `cd backend && ./mvnw test`
- **Results**: **101 tests run, 0 failures, 0 errors, 0 skipped** (Time: 40.8s)
- **Coverage**:
  - `AuthControllerTest` (5 tests): Login, password rejection, blank body, JWT profile, unauthenticated rejection.
  - `Phase3WorkflowAndScrutinyTest` (12 tests): Multi-stage application lifecycle transitions, APH scrutiny checklists, deficiency resolution.
  - `Phase4DeadlinesAlertsComplianceTest` (10 tests): Continuous compliance sweeps, certificate expiry countdowns, simulated alerts.
  - `Phase5IntegrationHardeningTest` (8 tests): Multi-tenant isolation, distributed job locks.
  - `DocumentComplianceTrackingTest` (4 tests): SHA-256 document storage, versioning, expiry tracking.
  - `NewCourseAndBranchWorkflowTest` (6 tests): Course additions, cadre ratio norms, lab verifications.
  - `EvcAuthorizationTest` (6 tests): Conflict-free committee panel appointments.
  - `InstitutionControllerTenancyTest` (4 tests): Regional jurisdiction enforcement.
  - `HealthEndpointTest` (2 tests): Liveness and JDBC readiness probes.

### B. FastAPI Automation & OCR Pytest Suite
- **Command**: `automation/venv/bin/pytest -v automation/tests`
- **Results**: **15 passed, 0 failed** in 1.62s
- **Coverage**:
  - `test_health.py` (3 tests): Liveness probe, backend connectivity readiness, correlation ID tracing.
  - `test_ocr.py` (7 tests): Indra OCR extraction, Tesseract fallback, path traversal defense, corrupt file handling, benchmark endpoint.
  - `test_security.py` (4 tests): Internal token verification, rejection of weak/default tokens in production mode.
  - `test_validation.py` (1 test): RFC 7807 Problem Details payload validation.

### C. React Frontend Test, Lint & Build Suite
- **Commands**:
  ```bash
  cd frontend
  npm run lint
  npm run build
  npx vitest run --no-file-parallelism
  ```
- **Results**:
  - **Lint**: 0 errors, 0 warnings.
  - **Build**: Successfully built production bundle (`dist/index.html`, `dist/assets/index-CYm8c7MC.css` 18.18 kB, `dist/assets/index-BDXnNQRQ.js` 707.10 kB).
  - **Vitest**: **59 passed across 15 test files**, 0 failures.

---

## 7. Remaining Manual Cloud Actions for AWS EC2

The application and deployment configurations are fully tested and prepared. The following operations must be performed in the AWS Management Console:

1. **Launch EC2 Instance**:
   - AMI: Ubuntu Server 24.04 LTS (HVM), SSD Volume Type
   - Instance Type: `t3.xlarge` (4 vCPU, 16 GB RAM) in `ap-south-1` (Mumbai)
   - Storage: 100 GB gp3 SSD
2. **Configure Security Group Inbound Rules**:
   - Port 22 (SSH): Restrict to administrator IP address.
   - Port 80 (HTTP): `0.0.0.0/0` (Redirects to HTTPS via Caddy).
   - Port 443 (HTTPS): `0.0.0.0/0` (Secure web traffic).
3. **Allocate & Associate Elastic IP**:
   - Allocate an Elastic IPv4 and attach it to the EC2 instance.
4. **Configure DNS Records**:
   - Point your domain's A-record (`sage.yourdomain.gov.in`) to the Elastic IP.
5. **Run Deployment Script**:
   - SSH into the instance and run `./scripts/deploy-production.sh`.

---

## 8. Final Classification

$$\mathbf{READY\ FOR\ AWS\ DEPLOYMENT}$$

All configuration inconsistencies and script defects have been identified and resolved. Zero regressions were introduced. The deployment pipeline is hardened, fully reproducible, and ready for execution on AWS EC2.
