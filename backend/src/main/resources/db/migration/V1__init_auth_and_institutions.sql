-- ==============================================================================
-- Flyway Migration V1: Initial Schema for Auth, Tenancy, and Audit
-- Supports PostgreSQL and H2 (in PostgreSQL compatibility mode)
-- ==============================================================================

-- 1. Roles Table
CREATE TABLE roles (
    id UUID PRIMARY KEY,
    code VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(128) NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 2. Permissions Table
CREATE TABLE permissions (
    id UUID PRIMARY KEY,
    code VARCHAR(64) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 3. Role-Permissions Join Table
CREATE TABLE role_permissions (
    role_id UUID NOT NULL,
    permission_id UUID NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_role_permissions_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE,
    CONSTRAINT fk_role_permissions_permission FOREIGN KEY (permission_id) REFERENCES permissions (id) ON DELETE CASCADE
);

-- 4. Institutions Table (Multi-tenant partition)
CREATE TABLE institutions (
    id UUID PRIMARY KEY,
    code VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    application_type VARCHAR(64) NOT NULL,
    state VARCHAR(100) NOT NULL,
    district VARCHAR(100) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_institutions_state ON institutions(state);
CREATE INDEX idx_institutions_status ON institutions(status);

-- 5. Users Table
CREATE TABLE users (
    id UUID PRIMARY KEY,
    username VARCHAR(128) NOT NULL UNIQUE,
    email VARCHAR(128) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);

-- 6. User-Roles Join Table
CREATE TABLE user_roles (
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
);

-- 7. Institution-User Affiliations (Resource-level Tenancy Binding)
CREATE TABLE institution_users (
    id UUID PRIMARY KEY,
    institution_id UUID NOT NULL,
    user_id UUID NOT NULL,
    role_in_institution VARCHAR(64) NOT NULL,
    is_primary_contact BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_inst_users_institution FOREIGN KEY (institution_id) REFERENCES institutions (id) ON DELETE CASCADE,
    CONSTRAINT fk_inst_users_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uq_institution_user UNIQUE (institution_id, user_id)
);

CREATE INDEX idx_inst_users_inst ON institution_users(institution_id);
CREATE INDEX idx_inst_users_user ON institution_users(user_id);

-- 8. Immutable Audit Events Table
CREATE TABLE audit_events (
    id UUID PRIMARY KEY,
    event_type VARCHAR(64) NOT NULL,
    principal VARCHAR(128) NOT NULL,
    institution_id UUID,
    resource_type VARCHAR(64) NOT NULL,
    resource_id VARCHAR(128) NOT NULL,
    action VARCHAR(32) NOT NULL,
    client_ip VARCHAR(64),
    correlation_id VARCHAR(64),
    details TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_events_institution FOREIGN KEY (institution_id) REFERENCES institutions (id) ON DELETE SET NULL
);

CREATE INDEX idx_audit_events_inst ON audit_events(institution_id, created_at DESC);
CREATE INDEX idx_audit_events_principal ON audit_events(principal, created_at DESC);
CREATE INDEX idx_audit_events_correlation ON audit_events(correlation_id);
