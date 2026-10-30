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
            maxWidth: '560px',
            boxShadow: 'var(--shadow-md)',
            overflow: 'hidden',
          }}
        >
          <div className="gov-tricolor-strip" />

          <div style={{ padding: '2rem' }}>
            <div style={{ textAlign: 'center', marginBottom: '1.75rem' }}>
              <img
                src="/sage-logo.png"
                alt="SAGE Logo"
                style={{
                  width: '90px',
                  height: '90px',
                  objectFit: 'contain',
                  marginBottom: '0.75rem',
                  display: 'inline-block',
                }}
              />
              <h1 style={{ fontSize: '1.45rem', fontWeight: 800, color: 'var(--gov-navy)' }}>
                SAGE Portal Login
              </h1>
              <p style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', marginTop: '0.35rem', fontWeight: 500 }}>
                System for Approval Governance and Evaluation
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
                  justifyContent: 'space-between',
                  marginBottom: '0.75rem',
                  color: 'var(--text-muted)',
                  fontSize: '0.75rem',
                  fontWeight: 700,
                  textTransform: 'uppercase',
                  letterSpacing: '0.04em',
                  flexWrap: 'wrap',
                  gap: '0.35rem',
                }}
              >
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.45rem' }}>
                  <KeyRound size={14} />
                  <span>Authorized Roles (All 7 Demonstration Profiles)</span>
                </div>
                <span className="badge badge-info" style={{ fontSize: '0.65rem' }}>Password123!</span>
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(210px, 1fr))', gap: '0.45rem' }}>
                <button
                  type="button"
                  className="btn btn-secondary btn-sm"
                  style={{ justifyContent: 'flex-start', textAlign: 'left', padding: '0.45rem 0.65rem' }}
                  onClick={() => fillCredentials('admin@aicte.gov.in', 'Password123!')}
                  id="preset-login-admin"
                >
                  <UserCheck size={14} color="var(--gov-blue)" style={{ flexShrink: 0 }} />
                  <div style={{ overflow: 'hidden' }}>
                    <div style={{ fontWeight: 600, fontSize: '0.78rem' }}>Central Admin</div>
                    <div style={{ color: 'var(--text-muted)', fontSize: '0.7rem', textOverflow: 'ellipsis', overflow: 'hidden', whiteSpace: 'nowrap' }}>admin@aicte.gov.in</div>
                  </div>
                </button>

                <button
                  type="button"
                  className="btn btn-secondary btn-sm"
                  style={{ justifyContent: 'flex-start', textAlign: 'left', padding: '0.45rem 0.65rem' }}
                  onClick={() => fillCredentials('regional@aicte.gov.in', 'Password123!')}
                  id="preset-login-regional"
                >
                  <UserCheck size={14} color="var(--gov-orange)" style={{ flexShrink: 0 }} />
                  <div style={{ overflow: 'hidden' }}>
                    <div style={{ fontWeight: 600, fontSize: '0.78rem' }}>Regional Officer</div>
                    <div style={{ color: 'var(--text-muted)', fontSize: '0.7rem', textOverflow: 'ellipsis', overflow: 'hidden', whiteSpace: 'nowrap' }}>regional@aicte.gov.in</div>
                  </div>
                </button>

                <button
                  type="button"
                  className="btn btn-secondary btn-sm"
                  style={{ justifyContent: 'flex-start', textAlign: 'left', padding: '0.45rem 0.65rem' }}
                  onClick={() => fillCredentials('officer@delhi-tech.edu', 'Password123!')}
                  id="preset-login-institution-delhi"
                >
                  <UserCheck size={14} color="var(--status-success-text)" style={{ flexShrink: 0 }} />
                  <div style={{ overflow: 'hidden' }}>
                    <div style={{ fontWeight: 600, fontSize: '0.78rem' }}>Institution Officer (Delhi)</div>
                    <div style={{ color: 'var(--text-muted)', fontSize: '0.7rem', textOverflow: 'ellipsis', overflow: 'hidden', whiteSpace: 'nowrap' }}>officer@delhi-tech.edu</div>
                  </div>
                </button>

                <button
                  type="button"
                  className="btn btn-secondary btn-sm"
                  style={{ justifyContent: 'flex-start', textAlign: 'left', padding: '0.45rem 0.65rem' }}
                  onClick={() => fillCredentials('officer@mumbai-engg.edu', 'Password123!')}
                  id="preset-login-institution-mumbai"
                >
                  <UserCheck size={14} color="#16a34a" style={{ flexShrink: 0 }} />
                  <div style={{ overflow: 'hidden' }}>
                    <div style={{ fontWeight: 600, fontSize: '0.78rem' }}>Institution Officer (Mumbai)</div>
                    <div style={{ color: 'var(--text-muted)', fontSize: '0.7rem', textOverflow: 'ellipsis', overflow: 'hidden', whiteSpace: 'nowrap' }}>officer@mumbai-engg.edu</div>
                  </div>
                </button>

                <button
                  type="button"
                  className="btn btn-secondary btn-sm"
                  style={{ justifyContent: 'flex-start', textAlign: 'left', padding: '0.45rem 0.65rem' }}
                  onClick={() => fillCredentials('compliance@aicte.gov.in', 'Password123!')}
                  id="preset-login-compliance"
                >
                  <UserCheck size={14} color="var(--gov-saffron)" style={{ flexShrink: 0 }} />
                  <div style={{ overflow: 'hidden' }}>
                    <div style={{ fontWeight: 600, fontSize: '0.78rem' }}>Compliance Officer</div>
                    <div style={{ color: 'var(--text-muted)', fontSize: '0.7rem', textOverflow: 'ellipsis', overflow: 'hidden', whiteSpace: 'nowrap' }}>compliance@aicte.gov.in</div>
                  </div>
                </button>

                <button
                  type="button"
                  className="btn btn-secondary btn-sm"
                  style={{ justifyContent: 'flex-start', textAlign: 'left', padding: '0.45rem 0.65rem' }}
                  onClick={() => fillCredentials('evc.coord@aicte.gov.in', 'Password123!')}
                  id="preset-login-evc"
                >
                  <UserCheck size={14} color="#7c3aed" style={{ flexShrink: 0 }} />
                  <div style={{ overflow: 'hidden' }}>
                    <div style={{ fontWeight: 600, fontSize: '0.78rem' }}>EVC Coordinator</div>
                    <div style={{ color: 'var(--text-muted)', fontSize: '0.7rem', textOverflow: 'ellipsis', overflow: 'hidden', whiteSpace: 'nowrap' }}>evc.coord@aicte.gov.in</div>
                  </div>
                </button>

                <button
                  type="button"
                  className="btn btn-secondary btn-sm"
                  style={{ justifyContent: 'flex-start', textAlign: 'left', padding: '0.45rem 0.65rem' }}
                  onClick={() => fillCredentials('auditor@aicte.gov.in', 'Password123!')}
                  id="preset-login-auditor"
                >
                  <UserCheck size={14} color="var(--status-info-text)" style={{ flexShrink: 0 }} />
                  <div style={{ overflow: 'hidden' }}>
                    <div style={{ fontWeight: 600, fontSize: '0.78rem' }}>Regulatory Auditor</div>
                    <div style={{ color: 'var(--text-muted)', fontSize: '0.7rem', textOverflow: 'ellipsis', overflow: 'hidden', whiteSpace: 'nowrap' }}>auditor@aicte.gov.in</div>
                  </div>
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
