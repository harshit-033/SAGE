#!/usr/bin/env bash
# ==============================================================================
# SAGE — Demonstration Database Reset Script
# Usage: ./scripts/reset-demo.sh [--confirm]
# ==============================================================================
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
DOCS_DIR="${ROOT_DIR}/backend/data/documents"

BOLD='\033[1m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

echo -e "${YELLOW}${BOLD}================================================================================${NC}"
echo -e "${YELLOW}${BOLD}   SAGE — Demonstration Database Reset Utility                                  ${NC}"
echo -e "${YELLOW}${BOLD}================================================================================${NC}"

# 1. Environment Safety Check (Guard against running in production)
ACTIVE_PROFILE="${SPRING_PROFILES_ACTIVE:-dev}"
ENVIRONMENT="${NODE_ENV:-development}"

if [[ "${ACTIVE_PROFILE}" == *"prod"* ]] || [[ "${ENVIRONMENT}" == "production" ]]; then
  echo -e "${RED}${BOLD}[FATAL] Reset script cannot be run in PRODUCTION environment!${NC}"
  exit 1
fi

echo -e "${YELLOW}Warning: This will regenerate demonstration documents and reset temporary data.${NC}"
echo -e "Environment: ${ACTIVE_PROFILE} / ${ENVIRONMENT}"

# 2. Clean temporary/seeded test documents
echo -e "\n${BOLD}[1/2] Resetting demonstration documents cache...${NC}"
if [ -d "${DOCS_DIR}" ]; then
  find "${DOCS_DIR}" -name "ffffffff-*.pdf" -delete 2>/dev/null || true
  echo -e "  ${GREEN}✔${NC} Cleaned demonstration document storage."
fi

# 3. Re-run seeding script
echo -e "\n${BOLD}[2/2] Re-executing demonstration seeder...${NC}"
bash "${ROOT_DIR}/scripts/seed-demo.sh"

echo -e "\n${GREEN}${BOLD}✔ Demonstration environment reset successfully.${NC}"
