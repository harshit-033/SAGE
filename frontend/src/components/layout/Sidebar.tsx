import React from 'react'
import { NavLink } from 'react-router-dom'
import {
  LayoutDashboard,
  Building2,
  FileCheck2,
  MapPin,
  Activity,
  Server,
  Lock
} from 'lucide-react'

export const Sidebar: React.FC = () => {
  const navItems = [
    {
      to: '/',
      label: 'Regulatory Dashboard',
      icon: <LayoutDashboard size={18} />,
      enabled: true,
      phase: null,
    },
    {
      to: '/institutions',
      label: 'Institutions & Tenancy',
      icon: <Building2 size={18} />,
      enabled: true,
      phase: null,
    },
    {
      to: '/system-health',
      label: 'System Probes & Health',
      icon: <Server size={18} />,
      enabled: true,
      phase: null,
    },
    {
      to: '/approvals',
      label: 'Approval Workflows',
      icon: <FileCheck2 size={18} />,
      enabled: false,
      phase: 'Phase 2',
    },
    {
      to: '/evc',
      label: 'EVC Inspections',
      icon: <MapPin size={18} />,
      enabled: false,
      phase: 'Phase 3',
    },
    {
      to: '/compliance',
      label: 'Compliance & Alerts',
      icon: <Activity size={18} />,
      enabled: false,
      phase: 'Phase 4',
    },
  ]

  return (
    <aside
      style={{
        width: '260px',
        borderRight: '1px solid var(--border-subtle)',
        background: 'var(--bg-secondary)',
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'space-between',
        padding: '1.25rem 0.75rem',
        minHeight: 'calc(100vh - 64px)',
      }}
    >
      <nav style={{ display: 'flex', flexDirection: 'column', gap: '0.35rem' }}>
        <div
          style={{
            padding: '0.5rem 0.75rem',
            fontSize: '0.7rem',
            fontWeight: 700,
            textTransform: 'uppercase',
            letterSpacing: '0.05em',
            color: 'var(--text-muted)',
          }}
        >
          Navigation
        </div>

        {navItems.map((item) => {
          if (!item.enabled) {
            return (
              <div
                key={item.label}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'space-between',
                  padding: '0.65rem 0.75rem',
                  borderRadius: 'var(--radius-md)',
                  color: 'var(--text-muted)',
                  fontSize: '0.85rem',
                  opacity: 0.6,
                  cursor: 'not-allowed',
                }}
                title={`${item.label} will be implemented in ${item.phase}`}
              >
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                  {item.icon}
                  <span>{item.label}</span>
                </div>
                <span
                  style={{
                    fontSize: '0.65rem',
                    padding: '0.15rem 0.4rem',
                    background: 'var(--bg-tertiary)',
                    borderRadius: 'var(--radius-sm)',
                    border: '1px solid var(--border-subtle)',
                  }}
                >
                  {item.phase}
                </span>
              </div>
            )
          }

          return (
            <NavLink
              key={item.to}
              to={item.to}
              style={({ isActive }) => ({
                display: 'flex',
                alignItems: 'center',
                gap: '0.75rem',
                padding: '0.65rem 0.75rem',
                borderRadius: 'var(--radius-md)',
                color: isActive ? '#ffffff' : 'var(--text-secondary)',
                background: isActive ? 'var(--primary-gradient)' : 'transparent',
                textDecoration: 'none',
                fontSize: '0.875rem',
                fontWeight: isActive ? 600 : 500,
                boxShadow: isActive ? 'var(--shadow-glow)' : 'none',
                transition: 'all 0.15s ease',
              })}
            >
              {item.icon}
              <span>{item.label}</span>
            </NavLink>
          )
        })}
      </nav>

      {/* Tenancy & Security Badge at Bottom */}
      <div
        className="glass-panel"
        style={{
          padding: '0.85rem',
          borderRadius: 'var(--radius-md)',
          fontSize: '0.75rem',
          background: 'rgba(0, 0, 0, 0.3)',
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.35rem' }}>
          <Lock size={14} color="var(--success)" />
          <span style={{ fontWeight: 600, color: '#ffffff' }}>Tenancy Guard Active</span>
        </div>
        <p style={{ color: 'var(--text-muted)', lineHeight: 1.4 }}>
          Resource-level multi-tenant isolation enforced on Spring Boot backend.
        </p>
      </div>
    </aside>
  )
}
