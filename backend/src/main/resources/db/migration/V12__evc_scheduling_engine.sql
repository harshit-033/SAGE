-- ==============================================================================
-- Flyway Migration V12: Expert Visit Committee (EVC) Scheduling Engine Schema
-- AICTE APH 2024-27 Clause 16.5 & Chapter I/II Physical / Virtual Inspections
-- Compatible with PostgreSQL and H2 (PostgreSQL compatibility mode)
-- ==============================================================================

-- 1. Evaluator / Peer Expert Registry
CREATE TABLE IF NOT EXISTS experts (
    id UUID PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    email VARCHAR(150),
    discipline VARCHAR(100) NOT NULL,
    designation VARCHAR(150),
    affiliated_institution_id UUID REFERENCES institutions(id) ON DELETE SET NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_experts_discipline ON experts(discipline);
CREATE INDEX IF NOT EXISTS idx_experts_active ON experts(active);

-- 2. Expert Calendar Availability
CREATE TABLE IF NOT EXISTS expert_availabilities (
    id UUID PRIMARY KEY,
    expert_id UUID NOT NULL REFERENCES experts(id) ON DELETE CASCADE,
    available_date DATE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_expert_available_date UNIQUE (expert_id, available_date)
);

CREATE INDEX IF NOT EXISTS idx_expert_availabilities_expert ON expert_availabilities(expert_id);
CREATE INDEX IF NOT EXISTS idx_expert_availabilities_date ON expert_availabilities(available_date);
CREATE INDEX IF NOT EXISTS idx_expert_availabilities_expert_date ON expert_availabilities(expert_id, available_date);

-- 3. EVC Inspections (Attached to canonical SAGE Institution & Approval Application)
CREATE TABLE IF NOT EXISTS evc_inspections (
    id UUID PRIMARY KEY,
    application_id UUID REFERENCES applications(id) ON DELETE CASCADE,
    institution_id UUID NOT NULL REFERENCES institutions(id) ON DELETE RESTRICT,
    duration_in_days INTEGER NOT NULL,
    required_member_count INTEGER NOT NULL,
    earliest_start_date DATE NOT NULL,
    latest_start_date DATE NOT NULL,
    scheduled_start_date DATE,
    scheduled_end_date DATE,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    special_instructions VARCHAR(500),
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_inspections_duration CHECK (duration_in_days > 0),
    CONSTRAINT chk_inspections_member_count CHECK (required_member_count > 0),
    CONSTRAINT chk_inspections_date_window CHECK (earliest_start_date <= latest_start_date),
    CONSTRAINT chk_inspections_status CHECK (status IN ('PENDING', 'SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'))
);

CREATE INDEX IF NOT EXISTS idx_evc_inspections_institution ON evc_inspections(institution_id);
CREATE INDEX IF NOT EXISTS idx_evc_inspections_application ON evc_inspections(application_id);
CREATE INDEX IF NOT EXISTS idx_evc_inspections_status ON evc_inspections(status);

-- 4. Committee Assignments (Pairing scheduled inspections with assigned experts)
CREATE TABLE IF NOT EXISTS committee_assignments (
    id UUID PRIMARY KEY,
    inspection_id UUID NOT NULL REFERENCES evc_inspections(id) ON DELETE CASCADE,
    expert_id UUID NOT NULL REFERENCES experts(id) ON DELETE RESTRICT,
    role VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PROPOSED',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_assignment_inspection_expert UNIQUE (inspection_id, expert_id),
    CONSTRAINT chk_assignments_role CHECK (role IN ('CHAIRPERSON', 'MEMBER', 'SPECIALIST')),
    CONSTRAINT chk_assignments_status CHECK (status IN ('PROPOSED', 'CONFIRMED', 'DECLINED'))
);

CREATE INDEX IF NOT EXISTS idx_assignments_inspection ON committee_assignments(inspection_id);
CREATE INDEX IF NOT EXISTS idx_assignments_expert ON committee_assignments(expert_id);
