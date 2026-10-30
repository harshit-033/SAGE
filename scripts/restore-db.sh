#!/usr/bin/env bash
# ==============================================================================
# SAGE — Production Database Restore Script
# Restores a PostgreSQL backup from a .sql or .sql.gz archive into the container.
# Usage: ./scripts/restore-db.sh <backup_file_path> [--force]
# ==============================================================================
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ENV_FILE="${ROOT_DIR}/infrastructure/.env.production"

if [ -f "${ENV_FILE}" ]; then
  set -a
  # shellcheck disable=SC1090
  source "${ENV_FILE}"
  set +a
fi

DB_CONTAINER="${DB_CONTAINER:-sage_postgres}"
POSTGRES_USER="${POSTGRES_USER:-sage_user}"
POSTGRES_DB="${POSTGRES_DB:-sage_db}"

BACKUP_FILE="${1:-}"
FORCE_FLAG="${2:-}"

if [ -z "${BACKUP_FILE}" ]; then
  echo "Usage: $0 <path_to_backup_file.sql.gz> [--force]" >&2
  exit 1
fi

if [ ! -f "${BACKUP_FILE}" ]; then
  echo "ERROR: Backup file '${BACKUP_FILE}' does not exist or is not readable." >&2
  exit 2
fi

echo "=== SAGE Database Restore Verification ==="
echo "Target Container: ${DB_CONTAINER}"
echo "Target Database:  ${POSTGRES_DB}"
echo "Backup File:      ${BACKUP_FILE}"
echo "Backup File Size: $(du -h "${BACKUP_FILE}" | cut -f1)"

# Check container running
if ! docker ps --filter "name=${DB_CONTAINER}" --format '{{.Names}}' | grep -q "^${DB_CONTAINER}$"; then
  echo "ERROR: PostgreSQL container '${DB_CONTAINER}' is not running." >&2
  exit 3
fi

# Mandatory human confirmation unless --force passed
if [ "${FORCE_FLAG}" != "--force" ]; then
  echo ""
  echo "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
  echo "WARNING: Restoring will overwrite existing data in '${POSTGRES_DB}'!"
  echo "To abort, press Ctrl+C."
  echo "To proceed, type exactly 'RESTORE_CONFIRM' and press Enter:"
  echo "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
  read -r CONFIRMATION
  if [ "${CONFIRMATION}" != "RESTORE_CONFIRM" ]; then
    echo "Restore aborted by user."
    exit 0
  fi
fi

echo "Initiating restore from '${BACKUP_FILE}'..."

if [[ "${BACKUP_FILE}" == *.gz ]]; then
  gunzip -c "${BACKUP_FILE}" | docker exec -i -e PGPASSWORD="${POSTGRES_PASSWORD:-}" "${DB_CONTAINER}" \
    psql -U "${POSTGRES_USER}" -d "${POSTGRES_DB}"
else
  docker exec -i -e PGPASSWORD="${POSTGRES_PASSWORD:-}" "${DB_CONTAINER}" \
    psql -U "${POSTGRES_USER}" -d "${POSTGRES_DB}" < "${BACKUP_FILE}"
fi

# Verify restoration by querying table count
TABLE_COUNT=$(docker exec -i -e PGPASSWORD="${POSTGRES_PASSWORD:-}" "${DB_CONTAINER}" \
  psql -U "${POSTGRES_USER}" -d "${POSTGRES_DB}" -t -c "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='public';")

echo "=== SAGE Database Restore Successfully Completed ==="
echo "Tables verified in schema 'public': ${TABLE_COUNT// /}"
exit 0
