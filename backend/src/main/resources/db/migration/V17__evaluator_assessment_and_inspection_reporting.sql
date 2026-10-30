-- ==============================================================================
-- Flyway Migration V15: Evaluator Assessment & Inspection Reporting Schema
-- SAGE Phase 1: Problem Statement Alignment & Full Workflow Lifecycle Completion
-- Compatible with PostgreSQL and H2 (PostgreSQL compatibility mode)
-- ==============================================================================

-- 1. Extend scrutiny_evaluations with Structured Evaluator Assessment fields
ALTER TABLE scrutiny_evaluations ADD COLUMN IF NOT EXISTS score INTEGER;
ALTER TABLE scrutiny_evaluations ADD COLUMN IF NOT EXISTS recommendation VARCHAR(64);
ALTER TABLE scrutiny_evaluations ADD COLUMN IF NOT EXISTS remarks TEXT;
ALTER TABLE scrutiny_evaluations ADD COLUMN IF NOT EXISTS criteria_breakdown_json TEXT;

-- 2. Extend evc_inspections with EVC Inspection Reporting fields
ALTER TABLE evc_inspections ADD COLUMN IF NOT EXISTS report_findings TEXT;
ALTER TABLE evc_inspections ADD COLUMN IF NOT EXISTS report_recommendation VARCHAR(64);
ALTER TABLE evc_inspections ADD COLUMN IF NOT EXISTS infrastructure_verified BOOLEAN DEFAULT FALSE;
ALTER TABLE evc_inspections ADD COLUMN IF NOT EXISTS faculty_verified BOOLEAN DEFAULT FALSE;
ALTER TABLE evc_inspections ADD COLUMN IF NOT EXISTS conducted_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE evc_inspections ADD COLUMN IF NOT EXISTS submitted_by_user_id UUID REFERENCES users(id);

CREATE INDEX IF NOT EXISTS idx_evc_inspections_report_rec ON evc_inspections(report_recommendation);
