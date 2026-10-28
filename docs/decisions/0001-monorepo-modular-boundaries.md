# ADR 0001: Monorepo Architecture with Strict Service Boundaries

## Status
Accepted

## Context
SIH Problem Statement 26262 requires an integrated system covering core regulatory workflows, document intelligence, automated inspection scheduling, compliance monitoring, and user-facing dashboards. Building completely decoupled repositories at this stage introduces version drift, complex local orchestration, and testing friction.

## Decision
We adopt a structured monorepo containing:
- `backend/`: Java 21 LTS + Spring Boot 3.4 (the authoritative business engine and data owner).
- `automation/`: Python 3.14 + FastAPI (the non-authoritative intelligence, worker, and algorithmic assistant).
- `frontend/`: React + TypeScript + Vite (the client-facing administrative and institutional portal).
- `infrastructure/`: Docker Compose configurations.
- `docs/`: Living architecture and decision documentation.
- `scripts/`: Unified developer workflow scripts.

## Consequences
- **Positive**: Single commit history, synchronized API contracts, unified developer tooling, straightforward local testing.
- **Negative**: Requires discipline to prevent accidental coupling between services; Python service must never access backend database tables directly.
