# EVC Scheduling Engine

The **EVC (Expert Visiting Committee) Scheduling Engine** is a high-performance standalone microservice built for the **SAGE** (System for Approval Governance and Evaluation) platform, developed for the AICTE (All India Council for Technical Education) SIH project.

It provides mathematical constraint optimization using Google OR-Tools CP-SAT to automatically assign qualified experts to institutional inspection visits, enforcing spatio-temporal disjunctions, multi-disciplinary requirements, dynamic committee sizes, dynamic durations, Sunday exclusion, and conflict-of-interest (COI) isolation.

---

## 1. Architectural Role in the SAGE Ecosystem

The EVC Scheduling Engine operates as an autonomous microservice alongside the primary SAGE backend:

```
┌──────────────────────────────────────────────────────────┐
│                   SAGE Backend (Port 8080)               │
│               AICTE Approval Governance Engine            │
└────────────┬─────────────────────────────────┬───────────┘
             │ 1. Synchronous REST             │ 
             │    (POST /api/v1/schedules)     │ Shared PostgreSQL
             │    (GET /api/v1/schedules/{id}) │ Server (Port 5432)
             ▼                                 │
┌───────────────────────────────┐              │
│    EVC Scheduling Engine      │              │
│         (Port 8081)           │              │
│  - Google OR-Tools CP-SAT     │              │
│  - Sunday-Aware Math Engine   │              │
│  - Dedicated Schema: 'evc'    │◄─────────────┘
└───────────────────────────────┘
```

- **Runtime Port:** Defaults to port **`8081`** (avoiding conflict with SAGE on `8080`). Includes automatic port-scan fallback if 8081 is occupied.
- **Database Isolation:** Connects to the shared PostgreSQL instance (`aicte_platform_db`) on port `5432` but executes strictly inside a dedicated database schema namespace: **`evc`**. Flyway migrations and Hibernate DDL operate exclusively within `evc`, ensuring zero interference with SAGE-owned tables in `public`.
- **Stateless REST Communication:** Pure synchronous JSON HTTP contracts authenticated via service-to-service API key or Bearer token.

---

## 2. Technology Stack

| Component | Technology | Version |
|---|---|---|
| **Language** | Java (OpenJDK LTS) | `21` |
| **Framework** | Spring Boot | `4.0.9` |
| **Core Framework** | Spring Framework | `7.0.10` |
| **Security** | Spring Security (Stateless Service Auth) | `7.0.10` |
| **Optimization Solver** | Google OR-Tools CP-SAT (Java Native) | `9.11.4210` |
| **API Documentation** | Springdoc OpenAPI UI / Swagger 3 | `2.8.5` |
| **Web / REST** | Spring WebMVC, Tomcat Embedded, Jackson | `Tomcat 11.0.27`, `Jackson 3.1.7` |
| **Persistence** | Spring Data JPA, Hibernate ORM | `Hibernate 7.2.25.Final` |
| **Database Migrations**| Flyway Core & Flyway Database PostgreSQL | `11.3.4` |
| **Validation** | Jakarta Bean Validation, Hibernate Validator | `Hibernate Validator 9.0.1.Final` |
| **Production Database** | PostgreSQL JDBC Driver | `42.7.14` |
| **Test Database** | H2 In-Memory Database Engine (PG Mode) | `2.4.240` |
| **Testing** | JUnit Jupiter, AssertJ, Mockito, Spring Security Test | `JUnit 6.0.3`, `AssertJ 3.27.7`, `Mockito 5.20.0` |
| **Build Tool** | Apache Maven (via Maven Wrapper) | `3.9.x` |

---

## 3. Production REST API Endpoints

All scheduling endpoints are grouped under `/api/v1` and documented interactively via OpenAPI / Swagger.

| Method | Endpoint | Access | Description | Status Codes |
|---|---|---|---|---|
| `POST` | `/api/v1/schedules` | Secured (SAGE) | Submits a batch scheduling request. Solves and transactionally persists assignments. | `200`, `400`, `401`, `403`, `409`, `422`, `500` |
| `GET` | `/api/v1/schedules/{id}` | Secured (SAGE) | Retrieves persisted schedule and assigned committee for an inspection (read-only; does not trigger solver). | `200`, `401`, `403`, `404` |
| `GET` | `/api/v1/health` | Public | Operational monitoring probe returning service health and metadata. | `200` |
| `GET` | `/v3/api-docs` | Public | Raw OpenAPI 3 specification JSON. | `200` |
| `GET` | `/swagger-ui.html` | Public | Interactive Swagger UI documentation browser. | `200` |

---

## 4. API Request & Response Contracts

### A. Submit Batch Schedule (`POST /api/v1/schedules`)

**Headers:**
```http
Content-Type: application/json
X-API-KEY: your-service-api-key
```
*(Alternatively: `Authorization: Bearer your-service-api-key`)*

**Request Body:**
```json
{
  "inspectionIds": [101, 102],
  "allowRescheduling": false,
  "timeLimitSeconds": 15,
  "eligibleExpertOverrides": {
    "101": [501, 502, 503, 504],
    "102": [502, 503, 505, 506]
  },
  "requestId": "sage-batch-run-2026-001"
}
```

**Success Response (`200 OK`):**
```json
{
  "requestId": "sage-batch-run-2026-001",
  "status": "OPTIMAL",
  "success": true,
  "persisted": true,
  "explanation": "Schedule solved and transactionally persisted",
  "totalInspectionsRequested": 2,
  "totalInspectionsScheduled": 2,
  "solveDurationMs": 28,
  "scheduledInspections": [
    {
      "inspectionId": 101,
      "institutionId": 12,
      "institutionName": "Government Engineering College, Bhopal",
      "scheduledStartDate": "2026-11-05",
      "scheduledEndDate": "2026-11-07",
      "durationInDays": 3,
      "requiredMemberCount": 3,
      "actualInspectionDays": [
        "2026-11-05",
        "2026-11-06",
        "2026-11-07"
      ],
      "assignedExperts": [
        {
          "expertId": 501,
          "expertName": "Dr. A. Sharma",
          "discipline": "Computer Science",
          "role": "CHAIRPERSON",
          "status": "CONFIRMED"
        },
        {
          "expertId": 502,
          "expertName": "Dr. B. Verma",
          "discipline": "Computer Science",
          "role": "MEMBER",
          "status": "CONFIRMED"
        },
        {
          "expertId": 503,
          "expertName": "Dr. C. Patel",
          "discipline": "Information Technology",
          "role": "MEMBER",
          "status": "CONFIRMED"
        }
      ]
    }
  ]
}
```

### B. Sunday Exclusion Semantics

Per AICTE regulatory guidelines (Phase 4A), **Sunday is strictly excluded from inspection activities**.
- `durationInDays` defines the count of *active non-Sunday inspection days*.
- If an inspection starts on Friday with `durationInDays = 3`:
  - Active inspection days: **Friday**, **Saturday**, and **Monday** (Sunday is skipped).
  - Calculated inclusive end date: **Monday**.
  - `actualInspectionDays` in the JSON response will explicitly enumerate `[2026-11-06, 2026-11-07, 2026-11-09]`.

### C. Error Status Code Mapping

| Status Code | Scenario | Payload Details |
|---|---|---|
| `400 Bad Request` | Validation failure (e.g., empty `inspectionIds`, negative timeout) | `ApiErrorResponse` with field-level constraint violation details |
| `401 Unauthorized` | Missing authentication credentials | Structured JSON: `"Missing service authentication credentials"` |
| `403 Forbidden` | Invalid API key provided | Structured JSON: `"Invalid API key provided for EVC Scheduling Engine"` |
| `409 Conflict` | Rescheduling rejection: inspection is already `SCHEDULED` and `allowRescheduling: false` | `ScheduleApiResponse` detailing the conflict without altering DB state |
| `422 Unprocessable Entity` | Mathematical infeasibility (insufficient expert pool, availability mismatch, or timeout) | `ScheduleApiResponse` with `status: INFEASIBLE` and diagnostic explanation; rollback guaranteed |
| `404 Not Found` | Inspection ID not found or not yet scheduled | `ApiErrorResponse` with descriptive message |
| `500 Internal Server Error` | Database connection or unhandled runtime failure | `ApiErrorResponse` |

---

## 5. Security & Authentication Configuration

Production calls from SAGE are authenticated statelessly using a shared secret.

### Credentials
- Configured via environment variable `EVC_API_KEY`.
- Fallback default (development only): `evc-dev-secret-key-2026`.

### Supported Authentication Headers
Clients may supply the key in either of two standard headers:
1. `X-API-KEY: <EVC_API_KEY>`
2. `Authorization: Bearer <EVC_API_KEY>`

Public endpoints bypassing authentication:
- `/api/v1/health`
- `/v3/api-docs/**`
- `/swagger-ui/**`, `/swagger-ui.html`
- `/dev-dashboard/**`, `/dev/dashboard/api/**` (when dev-dashboard profile is active)

---

## 6. Shared Database & Schema Isolation

The EVC Scheduling Engine connects to the PostgreSQL database shared with SAGE, utilizing strict schema-level isolation:

```
aicte_platform_db (PostgreSQL Port 5432)
├── public (Owned by SAGE)
│   ├── applications
│   ├── evc_visits
│   └── users
└── evc (Isolated namespace owned by EVC Engine)
    ├── institutions
    ├── experts
    ├── expert_availabilities
    ├── evc_inspections
    ├── committee_assignments
    └── flyway_schema_history
```

### PostgreSQL Connection Settings
```properties
spring.datasource.url=jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:aicte_platform_db}?currentSchema=evc
spring.flyway.schemas=evc
spring.flyway.default-schema=evc
spring.flyway.create-schemas=true
spring.jpa.properties.hibernate.default_schema=evc
spring.jpa.hibernate.ddl-auto=validate
```

---

## 7. Dynamic Port Selection & Fail-Safe Auto-Scan

The engine defaults to HTTP port **`8081`** to avoid port collisions with the main SAGE application (port `8080`).

### Startup Port Check
During startup in `EvcSchedulingEngineApplication`:
1. The configured port (`SERVER_PORT` or `8081`) is checked for availability via socket binding.
2. If available, the engine starts normally.
3. If port 8081 is occupied, the engine automatically scans sequential ports (`8082`, `8083`, ...) up to a 50-port range, selects the first available open port, logs an explicit warning, and binds to that port.
4. An application event listener logs the exact bound HTTP port and operational URLs.

---

## 8. Environment Variables Reference

Create a `.env` file in the project root or provide the following environment variables:

| Variable | Description | Default Value | Production Example |
|---|---|---|---|
| `SERVER_PORT` | HTTP port for the scheduling engine | `8081` | `8081` |
| `DB_HOST` | PostgreSQL hostname / host address | `localhost` | `db.internal.aicte.gov.in` |
| `DB_PORT` | PostgreSQL port | `5432` | `5432` |
| `DB_NAME` | PostgreSQL database name (shared with SAGE) | `aicte_platform_db` | `aicte_platform_prod` |
| `DB_USERNAME` | PostgreSQL database user | `postgres` | `aicte_app_user` |
| `DB_PASSWORD` | PostgreSQL database password | `postgres` | `(secure-password)` |
| `DB_SCHEMA` | Dedicated schema namespace for EVC engine | `evc` | `evc` |
| `EVC_API_KEY` | Shared secret for service-to-service auth | `evc-dev-secret-key-2026` | `(high-entropy-64-char-token)` |
| `SPRING_PROFILES_ACTIVE` | Active Spring profile(s) | `(none)` | `prod` (or `dev-dashboard`) |

Refer to [`.env.example`](file:///.env.example) for a pre-formatted template.

---

## 9. Build, Test & Run Instructions

The project uses the Maven Wrapper (`mvnw`), ensuring consistent builds across any Linux or Windows development environment.

### Compile Project
```bash
./mvnw clean compile
```

### Run Automated Tests (78 passing tests)
```bash
./mvnw test
```
*Tests automatically execute against an isolated in-memory test database with `evc` PostgreSQL schema emulation.*

### Package Executable JAR
```bash
./mvnw clean package
```
Generates `target/evc-scheduling-engine-0.0.1-SNAPSHOT.jar`.

### Run Standalone Engine
```bash
./mvnw spring-boot:run
```
Or run the packaged JAR:
```bash
java -jar target/evc-scheduling-engine-0.0.1-SNAPSHOT.jar
```

### Launch Temporary Development Dashboard
To visually experiment with test scenarios, start the engine with the `dev-dashboard` profile:
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev-dashboard
```
Then navigate in your browser to:
```
http://localhost:8081/dev-dashboard/index.html
```

---

## 10. Implementation Roadmap & Status

- [x] **Phase 1: Project Foundation & Dependency Verification**
  - Audited project structure, Maven POM, and Java 21 LTS runtime compatibility.
  - Verified Spring Boot 4 modular starters and zero-infrastructure testing.
- [x] **Phase 2: Domain Model & Persistence Foundation**
  - Designed core JPA domain model: `Institution`, `Expert`, `EvcInspection`, `CommitteeAssignment`.
  - Implemented dynamic duration and dynamic committee sizes per inspection.
  - Integrated Flyway database migrations (`V1__create_evc_schema.sql`) with schema validation (`ddl-auto=validate`).
- [x] **Phase 3: OR-Tools CP-SAT Integration & Scheduling Prototype**
  - Integrated official Google OR-Tools CP-SAT (`ortools-java:9.11.4210`) with verified Java 21 native bindings.
  - Implemented mathematical constraint solver: consecutive calendar intervals, non-overlapping assignments, pool eligibility.
- [x] **Phase 4: Scheduling Service & Persistence Integration**
  - Implemented transactional persistence service (`SchedulePersistenceService`).
  - Added rollback safety, rescheduling controls, and pre-solve infeasibility diagnostics.
- [x] **Phase 4A: Sunday Exclusion from Inspection Activities**
  - Established Sunday as a mandatory non-inspection day.
  - Dynamically calculated inclusive visit dates and multi-day spans across weekends.
- [x] **Phase 5: Production REST API & SAGE Integration**
  - Implemented `POST /api/v1/schedules` for batch scheduling and transactional persistence.
  - Implemented `GET /api/v1/schedules/{id}` for read-only schedule retrieval.
  - Configured shared PostgreSQL port 5432 with strict `evc` schema namespace isolation.
  - Implemented runtime port configuration (default 8081) with automated port-scan fallback.
  - Implemented service-to-service authentication via `X-API-KEY` and `Authorization: Bearer`.
  - Generated live OpenAPI 3 / Swagger UI documentation at `/swagger-ui.html`.
  - Added public operational health probe at `/api/v1/health`.
  - Full automated test suite passing (78 tests, 0 failures, 0 errors).
