# SAGE — Production Deployment Troubleshooting & Diagnostic Guide

> **Target Platform**: AWS EC2 Docker Compose Production Deployment  
> **Diagnostic Tools**: `docker compose logs`, `curl`, `ss`, `psql`, `journalctl`  

---

## 1. Quick Diagnostic Triage Matrix

```
                          [Problem Detected]
                                  |
            +---------------------+---------------------+
            |                                           |
    [Network / Ingress]                         [Application State]
            |                                           |
    +-------+-------+                           +-------+-------+
    |               |                           |               |
[HTTP 502 Bad]  [HTTPS/SSL]              [DB Connection] [OCR / OOM]
  Gateway         Failed                   Refused         Crashes
    |               |                           |               |
Check Upstream  Check Port 80               Check db logs   Check memory
Backend/Nginx   in Security Group           & healthcheck   & swapfile
```

---

## 2. Common Deployment Issues & Exact Resolutions

### 2.1 Issue: `HTTP 502 Bad Gateway` from Caddy or Nginx
- **Symptoms**: Browser shows `502 Bad Gateway` when accessing `https://sage.yourdomain.gov.in/` or `/api/`.
- **Root Cause**: The upstream service (`frontend` or `backend`) is either still booting, failed its healthcheck, or crashed.
- **Diagnostic Commands**:
  ```bash
  # Check container statuses
  docker compose --env-file infrastructure/.env.production -f infrastructure/docker-compose.prod.yml ps

  # Inspect backend logs
  docker compose --env-file infrastructure/.env.production -f infrastructure/docker-compose.prod.yml logs --tail 50 backend
  ```
- **Remediation**:
  1. If backend is still initializing Flyway migrations (takes 10–30s on first boot), wait until logs output `Started SageApplication`.
  2. If backend threw an exception, check database connectivity (`POSTGRES_PASSWORD` mismatch or database container not ready).

---

### 2.2 Issue: Automated TLS Certificate Issuance Fails in Caddy
- **Symptoms**: Caddy logs display `acme: error: 400 :: urn:ietf:params:acme:error:connection :: Fetching ...: Timeout during connect`.
- **Root Cause**: Port 80 is blocked by the AWS EC2 Security Group, or DNS A-record does not point to the Elastic IP.
- **Diagnostic Commands**:
  ```bash
  # Check if domain resolves to EC2 Elastic IP
  dig +short {$DOMAIN_NAME}

  # Test inbound port 80 accessibility from outside
  curl -I -m 5 http://{$DOMAIN_NAME}/
  ```
- **Remediation**:
  1. Open the AWS EC2 Console -> **Security Groups** -> Ensure inbound rule exists for **Port 80 (HTTP)** from `0.0.0.0/0`.
  2. Ensure your domain registrar has an **A Record** pointing directly to your Elastic IP.

---

### 2.3 Issue: `413 Request Entity Too Large` on Document Upload
- **Symptoms**: Institutional officer attempts to upload a large PDF certificate (e.g. 15MB) and the browser receives HTTP 413.
- **Root Cause**: Reverse proxy request body limit exceeded.
- **Remediation**:
  Both Caddy and Nginx have been configured with 50MB request body limits:
  - Caddy: `request_body { max_size 50MB }` in `infrastructure/Caddyfile`
  - Nginx: `client_max_body_size 50M;` in `frontend/nginx.conf`
  If a custom Nginx configuration was deployed, ensure `client_max_body_size 50M;` is present and reload the container:
  ```bash
  docker compose -f infrastructure/docker-compose.prod.yml restart frontend
  ```

---

### 2.4 Issue: Automation Engine OOM (Out of Memory) during OCR
- **Symptoms**: Automation container restarts unexpectedly during PDF text extraction or image preprocessing.
- **Root Cause**: Multi-page scanned PDF rendering exceeds container memory limit.
- **Diagnostic Commands**:
  ```bash
  docker inspect --format='{{.State.OOMKilled}}' sage_automation
  # Returns 'true' if container was killed by kernel OOM
  ```
- **Remediation**:
  1. Configure 4GB swap space on the host EC2 instance as documented in the deployment guide:
     ```bash
     sudo swapon --show
     ```
  2. If running concurrent multi-page batch OCR, adjust `deploy.resources.limits.memory` in `docker-compose.prod.yml` from `2048M` to `3072M`.

---

### 2.5 Issue: Inter-Service Token Authorization Rejected (403 Forbidden)
- **Symptoms**: Backend logs show `Failed to invoke Automation OCR service: 403 Forbidden` or Automation logs show `Invalid service token`.
- **Root Cause**: `INTERNAL_SERVICE_TOKEN` mismatch between backend and automation services.
- **Remediation**:
  Verify both services receive the exact same `INTERNAL_SERVICE_TOKEN` in `infrastructure/.env.production`:
  ```bash
  grep "INTERNAL_SERVICE_TOKEN" infrastructure/.env.production
  ```
  Ensure the token is at least 32 characters long and restart the containers.

---

### 2.6 Issue: Flyway Migration Checksum Mismatch
- **Symptoms**: Spring Boot backend fails to boot with `org.flywaydb.core.api.exception.FlywayValidateException: Validate failed: Migration checksum mismatch`.
- **Root Cause**: A previously applied SQL migration file was altered after being committed to the database.
- **Remediation**:
  Never modify existing migration files (`V1`–`V18`). Always add a new versioned migration (e.g. `V19__description.sql`).
  To repair checksums in a non-production recovery scenario:
  ```bash
  docker exec -it sage_postgres psql -U sage_user -d sage_db -c "DELETE FROM flyway_schema_history WHERE version='XX';"
  ```

---

## 3. Useful Operational Commands Cheat Sheet

| Purpose | Command |
| :--- | :--- |
| **View Real-Time Logs** | `docker compose -f infrastructure/docker-compose.prod.yml logs -f --tail 100` |
| **Inspect Running Containers** | `docker compose -f infrastructure/docker-compose.prod.yml ps` |
| **Monitor Host Resources** | `htop` or `docker stats` |
| **Test Database Connectivity** | `docker exec -it sage_postgres pg_isready -U sage_user -d sage_db` |
| **Connect to PostgreSQL CLI** | `docker exec -it sage_postgres psql -U sage_user -d sage_db` |
| **Inspect Document Storage** | `docker exec -it sage_backend ls -la /app/data/documents` |
| **Restart Single Service** | `docker compose -f infrastructure/docker-compose.prod.yml restart backend` |
| **Execute Smoke Test** | `./scripts/deployment-smoke-test.sh` |
