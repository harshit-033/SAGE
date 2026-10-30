#!/usr/bin/env bash
# ==============================================================================
# SAGE — Document & Media Storage Backup Script
# Archives all uploaded compliance certificates and evidence from Docker volume.
# Usage: ./scripts/backup-documents.sh [backup_destination_dir]
# ==============================================================================
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BACKEND_CONTAINER="${BACKEND_CONTAINER:-sage_backend}"
STORAGE_PATH="/app/data/documents"

BACKUP_DIR="${1:-${ROOT_DIR}/backups/documents}"
mkdir -p "${BACKUP_DIR}"

TIMESTAMP="$(date +"%Y%m%d_%H%M%S")"
BACKUP_ARCHIVE="${BACKUP_DIR}/sage_documents_${TIMESTAMP}.tar.gz"

echo "=== SAGE Document Storage Backup Initiated ==="
echo "Container:   ${BACKEND_CONTAINER}"
echo "Source Path: ${STORAGE_PATH}"
echo "Destination: ${BACKUP_ARCHIVE}"

# Check if backend container is running, or backup directly from volume
if docker ps --filter "name=${BACKEND_CONTAINER}" --format '{{.Names}}' | grep -q "^${BACKEND_CONTAINER}$"; then
  echo "Streaming document files directly from running container '${BACKEND_CONTAINER}'..."
  docker exec "${BACKEND_CONTAINER}" tar -czf - -C "${STORAGE_PATH}" . > "${BACKUP_ARCHIVE}"
else
  echo "Backend container not running. Archiving directly from Docker volume 'sage_documents'..."
  docker run --rm -v sage_documents:/data:ro -v "${BACKUP_DIR}":/backup alpine \
    tar -czf "/backup/sage_documents_${TIMESTAMP}.tar.gz" -C /data .
fi

if [ ! -s "${BACKUP_ARCHIVE}" ]; then
  echo "ERROR: Document archive was created but is empty." >&2
  rm -f "${BACKUP_ARCHIVE}"
  exit 1
fi

ARCHIVE_SIZE="$(du -h "${BACKUP_ARCHIVE}" | cut -f1)"
SHA256="$(sha256sum "${BACKUP_ARCHIVE}" | cut -d' ' -f1)"

echo "=== SAGE Document Storage Backup Successfully Completed ==="
echo "Archive Size: ${ARCHIVE_SIZE}"
echo "SHA-256:      ${SHA256}"
echo "File:         ${BACKUP_ARCHIVE}"
exit 0
