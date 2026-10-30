# Temporary Development Dashboard Cleanup Guide

This document provides a complete inventory of the temporary files created for the **EVC Scheduling Engine Developer Dashboard**, explains how to safely remove them before production deployment, and confirms the integrity of all permanent components.

---

## 1. Background and Purpose

The interactive developer dashboard was introduced to allow developers and stakeholders to test and visualize the Google OR-Tools CP-SAT visiting committee scheduler (`EvcCpSatScheduler`) interactively with dynamic durations, committee sizes, and conflict scenarios without waiting for Phase 4 production REST API development.

All dashboard endpoints, web configurations, and static assets are strictly isolated under the Spring profile:
```properties
spring.profiles.active=dev-dashboard
```
When this profile is not active (e.g. in `default` or `production` profiles), the endpoints are not exposed and `DevDashboardIsolationFilter` blocks any request to `/dev-dashboard/**` or `/dev/dashboard/**` with HTTP 404.

Before production release and integration with the SAGE platform, this temporary module can be completely deleted.

---

## 2. Complete Inventory of Temporary Files

### Java Backend Sources (`src/main/java/in/gov/aicte/evc/devdashboard/`)
- `src/main/java/in/gov/aicte/evc/devdashboard/DevDashboardController.java` — REST controller with solver invocation and 5 demo scenarios.
- `src/main/java/in/gov/aicte/evc/devdashboard/DevDashboardIsolationFilter.java` — Servlet filter enforcing HTTP 404 blocking outside `dev-dashboard` profile.
- `src/main/java/in/gov/aicte/evc/devdashboard/DevDashboardWebConfig.java` — WebMvcConfigurer forwarding `/dev-dashboard/` to `index.html`.
- `src/main/java/in/gov/aicte/evc/devdashboard/dto/DashboardInspectionDto.java` — Inspection payload DTO.
- `src/main/java/in/gov/aicte/evc/devdashboard/dto/DashboardExpertAvailabilityDto.java` — Expert availability DTO.
- `src/main/java/in/gov/aicte/evc/devdashboard/dto/DashboardScheduleRequest.java` — Solver request DTO.
- `src/main/java/in/gov/aicte/evc/devdashboard/dto/DashboardScheduledInspectionDto.java` — Solved inspection outcome DTO.
- `src/main/java/in/gov/aicte/evc/devdashboard/dto/DashboardScheduleResponse.java` — Solver response DTO with metrics.
- `src/main/java/in/gov/aicte/evc/devdashboard/dto/DemoScenarioDto.java` — Scenario metadata and preset request DTO.

### Frontend Static Assets (`src/main/resources/static/dev-dashboard/`)
- `src/main/resources/static/dev-dashboard/index.html` — Semantic HTML5 dashboard interface with responsive grid.
- `src/main/resources/static/dev-dashboard/app.css` — Custom dark-themed CSS3 styling (cards, tables, Gantt timeline).
- `src/main/resources/static/dev-dashboard/app.js` — Vanilla JavaScript client (scenario loader, input validation, CP-SAT API client, Gantt renderer).

### Test Suite (`src/test/java/in/gov/aicte/evc/devdashboard/`)
- `src/test/java/in/gov/aicte/evc/devdashboard/DevDashboardControllerTests.java` — Automated tests verifying profile isolation (active vs inactive), scenario listing, solver execution, infeasibility detection, and input validation.

### Documentation (`docs/`)
- `docs/temporary-dashboard-cleanup.md` — This cleanup guide.

---

## 3. Permanent Components (DO NOT DELETE)

The following components represent permanent core functionality and **must remain untouched**:
- **OR-Tools CP-SAT Solver**:
  - `src/main/java/in/gov/aicte/evc/scheduler/EvcCpSatScheduler.java`
  - `src/main/java/in/gov/aicte/evc/scheduler/model/*` (`InspectionRequest`, `ExpertAvailability`, `SchedulingProblem`, `ScheduledInspection`, `SchedulingStatus`, `SchedulingResult`)
  - `src/test/java/in/gov/aicte/evc/scheduler/EvcCpSatSchedulerTests.java`
  - `src/test/java/in/gov/aicte/evc/scheduler/EvcPrototypeRunner.java`
- **Domain Model & Persistence**:
  - `src/main/java/in/gov/aicte/evc/entity/*` (`Institution`, `Expert`, `EvcInspection`, `CommitteeAssignment`, `InspectionStatus`, `AssignmentRole`, `AssignmentStatus`)
  - `src/main/java/in/gov/aicte/evc/repository/*` (`InstitutionRepository`, `ExpertRepository`, `EvcInspectionRepository`, `CommitteeAssignmentRepository`)
  - `src/main/resources/db/migration/V1__create_evc_schema.sql`
  - `src/test/java/in/gov/aicte/evc/DomainModelPersistenceTests.java`
  - `src/test/java/in/gov/aicte/evc/InspectionValidationTests.java`
- **Core Configuration**:
  - `pom.xml`
  - `src/main/resources/application.properties`

---

## 4. Deletion Instructions

To completely remove the temporary development dashboard from the project, run the following single command from the project root:

```bash
rm -rf \
  src/main/java/in/gov/aicte/evc/devdashboard \
  src/main/resources/static/dev-dashboard \
  src/test/java/in/gov/aicte/evc/devdashboard \
  docs/temporary-dashboard-cleanup.md
```

### Post-Removal Verification

After executing the removal command, verify that the core application compiles and all permanent unit and integration tests continue to pass:

```bash
./mvnw clean test
```

Expected result:
- Tests run: 24 (including `DomainModelPersistenceTests`, `InspectionValidationTests`, `EvcCpSatSchedulerTests`, and `EvcSchedulingEngineApplicationTests`).
- Failures: 0, Errors: 0, Skipped: 0.
- `BUILD SUCCESS`.

