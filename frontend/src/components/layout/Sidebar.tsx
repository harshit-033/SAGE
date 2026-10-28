import React from 'react'
import { NavLink } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import {
  Building2,
  Users,
  ShieldCheck,
  FileCheck,
  ShieldAlert,
  Server,
  LayoutDashboard,
  CheckCircle2,
} from 'lucide-react'

export const Sidebar: React.FC = () => {
  const { isAdmin, hasRole } = useAuth()

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
      <div style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
        {/* Navigation Category */}
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
          <nav style={{ display: 'flex', flexDirection: 'column', gap: '0.2rem', marginTop: '0.35rem' }}>
            <NavLink
              to="/dashboard"
              style={({ isActive }) => ({
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
              })}
            >
              <LayoutDashboard size={16} />
              <span>Dashboard</span>
            </NavLink>

            <NavLink
              to="/institutions"
              style={({ isActive }) => ({
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
              })}
            >
              <Building2 size={16} />
              <span>Institutions</span>
            </NavLink>

            {isAdmin && (
              <NavLink
                to="/admin/users"
                style={({ isActive }) => ({
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
                })}
              >
                <Users size={16} />
                <span>Users &amp; Roles</span>
              </NavLink>
            )}

            {(isAdmin || hasRole('ROLE_AUDITOR')) && (
              <NavLink
                to="/admin/audit"
                style={({ isActive }) => ({
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
                })}
              >
                <ShieldAlert size={16} />
                <span>Regulatory Audit</span>
              </NavLink>
            )}

            <NavLink
              to="/system-health"
              style={({ isActive }) => ({
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
              })}
            >
              <Server size={16} />
              <span>System Probes</span>
            </NavLink>
          </nav>
        </div>

        {/* Regulatory Workflow Pipeline Status */}
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
            Statutory Phasing
          </div>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.35rem', marginTop: '0.35rem' }}>
            <div
              style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                padding: '0.45rem 0.5rem',
                fontSize: '0.78rem',
                backgroundColor: 'var(--status-success-bg)',
                color: 'var(--status-success-text)',
                borderRadius: 'var(--radius-xs)',
                border: '1px solid var(--status-success-border)',
                fontWeight: 600,
              }}
            >
              <span style={{ display: 'flex', alignItems: 'center', gap: '0.35rem' }}>
                <CheckCircle2 size={13} /> Phase 1: Security
              </span>
              <span>VERIFIED</span>
            </div>

            <div
              style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                padding: '0.45rem 0.5rem',
                fontSize: '0.78rem',
                backgroundColor: 'var(--status-info-bg)',
                color: 'var(--status-info-text)',
                borderRadius: 'var(--radius-xs)',
                border: '1px solid var(--status-info-border)',
                fontWeight: 700,
              }}
            >
              <span style={{ display: 'flex', alignItems: 'center', gap: '0.35rem' }}>
                <ShieldCheck size={13} /> Phase 2: Tenancy
              </span>
              <span>ACTIVE</span>
            </div>

            <div
              style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                padding: '0.45rem 0.5rem',
                fontSize: '0.78rem',
                color: 'var(--text-muted)',
                borderRadius: 'var(--radius-xs)',
                border: '1px solid var(--border-light)',
              }}
            >
              <span style={{ display: 'flex', alignItems: 'center', gap: '0.35rem' }}>
                <FileCheck size={13} /> Phase 3: Scrutiny &amp; EVC
              </span>
              <span>PENDING</span>
            </div>
          </div>
        </div>
      </div>

      {/* Tenancy Guard Badge */}
      <div
        style={{
          padding: '0.75rem',
          backgroundColor: 'var(--bg-surface-secondary)',
          border: '1px solid var(--border-medium)',
          borderRadius: 'var(--radius-sm)',
          fontSize: '0.75rem',
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', color: 'var(--gov-navy)', fontWeight: 700, marginBottom: '0.2rem' }}>
          <ShieldCheck size={14} color="var(--gov-green)" />
          <span>Tenancy Boundaries Enforced</span>
        </div>
        <p style={{ color: 'var(--text-secondary)', lineHeight: 1.35, fontSize: '0.72rem' }}>
          Backend verifies multi-tenant isolation on all REST endpoints.
        </p>
      </div>
    </aside>
  )
}
