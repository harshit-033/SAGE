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
        padding: '3.5rem 1.5rem',
      }}
    >
      <div
        style={{
          width: '64px',
          height: '64px',
          borderRadius: 'var(--radius-md)',
          backgroundColor: '#fef2f2',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          marginBottom: '1.25rem',
          border: '1px solid #fca5a5',
        }}
      >
        <ShieldAlert size={32} color="#dc2626" />
      </div>

      <h1 style={{ fontSize: '1.5rem', fontWeight: 700, color: 'var(--gov-navy)', marginBottom: '0.5rem' }}>
        Access Restricted &mdash; Authorization Required
      </h1>

      <p style={{ maxWidth: '520px', color: 'var(--text-secondary)', fontSize: '0.9rem', marginBottom: '1.75rem', lineHeight: 1.5 }}>
        You do not have permission to access this regulatory section or institutional record. If you require access to perform statutory duties, please contact your Regional AICTE Directorate or Central Administration.
      </p>

      <Link to="/dashboard" className="btn btn-primary" id="return-dashboard-btn">
        <ArrowLeft size={16} />
        <span>Return to Authorized Dashboard</span>
      </Link>
    </div>
  )
}
