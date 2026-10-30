#!/usr/bin/env bash
# ==============================================================================
# SAGE — Production Database Backup Script
# Creates a compressed, timestamped PostgreSQL backup from the Docker container.
# Usage: ./scripts/backup-db.sh [backup_destination_dir]
# ==============================================================================
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ENV_FILE="${ROOT_DIR}/infrastructure/.env.production"

if [ -f "${ENV_FILE}" ]; then
  # Load production variables safely without exporting comments
  set -a
  # shellcheck disable=SC1090
  source "${ENV_FILE}"
  set +a
fi

DB_CONTAINER="${DB_CONTAINER:-sage_postgres}"
POSTGRES_USER="${POSTGRES_USER:-sage_user}"
POSTGRES_DB="${POSTGRES_DB:-sage_db}"

BACKUP_DIR="${1:-${ROOT_DIR}/backups/database}"
mkdir -p "${BACKUP_DIR}"

TIMESTAMP="$(date +"%Y%m%d_%H%M%S")"
BACKUP_FILE="${BACKUP_DIR}/sage_db_${POSTGRES_DB}_${TIMESTAMP}.sql.gz"

echo "=== SAGE Database Backup Initiated ==="
echo "Container:   ${DB_CONTAINER}"
echo "Database:    ${POSTGRES_DB}"
echo "User:        ${POSTGRES_USER}"
echo "Destination: ${BACKUP_FILE}"

# Verify container is running
if ! docker ps --filter "name=${DB_CONTAINER}" --format '{{.Names}}' | grep -q "^${DB_CONTAINER}$"; then
  echo "ERROR: PostgreSQL container '${DB_CONTAINER}' is not running." >&2
  exit 1
fi

# Execute pg_dump and compress on the fly
docker exec -e PGPASSWORD="${POSTGRES_PASSWORD:-}" "${DB_CONTAINER}" \
  pg_dump -U "${POSTGRES_USER}" -d "${POSTGRES_DB}" --format=plain --no-owner --clean --if-exists \
  | gzip -9 > "${BACKUP_FILE}"

# Verify backup file size is greater than 0
if [ ! -s "${BACKUP_FILE}" ]; then
  echo "ERROR: Backup file was created but is empty." >&2
  rm -f "${BACKUP_FILE}"
  exit 2
fi

BACKUP_SIZE="$(du -h "${BACKUP_FILE}" | cut -f1)"
SHA256="$(sha256sum "${BACKUP_FILE}" | cut -d' ' -f1)"

echo "=== SAGE Database Backup Successfully Completed ==="
echo "File Size: ${BACKUP_SIZE}"
echo "SHA-256:   ${SHA256}"
echo "File:      ${BACKUP_FILE}"
exit 0
