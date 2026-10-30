# SAGE — AWS EC2 Production Deployment Runbook

> **Target Platform**: Amazon Web Services (AWS) Elastic Compute Cloud (EC2)  
> **Region**: Asia Pacific (Mumbai) — `ap-south-1`  
> **Operating System**: Ubuntu Server 24.04 LTS (Noble Numbat)  
> **Deployment Architecture**: Docker Engine + Docker Compose v2 + PostgreSQL 16 + Caddy Reverse Proxy  
> **Repository**: [https://github.com/harshit-033/SAGE](https://github.com/harshit-033/SAGE)  

---

## 1. Architectural Overview & Network Security

On AWS EC2, the SAGE platform runs inside an isolated Docker bridge network (`sage_network`). 
- **Only Ports 80 (HTTP) and 443 (HTTPS)** are exposed to the public internet via Caddy.
- **Port 22 (SSH)** is restricted strictly to the administrator's IP address.
- **Backend (:8080), Automation (:8000), and Database (:5432)** are never exposed to the public internet.

```
       [Internet User]
              |
              | HTTPS (443)
              v
     [AWS Elastic IP (Static)]
              |
              v
 [EC2 Security Group: 80, 443, 22]
              |
              v
      [Caddy Reverse Proxy]
              |
              v
    [Nginx Frontend: 80]
     |                |
     v                v
[Backend: 8080]  [Automation: 8000]
     |                |
     +------->+<------+
              |
              v
      [PostgreSQL: 5432]
```

---

## 2. AWS Console Setup (Step-by-Step)

### 2.1 Launch EC2 Instance
1. Open the [AWS EC2 Console](https://console.aws.amazon.com/ec2/) and switch the region to **Asia Pacific (Mumbai) `ap-south-1`**.
2. Click **Launch instance**.
3. **Name**: `SAGE-Production-Server`.
4. **Application and OS Images**: **Ubuntu Server 24.04 LTS (HVM), SSD Volume Type** (64-bit x86).
5. **Instance Type**:
   - *Recommended for Production*: **`t3.xlarge`** (4 vCPU, 16 GB RAM) — optimal for concurrent OCR and CP-SAT solver jobs.
   - *Minimum for Staging / Demo*: **`t3.large`** (2 vCPU, 8 GB RAM) or **`t3.medium`** (2 vCPU, 4 GB RAM + swap).
6. **Key Pair**: Select an existing key pair or **Create new key pair** (`sage-prod-key.pem`).
7. **Network Settings**:
   - VPC: Default VPC
   - Auto-assign public IP: Enable
   - Firewall (Security Groups): Select **Create security group**.
8. **Configure Storage**:
   - Volume 1: **100 GiB** General Purpose SSD (**gp3**), 3000 IOPS, 125 MB/s throughput.
9. Click **Launch instance**.

### 2.2 Configure Security Group Inbound Rules
Navigate to **EC2 > Security Groups**, select the instance security group, and configure:

| Type | Protocol | Port Range | Source | Purpose |
| :--- | :--- | :--- | :--- | :--- |
| **SSH** | TCP | `22` | `My IP` (`xx.xx.xx.xx/32`) | Secure terminal access |
| **HTTP** | TCP | `80` | `0.0.0.0/0` | ACME challenge & HTTP->HTTPS redirect |
| **HTTPS** | TCP | `443` | `0.0.0.0/0` | Public web application traffic |

### 2.3 Allocate and Associate an Elastic IP (Static Public IP)
1. In the left navigation, click **Elastic IPs** > **Allocate Elastic IP address**.
2. Select the allocated IP > **Actions** > **Associate Elastic IP address**.
3. Select your instance and click **Associate**. Note the public IP (e.g., `13.232.xxx.xxx`).

### 2.4 Configure DNS A-Record
In your DNS registrar (Route 53, GoDaddy, Cloudflare, etc.):
- Create an **A Record** pointing `sage.yourdomain.gov.in` to the **Elastic IP**.

---

## 3. Server Initialization (On the EC2 Instance)

Connect to your EC2 instance from your local workstation:
```bash
# [LOCAL WORKSTATION]
chmod 400 sage-prod-key.pem
ssh -i sage-prod-key.pem ubuntu@YOUR_ELASTIC_IP
```

Execute the following commands on the remote EC2 server:

### 3.1 Update OS Packages
```bash
# [EC2 INSTANCE]
sudo apt-get update && sudo apt-get upgrade -y
sudo apt-get install -y ca-certificates curl gnupg lsb-release git htop
```

### 3.2 Install Official Docker Engine & Compose v2
```bash
# [EC2 INSTANCE]
sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
sudo chmod a+r /etc/apt/keyrings/docker.gpg

echo \
  "deb [arch="$(dpkg --print-architecture)" signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  "$(. /etc/os-release && echo "$VERSION_CODENAME")" stable" | \
  sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

sudo apt-get update
sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

# Grant ubuntu user docker privileges
sudo usermod -aG docker $USER
newgrp docker
docker --version && docker compose version
```

### 3.3 Configure Swap Space (Recommended Resilience)
For instances with 8GB or less RAM, configure 4GB swap space:
```bash
# [EC2 INSTANCE]
sudo fallocate -l 4G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
```

---

## 4. Application Deployment Workflow

### 4.1 Clone Repository & Set Permissions
```bash
# [EC2 INSTANCE]
sudo mkdir -p /opt/sage
sudo chown -R $USER:$USER /opt/sage
git clone https://github.com/harshit-033/SAGE.git /opt/sage
cd /opt/sage
```

### 4.2 Configure Production Environment File
```bash
# [EC2 INSTANCE]
cp infrastructure/.env.production.example infrastructure/.env.production
chmod 600 infrastructure/.env.production
nano infrastructure/.env.production
```

Configure your production parameters:
```env
DOMAIN_NAME=sage.yourdomain.gov.in
ACME_EMAIL=admin@yourdomain.gov.in
POSTGRES_DB=sage_db
POSTGRES_USER=sage_user
POSTGRES_PASSWORD=your_production_postgres_password
JWT_SECRET=your_minimum_32_character_jwt_secret_here
INTERNAL_SERVICE_TOKEN=your_minimum_32_character_internal_service_token_here
GEMINI_API_KEY=your_gemini_api_key_here
GEMINI_MODEL=gemini-flash-lite-latest
SPRING_PROFILES_ACTIVE=prod
ENVIRONMENT=prod
CORS_ORIGINS=https://sage.yourdomain.gov.in
```

### 4.3 Execute Automated Deployment Script
```bash
# [EC2 INSTANCE]
./scripts/deploy-production.sh
```

The script will:
1. Verify system dependencies (`docker`, `docker compose`, `curl`).
2. Validate `infrastructure/docker-compose.prod.yml` against `.env.production`.
3. Build the container images (Backend, Automation, Frontend).
4. Launch the services: `sage_postgres`, `sage_backend`, `sage_automation`, `sage_frontend`, `sage_caddy`.
5. Poll container health probes until all report healthy.
6. Automatically run the post-deployment smoke test suite.

---

## 5. Verification & Live Smoke Testing

Once deployment completes, execute the smoke test suite:
```bash
# [EC2 INSTANCE]
./scripts/deployment-smoke-test.sh https://sage.yourdomain.gov.in
```

### Expected Output:
```
==============================================================================
SAGE Production Smoke Test Suite
Target Base URL: https://sage.yourdomain.gov.in
==============================================================================
Checking Frontend SPA Entrypoint (/)... [PASS] (HTTP 200)
Checking Frontend SPA Route Fallback (/login)... [PASS] (HTTP 200)
Checking Backend Liveness Probe (/api/v1/health)... [PASS] (HTTP 200)
Checking Database Readiness Probe (/api/v1/health/ready)... [PASS] (HTTP 200)
Checking Automation Service Health (/automation-api/v1/health)... [PASS] (HTTP 200)
Checking Authentication Filter Active (/api/v1/auth/login)... [PASS] (HTTP 401)
==============================================================================
Smoke Test Summary: 6 Passed, 0 Failed
==============================================================================
```

### Test User Login via CLI:
```bash
curl -s -X POST https://sage.yourdomain.gov.in/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin@aicte-demo.gov.in","password":"Password123!"}' | grep "accessToken"
```

---

## 6. Production Maintenance & Operations

### 6.1 View Live Container Logs
```bash
# Follow logs for all services
docker compose --env-file infrastructure/.env.production -f infrastructure/docker-compose.prod.yml logs -f

# Follow specific service (e.g. backend or caddy)
docker compose --env-file infrastructure/.env.production -f infrastructure/docker-compose.prod.yml logs -f backend
docker compose --env-file infrastructure/.env.production -f infrastructure/docker-compose.prod.yml logs -f caddy
```

### 6.2 Check Container Status & Resource Usage
```bash
docker compose --env-file infrastructure/.env.production -f infrastructure/docker-compose.prod.yml ps
docker stats --no-stream
```

### 6.3 Restart Services
```bash
# Graceful restart of backend
docker compose --env-file infrastructure/.env.production -f infrastructure/docker-compose.prod.yml restart backend
```

### 6.4 Pull Code Updates & Zero-Downtime Rebuild
```bash
cd /opt/sage
git pull origin main
./scripts/deploy-production.sh
```

### 6.5 Execute Immediate Database & Document Backup
```bash
./scripts/backup-db.sh
./scripts/backup-documents.sh
```

### 6.6 Restore Database in Disaster Recovery
```bash
./scripts/restore-db.sh /opt/sage/backups/database/sage_db_sage_db_YYYYMMDD_HHMMSS.sql.gz
```
