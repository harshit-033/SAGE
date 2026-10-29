import React from 'react'
import { NavLink } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import {
  Building2,
  Users,
  LogOut,
  ShieldAlert,
  Server,
  LayoutDashboard,
  Bell,
  Clock,
} from 'lucide-react'

export const Navbar: React.FC = () => {
  const { user, logout, isAdmin, hasRole } = useAuth()

  const getPrimaryRole = () => {
    if (!user || !user.roles.length) return 'Guest'
    if (user.roles.includes('ROLE_AICTE_ADMIN')) return 'AICTE Central Administrator'
    if (user.roles.includes('ROLE_REGIONAL_OFFICER')) return 'Regional Officer'
    if (user.roles.includes('ROLE_SCRUTINY_OFFICER')) return 'Scrutiny Officer'
    if (user.roles.includes('ROLE_COMPLIANCE_OFFICER')) return 'Compliance Officer'
    if (user.roles.includes('ROLE_AUDITOR')) return 'Regulatory Auditor'
    if (user.roles.includes('ROLE_EVC_COORDINATOR')) return 'EVC Coordinator'
    if (user.roles.includes('ROLE_EVC_MEMBER')) return 'EVC Committee Member'
    if (user.roles.includes('ROLE_INSTITUTION_OFFICER')) return 'Institution Signatory'
    return user.roles[0].replace('ROLE_', '')
  }

  return (
    <>
      {/* Institutional Main Branding Header */}
      <header className="gov-branding-header">
        <div className="gov-branding-inner">
          <div className="gov-brand-title">
            <div className="gov-brand-emblem" aria-hidden="true">
              AICTE
            </div>
            <div className="gov-brand-text">
              <h1>
                SAGE
                <span className="badge badge-info" style={{ fontSize: '0.7rem', fontWeight: 600 }}>
                  PROTOTYPE
                </span>
              </h1>
              <p>Integrated AICTE Approval Workflow Automation &amp; Compliance Monitoring System</p>
            </div>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
            {/* Jurisdiction Badge */}
            <div
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '0.45rem',
                padding: '0.35rem 0.75rem',
                backgroundColor: 'var(--bg-surface-secondary)',
                border: '1px solid var(--border-medium)',
                borderRadius: 'var(--radius-sm)',
                fontSize: '0.8rem',
              }}
            >
              <Building2 size={15} color="var(--gov-blue)" />
              <span style={{ color: 'var(--text-muted)' }}>Scope:</span>
              <span style={{ fontWeight: 600, color: 'var(--gov-navy)' }}>
                {isAdmin ? 'Central Regulatory HQ (All Tenancies)' : 'Scoped Institutional Tenancy'}
              </span>
            </div>

            {/* Regulatory Alerts Bell Button */}
            <NavLink
              to="/alerts"
              style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                width: '32px',
                height: '32px',
                borderRadius: 'var(--radius-sm)',
                backgroundColor: 'var(--bg-surface-secondary)',
                border: '1px solid var(--border-medium)',
                color: 'var(--gov-navy)',
                position: 'relative',
                textDecoration: 'none',
              }}
              title="Regulatory Alerts & Notifications"
              id="navbar-alerts-btn"
            >
              <Bell size={16} />
              <span
                style={{
                  position: 'absolute',
                  top: '-4px',
                  right: '-4px',
                  width: '8px',
                  height: '8px',
                  borderRadius: '50%',
                  backgroundColor: '#ef4444',
                }}
              />
            </NavLink>

            {/* User Profile Info */}
            {user && (
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                <div style={{ textAlign: 'right' }}>
                  <div style={{ fontSize: '0.85rem', fontWeight: 700, color: 'var(--gov-navy)' }}>
                    {user.fullName}
                  </div>
                  <div style={{ fontSize: '0.72rem', color: 'var(--text-muted)' }}>
                    {getPrimaryRole()}
                  </div>
                </div>
              </div>
            )}

            {/* Logout Button */}
            <button
              type="button"
              onClick={logout}
              className="btn btn-secondary btn-sm"
              id="logout-btn"
              title="Sign Out of Session"
            >
              <LogOut size={14} />
              <span>Sign Out</span>
            </button>
          </div>
        </div>
      </header>

      {/* Primary Navigation Bar (Navy Blue with Saffron Active Borders) */}
      <nav className="gov-nav-bar" aria-label="Main Navigation">
        <div className="gov-nav-inner">
          <ul className="gov-nav-links">
            <li className="gov-nav-item">
              <NavLink to="/dashboard" className={({ isActive }) => (isActive ? 'active' : '')}>
                <span style={{ display: 'inline-flex', alignItems: 'center', gap: '0.35rem' }}>
                  <LayoutDashboard size={15} /> Dashboard
                </span>
              </NavLink>
            </li>
            <li className="gov-nav-item">
              <NavLink to="/compliance" className={({ isActive }) => (isActive ? 'active' : '')}>
                <span style={{ display: 'inline-flex', alignItems: 'center', gap: '0.35rem' }}>
                  <Clock size={15} /> Compliance
                </span>
              </NavLink>
            </li>
            <li className="gov-nav-item">
              <NavLink to="/alerts" className={({ isActive }) => (isActive ? 'active' : '')}>
                <span style={{ display: 'inline-flex', alignItems: 'center', gap: '0.35rem' }}>
                  <Bell size={15} /> Alerts
                </span>
              </NavLink>
            </li>
            <li className="gov-nav-item">
              <NavLink to="/institutions" className={({ isActive }) => (isActive ? 'active' : '')}>
                <span style={{ display: 'inline-flex', alignItems: 'center', gap: '0.35rem' }}>
                  <Building2 size={15} /> Institutions
                </span>
              </NavLink>
            </li>

            {/* Administrator & Auditor Links */}
            {isAdmin && (
              <li className="gov-nav-item">
                <NavLink to="/admin/users" className={({ isActive }) => (isActive ? 'active' : '')}>
                  <span style={{ display: 'inline-flex', alignItems: 'center', gap: '0.35rem' }}>
                    <Users size={15} /> User Management
                  </span>
                </NavLink>
              </li>
            )}

            {(isAdmin || hasRole('ROLE_AUDITOR')) && (
              <li className="gov-nav-item">
                <NavLink to="/admin/audit" className={({ isActive }) => (isActive ? 'active' : '')}>
                  <span style={{ display: 'inline-flex', alignItems: 'center', gap: '0.35rem' }}>
                    <ShieldAlert size={15} /> Audit Trail
                  </span>
                </NavLink>
              </li>
            )}

            <li className="gov-nav-item">
              <NavLink to="/system-health" className={({ isActive }) => (isActive ? 'active' : '')}>
                <span style={{ display: 'inline-flex', alignItems: 'center', gap: '0.35rem' }}>
                  <Server size={15} /> System Probes
                </span>
              </NavLink>
            </li>
          </ul>

          <div style={{ fontSize: '0.75rem', color: '#cbd5e1' }}>
            <span style={{ color: 'var(--gov-saffron)', fontWeight: 700 }}>AICTE</span> Approval Automation
          </div>
        </div>
      </nav>
    </>
  )
}
