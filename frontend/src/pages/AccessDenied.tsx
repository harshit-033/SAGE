import React from 'react'
import { Link } from 'react-router-dom'
import { ShieldAlert, ArrowLeft } from 'lucide-react'

export const AccessDenied: React.FC = () => {
  return (
    <div
      className="fade-in"
      style={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        textAlign: 'center',
        padding: '4rem 1.5rem',
      }}
    >
      <div
        style={{
          width: '72px',
          height: '72px',
          borderRadius: 'var(--radius-lg)',
          background: 'var(--danger-bg)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          marginBottom: '1.5rem',
          border: '1px solid rgba(239, 68, 68, 0.3)',
        }}
      >
        <ShieldAlert size={36} color="var(--danger)" />
      </div>

      <h1 style={{ fontSize: '1.75rem', fontWeight: 800, color: '#ffffff', marginBottom: '0.5rem' }}>
        403 — Access Forbidden
      </h1>

      <p style={{ maxWidth: '480px', color: 'var(--text-secondary)', fontSize: '0.9rem', marginBottom: '2rem', lineHeight: 1.5 }}>
        You do not possess the required regulatory role or institutional tenancy permissions to view or modify this resource.
      </p>

      <Link to="/" className="btn-secondary" id="return-dashboard-btn">
        <ArrowLeft size={16} />
        <span>Return to Dashboard</span>
      </Link>
    </div>
  )
}
