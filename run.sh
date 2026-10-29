#!/usr/bin/env bash
# ==============================================================================
# SAGE — Smart AICTE Governance Engine
# Complete Platform Runner Script (Backend, Automation & Frontend)
# SIH 2026 Problem Statement 26262
# ==============================================================================
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# ANSI Terminal Colors
BOLD='\033[1m'
NAVY='\033[38;5;18m'
BLUE='\033[38;5;33m'
CYAN='\033[38;5;51m'
GREEN='\033[38;5;42m'
SAFFRON='\033[38;5;214m'
RED='\033[38;5;196m'
MUTED='\033[38;5;244m'
NC='\033[0m'

LOG_DIR="${ROOT_DIR}/.logs"
mkdir -p "${LOG_DIR}"

AUTOMATION_LOG="${LOG_DIR}/automation.log"
BACKEND_LOG="${LOG_DIR}/backend.log"
FRONTEND_LOG="${LOG_DIR}/frontend.log"

PID_AUTOMATION=0
PID_BACKEND=0
PID_FRONTEND=0

cleanup() {
  echo -e "\n${SAFFRON}${BOLD}Initiating graceful shutdown of all SAGE services...${NC}"

  if [ "${PID_FRONTEND}" -ne 0 ] && kill -0 "${PID_FRONTEND}" 2>/dev/null; then
    echo -e "  ${MUTED}• Stopping React Frontend (PID: ${PID_FRONTEND})...${NC}"
    kill -TERM "${PID_FRONTEND}" 2>/dev/null || true
  fi

  if [ "${PID_BACKEND}" -ne 0 ] && kill -0 "${PID_BACKEND}" 2>/dev/null; then
    echo -e "  ${MUTED}• Stopping Spring Boot Backend (PID: ${PID_BACKEND})...${NC}"
    kill -TERM "${PID_BACKEND}" 2>/dev/null || true
  fi

  if [ "${PID_AUTOMATION}" -ne 0 ] && kill -0 "${PID_AUTOMATION}" 2>/dev/null; then
    echo -e "  ${MUTED}• Stopping Python Automation Engine (PID: ${PID_AUTOMATION})...${NC}"
    kill -TERM "${PID_AUTOMATION}" 2>/dev/null || true
  fi

  # Fallback check for lingering port listeners
  sleep 1
  for PORT in 5173 8080 8000; do
    PIDS="$(lsof -ti :${PORT} 2>/dev/null || true)"
    if [ -n "${PIDS}" ]; then
      kill -9 ${PIDS} 2>/dev/null || true
    fi
  done

  echo -e "${GREEN}${BOLD}✔ All SAGE platform services terminated cleanly.${NC}"
  exit 0
}

trap cleanup SIGINT SIGTERM SIGHUP EXIT

clear 2>/dev/null || true

echo -e "${BLUE}${BOLD}================================================================================${NC}"
echo -e "${BLUE}${BOLD}        SAGE — Smart AICTE Governance Engine                                  ${NC}"
echo -e "${SAFFRON}        Integrated Approval Automation, Compliance & Alert Management          ${NC}"
echo -e "${CYAN}        SIH 2026 Problem Statement 26262 • Ministry of Education, Govt of India${NC}"
echo -e "${BLUE}${BOLD}================================================================================${NC}"

# ------------------------------------------------------------------------------
# 1. Pre-Flight Runtime Verification
# ------------------------------------------------------------------------------
echo -e "\n${BOLD}[1/4] Checking environment & runtime prerequisites...${NC}"

# Check Java
if ! command -v java >/dev/null 2>&1; then
  echo -e "${RED}[ERROR] Java runtime not found. Java 21 is required.${NC}"
  exit 1
fi
JAVA_VER="$(java -version 2>&1 | head -n 1 | awk -F '"' '{print $2}')"
echo -e "  ${GREEN}✔${NC} Java Runtime    : ${BOLD}${JAVA_VER}${NC}"

# Check Python
if ! command -v python3 >/dev/null 2>&1; then
  echo -e "${RED}[ERROR] Python 3 not found. Python 3.11+ is required.${NC}"
  exit 1
fi
PYTHON_VER="$(python3 --version 2>&1)"
echo -e "  ${GREEN}✔${NC} Python Engine   : ${BOLD}${PYTHON_VER}${NC}"

# Check Node / npm
if ! command -v node >/dev/null 2>&1 || ! command -v npm >/dev/null 2>&1; then
  echo -e "${RED}[ERROR] Node.js or npm not found. Node.js 18+ is required.${NC}"
  exit 1
fi
NODE_VER="$(node -v)"
NPM_VER="$(npm -v)"
echo -e "  ${GREEN}✔${NC} Node & npm      : ${BOLD}Node ${NODE_VER} / npm ${NPM_VER}${NC}"

# Ensure backend mvnw executable
chmod +x "${ROOT_DIR}/backend/mvnw" 2>/dev/null || true
chmod +x "${ROOT_DIR}/backend/run.sh" 2>/dev/null || true

# Check ports
for PORT in 8000 8080 5173; do
  EXISTING="$(lsof -ti :${PORT} 2>/dev/null || true)"
  if [ -n "${EXISTING}" ]; then
    echo -e "  ${SAFFRON}! Port ${PORT} currently occupied. Freeing port...${NC}"
    kill -9 ${EXISTING} 2>/dev/null || true
    sleep 0.5
  fi
done

# ------------------------------------------------------------------------------
# 2. Dependency Checks & Setup
# ------------------------------------------------------------------------------
echo -e "\n${BOLD}[2/4] Verifying project dependencies...${NC}"

# Automation venv
if [ ! -d "${ROOT_DIR}/automation/venv" ]; then
  echo -e "  ${SAFFRON}• Setting up Python virtual environment (automation/venv)...${NC}"
  python3 -m venv "${ROOT_DIR}/automation/venv"
  "${ROOT_DIR}/automation/venv/bin/pip" install --upgrade pip >/dev/null 2>&1
  "${ROOT_DIR}/automation/venv/bin/pip" install -r "${ROOT_DIR}/automation/requirements.txt" >/dev/null 2>&1
  echo -e "  ${GREEN}✔${NC} Python venv initialized"
else
  echo -e "  ${GREEN}✔${NC} Python automation venv present"
fi

# Frontend node_modules
if [ ! -d "${ROOT_DIR}/frontend/node_modules" ]; then
  echo -e "  ${SAFFRON}• Installing frontend npm dependencies...${NC}"
  (cd "${ROOT_DIR}/frontend" && npm install >/dev/null 2>&1)
  echo -e "  ${GREEN}✔${NC} Frontend node_modules installed"
else
  echo -e "  ${GREEN}✔${NC} Frontend node_modules present"
fi

# ------------------------------------------------------------------------------
# 3. Starting Services
# ------------------------------------------------------------------------------
echo -e "\n${BOLD}[3/4] Starting SAGE application services in parallel...${NC}"

# Start Automation Engine (Port 8000)
echo -e "  ${CYAN}▶ Launching Python Automation & OCR Engine (port 8000)...${NC}"
(
  cd "${ROOT_DIR}/automation"
  exec "${ROOT_DIR}/automation/venv/bin/uvicorn" app.main:app --host 0.0.0.0 --port 8000
) > "${AUTOMATION_LOG}" 2>&1 &
PID_AUTOMATION=$!

# Start Spring Boot Backend (Port 8080)
echo -e "  ${BLUE}▶ Launching Spring Boot Core API (port 8080)...${NC}"
(
  cd "${ROOT_DIR}/backend"
  exec ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
) > "${BACKEND_LOG}" 2>&1 &
PID_BACKEND=$!

# Start Vite Frontend (Port 5173)
echo -e "  ${GREEN}▶ Launching React Vite Web Portal (port 5173)...${NC}"
(
  cd "${ROOT_DIR}/frontend"
  exec npm run dev
) > "${FRONTEND_LOG}" 2>&1 &
PID_FRONTEND=$!

# ------------------------------------------------------------------------------
# 4. Service Health Polling
# ------------------------------------------------------------------------------
echo -e "\n${BOLD}[4/4] Waiting for services to become healthy & ready...${NC}"

wait_for_service() {
  local NAME="$1"
  local URL="$2"
  local TIMEOUT="${3:-45}"
  local ELAPSED=0

  while [ ${ELAPSED} -lt ${TIMEOUT} ]; do
    if curl -s -f -o /dev/null "${URL}" 2>/dev/null; then
      echo -e "  ${GREEN}✔ ${NAME} is healthy and responding!${NC}"
      return 0
    fi
    sleep 1
    ELAPSED=$((ELAPSED + 1))
    echo -ne "  ${MUTED}Waiting for ${NAME}... (${ELAPSED}s)${NC}\r"
  done

  echo -e "  ${RED}✖ Timed out waiting for ${NAME} at ${URL}${NC}"
  return 1
}

wait_for_service "Automation Engine" "http://localhost:8000/" 20
wait_for_service "Spring Boot Backend" "http://localhost:8080/actuator/health" 45
wait_for_service "React Vite Frontend" "http://localhost:5173/" 20

# ------------------------------------------------------------------------------
# Summary & Portal Dashboard Banner
# ------------------------------------------------------------------------------
echo -e "\n${BLUE}${BOLD}================================================================================${NC}"
echo -e "${GREEN}${BOLD}             SAGE PLATFORM IS OPERATIONAL & READY TO USE                        ${NC}"
echo -e "${BLUE}${BOLD}================================================================================${NC}"

echo -e "\n${BOLD}Service Access URLs:${NC}"
echo -e "  • ${BOLD}Web Portal (UI)${NC}      : ${GREEN}http://localhost:5173${NC}"
echo -e "  • ${BOLD}Core REST API${NC}        : ${CYAN}http://localhost:8080${NC}"
echo -e "  • ${BOLD}Interactive Swagger${NC}  : ${CYAN}http://localhost:8080/swagger-ui.html${NC}"
echo -e "  • ${BOLD}Automation Service${NC}   : ${CYAN}http://localhost:8000/docs${NC}"
echo -e "  • ${BOLD}Actuator Health${NC}      : ${CYAN}http://localhost:8080/actuator/health${NC}"

echo -e "\n${BOLD}Pre-Seeded Statutory Test Accounts (Password: ${GREEN}Password123!${NC}${BOLD}):${NC}"
echo -e "  ${CYAN}admin@aicte.gov.in${NC}        ${MUTED}→${NC} AICTE Central Administrator (Full Global Oversight)"
echo -e "  ${CYAN}regional@aicte.gov.in${NC}     ${MUTED}→${NC} AICTE Regional Officer (Northern Region / Scrutiny Queue)"
echo -e "  ${CYAN}officer@delhi-tech.edu${NC}    ${MUTED}→${NC} Technical Institution Representative (PID: 1-500123498)"
echo -e "  ${CYAN}compliance@aicte.gov.in${NC}   ${MUTED}→${NC} Compliance Officer (Continuous Surveillance / APH Scans)"
echo -e "  ${CYAN}auditor@aicte.gov.in${NC}      ${MUTED}→${NC} Regulatory Reviewer & Auditor (Immutable Audit Trail)"

echo -e "\n${BOLD}Logs:${NC}"
echo -e "  • Backend    : tail -f ${BACKEND_LOG}"
echo -e "  • Automation : tail -f ${AUTOMATION_LOG}"
echo -e "  • Frontend   : tail -f ${FRONTEND_LOG}"

echo -e "\n${SAFFRON}${BOLD}All services running in foreground. Press [Ctrl+C] to stop all processes.${NC}\n"

# Wait indefinitely for signal
wait
