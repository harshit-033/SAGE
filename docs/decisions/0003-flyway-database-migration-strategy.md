# ADR 0003: Version-Controlled Relational Database Migrations via Flyway

## Status
Accepted

## Context
Automated regulatory workflows require high data consistency, predictable schema evolution, and zero accidental schema loss. Hibernate auto-DDL (`hibernate.ddl-auto=update`) is hazardous in multi-developer and production environments.

## Decision
1. Flyway is configured as the sole authority for database DDL changes in Spring Boot.
2. Hibernate's `ddl-auto` is set to `validate` in production and staging environments.
3. Migrations follow the standard `V{version}__{description}.sql` naming pattern under `src/main/resources/db/migration/`.
4. Development seed data is segregated into a dedicated profile-gated migration or explicit runner.

## Consequences
- Guaranteed reproducibility across all developer workstations, CI/CD runners, and production clusters.
- Schema changes are peer-reviewed in pull requests alongside application code.
