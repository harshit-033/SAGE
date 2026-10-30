-- ==============================================================================
-- Flyway Migration V14: New Course/Branch Workflows & Document Compliance Tracking
-- Compatible with PostgreSQL and H2 (PostgreSQL mode)
-- Reference: AICTE Approval Process Handbook (APH 2024-25 to 2026-27)
-- ==============================================================================

-- 1. Extend documents table for compliance, verification states, and versioning
ALTER TABLE documents ALTER COLUMN application_id DROP NOT NULL;
ALTER TABLE documents ADD COLUMN document_name VARCHAR(255);
ALTER TABLE documents ADD COLUMN document_category VARCHAR(64) DEFAULT 'GENERAL';
ALTER TABLE documents ADD COLUMN valid_from TIMESTAMP WITH TIME ZONE;
ALTER TABLE documents ADD COLUMN expiry_date TIMESTAMP WITH TIME ZONE;
ALTER TABLE documents ADD COLUMN verification_status VARCHAR(64) DEFAULT 'PENDING_REVIEW';
ALTER TABLE documents ADD COLUMN compliance_status VARCHAR(64) DEFAULT 'COMPLIANT';
ALTER TABLE documents ADD COLUMN version_number INT DEFAULT 1;
ALTER TABLE documents ADD COLUMN previous_version_id UUID REFERENCES documents(id);
ALTER TABLE documents ADD COLUMN root_document_id UUID REFERENCES documents(id);
ALTER TABLE documents ADD COLUMN officer_comments TEXT;
ALTER TABLE documents ADD COLUMN clarification_text TEXT;

CREATE INDEX idx_documents_compliance ON documents(institution_id, compliance_status);
CREATE INDEX idx_documents_expiry ON documents(expiry_date);
CREATE INDEX idx_documents_verification ON documents(verification_status);
CREATE INDEX idx_documents_version ON documents(root_document_id, version_number);

-- 2. Document Corrective Actions Table
CREATE TABLE IF NOT EXISTS document_corrective_actions (
    id UUID PRIMARY KEY,
    institution_id UUID NOT NULL REFERENCES institutions(id) ON DELETE CASCADE,
    application_id UUID REFERENCES applications(id) ON DELETE CASCADE,
    document_id UUID REFERENCES documents(id) ON DELETE CASCADE,
    finding_id UUID,
    action_type VARCHAR(64) NOT NULL,
    description TEXT NOT NULL,
    priority VARCHAR(32) NOT NULL DEFAULT 'HIGH',
    responsible_party VARCHAR(64) NOT NULL DEFAULT 'INSTITUTION_OFFICER',
    due_date TIMESTAMP WITH TIME ZONE,
    status VARCHAR(32) NOT NULL DEFAULT 'OPEN',
    institution_response TEXT,
    resolution_evidence_doc_id UUID REFERENCES documents(id),
    officer_notes TEXT,
    resolved_by UUID REFERENCES users(id),
    resolved_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_corrective_actions_inst ON document_corrective_actions(institution_id, status);
CREATE INDEX idx_corrective_actions_due ON document_corrective_actions(due_date);
CREATE INDEX idx_corrective_actions_doc ON document_corrective_actions(document_id);

-- 3. Application Course Details Table (New Course Introduction)
CREATE TABLE IF NOT EXISTS application_course_details (
    id UUID PRIMARY KEY,
    application_id UUID NOT NULL REFERENCES applications(id) ON DELETE CASCADE UNIQUE,
    course_name VARCHAR(255) NOT NULL,
    programme_level VARCHAR(64) NOT NULL,
    degree_type VARCHAR(64) NOT NULL,
    course_discipline VARCHAR(128) NOT NULL,
    proposed_intake INT NOT NULL,
    mode_of_delivery VARCHAR(64) NOT NULL DEFAULT 'REGULAR',
    duration_years INT NOT NULL DEFAULT 4,
    regulatory_classification VARCHAR(64),
    proposed_department VARCHAR(128),
    existing_related_courses TEXT,
    available_classrooms INT NOT NULL DEFAULT 0,
    laboratories_count INT NOT NULL DEFAULT 0,
    laboratory_details TEXT,
    equipment_details TEXT,
    library_titles INT NOT NULL DEFAULT 0,
    library_volumes INT NOT NULL DEFAULT 0,
    library_journals INT NOT NULL DEFAULT 0,
    professors_count INT NOT NULL DEFAULT 0,
    associate_professors_count INT NOT NULL DEFAULT 0,
    assistant_professors_count INT NOT NULL DEFAULT 0,
    faculty_student_ratio VARCHAR(32),
    infrastructure_notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_course_details_app ON application_course_details(application_id);

-- 4. Application Branch Details Table (New Branch / Specialization)
CREATE TABLE IF NOT EXISTS application_branch_details (
    id UUID PRIMARY KEY,
    application_id UUID NOT NULL REFERENCES applications(id) ON DELETE CASCADE UNIQUE,
    existing_programme VARCHAR(128) NOT NULL,
    existing_department VARCHAR(128) NOT NULL,
    proposed_branch_name VARCHAR(255) NOT NULL,
    proposed_intake INT NOT NULL,
    existing_approved_intake INT NOT NULL DEFAULT 0,
    faculty_count INT NOT NULL DEFAULT 0,
    staff_count INT NOT NULL DEFAULT 0,
    available_classrooms INT NOT NULL DEFAULT 0,
    laboratory_count INT NOT NULL DEFAULT 0,
    laboratory_requirements TEXT,
    equipment_availability TEXT,
    institution_declarations TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_branch_details_app ON application_branch_details(application_id);
