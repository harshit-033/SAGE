import React, { useState, useEffect, useCallback } from 'react'
import {
  Bell,
  AlertTriangle,
  AlertCircle,
  CheckCircle2,
  ArrowUpRight,
  Clock,
  Check,
  Search,
  RefreshCw,
  Info,
  ShieldCheck,
  Building2,
} from 'lucide-react'
import { alertsApi, type AlertDto, type AlertSeverity, type AlertStatus } from '../api/alertsApi'
import { useAuth } from '../context/AuthContext'
import { Modal } from '../components/common/Modal'

export const Alerts: React.FC = () => {
  const { isAdmin, hasRole } = useAuth()
  const [alerts, setAlerts] = useState<AlertDto[]>([])
  const [loading, setLoading] = useState<boolean>(true)
  const [actionLoading, setActionLoading] = useState<boolean>(false)
  const [errorMsg, setErrorMsg] = useState<string | null>(null)
  const [successMsg, setSuccessMsg] = useState<string | null>(null)

  // Filters
  const [severityFilter, setSeverityFilter] = useState<string>('ALL')
  const [statusFilter, setStatusFilter] = useState<string>('ALL')
  const [searchQuery, setSearchQuery] = useState<string>('')

  // Modals state
  const [selectedAlert, setSelectedAlert] = useState<AlertDto | null>(null)
  const [ackModalOpen, setAckModalOpen] = useState<boolean>(false)
  const [ackNotes, setAckNotes] = useState<string>('')
  const [resolveModalOpen, setResolveModalOpen] = useState<boolean>(false)
  const [resolveNotes, setResolveNotes] = useState<string>('')

  const fetchAlerts = useCallback(async () => {
    try {
      setLoading(true)
      setErrorMsg(null)
      const data = await alertsApi.list({
        severity: severityFilter !== 'ALL' ? (severityFilter as AlertSeverity) : undefined,
        status: statusFilter !== 'ALL' ? (statusFilter as AlertStatus) : undefined,
        size: 50,
      })
      setAlerts(data.content)
    } catch (err: unknown) {
      setErrorMsg((err as Error).message || 'Failed to load regulatory alerts.')
    } finally {
      setLoading(false)
    }
  }, [severityFilter, statusFilter])

  useEffect(() => {
    fetchAlerts()
  }, [fetchAlerts])

  const handleAcknowledge = async () => {
    if (!selectedAlert) return
    if (!ackNotes.trim()) {
      setErrorMsg('Please enter acknowledgement notes for statutory compliance.')
      return
    }

    try {
      setActionLoading(true)
      setErrorMsg(null)
      await alertsApi.acknowledge(selectedAlert.id, { acknowledgementNotes: ackNotes.trim() })
      setSuccessMsg(`Alert ${selectedAlert.alertCode} acknowledged successfully.`)
      setAckModalOpen(false)
      setAckNotes('')
      setSelectedAlert(null)
      await fetchAlerts()
    } catch (err: unknown) {
      setErrorMsg((err as Error).message || 'Failed to acknowledge alert.')
    } finally {
      setActionLoading(false)
    }
  }

  const handleResolve = async () => {
    if (!selectedAlert) return
    if (!resolveNotes.trim()) {
      setErrorMsg('Please enter statutory resolution notes.')
      return
    }

    try {
      setActionLoading(true)
      setErrorMsg(null)
      await alertsApi.resolve(selectedAlert.id, { resolutionNotes: resolveNotes.trim() })
      setSuccessMsg(`Alert ${selectedAlert.alertCode} marked as resolved.`)
      setResolveModalOpen(false)
      setResolveNotes('')
      setSelectedAlert(null)
      await fetchAlerts()
    } catch (err: unknown) {
      setErrorMsg((err as Error).message || 'Failed to resolve alert.')
    } finally {
      setActionLoading(false)
    }
  }

  const handleEscalate = async (alert: AlertDto) => {
    if (!window.confirm(`Escalate alert "${alert.title}" to Regional HQ supervisory queue?`)) {
      return
    }

    try {
      setActionLoading(true)
      setErrorMsg(null)
      await alertsApi.escalate(alert.id, 'ROLE_REGIONAL_OFFICER')
      setSuccessMsg(`Alert escalated to Regional HQ supervisory queue.`)
      await fetchAlerts()
    } catch (err: unknown) {
      setErrorMsg((err as Error).message || 'Failed to escalate alert.')
    } finally {
      setActionLoading(false)
    }
  }

  // Filtered in-memory by search
  const filteredAlerts = alerts.filter((a) => {
    if (!searchQuery) return true
    const q = searchQuery.toLowerCase()
    return (
      a.title.toLowerCase().includes(q) ||
      a.message.toLowerCase().includes(q) ||
      (a.institutionName && a.institutionName.toLowerCase().includes(q)) ||
      a.alertCode.toLowerCase().includes(q)
    )
  })

  // Metric counts
  const totalCount = alerts.length
  const criticalCount = alerts.filter((a) => a.severity === 'CRITICAL' && a.status !== 'RESOLVED').length
  const unreadCount = alerts.filter((a) => a.status === 'UNREAD').length
  const escalatedCount = alerts.filter((a) => a.isEscalated && a.status !== 'RESOLVED').length

  const getSeverityBadge = (severity: AlertSeverity) => {
    switch (severity) {
      case 'CRITICAL':
        return (
          <span
            style={{
              display: 'inline-flex',
              alignItems: 'center',
              gap: '0.3rem',
              padding: '0.2rem 0.55rem',
              borderRadius: 'var(--radius-sm)',
              fontSize: '0.72rem',
              fontWeight: 700,
              backgroundColor: '#fee2e2',
              color: '#991b1b',
              border: '1px solid #f87171',
            }}
          >
            <AlertTriangle size={13} /> CRITICAL
          </span>
        )
      case 'WARNING':
        return (
          <span
            style={{
              display: 'inline-flex',
              alignItems: 'center',
              gap: '0.3rem',
              padding: '0.2rem 0.55rem',
              borderRadius: 'var(--radius-sm)',
              fontSize: '0.72rem',
              fontWeight: 700,
              backgroundColor: '#fef3c7',
              color: '#92400e',
              border: '1px solid #fcd34d',
            }}
          >
            <Clock size={13} /> WARNING
          </span>
        )
      case 'INFO':
      default:
        return (
          <span
            style={{
              display: 'inline-flex',
              alignItems: 'center',
              gap: '0.3rem',
              padding: '0.2rem 0.55rem',
              borderRadius: 'var(--radius-sm)',
              fontSize: '0.72rem',
              fontWeight: 700,
              backgroundColor: '#e0f2fe',
              color: '#0369a1',
              border: '1px solid #7dd3fc',
            }}
          >
            <Info size={13} /> INFO
          </span>
        )
    }
  }

  const getStatusBadge = (status: AlertStatus) => {
    switch (status) {
      case 'UNREAD':
        return (
          <span
            style={{
              padding: '0.15rem 0.5rem',
              borderRadius: 'var(--radius-sm)',
              fontSize: '0.7rem',
              fontWeight: 700,
              backgroundColor: '#fee2e2',
              color: '#b91c1c',
            }}
          >
            ● UNREAD
          </span>
        )
      case 'ACKNOWLEDGED':
        return (
          <span
            style={{
              padding: '0.15rem 0.5rem',
              borderRadius: 'var(--radius-sm)',
              fontSize: '0.7rem',
              fontWeight: 700,
              backgroundColor: '#fef3c7',
              color: '#b45309',
            }}
          >
            ACKNOWLEDGED
          </span>
        )
      case 'RESOLVED':
        return (
          <span
            style={{
              padding: '0.15rem 0.5rem',
              borderRadius: 'var(--radius-sm)',
              fontSize: '0.7rem',
              fontWeight: 700,
              backgroundColor: '#dcfce7',
              color: '#15803d',
            }}
          >
            ✓ RESOLVED
          </span>
        )
      default:
        return <span className="badge">{status}</span>
    }
  }

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
              <Bell size={20} />
            </div>
            <div>
              <h1 style={{ fontSize: '1.35rem', fontWeight: 700, color: 'var(--gov-navy)', margin: 0 }}>
                Regulatory Alerts &amp; Notification Engine
              </h1>
              <p style={{ margin: '0.2rem 0 0 0', fontSize: '0.85rem', color: 'var(--text-muted)' }}>
                Multi-channel compliance notifications, statutory deadline triggers, and supervisory escalations per AICTE APH 2024–27.
              </p>
            </div>
          </div>
        </div>

        <button
          type="button"
          onClick={fetchAlerts}
          className="btn btn-secondary btn-sm"
          disabled={loading}
          style={{ display: 'inline-flex', alignItems: 'center', gap: '0.4rem' }}
        >
          <RefreshCw size={14} className={loading ? 'animate-spin' : ''} />
          <span>Refresh Feeds</span>
        </button>
      </div>

      {/* Notifications / Alerts Banners */}
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
            Total Monitored Alerts
          </div>
          <div style={{ fontSize: '1.75rem', fontWeight: 800, color: 'var(--gov-navy)', marginTop: '0.25rem' }}>
            {totalCount}
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '0.35rem' }}>
            Scoped to authorized tenancy
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
            Active Critical Alerts
          </div>
          <div style={{ fontSize: '1.75rem', fontWeight: 800, color: '#b91c1c', marginTop: '0.25rem' }}>
            {criticalCount}
          </div>
          <div style={{ fontSize: '0.75rem', color: '#b91c1c', marginTop: '0.35rem' }}>
            Immediate statutory consequence
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
            Unread Notifications
          </div>
          <div style={{ fontSize: '1.75rem', fontWeight: 800, color: '#c2410c', marginTop: '0.25rem' }}>
            {unreadCount}
          </div>
          <div style={{ fontSize: '0.75rem', color: '#9a3412', marginTop: '0.35rem' }}>
            Requires officer acknowledgement
          </div>
        </div>

        <div
          style={{
            backgroundColor: 'var(--bg-surface)',
            padding: '1.25rem',
            borderRadius: 'var(--radius-md)',
            border: '1px solid #ddd6fe',
            borderLeft: '4px solid #8b5cf6',
            boxShadow: 'var(--shadow-xs)',
          }}
        >
          <div style={{ fontSize: '0.78rem', color: '#5b21b6', fontWeight: 600, textTransform: 'uppercase' }}>
            Supervisory Escalations
          </div>
          <div style={{ fontSize: '1.75rem', fontWeight: 800, color: '#6d28d9', marginTop: '0.25rem' }}>
            {escalatedCount}
          </div>
          <div style={{ fontSize: '0.75rem', color: '#5b21b6', marginTop: '0.35rem' }}>
            Elevated to Regional / HQ Officer
          </div>
        </div>
      </div>

      {/* Filter and Search Bar */}
      <div
        style={{
          backgroundColor: 'var(--bg-surface)',
          padding: '1rem 1.25rem',
          borderRadius: 'var(--radius-md)',
          border: '1px solid var(--border-medium)',
          display: 'flex',
          flexWrap: 'wrap',
          gap: '1rem',
          justifyContent: 'space-between',
          alignItems: 'center',
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', flex: '1', minWidth: '260px' }}>
          <Search size={16} color="var(--text-muted)" />
          <input
            type="text"
            className="form-control"
            placeholder="Search alerts by title, code, institution or keyword..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            style={{ width: '100%', fontSize: '0.85rem' }}
          />
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
            <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontWeight: 600 }}>Severity:</span>
            <select
              className="form-control"
              value={severityFilter}
              onChange={(e) => setSeverityFilter(e.target.value)}
              style={{ fontSize: '0.82rem', padding: '0.3rem 0.6rem' }}
            >
              <option value="ALL">All Severities</option>
              <option value="CRITICAL">Critical Only</option>
              <option value="WARNING">Warning Only</option>
              <option value="INFO">Info Only</option>
            </select>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
            <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontWeight: 600 }}>Status:</span>
            <select
              className="form-control"
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
              style={{ fontSize: '0.82rem', padding: '0.3rem 0.6rem' }}
            >
              <option value="ALL">All Statuses</option>
              <option value="UNREAD">Unread</option>
              <option value="ACKNOWLEDGED">Acknowledged</option>
              <option value="RESOLVED">Resolved</option>
            </select>
          </div>
        </div>
      </div>

      {/* Alerts Feed */}
      <div style={{ display: 'flex', flexDirection: 'column', gap: '0.85rem' }}>
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
            <p>Loading alerts from regulatory message bus...</p>
          </div>
        ) : filteredAlerts.length === 0 ? (
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
            <ShieldCheck size={36} color="var(--gov-green)" style={{ margin: '0 auto 0.75rem auto' }} />
            <h3 style={{ fontSize: '1.05rem', fontWeight: 600, color: 'var(--text-primary)' }}>
              No Active Alerts Matching Criteria
            </h3>
            <p style={{ fontSize: '0.85rem', marginTop: '0.25rem' }}>
              All statutory obligations and regulatory notifications are within expected parameters.
            </p>
          </div>
        ) : (
          filteredAlerts.map((alert) => (
            <div
              key={alert.id}
              style={{
                backgroundColor: 'var(--bg-surface)',
                borderRadius: 'var(--radius-md)',
                border: '1px solid var(--border-medium)',
                borderLeft:
                  alert.severity === 'CRITICAL'
                    ? '4px solid #ef4444'
                    : alert.severity === 'WARNING'
                    ? '4px solid #f59e0b'
                    : '4px solid #3b82f6',
                boxShadow: 'var(--shadow-xs)',
                padding: '1.25rem 1.5rem',
                display: 'flex',
                flexDirection: 'column',
                gap: '0.85rem',
              }}
            >
              {/* Top Row: Badges, Institution, Status */}
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '0.5rem' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.65rem' }}>
                  {getSeverityBadge(alert.severity)}
                  <span
                    style={{
                      padding: '0.15rem 0.5rem',
                      borderRadius: 'var(--radius-sm)',
                      fontSize: '0.7rem',
                      fontWeight: 600,
                      backgroundColor: 'var(--bg-surface-secondary)',
                      color: 'var(--text-secondary)',
                      border: '1px solid var(--border-light)',
                    }}
                  >
                    {alert.category}
                  </span>
                  {alert.isEscalated && (
                    <span
                      style={{
                        padding: '0.15rem 0.55rem',
                        borderRadius: 'var(--radius-sm)',
                        fontSize: '0.7rem',
                        fontWeight: 700,
                        backgroundColor: '#ede9fe',
                        color: '#6d28d9',
                        border: '1px solid #c4b5fd',
                      }}
                    >
                      ▲ ESCALATED TO REGIONAL HQ
                    </span>
                  )}
                </div>

                <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                  {getStatusBadge(alert.status)}
                  <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                    {new Date(alert.createdAt).toLocaleString('en-IN', {
                      day: '2-digit',
                      month: 'short',
                      year: 'numeric',
                      hour: '2-digit',
                      minute: '2-digit',
                    })}
                  </span>
                </div>
              </div>

              {/* Title & Message */}
              <div>
                <h3 style={{ fontSize: '1rem', fontWeight: 700, color: 'var(--gov-navy)', margin: '0 0 0.35rem 0' }}>
                  {alert.title}
                </h3>
                <p style={{ margin: 0, fontSize: '0.88rem', color: 'var(--text-secondary)', lineHeight: 1.45 }}>
                  {alert.message}
                </p>
              </div>

              {/* Context Row: Institution & Scope */}
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
                  {alert.institutionName && (
                    <span style={{ display: 'inline-flex', alignItems: 'center', gap: '0.35rem' }}>
                      <Building2 size={13} />
                      <strong style={{ color: 'var(--text-secondary)' }}>{alert.institutionName}</strong>
                    </span>
                  )}
                  {alert.jurisdictionState && (
                    <span>
                      State: <strong>{alert.jurisdictionState}</strong>
                    </span>
                  )}
                  <span style={{ fontFamily: 'var(--font-mono)', fontSize: '0.72rem' }}>
                    Code: {alert.alertCode}
                  </span>
                </div>

                {/* Delivery Badges */}
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
                  {alert.deliveries?.map((d) => (
                    <span
                      key={d.id}
                      style={{
                        padding: '0.1rem 0.4rem',
                        borderRadius: 'var(--radius-sm)',
                        fontSize: '0.68rem',
                        fontWeight: 600,
                        backgroundColor: d.status === 'DELIVERED' ? '#dcfce7' : '#f1f5f9',
                        color: d.status === 'DELIVERED' ? '#15803d' : '#64748b',
                        border: '1px solid var(--border-light)',
                      }}
                      title={`Recipient: ${d.recipientAddress} | Attempts: ${d.attemptCount}`}
                    >
                      {d.channel}: {d.status}
                    </span>
                  ))}
                </div>
              </div>

              {/* Acknowledgement / Resolution Notes If Any */}
              {alert.acknowledgementNotes && (
                <div
                  style={{
                    backgroundColor: 'var(--bg-surface-secondary)',
                    padding: '0.5rem 0.75rem',
                    borderRadius: 'var(--radius-sm)',
                    fontSize: '0.78rem',
                    color: 'var(--text-secondary)',
                    borderLeft: '3px solid var(--gov-blue)',
                  }}
                >
                  <strong>Acknowledged by {alert.acknowledgedByName}:</strong> {alert.acknowledgementNotes}
                </div>
              )}

              {alert.resolutionNotes && (
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
                  <strong>Resolved by {alert.resolvedByName}:</strong> {alert.resolutionNotes}
                </div>
              )}

              {/* Action Buttons */}
              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.65rem', paddingTop: '0.35rem' }}>
                {alert.status === 'UNREAD' && (
                  <button
                    type="button"
                    className="btn btn-primary btn-sm"
                    onClick={() => {
                      setSelectedAlert(alert)
                      setAckModalOpen(true)
                    }}
                    style={{ display: 'inline-flex', alignItems: 'center', gap: '0.35rem' }}
                  >
                    <Check size={14} />
                    <span>Acknowledge</span>
                  </button>
                )}

                {alert.status !== 'RESOLVED' && (
                  <button
                    type="button"
                    className="btn btn-secondary btn-sm"
                    onClick={() => {
                      setSelectedAlert(alert)
                      setResolveModalOpen(true)
                    }}
                    style={{ display: 'inline-flex', alignItems: 'center', gap: '0.35rem' }}
                  >
                    <CheckCircle2 size={14} />
                    <span>Mark Resolved</span>
                  </button>
                )}

                {/* Supervisory Escalation Button (Regional Officer or Admin) */}
                {(isAdmin || hasRole('ROLE_REGIONAL_OFFICER')) && !alert.isEscalated && alert.status !== 'RESOLVED' && (
                  <button
                    type="button"
                    className="btn btn-secondary btn-sm"
                    onClick={() => handleEscalate(alert)}
                    style={{
                      display: 'inline-flex',
                      alignItems: 'center',
                      gap: '0.35rem',
                      color: '#6d28d9',
                      borderColor: '#c4b5fd',
                    }}
                  >
                    <ArrowUpRight size={14} />
                    <span>Escalate to Regional HQ</span>
                  </button>
                )}
              </div>
            </div>
          ))
        )}
      </div>

      {/* Acknowledge Modal */}
      {ackModalOpen && selectedAlert && (
        <Modal
          title={`Acknowledge Regulatory Alert: ${selectedAlert.alertCode}`}
          isOpen={ackModalOpen}
          onClose={() => {
            setAckModalOpen(false)
            setSelectedAlert(null)
            setAckNotes('')
          }}
        >
          <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            <p style={{ fontSize: '0.88rem', color: 'var(--text-secondary)', margin: 0 }}>
              Formally acknowledge receipt of this statutory notice. Your acknowledgement is logged with cryptographic timestamping in the immutable audit trail.
            </p>

            <div
              style={{
                backgroundColor: 'var(--bg-surface-secondary)',
                padding: '0.75rem',
                borderRadius: 'var(--radius-sm)',
                fontSize: '0.85rem',
              }}
            >
              <strong>{selectedAlert.title}</strong>
              <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginTop: '0.25rem' }}>
                {selectedAlert.message}
              </div>
            </div>

            <div>
              <label style={{ display: 'block', fontSize: '0.82rem', fontWeight: 600, marginBottom: '0.35rem' }}>
                Action Initiated / Officer Remarks <span style={{ color: '#ef4444' }}>*</span>
              </label>
              <textarea
                className="form-control"
                rows={3}
                placeholder="Detail the remedial steps initiated or inspection response..."
                value={ackNotes}
                onChange={(e) => setAckNotes(e.target.value)}
                style={{ width: '100%', fontSize: '0.85rem' }}
              />
            </div>

            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '0.5rem' }}>
              <button
                type="button"
                className="btn btn-secondary btn-sm"
                onClick={() => {
                  setAckModalOpen(false)
                  setSelectedAlert(null)
                }}
              >
                Cancel
              </button>
              <button
                type="button"
                className="btn btn-primary btn-sm"
                disabled={actionLoading || !ackNotes.trim()}
                onClick={handleAcknowledge}
              >
                {actionLoading ? 'Recording...' : 'Confirm Acknowledgement'}
              </button>
            </div>
          </div>
        </Modal>
      )}

      {/* Resolve Modal */}
      {resolveModalOpen && selectedAlert && (
        <Modal
          title={`Resolve Statutory Alert: ${selectedAlert.alertCode}`}
          isOpen={resolveModalOpen}
          onClose={() => {
            setResolveModalOpen(false)
            setSelectedAlert(null)
            setResolveNotes('')
          }}
        >
          <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            <p style={{ fontSize: '0.88rem', color: 'var(--text-secondary)', margin: 0 }}>
              Confirm statutory compliance resolution. This will transition the alert to RESOLVED status and record resolution notes in the regulatory ledger.
            </p>

            <div>
              <label style={{ display: 'block', fontSize: '0.82rem', fontWeight: 600, marginBottom: '0.35rem' }}>
                Resolution Findings / Verification Summary <span style={{ color: '#ef4444' }}>*</span>
              </label>
              <textarea
                className="form-control"
                rows={3}
                placeholder="Describe verified documentary evidence or inspection completion..."
                value={resolveNotes}
                onChange={(e) => setResolveNotes(e.target.value)}
                style={{ width: '100%', fontSize: '0.85rem' }}
              />
            </div>

            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '0.5rem' }}>
              <button
                type="button"
                className="btn btn-secondary btn-sm"
                onClick={() => {
                  setResolveModalOpen(false)
                  setSelectedAlert(null)
                }}
              >
                Cancel
              </button>
              <button
                type="button"
                className="btn btn-primary btn-sm"
                disabled={actionLoading || !resolveNotes.trim()}
                onClick={handleResolve}
              >
                {actionLoading ? 'Resolving...' : 'Confirm Resolution'}
              </button>
            </div>
          </div>
        </Modal>
      )}
    </div>
  )
}
