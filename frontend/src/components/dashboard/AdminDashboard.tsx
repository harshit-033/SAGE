import React, { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import { applicationsApi, type ApplicationDto } from '../../api/applicationsApi'
import { institutionsApi } from '../../api/institutionsApi'
import { usersApi } from '../../api/usersApi'
import { complianceApi, type ComplianceCheckRunDto } from '../../api/complianceApi'
import { alertsApi, type AlertDto } from '../../api/alertsApi'
import { StatusBadge } from '../common/StatusBadge'
import {
  Building2,
  Users,
  FileCheck,
  ShieldAlert,
  Server,
  ShieldCheck,
  Activity,
  Bell,
} from 'lucide-react'

export const AdminDashboard: React.FC = () => {
  const { user } = useAuth()
  const [institutionsCount, setInstitutionsCount] = useState<number>(0)
  const [usersCount, setUsersCount] = useState<number>(0)
  const [applications, setApplications] = useState<ApplicationDto[]>([])
  const [complianceRuns, setComplianceRuns] = useState<ComplianceCheckRunDto[]>([])
  const [alerts, setAlerts] = useState<AlertDto[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    let isMounted = true

    const loadData = async () => {
      try {
        setLoading(true)
        const [instRes, userRes, appRes, compRes, alertRes] = await Promise.all([
          institutionsApi.listInstitutionsPaginated({ page: 0, size: 1 }),
          usersApi.listUsers({ page: 0, size: 1 }),
          applicationsApi.list({ size: 10 }),
          complianceApi.getCheckRuns({ size: 5 }),
          alertsApi.list({ size: 5 }),
        ])

        if (isMounted) {
          setInstitutionsCount(instRes?.totalElements || 0)
          setUsersCount(userRes?.totalElements || 0)
          setApplications(appRes?.content || [])
          setComplianceRuns(compRes?.content || [])
          setAlerts(alertRes?.content || [])
        }
      } catch (err) {
        console.error('Failed to load admin dashboard metrics', err)
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
      {/* Administrator Header */}
      <div className="gov-card" style={{ marginBottom: '1.5rem', borderLeft: '4px solid var(--gov-navy)' }}>
        <div className="gov-card-body" style={{ padding: '1.5rem 1.75rem' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '1rem' }}>
            <div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.65rem', marginBottom: '0.35rem' }}>
                <ShieldCheck size={22} color="var(--gov-blue)" />
                <h1 style={{ color: 'var(--gov-navy)', margin: 0, fontSize: '1.35rem' }}>
                  Central Regulatory Administrator Portal
                </h1>
                <span className="badge badge-success" style={{ fontSize: '0.72rem' }}>
                  GLOBAL REGULATORY AUTHORITY
                </span>
              </div>
              <p style={{ color: 'var(--text-secondary)', fontSize: '0.85rem', margin: '0.25rem 0 0.5rem 0' }}>
                Authenticated Principal: <strong>{user?.fullName}</strong> (@{user?.username}) &bull; Nationwide Multi-Tenant Regulatory Scope
              </p>
              <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                Section 10 AICTE Act Statutory Governance, Approval Approvals &amp; Compliance Oversight
              </div>
            </div>

            <div style={{ display: 'flex', gap: '0.65rem', flexWrap: 'wrap' }}>
              <Link to="/admin/users" className="btn btn-secondary btn-sm" id="admin-dash-users-btn">
                <Users size={14} />
                <span>Manage Users &amp; Roles</span>
              </Link>
              <Link to="/admin/audit" className="btn btn-secondary btn-sm" id="admin-dash-audit-btn">
                <ShieldAlert size={14} />
                <span>Audit Trail</span>
              </Link>
              <Link to="/system-health" className="btn btn-secondary btn-sm" id="admin-dash-probes-btn">
                <Server size={14} />
                <span>System Diagnostics</span>
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
              Registered Institutions
            </span>
            <Building2 size={18} color="var(--gov-navy)" />
          </div>
          <div style={{ fontSize: '1.75rem', fontWeight: 800, color: 'var(--gov-navy)', lineHeight: 1.2 }}>
            {loading ? '...' : institutionsCount}
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', marginTop: '0.4rem' }}>
            Multi-Tenant Technical Records
          </div>
        </div>

        <div className="gov-card" style={{ padding: '1.25rem', borderTop: '3px solid var(--gov-blue)' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
            <span style={{ fontSize: '0.8rem', fontWeight: 700, color: 'var(--text-muted)', textTransform: 'uppercase' }}>
              Approval Pipeline
            </span>
            <FileCheck size={18} color="var(--gov-blue)" />
          </div>
          <div style={{ fontSize: '1.75rem', fontWeight: 800, color: 'var(--gov-blue)', lineHeight: 1.2 }}>
            {loading ? '...' : applications.length}
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', marginTop: '0.4rem' }}>
            National Application Registry
          </div>
        </div>

        <div className="gov-card" style={{ padding: '1.25rem', borderTop: '3px solid var(--gov-orange)' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
            <span style={{ fontSize: '0.8rem', fontWeight: 700, color: 'var(--text-muted)', textTransform: 'uppercase' }}>
              Surveillance Check Runs
            </span>
            <Activity size={18} color="var(--gov-orange)" />
          </div>
          <div style={{ fontSize: '1.75rem', fontWeight: 800, color: 'var(--gov-navy)', lineHeight: 1.2 }}>
            {loading ? '...' : complianceRuns.length}
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', marginTop: '0.4rem' }}>
            Continuous Compliance Sweeps
          </div>
        </div>

        <div className="gov-card" style={{ padding: '1.25rem', borderTop: '3px solid var(--gov-green)' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
            <span style={{ fontSize: '0.8rem', fontWeight: 700, color: 'var(--text-muted)', textTransform: 'uppercase' }}>
              Authorized Users
            </span>
            <Users size={18} color="var(--gov-green)" />
          </div>
          <div style={{ fontSize: '1.75rem', fontWeight: 800, color: 'var(--gov-navy)', lineHeight: 1.2 }}>
            {loading ? '...' : usersCount}
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', marginTop: '0.4rem' }}>
            RBAC Enforced Identities
          </div>
        </div>
      </div>

      {/* Two Column Layout: Applications & Compliance Runs */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(450px, 1fr))', gap: '1.5rem', marginBottom: '1.5rem' }}>
        {/* Recent National Applications */}
        <div className="gov-card">
          <div className="gov-card-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <span className="gov-card-title">
              <FileCheck size={16} color="var(--gov-blue)" /> National Approval Pipeline
            </span>
            <Link to="/applications" className="btn btn-secondary btn-sm" style={{ fontSize: '0.75rem' }}>
              All Applications &rarr;
            </Link>
          </div>
          <div className="gov-card-body" style={{ padding: '0.5rem 0' }}>
            {loading ? (
              <div style={{ padding: '2rem', textAlign: 'center', color: 'var(--text-muted)' }}>
                Loading national applications...
              </div>
            ) : applications.length === 0 ? (
              <div style={{ padding: '2rem', textAlign: 'center', color: 'var(--text-secondary)' }}>
                No applications currently active.
              </div>
            ) : (
              <div className="gov-table-container">
                <table className="gov-table" style={{ margin: 0 }}>
                  <thead>
                    <tr>
                      <th>Application #</th>
                      <th>Institution</th>
                      <th>State</th>
                      <th>Current Status</th>
                      <th>Action</th>
                    </tr>
                  </thead>
                  <tbody>
                    {applications.slice(0, 5).map((app) => (
                      <tr key={app.id}>
                        <td style={{ fontWeight: 600, color: 'var(--gov-navy)' }}>
                          {app.applicationNumber}
                        </td>
                        <td>
                          <div style={{ fontWeight: 600 }}>{app.institutionName}</div>
                          <div style={{ fontSize: '0.72rem', color: 'var(--text-muted)' }}>AY: {app.academicYear}</div>
                        </td>
                        <td>{app.category}</td>
                        <td>
                          <StatusBadge status={app.currentState} />
                        </td>
                        <td>
                          <Link
                            to={`/applications/${app.id}`}
                            className="btn btn-secondary btn-sm"
                            style={{ fontSize: '0.72rem', padding: '0.2rem 0.5rem' }}
                          >
                            Inspect &rarr;
                          </Link>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>

        {/* Recent Compliance Surveillance Runs */}
        <div className="gov-card">
          <div className="gov-card-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <span className="gov-card-title">
              <Activity size={16} color="var(--gov-orange)" /> Recent Continuous Compliance Sweeps
            </span>
            <Link to="/compliance" className="btn btn-secondary btn-sm" style={{ fontSize: '0.75rem' }}>
              Compliance Center &rarr;
            </Link>
          </div>
          <div className="gov-card-body" style={{ padding: '0.75rem' }}>
            {loading ? (
              <div style={{ padding: '2rem', textAlign: 'center', color: 'var(--text-muted)' }}>
                Loading surveillance check runs...
              </div>
            ) : complianceRuns.length === 0 ? (
              <div style={{ padding: '1.5rem', textAlign: 'center', color: 'var(--text-muted)' }}>
                No continuous surveillance sweeps recorded yet.
              </div>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.65rem' }}>
                {complianceRuns.slice(0, 4).map((run) => (
                  <div
                    key={run.id}
                    style={{
                      padding: '0.75rem',
                      backgroundColor: 'var(--bg-surface-secondary)',
                      border: '1px solid var(--border-medium)',
                      borderRadius: 'var(--radius-sm)',
                      display: 'flex',
                      justifyContent: 'space-between',
                      alignItems: 'center',
                    }}
                  >
                    <div>
                      <div style={{ fontWeight: 600, fontSize: '0.85rem', color: 'var(--gov-navy)' }}>
                        {run.runType} ({run.triggerSource})
                      </div>
                      <div style={{ fontSize: '0.72rem', color: 'var(--text-muted)', marginTop: '0.2rem' }}>
                        Completed: {run.completedAt ? new Date(run.completedAt).toLocaleString() : 'In Progress'} &bull; {run.institutionsChecked} Institutions Checked
                      </div>
                    </div>
                    <div style={{ textAlign: 'right' }}>
                      <span className={`badge ${run.findingsGenerated > 0 ? 'badge-warning' : 'badge-success'}`}>
                        {run.findingsGenerated} Findings
                      </span>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>

      {/* System Alerts Feed */}
      <div className="gov-card">
        <div className="gov-card-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <span className="gov-card-title">
            <Bell size={16} color="var(--gov-navy)" /> Nationwide Regulatory Notices &amp; System Alerts
          </span>
          <Link to="/alerts" className="btn btn-secondary btn-sm" style={{ fontSize: '0.75rem' }}>
            Open Alerts Hub &rarr;
          </Link>
        </div>
        <div className="gov-card-body" style={{ padding: '0.75rem' }}>
          {alerts.length === 0 ? (
            <div style={{ padding: '1.5rem', textAlign: 'center', color: 'var(--text-muted)', fontSize: '0.85rem' }}>
              No critical system alerts or nationwide notices posted.
            </div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
              {alerts.slice(0, 3).map((alert) => (
                <div
                  key={alert.id}
                  style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    padding: '0.65rem 0.85rem',
                    borderLeft: `3px solid ${alert.severity === 'CRITICAL' ? '#dc2626' : alert.severity === 'WARNING' ? '#d97706' : 'var(--gov-blue)'}`,
                    backgroundColor: 'var(--bg-surface-secondary)',
                    borderRadius: 'var(--radius-xs)',
                  }}
                >
                  <div>
                    <div style={{ fontSize: '0.85rem', fontWeight: 600, color: 'var(--gov-navy)' }}>
                      {alert.title}
                    </div>
                    <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', marginTop: '0.15rem' }}>
                      {alert.message}
                    </div>
                  </div>
                  <span className={`badge ${alert.status === 'UNREAD' ? 'badge-warning' : 'badge-success'}`}>
                    {alert.status}
                  </span>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
