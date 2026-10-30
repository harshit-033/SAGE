import React from 'react'
import { Modal } from './Modal'
import { AlertTriangle, CheckCircle2, ShieldAlert, X } from 'lucide-react'
import type { DocumentAiValidationResult } from '../../api/documentComplianceApi'

interface SageAiPreUploadModalProps {
  isOpen: boolean
  onClose: () => void
  onProceed: (fullReport: string) => void
  validationResult: DocumentAiValidationResult | null
  isAnalyzing?: boolean
  isUploading?: boolean
  fileName: string
}

export const SageAiPreUploadModal: React.FC<SageAiPreUploadModalProps> = ({
  isOpen,
  onClose,
  onProceed,
  validationResult,
  isAnalyzing = false,
  isUploading = false,
  fileName,
}) => {
  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title="SAGE AI Pre-Upload Validation & Review"
    >
      <div style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
        {/* Analyzing Spinner */}
        {isAnalyzing && (
          <div style={{ padding: '2.5rem', textAlign: 'center' }}>
            <div
              style={{
                width: '48px',
                height: '48px',
                borderRadius: '50%',
                border: '3px solid var(--border-medium)',
                borderTopColor: 'var(--gov-blue)',
                animation: 'spin 1s linear infinite',
                margin: '0 auto 1rem auto',
              }}
            />
            <h4 style={{ margin: '0 0 0.5rem 0', color: 'var(--gov-navy)' }}>
              Inspecting Document with SAGE AI...
            </h4>
            <p style={{ margin: 0, fontSize: '0.85rem', color: 'var(--text-secondary)' }}>
              Checking format adherence, issuing authority markers, and digital forgery risks via Gemini Document Intelligence.
            </p>
          </div>
        )}

        {/* AI Findings Available */}
        {!isAnalyzing && validationResult && (
          <>
            {/* Header Badge */}
            <div
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '0.65rem',
                padding: '0.75rem 1rem',
                borderRadius: 'var(--radius-sm)',
                backgroundColor: validationResult.hasIssues ? '#fef3c7' : '#dcfce7',
                border: `1px solid ${validationResult.hasIssues ? '#fde68a' : '#bbf7d0'}`,
              }}
            >
              {validationResult.hasIssues ? (
                <AlertTriangle size={20} style={{ color: '#b45309', flexShrink: 0 }} />
              ) : (
                <CheckCircle2 size={20} style={{ color: '#15803d', flexShrink: 0 }} />
              )}
              <div style={{ flex: 1 }}>
                <div
                  style={{
                    fontWeight: 700,
                    fontSize: '0.9rem',
                    color: validationResult.hasIssues ? '#92400e' : '#166534',
                  }}
                >
                  {validationResult.hasIssues
                    ? 'Attention: Potential Format or Integrity Discrepancies Detected'
                    : 'Integrity Check Passed: Format Verified'}
                </div>
                <div style={{ fontSize: '0.78rem', color: validationResult.hasIssues ? '#78350f' : '#14532d' }}>
                  {validationResult.summary}
                </div>
              </div>
            </div>

            {/* Target File Info */}
            <div
              style={{
                fontSize: '0.8rem',
                color: 'var(--text-secondary)',
                display: 'flex',
                justifyContent: 'space-between',
                padding: '0.5rem 0.75rem',
                backgroundColor: 'var(--bg-page)',
                borderRadius: 'var(--radius-sm)',
              }}
            >
              <span>Selected Document: <strong>{fileName}</strong></span>
              <span>
                Forgery Risk: <strong style={{ color: validationResult.hasIssues ? '#b91c1c' : '#15803d' }}>
                  {validationResult.forgeryRisk}
                </strong>
              </span>
            </div>

            {/* Observations List */}
            {validationResult.hasIssues && (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                <div style={{ fontSize: '0.82rem', fontWeight: 700, color: 'var(--gov-navy)' }}>
                  SAGE AI Findings &amp; Observations:
                </div>
                <ul
                  style={{
                    margin: 0,
                    paddingLeft: '1.25rem',
                    fontSize: '0.8rem',
                    color: 'var(--text-secondary)',
                    lineHeight: 1.5,
                  }}
                >
                  {validationResult.forgeryObservations.map((obs, idx) => (
                    <li key={`forgery-${idx}`} style={{ marginBottom: '0.25rem' }}>
                      <strong style={{ color: '#991b1b' }}>Integrity Flag: </strong>
                      {obs}
                    </li>
                  ))}
                  {validationResult.formatObservations.map((obs, idx) => (
                    <li key={`format-${idx}`} style={{ marginBottom: '0.25rem' }}>
                      <strong style={{ color: '#92400e' }}>Format Note: </strong>
                      {obs}
                    </li>
                  ))}
                </ul>
              </div>
            )}

            {/* Statutory Choice & Agency Card */}
            <div
              style={{
                padding: '0.85rem 1rem',
                backgroundColor: '#eff6ff',
                borderLeft: '4px solid #2563eb',
                borderRadius: 'var(--radius-sm)',
                fontSize: '0.8rem',
                color: '#1e3a8a',
                lineHeight: 1.5,
              }}
            >
              <div style={{ fontWeight: 700, color: '#1e40af', marginBottom: '0.2rem' }}>
                User Authority &amp; Statutory Non-Rejection Notice:
              </div>
              In accordance with AICTE regulations, SAGE AI performs advisory review only and cannot reject or block your document. <strong>The final decision to proceed remains entirely in your hands.</strong> You may submit this document as-is, or cancel to provide a replacement.
            </div>

            {/* Action Buttons */}
            <div
              style={{
                display: 'flex',
                justifyContent: 'flex-end',
                gap: '0.75rem',
                paddingTop: '0.5rem',
                borderTop: '1px solid var(--border-light)',
              }}
            >
              <button
                type="button"
                onClick={onClose}
                disabled={isUploading}
                className="btn btn-outline"
                id="btn-ai-cancel-upload"
              >
                <X size={15} /> Cancel / Select Different File
              </button>
              <button
                type="button"
                onClick={() => onProceed(validationResult.fullReport)}
                disabled={isUploading}
                className={`btn ${validationResult.hasIssues ? 'btn-primary' : 'btn-primary'}`}
                style={{
                  backgroundColor: validationResult.hasIssues ? 'var(--gov-orange)' : 'var(--gov-green)',
                  borderColor: validationResult.hasIssues ? 'var(--gov-orange)' : 'var(--gov-green)',
                }}
                id="btn-ai-proceed-upload"
              >
                {isUploading ? (
                  <span>Uploading Document...</span>
                ) : validationResult.hasIssues ? (
                  <>
                    <ShieldAlert size={15} /> Proceed &amp; Upload Anyway
                  </>
                ) : (
                  <>
                    <CheckCircle2 size={15} /> Complete Upload
                  </>
                )}
              </button>
            </div>
          </>
        )}
      </div>
    </Modal>
  )
}
