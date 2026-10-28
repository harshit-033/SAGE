import React from 'react'
import { Link } from 'react-router-dom'
import { ShieldAlert, ArrowLeft } from 'lucide-react'

export const AccessDenied: React.FC = () => {
  return (
    <div
      style={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        textAlign: 'center',
        padding: '3rem 1.5rem',
      }}
    >
      <div
        style={{
          width: '64px',
          height: '64px',
          borderRadius: 'var(--radius-md)',
          backgroundColor: 'var(--status-danger-bg)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          marginBottom: '1.25rem',
          border: '1px solid var(--status-danger-border)',
        }}
      >
        <ShieldAlert size={32} color="var(--status-danger-text)" />
      </div>

      <h1 style={{ fontSize: '1.6rem', color: 'var(--gov-navy)', marginBottom: '0.5rem' }}>
        403 &mdash; Regulatory Access Forbidden
      </h1>

      <p style={{ maxWidth: '520px', color: 'var(--text-secondary)', fontSize: '0.9rem', marginBottom: '1.75rem', lineHeight: 1.5 }}>
        You do not possess the required regulatory role, institutional tenancy affiliation, or explicit permission to access this resource. All cross-tenant access violations are immutably logged for audit verification.
      </p>

      <Link to="/dashboard" className="btn btn-secondary" id="return-dashboard-btn">
        <ArrowLeft size={16} />
        <span>Return to Dashboard</span>
      </Link>
    </div>
  )
}
