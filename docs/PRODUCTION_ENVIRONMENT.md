# SAGE — Production Environment Configuration Guide

> **Document Version**: 1.0.0  
> **Target Environment**: AWS EC2 Docker Compose Production Deployment  
> **Reference File**: `infrastructure/.env.production.example`  

---

## 1. Overview & Security Principles

The SAGE platform utilizes a **fail-closed, 12-factor configuration model**. All environment-specific parameters, database credentials, cryptographic keys, and domain settings are injected through environment variables rather than hardcoded in application binaries.

### Core Security Guarantees
1. **No Insecure Defaults in Production**: The Spring Boot backend (`prod` profile) and FastAPI automation engine (`ENVIRONMENT=prod`) will fail to start if default or weak credentials are provided.
2. **Internal Network Privacy**: Backend, Automation, and PostgreSQL containers do NOT expose ports to the EC2 host. Only Caddy exposes ports `80` (HTTP) and `443` (HTTPS).
3. **Inter-Service Authentication**: Backend-to-Automation communication requires the `X-Internal-Service-Token` header, validated with a minimum 32-character secret.

---

## 2. Environment Variables Matrix

| Variable Name | Required Service | Example / Default | Minimum Constraint / Security Rule | Purpose |
| :--- | :--- | :--- | :--- | :--- |
| `DOMAIN_NAME` | `caddy` | `sage.aicte.gov.in` | Valid FQDN or `localhost` | Domain name for automated TLS certificate issuance and HTTPS reverse proxy routing. |
| `ACME_EMAIL` | `caddy` | `admin@aicte.gov.in` | Valid email address | Used by Let's Encrypt / ZeroSSL for certificate expiration and maintenance notices. |
| `POSTGRES_DB` | `db`, `backend` | `sage_db` | Alphanumeric + underscore | Name of the primary PostgreSQL database. |
| `POSTGRES_USER` | `db`, `backend` | `sage_user` | Alphanumeric + underscore | Administrative database username. |
| `POSTGRES_PASSWORD` | `db`, `backend` | *User Secret* | Strong alphanumeric (>= 12 chars) | Master PostgreSQL password. Injected into `db` and Spring Boot HikariCP pool. |
| `HIKARI_MAX_POOL_SIZE` | `backend` | `20` | Positive integer (10–50) | Maximum database connections allocated to the Spring Boot backend pool. |
| `HIKARI_MIN_IDLE` | `backend` | `5` | Positive integer (2–10) | Minimum idle database connections maintained by HikariCP. |
| `SPRING_PROFILES_ACTIVE` | `backend` | `prod` | Strictly `prod` | Activates PostgreSQL datasource, Flyway V1-V18 schema validation, and graceful shutdown. |
| `JWT_SECRET` | `backend` | *User Secret* | **Minimum 32 characters** | HMAC-SHA256 secret key for signing and verifying user authentication tokens. |
| `JWT_EXPIRATION_MS` | `backend` | `86400000` | Positive integer (milliseconds) | Access token validity duration (86400000 ms = 24 hours). |
| `INTERNAL_SERVICE_TOKEN` | `backend`, `automation` | *User Secret* | **Minimum 32 characters** | Shared symmetric secret for inter-service HTTP requests (`X-Internal-Service-Token`). |
| `GEMINI_API_KEY` | `backend`, `automation` | *API Key* | Valid Google AI Studio / GCP key | Powers automated document format analysis and digital forgery risk evaluation. |
| `GEMINI_MODEL` | `backend`, `automation` | `gemini-flash-lite-latest` | Valid Gemini model identifier | Model version queried for pre-upload document analysis. |
| `ENVIRONMENT` | `automation` | `prod` | Strictly `prod` | Activates production security validators in the FastAPI automation engine. |
| `CORS_ORIGINS` | `automation` | `https://sage.example.gov.in` | Comma-separated HTTPS URLs | Restricts browser cross-origin requests to authorized production domains. |

---

## 3. Configuration Step-by-Step on AWS EC2

### Step 1: Navigate to Infrastructure Directory
```bash
cd /opt/sage/infrastructure
```

### Step 2: Create `.env.production` from Template
```bash
cp .env.production.example .env.production
chmod 600 .env.production
```

### Step 3: Edit and Populate Secrets
```bash
nano .env.production
```

Populate the required values:
```bash
DOMAIN_NAME=your-ec2-domain-or-elastic-ip
ACME_EMAIL=your-email@example.com
POSTGRES_PASSWORD=your_production_postgres_password
JWT_SECRET=your_minimum_32_character_cryptographically_secure_jwt_secret
INTERNAL_SERVICE_TOKEN=your_minimum_32_character_internal_service_secret_token
GEMINI_API_KEY=your_gemini_api_key
```

### Step 4: Validate Compose Syntax with Environment File
```bash
docker compose --env-file .env.production -f docker-compose.prod.yml config
```

If the syntax is valid, docker compose will print the resolved container configuration without errors.
