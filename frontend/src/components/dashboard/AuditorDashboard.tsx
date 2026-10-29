import React, { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import { auditApi, type AuditEvent } from '../../api/auditApi'
import { complianceApi, type ComplianceFindingDto } from '../../api/complianceApi'
import {
  ShieldAlert,
  Clock,
  ArrowRight,
  Database,
  Lock,
} from 'lucide-react'

export const AuditorDashboard: React.FC = () => {
  const { user } = useAuth()
  const [auditEvents, setAuditEvents] = useState<AuditEvent[]>([])
  const [totalEvents, setTotalEvents] = useState<number>(0)
  const [findings, setFindings] = useState<ComplianceFindingDto[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    let isMounted = true

    const loadData = async () => {
      try {
        setLoading(true)
        const [auditRes, findingRes] = await Promise.all([
          auditApi.listAuditEvents({ page: 0, size: 8 }),
          complianceApi.getFindings({ size: 5 }),
        ])

        if (isMounted) {
          setAuditEvents(auditRes?.content || [])
          setTotalEvents(auditRes?.totalElements || 0)
          setFindings(findingRes?.content || [])
        }
      } catch (err) {
        console.error('Failed to load auditor dashboard data', err)
      } finally {
        if (isMounted) setLoading(false)
      }
    }

    loadData()
    return () => {
      isMounted = false
    }
  }, [])

  return (
    <div className="fade-in">
      {/* Auditor Header */}
      <div className="gov-card" style={{ marginBottom: '1.5rem', borderLeft: '4px solid var(--gov-navy)' }}>
        <div className="gov-card-body" style={{ padding: '1.5rem 1.75rem' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '1rem' }}>
            <div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.65rem', marginBottom: '0.35rem' }}>
                <ShieldAlert size={22} color="var(--gov-navy)" />
                <h1 style={{ color: 'var(--gov-navy)', margin: 0, fontSize: '1.35rem' }}>
                  Regulatory Reviewer &amp; Independent Audit Dashboard
                </h1>
                <span className="badge badge-info" style={{ fontSize: '0.72rem' }}>
                  AUDIT BUREAU
                </span>
              </div>
              <p style={{ color: 'var(--text-secondary)', fontSize: '0.85rem', margin: '0.25rem 0 0.5rem 0' }}>
                Regulatory Auditor: <strong>{user?.fullName}</strong> &bull; Read-Only Immutable Regulatory Audit Trail Verification
              </p>
              <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                Immutable event ledger guaranteeing non-repudiation of all institutional submissions, scrutiny scores, and statutory transitions.
              </div>
            </div>

            <div style={{ display: 'flex', gap: '0.65rem', flexWrap: 'wrap' }}>
              <Link to="/admin/audit" className="btn btn-primary btn-sm" id="auditor-dash-full-audit" style={{ backgroundColor: 'var(--gov-navy)' }}>
                <ShieldAlert size={14} />
                <span>Regulatory Audit Log</span>
                <ArrowRight size={13} />
              </Link>
            </div>
          </div>
        </div>
      </div>

      {/* KPI Cards */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '1.25rem', marginBottom: '1.5rem' }}>
        <div className="gov-card" style={{ padding: '1.25rem', borderTop: '3px solid var(--gov-navy)' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
            <span style={{ fontSize: '0.8rem', fontWeight: 700, color: 'var(--text-muted)', textTransform: 'uppercase' }}>
              Immutable Audit Events
            </span>
            <Database size={18} color="var(--gov-navy)" />
          </div>
          <div style={{ fontSize: '1.75rem', fontWeight: 800, color: 'var(--gov-navy)', lineHeight: 1.2 }}>
            {loading ? '...' : totalEvents}
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', marginTop: '0.4rem' }}>
            Persisted regulatory events
          </div>
        </div>

        <div className="gov-card" style={{ padding: '1.25rem', borderTop: '3px solid var(--gov-blue)' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
            <span style={{ fontSize: '0.8rem', fontWeight: 700, color: 'var(--text-muted)', textTransform: 'uppercase' }}>
              Tenancy Boundaries
            </span>
            <Lock size={18} color="var(--gov-blue)" />
          </div>
          <div style={{ fontSize: '1.75rem', fontWeight: 800, color: 'var(--gov-green)', lineHeight: 1.2 }}>
            Enforced
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', marginTop: '0.4rem' }}>
            Zero cross-tenant leakage
          </div>
        </div>

        <div className="gov-card" style={{ padding: '1.25rem', borderTop: '3px solid var(--gov-orange)' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
            <span style={{ fontSize: '0.8rem', fontWeight: 700, color: 'var(--text-muted)', textTransform: 'uppercase' }}>
              Active Deficiencies
            </span>
            <Clock size={18} color="var(--gov-orange)" />
          </div>
          <div style={{ fontSize: '1.75rem', fontWeight: 800, color: 'var(--gov-navy)', lineHeight: 1.2 }}>
            {loading ? '...' : findings.length}
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', marginTop: '0.4rem' }}>
            Surveillance records
          </div>
        </div>
      </div>

      {/* Immutable Audit Ledger Table */}
      <div className="gov-card">
        <div className="gov-card-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <span className="gov-card-title">
            <ShieldAlert size={16} color="var(--gov-navy)" /> Recent Immutable Regulatory Ledger Entries
          </span>
          <Link to="/admin/audit" className="btn btn-secondary btn-sm" style={{ fontSize: '0.75rem' }}>
            Inspect Full Ledger ({totalEvents}) &rarr;
          </Link>
        </div>
        <div className="gov-card-body" style={{ padding: '0.5rem 0' }}>
          {loading ? (
            <div style={{ padding: '2rem', textAlign: 'center', color: 'var(--text-muted)' }}>
              Loading audit events...
            </div>
          ) : auditEvents.length === 0 ? (
            <div style={{ padding: '2rem', textAlign: 'center', color: 'var(--text-muted)' }}>
              No audit events recorded yet.
            </div>
          ) : (
            <div className="gov-table-container">
              <table className="gov-table" style={{ margin: 0 }}>
                <thead>
                  <tr>
                    <th>Timestamp</th>
                    <th>Action</th>
                    <th>Principal</th>
                    <th>Resource Target</th>
                    <th>Correlation ID</th>
                  </tr>
                </thead>
                <tbody>
                  {auditEvents.map((evt) => (
                    <tr key={evt.id}>
                      <td style={{ fontSize: '0.78rem', color: 'var(--text-secondary)' }}>
                        {new Date(evt.createdAt).toLocaleString()}
                      </td>
                      <td>
                        <span className="badge badge-secondary" style={{ fontWeight: 700 }}>
                          {evt.action || evt.eventType}
                        </span>
                      </td>
                      <td style={{ fontWeight: 600, color: 'var(--gov-navy)' }}>
                        {evt.principal}
                      </td>
                      <td>
                        <span style={{ fontSize: '0.8rem' }}>
                          {evt.resourceType}: {evt.resourceId ? evt.resourceId.slice(0, 8) + '...' : 'N/A'}
                        </span>
                      </td>
                      <td style={{ fontSize: '0.72rem', color: 'var(--text-muted)', fontFamily: 'monospace' }}>
                        {evt.correlationId ? evt.correlationId.slice(0, 12) + '...' : 'N/A'}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
