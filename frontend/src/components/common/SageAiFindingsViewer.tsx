import React from 'react'
import { AlertTriangle, Sparkles, CheckCircle2, Info } from 'lucide-react'

interface SageAiFindingsViewerProps {
  reportText?: string | null
  report?: string | null
  documentName?: string
  documentType?: string
}

export const SageAiFindingsViewer: React.FC<SageAiFindingsViewerProps> = ({
  reportText,
  report,
  documentName,
  documentType,
}) => {
  const effectiveReport = reportText ?? report

  if (!effectiveReport) {
    return (
      <div
        style={{
          padding: '2.5rem',
          textAlign: 'center',
          backgroundColor: 'var(--bg-page)',
          border: '1px dashed var(--border-medium)',
          borderRadius: 'var(--radius-md)',
          color: 'var(--text-muted)',
        }}
      >
        <Sparkles size={32} style={{ color: 'var(--gov-blue)', marginBottom: '0.75rem' }} />
        <h4 style={{ margin: '0 0 0.5rem 0', color: 'var(--gov-navy)' }}>SAGE AI findings</h4>
        <p style={{ margin: 0, fontSize: '0.85rem' }}>
          Document is awaiting automated Gemini AI validation or initial review.
        </p>
      </div>
    )
  }

  // Parse key sections from report text
  const isHighRisk = effectiveReport.includes('HIGH') || effectiveReport.toLowerCase().includes('high risk') || effectiveReport.includes('REVIEW RECOMMENDED')
  const isMediumRisk = !isHighRisk && (effectiveReport.includes('MEDIUM') || effectiveReport.toLowerCase().includes('medium risk') || effectiveReport.includes('MINOR_IRREGULARITY'))

  return (
    <div
      style={{
        display: 'flex',
        flexDirection: 'column',
        gap: '1.25rem',
        padding: '1.25rem',
        backgroundColor: 'var(--bg-surface)',
        border: '1px solid var(--border-medium)',
        borderRadius: 'var(--radius-md)',
      }}
      id="sage-ai-findings-container"
    >
      {/* Official SAGE AI Header Banner */}
      <div
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          flexWrap: 'wrap',
          gap: '0.75rem',
          paddingBottom: '0.85rem',
          borderBottom: '1px solid var(--border-light)',
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.65rem' }}>
          <div
            style={{
              width: '36px',
              height: '36px',
              borderRadius: '8px',
              background: 'linear-gradient(135deg, #1e3a8a 0%, #3b82f6 100%)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: '#ffffff',
            }}
          >
            <Sparkles size={20} />
          </div>
          <div>
            <h3 style={{ margin: 0, fontSize: '1.1rem', color: 'var(--gov-navy)', fontWeight: 700 }}>
              SAGE AI findings
            </h3>
            <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
              Automated Integrity, Forgery &amp; Format Review (Gemini Document Intelligence)
            </span>
          </div>
        </div>

        {/* Status Pills */}
        <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center' }}>
          <span
            style={{
              padding: '0.3rem 0.75rem',
              borderRadius: '9999px',
              fontSize: '0.75rem',
              fontWeight: 700,
              display: 'inline-flex',
              alignItems: 'center',
              gap: '0.35rem',
              backgroundColor: isHighRisk ? '#fee2e2' : isMediumRisk ? '#fef3c7' : '#dcfce7',
              color: isHighRisk ? '#991b1b' : isMediumRisk ? '#92400e' : '#166534',
            }}
          >
            {isHighRisk ? (
              <>
                <AlertTriangle size={14} /> REVIEW RECOMMENDED (High Risk)
              </>
            ) : isMediumRisk ? (
              <>
                <AlertTriangle size={14} /> ADVISORY NOTICE (Medium Risk)
              </>
            ) : (
              <>
                <CheckCircle2 size={14} /> INTEGRITY VERIFIED (Low Risk)
              </>
            )}
          </span>

          <span
            style={{
              padding: '0.3rem 0.65rem',
              borderRadius: '9999px',
              fontSize: '0.75rem',
              fontWeight: 600,
              backgroundColor: 'var(--gov-blue-light)',
              color: 'var(--gov-blue)',
            }}
          >
            AI Review Only
          </span>
        </div>
      </div>

      {/* Target Document Context */}
      {(documentName || documentType) && (
        <div
          style={{
            display: 'flex',
            gap: '1.5rem',
            padding: '0.75rem 1rem',
            backgroundColor: 'var(--bg-page)',
            borderRadius: 'var(--radius-sm)',
            fontSize: '0.8rem',
          }}
        >
          {documentName && (
            <div>
              <span style={{ color: 'var(--text-muted)' }}>Document: </span>
              <strong style={{ color: 'var(--gov-navy)' }}>{documentName}</strong>
            </div>
          )}
          {documentType && (
            <div>
              <span style={{ color: 'var(--text-muted)' }}>Classification: </span>
              <strong style={{ color: 'var(--gov-navy)' }}>{documentType}</strong>
            </div>
          )}
        </div>
      )}

      {/* Formatted Report Card */}
      <div
        style={{
          padding: '1rem',
          backgroundColor: '#fafbfc',
          border: '1px solid #e2e8f0',
          borderRadius: 'var(--radius-sm)',
          fontSize: '0.85rem',
          lineHeight: 1.6,
          color: 'var(--text-primary)',
          whiteSpace: 'pre-wrap',
          fontFamily: 'inherit',
          maxHeight: '400px',
          overflowY: 'auto',
        }}
      >
        {effectiveReport}
      </div>

      {/* Statutory AICTE Disclaimer Banner */}
      <div
        style={{
          display: 'flex',
          alignItems: 'flex-start',
          gap: '0.65rem',
          padding: '0.85rem 1rem',
          backgroundColor: '#eff6ff',
          borderLeft: '4px solid #3b82f6',
          borderRadius: 'var(--radius-sm)',
          fontSize: '0.78rem',
          color: '#1e3a8a',
          lineHeight: 1.45,
        }}
      >
        <Info size={16} style={{ color: '#2563eb', flexShrink: 0, marginTop: '2px' }} />
        <div>
          <strong style={{ color: '#1e40af' }}>Statutory AICTE Advisory Notice:</strong> SAGE AI provides automated format validation and integrity review. In accordance with AICTE statutory guidelines, artificial intelligence has no authority to accept or reject applications. Final statutory approval remains strictly under the authority of designated AICTE Scrutiny Officers.
        </div>
      </div>
    </div>
  )
}
