import React, { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import { applicationsApi, type ApplicationDto } from '../../api/applicationsApi'
import { alertsApi, type AlertDto } from '../../api/alertsApi'
import { deadlinesApi, type DeadlineDto } from '../../api/deadlinesApi'
import { complianceApi, type ComplianceFindingDto } from '../../api/complianceApi'
import { institutionsApi, type Institution } from '../../api/institutionsApi'
import { StatusBadge } from '../common/StatusBadge'
import {
  Building2,
  FileCheck,
  AlertTriangle,
  Clock,
  Bell,
  ArrowRight,
  CheckCircle2,
} from 'lucide-react'

export const InstitutionDashboard: React.FC = () => {
  const { user } = useAuth()
  const [institution, setInstitution] = useState<Institution | null>(null)
  const [applications, setApplications] = useState<ApplicationDto[]>([])
  const [alerts, setAlerts] = useState<AlertDto[]>([])
  const [deadlines, setDeadlines] = useState<DeadlineDto[]>([])
  const [findings, setFindings] = useState<ComplianceFindingDto[]>([])
  const [loading, setLoading] = useState(true)

  const institutionId = user?.institutions?.[0]

  useEffect(() => {
    let isMounted = true

    const loadData = async () => {
      try {
        setLoading(true)
        const [appRes, alertRes, deadlineRes, findingRes, instRes] = await Promise.all([
          applicationsApi.list({ size: 10 }),
          alertsApi.list({ size: 5 }),
          deadlinesApi.list({ size: 5 }),
          complianceApi.getFindings({ size: 5 }),
          institutionId ? institutionsApi.getInstitutionById(institutionId) : Promise.resolve(null),
        ])

        if (isMounted) {
          setApplications(appRes?.content || [])
          setAlerts(alertRes?.content || [])
          setDeadlines(deadlineRes?.content || [])
          setFindings(findingRes?.content || [])
          if (instRes) setInstitution(instRes)
        }
      } catch (err) {
        console.error('Failed to load institution dashboard data', err)
      } finally {
        if (isMounted) setLoading(false)
      }
    }

    loadData()
    return () => {
      isMounted = false
    }
  }, [institutionId])

  const draftApps = applications.filter((a) => a.currentState === 'DRAFT')
  const submittedApps = applications.filter((a) => a.currentState === 'SUBMITTED' || a.currentState.includes('SCRUTINY'))
  const unreadAlerts = alerts.filter((a) => a.status === 'UNREAD')
  const openFindings = findings.filter((f) => f.status === 'OPEN' || f.status === 'UNDER_REVIEW')

  return (
    <div className="fade-in">
      {/* Institutional Profile Header */}
      <div className="gov-card" style={{ marginBottom: '1.5rem', borderLeft: '4px solid var(--gov-blue)' }}>
        <div className="gov-card-body" style={{ padding: '1.5rem 1.75rem' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '1rem' }}>
            <div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.65rem', marginBottom: '0.35rem' }}>
                <Building2 size={22} color="var(--gov-blue)" />
                <h1 style={{ color: 'var(--gov-navy)', margin: 0, fontSize: '1.35rem' }}>
                  {institution ? institution.name : 'Authorized Technical Institution'}
                </h1>
                {institution && (
                  <span className="badge badge-info" style={{ fontSize: '0.72rem' }}>
                    PID: {institution.code}
                  </span>
                )}
              </div>
              <p style={{ color: 'var(--text-secondary)', fontSize: '0.85rem', margin: '0.25rem 0 0.5rem 0' }}>
                Authorized Institutional Representative: <strong>{user?.fullName}</strong> &bull; Institutional Tenancy ID: {institutionId ? institutionId.slice(0, 8) + '...' : 'Not Linked'}
              </p>
              {institution && (
                <div style={{ display: 'flex', gap: '1.25rem', fontSize: '0.8rem', color: 'var(--text-muted)', flexWrap: 'wrap' }}>
                  <span><strong>Category:</strong> {institution.category || institution.applicationType}</span>
                  <span><strong>Location:</strong> {institution.city}, {institution.state}</span>
                  <span><strong>Status:</strong> <span style={{ color: 'var(--gov-green)', fontWeight: 600 }}>Active Regulatory Record</span></span>
                </div>
              )}
            </div>

            <div style={{ display: 'flex', gap: '0.65rem', flexWrap: 'wrap' }}>
              {institutionId && (
                <Link to={`/institutions/${institutionId}`} className="btn btn-secondary btn-sm" id="inst-dash-view-profile">
                  <Building2 size={14} />
                  <span>Institution Profile</span>
                </Link>
              )}
              <Link to="/documents-compliance" className="btn btn-secondary btn-sm" id="inst-dash-docs-compliance">
                <FileCheck size={14} />
                <span>Documents &amp; Compliance</span>
              </Link>
              <Link to="/applications" className="btn btn-primary btn-sm" id="inst-dash-new-app" style={{ backgroundColor: 'var(--gov-blue)' }}>
                <FileCheck size={14} />
                <span>Approval Applications</span>
                <ArrowRight size={13} />
              </Link>
            </div>
          </div>

          {/* Recommended Institution Navigation Bar */}
          <div
            style={{
              display: 'flex',
              gap: '0.5rem',
              marginTop: '1.25rem',
              paddingTop: '0.75rem',
              borderTop: '1px solid var(--border-light)',
              flexWrap: 'wrap',
            }}
          >
            <Link to="/dashboard" className="btn btn-sm btn-primary" style={{ fontSize: '0.75rem', padding: '0.3rem 0.75rem' }}>
              Overview
            </Link>
            <Link to="/applications" className="btn btn-sm btn-outline" style={{ fontSize: '0.75rem', padding: '0.3rem 0.75rem' }}>
              Applications
            </Link>
            <Link to="/documents-compliance" className="btn btn-sm btn-outline" style={{ fontSize: '0.75rem', padding: '0.3rem 0.75rem' }}>
              Documents &amp; Compliance
            </Link>
            <Link to="/required-actions" className="btn btn-sm btn-outline" style={{ fontSize: '0.75rem', padding: '0.3rem 0.75rem' }}>
              Required Actions
            </Link>
            <Link to="/alerts" className="btn btn-sm btn-outline" style={{ fontSize: '0.75rem', padding: '0.3rem 0.75rem' }}>
              Alerts
            </Link>
            {institutionId && (
              <Link to={`/institutions/${institutionId}`} className="btn btn-sm btn-outline" style={{ fontSize: '0.75rem', padding: '0.3rem 0.75rem' }}>
                Institution Profile
              </Link>
            )}
          </div>
        </div>
      </div>

      {/* KPI Cards */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '1.25rem', marginBottom: '1.5rem' }}>
        <div className="gov-card" style={{ padding: '1.25rem', borderTop: '3px solid var(--gov-blue)' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
            <span style={{ fontSize: '0.8rem', fontWeight: 700, color: 'var(--text-muted)', textTransform: 'uppercase' }}>
              My Applications
            </span>
            <FileCheck size={18} color="var(--gov-blue)" />
          </div>
          <div style={{ fontSize: '1.75rem', fontWeight: 800, color: 'var(--gov-navy)', lineHeight: 1.2 }}>
            {loading ? '...' : applications.length}
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', marginTop: '0.4rem' }}>
            {draftApps.length} in Draft &bull; {submittedApps.length} in Scrutiny
          </div>
        </div>

        <div className="gov-card" style={{ padding: '1.25rem', borderTop: '3px solid var(--gov-orange)' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
            <span style={{ fontSize: '0.8rem', fontWeight: 700, color: 'var(--text-muted)', textTransform: 'uppercase' }}>
              Compliance Health
            </span>
            <AlertTriangle size={18} color="var(--gov-orange)" />
          </div>
          <div style={{ fontSize: '1.75rem', fontWeight: 800, color: openFindings.length > 0 ? 'var(--status-danger-text)' : 'var(--gov-green)', lineHeight: 1.2 }}>
            {loading ? '...' : openFindings.length === 0 ? 'Optimal' : `${openFindings.length} Items`}
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', marginTop: '0.4rem' }}>
            {openFindings.length === 0 ? 'No open deficiencies' : 'Requires documentary rectification'}
          </div>
        </div>

        <div className="gov-card" style={{ padding: '1.25rem', borderTop: '3px solid #0284c7' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
            <span style={{ fontSize: '0.8rem', fontWeight: 700, color: 'var(--text-muted)', textTransform: 'uppercase' }}>
              Statutory Deadlines
            </span>
            <Clock size={18} color="#0284c7" />
          </div>
          <div style={{ fontSize: '1.75rem', fontWeight: 800, color: 'var(--gov-navy)', lineHeight: 1.2 }}>
            {loading ? '...' : deadlines.length}
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', marginTop: '0.4rem' }}>
            Tracked under AICTE APH 2024–27
          </div>
        </div>

        <div className="gov-card" style={{ padding: '1.25rem', borderTop: '3px solid #ef4444' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
            <span style={{ fontSize: '0.8rem', fontWeight: 700, color: 'var(--text-muted)', textTransform: 'uppercase' }}>
              Regulatory Notices
            </span>
            <Bell size={18} color="#ef4444" />
          </div>
          <div style={{ fontSize: '1.75rem', fontWeight: 800, color: unreadAlerts.length > 0 ? '#ef4444' : 'var(--gov-navy)', lineHeight: 1.2 }}>
            {loading ? '...' : unreadAlerts.length}
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', marginTop: '0.4rem' }}>
            {unreadAlerts.length === 0 ? 'All notices acknowledged' : 'Action required'}
          </div>
        </div>
      </div>

      {/* Main Two-Column Workflow Section */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(450px, 1fr))', gap: '1.5rem', marginBottom: '1.5rem' }}>
        {/* Active Applications Queue */}
        <div className="gov-card">
          <div className="gov-card-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <span className="gov-card-title">
              <FileCheck size={16} color="var(--gov-blue)" /> Active Approval Applications
            </span>
            <Link to="/applications" className="btn btn-secondary btn-sm" style={{ fontSize: '0.75rem' }}>
              View All ({applications.length})
            </Link>
          </div>
          <div className="gov-card-body" style={{ padding: '0.5rem 0' }}>
            {loading ? (
              <div style={{ padding: '2rem', textAlign: 'center', color: 'var(--text-muted)' }}>
                Loading approval applications...
              </div>
            ) : applications.length === 0 ? (
              <div style={{ padding: '2rem', textAlign: 'center' }}>
                <FileCheck size={32} color="var(--border-medium)" style={{ marginBottom: '0.5rem' }} />
                <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem', margin: '0 0 0.75rem 0' }}>
                  No active approval applications registered for this academic cycle.
                </p>
                <Link to="/applications" className="btn btn-primary btn-sm">
                  Create New Application
                </Link>
              </div>
            ) : (
              <div className="gov-table-container">
                <table className="gov-table" style={{ margin: 0 }}>
                  <thead>
                    <tr>
                      <th>Application #</th>
                      <th>Academic Year</th>
                      <th>Status</th>
                      <th>Action</th>
                    </tr>
                  </thead>
                  <tbody>
                    {applications.slice(0, 5).map((app) => (
                      <tr key={app.id}>
                        <td>
                          <div style={{ fontWeight: 600, color: 'var(--gov-navy)' }}>
                            {app.applicationNumber}
                          </div>
                          <div style={{ fontSize: '0.72rem', color: 'var(--text-muted)' }}>
                            {app.category}
                          </div>
                        </td>
                        <td>{app.academicYear}</td>
                        <td>
                          <StatusBadge status={app.currentState} />
                        </td>
                        <td>
                          <Link
                            to={`/applications/${app.id}`}
                            className="btn btn-secondary btn-sm"
                            style={{ fontSize: '0.75rem', padding: '0.2rem 0.5rem' }}
                          >
                            Open Details &rarr;
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

        {/* Action Items: Deadlines & Deficiencies */}
        <div className="gov-card">
          <div className="gov-card-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <span className="gov-card-title">
              <Clock size={16} color="var(--gov-orange)" /> Statutory Cutoffs &amp; Action Items
            </span>
            <Link to="/compliance" className="btn btn-secondary btn-sm" style={{ fontSize: '0.75rem' }}>
              Compliance Center
            </Link>
          </div>
          <div className="gov-card-body" style={{ padding: '0.75rem' }}>
            {loading ? (
              <div style={{ padding: '2rem', textAlign: 'center', color: 'var(--text-muted)' }}>
                Loading statutory deadlines...
              </div>
            ) : deadlines.length === 0 ? (
              <div style={{ padding: '1.5rem', textAlign: 'center', color: 'var(--text-secondary)', fontSize: '0.85rem' }}>
                <CheckCircle2 size={24} color="var(--gov-green)" style={{ marginBottom: '0.35rem' }} />
                <p style={{ margin: 0 }}>All statutory filings and mandatory submissions are up to date.</p>
              </div>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.65rem' }}>
                {deadlines.slice(0, 4).map((d) => (
                  <div
                    key={d.id}
                    style={{
                      display: 'flex',
                      justifyContent: 'space-between',
                      alignItems: 'center',
                      padding: '0.65rem 0.85rem',
                      backgroundColor: 'var(--bg-surface-secondary)',
                      border: '1px solid var(--border-medium)',
                      borderRadius: 'var(--radius-sm)',
                    }}
                  >
                    <div>
                      <div style={{ fontWeight: 600, fontSize: '0.85rem', color: 'var(--gov-navy)' }}>
                        {d.title}
                      </div>
                      <div style={{ fontSize: '0.72rem', color: 'var(--text-muted)', display: 'flex', gap: '0.5rem', marginTop: '0.2rem' }}>
                        <span>Target: {new Date(d.targetDate).toLocaleDateString()}</span>
                        {d.statutoryClause && <span>&bull; {d.statutoryClause}</span>}
                      </div>
                    </div>
                    <span className={`badge ${d.status === 'OVERDUE' ? 'badge-danger' : d.status === 'DUE_SOON' ? 'badge-warning' : 'badge-info'}`}>
                      {d.status}
                    </span>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Unread Regulatory Notices Feed */}
      <div className="gov-card">
        <div className="gov-card-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <span className="gov-card-title">
            <Bell size={16} color="var(--gov-navy)" /> Recent Regulatory Notices &amp; Acknowledgements
          </span>
          <Link to="/alerts" className="btn btn-secondary btn-sm" style={{ fontSize: '0.75rem' }}>
            Open Alerts Hub
          </Link>
        </div>
        <div className="gov-card-body" style={{ padding: '0.75rem' }}>
          {alerts.length === 0 ? (
            <div style={{ padding: '1.5rem', textAlign: 'center', color: 'var(--text-muted)', fontSize: '0.85rem' }}>
              No notices currently posted for this institution.
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
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                    <span className={`badge ${alert.status === 'UNREAD' ? 'badge-warning' : 'badge-success'}`}>
                      {alert.status}
                    </span>
                    <Link to="/alerts" className="btn btn-secondary btn-sm" style={{ fontSize: '0.72rem', padding: '0.2rem 0.5rem' }}>
                      Inspect &rarr;
                    </Link>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
