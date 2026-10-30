import React from 'react'
import { NavLink } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import {
  Building2,
  Users,
  FileCheck,
  ShieldAlert,
  Server,
  LayoutDashboard,
  Bell,
  Clock,
  HelpCircle,
  PhoneCall,
  CalendarCheck,
} from 'lucide-react'

export const Sidebar: React.FC = () => {
  const { isAdmin, hasRole } = useAuth()
  const showAdminSection = isAdmin || hasRole('ROLE_AUDITOR')

  const getNavLinkStyle = ({ isActive }: { isActive: boolean }) => ({
    display: 'flex',
    alignItems: 'center',
    gap: '0.65rem',
    padding: '0.55rem 0.75rem',
    borderRadius: 'var(--radius-sm)',
    color: isActive ? 'var(--gov-blue)' : 'var(--text-secondary)',
    backgroundColor: isActive ? 'var(--gov-blue-light)' : 'transparent',
    fontWeight: isActive ? 700 : 500,
    fontSize: '0.85rem',
    textDecoration: 'none',
    transition: 'background-color 0.15s ease, color 0.15s ease',
  })

  return (
    <aside
      style={{
        width: '240px',
        backgroundColor: 'var(--bg-surface)',
        borderRight: '1px solid var(--border-medium)',
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'space-between',
        padding: '1.25rem 0.75rem',
        minHeight: 'calc(100vh - 120px)',
      }}
      aria-label="Sidebar Navigation"
    >
      <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
        {/* Portal Modules Category */}
        <div>
          <div
            style={{
              padding: '0.25rem 0.5rem',
              fontSize: '0.72rem',
              fontWeight: 700,
              color: 'var(--text-muted)',
              textTransform: 'uppercase',
              letterSpacing: '0.05em',
            }}
          >
            Portal Modules
          </div>
          <nav style={{ display: 'flex', flexDirection: 'column', gap: '0.25rem', marginTop: '0.35rem' }}>
            <NavLink to="/dashboard" style={getNavLinkStyle} id="sidebar-nav-dashboard">
              <LayoutDashboard size={16} />
              <span>Dashboard</span>
            </NavLink>

            <NavLink to="/applications" style={getNavLinkStyle} id="sidebar-nav-applications">
              <FileCheck size={16} />
              <span>Applications &amp; Scrutiny</span>
            </NavLink>

            <NavLink to="/compliance" style={getNavLinkStyle} id="sidebar-nav-compliance">
              <Clock size={16} />
              <span>Compliance &amp; Deadlines</span>
            </NavLink>

            <NavLink to="/alerts" style={getNavLinkStyle} id="sidebar-nav-alerts">
              <Bell size={16} />
              <span>Regulatory Alerts</span>
            </NavLink>

            <NavLink to="/institutions" style={getNavLinkStyle} id="sidebar-nav-institutions">
              <Building2 size={16} />
              <span>Institutions Directory</span>
            </NavLink>

            <NavLink to="/evc" style={getNavLinkStyle} id="sidebar-nav-evc">
              <CalendarCheck size={16} />
              <span>EVC Scheduling</span>
            </NavLink>
          </nav>
        </div>

        {/* Administration Category - Strict RBAC Visibility */}
        {showAdminSection && (
          <div>
            <div
              style={{
                padding: '0.25rem 0.5rem',
                fontSize: '0.72rem',
                fontWeight: 700,
                color: 'var(--text-muted)',
                textTransform: 'uppercase',
                letterSpacing: '0.05em',
              }}
            >
              System Administration
            </div>
            <nav style={{ display: 'flex', flexDirection: 'column', gap: '0.25rem', marginTop: '0.35rem' }}>
              {isAdmin && (
                <NavLink to="/admin/users" style={getNavLinkStyle} id="sidebar-nav-users">
                  <Users size={16} />
                  <span>Users &amp; Roles</span>
                </NavLink>
              )}

              {(isAdmin || hasRole('ROLE_AUDITOR')) && (
                <NavLink to="/admin/audit" style={getNavLinkStyle} id="sidebar-nav-audit">
                  <ShieldAlert size={16} />
                  <span>Regulatory Audit Trail</span>
                </NavLink>
              )}

              {isAdmin && (
                <NavLink to="/system-health" style={getNavLinkStyle} id="sidebar-nav-system-health">
                  <Server size={16} />
                  <span>System Diagnostics</span>
                </NavLink>
              )}
            </nav>
          </div>
        )}
      </div>

      {/* Official Government Support & Statutory Reference Footer */}
      <div
        style={{
          marginTop: '1.5rem',
          padding: '0.85rem',
          backgroundColor: 'var(--bg-surface-secondary)',
          border: '1px solid var(--border-medium)',
          borderRadius: 'var(--radius-sm)',
          fontSize: '0.75rem',
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', color: 'var(--gov-navy)', fontWeight: 700, marginBottom: '0.35rem' }}>
          <HelpCircle size={14} color="var(--gov-blue)" />
          <span>Approval Bureau Helpdesk</span>
        </div>
        <p style={{ color: 'var(--text-secondary)', lineHeight: 1.4, fontSize: '0.72rem', margin: '0 0 0.5rem 0' }}>
          Statutory support for AICTE APH 2024–27 approvals and compliance queries.
        </p>
        <div style={{ display: 'flex', flexDirection: 'column', gap: '0.25rem', fontSize: '0.72rem', color: 'var(--text-muted)' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.35rem' }}>
            <PhoneCall size={12} color="var(--gov-green)" />
            <span>Toll Free: 1800-11-2345</span>
          </div>
          <div>Mon–Fri 09:30 – 17:30 IST</div>
        </div>
      </div>
    </aside>
  )
}
