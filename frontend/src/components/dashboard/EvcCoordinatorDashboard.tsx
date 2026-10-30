import React from 'react'
import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { evcApi, type EvcInspectionDto } from '../../api/evcApi'
import { alertsApi } from '../../api/alertsApi'
import {
  CalendarCheck,
  Clock,
  Users,
  CheckCircle2,
  AlertTriangle,
  ArrowRight,
  Building2,
  Calendar,
  FileCheck,
  ShieldAlert,
} from 'lucide-react'

export const EvcCoordinatorDashboard: React.FC = () => {
  const { data: pendingInspections = [], isLoading: loadingPending } = useQuery({
    queryKey: ['evc-pending-inspections'],
    queryFn: () => evcApi.getPendingInspections(),
  })

  const { data: scheduledInspections = [], isLoading: loadingScheduled } = useQuery({
    queryKey: ['evc-scheduled-inspections'],
    queryFn: () => evcApi.getScheduledInspections(),
  })

  const { data: alertsPage, isLoading: loadingAlerts } = useQuery({
    queryKey: ['evc-alerts'],
    queryFn: () => alertsApi.getAlerts({ page: 0, size: 5, unreadOnly: true }),
  })

  const isLoading = loadingPending || loadingScheduled || loadingAlerts

  const totalPending = pendingInspections.length
  const totalScheduled = scheduledInspections.length
  const totalCommitteesAssigned = scheduledInspections.filter(
    (i) => i.assignedCommittee && i.assignedCommittee.length >= (i.requiredMemberCount || 3)
  ).length
  const totalUnassigned = pendingInspections.filter(
    (i) => !i.assignedCommittee || i.assignedCommittee.length === 0
  ).length

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
      {/* Statutory Header */}
      <div
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'flex-start',
          borderBottom: '1px solid var(--border-medium)',
          paddingBottom: '1rem',
          flexWrap: 'wrap',
          gap: '1rem',
        }}
      >
        <div>
          <h1 style={{ fontSize: '1.4rem', fontWeight: 700, color: 'var(--gov-navy)', margin: 0 }}>
            Expert Visit Committee (EVC) Operations
          </h1>
          <p style={{ margin: '0.35rem 0 0 0', color: 'var(--text-secondary)', fontSize: '0.875rem' }}>
            Statutory panel allocation, expert peer review roster, and on-site physical inspection monitoring.
          </p>
        </div>
        <div style={{ display: 'flex', gap: '0.75rem' }}>
          <Link to="/evc" className="btn btn-primary" id="evc-launch-scheduling-btn">
            <CalendarCheck size={16} />
            <span>Statutory Panel Allocation</span>
          </Link>
        </div>
      </div>

      {/* Metrics Row */}
      <div
        style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))',
          gap: '1rem',
        }}
      >
        <div className="card" style={{ padding: '1.25rem' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <span style={{ fontSize: '0.8rem', fontWeight: 600, color: 'var(--text-muted)', textTransform: 'uppercase' }}>
              Pending Allocation
            </span>
            <div style={{ padding: '0.4rem', borderRadius: 'var(--radius-sm)', backgroundColor: '#fef3c7', color: '#d97706' }}>
              <Clock size={18} />
            </div>
          </div>
          <div style={{ fontSize: '1.85rem', fontWeight: 700, color: 'var(--text-primary)', marginTop: '0.5rem' }}>
            {isLoading ? '...' : totalPending}
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '0.25rem' }}>
            Institutions awaiting panel assignment
          </div>
        </div>

        <div className="card" style={{ padding: '1.25rem' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <span style={{ fontSize: '0.8rem', fontWeight: 600, color: 'var(--text-muted)', textTransform: 'uppercase' }}>
              Scheduled Visits
            </span>
            <div style={{ padding: '0.4rem', borderRadius: 'var(--radius-sm)', backgroundColor: '#eff6ff', color: '#005aaa' }}>
              <CalendarCheck size={18} />
            </div>
          </div>
          <div style={{ fontSize: '1.85rem', fontWeight: 700, color: 'var(--text-primary)', marginTop: '0.5rem' }}>
            {isLoading ? '...' : totalScheduled}
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '0.25rem' }}>
            Confirmatory visits officially slated
          </div>
        </div>

        <div className="card" style={{ padding: '1.25rem' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <span style={{ fontSize: '0.8rem', fontWeight: 600, color: 'var(--text-muted)', textTransform: 'uppercase' }}>
              Panels Formed
            </span>
            <div style={{ padding: '0.4rem', borderRadius: 'var(--radius-sm)', backgroundColor: '#f0fdf4', color: '#16a34a' }}>
              <Users size={18} />
            </div>
          </div>
          <div style={{ fontSize: '1.85rem', fontWeight: 700, color: 'var(--text-primary)', marginTop: '0.5rem' }}>
            {isLoading ? '...' : totalCommitteesAssigned}
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '0.25rem' }}>
            Committees meeting statutory quorum
          </div>
        </div>

        <div className="card" style={{ padding: '1.25rem' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <span style={{ fontSize: '0.8rem', fontWeight: 600, color: 'var(--text-muted)', textTransform: 'uppercase' }}>
              Panel Quorum Deficit
            </span>
            <div style={{ padding: '0.4rem', borderRadius: 'var(--radius-sm)', backgroundColor: '#fef2f2', color: '#dc2626' }}>
              <AlertTriangle size={18} />
            </div>
          </div>
          <div style={{ fontSize: '1.85rem', fontWeight: 700, color: 'var(--text-primary)', marginTop: '0.5rem' }}>
            {isLoading ? '...' : totalUnassigned}
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '0.25rem' }}>
            Requires immediate peer assignment
          </div>
        </div>
      </div>

      {/* Main Content Sections: Scheduled Visits Table & Regulatory Directives */}
      <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: '1.5rem' }}>
        {/* Scheduled Inspections Table */}
        <div className="card" style={{ padding: '1.25rem' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
            <h2 style={{ fontSize: '1.05rem', fontWeight: 700, color: 'var(--gov-navy)', margin: 0, display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <Calendar size={18} />
              <span>Upcoming Scheduled On-Site Inspections</span>
            </h2>
            <Link to="/evc" style={{ fontSize: '0.8rem', color: 'var(--gov-blue)', textDecoration: 'none', fontWeight: 600, display: 'flex', alignItems: 'center', gap: '0.25rem' }}>
              <span>View All</span>
              <ArrowRight size={14} />
            </Link>
          </div>

          {loadingScheduled ? (
            <div style={{ padding: '2rem', textAlign: 'center', color: 'var(--text-muted)' }}>
              Loading scheduled inspection registry...
            </div>
          ) : scheduledInspections.length === 0 ? (
            <div style={{ padding: '2.5rem 1rem', textAlign: 'center', backgroundColor: 'var(--bg-surface-secondary)', borderRadius: 'var(--radius-sm)', border: '1px dashed var(--border-medium)' }}>
              <CalendarCheck size={36} color="var(--border-dark)" style={{ margin: '0 auto 0.75rem auto' }} />
              <p style={{ margin: 0, fontWeight: 600, color: 'var(--text-secondary)', fontSize: '0.9rem' }}>
                No inspections currently scheduled
              </p>
              <p style={{ margin: '0.35rem 0 1rem 0', color: 'var(--text-muted)', fontSize: '0.8rem' }}>
                Execute the statutory panel allocation engine to generate visit timetables.
              </p>
              <Link to="/evc" className="btn btn-secondary btn-sm">
                Open Panel Allocation
              </Link>
            </div>
          ) : (
            <div className="table-container">
              <table className="gov-table">
                <thead>
                  <tr>
                    <th>Application &amp; Institution</th>
                    <th>Visit Dates</th>
                    <th>Committee Roster</th>
                    <th>Status</th>
                  </tr>
                </thead>
                <tbody>
                  {scheduledInspections.slice(0, 5).map((visit: EvcInspectionDto) => (
                    <tr key={visit.id}>
                      <td>
                        <div style={{ fontWeight: 600, color: 'var(--gov-navy)', fontSize: '0.875rem' }}>
                          {visit.applicationNumber}
                        </div>
                        <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)', display: 'flex', alignItems: 'center', gap: '0.3rem', marginTop: '0.15rem' }}>
                          <Building2 size={12} />
                          <span>{visit.institutionName}</span>
                        </div>
                      </td>
                      <td style={{ fontSize: '0.825rem', whiteSpace: 'nowrap' }}>
                        <div>
                          {visit.scheduledStartDate ? new Date(visit.scheduledStartDate).toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' }) : '—'}
                        </div>
                        <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                          to {visit.scheduledEndDate ? new Date(visit.scheduledEndDate).toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' }) : '—'}
                        </div>
                      </td>
                      <td>
                        <div style={{ fontSize: '0.8rem' }}>
                          <span style={{ fontWeight: 600 }}>{visit.assignedCommittee?.length || 0}</span> / {visit.requiredMemberCount || 3} Experts
                        </div>
                        <div style={{ fontSize: '0.72rem', color: 'var(--text-muted)' }}>
                          {visit.assignedCommittee && visit.assignedCommittee.length > 0
                            ? visit.assignedCommittee.map((e) => e.name).slice(0, 2).join(', ') + (visit.assignedCommittee.length > 2 ? '...' : '')
                            : 'No experts assigned'}
                        </div>
                      </td>
                      <td>
                        <span className="badge badge-success" style={{ fontSize: '0.72rem' }}>
                          SCHEDULED
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>

        {/* Attention Items & Regulatory Notices */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
          {/* Quick Actions Panel */}
          <div className="card" style={{ padding: '1.25rem' }}>
            <h2 style={{ fontSize: '0.95rem', fontWeight: 700, color: 'var(--gov-navy)', margin: '0 0 0.85rem 0' }}>
              EVC Statutory Actions
            </h2>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.65rem' }}>
              <Link
                to="/evc"
                className="btn btn-secondary"
                style={{ justifyContent: 'flex-start', textAlign: 'left', fontSize: '0.825rem', gap: '0.65rem' }}
              >
                <CalendarCheck size={16} color="var(--gov-blue)" />
                <span>Run Statutory Panel Allocation</span>
              </Link>
              <Link
                to="/alerts"
                className="btn btn-secondary"
                style={{ justifyContent: 'flex-start', textAlign: 'left', fontSize: '0.825rem', gap: '0.65rem' }}
              >
                <BellNoticeIcon />
                <span>Inspection &amp; Schedule Alerts</span>
              </Link>
            </div>
          </div>

          {/* Attention Notices */}
          <div className="card" style={{ padding: '1.25rem' }}>
            <h2 style={{ fontSize: '0.95rem', fontWeight: 700, color: 'var(--gov-navy)', margin: '0 0 0.85rem 0', display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
              <ShieldAlert size={16} color="var(--gov-saffron)" />
              <span>Operational Guidelines</span>
            </h2>
            <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', lineHeight: 1.5, display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
              <div style={{ padding: '0.65rem', backgroundColor: 'var(--bg-surface-secondary)', borderRadius: 'var(--radius-sm)', borderLeft: '3px solid var(--gov-blue)' }}>
                <strong>Quorum Standard:</strong> All physical inspections require a minimum of 3 peer experts (Chairperson, Member, Member Secretary) with non-conflict declarations.
              </div>
              <div style={{ padding: '0.65rem', backgroundColor: 'var(--bg-surface-secondary)', borderRadius: 'var(--radius-sm)', borderLeft: '3px solid var(--gov-saffron)' }}>
                <strong>Institutional Affiliation:</strong> Peer experts are strictly prohibited from evaluating institutions situated within their home state or current employer.
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}

function BellNoticeIcon() {
  return <Clock size={16} color="var(--gov-saffron)" />
}
