# SAGE API Contracts & Integration Protocols

## 1. Overview & Architectural Principles

SAGE implements RESTful micro-contracts adhering to:
- **RFC 7807 Problem Details** for consistent client error handling across Spring Boot and FastAPI.
- **W3C Distributed Tracing / X-Correlation-ID** across all service boundaries.
- **Fail-Closed Tenancy Validation** on all resource endpoints.
- **Explicit DTO Envelopes & Pagination** (`ApiResponse<T>` and `PageResponse<T>`).
- **OpenAPI 3.0 / Swagger** documentation automatically generated from source code (`/swagger-ui.html`).

---

## 2. Health & Readiness Contracts

Both backend (Spring Boot on `:8080`) and automation (FastAPI on `:8000`) expose standardized health contracts.

### Core Backend Health Endpoints (`http://localhost:8080`)
- `GET /api/v1/health`: Liveness probe (`200 OK`).
- `GET /api/v1/health/ready`: Readiness probe validating database connectivity and Flyway migration state (`200 OK`).

### Python Automation Health Endpoints (`http://localhost:8000`)
- `GET /api/v1/health`: Liveness probe (`200 OK`).
- `GET /api/v1/health/ready`: Readiness probe validating dependencies and backend reachability (`200 OK`).

---

## 3. Standard Error Format (RFC 7807 Problem Details)

Every non-2xx response from Spring Boot conforms to RFC 7807:

```json
{
  "type": "https://api.aicte.gov.in/errors/tenant-access-denied",
  "title": "Tenant Access Denied",
  "status": 403,
  "detail": "Cross-tenant access violation: Principal 'officer@delhi-tech.edu' is not authorized to access Institution ID: '33333333-3333-3333-3333-333333333333'",
  "instance": "/api/v1/institutions/33333333-3333-3333-3333-333333333333",
  "correlationId": "8b92d64f-46df-4998-8924-c117d3325c89"
}
```

---

## 4. Authentication API Endpoints (`/api/v1/auth`)

### `POST /api/v1/auth/login`
- **Access**: Public
- **Request Body**:
```json
{
  "username": "admin@aicte.gov.in",
  "password": "Password123!"
}
```
- **Success Response**: `200 OK`
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6...",
    "tokenType": "Bearer",
    "expiresInMs": 86400000,
    "user": {
      "id": "11111111-1111-1111-1111-111111111111",
      "username": "admin@aicte.gov.in",
      "email": "admin@aicte.gov.in",
      "fullName": "AICTE Central Administrator",
      "roles": ["ROLE_AICTE_ADMIN"],
      "permissions": ["INSTITUTION_READ", "INSTITUTION_WRITE", "USER_READ", "USER_WRITE", "AUDIT_EXPORT"],
      "institutions": []
    }
  }
}
```

### `GET /api/v1/auth/me`
- **Access**: Authenticated (`Bearer <token>`)
- **Success Response**: `200 OK` (Current principal profile with role and permission grants).

### `POST /api/v1/auth/logout`
- **Access**: Authenticated (`Bearer <token>`)
- **Operation**: Invalidates session context, clears security context, and records an `AUTH_LOGOUT` audit event.
- **Success Response**: `200 OK`

---

## 5. Institution Management API Endpoints (`/api/v1/institutions`)

### `GET /api/v1/institutions`
- **Access**: Authenticated (`ROLE_AICTE_ADMIN`, `ROLE_REGIONAL_OFFICER`, `ROLE_INSTITUTION_OFFICER`, `ROLE_AUDITOR`)
- **Parameters**: `page` (default 0), `size` (default 10), `query`, `category`, `state`, `active`, `sortBy`, `sortDir`
- **Tenancy Scoping**:
  - `ROLE_AICTE_ADMIN` / Regional / Auditor: Nationwide view.
  - `ROLE_INSTITUTION_OFFICER`: Automatically filtered to affiliated institutions.
- **Success Response**: `200 OK` (`PageResponse<InstitutionDto>`).

### `GET /api/v1/institutions/{id}`
- **Access**: Authenticated (`@tenancyEvaluator.hasAccessToInstitution`)
- **Tenancy Behavior**: Unauthorized cross-tenant attempts reject with `403 Forbidden` and log `TENANCY_VIOLATION_ATTEMPT`.
- **Success Response**: `200 OK` (`InstitutionDto` with full location, contacts, and coordinates).

### `POST /api/v1/institutions`
- **Access**: `ROLE_AICTE_ADMIN`
- **Request Body**: `code`, `name`, `applicationType`, `state`, `district`.
- **Success Response**: `201 Created` (`INSTITUTION_CREATED` audit event logged).

### `PUT /api/v1/institutions/{id}`
- **Access**: `@tenancyEvaluator.canUpdateInstitution(authentication, #id)`
  - Admin can update any institution.
  - Institution Officer can update ONLY their own institution and must possess `INSTITUTION_WRITE` permission.
- **Request Body**: `name`, `category`, `addressLine1`, `addressLine2`, `city`, `district`, `state`, `pincode`, `officialEmail`, `officialPhone`, `alternatePhone`, `websiteUrl`, `establishmentYear`, `latitude`, `longitude`.
- **Success Response**: `200 OK` (`INSTITUTION_UPDATED` audit event logged).

### `PATCH /api/v1/institutions/{id}/status`
- **Access**: `hasAuthority('INSTITUTION_ACTIVATE') or hasRole('ROLE_AICTE_ADMIN')`
- **Request Body**: `{"active": false, "reason": "Statutory audit pending"}`
- **Success Response**: `200 OK` (`INSTITUTION_STATUS_CHANGED` audit event logged).

### `GET /api/v1/institutions/{id}/users`
- **Access**: Authorized institutional access
- **Success Response**: `200 OK` (List of affiliated officers, roles, and signatory status).

### `POST /api/v1/institutions/{id}/users`
- **Access**: Authorized institution manager (`@tenancyEvaluator.canUpdateInstitution`)
- **Request Body**: `{"userId": "UUID", "roleInInstitution": "NODAL_OFFICER", "primaryContact": true}`
- **Success Response**: `201 Created` (`INSTITUTION_USER_AFFILIATED` audit event logged).

### `DELETE /api/v1/institutions/{id}/users/{userId}`
- **Access**: Authorized institution manager
- **Success Response**: `200 OK` (`INSTITUTION_USER_REMOVED` audit event logged).

---

## 6. Administrative User Management API Endpoints (`/api/v1/users`)

### `GET /api/v1/users`
- **Access**: `USER_READ` or `ROLE_AICTE_ADMIN`
- **Parameters**: `page`, `size`, `query`, `active`, `sortBy`, `sortDir`
- **Success Response**: `200 OK` (`PageResponse<UserDto>`).

### `POST /api/v1/users`
- **Access**: `USER_WRITE` or `ROLE_AICTE_ADMIN`
- **Request Body**: `username`, `email`, `fullName`, `password`, `roles` (array), `institutionId` (optional).
- **Security**: Cryptographically hashes password with BCrypt; enforces unique username & email.
- **Success Response**: `201 Created` (`USER_CREATED` audit event logged).

### `PUT /api/v1/users/{id}`
- **Access**: `USER_WRITE` or `ROLE_AICTE_ADMIN`
- **Request Body**: `fullName`, `email`.
- **Success Response**: `200 OK`.

### `PATCH /api/v1/users/{id}/status`
- **Access**: `USER_ACTIVATE` or `ROLE_AICTE_ADMIN`
- **Request Body**: `{"active": false, "reason": "Offboarding"}`
- **Constraint**: Administrators cannot suspend their own active session account.
- **Success Response**: `200 OK` (`USER_STATUS_CHANGED` audit event logged).

### `PUT /api/v1/users/{id}/roles`
- **Access**: `ROLE_ASSIGN` or `ROLE_AICTE_ADMIN`
- **Request Body**: `{"roles": ["ROLE_SCRUTINY_OFFICER", "ROLE_EVC_MEMBER"]}`
- **Success Response**: `200 OK` (`ROLE_ASSIGNED` audit event logged).

### `GET /api/v1/users/roles`
- **Access**: Authenticated
- **Success Response**: `200 OK` (List of available system roles).

---

## 7. Regulatory Audit Log Inspection Endpoints (`/api/v1/audit`)

### `GET /api/v1/audit`
- **Access**: `ROLE_AICTE_ADMIN`, `ROLE_AUDITOR`, or `AUDIT_READ`
- **Parameters**: `page`, `size`, `principal`, `eventType`, `resourceType`, `correlationId`, `sortBy`, `sortDir`
- **Success Response**: `200 OK` (`PageResponse<AuditEventDto>`).
