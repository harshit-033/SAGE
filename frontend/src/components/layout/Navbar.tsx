import React from 'react'
import { useAuth } from '../../context/AuthContext'
import { ShieldCheck, LogOut, Building, User as UserIcon } from 'lucide-react'

export const Navbar: React.FC = () => {
  const { user, logout, isAdmin } = useAuth()

  const getPrimaryRole = () => {
    if (!user || !user.roles.length) return 'Guest'
    if (user.roles.includes('ROLE_AICTE_ADMIN')) return 'Central Administrator'
    if (user.roles.includes('ROLE_REGIONAL_OFFICER')) return 'Regional Officer'
    if (user.roles.includes('ROLE_INSTITUTION_OFFICER')) return 'Institution Signatory'
    return user.roles[0].replace('ROLE_', '')
  }

  return (
    <header
      style={{
        height: '64px',
        borderBottom: '1px solid var(--border-subtle)',
        background: 'rgba(11, 15, 25, 0.85)',
        backdropFilter: 'blur(16px)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        padding: '0 1.5rem',
        position: 'sticky',
        top: 0,
        zIndex: 40,
      }}
    >
      <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
        <div
          style={{
            width: '36px',
            height: '36px',
            borderRadius: 'var(--radius-md)',
            background: 'var(--primary-gradient)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            boxShadow: 'var(--shadow-glow)',
          }}
        >
          <ShieldCheck size={20} color="#ffffff" />
        </div>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <span style={{ fontWeight: 800, fontSize: '1.1rem', letterSpacing: '0.05em', color: '#ffffff' }}>
              SAGE
            </span>
            <span className="badge badge-info" style={{ fontSize: '0.65rem' }}>
              PHASE 1
            </span>
          </div>
          <p style={{ fontSize: '0.7rem', color: 'var(--text-muted)' }}>
            AICTE Regulatory Automation Platform (SIH 26262)
          </p>
        </div>
      </div>

      <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
        {/* Active Tenancy Indicator */}
        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: '0.5rem',
            padding: '0.35rem 0.75rem',
            background: 'var(--bg-secondary)',
            borderRadius: 'var(--radius-md)',
            border: '1px solid var(--border-subtle)',
            fontSize: '0.8rem',
          }}
        >
          <Building size={14} color="var(--primary-500)" />
          <span style={{ color: 'var(--text-secondary)' }}>Jurisdiction:</span>
          <span style={{ fontWeight: 600, color: 'var(--text-primary)' }}>
            {isAdmin ? 'AICTE Central HQ (National)' : 'Institutional Jurisdiction'}
          </span>
        </div>

        {/* User Profile Card */}
        {user && (
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
            <div style={{ textAlign: 'right', display: 'flex', flexDirection: 'column' }}>
              <span style={{ fontSize: '0.825rem', fontWeight: 600, color: 'var(--text-primary)' }}>
                {user.fullName}
              </span>
              <span style={{ fontSize: '0.7rem', color: 'var(--text-muted)' }}>
                {getPrimaryRole()}
              </span>
            </div>
            <div
              style={{
                width: '34px',
                height: '34px',
                borderRadius: '50%',
                background: 'var(--bg-tertiary)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                border: '1px solid var(--border-medium)',
              }}
            >
              <UserIcon size={16} color="var(--text-secondary)" />
            </div>
          </div>
        )}

        {/* Logout */}
        <button
          onClick={logout}
          className="btn-secondary"
          title="Sign Out"
          style={{ padding: '0.5rem 0.75rem' }}
          id="logout-btn"
        >
          <LogOut size={16} />
          <span>Exit</span>
        </button>
      </div>
    </header>
  )
}
