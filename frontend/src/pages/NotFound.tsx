import React from 'react'
import { Link } from 'react-router-dom'
import { FileQuestion, ArrowLeft } from 'lucide-react'

export const NotFound: React.FC = () => {
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
          backgroundColor: 'var(--bg-surface-secondary)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          marginBottom: '1.25rem',
          border: '1px solid var(--border-medium)',
        }}
      >
        <FileQuestion size={32} color="var(--text-muted)" />
      </div>

      <h1 style={{ fontSize: '1.5rem', fontWeight: 700, color: 'var(--gov-navy)', marginBottom: '0.5rem' }}>
        Record or Page Not Found
      </h1>

      <p style={{ maxWidth: '480px', color: 'var(--text-secondary)', fontSize: '0.9rem', marginBottom: '1.75rem', lineHeight: 1.5 }}>
        The requested institutional record, document, or regulatory page could not be located on the SAGE portal.
      </p>

      <Link to="/" className="btn btn-primary" id="return-home-btn">
        <ArrowLeft size={16} />
        <span>Return to Dashboard</span>
      </Link>
    </div>
  )
}
