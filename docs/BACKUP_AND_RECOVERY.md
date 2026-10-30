# SAGE — Production Backup & Disaster Recovery Guide

> **Document Version**: 1.0.0  
> **Target Environment**: AWS EC2 Docker Volumes & PostgreSQL 16  
> **Recovery Targets**:  
> - **RPO (Recovery Point Objective)**: < 24 Hours (Daily Automated Backups)  
> - **RTO (Recovery Time Objective)**: < 30 Minutes  

---

## 1. Overview of Persistent State

The SAGE platform stores persistent state in two dedicated Docker named volumes:
1. `sage_pgdata`: Contains PostgreSQL 16 database clusters (all transactional user data, applications, evaluations, audit logs, and document metadata).
2. `sage_documents`: Contains uploaded compliance certificates, affidavits, building safety approvals, and OCR processing files mounted at `/app/data/documents`.

---

## 2. Automated Backup Scripts

### 2.1 Database Backup Script (`scripts/backup-db.sh`)
Executes `pg_dump` inside the running PostgreSQL container, strips container-specific ownership tags, formats SQL with clean drop commands, and compresses into gzip.

**Manual Execution**:
```bash
./scripts/backup-db.sh
```
Default destination: `backups/database/sage_db_sage_db_YYYYMMDD_HHMMSS.sql.gz`

### 2.2 Document Storage Backup Script (`scripts/backup-documents.sh`)
Archives the document volume preserving directory structure, timestamps, and permissions. Can execute while the backend is running or independently via an ephemeral Alpine utility container.

**Manual Execution**:
```bash
./scripts/backup-documents.sh
```
Default destination: `backups/documents/sage_documents_YYYYMMDD_HHMMSS.tar.gz`

---

## 3. Automated Scheduling with Linux Cron

On your AWS EC2 instance, configure cron jobs to run backups automatically:

```bash
sudo crontab -e
```

Add the following daily schedule (e.g. running at 02:00 AM and 02:30 AM IST):
```cron
# Daily SAGE Database Backup at 02:00 AM IST
0 2 * * * /opt/sage/scripts/backup-db.sh /opt/sage/backups/database >> /var/log/sage-db-backup.log 2>&1

# Daily SAGE Document Storage Backup at 02:30 AM IST
30 2 * * * /opt/sage/scripts/backup-documents.sh /opt/sage/backups/documents >> /var/log/sage-docs-backup.log 2>&1

# Optional: Clean up local backups older than 30 days
0 3 * * * find /opt/sage/backups -type f -mtime +30 -delete
```

---

## 4. Database Restoration Procedure (`scripts/restore-db.sh`)

> [!CAUTION]
> Restoring a database will overwrite the current active data. Only run this during maintenance windows or disaster recovery scenarios.

### Step 1: Identify Backup File
```bash
ls -la /opt/sage/backups/database/
```

### Step 2: Execute Restore Script
```bash
./scripts/restore-db.sh /opt/sage/backups/database/sage_db_sage_db_20261031_020000.sql.gz
```

The script will prompt for safety confirmation:
```
Type exactly 'RESTORE_CONFIRM' and press Enter:
```
Once confirmed, the script streams the decompressed SQL dump into PostgreSQL and verifies the table count.

---

## 5. Document Storage Restoration Procedure

To restore an archived document backup into the `sage_documents` volume:

```bash
BACKUP_ARCHIVE="/opt/sage/backups/documents/sage_documents_20261031_023000.tar.gz"

docker run --rm \
  -v sage_documents:/data \
  -v "${BACKUP_ARCHIVE}":/backup.tar.gz:ro \
  alpine sh -c "rm -rf /data/* && tar -xzf /backup.tar.gz -C /data"
```

---

## 6. Optional Offsite Cloud Sync (AWS S3)

To safeguard against AWS EC2 instance termination or EBS volume failure, synchronize local backups to a private S3 bucket:

```bash
# Install AWS CLI if not present
sudo apt-get install -y awscli

# Sync backups to private AWS S3 bucket
aws s3 sync /opt/sage/backups/ s3://sage-regulatory-backups-ap-south-1/backups/ \
  --sse aws:kms \
  --storage-class STANDARD_IA
```
Add this command to your cron schedule following backup completion.
