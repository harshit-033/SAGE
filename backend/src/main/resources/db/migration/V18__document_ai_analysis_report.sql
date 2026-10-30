-- ==============================================================================
-- Flyway Migration V18: SAGE AI Document Integrity & Forgery Analysis Report
-- Compatible with PostgreSQL and H2 (PostgreSQL mode)
-- Reference: SAGE AI Review & Verification Engine (Gemini Document Intelligence)
-- ==============================================================================

ALTER TABLE documents ADD COLUMN IF NOT EXISTS ai_analysis_report TEXT;
