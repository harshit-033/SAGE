# SAGE — Smart AICTE Guidance and Evaluation

> **SIH 2026 Problem Statement 26262**:
> *"Integrated AICTE Approval Workflow Automation, Compliance Monitoring and Alert Management System."*

---

## 1. Project Overview

SAGE is an enterprise-grade, multi-tenant regulatory automation platform engineered for the All India Council for Technical Education (AICTE). It automates and streamlines institutional approvals, scrutinies, Expert Visit Committee (EVC) allocations, continuous compliance monitoring, and statutory alert escalation.

Phase 1 establishes the production-oriented, testable foundation:
- **Core Backend**: Spring Boot 3.4 on Java 21 LTS with Spring Security, Spring Data JPA, PostgreSQL, and Flyway.
- **Automation Service**: Python 3.14 with FastAPI, Pydantic, and clean worker interfaces.
- **Frontend Portal**: React 18/19 with TypeScript, Vite, TanStack Query, and a modern, accessible design system.
- **Infrastructure**: Versioned Flyway migrations, Docker Compose orchestration, RFC 7807 problem details, and `X-Correlation-ID` tracing.

---

## 2. Directory Structure

```text
sage/
  backend/           # Java 21 / Spring Boot 3.4 REST API & Security Engine
  automation/        # Python 3.14 / FastAPI Automation & Intelligence Service
  frontend/          # React + TypeScript + Vite Administrative Portal
  infrastructure/    # Docker Compose and container orchestration configs
  docs/              # Architectural specs, security models, ADRs, and roadmap
    decisions/       # Architecture Decision Records (ADRs)
  scripts/           # Developer workflow and automated test scripts
  .env.example       # Template for environment configuration
  .gitignore         # Git ignore rules for Java, Python, and Node
  README.md          # Project guide and execution documentation
```

---

## 3. Environment & Prerequisites

Inspected environment status:
- **Operating System**: Linux (Arch Linux rolling, x86_64)
- **Java**: OpenJDK 21 LTS (`21.0.11`)
- **Python**: Python 3.14.8 with `pip` and `venv`
- **Node.js**: Node v22.14.0, npm 10.9.2
- **Docker**: Docker 28.0.1, Docker Compose v5.6.1 *(Daemon currently inactive)*
- **Maven**: Bundled Maven Wrapper (`./mvnw`) inside `backend/`

### Docker Daemon Note
To start Docker daemon if running via Docker Compose:
```bash
sudo systemctl start docker
```
If developing locally without Docker, PostgreSQL 18.6 binaries are available locally or embedded H2 in PostgreSQL compatibility mode is utilized for test execution.

---

## 4. Quick Start (Local Development)

### A. Environment Configuration
Copy `.env.example` to create your local `.env`:
```bash
cp .env.example .env
```

### B. Running Services Individually

1. **Start Core Backend (Spring Boot)**:
   ```bash
   cd backend
   ./mvnw spring-boot:run
   ```
   Backend listens at: `http://localhost:8080`
   - Health check: `http://localhost:8080/api/v1/health`
   - Readiness check: `http://localhost:8080/api/v1/health/ready`
   - OpenAPI Docs: `http://localhost:8080/swagger-ui.html`

2. **Start Automation Service (FastAPI)**:
   ```bash
   cd automation
   python3 -m venv venv
   source venv/bin/activate
   pip install -r requirements.txt
   uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
   ```
   Automation service listens at: `http://localhost:8000`
   - Health check: `http://localhost:8000/api/v1/health`
   - OpenAPI Docs: `http://localhost:8000/docs`

3. **Start Frontend Portal (React + Vite)**:
   ```bash
   cd frontend
   npm install
   npm run dev
   ```
   Frontend portal listens at: `http://localhost:5173`

### C. Running via Docker Compose
When Docker daemon is active:
```bash
docker compose -f infrastructure/docker-compose.yml up --build
```

---

## 5. Security & Default Development Credentials

> **CRITICAL**: The credentials below are synthetic, non-production credentials loaded **only** under the `dev` profile via `V2__dev_seed_data.sql`. In production (`prod`), local seed accounts are disabled and secrets must be injected through KMS / environment variables.

| Username | Password | Role | Description |
|---|---|---|---|
| `admin@aicte.gov.in` | `Password123!` | `ROLE_AICTE_ADMIN` | AICTE Central Administrator (Global Access) |
| `regional@aicte.gov.in` | `Password123!` | `ROLE_REGIONAL_OFFICER` | AICTE Regional Directorate Officer |
| `officer@delhi-tech.edu` | `Password123!` | `ROLE_INSTITUTION_OFFICER` | College Representative (Delhi Institute of Tech) |
| `officer@mumbai-engg.edu` | `Password123!` | `ROLE_INSTITUTION_OFFICER` | College Representative (Mumbai Engg Academy) |

### Testing Cross-Institution Isolation
- Logging in as `officer@delhi-tech.edu` allows access to Delhi Institute records (`1-500123498`).
- Attempting to access Mumbai Engineering Academy (`1-500987654`) triggers HTTP `403 Forbidden` (`TenantIsolationException`) and records an audit log.

---

## 6. Running Tests

Run all unit and slice tests across Java, Python, and React using the unified test runner:
```bash
./scripts/test.sh all
```
Or execute individually:
- Backend: `cd backend && ./mvnw clean test`
- Automation: `cd automation && pytest`
- Frontend: `cd frontend && npm test -- --run`
