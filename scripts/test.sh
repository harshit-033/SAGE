#!/usr/bin/env bash
# ==============================================================================
# SAGE Test Suite Runner Script
# Usage: ./scripts/test.sh [backend|automation|frontend|all]
# ==============================================================================
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

run_backend_tests() {
  echo "=== Running Java Spring Boot Backend Tests ==="
  cd "${ROOT_DIR}/backend"
  ./mvnw clean test
}

run_automation_tests() {
  echo "=== Running Python Automation Service Tests ==="
  cd "${ROOT_DIR}/automation"
  if [ ! -d "venv" ]; then
    python3 -m venv venv
    ./venv/bin/pip install --upgrade pip
    ./venv/bin/pip install -r requirements.txt
  fi
  source venv/bin/activate
  pytest -v
}

run_frontend_tests() {
  echo "=== Running Frontend Vitest Suite ==="
  cd "${ROOT_DIR}/frontend"
  if [ ! -d "node_modules" ]; then
    npm install
  fi
  npm test -- --run
}

case "${1:-all}" in
  backend)
    run_backend_tests
    ;;
  automation)
    run_automation_tests
    ;;
  frontend)
    run_frontend_tests
    ;;
  all)
    echo "Executing all SAGE test suites..."
    run_backend_tests
    run_automation_tests
    run_frontend_tests
    echo "All test suites completed successfully!"
    ;;
  *)
    echo "Unknown test target: $1. Valid targets: backend, automation, frontend, all"
    exit 1
    ;;
esac
