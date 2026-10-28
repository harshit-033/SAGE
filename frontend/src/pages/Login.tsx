import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { TopGovBar } from '../components/common/TopGovBar'
import { AlertCircle, ArrowRight, UserCheck, KeyRound } from 'lucide-react'

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
      navigate('/dashboard')
    } catch (err: unknown) {
      const errorObj = err as { problem?: { detail?: string }; message?: string }
      setError(errorObj?.problem?.detail || errorObj?.message || 'Authentication failed. Please verify credentials.')
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
    <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column', backgroundColor: 'var(--bg-page)' }}>
      <TopGovBar />

      <div
        style={{
          flex: 1,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          padding: '2rem 1rem',
        }}
      >
        <div
          className="gov-card"
          style={{
            width: '100%',
            maxWidth: '480px',
            boxShadow: 'var(--shadow-md)',
            overflow: 'hidden',
          }}
        >
          <div className="gov-tricolor-strip" />

          <div style={{ padding: '2rem' }}>
            <div style={{ textAlign: 'center', marginBottom: '1.75rem' }}>
              <div
                style={{
                  width: '54px',
                  height: '54px',
                  borderRadius: 'var(--radius-sm)',
                  backgroundColor: 'var(--gov-navy)',
                  color: '#ffffff',
                  display: 'inline-flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  fontWeight: 800,
                  fontSize: '1.25rem',
                  border: '2px solid var(--gov-saffron)',
                  marginBottom: '0.75rem',
                }}
              >
                AICTE
              </div>
              <h1 style={{ fontSize: '1.4rem', color: 'var(--gov-navy)' }}>
                SAGE Portal Login
              </h1>
              <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', marginTop: '0.35rem' }}>
                Integrated Approval Automation &amp; Compliance Monitoring (SIH 26262)
              </p>
            </div>

            {error && (
              <div className="gov-alert gov-alert-danger" role="alert">
                <AlertCircle size={18} style={{ flexShrink: 0 }} />
                <span>{error}</span>
              </div>
            )}

            <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1.15rem' }}>
              <div className="form-group">
                <label className="form-label" htmlFor="username">
                  Official Email or Username <span className="required">*</span>
                </label>
                <input
                  id="username"
                  type="text"
                  className="form-control"
                  placeholder="e.g. admin@aicte.gov.in"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  required
                />
              </div>

              <div className="form-group">
                <label className="form-label" htmlFor="password">
                  Security Password <span className="required">*</span>
                </label>
                <input
                  id="password"
                  type="password"
                  className="form-control"
                  placeholder="Enter account password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                />
              </div>

              <button
                type="submit"
                className="btn btn-saffron"
                style={{ width: '100%', padding: '0.75rem', marginTop: '0.35rem' }}
                disabled={submitting}
                id="login-submit-btn"
              >
                {submitting ? 'Verifying Identity...' : 'Sign In to Portal'}
                {!submitting && <ArrowRight size={16} />}
              </button>
            </form>

            {/* Development Demonstration Roles Preset Switcher */}
            <div
              style={{
                marginTop: '1.75rem',
                paddingTop: '1.25rem',
                borderTop: '1px solid var(--border-light)',
              }}
            >
              <div
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: '0.45rem',
                  marginBottom: '0.75rem',
                  color: 'var(--text-muted)',
                  fontSize: '0.75rem',
                  fontWeight: 700,
                  textTransform: 'uppercase',
                  letterSpacing: '0.04em',
                }}
              >
                <KeyRound size={14} />
                <span>Development Seed Accounts (Dev / Evaluation Only)</span>
              </div>

              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.4rem' }}>
                <button
                  type="button"
                  className="btn btn-secondary btn-sm"
                  style={{ justifyContent: 'flex-start', textAlign: 'left' }}
                  onClick={() => fillCredentials('admin@aicte.gov.in', 'Password123!')}
                >
                  <UserCheck size={14} color="var(--gov-blue)" />
                  <span style={{ fontWeight: 600 }}>Central Admin:</span>
                  <span style={{ color: 'var(--text-muted)' }}>admin@aicte.gov.in</span>
                </button>

                <button
                  type="button"
                  className="btn btn-secondary btn-sm"
                  style={{ justifyContent: 'flex-start', textAlign: 'left' }}
                  onClick={() => fillCredentials('officer@delhi-tech.edu', 'Password123!')}
                >
                  <UserCheck size={14} color="var(--status-success-text)" />
                  <span style={{ fontWeight: 600 }}>Institution Officer:</span>
                  <span style={{ color: 'var(--text-muted)' }}>officer@delhi-tech.edu (Delhi Tech)</span>
                </button>

                <button
                  type="button"
                  className="btn btn-secondary btn-sm"
                  style={{ justifyContent: 'flex-start', textAlign: 'left' }}
                  onClick={() => fillCredentials('compliance@aicte.gov.in', 'Password123!')}
                >
                  <UserCheck size={14} color="var(--gov-saffron)" />
                  <span style={{ fontWeight: 600 }}>Compliance Officer:</span>
                  <span style={{ color: 'var(--text-muted)' }}>compliance@aicte.gov.in</span>
                </button>

                <button
                  type="button"
                  className="btn btn-secondary btn-sm"
                  style={{ justifyContent: 'flex-start', textAlign: 'left' }}
                  onClick={() => fillCredentials('auditor@aicte.gov.in', 'Password123!')}
                >
                  <UserCheck size={14} color="var(--status-info-text)" />
                  <span style={{ fontWeight: 600 }}>Regulatory Auditor:</span>
                  <span style={{ color: 'var(--text-muted)' }}>auditor@aicte.gov.in</span>
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
