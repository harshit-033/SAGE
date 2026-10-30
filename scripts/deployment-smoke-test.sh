#!/usr/bin/env bash
# ==============================================================================
# SAGE — Production Deployment Smoke Test Suite
# Validates core endpoint health, database readiness, reverse proxy, and auth.
# Usage: ./scripts/deployment-smoke-test.sh [TARGET_BASE_URL]
# Example: ./scripts/deployment-smoke-test.sh http://localhost
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

BASE_URL="${1:-}"
if [ -z "${BASE_URL}" ]; then
  if [ -n "${DOMAIN_NAME:-}" ] && [ "${DOMAIN_NAME}" != "localhost" ]; then
    BASE_URL="https://${DOMAIN_NAME}"
  else
    BASE_URL="http://localhost"
  fi
fi

echo "=============================================================================="
echo "SAGE Production Smoke Test Suite"
echo "Target Base URL: ${BASE_URL}"
echo "Timestamp:       $(date -u +"%Y-%m-%dT%H:%M:%SZ")"
echo "=============================================================================="

PASSED=0
FAILED=0

run_check() {
  local NAME="$1"
  local URL="$2"
  local EXPECTED_CODE="$3"
  local METHOD="${4:-GET}"
  local BODY="${5:-}"

  echo -n "Checking ${NAME}... "
  local HTTP_CODE
  if [ "${METHOD}" = "POST" ]; then
    HTTP_CODE=$(curl -k -s -o /dev/null -w "%{http_code}" -X POST "${URL}" \
      -H "Content-Type: application/json" -d "${BODY}" || echo "000")
  else
    HTTP_CODE=$(curl -k -s -o /dev/null -w "%{http_code}" "${URL}" || echo "000")
  fi

  if [ "${HTTP_CODE}" = "${EXPECTED_CODE}" ]; then
    echo "[PASS] (HTTP ${HTTP_CODE})"
    PASSED=$((PASSED + 1))
  else
    echo "[FAIL] (Expected ${EXPECTED_CODE}, Got ${HTTP_CODE})"
    FAILED=$((FAILED + 1))
  fi
}

# 1. Frontend SPA root
run_check "Frontend SPA Entrypoint (/)" "${BASE_URL}/" "200"

# 2. Frontend SPA client-side route fallback
run_check "Frontend SPA Route Fallback (/login)" "${BASE_URL}/login" "200"

# 3. Backend Liveness Probe
run_check "Backend Liveness Probe (/api/v1/health)" "${BASE_URL}/api/v1/health" "200"

# 4. Backend Database Readiness Probe (Verifies PostgreSQL connection)
run_check "Database Readiness Probe (/api/v1/health/ready)" "${BASE_URL}/api/v1/health/ready" "200"

# 5. Automation Service Health Probe
run_check "Automation Service Health (/automation-api/v1/health)" "${BASE_URL}/automation-api/v1/health" "200"

# 6. Authentication API Endpoint (Expects 401 on empty/invalid credentials, proving route & filter are active)
run_check "Authentication Filter Active (/api/v1/auth/login)" "${BASE_URL}/api/v1/auth/login" "401" "POST" '{"username":"invalid@test.gov.in","password":"wrong"}'

echo "=============================================================================="
echo "Smoke Test Summary: ${PASSED} Passed, ${FAILED} Failed"
echo "=============================================================================="

if [ "${FAILED}" -gt 0 ]; then
  echo "Smoke tests encountered failures. Review service logs for details." >&2
  exit 1
fi

echo "All smoke tests passed successfully! System is fully operational."
exit 0
