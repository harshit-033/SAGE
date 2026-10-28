import React from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import {
  Building2,
  Server,
  Layers,
  Lock,
  ArrowRight,
  Database,
  Cpu,
  CheckCircle2
} from 'lucide-react'

export const Dashboard: React.FC = () => {
  const { user, isAdmin } = useAuth()

  return (
    <div className="fade-in" style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
      {/* Welcome & Role Scope Header */}
      <div
        className="glass-panel"
        style={{
          padding: '2rem',
          background: 'linear-gradient(135deg, rgba(31, 41, 55, 0.7) 0%, rgba(17, 24, 39, 0.9) 100%)',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          flexWrap: 'wrap',
          gap: '1.5rem',
        }}
      >
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '0.5rem' }}>
            <h1 style={{ fontSize: '1.75rem', fontWeight: 800, color: '#ffffff' }}>
              Welcome, {user?.fullName}
            </h1>
            <span className="badge badge-success">
              <CheckCircle2 size={12} />
              Session Verified
            </span>
          </div>
          <p style={{ color: 'var(--text-secondary)', maxWidth: '650px', fontSize: '0.9rem' }}>
            {isAdmin
              ? 'You are operating under AICTE Central Administrator privileges with national cross-institutional regulatory oversight.'
              : 'You are authenticated with institutional tenancy privileges. Access is strictly scoped to your authorized institution.'}
          </p>
        </div>

        <div style={{ display: 'flex', gap: '0.75rem' }}>
          <Link to="/institutions" className="btn-primary" id="dashboard-view-institutions">
            <Building2 size={16} />
            <span>View Institutions</span>
            <ArrowRight size={14} />
          </Link>
          <Link to="/system-health" className="btn-secondary" id="dashboard-view-health">
            <Server size={16} />
            <span>System Health</span>
          </Link>
        </div>
      </div>

      {/* Security & Architecture Highlights */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '1.5rem' }}>
        <div className="glass-panel" style={{ padding: '1.5rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '1rem' }}>
            <div
              style={{
                width: '40px',
                height: '40px',
                borderRadius: 'var(--radius-md)',
                background: 'rgba(99, 102, 241, 0.15)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
              }}
            >
              <Lock size={20} color="var(--primary-500)" />
            </div>
            <div>
              <h2 style={{ fontSize: '1rem', fontWeight: 700, color: '#ffffff' }}>
                Tenancy Isolation
              </h2>
              <p style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                Zero-Trust Multi-Tenant Guard
              </p>
            </div>
          </div>
          <p style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', lineHeight: 1.5 }}>
            Resource-level isolation prevents cross-institutional inspection of applications or records. Unauthorized access attempts are rejected with HTTP 403 and logged in the immutable audit trail.
          </p>
        </div>

        <div className="glass-panel" style={{ padding: '1.5rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '1rem' }}>
            <div
              style={{
                width: '40px',
                height: '40px',
                borderRadius: 'var(--radius-md)',
                background: 'rgba(16, 185, 129, 0.15)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
              }}
            >
              <Database size={20} color="var(--success)" />
            </div>
            <div>
              <h2 style={{ fontSize: '1rem', fontWeight: 700, color: '#ffffff' }}>
                Flyway Governance
              </h2>
              <p style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                PostgreSQL Versioned Schema
              </p>
            </div>
          </div>
          <p style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', lineHeight: 1.5 }}>
            All schemas for users, roles, permissions, institutions, and audit trails are governed by versioned Flyway migrations (<code style={{ color: 'var(--primary-100)' }}>V1</code> and <code style={{ color: 'var(--primary-100)' }}>V2</code>).
          </p>
        </div>

        <div className="glass-panel" style={{ padding: '1.5rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '1rem' }}>
            <div
              style={{
                width: '40px',
                height: '40px',
                borderRadius: 'var(--radius-md)',
                background: 'rgba(6, 182, 212, 0.15)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
              }}
            >
              <Cpu size={20} color="#06b6d4" />
            </div>
            <div>
              <h2 style={{ fontSize: '1rem', fontWeight: 700, color: '#ffffff' }}>
                Automation Boundary
              </h2>
              <p style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                FastAPI Scrutiny Engine
              </p>
            </div>
          </div>
          <p style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', lineHeight: 1.5 }}>
            Python service operates strictly as a non-authoritative calculation assistant. Clean interfaces are established for background workers, OCR, and EVC routing.
          </p>
        </div>
      </div>

      {/* Phase 1 Status Banner */}
      <div
        className="glass-panel"
        style={{
          padding: '1.5rem',
          borderLeft: '4px solid var(--primary-500)',
          display: 'flex',
          alignItems: 'flex-start',
          gap: '1rem',
        }}
      >
        <Layers size={24} color="var(--primary-500)" style={{ flexShrink: 0, marginTop: '2px' }} />
        <div>
          <h3 style={{ fontSize: '0.95rem', fontWeight: 700, color: '#ffffff', marginBottom: '0.25rem' }}>
            Phase 1 Foundation Scope
          </h3>
          <p style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', lineHeight: 1.5 }}>
            This interface reflects the production-oriented architecture established in Phase 1. Subsequent regulatory workflow state machines, document parsing, and EVC scheduling are scheduled for Phases 2 through 4 per the implementation roadmap.
          </p>
        </div>
      </div>
    </div>
  )
}
