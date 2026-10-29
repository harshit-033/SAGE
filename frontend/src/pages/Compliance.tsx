import React, { useState, useEffect } from 'react'
import {
  ShieldCheck,
  Calendar,
  AlertTriangle,
  Play,
  CheckCircle2,
  AlertCircle,
  RefreshCw,
  History,
  Building2,
  Check,
} from 'lucide-react'
import {
  deadlinesApi,
  type DeadlineDto,
  type DeadlineHistoryDto,
  type DeadlineStatus,
} from '../api/deadlinesApi'
import {
  complianceApi,
  type ComplianceFindingDto,
  type ComplianceCheckRunDto,
} from '../api/complianceApi'
import { useAuth } from '../context/AuthContext'
import { Modal } from '../components/common/Modal'

export const Compliance: React.FC = () => {
  const { isAdmin, hasRole } = useAuth()

  // Active Tab: 'deadlines' | 'findings' | 'runs'
  const [activeTab, setActiveTab] = useState<'deadlines' | 'findings' | 'runs'>('deadlines')

  // Data states
  const [deadlines, setDeadlines] = useState<DeadlineDto[]>([])
  const [findings, setFindings] = useState<ComplianceFindingDto[]>([])
  const [runs, setRuns] = useState<ComplianceCheckRunDto[]>([])

  // Loaders & feedback
  const [loading, setLoading] = useState<boolean>(true)
  const [scanRunning, setScanRunning] = useState<boolean>(false)
  const [errorMsg, setErrorMsg] = useState<string | null>(null)
  const [successMsg, setSuccessMsg] = useState<string | null>(null)

  // Modals state: Recalculate Deadline
  const [recalcModalOpen, setRecalcModalOpen] = useState<boolean>(false)
  const [selectedDeadline, setSelectedDeadline] = useState<DeadlineDto | null>(null)
  const [newTargetDate, setNewTargetDate] = useState<string>('')
  const [recalcReason, setRecalcReason] = useState<string>('')

  // Modals state: Resolve Deadline
  const [resolveDeadlineModalOpen, setResolveDeadlineModalOpen] = useState<boolean>(false)
  const [deadlineResolutionNotes, setDeadlineResolutionNotes] = useState<string>('')

  // Modals state: Deadline History
  const [historyModalOpen, setHistoryModalOpen] = useState<boolean>(false)
  const [deadlineHistory, setDeadlineHistory] = useState<DeadlineHistoryDto[]>([])
  const [historyLoading, setHistoryLoading] = useState<boolean>(false)

  // Modals state: Resolve Finding
  const [resolveFindingModalOpen, setResolveFindingModalOpen] = useState<boolean>(false)
  const [selectedFinding, setSelectedFinding] = useState<ComplianceFindingDto | null>(null)
  const [findingResolutionNotes, setFindingResolutionNotes] = useState<string>('')

  // Action loader
  const [actionLoading, setActionLoading] = useState<boolean>(false)

  // Load all data
  const loadData = async () => {
    try {
      setLoading(true)
      setErrorMsg(null)
      const [deadlinesData, findingsData, runsData] = await Promise.all([
        deadlinesApi.list({ size: 50, sortBy: 'targetDate', sortDir: 'asc' }),
        complianceApi.getFindings({ size: 50 }),
        complianceApi.getCheckRuns({ size: 20 }),
      ])
      setDeadlines(deadlinesData.content)
      setFindings(findingsData.content)
      setRuns(runsData.content)
    } catch (err: unknown) {
      setErrorMsg((err as Error).message || 'Failed to load compliance data.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadData()
  }, [])

  // Trigger continuous compliance scan
  const handleTriggerScan = async () => {
    try {
      setScanRunning(true)
      setErrorMsg(null)
      setSuccessMsg(null)
      const run = await complianceApi.triggerScan()
      setSuccessMsg(
        `Continuous compliance scan executed! Scanned ${run.institutionsChecked} institution(s), identified ${run.findingsGenerated} findings.`
      )
      await loadData()
    } catch (err: unknown) {
      setErrorMsg((err as Error).message || 'Compliance scan failed.')
    } finally {
      setScanRunning(false)
    }
  }

  // Recalculate Deadline
  const handleRecalculateDeadline = async () => {
    if (!selectedDeadline) return
    if (!newTargetDate) {
      setErrorMsg('Please select a new target date.')
      return
    }
    if (!recalcReason.trim()) {
      setErrorMsg('Mandatory statutory justification reason is required.')
      return
    }

    try {
      setActionLoading(true)
      setErrorMsg(null)
      const isoDate = new Date(newTargetDate).toISOString()
      await deadlinesApi.recalculate(selectedDeadline.id, {
        newTargetDate: isoDate,
        reason: recalcReason.trim(),
      })
      setSuccessMsg(`Deadline ${selectedDeadline.deadlineCode} recalculated successfully.`)
      setRecalcModalOpen(false)
      setSelectedDeadline(null)
      setNewTargetDate('')
      setRecalcReason('')
      await loadData()
    } catch (err: unknown) {
      setErrorMsg((err as Error).message || 'Failed to recalculate deadline.')
    } finally {
      setActionLoading(false)
    }
  }

  // Resolve Deadline
  const handleResolveDeadline = async () => {
    if (!selectedDeadline) return
    if (!deadlineResolutionNotes.trim()) {
      setErrorMsg('Please enter statutory resolution notes.')
      return
    }

    try {
      setActionLoading(true)
      setErrorMsg(null)
      await deadlinesApi.resolve(selectedDeadline.id, {
        resolutionNotes: deadlineResolutionNotes.trim(),
      })
      setSuccessMsg(`Deadline ${selectedDeadline.deadlineCode} marked as resolved.`)
      setResolveDeadlineModalOpen(false)
      setSelectedDeadline(null)
      setDeadlineResolutionNotes('')
      await loadData()
    } catch (err: unknown) {
      setErrorMsg((err as Error).message || 'Failed to resolve deadline.')
    } finally {
      setActionLoading(false)
    }
  }

  // View Deadline History
  const handleViewHistory = async (deadline: DeadlineDto) => {
    setSelectedDeadline(deadline)
    setHistoryModalOpen(true)
    setHistoryLoading(true)
    try {
      const hist = await deadlinesApi.getHistory(deadline.id)
      setDeadlineHistory(hist)
    } catch (err: unknown) {
      setErrorMsg((err as Error).message || 'Failed to fetch deadline audit history.')
    } finally {
      setHistoryLoading(false)
    }
  }

  // Resolve Compliance Finding
  const handleResolveFinding = async () => {
    if (!selectedFinding) return
    if (!findingResolutionNotes.trim()) {
      setErrorMsg('Please provide statutory resolution notes.')
      return
    }

    try {
      setActionLoading(true)
      setErrorMsg(null)
      await complianceApi.resolveFinding(selectedFinding.id, {
        resolutionNotes: findingResolutionNotes.trim(),
      })
      setSuccessMsg(`Compliance finding resolved successfully.`)
      setResolveFindingModalOpen(false)
      setSelectedFinding(null)
      setFindingResolutionNotes('')
      await loadData()
    } catch (err: unknown) {
      setErrorMsg((err as Error).message || 'Failed to resolve compliance finding.')
    } finally {
      setActionLoading(false)
    }
  }

  // Status badge helpers
  const getDeadlineStatusBadge = (status: DeadlineStatus, targetDate: string) => {
    const target = new Date(targetDate).getTime()
    const now = new Date().getTime()
    const daysRemaining = Math.ceil((target - now) / (1000 * 60 * 60 * 24))

    switch (status) {
      case 'OVERDUE':
        return (
          <span
            style={{
              padding: '0.2rem 0.55rem',
              borderRadius: 'var(--radius-sm)',
              fontSize: '0.72rem',
              fontWeight: 700,
              backgroundColor: '#fee2e2',
              color: '#991b1b',
              border: '1px solid #f87171',
            }}
          >
            ● OVERDUE ({Math.abs(daysRemaining)}d ago)
          </span>
        )
      case 'DUE_TODAY':
        return (
          <span
            style={{
              padding: '0.2rem 0.55rem',
              borderRadius: 'var(--radius-sm)',
              fontSize: '0.72rem',
              fontWeight: 700,
              backgroundColor: '#fef2f2',
              color: '#dc2626',
              border: '1px solid #fca5a5',
            }}
          >
            ! DUE TODAY
          </span>
        )
      case 'DUE_SOON':
        return (
          <span
            style={{
              padding: '0.2rem 0.55rem',
              borderRadius: 'var(--radius-sm)',
              fontSize: '0.72rem',
              fontWeight: 700,
              backgroundColor: '#fef3c7',
              color: '#92400e',
              border: '1px solid #fcd34d',
            }}
          >
            DUE SOON ({daysRemaining}d left)
          </span>
        )
      case 'RESOLVED':
        return (
          <span
            style={{
              padding: '0.2rem 0.55rem',
              borderRadius: 'var(--radius-sm)',
              fontSize: '0.72rem',
              fontWeight: 700,
              backgroundColor: '#dcfce7',
              color: '#15803d',
              border: '1px solid #86efac',
            }}
          >
            ✓ RESOLVED
          </span>
        )
      case 'PENDING':
      default:
        return (
          <span
            style={{
              padding: '0.2rem 0.55rem',
              borderRadius: 'var(--radius-sm)',
              fontSize: '0.72rem',
              fontWeight: 600,
              backgroundColor: '#e0f2fe',
              color: '#0369a1',
              border: '1px solid #7dd3fc',
            }}
          >
            PENDING ({daysRemaining}d left)
          </span>
        )
    }
  }

  // Counts
  const openFindingsCount = findings.filter((f) => f.status === 'OPEN').length
  const criticalFindingsCount = findings.filter(
    (f) => f.status === 'OPEN' && f.severity === 'CRITICAL'
  ).length
  const urgentDeadlinesCount = deadlines.filter(
    (d) => d.status === 'OVERDUE' || d.status === 'DUE_TODAY' || d.status === 'DUE_SOON'
  ).length

  return (
    <div style={{ maxWidth: '1240px', margin: '0 auto', display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
      {/* Header Banner */}
      <div
        style={{
          backgroundColor: 'var(--bg-surface)',
          padding: '1.5rem 1.75rem',
          borderRadius: 'var(--radius-md)',
          border: '1px solid var(--border-medium)',
          boxShadow: 'var(--shadow-sm)',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'flex-start',
          flexWrap: 'wrap',
          gap: '1rem',
        }}
      >
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.65rem' }}>
            <div
              style={{
                width: '36px',
                height: '36px',
                borderRadius: 'var(--radius-sm)',
                backgroundColor: 'var(--gov-blue-light)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                color: 'var(--gov-blue)',
              }}
            >
              <ShieldCheck size={22} />
            </div>
            <div>
              <h1 style={{ fontSize: '1.35rem', fontWeight: 700, color: 'var(--gov-navy)', margin: 0 }}>
                Continuous Compliance Monitoring &amp; Deadline Engine
              </h1>
              <p style={{ margin: '0.2rem 0 0 0', fontSize: '0.85rem', color: 'var(--text-muted)' }}>
                Continuous surveillance of statutory validities, fire safety renewals, and APH 2024–27 deadline progression.
              </p>
            </div>
          </div>
        </div>

        {/* Scan Trigger Button */}
        {(isAdmin || hasRole('ROLE_COMPLIANCE_OFFICER')) && (
          <button
            type="button"
            className="btn btn-primary btn-sm"
            onClick={handleTriggerScan}
            disabled={scanRunning}
            style={{ display: 'inline-flex', alignItems: 'center', gap: '0.45rem' }}
          >
            {scanRunning ? (
              <>
                <RefreshCw size={14} className="animate-spin" />
                <span>Running Scan...</span>
              </>
            ) : (
              <>
                <Play size={14} />
                <span>Trigger Compliance Scan</span>
              </>
            )}
          </button>
        )}
      </div>

      {/* Alerts / Error Banners */}
      {errorMsg && (
        <div
          style={{
            padding: '0.85rem 1.25rem',
            backgroundColor: '#fef2f2',
            border: '1px solid #f87171',
            borderRadius: 'var(--radius-sm)',
            color: '#991b1b',
            fontSize: '0.88rem',
            display: 'flex',
            alignItems: 'center',
            gap: '0.5rem',
          }}
        >
          <AlertCircle size={18} />
          <span>{errorMsg}</span>
        </div>
      )}

      {successMsg && (
        <div
          style={{
            padding: '0.85rem 1.25rem',
            backgroundColor: '#f0fdf4',
            border: '1px solid #86efac',
            borderRadius: 'var(--radius-sm)',
            color: '#166534',
            fontSize: '0.88rem',
            display: 'flex',
            alignItems: 'center',
            gap: '0.5rem',
          }}
        >
          <CheckCircle2 size={18} />
          <span>{successMsg}</span>
        </div>
      )}

      {/* Stat Cards */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '1rem' }}>
        <div
          style={{
            backgroundColor: 'var(--bg-surface)',
            padding: '1.25rem',
            borderRadius: 'var(--radius-md)',
            border: '1px solid var(--border-medium)',
            boxShadow: 'var(--shadow-xs)',
          }}
        >
          <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)', fontWeight: 600, textTransform: 'uppercase' }}>
            Open Compliance Findings
          </div>
          <div style={{ fontSize: '1.75rem', fontWeight: 800, color: 'var(--gov-navy)', marginTop: '0.25rem' }}>
            {openFindingsCount}
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '0.35rem' }}>
            Active documentary &amp; physical defects
          </div>
        </div>

        <div
          style={{
            backgroundColor: 'var(--bg-surface)',
            padding: '1.25rem',
            borderRadius: 'var(--radius-md)',
            border: '1px solid #fecaca',
            borderLeft: '4px solid #ef4444',
            boxShadow: 'var(--shadow-xs)',
          }}
        >
          <div style={{ fontSize: '0.78rem', color: '#991b1b', fontWeight: 600, textTransform: 'uppercase' }}>
            Critical Failures
          </div>
          <div style={{ fontSize: '1.75rem', fontWeight: 800, color: '#b91c1c', marginTop: '0.25rem' }}>
            {criticalFindingsCount}
          </div>
          <div style={{ fontSize: '0.75rem', color: '#b91c1c', marginTop: '0.35rem' }}>
            Requires immediate rectification
          </div>
        </div>

        <div
          style={{
            backgroundColor: 'var(--bg-surface)',
            padding: '1.25rem',
            borderRadius: 'var(--radius-md)',
            border: '1px solid #fed7aa',
            borderLeft: '4px solid #f97316',
            boxShadow: 'var(--shadow-xs)',
          }}
        >
          <div style={{ fontSize: '0.78rem', color: '#9a3412', fontWeight: 600, textTransform: 'uppercase' }}>
            Urgent Statutory Deadlines
          </div>
          <div style={{ fontSize: '1.75rem', fontWeight: 800, color: '#c2410c', marginTop: '0.25rem' }}>
            {urgentDeadlinesCount}
          </div>
          <div style={{ fontSize: '0.75rem', color: '#9a3412', marginTop: '0.35rem' }}>
            Due within 15 days or overdue
          </div>
        </div>

        <div
          style={{
            backgroundColor: 'var(--bg-surface)',
            padding: '1.25rem',
            borderRadius: 'var(--radius-md)',
            border: '1px solid var(--border-medium)',
            boxShadow: 'var(--shadow-xs)',
          }}
        >
          <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)', fontWeight: 600, textTransform: 'uppercase' }}>
            Total Monitored Runs
          </div>
          <div style={{ fontSize: '1.75rem', fontWeight: 800, color: 'var(--gov-navy)', marginTop: '0.25rem' }}>
            {runs.length}
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '0.35rem' }}>
            Periodic &amp; manual audit scans
          </div>
        </div>
      </div>

      {/* Tabs */}
      <div style={{ display: 'flex', borderBottom: '2px solid var(--border-medium)', gap: '0.5rem' }}>
        <button
          type="button"
          onClick={() => setActiveTab('deadlines')}
          style={{
            padding: '0.75rem 1.25rem',
            fontSize: '0.9rem',
            fontWeight: 700,
            color: activeTab === 'deadlines' ? 'var(--gov-blue)' : 'var(--text-muted)',
            backgroundColor: 'transparent',
            border: 'none',
            borderBottom: activeTab === 'deadlines' ? '3px solid var(--gov-blue)' : '3px solid transparent',
            marginBottom: '-2px',
            cursor: 'pointer',
            display: 'inline-flex',
            alignItems: 'center',
            gap: '0.45rem',
          }}
        >
          <Calendar size={16} />
          <span>Statutory Deadlines ({deadlines.length})</span>
        </button>

        <button
          type="button"
          onClick={() => setActiveTab('findings')}
          style={{
            padding: '0.75rem 1.25rem',
            fontSize: '0.9rem',
            fontWeight: 700,
            color: activeTab === 'findings' ? 'var(--gov-blue)' : 'var(--text-muted)',
            backgroundColor: 'transparent',
            border: 'none',
            borderBottom: activeTab === 'findings' ? '3px solid var(--gov-blue)' : '3px solid transparent',
            marginBottom: '-2px',
            cursor: 'pointer',
            display: 'inline-flex',
            alignItems: 'center',
            gap: '0.45rem',
          }}
        >
          <AlertTriangle size={16} />
          <span>Compliance Findings ({findings.length})</span>
        </button>

        <button
          type="button"
          onClick={() => setActiveTab('runs')}
          style={{
            padding: '0.75rem 1.25rem',
            fontSize: '0.9rem',
            fontWeight: 700,
            color: activeTab === 'runs' ? 'var(--gov-blue)' : 'var(--text-muted)',
            backgroundColor: 'transparent',
            border: 'none',
            borderBottom: activeTab === 'runs' ? '3px solid var(--gov-blue)' : '3px solid transparent',
            marginBottom: '-2px',
            cursor: 'pointer',
            display: 'inline-flex',
            alignItems: 'center',
            gap: '0.45rem',
          }}
        >
          <History size={16} />
          <span>Scan Audit Runs ({runs.length})</span>
        </button>
      </div>

      {loading ? (
        <div
          style={{
            padding: '3rem',
            textAlign: 'center',
            backgroundColor: 'var(--bg-surface)',
            borderRadius: 'var(--radius-md)',
            border: '1px solid var(--border-medium)',
            color: 'var(--text-muted)',
          }}
        >
          <RefreshCw size={24} className="animate-spin" style={{ margin: '0 auto 0.75rem auto' }} />
          <p>Loading statutory compliance ledger...</p>
        </div>
      ) : (
        <>
          {/* Tab 1: Deadlines */}
          {activeTab === 'deadlines' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '0.85rem' }}>
          {deadlines.length === 0 ? (
            <div
              style={{
                padding: '3rem',
                textAlign: 'center',
                backgroundColor: 'var(--bg-surface)',
                borderRadius: 'var(--radius-md)',
                border: '1px solid var(--border-medium)',
                color: 'var(--text-muted)',
              }}
            >
              <Calendar size={32} style={{ margin: '0 auto 0.75rem auto' }} />
              <p>No statutory deadlines recorded in current tenancy scope.</p>
            </div>
          ) : (
            deadlines.map((dl) => (
              <div
                key={dl.id}
                style={{
                  backgroundColor: 'var(--bg-surface)',
                  borderRadius: 'var(--radius-md)',
                  border: '1px solid var(--border-medium)',
                  boxShadow: 'var(--shadow-xs)',
                  padding: '1.25rem 1.5rem',
                  display: 'flex',
                  flexDirection: 'column',
                  gap: '0.85rem',
                }}
              >
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '0.5rem' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.65rem' }}>
                    {getDeadlineStatusBadge(dl.status, dl.targetDate)}
                    <span
                      style={{
                        padding: '0.15rem 0.5rem',
                        borderRadius: 'var(--radius-sm)',
                        fontSize: '0.7rem',
                        fontWeight: 600,
                        backgroundColor: 'var(--bg-surface-secondary)',
                        color: 'var(--text-secondary)',
                      }}
                    >
                      {dl.deadlineType}
                    </span>
                    {dl.statutoryClause && (
                      <span
                        style={{
                          padding: '0.15rem 0.5rem',
                          borderRadius: 'var(--radius-sm)',
                          fontSize: '0.7rem',
                          fontWeight: 700,
                          backgroundColor: '#f1f5f9',
                          color: 'var(--gov-navy)',
                          border: '1px solid var(--border-medium)',
                        }}
                      >
                        {dl.statutoryClause}
                      </span>
                    )}
                  </div>

                  <div style={{ fontSize: '0.82rem', color: 'var(--text-muted)' }}>
                    Target: <strong style={{ color: 'var(--text-primary)' }}>{new Date(dl.targetDate).toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' })}</strong>
                  </div>
                </div>

                <div>
                  <h3 style={{ fontSize: '1rem', fontWeight: 700, color: 'var(--gov-navy)', margin: '0 0 0.35rem 0' }}>
                    {dl.title}
                  </h3>
                  <p style={{ margin: 0, fontSize: '0.88rem', color: 'var(--text-secondary)', lineHeight: 1.45 }}>
                    {dl.description}
                  </p>
                </div>

                <div
                  style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    fontSize: '0.78rem',
                    color: 'var(--text-muted)',
                    borderTop: '1px solid var(--border-light)',
                    paddingTop: '0.65rem',
                    flexWrap: 'wrap',
                    gap: '0.5rem',
                  }}
                >
                  <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
                    {dl.institutionName && (
                      <span style={{ display: 'inline-flex', alignItems: 'center', gap: '0.35rem' }}>
                        <Building2 size={13} />
                        <strong>{dl.institutionName}</strong>
                      </span>
                    )}
                    <span style={{ fontFamily: 'var(--font-mono)', fontSize: '0.72rem' }}>
                      {dl.deadlineCode}
                    </span>
                  </div>

                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                    <button
                      type="button"
                      className="btn btn-secondary btn-sm"
                      onClick={() => handleViewHistory(dl)}
                      style={{ display: 'inline-flex', alignItems: 'center', gap: '0.3rem', fontSize: '0.75rem' }}
                    >
                      <History size={13} />
                      <span>Audit History</span>
                    </button>

                    {dl.status !== 'RESOLVED' && (
                      <>
                        <button
                          type="button"
                          className="btn btn-secondary btn-sm"
                          onClick={() => {
                            setSelectedDeadline(dl)
                            setNewTargetDate(dl.targetDate.slice(0, 10))
                            setRecalcModalOpen(true)
                          }}
                          style={{ fontSize: '0.75rem' }}
                        >
                          Recalculate Date
                        </button>
                        <button
                          type="button"
                          className="btn btn-primary btn-sm"
                          onClick={() => {
                            setSelectedDeadline(dl)
                            setResolveDeadlineModalOpen(true)
                          }}
                          style={{ fontSize: '0.75rem', display: 'inline-flex', alignItems: 'center', gap: '0.3rem' }}
                        >
                          <Check size={13} />
                          <span>Resolve</span>
                        </button>
                      </>
                    )}
                  </div>
                </div>

                {dl.resolutionNotes && (
                  <div
                    style={{
                      backgroundColor: '#f0fdf4',
                      padding: '0.5rem 0.75rem',
                      borderRadius: 'var(--radius-sm)',
                      fontSize: '0.78rem',
                      color: '#166534',
                      borderLeft: '3px solid #16a34a',
                    }}
                  >
                    <strong>Resolved by {dl.resolvedByName}:</strong> {dl.resolutionNotes}
                  </div>
                )}
              </div>
            ))
          )}
        </div>
      )}

      {/* Tab 2: Findings */}
      {activeTab === 'findings' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '0.85rem' }}>
          {findings.length === 0 ? (
            <div
              style={{
                padding: '3rem',
                textAlign: 'center',
                backgroundColor: 'var(--bg-surface)',
                borderRadius: 'var(--radius-md)',
                border: '1px solid var(--border-medium)',
                color: 'var(--text-muted)',
              }}
            >
              <CheckCircle2 size={32} color="var(--gov-green)" style={{ margin: '0 auto 0.75rem auto' }} />
              <p>No continuous compliance findings recorded. All institutional records are in compliance.</p>
            </div>
          ) : (
            findings.map((f) => (
              <div
                key={f.id}
                style={{
                  backgroundColor: 'var(--bg-surface)',
                  borderRadius: 'var(--radius-md)',
                  border: '1px solid var(--border-medium)',
                  borderLeft:
                    f.severity === 'CRITICAL'
                      ? '4px solid #ef4444'
                      : f.severity === 'WARNING'
                      ? '4px solid #f59e0b'
                      : '4px solid #3b82f6',
                  boxShadow: 'var(--shadow-xs)',
                  padding: '1.25rem 1.5rem',
                  display: 'flex',
                  flexDirection: 'column',
                  gap: '0.75rem',
                }}
              >
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '0.5rem' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.65rem' }}>
                    <span
                      style={{
                        padding: '0.15rem 0.5rem',
                        borderRadius: 'var(--radius-sm)',
                        fontSize: '0.72rem',
                        fontWeight: 700,
                        backgroundColor: f.severity === 'CRITICAL' ? '#fee2e2' : '#fef3c7',
                        color: f.severity === 'CRITICAL' ? '#991b1b' : '#92400e',
                      }}
                    >
                      {f.severity}
                    </span>
                    <span
                      style={{
                        padding: '0.15rem 0.5rem',
                        borderRadius: 'var(--radius-sm)',
                        fontSize: '0.7rem',
                        fontWeight: 600,
                        backgroundColor: 'var(--bg-surface-secondary)',
                        color: 'var(--text-secondary)',
                      }}
                    >
                      {f.findingType}
                    </span>
                    <span
                      style={{
                        padding: '0.15rem 0.5rem',
                        borderRadius: 'var(--radius-sm)',
                        fontSize: '0.7rem',
                        fontWeight: 700,
                        backgroundColor: '#f1f5f9',
                        color: 'var(--gov-navy)',
                        border: '1px solid var(--border-medium)',
                      }}
                    >
                      {f.aphClause}
                    </span>
                  </div>

                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                    <span
                      style={{
                        padding: '0.15rem 0.5rem',
                        borderRadius: 'var(--radius-sm)',
                        fontSize: '0.72rem',
                        fontWeight: 700,
                        backgroundColor: f.status === 'RESOLVED' ? '#dcfce7' : '#fee2e2',
                        color: f.status === 'RESOLVED' ? '#15803d' : '#991b1b',
                      }}
                    >
                      {f.status}
                    </span>
                  </div>
                </div>

                <div>
                  <h3 style={{ fontSize: '1rem', fontWeight: 700, color: 'var(--gov-navy)', margin: '0 0 0.35rem 0' }}>
                    {f.title}
                  </h3>
                  <p style={{ margin: 0, fontSize: '0.88rem', color: 'var(--text-secondary)' }}>
                    {f.description}
                  </p>
                </div>

                {f.evidenceSummary && (
                  <div
                    style={{
                      backgroundColor: 'var(--bg-surface-secondary)',
                      padding: '0.6rem 0.85rem',
                      borderRadius: 'var(--radius-sm)',
                      fontSize: '0.8rem',
                      color: 'var(--text-secondary)',
                    }}
                  >
                    <strong>Observed Regulatory Evidence:</strong> {f.evidenceSummary}
                  </div>
                )}

                <div
                  style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    fontSize: '0.78rem',
                    color: 'var(--text-muted)',
                    borderTop: '1px solid var(--border-light)',
                    paddingTop: '0.65rem',
                    flexWrap: 'wrap',
                    gap: '0.5rem',
                  }}
                >
                  <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
                    <span>
                      Institution: <strong>{f.institutionName}</strong>
                    </span>
                    {f.applicationNumber && (
                      <span>
                        Application: <strong>{f.applicationNumber}</strong>
                      </span>
                    )}
                    <span style={{ fontFamily: 'var(--font-mono)', fontSize: '0.72rem' }}>
                      Rule: {f.ruleCode}
                    </span>
                  </div>

                  {f.status !== 'RESOLVED' && (
                    <button
                      type="button"
                      className="btn btn-secondary btn-sm"
                      onClick={() => {
                        setSelectedFinding(f)
                        setResolveFindingModalOpen(true)
                      }}
                      style={{ fontSize: '0.75rem', display: 'inline-flex', alignItems: 'center', gap: '0.3rem' }}
                    >
                      <CheckCircle2 size={13} />
                      <span>Resolve Finding</span>
                    </button>
                  )}
                </div>

                {f.resolutionNotes && (
                  <div
                    style={{
                      backgroundColor: '#f0fdf4',
                      padding: '0.5rem 0.75rem',
                      borderRadius: 'var(--radius-sm)',
                      fontSize: '0.78rem',
                      color: '#166534',
                      borderLeft: '3px solid #16a34a',
                    }}
                  >
                    <strong>Resolved by {f.resolvedByName}:</strong> {f.resolutionNotes}
                  </div>
                )}
              </div>
            ))
          )}
        </div>
      )}

      {/* Tab 3: Scan Audit Runs */}
      {activeTab === 'runs' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '0.85rem' }}>
          {runs.map((r) => (
            <div
              key={r.id}
              style={{
                backgroundColor: 'var(--bg-surface)',
                borderRadius: 'var(--radius-md)',
                border: '1px solid var(--border-medium)',
                boxShadow: 'var(--shadow-xs)',
                padding: '1.25rem 1.5rem',
                display: 'flex',
                flexDirection: 'column',
                gap: '0.65rem',
              }}
            >
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.65rem' }}>
                  <span
                    style={{
                      padding: '0.2rem 0.55rem',
                      borderRadius: 'var(--radius-sm)',
                      fontSize: '0.72rem',
                      fontWeight: 700,
                      backgroundColor: r.status === 'SUCCESS' ? '#dcfce7' : '#fee2e2',
                      color: r.status === 'SUCCESS' ? '#15803d' : '#991b1b',
                    }}
                  >
                    {r.status}
                  </span>
                  <span style={{ fontSize: '0.85rem', fontWeight: 700, color: 'var(--gov-navy)' }}>
                    {r.runType} SCAN
                  </span>
                  <span style={{ fontSize: '0.78rem', color: 'var(--text-muted)' }}>
                    Triggered by: {r.triggerSource}
                  </span>
                </div>

                <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                  {new Date(r.startedAt).toLocaleString('en-IN')}
                </div>
              </div>

              <div style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>
                {r.summaryNotes}
              </div>

              <div
                style={{
                  display: 'flex',
                  gap: '1.5rem',
                  fontSize: '0.78rem',
                  color: 'var(--text-muted)',
                  borderTop: '1px solid var(--border-light)',
                  paddingTop: '0.5rem',
                }}
              >
                <span>
                  Institutions Checked: <strong>{r.institutionsChecked}</strong>
                </span>
                <span>
                  Findings Generated: <strong>{r.findingsGenerated}</strong>
                </span>
                <span>
                  Alerts Triggered: <strong>{r.alertsCreated}</strong>
                </span>
              </div>
            </div>
          ))}
        </div>
      )}
        </>
      )}

      {/* Recalculate Deadline Modal */}
      {recalcModalOpen && selectedDeadline && (
        <Modal
          title={`Recalculate Statutory Deadline: ${selectedDeadline.deadlineCode}`}
          isOpen={recalcModalOpen}
          onClose={() => {
            setRecalcModalOpen(false)
            setSelectedDeadline(null)
          }}
        >
          <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            <p style={{ fontSize: '0.88rem', color: 'var(--text-secondary)', margin: 0 }}>
              Adjust the statutory compliance target date. Every modification creates a permanent audit record tracking the old date, new date, reason, and officer identity.
            </p>

            <div>
              <label style={{ display: 'block', fontSize: '0.82rem', fontWeight: 600, marginBottom: '0.35rem' }}>
                New Target Date <span style={{ color: '#ef4444' }}>*</span>
              </label>
              <input
                type="date"
                className="form-control"
                value={newTargetDate}
                onChange={(e) => setNewTargetDate(e.target.value)}
                style={{ width: '100%', fontSize: '0.85rem' }}
              />
            </div>

            <div>
              <label style={{ display: 'block', fontSize: '0.82rem', fontWeight: 600, marginBottom: '0.35rem' }}>
                Statutory Justification Reason <span style={{ color: '#ef4444' }}>*</span>
              </label>
              <textarea
                className="form-control"
                rows={3}
                placeholder="E.g., AICTE public notice notification extension, administrative delay in regional hearing, etc."
                value={recalcReason}
                onChange={(e) => setRecalcReason(e.target.value)}
                style={{ width: '100%', fontSize: '0.85rem' }}
              />
            </div>

            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '0.5rem' }}>
              <button
                type="button"
                className="btn btn-secondary btn-sm"
                onClick={() => setRecalcModalOpen(false)}
              >
                Cancel
              </button>
              <button
                type="button"
                className="btn btn-primary btn-sm"
                disabled={actionLoading || !newTargetDate || !recalcReason.trim()}
                onClick={handleRecalculateDeadline}
              >
                {actionLoading ? 'Updating...' : 'Save Recalculation'}
              </button>
            </div>
          </div>
        </Modal>
      )}

      {/* Resolve Deadline Modal */}
      {resolveDeadlineModalOpen && selectedDeadline && (
        <Modal
          title={`Resolve Deadline: ${selectedDeadline.deadlineCode}`}
          isOpen={resolveDeadlineModalOpen}
          onClose={() => {
            setResolveDeadlineModalOpen(false)
            setSelectedDeadline(null)
          }}
        >
          <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            <p style={{ fontSize: '0.88rem', color: 'var(--text-secondary)', margin: 0 }}>
              Confirm statutory compliance fulfillment. This transitions the deadline to RESOLVED and records resolution proof in the audit trail.
            </p>

            <div>
              <label style={{ display: 'block', fontSize: '0.82rem', fontWeight: 600, marginBottom: '0.35rem' }}>
                Resolution Findings / Verification Remarks <span style={{ color: '#ef4444' }}>*</span>
              </label>
              <textarea
                className="form-control"
                rows={3}
                placeholder="Detail verification results, submitted documents, or completed hearing..."
                value={deadlineResolutionNotes}
                onChange={(e) => setDeadlineResolutionNotes(e.target.value)}
                style={{ width: '100%', fontSize: '0.85rem' }}
              />
            </div>

            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '0.5rem' }}>
              <button
                type="button"
                className="btn btn-secondary btn-sm"
                onClick={() => setResolveDeadlineModalOpen(false)}
              >
                Cancel
              </button>
              <button
                type="button"
                className="btn btn-primary btn-sm"
                disabled={actionLoading || !deadlineResolutionNotes.trim()}
                onClick={handleResolveDeadline}
              >
                {actionLoading ? 'Resolving...' : 'Confirm Deadline Resolution'}
              </button>
            </div>
          </div>
        </Modal>
      )}

      {/* Deadline History Modal */}
      {historyModalOpen && selectedDeadline && (
        <Modal
          title={`Audit History: ${selectedDeadline.deadlineCode}`}
          isOpen={historyModalOpen}
          onClose={() => {
            setHistoryModalOpen(false)
            setSelectedDeadline(null)
            setDeadlineHistory([])
          }}
        >
          <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            <p style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', margin: 0 }}>
              Chronological ledger of statutory target date recalculations, justification reasons, and officer signatures.
            </p>

            {historyLoading ? (
              <div style={{ textAlign: 'center', padding: '1.5rem', color: 'var(--text-muted)' }}>
                Loading audit trail...
              </div>
            ) : deadlineHistory.length === 0 ? (
              <div style={{ textAlign: 'center', padding: '1.5rem', color: 'var(--text-muted)' }}>
                No recalculations on record. Original target date has remained unchanged.
              </div>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.65rem' }}>
                {deadlineHistory.map((h) => (
                  <div
                    key={h.id}
                    style={{
                      padding: '0.75rem',
                      borderRadius: 'var(--radius-sm)',
                      backgroundColor: 'var(--bg-surface-secondary)',
                      border: '1px solid var(--border-medium)',
                      display: 'flex',
                      flexDirection: 'column',
                      gap: '0.35rem',
                    }}
                  >
                    <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                      <span>Changed by: <strong>{h.changedByName}</strong></span>
                      <span>{new Date(h.changedAt).toLocaleString('en-IN')}</span>
                    </div>
                    <div style={{ fontSize: '0.82rem', color: 'var(--text-primary)' }}>
                      From: <span style={{ textDecoration: 'line-through' }}>{new Date(h.previousTargetDate).toLocaleDateString('en-IN')}</span> → To: <strong>{new Date(h.newTargetDate).toLocaleDateString('en-IN')}</strong>
                    </div>
                    <div style={{ fontSize: '0.78rem', color: 'var(--text-secondary)' }}>
                      <em>"{h.reason}"</em>
                    </div>
                  </div>
                ))}
              </div>
            )}

            <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: '0.5rem' }}>
              <button
                type="button"
                className="btn btn-secondary btn-sm"
                onClick={() => setHistoryModalOpen(false)}
              >
                Close
              </button>
            </div>
          </div>
        </Modal>
      )}

      {/* Resolve Compliance Finding Modal */}
      {resolveFindingModalOpen && selectedFinding && (
        <Modal
          title="Resolve Compliance Finding"
          isOpen={resolveFindingModalOpen}
          onClose={() => {
            setResolveFindingModalOpen(false)
            setSelectedFinding(null)
          }}
        >
          <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            <p style={{ fontSize: '0.88rem', color: 'var(--text-secondary)', margin: 0 }}>
              Confirm rectification of statutory finding <strong>{selectedFinding.title}</strong> under {selectedFinding.aphClause}.
            </p>

            <div>
              <label style={{ display: 'block', fontSize: '0.82rem', fontWeight: 600, marginBottom: '0.35rem' }}>
                Verification Summary &amp; Documentary Evidence <span style={{ color: '#ef4444' }}>*</span>
              </label>
              <textarea
                className="form-control"
                rows={3}
                placeholder="Describe verified documentary proof or rectification verification..."
                value={findingResolutionNotes}
                onChange={(e) => setFindingResolutionNotes(e.target.value)}
                style={{ width: '100%', fontSize: '0.85rem' }}
              />
            </div>

            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '0.5rem' }}>
              <button
                type="button"
                className="btn btn-secondary btn-sm"
                onClick={() => setResolveFindingModalOpen(false)}
              >
                Cancel
              </button>
              <button
                type="button"
                className="btn btn-primary btn-sm"
                disabled={actionLoading || !findingResolutionNotes.trim()}
                onClick={handleResolveFinding}
              >
                {actionLoading ? 'Recording...' : 'Confirm Resolution'}
              </button>
            </div>
          </div>
        </Modal>
      )}
    </div>
  )
}
