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
  FileCheck,
  CalendarCheck,
} from 'lucide-react'

export const Navbar: React.FC = () => {
  const { user, logout, isAdmin, hasRole } = useAuth()

  const isRegional = hasRole('ROLE_REGIONAL_OFFICER')
  const isInstitution = hasRole('ROLE_INSTITUTION_OFFICER') && !isAdmin
  const isCompliance = hasRole('ROLE_COMPLIANCE_OFFICER')
  const isAuditor = hasRole('ROLE_AUDITOR')
  const isEvc = hasRole('ROLE_EVC_COORDINATOR')

  const primaryInstitutionId = user?.institutions && user.institutions.length > 0 ? user.institutions[0] : null
  const institutionProfileUrl = primaryInstitutionId ? `/institutions/${primaryInstitutionId}` : '/institutions'

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
          <NavLink to="/dashboard" className="gov-brand-title" style={{ textDecoration: 'none' }}>
            <img
              src="/sage-logo.png"
              alt="SAGE Logo"
              className="gov-brand-logo"
            />
            <div className="gov-brand-text">
              <h1>
                SAGE
                <span className="badge badge-info" style={{ fontSize: '0.7rem', fontWeight: 600, letterSpacing: '0.05em' }}>
                  GOVERNANCE PORTAL
                </span>
              </h1>
              <p>System for Approval Governance and Evaluation</p>
            </div>
          </NavLink>

          <div className="gov-branding-actions" style={{ display: 'flex', alignItems: 'center', gap: '1rem', flexWrap: 'wrap' }}>
            {/* Jurisdiction Badge */}
            <div
              className="gov-scope-badge"
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
            {/* Dashboard: All authorized roles */}
            <li className="gov-nav-item">
              <NavLink to="/dashboard" className={({ isActive }) => (isActive ? 'active' : '')} id="navbar-nav-dashboard">
                <span style={{ display: 'inline-flex', alignItems: 'center', gap: '0.35rem' }}>
                  <LayoutDashboard size={15} /> Dashboard
                </span>
              </NavLink>
            </li>

            {/* Institution Officer ONLY: My Institution (Strictly hidden for Admin & non-institutions) */}
            {isInstitution && !isAdmin && (
              <li className="gov-nav-item">
                <NavLink to={institutionProfileUrl} className={({ isActive }) => (isActive ? 'active' : '')} id="navbar-nav-my-institution">
                  <span style={{ display: 'inline-flex', alignItems: 'center', gap: '0.35rem' }}>
                    <Building2 size={15} /> My Institution
                  </span>
                </NavLink>
              </li>
            )}

            {/* Applications & Scrutiny: Admin, Regional, Institution, Auditor (HIDDEN for Compliance & EVC) */}
            {(isAdmin || isRegional || isInstitution || isAuditor) && !isCompliance && !isEvc && (
              <li className="gov-nav-item">
                <NavLink to="/applications" className={({ isActive }) => (isActive ? 'active' : '')} id="navbar-nav-applications">
                  <span style={{ display: 'inline-flex', alignItems: 'center', gap: '0.35rem' }}>
                    <FileCheck size={15} /> {isInstitution ? 'My Applications' : isRegional ? 'Regional Applications' : isAuditor ? 'Applications Review' : 'Applications'}
                  </span>
                </NavLink>
              </li>
            )}

            {/* Documents & Compliance: Admin, Regional, Institution, Compliance, Auditor (HIDDEN for EVC) */}
            {(isAdmin || isRegional || isInstitution || isCompliance || isAuditor) && !isEvc && (
              <li className="gov-nav-item">
                <NavLink to="/documents-compliance" className={({ isActive }) => (isActive ? 'active' : '')} id="navbar-nav-documents-compliance">
                  <span style={{ display: 'inline-flex', alignItems: 'center', gap: '0.35rem' }}>
                    <FileCheck size={15} /> {isInstitution ? 'Document Compliance' : 'Documents'}
                  </span>
                </NavLink>
              </li>
            )}

            {/* Required Actions: Admin, Regional, Institution, Compliance (HIDDEN for Auditor & EVC) */}
            {(isAdmin || isRegional || isInstitution || isCompliance) && !isAuditor && !isEvc && (
              <li className="gov-nav-item">
                <NavLink to="/required-actions" className={({ isActive }) => (isActive ? 'active' : '')} id="navbar-nav-required-actions">
                  <span style={{ display: 'inline-flex', alignItems: 'center', gap: '0.35rem' }}>
                    <ShieldAlert size={15} /> Actions
                  </span>
                </NavLink>
              </li>
            )}

            {/* Compliance & Deadlines: Admin, Compliance, Auditor (HIDDEN for Institution, Regional & EVC) */}
            {(isAdmin || isCompliance || isAuditor) && !isInstitution && !isRegional && !isEvc && (
              <li className="gov-nav-item">
                <NavLink to="/compliance" className={({ isActive }) => (isActive ? 'active' : '')} id="navbar-nav-compliance">
                  <span style={{ display: 'inline-flex', alignItems: 'center', gap: '0.35rem' }}>
                    <Clock size={15} /> {isAuditor ? 'Compliance Surveillance' : 'Compliance'}
                  </span>
                </NavLink>
              </li>
            )}

            {/* Regulatory Alerts: All authenticated roles */}
            {(isAdmin || isRegional || isInstitution || isCompliance || isEvc || isAuditor) && (
              <li className="gov-nav-item">
                <NavLink to="/alerts" className={({ isActive }) => (isActive ? 'active' : '')} id="navbar-nav-alerts">
                  <span style={{ display: 'inline-flex', alignItems: 'center', gap: '0.35rem' }}>
                    <Bell size={15} /> Alerts
                  </span>
                </NavLink>
              </li>
            )}

            {/* Institutions Directory: Admin, Regional, Compliance, Auditor (HIDDEN for Institution Officers & EVC) */}
            {(isAdmin || isRegional || isCompliance || isAuditor) && !isInstitution && !isEvc && (
              <li className="gov-nav-item">
                <NavLink to="/institutions" className={({ isActive }) => (isActive ? 'active' : '')} id="navbar-nav-institutions">
                  <span style={{ display: 'inline-flex', alignItems: 'center', gap: '0.35rem' }}>
                    <Building2 size={15} /> {isRegional ? 'Regional Institutions' : 'Institutions'}
                  </span>
                </NavLink>
              </li>
            )}

            {/* EVC Scheduling / Inspections: Admin, Regional, EVC Coordinator (HIDDEN for Institution, Compliance, Auditor) */}
            {(isAdmin || isRegional || isEvc) && !isInstitution && !isCompliance && !isAuditor && (
              <li className="gov-nav-item">
                <NavLink to="/evc" className={({ isActive }) => (isActive ? 'active' : '')} id="navbar-nav-evc">
                  <span style={{ display: 'inline-flex', alignItems: 'center', gap: '0.35rem' }}>
                    <CalendarCheck size={15} /> {isEvc ? 'Panel Allocation' : 'EVC Scheduling'}
                  </span>
                </NavLink>
              </li>
            )}

            {/* Administrator & Auditor Links */}
            {isAdmin && (
              <li className="gov-nav-item">
                <NavLink to="/admin/users" className={({ isActive }) => (isActive ? 'active' : '')} id="navbar-nav-users">
                  <span style={{ display: 'inline-flex', alignItems: 'center', gap: '0.35rem' }}>
                    <Users size={15} /> Users &amp; Roles
                  </span>
                </NavLink>
              </li>
            )}

            {(isAdmin || hasRole('ROLE_AUDITOR')) && (
              <li className="gov-nav-item">
                <NavLink to="/admin/audit" className={({ isActive }) => (isActive ? 'active' : '')} id="navbar-nav-audit">
                  <span style={{ display: 'inline-flex', alignItems: 'center', gap: '0.35rem' }}>
                    <ShieldAlert size={15} /> Audit Trail
                  </span>
                </NavLink>
              </li>
            )}

            {isAdmin && (
              <li className="gov-nav-item">
                <NavLink to="/system-health" className={({ isActive }) => (isActive ? 'active' : '')} id="navbar-nav-system-health">
                  <span style={{ display: 'inline-flex', alignItems: 'center', gap: '0.35rem' }}>
                    <Server size={15} /> System Diagnostics
                  </span>
                </NavLink>
              </li>
            )}
          </ul>

          <div style={{ fontSize: '0.75rem', color: '#cbd5e1' }}>
            <span style={{ color: 'var(--gov-saffron)', fontWeight: 700 }}>AICTE</span> Approval Automation
          </div>
        </div>
      </nav>
    </>
  )
}
