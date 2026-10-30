import React, { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { evcApi } from '../api/evcApi'
import type { EvcInspectionDto, SchedulingResponseDto } from '../api/evcApi'
import { useAuth } from '../context/AuthContext'
import { StatusBadge } from '../components/common/StatusBadge'
import { Modal } from '../components/common/Modal'
import {
  Calendar,
  Clock,
  Users,
  ShieldCheck,
  AlertTriangle,
  CheckCircle,
  Play,
  CalendarCheck,
  UserCheck,
  Cpu,
  RefreshCw,
} from 'lucide-react'

export const EvcScheduling: React.FC = () => {
  const queryClient = useQueryClient()
  const { hasRole, isAdmin } = useAuth()

  // Authorized roles for scheduler execution
  const canSchedule =
    isAdmin ||
    hasRole('ROLE_EVC_COORDINATOR') ||
    hasRole('ROLE_REGIONAL_OFFICER')

  // Tabs: 'pending' | 'scheduled' | 'experts'
  const [activeTab, setActiveTab] = useState<'pending' | 'scheduled' | 'experts'>('pending')

  // Scheduler Form Parameters
  const [selectedInspectionIds, setSelectedInspectionIds] = useState<string[]>([])
  const [timeLimitSeconds, setTimeLimitSeconds] = useState<number>(10)
  const [allowRescheduling, setAllowRescheduling] = useState<boolean>(false)
  const [solverResult, setSolverResult] = useState<SchedulingResponseDto | null>(null)
  const [schedulingError, setSchedulingError] = useState<string | null>(null)

  // Details Modal State
  const [selectedInspectionForDetails, setSelectedInspectionForDetails] = useState<EvcInspectionDto | null>(null)
  const [isDetailsModalOpen, setIsDetailsModalOpen] = useState(false)

  // Queries
  const {
    data: pendingInspections = [],
    isLoading: isLoadingPending,
  } = useQuery({
    queryKey: ['evc', 'pending'],
    queryFn: evcApi.getPendingInspections,
  })

  const {
    data: scheduledInspections = [],
    isLoading: isLoadingScheduled,
  } = useQuery({
    queryKey: ['evc', 'scheduled'],
    queryFn: evcApi.getScheduledInspections,
  })

  const {
    data: expertsData,
    isLoading: isLoadingExperts,
  } = useQuery({
    queryKey: ['evc', 'experts'],
    queryFn: () => evcApi.getActiveExperts(0, 50),
  })

  // Schedule Generation Mutation
  const scheduleMutation = useMutation({
    mutationFn: evcApi.generateSchedule,
    onSuccess: (data) => {
      setSolverResult(data)
      setSchedulingError(null)
      if (data.status === 'OPTIMAL' || data.status === 'FEASIBLE') {
        queryClient.invalidateQueries({ queryKey: ['evc', 'pending'] })
        queryClient.invalidateQueries({ queryKey: ['evc', 'scheduled'] })
        setSelectedInspectionIds([])
      }
    },
    onError: (err: any) => {
      setSchedulingError(err?.problem?.detail || err.message || 'Scheduling execution failed')
      setSolverResult(null)
    },
  })

  // Selection helpers
  const handleSelectAll = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.checked) {
      setSelectedInspectionIds(pendingInspections.map((i) => i.id))
    } else {
      setSelectedInspectionIds([])
    }
  }

  const handleToggleSelect = (id: string) => {
    setSelectedInspectionIds((prev) =>
      prev.includes(id) ? prev.filter((item) => item !== id) : [...prev, id]
    )
  }

  const handleLaunchScheduler = () => {
    if (selectedInspectionIds.length === 0) return
    setSchedulingError(null)
    setSolverResult(null)
    scheduleMutation.mutate({
      inspectionIds: selectedInspectionIds,
      allowRescheduling,
      timeLimitSeconds,
    })
  }

  const handleOpenDetails = (inspection: EvcInspectionDto) => {
    setSelectedInspectionForDetails(inspection)
    setIsDetailsModalOpen(true)
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
      {/* Header & Statutory Context */}
      <div
        style={{
          display: 'flex',
          flexDirection: 'column',
          gap: '0.4rem',
          borderBottom: '1px solid var(--border-medium)',
          paddingBottom: '1rem',
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: '0.5rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.65rem' }}>
            <CalendarCheck size={26} color="var(--gov-navy)" />
            <h1 style={{ fontSize: '1.35rem', fontWeight: 700, color: 'var(--gov-navy)', margin: 0 }}>
              Expert Visit Committee (EVC) Scheduling Engine
            </h1>
          </div>
          <div
            style={{
              display: 'inline-flex',
              alignItems: 'center',
              gap: '0.35rem',
              padding: '0.3rem 0.65rem',
              borderRadius: 'var(--radius-sm)',
              backgroundColor: 'var(--gov-blue-light)',
              color: 'var(--gov-navy)',
              fontSize: '0.75rem',
              fontWeight: 600,
              border: '1px solid var(--border-medium)',
            }}
          >
            <ShieldCheck size={14} color="var(--gov-blue)" />
            <span>AICTE APH 2024–27 Statutory Verification</span>
          </div>
        </div>
        <p style={{ margin: 0, fontSize: '0.85rem', color: 'var(--text-secondary)' }}>
          Autonomous multi-day committee dispatch enforcing strict Sunday exclusions, multi-day non-overlapping schedules, and statutory conflict-of-interest isolation.
        </p>
      </div>

      {/* Summary KPI Cards */}
      <div
        style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(210px, 1fr))',
          gap: '1rem',
        }}
      >
        <div
          style={{
            backgroundColor: 'var(--bg-surface)',
            border: '1px solid var(--border-medium)',
            borderRadius: 'var(--radius-sm)',
            padding: '1rem',
            boxShadow: 'var(--shadow-xs)',
          }}
        >
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
            <span style={{ fontSize: '0.78rem', fontWeight: 600, color: 'var(--text-secondary)', textTransform: 'uppercase' }}>
              Pending Scheduling
            </span>
            <Clock size={16} color="var(--gov-saffron)" />
          </div>
          <div style={{ fontSize: '1.6rem', fontWeight: 700, color: 'var(--gov-navy)' }}>
            {isLoadingPending ? '...' : pendingInspections.length}
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '0.25rem' }}>
            Awaiting committee formation
          </div>
        </div>

        <div
          style={{
            backgroundColor: 'var(--bg-surface)',
            border: '1px solid var(--border-medium)',
            borderRadius: 'var(--radius-sm)',
            padding: '1rem',
            boxShadow: 'var(--shadow-xs)',
          }}
        >
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
            <span style={{ fontSize: '0.78rem', fontWeight: 600, color: 'var(--text-secondary)', textTransform: 'uppercase' }}>
              Confirmed Schedules
            </span>
            <CheckCircle size={16} color="var(--gov-green)" />
          </div>
          <div style={{ fontSize: '1.6rem', fontWeight: 700, color: 'var(--gov-navy)' }}>
            {isLoadingScheduled ? '...' : scheduledInspections.length}
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '0.25rem' }}>
            Visits committed &amp; notified
          </div>
        </div>

        <div
          style={{
            backgroundColor: 'var(--bg-surface)',
            border: '1px solid var(--border-medium)',
            borderRadius: 'var(--radius-sm)',
            padding: '1rem',
            boxShadow: 'var(--shadow-xs)',
          }}
        >
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
            <span style={{ fontSize: '0.78rem', fontWeight: 600, color: 'var(--text-secondary)', textTransform: 'uppercase' }}>
              Accredited Experts Pool
            </span>
            <Users size={16} color="var(--gov-blue)" />
          </div>
          <div style={{ fontSize: '1.6rem', fontWeight: 700, color: 'var(--gov-navy)' }}>
            {isLoadingExperts ? '...' : (expertsData?.totalElements ?? 0)}
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '0.25rem' }}>
            Active verified peer evaluators
          </div>
        </div>

        <div
          style={{
            backgroundColor: 'var(--bg-surface)',
            border: '1px solid var(--border-medium)',
            borderRadius: 'var(--radius-sm)',
            padding: '1rem',
            boxShadow: 'var(--shadow-xs)',
          }}
        >
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
            <span style={{ fontSize: '0.78rem', fontWeight: 600, color: 'var(--text-secondary)', textTransform: 'uppercase' }}>
              Panel Allocation Status
            </span>
            <Cpu size={16} color="var(--gov-navy)" />
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', marginTop: '0.2rem' }}>
            <span style={{ display: 'inline-block', width: '8px', height: '8px', borderRadius: '50%', backgroundColor: 'var(--gov-green)' }} />
            <span style={{ fontSize: '1rem', fontWeight: 700, color: 'var(--gov-navy)' }}>Statutory Allocation Engine</span>
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '0.25rem' }}>
            Statutory panel allocation active
          </div>
        </div>
      </div>

      {/* Tabs Navigation */}
      <div
        style={{
          display: 'flex',
          gap: '0.5rem',
          borderBottom: '2px solid var(--border-light)',
          paddingBottom: '0.25rem',
        }}
        role="tablist"
      >
        <button
          role="tab"
          aria-selected={activeTab === 'pending'}
          onClick={() => setActiveTab('pending')}
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: '0.45rem',
            padding: '0.6rem 1rem',
            border: 'none',
            borderBottom: activeTab === 'pending' ? '3px solid var(--gov-blue)' : '3px solid transparent',
            backgroundColor: 'transparent',
            color: activeTab === 'pending' ? 'var(--gov-blue)' : 'var(--text-secondary)',
            fontWeight: activeTab === 'pending' ? 700 : 500,
            fontSize: '0.88rem',
            cursor: 'pointer',
            marginBottom: '-2px',
          }}
          id="tab-pending-inspections"
        >
          <Clock size={16} />
          <span>Pending Inspections &amp; Scheduling ({pendingInspections.length})</span>
        </button>

        <button
          role="tab"
          aria-selected={activeTab === 'scheduled'}
          onClick={() => setActiveTab('scheduled')}
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: '0.45rem',
            padding: '0.6rem 1rem',
            border: 'none',
            borderBottom: activeTab === 'scheduled' ? '3px solid var(--gov-blue)' : '3px solid transparent',
            backgroundColor: 'transparent',
            color: activeTab === 'scheduled' ? 'var(--gov-blue)' : 'var(--text-secondary)',
            fontWeight: activeTab === 'scheduled' ? 700 : 500,
            fontSize: '0.88rem',
            cursor: 'pointer',
            marginBottom: '-2px',
          }}
          id="tab-scheduled-inspections"
        >
          <CalendarCheck size={16} />
          <span>Confirmed Inspection Schedules ({scheduledInspections.length})</span>
        </button>

        <button
          role="tab"
          aria-selected={activeTab === 'experts'}
          onClick={() => setActiveTab('experts')}
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: '0.45rem',
            padding: '0.6rem 1rem',
            border: 'none',
            borderBottom: activeTab === 'experts' ? '3px solid var(--gov-blue)' : '3px solid transparent',
            backgroundColor: 'transparent',
            color: activeTab === 'experts' ? 'var(--gov-blue)' : 'var(--text-secondary)',
            fontWeight: activeTab === 'experts' ? 700 : 500,
            fontSize: '0.88rem',
            cursor: 'pointer',
            marginBottom: '-2px',
          }}
          id="tab-experts-registry"
        >
          <Users size={16} />
          <span>Accredited Experts Registry</span>
        </button>
      </div>

      {/* Solver Diagnostics Banner (When Results Exist) */}
      {solverResult && (
        <div
          style={{
            padding: '1rem 1.25rem',
            borderRadius: 'var(--radius-sm)',
            border: `1px solid ${
              solverResult.status === 'OPTIMAL' || solverResult.status === 'FEASIBLE'
                ? 'var(--status-success-border)'
                : 'var(--status-warning-border)'
            }`,
            backgroundColor:
              solverResult.status === 'OPTIMAL' || solverResult.status === 'FEASIBLE'
                ? 'var(--status-success-bg)'
                : 'var(--status-warning-bg)',
            color:
              solverResult.status === 'OPTIMAL' || solverResult.status === 'FEASIBLE'
                ? 'var(--status-success-text)'
                : 'var(--status-warning-text)',
            display: 'flex',
            flexDirection: 'column',
            gap: '0.5rem',
          }}
          id="solver-result-banner"
        >
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontWeight: 700, fontSize: '0.92rem' }}>
              {solverResult.status === 'OPTIMAL' || solverResult.status === 'FEASIBLE' ? (
                <CheckCircle size={18} />
              ) : (
                <AlertTriangle size={18} />
              )}
              <span>
                Solver Result: {solverResult.status} (Execution Time: {solverResult.solveDurationMs} ms)
              </span>
            </div>
            <span style={{ fontSize: '0.78rem', fontWeight: 600 }}>
              Inspections Processed: {solverResult.totalInspections}
            </span>
          </div>
          {solverResult.explanation && (
            <div style={{ fontSize: '0.82rem', lineHeight: 1.45 }}>
              {solverResult.explanation}
            </div>
          )}

          {/* If Successful, show short summary of assignments */}
          {solverResult.scheduledInspections && solverResult.scheduledInspections.length > 0 && (
            <div style={{ marginTop: '0.5rem', borderTop: '1px solid rgba(0,0,0,0.08)', paddingTop: '0.5rem' }}>
              <div style={{ fontSize: '0.8rem', fontWeight: 600, marginBottom: '0.25rem' }}>
                Committed Inspection Schedules:
              </div>
              <ul style={{ margin: 0, paddingLeft: '1.2rem', fontSize: '0.8rem' }}>
                {solverResult.scheduledInspections.map((s) => (
                  <li key={s.inspectionId}>
                    <strong>{s.institutionName}</strong> ({s.applicationNumber}): Visit from{' '}
                    <strong>{s.scheduledStartDate}</strong> to <strong>{s.scheduledEndDate}</strong>. Committee:{' '}
                    {s.assignedExperts?.map((m) => `${m.name} (${m.role})`).join(', ')}
                  </li>
                ))}
              </ul>
            </div>
          )}
        </div>
      )}

      {/* Error Banner */}
      {schedulingError && (
        <div
          style={{
            padding: '0.85rem 1.15rem',
            borderRadius: 'var(--radius-sm)',
            border: '1px solid var(--status-danger-border)',
            backgroundColor: 'var(--status-danger-bg)',
            color: 'var(--status-danger-text)',
            display: 'flex',
            alignItems: 'center',
            gap: '0.5rem',
            fontSize: '0.85rem',
          }}
          id="scheduling-error-banner"
        >
          <AlertTriangle size={18} />
          <span>{schedulingError}</span>
        </div>
      )}

      {/* TAB 1: PENDING INSPECTIONS & SCHEDULING FORM */}
      {activeTab === 'pending' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          {/* Action & Solver Controls Bar (Only for authorized roles) */}
          {canSchedule && (
            <div
              style={{
                backgroundColor: 'var(--bg-surface)',
                border: '1px solid var(--border-medium)',
                borderRadius: 'var(--radius-sm)',
                padding: '1rem',
                display: 'flex',
                flexWrap: 'wrap',
                alignItems: 'center',
                justifyContent: 'space-between',
                gap: '1rem',
              }}
            >
              <div style={{ display: 'flex', alignItems: 'center', flexWrap: 'wrap', gap: '1.25rem' }}>
                {/* Time Limit Setting */}
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                  <label htmlFor="evc-time-limit" style={{ fontSize: '0.8rem', fontWeight: 600, color: 'var(--text-secondary)' }}>
                    Solver Time Limit:
                  </label>
                  <select
                    id="evc-time-limit"
                    value={timeLimitSeconds}
                    onChange={(e) => setTimeLimitSeconds(Number(e.target.value))}
                    style={{
                      padding: '0.35rem 0.6rem',
                      border: '1px solid var(--border-medium)',
                      borderRadius: 'var(--radius-sm)',
                      fontSize: '0.82rem',
                      backgroundColor: 'var(--bg-surface)',
                    }}
                  >
                    <option value={5}>5 seconds (Fast)</option>
                    <option value={10}>10 seconds (Standard)</option>
                    <option value={30}>30 seconds (Thorough)</option>
                    <option value={60}>60 seconds (Maximum)</option>
                  </select>
                </div>

                {/* Allow Rescheduling Toggle */}
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
                  <input
                    type="checkbox"
                    id="evc-allow-rescheduling"
                    checked={allowRescheduling}
                    onChange={(e) => setAllowRescheduling(e.target.checked)}
                    style={{ cursor: 'pointer' }}
                  />
                  <label
                    htmlFor="evc-allow-rescheduling"
                    style={{ fontSize: '0.8rem', fontWeight: 500, color: 'var(--text-secondary)', cursor: 'pointer' }}
                  >
                    Allow Rescheduling of Previously Committed Inspections
                  </label>
                </div>
              </div>

              {/* Primary Action Button */}
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.65rem' }}>
                <button
                  type="button"
                  onClick={handleLaunchScheduler}
                  disabled={selectedInspectionIds.length === 0 || scheduleMutation.isPending}
                  style={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: '0.45rem',
                    padding: '0.55rem 1.15rem',
                    backgroundColor:
                      selectedInspectionIds.length === 0 || scheduleMutation.isPending
                        ? 'var(--border-medium)'
                        : 'var(--gov-blue)',
                    color: 'var(--text-inverse)',
                    border: 'none',
                    borderRadius: 'var(--radius-sm)',
                    fontWeight: 600,
                    fontSize: '0.85rem',
                    cursor: selectedInspectionIds.length === 0 || scheduleMutation.isPending ? 'not-allowed' : 'pointer',
                    boxShadow: 'var(--shadow-xs)',
                    transition: 'background-color 0.15s ease',
                  }}
                  id="btn-generate-schedule"
                >
                  {scheduleMutation.isPending ? (
                    <>
                      <RefreshCw size={15} className="spin" />
                      <span>Allocating Committee Panels...</span>
                    </>
                  ) : (
                    <>
                      <Play size={15} />
                      <span>Generate Committee Schedule ({selectedInspectionIds.length})</span>
                    </>
                  )}
                </button>
              </div>
            </div>
          )}

          {/* Pending Table */}
          <div
            style={{
              backgroundColor: 'var(--bg-surface)',
              border: '1px solid var(--border-medium)',
              borderRadius: 'var(--radius-sm)',
              overflowX: 'auto',
            }}
          >
            <table
              style={{
                width: '100%',
                borderCollapse: 'collapse',
                textAlign: 'left',
                fontSize: '0.82rem',
              }}
              aria-label="Pending Inspections Table"
            >
              <thead>
                <tr style={{ backgroundColor: 'var(--bg-surface-secondary)', borderBottom: '1px solid var(--border-medium)' }}>
                  {canSchedule && (
                    <th style={{ padding: '0.65rem 0.75rem', width: '38px', textAlign: 'center' }}>
                      <input
                        type="checkbox"
                        checked={
                          pendingInspections.length > 0 &&
                          selectedInspectionIds.length === pendingInspections.length
                        }
                        onChange={handleSelectAll}
                        aria-label="Select all pending inspections"
                      />
                    </th>
                  )}
                  <th style={{ padding: '0.65rem 0.75rem', fontWeight: 600, color: 'var(--gov-navy)' }}>
                    Application / Institution
                  </th>
                  <th style={{ padding: '0.65rem 0.75rem', fontWeight: 600, color: 'var(--gov-navy)' }}>
                    Inspection Window
                  </th>
                  <th style={{ padding: '0.65rem 0.75rem', fontWeight: 600, color: 'var(--gov-navy)' }}>
                    Duration &amp; Committee
                  </th>
                  <th style={{ padding: '0.65rem 0.75rem', fontWeight: 600, color: 'var(--gov-navy)' }}>
                    Status
                  </th>
                  <th style={{ padding: '0.65rem 0.75rem', fontWeight: 600, color: 'var(--gov-navy)', textAlign: 'right' }}>
                    Action
                  </th>
                </tr>
              </thead>
              <tbody>
                {isLoadingPending ? (
                  <tr>
                    <td colSpan={6} style={{ padding: '2rem', textAlign: 'center', color: 'var(--text-muted)' }}>
                      Loading eligible inspections...
                    </td>
                  </tr>
                ) : pendingInspections.length === 0 ? (
                  <tr>
                    <td colSpan={6} style={{ padding: '2rem', textAlign: 'center', color: 'var(--text-muted)' }}>
                      No pending inspections awaiting scheduling at this time.
                    </td>
                  </tr>
                ) : (
                  pendingInspections.map((inspection) => {
                    const isSelected = selectedInspectionIds.includes(inspection.id)
                    return (
                      <tr
                        key={inspection.id}
                        style={{
                          borderBottom: '1px solid var(--border-light)',
                          backgroundColor: isSelected ? 'var(--gov-blue-light)' : 'transparent',
                        }}
                      >
                        {canSchedule && (
                          <td style={{ padding: '0.65rem 0.75rem', textAlign: 'center' }}>
                            <input
                              type="checkbox"
                              checked={isSelected}
                              onChange={() => handleToggleSelect(inspection.id)}
                              aria-label={`Select inspection for ${inspection.institutionName}`}
                            />
                          </td>
                        )}
                        <td style={{ padding: '0.65rem 0.75rem' }}>
                          <div style={{ fontWeight: 600, color: 'var(--gov-navy)' }}>
                            {inspection.institutionName}
                          </div>
                          <div style={{ fontSize: '0.74rem', color: 'var(--text-muted)' }}>
                            App No: {inspection.applicationNumber || 'APP-202425-DEMO'}
                          </div>
                        </td>
                        <td style={{ padding: '0.65rem 0.75rem' }}>
                          <div style={{ display: 'flex', alignItems: 'center', gap: '0.35rem', color: 'var(--text-secondary)' }}>
                            <Calendar size={13} color="var(--gov-blue)" />
                            <span>
                              {inspection.earliestStartDate} &rarr; {inspection.latestStartDate}
                            </span>
                          </div>
                        </td>
                        <td style={{ padding: '0.65rem 0.75rem' }}>
                          <div style={{ fontWeight: 500 }}>
                            {inspection.durationInDays} Day(s) required
                          </div>
                          <div style={{ fontSize: '0.74rem', color: 'var(--text-muted)' }}>
                            Committee: {inspection.requiredMemberCount} Experts
                          </div>
                        </td>
                        <td style={{ padding: '0.65rem 0.75rem' }}>
                          <StatusBadge status={inspection.status} />
                        </td>
                        <td style={{ padding: '0.65rem 0.75rem', textAlign: 'right' }}>
                          <button
                            type="button"
                            onClick={() => handleOpenDetails(inspection)}
                            style={{
                              padding: '0.3rem 0.6rem',
                              border: '1px solid var(--border-medium)',
                              backgroundColor: 'var(--bg-surface)',
                              color: 'var(--gov-blue)',
                              borderRadius: 'var(--radius-sm)',
                              fontSize: '0.75rem',
                              fontWeight: 600,
                              cursor: 'pointer',
                            }}
                          >
                            Details
                          </button>
                        </td>
                      </tr>
                    )
                  })
                )}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* TAB 2: SCHEDULED INSPECTIONS */}
      {activeTab === 'scheduled' && (
        <div
          style={{
            backgroundColor: 'var(--bg-surface)',
            border: '1px solid var(--border-medium)',
            borderRadius: 'var(--radius-sm)',
            overflowX: 'auto',
          }}
        >
          <table
            style={{
              width: '100%',
              borderCollapse: 'collapse',
              textAlign: 'left',
              fontSize: '0.82rem',
            }}
            aria-label="Confirmed Inspection Schedules Table"
          >
            <thead>
              <tr style={{ backgroundColor: 'var(--bg-surface-secondary)', borderBottom: '1px solid var(--border-medium)' }}>
                <th style={{ padding: '0.65rem 0.75rem', fontWeight: 600, color: 'var(--gov-navy)' }}>
                  Institution / Application
                </th>
                <th style={{ padding: '0.65rem 0.75rem', fontWeight: 600, color: 'var(--gov-navy)' }}>
                  Confirmed Visit Dates
                </th>
                <th style={{ padding: '0.65rem 0.75rem', fontWeight: 600, color: 'var(--gov-navy)' }}>
                  Assigned Committee
                </th>
                <th style={{ padding: '0.65rem 0.75rem', fontWeight: 600, color: 'var(--gov-navy)' }}>
                  Status
                </th>
                <th style={{ padding: '0.65rem 0.75rem', fontWeight: 600, color: 'var(--gov-navy)', textAlign: 'right' }}>
                  Action
                </th>
              </tr>
            </thead>
            <tbody>
              {isLoadingScheduled ? (
                <tr>
                  <td colSpan={5} style={{ padding: '2rem', textAlign: 'center', color: 'var(--text-muted)' }}>
                    Loading confirmed schedules...
                  </td>
                </tr>
              ) : scheduledInspections.length === 0 ? (
                <tr>
                  <td colSpan={5} style={{ padding: '2rem', textAlign: 'center', color: 'var(--text-muted)' }}>
                    No inspections scheduled yet. Run the scheduling engine from the pending tab.
                  </td>
                </tr>
              ) : (
                scheduledInspections.map((inspection) => (
                  <tr key={inspection.id} style={{ borderBottom: '1px solid var(--border-light)' }}>
                    <td style={{ padding: '0.65rem 0.75rem' }}>
                      <div style={{ fontWeight: 600, color: 'var(--gov-navy)' }}>
                        {inspection.institutionName}
                      </div>
                      <div style={{ fontSize: '0.74rem', color: 'var(--text-muted)' }}>
                        App No: {inspection.applicationNumber || 'APP-202425-DEMO'}
                      </div>
                    </td>
                    <td style={{ padding: '0.65rem 0.75rem' }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '0.35rem', fontWeight: 600, color: 'var(--gov-navy)' }}>
                        <CalendarCheck size={14} color="var(--gov-green)" />
                        <span>
                          {inspection.scheduledStartDate} &rarr; {inspection.scheduledEndDate}
                        </span>
                      </div>
                      <div style={{ fontSize: '0.74rem', color: 'var(--text-muted)' }}>
                        Duration: {inspection.durationInDays} Day(s) (Sunday Excluded)
                      </div>
                    </td>
                    <td style={{ padding: '0.65rem 0.75rem' }}>
                      {inspection.assignedCommittee && inspection.assignedCommittee.length > 0 ? (
                        <div style={{ display: 'flex', flexDirection: 'column', gap: '0.2rem' }}>
                          {inspection.assignedCommittee.map((member) => (
                            <div key={member.expertId} style={{ display: 'flex', alignItems: 'center', gap: '0.35rem', fontSize: '0.76rem' }}>
                              <span
                                style={{
                                  padding: '0.1rem 0.35rem',
                                  borderRadius: '2px',
                                  fontSize: '0.68rem',
                                  fontWeight: 700,
                                  backgroundColor:
                                    member.role === 'CHAIRPERSON'
                                      ? 'var(--gov-saffron)'
                                      : 'var(--gov-blue-light)',
                                  color:
                                    member.role === 'CHAIRPERSON'
                                      ? '#ffffff'
                                      : 'var(--gov-blue)',
                                }}
                              >
                                {member.role}
                              </span>
                              <span style={{ fontWeight: 500 }}>{member.name}</span>
                              <span style={{ color: 'var(--text-muted)' }}>({member.discipline})</span>
                            </div>
                          ))}
                        </div>
                      ) : (
                        <span style={{ color: 'var(--text-muted)', fontSize: '0.76rem' }}>
                          Committee unassigned
                        </span>
                      )}
                    </td>
                    <td style={{ padding: '0.65rem 0.75rem' }}>
                      <StatusBadge status={inspection.status} />
                    </td>
                    <td style={{ padding: '0.65rem 0.75rem', textAlign: 'right' }}>
                      <button
                        type="button"
                        onClick={() => handleOpenDetails(inspection)}
                        style={{
                          padding: '0.3rem 0.6rem',
                          border: '1px solid var(--border-medium)',
                          backgroundColor: 'var(--bg-surface)',
                          color: 'var(--gov-blue)',
                          borderRadius: 'var(--radius-sm)',
                          fontSize: '0.75rem',
                          fontWeight: 600,
                          cursor: 'pointer',
                        }}
                      >
                        Details
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      )}

      {/* TAB 3: ACCREDITED EXPERTS REGISTRY */}
      {activeTab === 'experts' && (
        <div
          style={{
            backgroundColor: 'var(--bg-surface)',
            border: '1px solid var(--border-medium)',
            borderRadius: 'var(--radius-sm)',
            overflowX: 'auto',
          }}
        >
          <table
            style={{
              width: '100%',
              borderCollapse: 'collapse',
              textAlign: 'left',
              fontSize: '0.82rem',
            }}
            aria-label="Accredited Experts Registry Table"
          >
            <thead>
              <tr style={{ backgroundColor: 'var(--bg-surface-secondary)', borderBottom: '1px solid var(--border-medium)' }}>
                <th style={{ padding: '0.65rem 0.75rem', fontWeight: 600, color: 'var(--gov-navy)' }}>
                  Expert Name &amp; Designation
                </th>
                <th style={{ padding: '0.65rem 0.75rem', fontWeight: 600, color: 'var(--gov-navy)' }}>
                  Discipline / Specialization
                </th>
                <th style={{ padding: '0.65rem 0.75rem', fontWeight: 600, color: 'var(--gov-navy)' }}>
                  Parent Organization / Affiliation
                </th>
                <th style={{ padding: '0.65rem 0.75rem', fontWeight: 600, color: 'var(--gov-navy)' }}>
                  Availability Status
                </th>
                <th style={{ padding: '0.65rem 0.75rem', fontWeight: 600, color: 'var(--gov-navy)' }}>
                  Accreditation
                </th>
              </tr>
            </thead>
            <tbody>
              {isLoadingExperts ? (
                <tr>
                  <td colSpan={5} style={{ padding: '2rem', textAlign: 'center', color: 'var(--text-muted)' }}>
                    Loading accredited peer reviewers...
                  </td>
                </tr>
              ) : expertsData?.content?.length === 0 ? (
                <tr>
                  <td colSpan={5} style={{ padding: '2rem', textAlign: 'center', color: 'var(--text-muted)' }}>
                    No active experts found in registry.
                  </td>
                </tr>
              ) : (
                expertsData?.content?.map((expert) => (
                  <tr key={expert.id} style={{ borderBottom: '1px solid var(--border-light)' }}>
                    <td style={{ padding: '0.65rem 0.75rem' }}>
                      <div style={{ fontWeight: 600, color: 'var(--gov-navy)' }}>
                        {expert.name}
                      </div>
                      <div style={{ fontSize: '0.74rem', color: 'var(--text-muted)' }}>
                        {expert.designation || 'Senior Professor'} &bull; {expert.email}
                      </div>
                    </td>
                    <td style={{ padding: '0.65rem 0.75rem' }}>
                      <span
                        style={{
                          padding: '0.15rem 0.45rem',
                          borderRadius: 'var(--radius-xs)',
                          backgroundColor: 'var(--gov-blue-light)',
                          color: 'var(--gov-blue)',
                          fontWeight: 600,
                          fontSize: '0.75rem',
                        }}
                      >
                        {expert.discipline}
                      </span>
                    </td>
                    <td style={{ padding: '0.65rem 0.75rem', color: 'var(--text-secondary)' }}>
                      {expert.organization || 'National Institute of Technology'}
                    </td>
                    <td style={{ padding: '0.65rem 0.75rem' }}>
                      {expert.availableDates && expert.availableDates.length > 0 ? (
                        <div style={{ display: 'flex', alignItems: 'center', gap: '0.35rem', color: 'var(--gov-green)' }}>
                          <CheckCircle size={14} />
                          <span style={{ fontSize: '0.75rem', fontWeight: 600 }}>
                            {expert.availableDates.length} days verified
                          </span>
                        </div>
                      ) : (
                        <span style={{ color: 'var(--text-muted)', fontSize: '0.75rem' }}>
                          On demand / schedule
                        </span>
                      )}
                    </td>
                    <td style={{ padding: '0.65rem 0.75rem' }}>
                      <span
                        style={{
                          display: 'inline-flex',
                          alignItems: 'center',
                          gap: '0.25rem',
                          color: expert.active ? 'var(--gov-green)' : 'var(--text-muted)',
                          fontWeight: 600,
                          fontSize: '0.75rem',
                        }}
                      >
                        <UserCheck size={14} />
                        {expert.active ? 'Verified' : 'Inactive'}
                      </span>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      )}

      {/* Inspection Details & Committee Assignment Modal */}
      {isDetailsModalOpen && selectedInspectionForDetails && (
        <Modal
          isOpen={isDetailsModalOpen}
          onClose={() => setIsDetailsModalOpen(false)}
          title={`EVC Inspection: ${selectedInspectionForDetails.institutionName}`}
        >
          <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem', fontSize: '0.85rem' }}>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem' }}>
              <div>
                <span style={{ color: 'var(--text-muted)', fontSize: '0.75rem' }}>Application Number</span>
                <div style={{ fontWeight: 600 }}>{selectedInspectionForDetails.applicationNumber || 'APP-202425-DEMO'}</div>
              </div>
              <div>
                <span style={{ color: 'var(--text-muted)', fontSize: '0.75rem' }}>Workflow Status</span>
                <div>
                  <StatusBadge status={selectedInspectionForDetails.status} />
                </div>
              </div>
              <div>
                <span style={{ color: 'var(--text-muted)', fontSize: '0.75rem' }}>Permissible Start Window</span>
                <div style={{ fontWeight: 500 }}>
                  {selectedInspectionForDetails.earliestStartDate} to {selectedInspectionForDetails.latestStartDate}
                </div>
              </div>
              <div>
                <span style={{ color: 'var(--text-muted)', fontSize: '0.75rem' }}>Required Committee Size</span>
                <div style={{ fontWeight: 500 }}>{selectedInspectionForDetails.requiredMemberCount} Accredited Experts</div>
              </div>
              <div>
                <span style={{ color: 'var(--text-muted)', fontSize: '0.75rem' }}>Duration</span>
                <div style={{ fontWeight: 500 }}>{selectedInspectionForDetails.durationInDays} Day(s) (Sunday excluded)</div>
              </div>
              {selectedInspectionForDetails.scheduledStartDate && (
                <div>
                  <span style={{ color: 'var(--text-muted)', fontSize: '0.75rem' }}>Committed Inspection Dates</span>
                  <div style={{ fontWeight: 700, color: 'var(--gov-green)' }}>
                    {selectedInspectionForDetails.scheduledStartDate} &rarr; {selectedInspectionForDetails.scheduledEndDate}
                  </div>
                </div>
              )}
            </div>

            {/* Committee Assignments Section */}
            <div style={{ marginTop: '0.5rem', borderTop: '1px solid var(--border-light)', paddingTop: '0.75rem' }}>
              <div style={{ fontWeight: 700, color: 'var(--gov-navy)', marginBottom: '0.5rem', display: 'flex', alignItems: 'center', gap: '0.35rem' }}>
                <Users size={16} />
                <span>Assigned Committee Members</span>
              </div>
              {selectedInspectionForDetails.assignedCommittee && selectedInspectionForDetails.assignedCommittee.length > 0 ? (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                  {selectedInspectionForDetails.assignedCommittee.map((member) => (
                    <div
                      key={member.expertId}
                      style={{
                        padding: '0.5rem 0.75rem',
                        border: '1px solid var(--border-medium)',
                        borderRadius: 'var(--radius-sm)',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'space-between',
                        backgroundColor: 'var(--bg-surface-secondary)',
                      }}
                    >
                      <div>
                        <div style={{ fontWeight: 600, color: 'var(--gov-navy)' }}>{member.name}</div>
                        <div style={{ fontSize: '0.74rem', color: 'var(--text-muted)' }}>
                          Discipline: {member.discipline}
                        </div>
                      </div>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                        <span
                          style={{
                            padding: '0.15rem 0.45rem',
                            borderRadius: 'var(--radius-xs)',
                            fontSize: '0.7rem',
                            fontWeight: 700,
                            backgroundColor:
                              member.role === 'CHAIRPERSON'
                                ? 'var(--gov-saffron)'
                                : 'var(--gov-blue-light)',
                            color:
                              member.role === 'CHAIRPERSON'
                                ? '#ffffff'
                                : 'var(--gov-blue)',
                          }}
                        >
                          {member.role}
                        </span>
                        <StatusBadge status={member.status} />
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <div style={{ color: 'var(--text-muted)', fontSize: '0.8rem', fontStyle: 'italic' }}>
                  No committee members assigned yet. This inspection will receive peer evaluators when the statutory allocation engine is executed.
                </div>
              )}
            </div>

            <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: '0.5rem' }}>
              <button
                type="button"
                onClick={() => setIsDetailsModalOpen(false)}
                style={{
                  padding: '0.45rem 1rem',
                  border: '1px solid var(--border-medium)',
                  backgroundColor: 'var(--bg-surface)',
                  color: 'var(--text-primary)',
                  borderRadius: 'var(--radius-sm)',
                  fontSize: '0.8rem',
                  fontWeight: 600,
                  cursor: 'pointer',
                }}
              >
                Close
              </button>
            </div>
          </div>
        </Modal>
      )}
    </div>
  )
}

export default EvcScheduling
