import React, { useState, useEffect, useCallback } from 'react'
import { auditApi, type AuditEvent } from '../api/auditApi'
import { Breadcrumbs } from '../components/common/Breadcrumbs'
import { Pagination } from '../components/common/Pagination'
import { Alert } from '../components/common/Alert'
import {
  ShieldAlert,
  Search,
  RefreshCw,
  Clock,
} from 'lucide-react'

export const AuditLogViewer: React.FC = () => {
  const [events, setEvents] = useState<AuditEvent[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  // Filters
  const [principalQuery, setPrincipalQuery] = useState('')
  const [selectedEventType, setSelectedEventType] = useState('')
  const [selectedResourceType, setSelectedResourceType] = useState('')
  const [correlationIdQuery, setCorrelationIdQuery] = useState('')

  // Pagination
  const [page, setPage] = useState(0)
  const [pageSize] = useState(15)
  const [totalPages, setTotalPages] = useState(1)
  const [totalElements, setTotalElements] = useState(0)

  const fetchEvents = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const data = await auditApi.listAuditEvents({
        page,
        size: pageSize,
        principal: principalQuery || undefined,
        eventType: selectedEventType || undefined,
        resourceType: selectedResourceType || undefined,
        correlationId: correlationIdQuery || undefined,
      })
      setEvents(data.content)
      setTotalPages(data.totalPages)
      setTotalElements(data.totalElements)
    } catch (err: unknown) {
      const errorObj = err as { problem?: { detail?: string }; message?: string }
      setError(errorObj?.problem?.detail || errorObj?.message || 'Failed to load audit events')
    } finally {
      setLoading(false)
    }
  }, [page, pageSize, principalQuery, selectedEventType, selectedResourceType, correlationIdQuery])

  useEffect(() => {
    fetchEvents()
  }, [fetchEvents])

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    setPage(0)
    fetchEvents()
  }

  const getEventBadgeClass = (eventType: string) => {
    if (eventType.includes('FAILURE') || eventType.includes('VIOLATION')) {
      return 'badge-danger'
    }
    if (eventType.includes('SUCCESS') || eventType.includes('CREATED') || eventType.includes('ACTIVATE')) {
      return 'badge-success'
    }
    if (eventType.includes('SUSPEND') || eventType.includes('DEACTIVATE') || eventType.includes('REMOVED')) {
      return 'badge-warning'
    }
    return 'badge-info'
  }

  return (
    <div>
      <Breadcrumbs items={[{ label: 'Regulatory Audit Trail' }]} />

      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '1rem', marginBottom: '1.25rem' }}>
        <div>
          <h1 style={{ color: 'var(--gov-navy)', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <ShieldAlert size={24} color="var(--gov-blue)" />
            <span>Immutable Regulatory Audit Trail</span>
          </h1>
          <p style={{ fontSize: '0.825rem', color: 'var(--text-secondary)', marginTop: '0.2rem' }}>
            Consequential identity, authorization, status toggles, and cross-tenancy attempts logged with correlation IDs.
          </p>
        </div>

        <button
          type="button"
          className="btn btn-secondary btn-sm"
          onClick={fetchEvents}
          title="Refresh Audit Logs"
          id="refresh-audit-btn"
        >
          <RefreshCw size={14} />
          <span>Refresh Logs</span>
        </button>
      </div>

      {error && (
        <Alert type="danger" onClose={() => setError(null)}>
          {error}
        </Alert>
      )}

      {/* Filter Form */}
      <div className="gov-card" style={{ marginBottom: '1.25rem' }}>
        <div className="gov-card-body" style={{ padding: '1rem 1.25rem' }}>
          <form onSubmit={handleSearchSubmit} style={{ display: 'flex', gap: '0.85rem', flexWrap: 'wrap', alignItems: 'flex-end' }}>
            <div style={{ flex: '1.5', minWidth: '180px' }}>
              <label className="form-label" htmlFor="principal-filter" style={{ fontSize: '0.78rem' }}>
                Principal (Actor)
              </label>
              <input
                id="principal-filter"
                type="text"
                className="form-control"
                placeholder="e.g. admin@aicte.gov.in"
                value={principalQuery}
                onChange={(e) => setPrincipalQuery(e.target.value)}
              />
            </div>

            <div style={{ flex: '1.5', minWidth: '180px' }}>
              <label className="form-label" htmlFor="event-filter" style={{ fontSize: '0.78rem' }}>
                Event Type
              </label>
              <select
                id="event-filter"
                className="form-control"
                value={selectedEventType}
                onChange={(e) => {
                  setSelectedEventType(e.target.value)
                  setPage(0)
                }}
              >
                <option value="">All Event Types</option>
                <option value="AUTH_LOGIN_SUCCESS">AUTH_LOGIN_SUCCESS</option>
                <option value="AUTH_LOGIN_FAILURE">AUTH_LOGIN_FAILURE</option>
                <option value="AUTH_LOGOUT">AUTH_LOGOUT</option>
                <option value="TENANCY_VIOLATION_ATTEMPT">TENANCY_VIOLATION_ATTEMPT</option>
                <option value="INSTITUTION_CREATED">INSTITUTION_CREATED</option>
                <option value="INSTITUTION_UPDATED">INSTITUTION_UPDATED</option>
                <option value="INSTITUTION_STATUS_CHANGED">INSTITUTION_STATUS_CHANGED</option>
                <option value="INSTITUTION_USER_AFFILIATED">INSTITUTION_USER_AFFILIATED</option>
                <option value="INSTITUTION_USER_REMOVED">INSTITUTION_USER_REMOVED</option>
                <option value="USER_CREATED">USER_CREATED</option>
                <option value="USER_UPDATED">USER_UPDATED</option>
                <option value="USER_STATUS_CHANGED">USER_STATUS_CHANGED</option>
                <option value="ROLE_ASSIGNED">ROLE_ASSIGNED</option>
              </select>
            </div>

            <div style={{ flex: '1', minWidth: '140px' }}>
              <label className="form-label" htmlFor="res-filter" style={{ fontSize: '0.78rem' }}>
                Resource Type
              </label>
              <select
                id="res-filter"
                className="form-control"
                value={selectedResourceType}
                onChange={(e) => {
                  setSelectedResourceType(e.target.value)
                  setPage(0)
                }}
              >
                <option value="">All Resources</option>
                <option value="USER">USER</option>
                <option value="INSTITUTION">INSTITUTION</option>
                <option value="INSTITUTION_USER">INSTITUTION_USER</option>
              </select>
            </div>

            <div style={{ flex: '1', minWidth: '150px' }}>
              <label className="form-label" htmlFor="corr-filter" style={{ fontSize: '0.78rem' }}>
                Correlation ID
              </label>
              <input
                id="corr-filter"
                type="text"
                className="form-control"
                placeholder="UUID prefix"
                value={correlationIdQuery}
                onChange={(e) => setCorrelationIdQuery(e.target.value)}
              />
            </div>

            <button type="submit" className="btn btn-primary" style={{ padding: '0.55rem 1rem' }}>
              <Search size={15} />
              <span>Filter</span>
            </button>
          </form>
        </div>
      </div>

      {/* Events Table */}
      <div className="gov-card">
        <div className="gov-card-body" style={{ padding: 0 }}>
          {loading ? (
            <div style={{ padding: '3rem', textAlign: 'center', color: 'var(--text-muted)' }}>
              Loading regulatory audit logs...
            </div>
          ) : events.length === 0 ? (
            <div style={{ padding: '2.5rem', textAlign: 'center', color: 'var(--text-muted)' }}>
              No audit events matched the specified filter criteria.
            </div>
          ) : (
            <div className="gov-table-container">
              <table className="gov-table" id="audit-table">
                <thead>
                  <tr>
                    <th>Timestamp</th>
                    <th>Event Type</th>
                    <th>Principal</th>
                    <th>Resource Target</th>
                    <th>Action</th>
                    <th>Correlation ID</th>
                    <th>Audit Summary</th>
                  </tr>
                </thead>
                <tbody>
                  {events.map((ev) => (
                    <tr key={ev.id}>
                      <td style={{ whiteSpace: 'nowrap', fontSize: '0.8rem' }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '0.35rem' }}>
                          <Clock size={13} color="var(--text-muted)" />
                          <span>{new Date(ev.createdAt).toLocaleString()}</span>
                        </div>
                      </td>
                      <td>
                        <span className={`badge ${getEventBadgeClass(ev.eventType)}`}>
                          {ev.eventType}
                        </span>
                      </td>
                      <td style={{ fontWeight: 600 }}>{ev.principal}</td>
                      <td>
                        <div style={{ fontSize: '0.8rem' }}>
                          <span className="badge badge-info" style={{ fontSize: '0.65rem' }}>
                            {ev.resourceType}
                          </span>
                          <span style={{ fontFamily: 'var(--font-mono)', fontSize: '0.72rem', marginLeft: '0.35rem' }}>
                            {ev.resourceId.length > 18 ? `${ev.resourceId.substring(0, 18)}...` : ev.resourceId}
                          </span>
                        </div>
                      </td>
                      <td style={{ fontWeight: 600, fontSize: '0.8rem' }}>{ev.action}</td>
                      <td>
                        <code style={{ fontSize: '0.75rem', color: 'var(--gov-blue)' }}>
                          {ev.correlationId ? ev.correlationId.substring(0, 8) : 'N/A'}
                        </code>
                      </td>
                      <td style={{ fontSize: '0.825rem', color: 'var(--text-primary)', maxWidth: '320px' }}>
                        {ev.details || 'None'}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}

          <div style={{ padding: '0.5rem 1.25rem' }}>
            <Pagination
              currentPage={page}
              totalPages={totalPages}
              totalElements={totalElements}
              pageSize={pageSize}
              onPageChange={(p) => setPage(p)}
            />
          </div>
        </div>
      </div>
    </div>
  )
}
