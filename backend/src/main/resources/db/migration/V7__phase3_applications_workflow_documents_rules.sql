-- ==============================================================================
-- Flyway Migration V7: Applications, Workflow Engine, Document Scrutiny & Rule Engine
-- Compatible with PostgreSQL and H2 (PostgreSQL compatibility mode)
-- Reference: AICTE Approval Process Handbook (APH 2024-25 to 2026-27)
-- ==============================================================================

-- 1. Workflow Definitions
CREATE TABLE IF NOT EXISTS workflow_definitions (
    id UUID PRIMARY KEY,
    code VARCHAR(64) NOT NULL UNIQUE,
    category VARCHAR(64) NOT NULL,
    name VARCHAR(128) NOT NULL,
    version VARCHAR(32) NOT NULL,
    aph_reference VARCHAR(128),
    requires_evc BOOLEAN NOT NULL DEFAULT FALSE,
    requires_shc BOOLEAN NOT NULL DEFAULT FALSE,
    requires_sac BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_workflow_def_cat ON workflow_definitions(category, is_active);

-- 2. Applications (modelled independently from Institution Profile)
CREATE TABLE IF NOT EXISTS applications (
    id UUID PRIMARY KEY,
    application_number VARCHAR(64) NOT NULL UNIQUE,
    institution_id UUID NOT NULL REFERENCES institutions(id) ON DELETE CASCADE,
    academic_year VARCHAR(32) NOT NULL,
    category VARCHAR(64) NOT NULL,
    workflow_definition_id UUID NOT NULL REFERENCES workflow_definitions(id),
    current_state VARCHAR(64) NOT NULL,
    assigned_to_user_id UUID REFERENCES users(id),
    created_by UUID NOT NULL REFERENCES users(id),
    submitted_at TIMESTAMP WITH TIME ZONE,
    final_decision_at TIMESTAMP WITH TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_apps_institution ON applications(institution_id, current_state);
CREATE INDEX IF NOT EXISTS idx_apps_assigned_user ON applications(assigned_to_user_id, current_state);
CREATE INDEX IF NOT EXISTS idx_apps_academic_year ON applications(academic_year, category);

-- 3. Workflow Transitions History
CREATE TABLE IF NOT EXISTS workflow_transitions (
    id UUID PRIMARY KEY,
    application_id UUID NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
    from_state VARCHAR(64) NOT NULL,
    to_state VARCHAR(64) NOT NULL,
    actor_id UUID NOT NULL REFERENCES users(id),
    actor_role VARCHAR(64) NOT NULL,
    reason TEXT,
    evidence_summary TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_workflow_trans_app ON workflow_transitions(application_id, created_at);

-- 4. Secure Document Registry
CREATE TABLE IF NOT EXISTS documents (
    id UUID PRIMARY KEY,
    application_id UUID NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
    institution_id UUID NOT NULL REFERENCES institutions(id) ON DELETE CASCADE,
    document_type VARCHAR(64) NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    stored_filename VARCHAR(255) NOT NULL,
    file_path VARCHAR(512) NOT NULL,
    content_type VARCHAR(128) NOT NULL,
    file_size_bytes BIGINT NOT NULL,
    sha256_hash VARCHAR(64) NOT NULL,
    ocr_status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    ocr_engine VARCHAR(32),
    overall_confidence DOUBLE PRECISION,
    is_uncertain BOOLEAN NOT NULL DEFAULT FALSE,
    uploaded_by UUID NOT NULL REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_documents_app ON documents(application_id, document_type);
CREATE INDEX IF NOT EXISTS idx_documents_inst ON documents(institution_id);
CREATE INDEX IF NOT EXISTS idx_documents_hash ON documents(sha256_hash);

-- 5. Document Extracted Fields & Provenance
CREATE TABLE IF NOT EXISTS document_extracted_fields (
    id UUID PRIMARY KEY,
    document_id UUID NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
    field_key VARCHAR(64) NOT NULL,
    extracted_value TEXT NOT NULL,
    normalized_value TEXT,
    confidence_score DOUBLE PRECISION NOT NULL DEFAULT 1.0,
    is_uncertain BOOLEAN NOT NULL DEFAULT FALSE,
    source_page INT NOT NULL DEFAULT 1,
    bounding_box_json TEXT,
    raw_text_snippet TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_extracted_fields_doc ON document_extracted_fields(document_id, field_key);

-- 6. Deterministic Scrutiny Rules Catalog (APH 2024-27)
CREATE TABLE IF NOT EXISTS scrutiny_rules (
    id VARCHAR(64) PRIMARY KEY,
    version VARCHAR(32) NOT NULL,
    name VARCHAR(128) NOT NULL,
    description TEXT,
    aph_clause VARCHAR(128) NOT NULL,
    aph_page_number INT,
    application_category VARCHAR(64) NOT NULL DEFAULT 'ALL',
    severity VARCHAR(32) NOT NULL DEFAULT 'CRITICAL_DEFICIENCY',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_scrutiny_rules_cat ON scrutiny_rules(application_category, is_active);

-- 7. Scrutiny Evaluations
CREATE TABLE IF NOT EXISTS scrutiny_evaluations (
    id UUID PRIMARY KEY,
    application_id UUID NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
    status VARCHAR(32) NOT NULL,
    summary TEXT,
    evaluated_by_user_id UUID REFERENCES users(id),
    evaluated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_scrutiny_eval_app ON scrutiny_evaluations(application_id, evaluated_at);

-- 8. Scrutiny Findings & Deficiencies
CREATE TABLE IF NOT EXISTS scrutiny_findings (
    id UUID PRIMARY KEY,
    evaluation_id UUID NOT NULL REFERENCES scrutiny_evaluations(id) ON DELETE CASCADE,
    application_id UUID NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
    rule_id VARCHAR(64) NOT NULL REFERENCES scrutiny_rules(id),
    document_id UUID REFERENCES documents(id),
    status VARCHAR(32) NOT NULL,
    deficiency_type VARCHAR(64),
    explanation TEXT NOT NULL,
    evidence_text TEXT,
    is_resolved BOOLEAN NOT NULL DEFAULT FALSE,
    resolution_notes TEXT,
    resolved_by UUID REFERENCES users(id),
    resolved_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_findings_eval ON scrutiny_findings(evaluation_id, status);
CREATE INDEX IF NOT EXISTS idx_findings_app ON scrutiny_findings(application_id, is_resolved);

-- 9. Phase 3 Permissions (Excluding APPLICATION_READ and APPLICATION_SUBMIT already in V2, WORKFLOW_TRANSITION in V3)
INSERT INTO permissions (id, code, description) VALUES
    ('00000000-0000-0000-0000-000000000031', 'APPLICATION_CREATE', 'Create and initialize approval applications'),
    ('00000000-0000-0000-0000-000000000033', 'DOCUMENT_UPLOAD', 'Upload institutional compliance documents'),
    ('00000000-0000-0000-0000-000000000034', 'DOCUMENT_READ', 'View documents, OCR outputs and extracted fields'),
    ('00000000-0000-0000-0000-000000000035', 'SCRUTINY_EXECUTE', 'Trigger deterministic OCR rule scrutiny evaluation'),
    ('00000000-0000-0000-0000-000000000036', 'SCRUTINY_REVIEW', 'Resolve scrutiny deficiency findings and recommend transitions');

-- 10. Role-Permission mappings for Phase 3
-- Administrator (ROLE_AICTE_ADMIN)
INSERT INTO role_permissions (role_id, permission_id) VALUES
    ('00000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000031'),
    ('00000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000013'),
    ('00000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000033'),
    ('00000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000034'),
    ('00000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000035'),
    ('00000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000036');

-- Regional Officer (ROLE_REGIONAL_OFFICER)
INSERT INTO role_permissions (role_id, permission_id) VALUES
    ('00000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000012'),
    ('00000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000034'),
    ('00000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000035'),
    ('00000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000036');

-- Committee Member (ROLE_COMMITTEE_MEMBER)
INSERT INTO role_permissions (role_id, permission_id) VALUES
    ('00000000-0000-0000-0000-000000000007', '00000000-0000-0000-0000-000000000012'),
    ('00000000-0000-0000-0000-000000000007', '00000000-0000-0000-0000-000000000034'),
    ('00000000-0000-0000-0000-000000000007', '00000000-0000-0000-0000-000000000035'),
    ('00000000-0000-0000-0000-000000000007', '00000000-0000-0000-0000-000000000036');

-- Institution Officer (ROLE_INSTITUTION_OFFICER)
INSERT INTO role_permissions (role_id, permission_id) VALUES
    ('00000000-0000-0000-0000-000000000005', '00000000-0000-0000-0000-000000000012'),
    ('00000000-0000-0000-0000-000000000005', '00000000-0000-0000-0000-000000000031'),
    ('00000000-0000-0000-0000-000000000005', '00000000-0000-0000-0000-000000000033'),
    ('00000000-0000-0000-0000-000000000005', '00000000-0000-0000-0000-000000000034');

-- Auditor (ROLE_AUDITOR)
INSERT INTO role_permissions (role_id, permission_id) VALUES
    ('00000000-0000-0000-0000-000000000009', '00000000-0000-0000-0000-000000000012'),
    ('00000000-0000-0000-0000-000000000009', '00000000-0000-0000-0000-000000000034');

