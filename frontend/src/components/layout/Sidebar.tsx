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
  const { user, isAdmin, hasRole } = useAuth()

  const isRegional = hasRole('ROLE_REGIONAL_OFFICER')
  const isInstitution = hasRole('ROLE_INSTITUTION_OFFICER') && !isAdmin
  const isCompliance = hasRole('ROLE_COMPLIANCE_OFFICER')
  const isAuditor = hasRole('ROLE_AUDITOR')
  const isEvc = hasRole('ROLE_EVC_COORDINATOR')

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

  // Determine role-specific portal heading
  const getSectionTitle = () => {
    if (isAdmin) return 'Central Governance'
    if (isRegional) return 'Regional Directorate'
    if (isInstitution) return 'Institutional Portal'
    if (isCompliance) return 'Compliance Directorate'
    if (isAuditor) return 'Statutory Oversight'
    if (isEvc) return 'Inspection Operations'
    return 'Portal Modules'
  }

  // Primary institution profile URL for institution officers
  const primaryInstitutionId = user?.institutions && user.institutions.length > 0 ? user.institutions[0] : null
  const institutionProfileUrl = primaryInstitutionId ? `/institutions/${primaryInstitutionId}` : '/institutions'

  return (
    <aside className="app-sidebar" aria-label="Sidebar Navigation">
      <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
        {/* Role-Specific Portal Section */}
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
            {getSectionTitle()}
          </div>

          <nav style={{ display: 'flex', flexDirection: 'column', gap: '0.25rem', marginTop: '0.35rem' }}>
            {/* Dashboard: All authorized roles */}
            <NavLink to="/dashboard" style={getNavLinkStyle} id="sidebar-nav-dashboard">
              <LayoutDashboard size={16} />
              <span>
                {isRegional
                  ? 'Regional Dashboard'
                  : isInstitution
                  ? 'Institution Dashboard'
                  : isCompliance
                  ? 'Compliance Dashboard'
                  : isAuditor
                  ? 'Audit Dashboard'
                  : isEvc
                  ? 'EVC Dashboard'
                  : 'Dashboard'}
              </span>
            </NavLink>

            {/* Institution Officer ONLY: My Institution Profile (Strictly hidden for Admin & non-institutions) */}
            {isInstitution && !isAdmin && (
              <NavLink to={institutionProfileUrl} style={getNavLinkStyle} id="sidebar-nav-my-institution">
                <Building2 size={16} />
                <span>My Institution</span>
              </NavLink>
            )}

            {/* Applications & Scrutiny: Admin, Regional, Institution, Auditor (HIDDEN for Compliance & EVC) */}
            {(isAdmin || isRegional || isInstitution || isAuditor) && !isCompliance && !isEvc && (
              <NavLink to="/applications" style={getNavLinkStyle} id="sidebar-nav-applications">
                <FileCheck size={16} />
                <span>
                  {isInstitution
                    ? 'My Applications'
                    : isAuditor
                    ? 'Applications Review'
                    : isRegional
                    ? 'Regional Applications'
                    : 'Applications & Scrutiny'}
                </span>
              </NavLink>
            )}

            {/* Documents & Compliance: Admin, Regional, Institution, Compliance, Auditor (HIDDEN for EVC) */}
            {(isAdmin || isRegional || isInstitution || isCompliance || isAuditor) && !isEvc && (
              <NavLink to="/documents-compliance" style={getNavLinkStyle} id="sidebar-nav-documents-compliance">
                <FileCheck size={16} />
                <span>
                  {isInstitution
                    ? 'Document Compliance'
                    : isAuditor
                    ? 'Document Registry'
                    : 'Documents & Compliance'}
                </span>
              </NavLink>
            )}

            {/* Required Actions: Admin, Regional, Institution, Compliance (HIDDEN for Auditor & EVC) */}
            {(isAdmin || isRegional || isInstitution || isCompliance) && !isAuditor && !isEvc && (
              <NavLink to="/required-actions" style={getNavLinkStyle} id="sidebar-nav-required-actions">
                <ShieldAlert size={16} />
                <span>Required Actions</span>
              </NavLink>
            )}

            {/* Compliance & Deadlines: Admin, Compliance, Auditor (HIDDEN for Institution, Regional & EVC) */}
            {(isAdmin || isCompliance || isAuditor) && !isInstitution && !isRegional && !isEvc && (
              <NavLink to="/compliance" style={getNavLinkStyle} id="sidebar-nav-compliance">
                <Clock size={16} />
                <span>{isAuditor ? 'Compliance Surveillance' : 'Compliance & Deadlines'}</span>
              </NavLink>
            )}

            {/* Regulatory Alerts: All authenticated roles */}
            {(isAdmin || isRegional || isInstitution || isCompliance || isEvc || isAuditor) && (
              <NavLink to="/alerts" style={getNavLinkStyle} id="sidebar-nav-alerts">
                <Bell size={16} />
                <span>Regulatory Alerts</span>
              </NavLink>
            )}

            {/* Institutions Directory: Admin, Regional, Compliance, Auditor (HIDDEN for Institution Officers & EVC) */}
            {(isAdmin || isRegional || isCompliance || isAuditor) && !isInstitution && !isEvc && (
              <NavLink to="/institutions" style={getNavLinkStyle} id="sidebar-nav-institutions">
                <Building2 size={16} />
                <span>{isRegional ? 'Regional Institutions' : 'Institutions Directory'}</span>
              </NavLink>
            )}

            {/* EVC Scheduling / Inspections: Admin, Regional, EVC Coordinator (HIDDEN for Institution, Compliance, Auditor) */}
            {(isAdmin || isRegional || isEvc) && !isInstitution && !isCompliance && !isAuditor && (
              <NavLink to="/evc" style={getNavLinkStyle} id="sidebar-nav-evc">
                <CalendarCheck size={16} />
                <span>{isEvc ? 'Panel Allocation' : 'EVC Scheduling'}</span>
              </NavLink>
            )}
          </nav>
        </div>

        {/* System Administration & Statutory Audit - Strict RBAC Visibility */}
        {(isAdmin || isAuditor) && (
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
              {isAdmin ? 'System Governance' : 'Statutory Audit'}
            </div>
            <nav style={{ display: 'flex', flexDirection: 'column', gap: '0.25rem', marginTop: '0.35rem' }}>
              {isAdmin && (
                <NavLink to="/admin/users" style={getNavLinkStyle} id="sidebar-nav-users">
                  <Users size={16} />
                  <span>Users &amp; Roles</span>
                </NavLink>
              )}

              {(isAdmin || isAuditor) && (
                <NavLink to="/admin/audit" style={getNavLinkStyle} id="sidebar-nav-audit">
                  <ShieldAlert size={16} />
                  <span>Regulatory Audit Trail</span>
                </NavLink>
              )}

              {isAdmin && (
                <NavLink to="/system-health" style={getNavLinkStyle} id="sidebar-nav-system-health">
                  <Server size={16} />
                  <span>Platform Operations</span>
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
