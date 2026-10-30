#!/usr/bin/env bash
# ==============================================================================
# SAGE — Production Deployment Orchestrator
# Builds, validates, deploys, and smoke-tests SAGE on AWS EC2 with Docker Compose.
# Usage: ./scripts/deploy-production.sh [--skip-smoke-test]
# ==============================================================================
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
INFRA_DIR="${ROOT_DIR}/infrastructure"
COMPOSE_FILE="${INFRA_DIR}/docker-compose.prod.yml"
ENV_FILE="${INFRA_DIR}/.env.production"
ENV_EXAMPLE="${INFRA_DIR}/.env.production.example"

# Parse Command-Line Arguments
TARGET_URL=""
SKIP_SMOKE="false"

while [[ $# -gt 0 ]]; do
  case "$1" in
    --skip-smoke-test)
      SKIP_SMOKE="true"
      shift
      ;;
    --url)
      TARGET_URL="$2"
      shift 2
      ;;
    http://*|https://*)
      TARGET_URL="$1"
      shift
      ;;
    *)
      echo "WARNING: Unknown argument '$1' ignored." >&2
      shift
      ;;
  esac
done

echo "=============================================================================="
echo "SAGE — Production Deployment Orchestrator"
echo "Repository Root: ${ROOT_DIR}"
echo "Compose File:    ${COMPOSE_FILE}"
echo "=============================================================================="

# 1. Check Required System Tools
echo -n "• Checking required deployment tools... "
for TOOL in docker curl; do
  if ! command -v "${TOOL}" >/dev/null 2>&1; then
    echo "FAILED"
    echo "ERROR: Required tool '${TOOL}' is not installed or not in PATH." >&2
    exit 1
  fi
done
if ! docker compose version >/dev/null 2>&1; then
  echo "FAILED"
  echo "ERROR: 'docker compose' (v2 plugin) is required." >&2
  exit 1
fi
echo "OK"

# 2. Check Production Environment Configuration
echo -n "• Verifying production environment configuration... "
if [ ! -f "${ENV_FILE}" ]; then
  echo "MISSING"
  echo "WARNING: Production environment file '${ENV_FILE}' not found."
  if [ -f "${ENV_EXAMPLE}" ]; then
    echo "Creating '${ENV_FILE}' from '${ENV_EXAMPLE}'..."
    cp "${ENV_EXAMPLE}" "${ENV_FILE}"
    echo "PLEASE EDIT '${ENV_FILE}' with your production credentials before continuing."
    exit 2
  else
    echo "ERROR: Neither .env.production nor .env.production.example found." >&2
    exit 2
  fi
fi
echo "OK"

# 3. Validate Docker Compose Specification
echo -n "• Validating production Compose configuration... "
docker compose --env-file "${ENV_FILE}" -f "${COMPOSE_FILE}" config >/dev/null
echo "OK"

# 4. Build Production Container Images
echo "• Building production Docker images (Backend, Automation, Frontend)..."
docker compose --env-file "${ENV_FILE}" -f "${COMPOSE_FILE}" build

# 5. Start / Update Production Services
echo "• Deploying production services with in-place container recreation..."
docker compose --env-file "${ENV_FILE}" -f "${COMPOSE_FILE}" up -d --remove-orphans

# 6. Wait for Service Health with Independent Per-Service Timeouts
echo "• Awaiting container health confirmation..."
SERVICES=("db" "backend" "automation" "frontend" "caddy")

for SVC in "${SERVICES[@]}"; do
  echo -n "  - Waiting for '${SVC}' to become healthy... "
  ELAPSED=0
  SVC_TIMEOUT=60
  # Spring Boot backend executes Flyway migrations (V1-V18) and JPA validation
  if [ "${SVC}" = "backend" ]; then
    SVC_TIMEOUT=120
  fi

  while true; do
    CONTAINER_NAME=$(docker compose --env-file "${ENV_FILE}" -f "${COMPOSE_FILE}" ps -q "${SVC}" 2>/dev/null || echo "")
    if [ -z "${CONTAINER_NAME}" ]; then
      STATUS="not_started"
    else
      STATUS=$(docker inspect --format='{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}' "${CONTAINER_NAME}" 2>/dev/null || echo "unknown")
    fi

    if [ "${STATUS}" = "healthy" ] || [ "${STATUS}" = "running" ]; then
      echo "OK (${STATUS})"
      break
    elif [ "${STATUS}" = "unhealthy" ]; then
      echo "FAILED"
      echo "ERROR: Service '${SVC}' reported UNHEALTHY state." >&2
      docker compose --env-file "${ENV_FILE}" -f "${COMPOSE_FILE}" logs --tail 100 "${SVC}"
      exit 3
    fi

    sleep 3
    ELAPSED=$((ELAPSED + 3))
    if [ "${ELAPSED}" -ge "${SVC_TIMEOUT}" ]; then
      echo "TIMEOUT"
      echo "ERROR: Timeout after ${SVC_TIMEOUT}s waiting for service '${SVC}' to report healthy." >&2
      docker compose --env-file "${ENV_FILE}" -f "${COMPOSE_FILE}" logs --tail 100 "${SVC}"
      exit 4
    fi
  done
done

# 7. Execute Post-Deployment Smoke Tests
if [ "${SKIP_SMOKE}" != "true" ]; then
  echo ""
  echo "• Executing deployment smoke tests..."
  if [ -n "${TARGET_URL}" ]; then
    "${ROOT_DIR}/scripts/deployment-smoke-test.sh" "${TARGET_URL}"
  else
    "${ROOT_DIR}/scripts/deployment-smoke-test.sh"
  fi
fi

echo ""
echo "=============================================================================="
echo "SAGE PRODUCTION DEPLOYMENT COMPLETED SUCCESSFULLY"
echo "=============================================================================="
docker compose --env-file "${ENV_FILE}" -f "${COMPOSE_FILE}" ps
exit 0
