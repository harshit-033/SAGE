import React, { useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { applicationsApi, type DocumentDto, type ScrutinyFindingDto } from '../api/applicationsApi'
import { useAuth } from '../context/AuthContext'
import { StatusBadge } from '../components/common/StatusBadge'
import { Modal } from '../components/common/Modal'
import {
  FileText,
  Upload,
  Cpu,
  AlertTriangle,
  Clock,
  ArrowLeft,
  RefreshCw,
  Send,
  ShieldCheck,
  Layers,
  FileCheck2
} from 'lucide-react'

interface ApiErrorResponse {
  response?: {
    data?: {
      detail?: string
    }
  }
}

export const ApplicationDetails: React.FC = () => {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const { hasRole, isAdmin } = useAuth()

  const [activeTab, setActiveTab] = useState<'documents' | 'scrutiny' | 'workflow'>('documents')

  // Upload Form State
  const [selectedDocType, setSelectedDocType] = useState<string>('FIRE_SAFETY_CERTIFICATE')
  const [uploadFile, setUploadFile] = useState<File | null>(null)
  const [uploadError, setUploadError] = useState<string | null>(null)

  // Finding Resolution Modal State
  const [resolvingFinding, setResolvingFinding] = useState<ScrutinyFindingDto | null>(null)
  const [resolutionNotes, setResolutionNotes] = useState<string>('')
  const [resolveError, setResolveError] = useState<string | null>(null)

  // Transition Modal State
  const [isTransitionModalOpen, setIsTransitionModalOpen] = useState<boolean>(false)
  const [targetState, setTargetState] = useState<string>('UNDER_SCRUTINY')
  const [transitionReason, setTransitionReason] = useState<string>('')
  const [transitionError, setTransitionError] = useState<string | null>(null)

  // Fetch application details
  const { data: application, isLoading: isAppLoading } = useQuery({
    queryKey: ['application', id],
    queryFn: () => applicationsApi.get(id!),
    enabled: !!id,
  })

  // Fetch documents
  const { data: documents = [] } = useQuery({
    queryKey: ['application-documents', id],
    queryFn: () => applicationsApi.listDocuments(id!),
    enabled: !!id,
  })

  // Fetch scrutiny findings
  const { data: findings = [] } = useQuery({
    queryKey: ['application-findings', id],
    queryFn: () => applicationsApi.getFindings(id!),
    enabled: !!id,
  })

  // Fetch workflow transitions
  const { data: transitions = [] } = useQuery({
    queryKey: ['application-transitions', id],
    queryFn: () => applicationsApi.getTransitions(id!),
    enabled: !!id,
  })

  // Mutations
  const uploadMutation = useMutation({
    mutationFn: ({ docType, file }: { docType: string; file: File }) =>
      applicationsApi.uploadDocument(id!, docType, file),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['application-documents', id] })
      setUploadFile(null)
      setUploadError(null)
    },
    onError: (err: unknown) => {
      const e = err as ApiErrorResponse
      setUploadError(e.response?.data?.detail || 'Failed to upload document')
    },
  })

  const reprocessOcrMutation = useMutation({
    mutationFn: (docId: string) => applicationsApi.reprocessOcr(id!, docId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['application-documents', id] })
    },
  })

  const evaluateScrutinyMutation = useMutation({
    mutationFn: () => applicationsApi.evaluateScrutiny(id!),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['application-findings', id] })
      queryClient.invalidateQueries({ queryKey: ['application', id] })
      setActiveTab('scrutiny')
    },
  })

  const resolveFindingMutation = useMutation({
    mutationFn: ({ findingId, notes }: { findingId: string; notes: string }) =>
      applicationsApi.resolveFinding(id!, findingId, { resolutionNotes: notes }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['application-findings', id] })
      queryClient.invalidateQueries({ queryKey: ['application', id] })
      setResolvingFinding(null)
      setResolutionNotes('')
    },
    onError: (err: unknown) => {
      const e = err as ApiErrorResponse
      setResolveError(e.response?.data?.detail || 'Failed to resolve finding')
    },
  })

  const submitAppMutation = useMutation({
    mutationFn: () => applicationsApi.submit(id!),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['application', id] })
      queryClient.invalidateQueries({ queryKey: ['application-transitions', id] })
    },
  })

  const transitionMutation = useMutation({
    mutationFn: ({ state, reason }: { state: string; reason: string }) =>
      applicationsApi.transition(id!, { targetState: state, reason }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['application', id] })
      queryClient.invalidateQueries({ queryKey: ['application-transitions', id] })
      setIsTransitionModalOpen(false)
      setTransitionReason('')
    },
    onError: (err: unknown) => {
      const e = err as ApiErrorResponse
      setTransitionError(e.response?.data?.detail || 'Transition rejected')
    },
  })

  const handleUploadSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    if (!uploadFile) {
      setUploadError('Please select a PDF or image file')
      return
    }
    uploadMutation.mutate({ docType: selectedDocType, file: uploadFile })
  }

  const handleResolveSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    if (!resolvingFinding || !resolutionNotes.trim()) {
      setResolveError('Resolution justification notes are mandatory')
      return
    }
    resolveFindingMutation.mutate({ findingId: resolvingFinding.id, notes: resolutionNotes })
  }

  const handleTransitionSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    transitionMutation.mutate({ state: targetState, reason: transitionReason })
  }

  if (isAppLoading || !application) {
    return (
      <div style={{ padding: '3rem', textAlign: 'center', color: 'var(--text-secondary)' }}>
        Loading application details and statutory workflow state...
      </div>
    )
  }

  // Stepper State Logic
  const workflowSteps = [
    { label: 'Draft', state: 'DRAFT' },
    { label: 'Submitted', state: 'SUBMITTED' },
    { label: 'Scrutiny & OCR', state: 'UNDER_SCRUTINY' },
    { label: 'Scrutiny Passed', state: 'SCRUTINY_PASSED' },
    { label: 'EVC / Committee', state: 'EVC_SCHEDULED' },
    { label: 'Final Decision', state: 'APPROVED' },
  ]

  const isOfficerOrAdmin = isAdmin || hasRole('ROLE_REGIONAL_OFFICER') || hasRole('ROLE_COMMITTEE_MEMBER')

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
      {/* Back button and breadcrumb */}
      <div>
        <button
          onClick={() => navigate('/applications')}
          style={{
            display: 'inline-flex',
            alignItems: 'center',
            gap: '0.4rem',
            background: 'none',
            border: 'none',
            color: 'var(--text-secondary)',
            fontSize: '0.85rem',
            cursor: 'pointer',
            padding: '0.2rem 0',
          }}
        >
          <ArrowLeft size={16} />
          <span>Back to Applications</span>
        </button>
      </div>

      {/* Header Card */}
      <div
        style={{
          backgroundColor: 'var(--bg-surface)',
          padding: '1.5rem',
          borderRadius: 'var(--radius-md)',
          border: '1px solid var(--border-medium)',
          display: 'flex',
          flexDirection: 'column',
          gap: '1.25rem',
        }}
      >
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '1rem' }}>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
              <h1 style={{ fontSize: '1.5rem', fontWeight: 800, color: 'var(--gov-navy)' }}>
                {application.applicationNumber}
              </h1>
              <StatusBadge status={application.currentState} />
            </div>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem', marginTop: '0.25rem' }}>
              {application.institutionName} • Category: <strong>{application.category.replace(/_/g, ' ')}</strong> • Session: <strong>{application.academicYear}</strong>
            </p>
          </div>

          {/* Quick Actions */}
          <div style={{ display: 'flex', gap: '0.75rem', flexWrap: 'wrap' }}>
            {application.currentState === 'DRAFT' && (
              <button
                onClick={() => submitAppMutation.mutate()}
                disabled={submitAppMutation.isPending}
                className="btn btn-primary"
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: '0.4rem',
                  backgroundColor: 'var(--gov-orange)',
                  color: '#ffffff',
                  fontWeight: 700,
                  fontSize: '0.85rem',
                }}
              >
                <Send size={15} />
                <span>Submit Application</span>
              </button>
            )}

            <button
              onClick={() => evaluateScrutinyMutation.mutate()}
              disabled={evaluateScrutinyMutation.isPending}
              className="btn btn-outline"
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '0.4rem',
                borderColor: 'var(--gov-blue)',
                color: 'var(--gov-blue)',
                fontWeight: 700,
                fontSize: '0.85rem',
              }}
            >
              <Cpu size={15} />
              <span>{evaluateScrutinyMutation.isPending ? 'Scrutinizing...' : 'Evaluate Scrutiny Rules'}</span>
            </button>

            {isOfficerOrAdmin && (
              <button
                onClick={() => {
                  setTransitionError(null)
                  setIsTransitionModalOpen(true)
                }}
                className="btn btn-outline"
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: '0.4rem',
                  fontSize: '0.85rem',
                  fontWeight: 600,
                }}
              >
                <Layers size={15} />
                <span>Advance Workflow</span>
              </button>
            )}
          </div>
        </div>

        {/* Workflow State Pipeline Visualizer */}
        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            backgroundColor: 'var(--bg-surface-secondary)',
            padding: '1rem',
            borderRadius: 'var(--radius-sm)',
            border: '1px solid var(--border-light)',
            overflowX: 'auto',
          }}
        >
          {workflowSteps.map((step, idx) => {
            const isCurrent = application.currentState === step.state
            return (
              <div
                key={step.state}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: '0.5rem',
                  color: isCurrent ? 'var(--gov-orange)' : 'var(--text-secondary)',
                  fontWeight: isCurrent ? 800 : 500,
                  fontSize: '0.82rem',
                }}
              >
                <div
                  style={{
                    width: '24px',
                    height: '24px',
                    borderRadius: '50%',
                    backgroundColor: isCurrent ? 'var(--gov-orange)' : 'var(--border-medium)',
                    color: isCurrent ? '#ffffff' : 'var(--text-muted)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    fontSize: '0.75rem',
                    fontWeight: 700,
                  }}
                >
                  {idx + 1}
                </div>
                <span>{step.label}</span>
                {idx < workflowSteps.length - 1 && (
                  <span style={{ color: 'var(--text-muted)', margin: '0 0.5rem' }}>→</span>
                )}
              </div>
            )
          })}
        </div>
      </div>

      {/* Navigation Tabs */}
      <div style={{ display: 'flex', gap: '0.5rem', borderBottom: '2px solid var(--border-medium)' }}>
        <button
          onClick={() => setActiveTab('documents')}
          style={{
            padding: '0.75rem 1.25rem',
            fontSize: '0.875rem',
            fontWeight: 700,
            border: 'none',
            borderBottom: activeTab === 'documents' ? '3px solid var(--gov-orange)' : '3px solid transparent',
            color: activeTab === 'documents' ? 'var(--gov-navy)' : 'var(--text-secondary)',
            backgroundColor: 'transparent',
            cursor: 'pointer',
            display: 'flex',
            alignItems: 'center',
            gap: '0.4rem',
          }}
        >
          <FileText size={16} />
          <span>Compliance Documents &amp; OCR ({documents.length})</span>
        </button>

        <button
          onClick={() => setActiveTab('scrutiny')}
          style={{
            padding: '0.75rem 1.25rem',
            fontSize: '0.875rem',
            fontWeight: 700,
            border: 'none',
            borderBottom: activeTab === 'scrutiny' ? '3px solid var(--gov-orange)' : '3px solid transparent',
            color: activeTab === 'scrutiny' ? 'var(--gov-navy)' : 'var(--text-secondary)',
            backgroundColor: 'transparent',
            cursor: 'pointer',
            display: 'flex',
            alignItems: 'center',
            gap: '0.4rem',
          }}
        >
          <FileCheck2 size={16} />
          <span>Deterministic Scrutiny Findings ({findings.length})</span>
        </button>

        <button
          onClick={() => setActiveTab('workflow')}
          style={{
            padding: '0.75rem 1.25rem',
            fontSize: '0.875rem',
            fontWeight: 700,
            border: 'none',
            borderBottom: activeTab === 'workflow' ? '3px solid var(--gov-orange)' : '3px solid transparent',
            color: activeTab === 'workflow' ? 'var(--gov-navy)' : 'var(--text-secondary)',
            backgroundColor: 'transparent',
            cursor: 'pointer',
            display: 'flex',
            alignItems: 'center',
            gap: '0.4rem',
          }}
        >
          <Clock size={16} />
          <span>Workflow &amp; Audit Trail ({transitions.length})</span>
        </button>
      </div>

      {/* Tab 1: Documents & OCR */}
      {activeTab === 'documents' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
          {/* Upload Form */}
          <div
            style={{
              backgroundColor: 'var(--bg-surface)',
              padding: '1.25rem',
              borderRadius: 'var(--radius-md)',
              border: '1px solid var(--border-medium)',
            }}
          >
            <h3 style={{ fontSize: '1rem', fontWeight: 700, color: 'var(--gov-navy)', marginBottom: '0.75rem' }}>
              Upload Regulatory Document
            </h3>

            {uploadError && (
              <div
                style={{
                  padding: '0.75rem',
                  backgroundColor: 'var(--status-danger-bg)',
                  color: 'var(--status-danger-text)',
                  borderRadius: 'var(--radius-sm)',
                  fontSize: '0.85rem',
                  marginBottom: '1rem',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '0.5rem',
                }}
              >
                <AlertTriangle size={16} />
                <span>{uploadError}</span>
              </div>
            )}

            <form
              onSubmit={handleUploadSubmit}
              style={{ display: 'flex', alignItems: 'flex-end', gap: '1rem', flexWrap: 'wrap' }}
            >
              <div style={{ flex: '1', minWidth: '220px' }}>
                <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 600, marginBottom: '0.35rem' }}>
                  Document Type (APH Mandatory Checklist)
                </label>
                <select
                  value={selectedDocType}
                  onChange={(e) => setSelectedDocType(e.target.value)}
                  className="form-control"
                  style={{ fontSize: '0.85rem' }}
                >
                  <option value="FIRE_SAFETY_CERTIFICATE">Fire Safety Certificate / NOC</option>
                  <option value="OCCUPANCY_CERTIFICATE">Building Occupancy / Completion Certificate</option>
                  <option value="LAND_REGISTRATION">Land Title Deed &amp; Survey Schedule</option>
                  <option value="AFFIDAVIT_FORMAT_1">Statutory Anti-Ragging Affidavit (Format-1)</option>
                  <option value="BOARD_RESOLUTION">Governing Body Board Resolution</option>
                </select>
              </div>

              <div style={{ flex: '1.5', minWidth: '250px' }}>
                <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 600, marginBottom: '0.35rem' }}>
                  Select PDF or Scan File (Max 15MB, Verified Magic Bytes)
                </label>
                <input
                  type="file"
                  accept=".pdf,.png,.jpg,.jpeg"
                  onChange={(e) => setUploadFile(e.target.files ? e.target.files[0] : null)}
                  className="form-control"
                  style={{ fontSize: '0.85rem' }}
                />
              </div>

              <button
                type="submit"
                disabled={uploadMutation.isPending || !uploadFile}
                className="btn btn-primary"
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: '0.4rem',
                  backgroundColor: 'var(--gov-orange)',
                  color: '#ffffff',
                  fontWeight: 700,
                  fontSize: '0.85rem',
                }}
              >
                <Upload size={15} />
                <span>{uploadMutation.isPending ? 'Processing OCR...' : 'Upload & Scrutinize'}</span>
              </button>
            </form>
          </div>

          {/* Documents List */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            {documents.length === 0 ? (
              <div
                style={{
                  backgroundColor: 'var(--bg-surface)',
                  padding: '2.5rem',
                  textAlign: 'center',
                  borderRadius: 'var(--radius-md)',
                  border: '1px solid var(--border-light)',
                }}
              >
                <FileText size={32} color="var(--text-muted)" style={{ margin: '0 auto 0.5rem' }} />
                <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>
                  No documents uploaded yet. Upload mandatory certificates above to trigger Indra OCR scrutiny.
                </p>
              </div>
            ) : (
              documents.map((doc: DocumentDto) => (
                <div
                  key={doc.id}
                  style={{
                    backgroundColor: 'var(--bg-surface)',
                    borderRadius: 'var(--radius-md)',
                    border: '1px solid var(--border-medium)',
                    padding: '1.25rem',
                    display: 'flex',
                    flexDirection: 'column',
                    gap: '1rem',
                  }}
                >
                  {/* Document Meta Row */}
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '0.75rem' }}>
                    <div>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                        <h4 style={{ fontSize: '1rem', fontWeight: 700, color: 'var(--gov-navy)' }}>
                          {doc.originalFilename}
                        </h4>
                        <span
                          style={{
                            fontSize: '0.72rem',
                            fontWeight: 700,
                            padding: '0.2rem 0.5rem',
                            borderRadius: 'var(--radius-xs)',
                            backgroundColor: 'var(--gov-blue-light)',
                            color: 'var(--gov-blue)',
                          }}
                        >
                          {doc.documentType.replace(/_/g, ' ')}
                        </span>
                      </div>
                      <p style={{ color: 'var(--text-muted)', fontSize: '0.75rem', marginTop: '0.2rem', fontFamily: 'monospace' }}>
                        SHA-256: {doc.sha256Hash} • {(doc.fileSizeBytes / 1024).toFixed(1)} KB
                      </p>
                    </div>

                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                      {/* OCR Engine Badge */}
                      <div
                        style={{
                          display: 'flex',
                          alignItems: 'center',
                          gap: '0.35rem',
                          padding: '0.3rem 0.6rem',
                          borderRadius: 'var(--radius-xs)',
                          backgroundColor: '#f3e8ff',
                          color: '#6b21a8',
                          fontSize: '0.75rem',
                          fontWeight: 700,
                        }}
                      >
                        <Cpu size={14} />
                        <span>{doc.ocrEngine || 'Indra OCR'}</span>
                      </div>

                      {/* OCR Confidence Badge */}
                      <div
                        style={{
                          padding: '0.3rem 0.6rem',
                          borderRadius: 'var(--radius-xs)',
                          backgroundColor: (doc.overallConfidence ?? 1) >= 0.70 ? 'var(--status-success-bg)' : 'var(--status-warning-bg)',
                          color: (doc.overallConfidence ?? 1) >= 0.70 ? 'var(--status-success-text)' : 'var(--status-warning-text)',
                          fontSize: '0.75rem',
                          fontWeight: 700,
                        }}
                      >
                        Confidence: {Math.round((doc.overallConfidence ?? 1) * 100)}%
                      </div>

                      <button
                        onClick={() => reprocessOcrMutation.mutate(doc.id)}
                        disabled={reprocessOcrMutation.isPending}
                        title="Rerun Indra OCR analysis"
                        className="btn btn-outline"
                        style={{ padding: '0.3rem 0.6rem', fontSize: '0.75rem', display: 'flex', alignItems: 'center', gap: '0.3rem' }}
                      >
                        <RefreshCw size={12} />
                        <span>Rerun OCR</span>
                      </button>
                    </div>
                  </div>

                  {/* Extracted Fields Table */}
                  {doc.extractedFields && doc.extractedFields.length > 0 ? (
                    <div
                      style={{
                        backgroundColor: 'var(--bg-surface-secondary)',
                        padding: '0.75rem',
                        borderRadius: 'var(--radius-sm)',
                        border: '1px solid var(--border-light)',
                      }}
                    >
                      <h5 style={{ fontSize: '0.78rem', fontWeight: 700, color: 'var(--text-muted)', textTransform: 'uppercase', marginBottom: '0.5rem' }}>
                        Structured Fields Extracted by Indra OCR (with Provenance)
                      </h5>
                      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))', gap: '0.65rem' }}>
                        {doc.extractedFields.map((f) => (
                          <div
                            key={f.id}
                            style={{
                              backgroundColor: 'var(--bg-surface)',
                              padding: '0.5rem 0.75rem',
                              borderRadius: 'var(--radius-xs)',
                              border: '1px solid var(--border-light)',
                            }}
                          >
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                              <span style={{ fontSize: '0.72rem', color: 'var(--text-muted)', fontWeight: 600 }}>
                                {f.fieldKey.replace(/_/g, ' ').toUpperCase()}
                              </span>
                              <span style={{ fontSize: '0.7rem', color: 'var(--gov-green)', fontWeight: 700 }}>
                                {Math.round(f.confidenceScore * 100)}% (p.{f.sourcePage})
                              </span>
                            </div>
                            <div style={{ fontSize: '0.85rem', fontWeight: 700, color: 'var(--text-primary)', marginTop: '0.15rem' }}>
                              {f.normalizedValue || f.extractedValue}
                            </div>
                          </div>
                        ))}
                      </div>
                    </div>
                  ) : (
                    <div style={{ color: 'var(--text-muted)', fontSize: '0.8rem', fontStyle: 'italic' }}>
                      No structured fields extracted or OCR processing pending.
                    </div>
                  )}
                </div>
              ))
            )}
          </div>
        </div>
      )}

      {/* Tab 2: Deterministic Scrutiny Findings */}
      {activeTab === 'scrutiny' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
          {/* Action & Banner */}
          <div
            style={{
              backgroundColor: 'var(--bg-surface)',
              padding: '1.25rem',
              borderRadius: 'var(--radius-md)',
              border: '1px solid var(--border-medium)',
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
              flexWrap: 'wrap',
              gap: '1rem',
            }}
          >
            <div>
              <h3 style={{ fontSize: '1rem', fontWeight: 800, color: 'var(--gov-navy)' }}>
                APH 2024-27 Scrutiny Rule Findings
              </h3>
              <p style={{ color: 'var(--text-secondary)', fontSize: '0.82rem', marginTop: '0.2rem' }}>
                Deterministic rule engine evaluates mandatory document presence, validity, area norms, and profile concordance.
              </p>
            </div>

            <button
              onClick={() => evaluateScrutinyMutation.mutate()}
              disabled={evaluateScrutinyMutation.isPending}
              className="btn btn-primary"
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '0.4rem',
                backgroundColor: 'var(--gov-blue)',
                color: '#ffffff',
                fontWeight: 700,
                fontSize: '0.85rem',
              }}
            >
              <Cpu size={15} />
              <span>{evaluateScrutinyMutation.isPending ? 'Evaluating Rules...' : 'Re-evaluate All Rules'}</span>
            </button>
          </div>

          {/* Findings Cards */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            {findings.length === 0 ? (
              <div
                style={{
                  backgroundColor: 'var(--bg-surface)',
                  padding: '3rem',
                  textAlign: 'center',
                  borderRadius: 'var(--radius-md)',
                  border: '1px solid var(--border-light)',
                }}
              >
                <Cpu size={36} color="var(--text-muted)" style={{ margin: '0 auto 0.75rem' }} />
                <h4 style={{ fontSize: '1.05rem', fontWeight: 700, color: 'var(--text-primary)' }}>
                  Scrutiny not yet evaluated
                </h4>
                <p style={{ color: 'var(--text-secondary)', fontSize: '0.85rem', marginTop: '0.25rem' }}>
                  Click "Evaluate Scrutiny Rules" above to trigger automated scrutiny against uploaded compliance documents.
                </p>
              </div>
            ) : (
              findings.map((finding: ScrutinyFindingDto) => {
                const isDeficient = finding.status === 'DEFICIENT' && !finding.resolved
                const isUncertain = finding.status === 'UNCERTAIN_OCR' && !finding.resolved

                return (
                  <div
                    key={finding.id}
                    style={{
                      backgroundColor: 'var(--bg-surface)',
                      borderRadius: 'var(--radius-md)',
                      border: isDeficient
                        ? '1px solid var(--status-danger-border)'
                        : isUncertain
                        ? '1px solid var(--status-warning-border)'
                        : '1px solid var(--status-success-border)',
                      padding: '1.25rem',
                      display: 'flex',
                      flexDirection: 'column',
                      gap: '0.75rem',
                    }}
                  >
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '0.75rem' }}>
                      <div>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', flexWrap: 'wrap' }}>
                          <span
                            style={{
                              fontSize: '0.72rem',
                              fontWeight: 800,
                              padding: '0.2rem 0.5rem',
                              borderRadius: 'var(--radius-xs)',
                              backgroundColor: 'var(--gov-navy)',
                              color: '#ffffff',
                            }}
                          >
                            {finding.ruleId}
                          </span>
                          <h4 style={{ fontSize: '0.95rem', fontWeight: 700, color: 'var(--gov-navy)' }}>
                            {finding.ruleName}
                          </h4>
                          <span
                            style={{
                              fontSize: '0.72rem',
                              fontWeight: 700,
                              padding: '0.2rem 0.5rem',
                              borderRadius: 'var(--radius-xs)',
                              backgroundColor: isDeficient ? 'var(--status-danger-bg)' : isUncertain ? 'var(--status-warning-bg)' : 'var(--status-success-bg)',
                              color: isDeficient ? 'var(--status-danger-text)' : isUncertain ? 'var(--status-warning-text)' : 'var(--status-success-text)',
                            }}
                          >
                            {finding.resolved ? 'RESOLVED BY OFFICER' : finding.status}
                          </span>
                        </div>

                        <div style={{ display: 'flex', alignItems: 'center', gap: '0.65rem', marginTop: '0.35rem', color: 'var(--text-muted)', fontSize: '0.78rem' }}>
                          <span>Statutory Citation: <strong>{finding.aphClause}</strong></span>
                          {finding.aphPageNumber && <span>• Printed Page: <strong>{finding.aphPageNumber}</strong></span>}
                          <span>• Severity: <strong>{finding.severity}</strong></span>
                        </div>
                      </div>

                      {/* Resolve Action */}
                      {isDeficient && isOfficerOrAdmin && (
                        <button
                          onClick={() => {
                            setResolvingFinding(finding)
                            setResolutionNotes('')
                            setResolveError(null)
                          }}
                          className="btn btn-outline"
                          style={{
                            fontSize: '0.8rem',
                            fontWeight: 700,
                            borderColor: 'var(--gov-green)',
                            color: 'var(--gov-green)',
                            padding: '0.35rem 0.75rem',
                          }}
                        >
                          Resolve Deficiency
                        </button>
                      )}
                    </div>

                    {/* Explanation */}
                    <p style={{ fontSize: '0.85rem', color: 'var(--text-primary)', lineHeight: 1.45 }}>
                      {finding.explanation}
                    </p>

                    {/* Evidence Snippet */}
                    {finding.evidenceText && (
                      <div
                        style={{
                          backgroundColor: 'var(--bg-surface-secondary)',
                          padding: '0.5rem 0.75rem',
                          borderRadius: 'var(--radius-xs)',
                          border: '1px solid var(--border-light)',
                          fontSize: '0.78rem',
                          color: 'var(--text-secondary)',
                          fontFamily: 'monospace',
                        }}
                      >
                        Evidence: {finding.evidenceText}
                      </div>
                    )}

                    {/* Resolution Notes Banner */}
                    {finding.resolved && (
                      <div
                        style={{
                          backgroundColor: 'var(--status-success-bg)',
                          color: 'var(--status-success-text)',
                          padding: '0.5rem 0.75rem',
                          borderRadius: 'var(--radius-xs)',
                          fontSize: '0.78rem',
                          display: 'flex',
                          alignItems: 'center',
                          gap: '0.4rem',
                        }}
                      >
                        <ShieldCheck size={14} />
                        <span>
                          Resolved by <strong>{finding.resolvedByName || 'Authorized Officer'}</strong>: "{finding.resolutionNotes}"
                        </span>
                      </div>
                    )}
                  </div>
                )
              })
            )}
          </div>
        </div>
      )}

      {/* Tab 3: Workflow & Transition Audit Trail */}
      {activeTab === 'workflow' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
          <div
            style={{
              backgroundColor: 'var(--bg-surface)',
              borderRadius: 'var(--radius-md)',
              border: '1px solid var(--border-medium)',
              overflow: 'hidden',
            }}
          >
            <div style={{ padding: '1rem', borderBottom: '1px solid var(--border-light)' }}>
              <h3 style={{ fontSize: '1rem', fontWeight: 700, color: 'var(--gov-navy)' }}>
                Immutable State Transition Audit Trail
              </h3>
              <p style={{ color: 'var(--text-secondary)', fontSize: '0.8rem', marginTop: '0.2rem' }}>
                Every consequential statutory status change is cryptographically audited and recorded with actor provenance.
              </p>
            </div>

            <div style={{ overflowX: 'auto' }}>
              <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.85rem' }}>
                <thead>
                  <tr
                    style={{
                      backgroundColor: 'var(--bg-surface-secondary)',
                      borderBottom: '1px solid var(--border-medium)',
                      color: 'var(--text-muted)',
                      fontWeight: 700,
                      textTransform: 'uppercase',
                      fontSize: '0.72rem',
                    }}
                  >
                    <th style={{ padding: '0.75rem 1rem' }}>Timestamp</th>
                    <th style={{ padding: '0.75rem 1rem' }}>Transition</th>
                    <th style={{ padding: '0.75rem 1rem' }}>Actor</th>
                    <th style={{ padding: '0.75rem 1rem' }}>Statutory Role</th>
                    <th style={{ padding: '0.75rem 1rem' }}>Reason / Notes</th>
                  </tr>
                </thead>
                <tbody>
                  {transitions.map((t) => (
                    <tr key={t.id} style={{ borderBottom: '1px solid var(--border-light)' }}>
                      <td style={{ padding: '0.75rem 1rem', color: 'var(--text-muted)', fontSize: '0.8rem' }}>
                        {new Date(t.createdAt).toLocaleString()}
                      </td>
                      <td style={{ padding: '0.75rem 1rem', fontWeight: 700 }}>
                        <span style={{ color: 'var(--text-secondary)' }}>{t.fromState}</span>
                        <span style={{ margin: '0 0.4rem', color: 'var(--gov-orange)' }}>→</span>
                        <span style={{ color: 'var(--gov-navy)' }}>{t.toState}</span>
                      </td>
                      <td style={{ padding: '0.75rem 1rem', color: 'var(--text-primary)' }}>
                        {t.actorName}
                      </td>
                      <td style={{ padding: '0.75rem 1rem', color: 'var(--text-secondary)' }}>
                        {t.actorRole}
                      </td>
                      <td style={{ padding: '0.75rem 1rem', color: 'var(--text-secondary)', fontSize: '0.82rem' }}>
                        {t.reason || 'Standard statutory workflow progression'}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      )}

      {/* Resolve Deficiency Modal */}
      <Modal
        isOpen={!!resolvingFinding}
        onClose={() => setResolvingFinding(null)}
        title="Resolve Regulatory Scrutiny Finding"
      >
        <form onSubmit={handleResolveSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          {resolveError && (
            <div style={{ padding: '0.75rem', backgroundColor: 'var(--status-danger-bg)', color: 'var(--status-danger-text)', borderRadius: 'var(--radius-sm)', fontSize: '0.85rem' }}>
              {resolveError}
            </div>
          )}

          <div>
            <div style={{ fontSize: '0.82rem', fontWeight: 700, color: 'var(--gov-navy)', marginBottom: '0.25rem' }}>
              Deficiency: {resolvingFinding?.ruleName} ({resolvingFinding?.ruleId})
            </div>
            <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
              {resolvingFinding?.explanation}
            </p>
          </div>

          <div>
            <label style={{ display: 'block', fontSize: '0.82rem', fontWeight: 600, marginBottom: '0.35rem' }}>
              Statutory Justification / Resolution Notes (Mandatory for Audit Trail)
            </label>
            <textarea
              rows={3}
              className="form-control"
              value={resolutionNotes}
              onChange={(e) => setResolutionNotes(e.target.value)}
              placeholder="e.g. Physical original certificate inspected during committee sitting; verified valid NOC number."
              required
            />
          </div>

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '0.5rem' }}>
            <button type="button" onClick={() => setResolvingFinding(null)} className="btn btn-outline">
              Cancel
            </button>
            <button type="submit" disabled={resolveFindingMutation.isPending} className="btn btn-primary">
              {resolveFindingMutation.isPending ? 'Resolving...' : 'Confirm Statutory Resolution'}
            </button>
          </div>
        </form>
      </Modal>

      {/* Advance Workflow Modal */}
      <Modal
        isOpen={isTransitionModalOpen}
        onClose={() => setIsTransitionModalOpen(false)}
        title="Execute Statutory Workflow State Transition"
      >
        <form onSubmit={handleTransitionSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          {transitionError && (
            <div style={{ padding: '0.75rem', backgroundColor: 'var(--status-danger-bg)', color: 'var(--status-danger-text)', borderRadius: 'var(--radius-sm)', fontSize: '0.85rem' }}>
              {transitionError}
            </div>
          )}

          <div>
            <label style={{ display: 'block', fontSize: '0.82rem', fontWeight: 600, marginBottom: '0.35rem' }}>
              Target State (Subject to Regulatory Authority)
            </label>
            <select
              value={targetState}
              onChange={(e) => setTargetState(e.target.value)}
              className="form-control"
            >
              <option value="UNDER_SCRUTINY">UNDER_SCRUTINY (Initiate Scrutiny)</option>
              <option value="SCRUTINY_PASSED">SCRUTINY_PASSED (Scrutiny Verified)</option>
              <option value="EVC_SCHEDULED">EVC_SCHEDULED (Schedule Expert Visit)</option>
              <option value="EVC_CONDUCTED">EVC_CONDUCTED (Inspection Complete)</option>
              <option value="SHC_REVIEW">SHC_REVIEW (Standing Hearing Committee)</option>
              <option value="APPROVED">APPROVED (Grant AICTE Approval)</option>
              <option value="REJECTED">REJECTED (Statutory Refusal)</option>
            </select>
          </div>

          <div>
            <label style={{ display: 'block', fontSize: '0.82rem', fontWeight: 600, marginBottom: '0.35rem' }}>
              Justification / Meeting Reference
            </label>
            <textarea
              rows={2}
              className="form-control"
              value={transitionReason}
              onChange={(e) => setTransitionReason(e.target.value)}
              placeholder="e.g. Scrutiny committee meeting recommended EoA approval."
              required
            />
          </div>

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '0.5rem' }}>
            <button type="button" onClick={() => setIsTransitionModalOpen(false)} className="btn btn-outline">
              Cancel
            </button>
            <button type="submit" disabled={transitionMutation.isPending} className="btn btn-primary">
              {transitionMutation.isPending ? 'Executing...' : 'Advance Transition'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  )
}
