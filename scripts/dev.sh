#!/usr/bin/env bash
# ==============================================================================
# SAGE Development Runner Script
# Usage: ./scripts/dev.sh [backend|automation|frontend|all|docker]
# ==============================================================================
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

show_help() {
  echo "SAGE Development Service Runner"
  echo "Usage: $0 [command]"
  echo ""
  echo "Commands:"
  echo "  backend     Start Spring Boot backend (port 8080)"
  echo "  automation  Start Python FastAPI automation service (port 8000)"
  echo "  frontend    Start React Vite development server (port 5173)"
  echo "  docker      Start all services via Docker Compose"
  echo "  help        Display this help message"
}

case "${1:-help}" in
  backend)
    echo "Starting SAGE Spring Boot Backend..."
    cd "${ROOT_DIR}/backend"
    ./mvnw spring-boot:run
    ;;
  automation)
    echo "Starting SAGE Python Automation Service..."
    cd "${ROOT_DIR}/automation"
    if [ ! -d "venv" ]; then
      echo "Virtual environment not found. Setting up venv..."
      python3 -m venv venv
      ./venv/bin/pip install --upgrade pip
      ./venv/bin/pip install -r requirements.txt
    fi
    source venv/bin/activate
    uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
    ;;
  frontend)
    echo "Starting SAGE React Frontend..."
    cd "${ROOT_DIR}/frontend"
    if [ ! -d "node_modules" ]; then
      echo "Installing node dependencies..."
      npm install
    fi
    npm run dev
    ;;
  docker)
    echo "Starting SAGE Stack via Docker Compose..."
    docker compose -f "${ROOT_DIR}/infrastructure/docker-compose.yml" up --build
    ;;
  *)
    show_help
    exit 1
    ;;
esac
