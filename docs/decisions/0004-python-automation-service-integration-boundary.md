# ADR 0004: Python Automation Service as Non-Authoritative Calculation Engine

## Status
Accepted

## Context
Complex automation components (document parsing, OCR, geospatial route optimization for EVC panels, machine learning) are best written in Python's rich data and AI ecosystem. However, allowing the Python service to mutate PostgreSQL workflow tables directly risks bypassing domain invariants, security guards, and audit trail generation implemented in Spring Boot.

## Decision
1. The Python FastAPI service is strictly non-authoritative regarding regulatory state.
2. The Python service never connects directly to PostgreSQL with write permissions for workflow tables.
3. The Python service exposes stateless computational endpoints (e.g. `POST /api/v1/scrutiny/evaluate`, `POST /api/v1/evc/optimize-routes`).
4. Consequential mutations must be committed by Spring Boot after validating the automation results.
5. In Phase 1, the Python service implements clean abstract interfaces (`Protocol` classes) without introducing premature message brokers (Celery/Kafka).

## Consequences
- Clean separation between transactional domain authority (Spring Boot) and stateless automation workloads (Python).
- Auditing and tenancy enforcement remain unified in one authoritative codebase.
