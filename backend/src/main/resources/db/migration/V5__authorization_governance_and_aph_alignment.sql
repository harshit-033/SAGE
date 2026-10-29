-- ==============================================================================
-- Flyway Migration V5: Authorization Governance, Tenancy Assignments & APH Alignment
-- Compatible with PostgreSQL and H2 (PostgreSQL compatibility mode)
-- ==============================================================================

-- 1. Explicit Jurisdiction Assignments for Regional Officers & Scope Authorities
CREATE TABLE IF NOT EXISTS jurisdiction_assignments (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    region VARCHAR(64) NOT NULL,
    state VARCHAR(100) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    valid_from TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    valid_until TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_jurisdiction_user ON jurisdiction_assignments(user_id, active);
CREATE INDEX IF NOT EXISTS idx_jurisdiction_state ON jurisdiction_assignments(state, active);

-- 2. Explicit Case Assignments for Application, Scrutiny, EVC, Compliance & Audit
CREATE TABLE IF NOT EXISTS case_assignments (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    institution_id UUID NOT NULL REFERENCES institutions(id) ON DELETE CASCADE,
    assignment_type VARCHAR(64) NOT NULL,
    case_reference VARCHAR(128),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    assigned_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    valid_until TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_case_assign_user_inst ON case_assignments(user_id, institution_id, active);

-- 3. Explicit Statutory Committee Appointments per APH 2024-27 Norms
-- Supports distinct committees: SCRUTINY_CH1, SCRUTINY_CH2, EVC, SHC, SAC, SCSC, EXECUTIVE_COMMITTEE, COUNCIL
CREATE TABLE IF NOT EXISTS committee_appointments (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    committee_type VARCHAR(64) NOT NULL,
    role_in_committee VARCHAR(64) NOT NULL,
    qualification_details VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    valid_from TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    valid_until TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_committee_appt_user ON committee_appointments(user_id, committee_type, active);

-- 4. Server-Side Token Revocations Table for Authoritative Session Invalidation
CREATE TABLE IF NOT EXISTS token_revocations (
    id UUID PRIMARY KEY,
    token_id VARCHAR(128) NOT NULL UNIQUE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    revoked_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    reason VARCHAR(255)
);

CREATE INDEX IF NOT EXISTS idx_token_revocations_tid ON token_revocations(token_id);
CREATE INDEX IF NOT EXISTS idx_token_revocations_exp ON token_revocations(expires_at);
