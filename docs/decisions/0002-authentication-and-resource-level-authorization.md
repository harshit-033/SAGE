# ADR 0002: Stateless JWT Authentication with Resource-Level Tenancy Guard

## Status
Accepted

## Context
AICTE manages thousands of technical institutes across India. Institutions must have access strictly to their own applications, deficiencies, and compliance submissions. Traditional role checks (e.g. `hasRole('INSTITUTION_OFFICER')`) allow any authenticated college officer to access another college's confidential files if only endpoint URLs are checked (BOLA / IDOR vulnerability).

## Decision
1. Use **Stateless JWT Tokens** signed using HMAC-SHA256 containing user ID, username, assigned roles, and bound institution IDs.
2. Implement **Two-Layer Authorization**:
   - Layer 1: Spring Security `@PreAuthorize` checks broad role privileges.
   - Layer 2: A custom `TenancyAuthorizationEvaluator` enforces that if the requested resource belongs to an institution, the authenticated principal must possess an explicit affiliation to that institution (or hold `ROLE_AICTE_ADMIN`).
3. If an unauthorized tenant access attempt occurs:
   - Request is immediately rejected with HTTP `403 Forbidden` (`ProblemDetails`).
   - A security violation audit event is recorded to `audit_events`.

## Consequences
- Prevents cross-institution data leakage at the architectural layer.
- Eliminates reliance on frontend client logic for tenant isolation.
