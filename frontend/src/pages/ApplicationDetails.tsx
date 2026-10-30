import React, { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import {
  applicationsApi,
  type DocumentDto,
  type ScrutinyFindingDto,
  type CourseDetailsDto,
  type BranchDetailsDto,
  type SubmitEvaluatorAssessmentRequest,
} from '../api/applicationsApi'
import { useAuth } from '../context/AuthContext'
import { useMessage } from '../context/MessageContext'
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
  FileCheck2,
  BookOpen,
  GitBranch,
  Edit3,
  Save,
  XCircle,
  Award,
  CheckCircle,
} from 'lucide-react'

export const ApplicationDetails: React.FC = () => {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const { hasRole, isAdmin } = useAuth()
  const { showSuccess, handleApiError, showConfirmation } = useMessage()

  const [activeTab, setActiveTab] = useState<'specification' | 'documents' | 'scrutiny' | 'workflow'>('specification')

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

  // Evaluator Assessment Modal State
  const [isAssessmentModalOpen, setIsAssessmentModalOpen] = useState<boolean>(false)
  const [assessmentScore, setAssessmentScore] = useState<number>(85)
  const [assessmentRecommendation, setAssessmentRecommendation] = useState<string>('RECOMMENDED_APPROVAL')
  const [assessmentRemarks, setAssessmentRemarks] = useState<string>('')
  const [assessmentError, setAssessmentError] = useState<string | null>(null)

  // Specification Edit States
  const [isEditingCourse, setIsEditingCourse] = useState(false)
  const [courseForm, setCourseForm] = useState<Partial<CourseDetailsDto>>({})

  const [isEditingBranch, setIsEditingBranch] = useState(false)
  const [branchForm, setBranchForm] = useState<Partial<BranchDetailsDto>>({})

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

  // Fetch scrutiny evaluations
  const { data: evaluations = [] } = useQuery({
    queryKey: ['application-evaluations', id],
    queryFn: () => applicationsApi.getEvaluations(id!),
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
      showSuccess('Document Uploaded', 'Document successfully uploaded and queued for regulatory verification.')
    },
    onError: (err: unknown) => {
      handleApiError(err, 'Upload Notice', 'Failed to upload document')
    },
  })

  const reprocessOcrMutation = useMutation({
    mutationFn: (docId: string) => applicationsApi.reprocessOcr(id!, docId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['application-documents', id] })
      showSuccess('Extraction Initiated', 'Re-extraction of document fields has commenced.')
    },
    onError: (err: unknown) => {
      handleApiError(err, 'Extraction Notice', 'Failed to re-extract document fields')
    },
  })

  const evaluateScrutinyMutation = useMutation({
    mutationFn: () => applicationsApi.evaluateScrutiny(id!),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['application-findings', id] })
      queryClient.invalidateQueries({ queryKey: ['application', id] })
      setActiveTab('scrutiny')
      showSuccess('Scrutiny Completed', 'Automated regulatory scrutiny checks have been re-evaluated.')
    },
    onError: (err: unknown) => {
      handleApiError(err, 'Scrutiny Notice', 'Failed to evaluate scrutiny')
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
      showSuccess('Finding Resolved', 'Your resolution justification has been recorded.')
    },
    onError: (err: unknown) => {
      handleApiError(err, 'Resolution Notice', 'Failed to resolve finding')
    },
  })

  const assessMutation = useMutation({
    mutationFn: (data: SubmitEvaluatorAssessmentRequest) =>
      applicationsApi.assessApplication(id!, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['application', id] })
      queryClient.invalidateQueries({ queryKey: ['application-evaluations', id] })
      queryClient.invalidateQueries({ queryKey: ['application-findings', id] })
      queryClient.invalidateQueries({ queryKey: ['application-transitions', id] })
      setIsAssessmentModalOpen(false)
      setAssessmentRemarks('')
      setAssessmentError(null)
      showSuccess('Evaluator Assessment Recorded', 'Structured evaluation score and statutory recommendation committed.')
    },
    onError: (err: unknown) => {
      handleApiError(err, 'Assessment Notice', 'Failed to commit evaluator assessment')
      const msg = err instanceof Error ? err.message : 'Assessment submission failed'
      setAssessmentError(msg)
    },
  })

  const submitAppMutation = useMutation({
    mutationFn: () => applicationsApi.submit(id!),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['application', id] })
      queryClient.invalidateQueries({ queryKey: ['application-transitions', id] })
      showSuccess('Application Submitted', 'Your application has been submitted successfully. You can track its progress from the Applications section.')
    },
    onError: (err: unknown) => {
      handleApiError(err, 'Submission Notice', 'Failed to submit application')
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
      showSuccess('State Transition Completed', 'The application lifecycle state was updated successfully.')
    },
    onError: (err: unknown) => {
      handleApiError(err, 'Workflow Notice', 'Transition could not be executed')
    },
  })

  useEffect(() => {
    if (application) {
      if (application.category === 'NEW_COURSE_INTRODUCTION' && application.courseDetails) {
        setCourseForm(application.courseDetails)
        setActiveTab('specification')
      } else if (application.category === 'NEW_BRANCH_INTRODUCTION' && application.branchDetails) {
        setBranchForm(application.branchDetails)
        setActiveTab('specification')
      } else {
        setActiveTab('documents')
      }
    }
  }, [application])

  const saveCourseMutation = useMutation({
    mutationFn: (data: CourseDetailsDto) => applicationsApi.saveCourseDetails(id!, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['application', id] })
      setIsEditingCourse(false)
    },
  })

  const saveBranchMutation = useMutation({
    mutationFn: (data: BranchDetailsDto) => applicationsApi.saveBranchDetails(id!, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['application', id] })
      setIsEditingBranch(false)
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

  const isOfficerOrAdmin = isAdmin || hasRole('ROLE_REGIONAL_OFFICER') || hasRole('ROLE_COMMITTEE_MEMBER') || hasRole('ROLE_EVC_COORDINATOR')

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

            {application.currentState === 'DEFICIENT' && (hasRole('ROLE_INSTITUTION_OFFICER') || isAdmin) && (
              <button
                onClick={() => transitionMutation.mutate({ state: 'RESUBMITTED', reason: 'Deficiencies rectified and compliance documents updated.' })}
                disabled={transitionMutation.isPending}
                className="btn btn-primary"
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: '0.4rem',
                  backgroundColor: 'var(--gov-green)',
                  color: '#ffffff',
                  fontWeight: 700,
                  fontSize: '0.85rem',
                }}
              >
                <Send size={15} />
                <span>{transitionMutation.isPending ? 'Resubmitting...' : 'Resubmit Rectified Application'}</span>
              </button>
            )}

            {(application.currentState === 'DRAFT' || application.currentState === 'SUBMITTED' || application.currentState === 'DEFICIENT') && (hasRole('ROLE_INSTITUTION_OFFICER') || isAdmin) && (
              <button
                onClick={async () => {
                  const confirmed = await showConfirmation({
                    title: 'Withdraw Application',
                    message: 'Are you sure you want to withdraw this application?',
                    description: 'This will terminate the approval process and mark the application as withdrawn per statutory norms.',
                    confirmVariant: 'danger',
                    confirmText: 'Withdraw',
                  })
                  if (confirmed) {
                    transitionMutation.mutate({ state: 'WITHDRAWN', reason: 'Application voluntarily withdrawn by applicant.' })
                  }
                }}
                disabled={transitionMutation.isPending}
                className="btn btn-outline"
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: '0.4rem',
                  borderColor: 'var(--status-danger-border)',
                  color: 'var(--status-danger-text)',
                  fontWeight: 600,
                  fontSize: '0.85rem',
                }}
              >
                <XCircle size={15} />
                <span>Withdraw</span>
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
      <div style={{ display: 'flex', gap: '0.5rem', borderBottom: '2px solid var(--border-medium)', flexWrap: 'wrap' }}>
        {application.category === 'NEW_COURSE_INTRODUCTION' && (
          <button
            onClick={() => setActiveTab('specification')}
            style={{
              padding: '0.75rem 1.25rem',
              fontSize: '0.875rem',
              fontWeight: 700,
              border: 'none',
              borderBottom: activeTab === 'specification' ? '3px solid var(--gov-orange)' : '3px solid transparent',
              color: activeTab === 'specification' ? 'var(--gov-navy)' : 'var(--text-secondary)',
              backgroundColor: 'transparent',
              cursor: 'pointer',
              display: 'flex',
              alignItems: 'center',
              gap: '0.4rem',
            }}
          >
            <BookOpen size={16} />
            <span>Course Specification &amp; Labs</span>
          </button>
        )}

        {application.category === 'NEW_BRANCH_INTRODUCTION' && (
          <button
            onClick={() => setActiveTab('specification')}
            style={{
              padding: '0.75rem 1.25rem',
              fontSize: '0.875rem',
              fontWeight: 700,
              border: 'none',
              borderBottom: activeTab === 'specification' ? '3px solid var(--gov-orange)' : '3px solid transparent',
              color: activeTab === 'specification' ? 'var(--gov-navy)' : 'var(--text-secondary)',
              backgroundColor: 'transparent',
              cursor: 'pointer',
              display: 'flex',
              alignItems: 'center',
              gap: '0.4rem',
            }}
          >
            <GitBranch size={16} />
            <span>Branch Specification &amp; Intake</span>
          </button>
        )}

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

      {/* Tab: Course Specification (NEW_COURSE_INTRODUCTION) */}
      {activeTab === 'specification' && application.category === 'NEW_COURSE_INTRODUCTION' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
          <div
            style={{
              backgroundColor: 'var(--bg-surface)',
              borderRadius: 'var(--radius-md)',
              border: '1px solid var(--border-medium)',
              padding: '1.5rem',
              display: 'flex',
              flexDirection: 'column',
              gap: '1.25rem',
            }}
          >
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '1rem' }}>
              <div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                  <h3 style={{ fontSize: '1.15rem', fontWeight: 800, color: 'var(--gov-navy)' }}>
                    {application.courseDetails?.courseName || 'Course Specification Pending'}
                  </h3>
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
                    {application.courseDetails?.programmeLevel || 'UNDERGRADUATE'}
                  </span>
                  <span
                    style={{
                      fontSize: '0.72rem',
                      fontWeight: 700,
                      padding: '0.2rem 0.5rem',
                      borderRadius: 'var(--radius-xs)',
                      backgroundColor: '#f3e8ff',
                      color: '#6b21a8',
                    }}
                  >
                    {application.courseDetails?.degreeType || 'BACHELOR_OF_TECHNOLOGY'}
                  </span>
                </div>
                <p style={{ color: 'var(--text-secondary)', fontSize: '0.85rem', marginTop: '0.25rem' }}>
                  Discipline: <strong>{application.courseDetails?.courseDiscipline || 'ENGINEERING_AND_TECHNOLOGY'}</strong> • Department: <strong>{application.courseDetails?.proposedDepartment || 'Computer Science & Engineering'}</strong>
                </p>
              </div>

              {application.currentState === 'DRAFT' && (
                <button
                  onClick={() => setIsEditingCourse(!isEditingCourse)}
                  className="btn btn-outline"
                  style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', fontSize: '0.82rem', fontWeight: 700 }}
                >
                  <Edit3 size={14} />
                  <span>{isEditingCourse ? 'Cancel Editing' : 'Edit Specification'}</span>
                </button>
              )}
            </div>

            {isEditingCourse ? (
              <form
                onSubmit={(e) => {
                  e.preventDefault()
                  saveCourseMutation.mutate(courseForm as CourseDetailsDto)
                }}
                style={{ display: 'flex', flexDirection: 'column', gap: '1rem', borderTop: '1px solid var(--border-light)', paddingTop: '1rem' }}
              >
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1rem' }}>
                  <div>
                    <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 600, marginBottom: '0.25rem' }}>Course Name</label>
                    <input
                      type="text"
                      className="form-control"
                      value={courseForm.courseName || ''}
                      onChange={(e) => setCourseForm({ ...courseForm, courseName: e.target.value })}
                      required
                    />
                  </div>
                  <div>
                    <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 600, marginBottom: '0.25rem' }}>Proposed Intake</label>
                    <input
                      type="number"
                      className="form-control"
                      value={courseForm.proposedIntake || 60}
                      onChange={(e) => setCourseForm({ ...courseForm, proposedIntake: parseInt(e.target.value) || 0 })}
                      required
                    />
                  </div>
                  <div>
                    <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 600, marginBottom: '0.25rem' }}>Duration (Years)</label>
                    <input
                      type="number"
                      className="form-control"
                      value={courseForm.durationYears || 4}
                      onChange={(e) => setCourseForm({ ...courseForm, durationYears: parseInt(e.target.value) || 0 })}
                    />
                  </div>
                  <div>
                    <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 600, marginBottom: '0.25rem' }}>Laboratories Setup</label>
                    <input
                      type="number"
                      className="form-control"
                      value={courseForm.laboratoriesCount || 4}
                      onChange={(e) => setCourseForm({ ...courseForm, laboratoriesCount: parseInt(e.target.value) || 0 })}
                    />
                  </div>
                  <div>
                    <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 600, marginBottom: '0.25rem' }}>Professors</label>
                    <input
                      type="number"
                      className="form-control"
                      value={courseForm.professorsCount || 1}
                      onChange={(e) => setCourseForm({ ...courseForm, professorsCount: parseInt(e.target.value) || 0 })}
                    />
                  </div>
                  <div>
                    <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 600, marginBottom: '0.25rem' }}>Associate Professors</label>
                    <input
                      type="number"
                      className="form-control"
                      value={courseForm.associateProfessorsCount || 2}
                      onChange={(e) => setCourseForm({ ...courseForm, associateProfessorsCount: parseInt(e.target.value) || 0 })}
                    />
                  </div>
                  <div>
                    <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 600, marginBottom: '0.25rem' }}>Assistant Professors</label>
                    <input
                      type="number"
                      className="form-control"
                      value={courseForm.assistantProfessorsCount || 6}
                      onChange={(e) => setCourseForm({ ...courseForm, assistantProfessorsCount: parseInt(e.target.value) || 0 })}
                    />
                  </div>
                  <div>
                    <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 600, marginBottom: '0.25rem' }}>Classrooms Available</label>
                    <input
                      type="number"
                      className="form-control"
                      value={courseForm.availableClassrooms || 3}
                      onChange={(e) => setCourseForm({ ...courseForm, availableClassrooms: parseInt(e.target.value) || 0 })}
                    />
                  </div>
                </div>

                <div>
                  <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 600, marginBottom: '0.25rem' }}>Laboratory Equipment Details</label>
                  <textarea
                    rows={2}
                    className="form-control"
                    value={courseForm.laboratoryDetails || ''}
                    onChange={(e) => setCourseForm({ ...courseForm, laboratoryDetails: e.target.value })}
                  />
                </div>

                <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem' }}>
                  <button type="button" onClick={() => setIsEditingCourse(false)} className="btn btn-outline">
                    Cancel
                  </button>
                  <button type="submit" disabled={saveCourseMutation.isPending} className="btn btn-primary" style={{ backgroundColor: 'var(--gov-orange)', color: '#ffffff', display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
                    <Save size={15} />
                    <span>{saveCourseMutation.isPending ? 'Saving...' : 'Save Course Details'}</span>
                  </button>
                </div>
              </form>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '1rem' }}>
                  <div style={{ padding: '0.75rem', backgroundColor: 'var(--bg-surface-secondary)', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border-light)' }}>
                    <span style={{ fontSize: '0.72rem', color: 'var(--text-muted)', fontWeight: 600 }}>PROPOSED INTAKE</span>
                    <div style={{ fontSize: '1.25rem', fontWeight: 800, color: 'var(--gov-navy)' }}>
                      {application.courseDetails?.proposedIntake || 0} Seats
                    </div>
                  </div>
                  <div style={{ padding: '0.75rem', backgroundColor: 'var(--bg-surface-secondary)', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border-light)' }}>
                    <span style={{ fontSize: '0.72rem', color: 'var(--text-muted)', fontWeight: 600 }}>DURATION &amp; DELIVERY</span>
                    <div style={{ fontSize: '1.25rem', fontWeight: 800, color: 'var(--gov-navy)' }}>
                      {application.courseDetails?.durationYears || 4} Years ({application.courseDetails?.modeOfDelivery || 'REGULAR'})
                    </div>
                  </div>
                  <div style={{ padding: '0.75rem', backgroundColor: 'var(--bg-surface-secondary)', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border-light)' }}>
                    <span style={{ fontSize: '0.72rem', color: 'var(--text-muted)', fontWeight: 600 }}>LABORATORIES SETUP</span>
                    <div style={{ fontSize: '1.25rem', fontWeight: 800, color: 'var(--gov-navy)' }}>
                      {application.courseDetails?.laboratoriesCount || 0} Dedicated Labs
                    </div>
                  </div>
                  <div style={{ padding: '0.75rem', backgroundColor: 'var(--bg-surface-secondary)', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border-light)' }}>
                    <span style={{ fontSize: '0.72rem', color: 'var(--text-muted)', fontWeight: 600 }}>FACULTY IDENTIFIED</span>
                    <div style={{ fontSize: '1.25rem', fontWeight: 800, color: 'var(--gov-navy)' }}>
                      {(application.courseDetails?.professorsCount || 0) + (application.courseDetails?.associateProfessorsCount || 0) + (application.courseDetails?.assistantProfessorsCount || 0)} Faculty Members
                    </div>
                  </div>
                </div>

                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '1rem' }}>
                  <div style={{ padding: '1rem', border: '1px solid var(--border-light)', borderRadius: 'var(--radius-sm)' }}>
                    <h4 style={{ fontSize: '0.85rem', fontWeight: 700, color: 'var(--gov-navy)', marginBottom: '0.5rem' }}>
                      Faculty Cadre Ratio Breakdown
                    </h4>
                    <p style={{ fontSize: '0.82rem', color: 'var(--text-secondary)' }}>
                      Professors: <strong>{application.courseDetails?.professorsCount || 0}</strong> • Associate Professors: <strong>{application.courseDetails?.associateProfessorsCount || 0}</strong> • Assistant Professors: <strong>{application.courseDetails?.assistantProfessorsCount || 0}</strong>
                    </p>
                    <p style={{ fontSize: '0.8rem', color: 'var(--gov-green)', fontWeight: 600, marginTop: '0.25rem' }}>
                      Cadre Ratio Status: {application.courseDetails?.facultyStudentRatio || 'Compliant with AICTE 1:20 norm'}
                    </p>
                  </div>

                  <div style={{ padding: '1rem', border: '1px solid var(--border-light)', borderRadius: 'var(--radius-sm)' }}>
                    <h4 style={{ fontSize: '0.85rem', fontWeight: 700, color: 'var(--gov-navy)', marginBottom: '0.5rem' }}>
                      Infrastructure &amp; Lab Notes
                    </h4>
                    <p style={{ fontSize: '0.82rem', color: 'var(--text-secondary)' }}>
                      {application.courseDetails?.laboratoryDetails || 'Specialized laboratories with state-of-the-art workstations and equipment.'}
                    </p>
                  </div>
                </div>
              </div>
            )}
          </div>
        </div>
      )}

      {/* Tab: Branch Specification (NEW_BRANCH_INTRODUCTION) */}
      {activeTab === 'specification' && application.category === 'NEW_BRANCH_INTRODUCTION' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
          <div
            style={{
              backgroundColor: 'var(--bg-surface)',
              borderRadius: 'var(--radius-md)',
              border: '1px solid var(--border-medium)',
              padding: '1.5rem',
              display: 'flex',
              flexDirection: 'column',
              gap: '1.25rem',
            }}
          >
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '1rem' }}>
              <div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                  <h3 style={{ fontSize: '1.15rem', fontWeight: 800, color: 'var(--gov-navy)' }}>
                    {application.branchDetails?.proposedBranchName || 'Branch Specification Pending'}
                  </h3>
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
                    Parent: {application.branchDetails?.existingProgramme || 'ENGINEERING_AND_TECHNOLOGY'}
                  </span>
                </div>
                <p style={{ color: 'var(--text-secondary)', fontSize: '0.85rem', marginTop: '0.25rem' }}>
                  Existing Department: <strong>{application.branchDetails?.existingDepartment || 'Information Technology'}</strong>
                </p>
              </div>

              {application.currentState === 'DRAFT' && (
                <button
                  onClick={() => setIsEditingBranch(!isEditingBranch)}
                  className="btn btn-outline"
                  style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', fontSize: '0.82rem', fontWeight: 700 }}
                >
                  <Edit3 size={14} />
                  <span>{isEditingBranch ? 'Cancel Editing' : 'Edit Specification'}</span>
                </button>
              )}
            </div>

            {isEditingBranch ? (
              <form
                onSubmit={(e) => {
                  e.preventDefault()
                  saveBranchMutation.mutate(branchForm as BranchDetailsDto)
                }}
                style={{ display: 'flex', flexDirection: 'column', gap: '1rem', borderTop: '1px solid var(--border-light)', paddingTop: '1rem' }}
              >
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1rem' }}>
                  <div>
                    <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 600, marginBottom: '0.25rem' }}>Proposed Branch Name</label>
                    <input
                      type="text"
                      className="form-control"
                      value={branchForm.proposedBranchName || ''}
                      onChange={(e) => setBranchForm({ ...branchForm, proposedBranchName: e.target.value })}
                      required
                    />
                  </div>
                  <div>
                    <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 600, marginBottom: '0.25rem' }}>Proposed Branch Intake</label>
                    <input
                      type="number"
                      className="form-control"
                      value={branchForm.proposedIntake || 60}
                      onChange={(e) => setBranchForm({ ...branchForm, proposedIntake: parseInt(e.target.value) || 0 })}
                      required
                    />
                  </div>
                  <div>
                    <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 600, marginBottom: '0.25rem' }}>Existing Approved Intake</label>
                    <input
                      type="number"
                      className="form-control"
                      value={branchForm.existingApprovedIntake || 120}
                      onChange={(e) => setBranchForm({ ...branchForm, existingApprovedIntake: parseInt(e.target.value) || 0 })}
                    />
                  </div>
                  <div>
                    <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 600, marginBottom: '0.25rem' }}>Core Faculty Count</label>
                    <input
                      type="number"
                      className="form-control"
                      value={branchForm.facultyCount || 4}
                      onChange={(e) => setBranchForm({ ...branchForm, facultyCount: parseInt(e.target.value) || 0 })}
                    />
                  </div>
                  <div>
                    <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 600, marginBottom: '0.25rem' }}>Laboratories Available</label>
                    <input
                      type="number"
                      className="form-control"
                      value={branchForm.laboratoryCount || 2}
                      onChange={(e) => setBranchForm({ ...branchForm, laboratoryCount: parseInt(e.target.value) || 0 })}
                    />
                  </div>
                </div>

                <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem' }}>
                  <button type="button" onClick={() => setIsEditingBranch(false)} className="btn btn-outline">
                    Cancel
                  </button>
                  <button type="submit" disabled={saveBranchMutation.isPending} className="btn btn-primary" style={{ backgroundColor: 'var(--gov-orange)', color: '#ffffff', display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
                    <Save size={15} />
                    <span>{saveBranchMutation.isPending ? 'Saving...' : 'Save Branch Details'}</span>
                  </button>
                </div>
              </form>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '1rem' }}>
                  <div style={{ padding: '0.75rem', backgroundColor: 'var(--bg-surface-secondary)', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border-light)' }}>
                    <span style={{ fontSize: '0.72rem', color: 'var(--text-muted)', fontWeight: 600 }}>PROPOSED BRANCH INTAKE</span>
                    <div style={{ fontSize: '1.25rem', fontWeight: 800, color: 'var(--gov-navy)' }}>
                      {application.branchDetails?.proposedIntake || 0} Seats
                    </div>
                  </div>
                  <div style={{ padding: '0.75rem', backgroundColor: 'var(--bg-surface-secondary)', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border-light)' }}>
                    <span style={{ fontSize: '0.72rem', color: 'var(--text-muted)', fontWeight: 600 }}>EXISTING PARENT INTAKE</span>
                    <div style={{ fontSize: '1.25rem', fontWeight: 800, color: 'var(--gov-navy)' }}>
                      {application.branchDetails?.existingApprovedIntake || 0} Seats
                    </div>
                  </div>
                  <div style={{ padding: '0.75rem', backgroundColor: 'var(--bg-surface-secondary)', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border-light)' }}>
                    <span style={{ fontSize: '0.72rem', color: 'var(--text-muted)', fontWeight: 600 }}>CORE FACULTY ALLOCATED</span>
                    <div style={{ fontSize: '1.25rem', fontWeight: 800, color: 'var(--gov-navy)' }}>
                      {application.branchDetails?.facultyCount || 0} Faculty Members
                    </div>
                  </div>
                  <div style={{ padding: '0.75rem', backgroundColor: 'var(--bg-surface-secondary)', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border-light)' }}>
                    <span style={{ fontSize: '0.72rem', color: 'var(--text-muted)', fontWeight: 600 }}>SPECIALIZED LABS</span>
                    <div style={{ fontSize: '1.25rem', fontWeight: 800, color: 'var(--gov-navy)' }}>
                      {application.branchDetails?.laboratoryCount || 0} Laboratories
                    </div>
                  </div>
                </div>
              </div>
            )}
          </div>
        </div>
      )}

      {/* Tab: Documents & OCR */}
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
                  <option value="FACULTY_LIST_QUALIFICATION">Faculty Cadre &amp; Qualification Roster (APH 2024)</option>
                  <option value="LABORATORY_INFRASTRUCTURE_REPORT">Laboratory Setup &amp; Carpet Area Report</option>
                  <option value="EQUIPMENT_UTILIZATION_CERTIFICATE">Equipment Stock &amp; Utilization Certificate</option>
                  <option value="BUILDING_PLAN_APPROVAL">Municipal Building Plan Approval</option>
                  <option value="FINANCIAL_AUDIT_REPORT">Audited Financial Statements (Last 3 Yrs)</option>
                  <option value="SYLLABUS_CURRICULUM_APPROVAL">University Syllabus / Curriculum Endorsement</option>
                  <option value="OTHER_SUPPORTING">Other Supporting Regulatory Document</option>
                </select>
              </div>

              <div style={{ flex: '1.5', minWidth: '250px' }}>
                <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 600, marginBottom: '0.35rem' }}>
                  Select Document File (PDF, PNG, JPEG up to 15MB)
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
                <span>{uploadMutation.isPending ? 'Verifying Document...' : 'Upload & Verify'}</span>
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
                  No documents uploaded yet. Upload mandatory certificates above to initiate regulatory verification.
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
                        Document Hash: {doc.sha256Hash ? doc.sha256Hash.substring(0, 16) + '...' : 'Verified'} • {(doc.fileSizeBytes / 1024).toFixed(1)} KB
                      </p>
                    </div>

                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                      {/* Automated Verification Badge */}
                      <div
                        style={{
                          display: 'flex',
                          alignItems: 'center',
                          gap: '0.35rem',
                          padding: '0.3rem 0.6rem',
                          borderRadius: 'var(--radius-xs)',
                          backgroundColor: '#f1f5f9',
                          color: 'var(--gov-navy)',
                          fontSize: '0.75rem',
                          fontWeight: 700,
                        }}
                      >
                        <ShieldCheck size={14} color="var(--gov-blue)" />
                        <span>Automated Verification</span>
                      </div>

                      {/* Extraction Confidence Badge */}
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
                        title="Re-extract Document Details"
                        className="btn btn-outline"
                        style={{ padding: '0.3rem 0.6rem', fontSize: '0.75rem', display: 'flex', alignItems: 'center', gap: '0.3rem' }}
                      >
                        <RefreshCw size={12} />
                        <span>Re-extract</span>
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
                        Extracted Regulatory Fields (with Document Provenance)
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

            <div style={{ display: 'flex', gap: '0.6rem', flexWrap: 'wrap' }}>
              {isOfficerOrAdmin && (
                <button
                  type="button"
                  onClick={() => {
                    const latest = evaluations && evaluations.length > 0 ? evaluations[0] : null
                    setAssessmentScore(latest?.score ?? 85)
                    setAssessmentRecommendation(latest?.recommendation ?? 'RECOMMENDED_APPROVAL')
                    setAssessmentRemarks(latest?.remarks ?? '')
                    setAssessmentError(null)
                    setIsAssessmentModalOpen(true)
                  }}
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
                  id="btn-conduct-evaluator-assessment"
                >
                  <Award size={15} />
                  <span>{evaluations && evaluations.length > 0 && evaluations[0].score != null ? 'Update Evaluator Assessment' : 'Conduct Evaluator Assessment'}</span>
                </button>
              )}
              <button
                type="button"
                onClick={() => evaluateScrutinyMutation.mutate()}
                disabled={evaluateScrutinyMutation.isPending}
                className="btn btn-outline"
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: '0.4rem',
                  backgroundColor: 'var(--bg-surface)',
                  color: 'var(--gov-blue)',
                  borderColor: 'var(--gov-blue)',
                  fontWeight: 700,
                  fontSize: '0.85rem',
                }}
              >
                <Cpu size={15} />
                <span>{evaluateScrutinyMutation.isPending ? 'Evaluating Rules...' : 'Re-evaluate All Rules'}</span>
              </button>
            </div>
          </div>

          {/* Evaluator Assessment Summary Card */}
          {evaluations && evaluations.length > 0 && evaluations[0].score != null && (
            <div
              style={{
                backgroundColor: 'var(--bg-surface)',
                padding: '1.25rem',
                borderRadius: 'var(--radius-md)',
                border: '1px solid var(--border-medium)',
                display: 'flex',
                flexDirection: 'column',
                gap: '0.75rem',
              }}
            >
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '0.5rem' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                  <Award size={20} color="var(--gov-orange)" />
                  <div>
                    <h4 style={{ fontSize: '0.95rem', fontWeight: 700, color: 'var(--gov-navy)' }}>
                      Structured Evaluator Assessment
                    </h4>
                    <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                      Evaluated by {evaluations[0].evaluatedByName} &bull; {new Date(evaluations[0].evaluatedAt).toLocaleString()}
                    </span>
                  </div>
                </div>

                <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                  <div style={{ textAlign: 'right' }}>
                    <div style={{ fontSize: '0.7rem', color: 'var(--text-muted)', fontWeight: 600 }}>COMPOSITE SCORE</div>
                    <div style={{ fontSize: '1.35rem', fontWeight: 800, color: evaluations[0].score >= 75 ? 'var(--gov-green)' : evaluations[0].score >= 50 ? 'var(--gov-orange)' : 'var(--gov-red)' }}>
                      {evaluations[0].score} / 100
                    </div>
                  </div>
                  <span
                    style={{
                      padding: '0.3rem 0.65rem',
                      borderRadius: 'var(--radius-xs)',
                      fontSize: '0.75rem',
                      fontWeight: 700,
                      backgroundColor:
                        evaluations[0].recommendation === 'RECOMMENDED_APPROVAL'
                          ? 'var(--status-success-bg)'
                          : evaluations[0].recommendation === 'DEFICIENCY_RECTIFICATION'
                          ? 'var(--status-danger-bg)'
                          : 'var(--gov-blue-light)',
                      color:
                        evaluations[0].recommendation === 'RECOMMENDED_APPROVAL'
                          ? 'var(--status-success-text)'
                          : evaluations[0].recommendation === 'DEFICIENCY_RECTIFICATION'
                          ? 'var(--status-danger-text)'
                          : 'var(--gov-blue)',
                    }}
                  >
                    {evaluations[0].recommendation?.replace(/_/g, ' ')}
                  </span>
                </div>
              </div>

              {/* Score Progress Bar */}
              <div style={{ width: '100%', height: '8px', backgroundColor: 'var(--border-light)', borderRadius: '4px', overflow: 'hidden' }}>
                <div
                  style={{
                    width: `${Math.min(100, Math.max(0, evaluations[0].score))}%`,
                    height: '100%',
                    backgroundColor: evaluations[0].score >= 75 ? 'var(--gov-green)' : evaluations[0].score >= 50 ? 'var(--gov-orange)' : 'var(--gov-red)',
                    transition: 'width 0.3s ease',
                  }}
                />
              </div>

              {/* Remarks */}
              {evaluations[0].remarks && (
                <div style={{ fontSize: '0.82rem', color: 'var(--text-primary)', backgroundColor: 'var(--bg-surface-secondary)', padding: '0.65rem 0.85rem', borderRadius: 'var(--radius-sm)' }}>
                  <strong>Evaluator Remarks:</strong> {evaluations[0].remarks}
                </div>
              )}
            </div>
          )}

          {/* EVC Inspection Notice if Scheduled */}
          {application.currentState === 'EVC_SCHEDULED' && (
            <div
              style={{
                backgroundColor: 'var(--gov-blue-light)',
                border: '1px solid var(--gov-blue)',
                padding: '0.85rem 1.15rem',
                borderRadius: 'var(--radius-md)',
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
                flexWrap: 'wrap',
                gap: '0.75rem',
              }}
            >
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <Clock size={18} color="var(--gov-blue)" />
                <span style={{ fontSize: '0.85rem', fontWeight: 600, color: 'var(--gov-navy)' }}>
                  Expert Visit Committee (EVC) Inspection has been scheduled for this institution.
                </span>
              </div>
              <button
                type="button"
                onClick={() => navigate('/evc-scheduling')}
                style={{
                  padding: '0.35rem 0.85rem',
                  border: 'none',
                  backgroundColor: 'var(--gov-blue)',
                  color: '#ffffff',
                  borderRadius: 'var(--radius-sm)',
                  fontSize: '0.8rem',
                  fontWeight: 600,
                  cursor: 'pointer',
                }}
              >
                Go to EVC Inspection Portal &rarr;
              </button>
            </div>
          )}

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
              <option value="DEFICIENT">DEFICIENT (Deficiencies Flagged)</option>
              <option value="SCRUTINY_PASSED">SCRUTINY_PASSED (Scrutiny Verified)</option>
              <option value="EVC_SCHEDULED">EVC_SCHEDULED (Schedule Expert Visit)</option>
              <option value="EVC_CONDUCTED">EVC_CONDUCTED (Inspection Complete)</option>
              <option value="SHC_SCHEDULED">SHC_SCHEDULED (Schedule Hearing)</option>
              <option value="SHC_REVIEW">SHC_REVIEW (Standing Hearing Committee)</option>
              <option value="SAC_REVIEW">SAC_REVIEW (Standing Appellate Committee)</option>
              <option value="APPROVED">APPROVED (Grant AICTE Approval)</option>
              <option value="REJECTED">REJECTED (Statutory Refusal)</option>
              <option value="WITHDRAWN">WITHDRAWN (Statutory Withdrawal)</option>
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

      {/* Evaluator Assessment Modal */}
      {isAssessmentModalOpen && (
        <Modal
          isOpen={isAssessmentModalOpen}
          onClose={() => setIsAssessmentModalOpen(false)}
          title={`Conduct Evaluator Assessment: ${application?.applicationNumber}`}
        >
          <form
            onSubmit={(e) => {
              e.preventDefault()
              if (!assessmentRemarks.trim()) {
                setAssessmentError('Please enter evaluative remarks and statutory justification.')
                return
              }
              assessMutation.mutate({
                score: assessmentScore,
                recommendation: assessmentRecommendation,
                remarks: assessmentRemarks.trim(),
                summary: `Evaluator assessment: ${assessmentRecommendation} (${assessmentScore}/100). ${assessmentRemarks.trim()}`,
              })
            }}
            style={{ display: 'flex', flexDirection: 'column', gap: '1rem', fontSize: '0.85rem' }}
          >
            {assessmentError && (
              <div style={{ padding: '0.65rem 0.85rem', backgroundColor: 'var(--status-danger-bg)', color: 'var(--status-danger-text)', borderRadius: 'var(--radius-sm)', fontSize: '0.8rem', display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
                <AlertTriangle size={15} />
                <span>{assessmentError}</span>
              </div>
            )}

            {/* Criteria Info Banner */}
            <div style={{ padding: '0.75rem', backgroundColor: 'var(--bg-surface-secondary)', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border-light)' }}>
              <span style={{ fontWeight: 700, color: 'var(--gov-navy)', fontSize: '0.8rem' }}>APH Statutory Scoring &amp; Scrutiny Matrix</span>
              <ul style={{ margin: '0.35rem 0 0 1.15rem', padding: 0, fontSize: '0.76rem', color: 'var(--text-secondary)', lineHeight: 1.4 }}>
                <li>Document Authenticity &amp; OCR Verification (25 pts)</li>
                <li>Physical Infrastructure, Carpet Area &amp; Laboratories (25 pts)</li>
                <li>Faculty Cadre Ratios (1:20 norm) &amp; Professor Appointments (25 pts)</li>
                <li>Land Title, Fire Safety &amp; Municipal Clearances (25 pts)</li>
              </ul>
            </div>

            <div>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.35rem' }}>
                <label style={{ fontWeight: 600, color: 'var(--gov-navy)' }}>
                  Evaluator Composite Compliance Score (0–100) *
                </label>
                <span style={{ fontSize: '1.1rem', fontWeight: 800, color: assessmentScore >= 75 ? 'var(--gov-green)' : assessmentScore >= 50 ? 'var(--gov-orange)' : 'var(--gov-red)' }}>
                  {assessmentScore} / 100
                </span>
              </div>
              <input
                type="range"
                min="0"
                max="100"
                step="1"
                value={assessmentScore}
                onChange={(e) => setAssessmentScore(parseInt(e.target.value, 10))}
                style={{ width: '100%', cursor: 'pointer' }}
              />
            </div>

            <div>
              <label style={{ display: 'block', fontWeight: 600, color: 'var(--gov-navy)', marginBottom: '0.25rem' }}>
                Statutory Recommendation *
              </label>
              <select
                value={assessmentRecommendation}
                onChange={(e) => setAssessmentRecommendation(e.target.value)}
                className="form-control"
                style={{ fontSize: '0.85rem' }}
              >
                <option value="RECOMMENDED_APPROVAL">RECOMMENDED_APPROVAL — Cleared Scrutiny for Final Approval</option>
                <option value="RECOMMENDED_EVC_INSPECTION">RECOMMENDED_EVC_INSPECTION — Forward to Expert Visit Committee</option>
                <option value="DEFICIENCY_RECTIFICATION">DEFICIENCY_RECTIFICATION — Deficiencies Flagged; Applicant Must Rectify</option>
                <option value="REJECTED">REJECTED — Severe Non-Compliance; Recommend Refusal</option>
              </select>
            </div>

            <div>
              <label style={{ display: 'block', fontWeight: 600, color: 'var(--gov-navy)', marginBottom: '0.25rem' }}>
                Evaluator Remarks &amp; Statutory Justification *
              </label>
              <textarea
                rows={3}
                className="form-control"
                value={assessmentRemarks}
                onChange={(e) => setAssessmentRemarks(e.target.value)}
                placeholder="Enter detailed assessment observations, committee recommendations, and APH compliance rationale..."
                required
                style={{ fontSize: '0.82rem', fontFamily: 'inherit' }}
              />
            </div>

            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.65rem', marginTop: '0.5rem' }}>
              <button
                type="button"
                onClick={() => setIsAssessmentModalOpen(false)}
                disabled={assessMutation.isPending}
                className="btn btn-outline"
                style={{ fontSize: '0.8rem' }}
              >
                Cancel
              </button>
              <button
                type="submit"
                disabled={assessMutation.isPending}
                className="btn btn-primary"
                style={{
                  backgroundColor: 'var(--gov-orange)',
                  color: '#ffffff',
                  fontWeight: 700,
                  fontSize: '0.8rem',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '0.35rem',
                }}
              >
                {assessMutation.isPending ? (
                  <>
                    <RefreshCw size={14} className="spin" />
                    <span>Committing Assessment...</span>
                  </>
                ) : (
                  <>
                    <CheckCircle size={14} />
                    <span>Commit Evaluator Assessment</span>
                  </>
                )}
              </button>
            </div>
          </form>
        </Modal>
      )}
    </div>
  )
}
