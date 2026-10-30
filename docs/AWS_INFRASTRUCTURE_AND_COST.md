# SAGE — AWS Infrastructure & Cost Architecture Guide

> **Target Cloud**: Amazon Web Services (AWS)  
> **Region**: Asia Pacific (Mumbai) — `ap-south-1`  
> **Currency**: USD ($) and approximate INR (₹ at ₹84/USD)  
> **Pricing Reference**: [AWS Pricing Calculator](https://calculator.aws/#/) (Current AWS Public On-Demand Rates)  

---

## 1. Sizing Profiles & Architectural Recommendations

To balance statutory performance with cloud economics, SAGE provides two sizing models:
1. **Production Deployment Profile**: Sized for concurrent regional operations, multi-page OCR document batch processing, and CP-SAT committee scheduling.
2. **Demonstration / Evaluation Profile**: Minimum viable configuration suitable for prototype demonstrations, sandbox evaluation, and staging validation.

---

## 2. Infrastructure Sizing Matrix

| Component | Production Recommendation | Demonstration / Staging Recommendation | Technical Rationale |
| :--- | :--- | :--- | :--- |
| **Compute (EC2)** | **`t3.xlarge`** (4 vCPU, 16 GiB RAM) | **`t3.large`** (2 vCPU, 8 GiB RAM) | Java 21 Spring Boot requires ~1.5GB RAM, PostgreSQL requires ~1GB RAM, Python OCR/Indra requires ~1.5GB RAM, and Google OR-Tools CP-SAT solver utilizes up to 4 parallel worker threads during committee optimization. |
| **Operating System** | Ubuntu Server 24.04 LTS (x86_64) | Ubuntu Server 24.04 LTS (x86_64) | Long Term Support through 2029, kernel 6.8+, native Docker CE & systemd support. |
| **Primary Storage (EBS)** | **100 GiB gp3 SSD** (3000 IOPS, 125 MB/s) | **50 GiB gp3 SSD** (3000 IOPS, 125 MB/s) | Accommodates OS (~4GB), Docker images (~6GB), PostgreSQL database (~10GB), and document uploads (~80GB). |
| **Network IP** | 1x AWS Elastic IP (Static IPv4) | 1x AWS Elastic IP (Static IPv4) | Stable A-record routing for Let's Encrypt automated TLS certificate issuance. |
| **Backup Storage (S3)** | AWS S3 Standard-IA (Infrequent Access) | AWS S3 Standard-IA (Infrequent Access) | Automated daily snapshots of database dumps and document tarballs. |
| **Reverse Proxy** | Caddy v2 (Embedded in Docker) | Caddy v2 (Embedded in Docker) | Replaces expensive AWS Application Load Balancer ($22+/month) with zero-cost containerized TLS termination. |

---

## 3. Detailed Monthly Cost Breakdown (Mumbai `ap-south-1`)

### 3.1 Option A: Production Profile (`t3.xlarge`, 100 GB EBS)

| AWS Resource | Specification | Monthly Cost (USD) | Monthly Cost (INR Approx.) |
| :--- | :--- | :--- | :--- |
| **EC2 Compute** | `t3.xlarge` On-Demand (730 hours/month @ $0.1664/hr) | ~$121.50 | ₹10,200 |
| **EBS Storage (gp3)** | 100 GiB baseline ($0.0912/GB-month) | ~$9.12 | ₹765 |
| **Elastic IP** | 1 IPv4 address (Free while attached to active running instance) | $0.00 | ₹0 |
| **Data Transfer Out** | First 100 GB free/month, estimated 50 GB billable @ $0.1093/GB | ~$5.46 | ₹460 |
| **S3 Off-Site Backups** | 50 GiB Standard-IA ($0.0131/GB-month + PUT requests) | ~$1.00 | ₹85 |
| **Total Production Estimate** | **Full Workload SAGE Platform** | **~$137.08 / month** | **~₹11,510 / month** |

*Note: Utilizing a 1-Year or 3-Year AWS Compute Savings Plan reduces the EC2 compute cost by 35% to 55% (dropping total monthly cost to ~$85 / month).*

---

### 3.2 Option B: Demonstration / Staging Profile (`t3.large`, 50 GB EBS)

| AWS Resource | Specification | Monthly Cost (USD) | Monthly Cost (INR Approx.) |
| :--- | :--- | :--- | :--- |
| **EC2 Compute** | `t3.large` On-Demand (730 hours/month @ $0.0832/hr) | ~$60.75 | ₹5,100 |
| **EBS Storage (gp3)** | 50 GiB baseline ($0.0912/GB-month) | ~$4.56 | ₹380 |
| **Elastic IP** | 1 IPv4 address attached to instance | $0.00 | ₹0 |
| **Data Transfer Out** | Within 100 GB free tier | $0.00 | ₹0 |
| **S3 Off-Site Backups** | 10 GiB Standard-IA | ~$0.20 | ₹17 |
| **Total Demo Estimate** | **Demonstration / Hackathon Staging** | **~$65.51 / month** | **~₹5,500 / month** |

---

## 4. External API Services: Google Gemini Intelligence

- **Model**: `gemini-flash-lite-latest`
- **Pricing**:
  - Input tokens: Free tier available; $0.075 per 1,000,000 tokens for paid tier.
  - Output tokens: $0.30 per 1,000,000 tokens.
- **Estimated Usage for SAGE**:
  - Average document upload inspection prompt: ~1,200 input tokens.
  - SAGE AI advisory review findings payload: ~350 output tokens.
  - **Cost per 1,000 document uploads**: Less than **$0.20 (approx. ₹17)**.

---

## 5. Cost Optimization Strategies

1. **Scheduled On/Off for Non-Production**:
   - For demo or staging environments, schedule instance stop during off-hours (e.g. 8 PM to 8 AM IST), cutting EC2 costs by 50%.
2. **Eliminate Managed Service Overhead**:
   - Using containerized PostgreSQL 16 on local EBS storage avoids managed AWS RDS PostgreSQL costs ($40–$120/month).
   - Using containerized Caddy v2 avoids AWS Application Load Balancer costs ($22–$35/month).
3. **EBS Snapshot Lifecycle Policies**:
   - Configure AWS Data Lifecycle Manager (DLM) to retain daily snapshots for 7 days, weekly for 4 weeks, automatically purging older snapshots.
