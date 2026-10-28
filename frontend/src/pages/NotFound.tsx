import React from 'react'
import { Link } from 'react-router-dom'
import { FileQuestion, ArrowLeft } from 'lucide-react'

export const NotFound: React.FC = () => {
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
          background: 'var(--bg-tertiary)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          marginBottom: '1.5rem',
          border: '1px solid var(--border-medium)',
        }}
      >
        <FileQuestion size={36} color="var(--text-muted)" />
      </div>

      <h1 style={{ fontSize: '1.75rem', fontWeight: 800, color: '#ffffff', marginBottom: '0.5rem' }}>
        404 — Page Not Found
      </h1>

      <p style={{ maxWidth: '480px', color: 'var(--text-secondary)', fontSize: '0.9rem', marginBottom: '2rem' }}>
        The regulatory resource or page you requested could not be located on the SAGE portal.
      </p>

      <Link to="/" className="btn-secondary">
        <ArrowLeft size={16} />
        <span>Return to Dashboard</span>
      </Link>
    </div>
  )
}
