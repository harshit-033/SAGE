import React, { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import { applicationsApi, type ApplicationDto } from '../../api/applicationsApi'
import { alertsApi, type AlertDto } from '../../api/alertsApi'
import { deadlinesApi, type DeadlineDto } from '../../api/deadlinesApi'
import { institutionsApi } from '../../api/institutionsApi'
import { StatusBadge } from '../common/StatusBadge'
import {
  Building2,
  FileCheck,
  AlertTriangle,
  Clock,
  Bell,
  ArrowRight,
  CheckCircle2,
  Compass,
  CalendarCheck,
} from 'lucide-react'

export const RegionalDashboard: React.FC = () => {
  const { user } = useAuth()
  const [applications, setApplications] = useState<ApplicationDto[]>([])
  const [alerts, setAlerts] = useState<AlertDto[]>([])
  const [deadlines, setDeadlines] = useState<DeadlineDto[]>([])
  const [institutionsCount, setInstitutionsCount] = useState<number>(0)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    let isMounted = true

    const loadData = async () => {
      try {
        setLoading(true)
        const [appRes, alertRes, deadlineRes, instRes] = await Promise.all([
          applicationsApi.list({ size: 20 }),
          alertsApi.list({ size: 10 }),
          deadlinesApi.list({ size: 10 }),
          institutionsApi.listInstitutionsPaginated({ page: 0, size: 1 }),
        ])

        if (isMounted) {
          setApplications(appRes?.content || [])
          setAlerts(alertRes?.content || [])
          setDeadlines(deadlineRes?.content || [])
          setInstitutionsCount(instRes?.totalElements || 0)
        }
      } catch (err) {
        console.error('Failed to load regional dashboard data', err)
      } finally {
        if (isMounted) setLoading(false)
      }
    }

    loadData()
    return () => {
      isMounted = false
    }
  }, [])

  const pendingScrutiny = applications.filter(
    (a) => a.currentState === 'SUBMITTED' || a.currentState === 'UNDER_SCRUTINY' || a.currentState === 'SCRUTINY_DEFICIENT'
  )
  const escalatedAlerts = alerts.filter((a) => a.isEscalated)
  const upcomingDeadlines = deadlines.filter((d) => d.status === 'DUE_SOON' || d.status === 'OVERDUE')

  return (
    <div className="fade-in">
      {/* Regional Officer Header */}
      <div className="gov-card" style={{ marginBottom: '1.5rem', borderLeft: '4px solid var(--gov-saffron)' }}>
        <div className="gov-card-body" style={{ padding: '1.5rem 1.75rem' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '1rem' }}>
            <div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.65rem', marginBottom: '0.35rem' }}>
                <Compass size={22} color="var(--gov-orange)" />
                <h1 style={{ color: 'var(--gov-navy)', margin: 0, fontSize: '1.35rem' }}>
                  Regional Jurisdiction Oversight Dashboard
                </h1>
                <span className="badge badge-info" style={{ fontSize: '0.72rem' }}>
                  REGIONAL HQ
                </span>
              </div>
              <p style={{ color: 'var(--text-secondary)', fontSize: '0.85rem', margin: '0.25rem 0 0.5rem 0' }}>
                Designated Regional Officer: <strong>{user?.fullName}</strong> &bull; Northern Regional Jurisdiction (Delhi / NCR / Assigned States)
              </p>
              <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                APH 2024–27 Chapter I &amp; II Scrutiny Committees, EVC Coordination, and Standing Hearing Committee (SHC) oversight.
              </div>
            </div>

            <div style={{ display: 'flex', gap: '0.65rem', flexWrap: 'wrap' }}>
              <Link to="/applications" className="btn btn-primary btn-sm" id="reg-dash-scrutiny-btn" style={{ backgroundColor: 'var(--gov-blue)' }}>
                <FileCheck size={14} />
                <span>Regional Scrutiny Queue</span>
                <ArrowRight size={13} />
              </Link>
              <Link to="/alerts" className="btn btn-secondary btn-sm" id="reg-dash-escalations-btn">
                <Bell size={14} />
                <span>Supervisory Alerts</span>
              </Link>
              <Link to="/evc" className="btn btn-secondary btn-sm" id="reg-dash-evc-btn">
                <CalendarCheck size={14} />
                <span>EVC Scheduling</span>
              </Link>
            </div>
          </div>
        </div>
      </div>

      {/* KPI Cards */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '1.25rem', marginBottom: '1.5rem' }}>
        <div className="gov-card" style={{ padding: '1.25rem', borderTop: '3px solid var(--gov-orange)' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
            <span style={{ fontSize: '0.8rem', fontWeight: 700, color: 'var(--text-muted)', textTransform: 'uppercase' }}>
              Pending Scrutiny Queue
            </span>
            <FileCheck size={18} color="var(--gov-orange)" />
          </div>
          <div style={{ fontSize: '1.75rem', fontWeight: 800, color: pendingScrutiny.length > 0 ? 'var(--gov-orange)' : 'var(--gov-navy)', lineHeight: 1.2 }}>
            {loading ? '...' : pendingScrutiny.length}
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', marginTop: '0.4rem' }}>
            Awaiting Committee Review
          </div>
        </div>

        <div className="gov-card" style={{ padding: '1.25rem', borderTop: '3px solid #dc2626' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
            <span style={{ fontSize: '0.8rem', fontWeight: 700, color: 'var(--text-muted)', textTransform: 'uppercase' }}>
              Supervisory Escalations
            </span>
            <AlertTriangle size={18} color="#dc2626" />
          </div>
          <div style={{ fontSize: '1.75rem', fontWeight: 800, color: escalatedAlerts.length > 0 ? '#dc2626' : 'var(--gov-green)', lineHeight: 1.2 }}>
            {loading ? '...' : escalatedAlerts.length}
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', marginTop: '0.4rem' }}>
            {escalatedAlerts.length > 0 ? 'Urgent regional intervention' : 'Zero unhandled escalations'}
          </div>
        </div>

        <div className="gov-card" style={{ padding: '1.25rem', borderTop: '3px solid #0284c7' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
            <span style={{ fontSize: '0.8rem', fontWeight: 700, color: 'var(--text-muted)', textTransform: 'uppercase' }}>
              Critical Cutoffs
            </span>
            <Clock size={18} color="#0284c7" />
          </div>
          <div style={{ fontSize: '1.75rem', fontWeight: 800, color: 'var(--gov-navy)', lineHeight: 1.2 }}>
            {loading ? '...' : upcomingDeadlines.length}
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', marginTop: '0.4rem' }}>
            Due within statutory window
          </div>
        </div>

        <div className="gov-card" style={{ padding: '1.25rem', borderTop: '3px solid var(--gov-green)' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
            <span style={{ fontSize: '0.8rem', fontWeight: 700, color: 'var(--text-muted)', textTransform: 'uppercase' }}>
              Regional Institutions
            </span>
            <Building2 size={18} color="var(--gov-green)" />
          </div>
          <div style={{ fontSize: '1.75rem', fontWeight: 800, color: 'var(--gov-navy)', lineHeight: 1.2 }}>
            {loading ? '...' : institutionsCount}
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', marginTop: '0.4rem' }}>
            Registered in jurisdiction
          </div>
        </div>
      </div>

      {/* Scrutiny Queue Table */}
      <div className="gov-card" style={{ marginBottom: '1.5rem' }}>
        <div className="gov-card-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <span className="gov-card-title">
            <FileCheck size={16} color="var(--gov-blue)" /> Applications Pending Scrutiny &amp; Verification
          </span>
          <Link to="/applications" className="btn btn-secondary btn-sm" style={{ fontSize: '0.75rem' }}>
            Full Scrutiny Board &rarr;
          </Link>
        </div>
        <div className="gov-card-body" style={{ padding: '0.5rem 0' }}>
          {loading ? (
            <div style={{ padding: '2rem', textAlign: 'center', color: 'var(--text-muted)' }}>
              Loading regional scrutiny queue...
            </div>
          ) : pendingScrutiny.length === 0 ? (
            <div style={{ padding: '2rem', textAlign: 'center', color: 'var(--text-secondary)' }}>
              <CheckCircle2 size={32} color="var(--gov-green)" style={{ marginBottom: '0.5rem' }} />
              <p style={{ margin: 0, fontWeight: 600 }}>Regional scrutiny queue is fully up to date.</p>
              <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginTop: '0.25rem' }}>
                All submitted applications in your jurisdiction have been evaluated.
              </p>
            </div>
          ) : (
            <div className="gov-table-container">
              <table className="gov-table" style={{ margin: 0 }}>
                <thead>
                  <tr>
                    <th>Application #</th>
                    <th>Technical Institution</th>
                    <th>Category</th>
                    <th>Current Stage</th>
                    <th>Action</th>
                  </tr>
                </thead>
                <tbody>
                  {pendingScrutiny.slice(0, 6).map((app) => (
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
                          className="btn btn-primary btn-sm"
                          style={{ fontSize: '0.75rem', padding: '0.2rem 0.55rem', backgroundColor: 'var(--gov-blue)' }}
                        >
                          Conduct Scrutiny &rarr;
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

      {/* Escalated Alerts Feed */}
      <div className="gov-card">
        <div className="gov-card-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <span className="gov-card-title">
            <AlertTriangle size={16} color="#dc2626" /> Supervisory Escalation Queue (Regional HQ)
          </span>
          <Link to="/alerts" className="btn btn-secondary btn-sm" style={{ fontSize: '0.75rem' }}>
            View All Alerts
          </Link>
        </div>
        <div className="gov-card-body" style={{ padding: '0.75rem' }}>
          {escalatedAlerts.length === 0 ? (
            <div style={{ padding: '1.5rem', textAlign: 'center', color: 'var(--text-muted)', fontSize: '0.85rem' }}>
              No critical alerts escalated to the regional supervisory desk.
            </div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
              {escalatedAlerts.slice(0, 4).map((alert) => (
                <div
                  key={alert.id}
                  style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    padding: '0.75rem 1rem',
                    borderLeft: '4px solid #dc2626',
                    backgroundColor: 'var(--bg-surface-secondary)',
                    borderRadius: 'var(--radius-xs)',
                  }}
                >
                  <div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                      <span className="badge badge-danger">ESCALATED</span>
                      <span style={{ fontSize: '0.85rem', fontWeight: 700, color: 'var(--gov-navy)' }}>
                        {alert.title}
                      </span>
                    </div>
                    <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', marginTop: '0.2rem' }}>
                      {alert.message}
                    </div>
                    {alert.institutionName && (
                      <div style={{ fontSize: '0.72rem', color: 'var(--text-muted)', marginTop: '0.15rem' }}>
                        Institution: {alert.institutionName}
                      </div>
                    )}
                  </div>
                  <Link
                    to="/alerts"
                    className="btn btn-secondary btn-sm"
                    style={{ fontSize: '0.75rem', padding: '0.25rem 0.65rem' }}
                  >
                    Take Action &rarr;
                  </Link>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
