import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { ShieldCheck, AlertCircle, Key, ArrowRight, UserCheck } from 'lucide-react'

export const Login: React.FC = () => {
  const { login } = useAuth()
  const navigate = useNavigate()

  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError(null)
    setSubmitting(true)
    try {
      await login(username, password)
      navigate('/')
    } catch (err: unknown) {
      const errorObj = err as { problem?: { detail?: string }; message?: string }
      setError(errorObj?.problem?.detail || errorObj?.message || 'Authentication failed')
    } finally {
      setSubmitting(false)
    }
  }

  const fillCredentials = (user: string, pass: string) => {
    setUsername(user)
    setPassword(pass)
    setError(null)
  }

  return (
    <div
      style={{
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        padding: '1.5rem',
        background: 'radial-gradient(ellipse at top, #111827 0%, #0b0f19 100%)',
      }}
    >
      <div
        className="glass-panel fade-in"
        style={{
          width: '100%',
          maxWidth: '460px',
          padding: '2.5rem',
          borderRadius: 'var(--radius-lg)',
        }}
      >
        <div style={{ textAlign: 'center', marginBottom: '2rem' }}>
          <div
            style={{
              width: '56px',
              height: '56px',
              borderRadius: 'var(--radius-md)',
              background: 'var(--primary-gradient)',
              display: 'inline-flex',
              alignItems: 'center',
              justifyContent: 'center',
              boxShadow: 'var(--shadow-glow)',
              marginBottom: '1rem',
            }}
          >
            <ShieldCheck size={32} color="#ffffff" />
          </div>
          <h1 style={{ fontSize: '1.5rem', fontWeight: 800, color: '#ffffff' }}>
            SAGE AICTE Portal
          </h1>
          <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', marginTop: '0.35rem' }}>
            Integrated AICTE Approval Workflow Automation & Compliance (PS 26262)
          </p>
        </div>

        {error && (
          <div
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '0.75rem',
              padding: '0.85rem 1rem',
              borderRadius: 'var(--radius-md)',
              background: 'var(--danger-bg)',
              border: '1px solid rgba(239, 68, 68, 0.3)',
              color: '#f87171',
              fontSize: '0.875rem',
              marginBottom: '1.5rem',
            }}
          >
            <AlertCircle size={18} />
            <span>{error}</span>
          </div>
        )}

        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
          <div>
            <label
              htmlFor="username"
              style={{ display: 'block', fontSize: '0.8rem', fontWeight: 600, color: 'var(--text-secondary)', marginBottom: '0.5rem' }}
            >
              Account Email or Username
            </label>
            <input
              id="username"
              type="text"
              className="input-field"
              placeholder="e.g. admin@aicte.gov.in"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
            />
          </div>

          <div>
            <label
              htmlFor="password"
              style={{ display: 'block', fontSize: '0.8rem', fontWeight: 600, color: 'var(--text-secondary)', marginBottom: '0.5rem' }}
            >
              Security Password
            </label>
            <input
              id="password"
              type="password"
              className="input-field"
              placeholder="••••••••••••"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </div>

          <button
            type="submit"
            className="btn-primary"
            style={{ width: '100%', padding: '0.85rem', marginTop: '0.5rem' }}
            disabled={submitting}
            id="login-submit-btn"
          >
            {submitting ? 'Verifying Credentials...' : 'Authenticate & Enter'}
            {!submitting && <ArrowRight size={16} />}
          </button>
        </form>

        {/* Development Seed Accounts Preset Selector */}
        <div
          style={{
            marginTop: '2rem',
            paddingTop: '1.5rem',
            borderTop: '1px solid var(--border-subtle)',
          }}
        >
          <div
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '0.5rem',
              marginBottom: '0.75rem',
              color: 'var(--text-muted)',
              fontSize: '0.75rem',
              fontWeight: 600,
              textTransform: 'uppercase',
              letterSpacing: '0.05em',
            }}
          >
            <Key size={14} />
            <span>Synthetic Demonstration Accounts (Dev Only)</span>
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
            <button
              type="button"
              className="btn-secondary"
              style={{ justifyContent: 'flex-start', padding: '0.5rem 0.75rem', fontSize: '0.8rem' }}
              onClick={() => fillCredentials('admin@aicte.gov.in', 'Password123!')}
            >
              <UserCheck size={14} color="var(--primary-500)" />
              <div style={{ textAlign: 'left' }}>
                <span style={{ fontWeight: 600 }}>Central Admin</span>
                <span style={{ color: 'var(--text-muted)', marginLeft: '0.5rem' }}>(admin@aicte.gov.in)</span>
              </div>
            </button>

            <button
              type="button"
              className="btn-secondary"
              style={{ justifyContent: 'flex-start', padding: '0.5rem 0.75rem', fontSize: '0.8rem' }}
              onClick={() => fillCredentials('officer@delhi-tech.edu', 'Password123!')}
            >
              <UserCheck size={14} color="var(--success)" />
              <div style={{ textAlign: 'left' }}>
                <span style={{ fontWeight: 600 }}>Delhi Tech Officer</span>
                <span style={{ color: 'var(--text-muted)', marginLeft: '0.5rem' }}>(Tenant Scoped)</span>
              </div>
            </button>

            <button
              type="button"
              className="btn-secondary"
              style={{ justifyContent: 'flex-start', padding: '0.5rem 0.75rem', fontSize: '0.8rem' }}
              onClick={() => fillCredentials('officer@mumbai-engg.edu', 'Password123!')}
            >
              <UserCheck size={14} color="var(--info)" />
              <div style={{ textAlign: 'left' }}>
                <span style={{ fontWeight: 600 }}>Mumbai Pharma Officer</span>
                <span style={{ color: 'var(--text-muted)', marginLeft: '0.5rem' }}>(Tenant Scoped)</span>
              </div>
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}
