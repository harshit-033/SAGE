#!/usr/bin/env bash
# ==============================================================================
# SAGE — Core Backend Runner (Spring Boot 3.4.3 / Java 21)
# Smart AICTE Governance Engine (SIH Problem Statement 26262)
# ==============================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"

# ANSI Terminal Colors
BOLD='\033[1m'
BLUE='\033[38;5;33m'
CYAN='\033[38;5;51m'
GREEN='\033[38;5;42m'
YELLOW='\033[38;5;214m'
RED='\033[38;5;196m'
NC='\033[0m'

PROFILE="dev"
PORT="8080"
DEBUG_MODE=false

# Parse optional command-line flags
while [[ $# -gt 0 ]]; do
  case "$1" in
    --profile|-p)
      PROFILE="$2"
      shift 2
      ;;
    --port)
      PORT="$2"
      shift 2
      ;;
    --debug|-d)
      DEBUG_MODE=true
      shift
      ;;
    --help|-h)
      echo -e "${BOLD}SAGE Spring Boot Backend Runner${NC}"
      echo -e "Usage: ./run.sh [options]"
      echo ""
      echo "Options:"
      echo "  --profile, -p <name>   Spring profile to activate (default: dev)"
      echo "  --port <port>          Server HTTP port (default: 8080)"
      echo "  --debug, -d            Enable verbose debug logging"
      echo "  --help, -h             Display this help message"
      exit 0
      ;;
    *)
      echo -e "${RED}Unknown option: $1${NC}. Use --help for usage."
      exit 1
      ;;
  esac
done

cd "${SCRIPT_DIR}"

echo -e "${BLUE}${BOLD}======================================================================${NC}"
echo -e "${BLUE}${BOLD}             SAGE — Smart AICTE Governance Engine                    ${NC}"
echo -e "${CYAN}             Spring Boot Core API Service Runner                    ${NC}"
echo -e "${BLUE}${BOLD}======================================================================${NC}"

# 1. Environment & Prerequisite Checks
echo -e "\n${BOLD}[1/3] Verifying runtime prerequisites...${NC}"

if ! command -v java >/dev/null 2>&1; then
  echo -e "${RED}[ERROR] Java runtime not found. Java 21 is required.${NC}"
  exit 1
fi

JAVA_VER="$(java -version 2>&1 | head -n 1 | awk -F '"' '{print $2}')"
echo -e "  ${GREEN}✔${NC} Java Runtime detected: ${BOLD}${JAVA_VER}${NC}"

# Ensure Maven wrapper is executable
if [ ! -x "./mvnw" ]; then
  chmod +x ./mvnw
  echo -e "  ${GREEN}✔${NC} Granted execute permissions to ./mvnw"
fi

# Load root .env if present
if [ -f "${ROOT_DIR}/.env" ]; then
  echo -e "  ${GREEN}✔${NC} Loading environment overrides from root .env"
  # shellcheck disable=SC1091
  set -a && source "${ROOT_DIR}/.env" && set +a
elif [ -f "${SCRIPT_DIR}/.env" ]; then
  echo -e "  ${GREEN}✔${NC} Loading environment overrides from backend/.env"
  # shellcheck disable=SC1091
  set -a && source "${SCRIPT_DIR}/.env" && set +a
fi

# 2. Configuration Summary
echo -e "\n${BOLD}[2/3] Configuring Spring Boot Runtime...${NC}"
echo -e "  Active Profile : ${CYAN}${PROFILE}${NC}"
echo -e "  Server Port    : ${CYAN}${PORT}${NC}"
echo -e "  Debug Mode     : ${CYAN}${DEBUG_MODE}${NC}"

SPRING_ARGS=(
  "-Dspring-boot.run.profiles=${PROFILE}"
  "-Dspring-boot.run.arguments=--server.port=${PORT}"
)

if [ "${DEBUG_MODE}" = true ]; then
  SPRING_ARGS+=("-Dspring-boot.run.jvmArguments=-Dlogging.level.org.aicte.sage=DEBUG")
fi

# 3. Execution
echo -e "\n${BOLD}[3/3] Starting SAGE Spring Boot application...${NC}"
echo -e "  Endpoints:"
echo -e "    • Health Check : ${GREEN}http://localhost:${PORT}/actuator/health${NC}"
echo -e "    • Swagger Docs : ${GREEN}http://localhost:${PORT}/swagger-ui.html${NC}"
echo -e "    • OpenAPI JSON : ${GREEN}http://localhost:${PORT}/v3/api-docs${NC}"
echo -e "\n${YELLOW}Press Ctrl+C to shut down the backend cleanly.${NC}\n"

exec ./mvnw spring-boot:run "${SPRING_ARGS[@]}"
