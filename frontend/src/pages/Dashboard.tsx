import React from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { Breadcrumbs } from '../components/common/Breadcrumbs'
import {
  Building2,
  Users,
  ShieldAlert,
  Server,
  Lock,
  ArrowRight,
  CheckCircle2,
  FileCheck,
  Cpu,
} from 'lucide-react'

export const Dashboard: React.FC = () => {
  const { user, isAdmin, hasRole } = useAuth()

  const getRoleDisplayName = () => {
    if (!user || !user.roles.length) return 'Portal User'
    if (user.roles.includes('ROLE_AICTE_ADMIN')) return 'AICTE Central Administrator'
    if (user.roles.includes('ROLE_REGIONAL_OFFICER')) return 'AICTE Regional Officer'
    if (user.roles.includes('ROLE_SCRUTINY_OFFICER')) return 'Scrutiny Officer'
    if (user.roles.includes('ROLE_COMPLIANCE_OFFICER')) return 'Compliance Officer'
    if (user.roles.includes('ROLE_AUDITOR')) return 'Regulatory Auditor'
    if (user.roles.includes('ROLE_INSTITUTION_OFFICER')) return 'Authorized Institutional Representative'
    return user.roles[0].replace('ROLE_', '')
  }

  return (
    <div>
      <Breadcrumbs items={[]} />

      {/* Welcome & Role Scope Header Card */}
      <div className="gov-card" style={{ marginBottom: '1.5rem' }}>
        <div className="gov-card-body" style={{ padding: '1.75rem' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '1.25rem' }}>
            <div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.65rem', marginBottom: '0.35rem' }}>
                <h1 style={{ color: 'var(--gov-navy)' }}>
                  Welcome, {user?.fullName}
                </h1>
                <span className="badge badge-success">
                  <CheckCircle2 size={13} />
                  Session Verified
                </span>
              </div>
              <p style={{ color: 'var(--text-secondary)', maxWidth: '700px', fontSize: '0.875rem' }}>
                {isAdmin
                  ? 'Authenticated with Central Regulatory Administrator privileges across all registered technical institutions and regional zones.'
                  : 'Authenticated with Institutional Tenancy privileges. Access to records and confidential profiles is strictly isolated to your authorized institution.'}
              </p>
              <div style={{ marginTop: '0.5rem', fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                <strong>Role:</strong> {getRoleDisplayName()} &bull; <strong>Username:</strong> @{user?.username}
              </div>
            </div>

            <div style={{ display: 'flex', gap: '0.75rem', flexWrap: 'wrap' }}>
              <Link to="/applications" className="btn btn-primary" id="dashboard-view-applications" style={{ backgroundColor: 'var(--gov-orange)', color: '#ffffff' }}>
                <FileCheck size={16} />
                <span>Workflow &amp; OCR Engine</span>
                <ArrowRight size={14} />
              </Link>
              <Link to="/institutions" className="btn btn-secondary" id="dashboard-view-institutions">
                <Building2 size={16} />
                <span>Institutions</span>
              </Link>
              {isAdmin && (
                <Link to="/admin/users" className="btn btn-secondary" id="dashboard-view-users">
                  <Users size={16} />
                  <span>Users</span>
                </Link>
              )}
              {(isAdmin || hasRole('ROLE_AUDITOR')) && (
                <Link to="/admin/audit" className="btn btn-secondary" id="dashboard-view-audit">
                  <ShieldAlert size={16} />
                  <span>Audit Trail</span>
                </Link>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* Security & Regulatory Scope Metrics */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '1.25rem', marginBottom: '1.5rem' }}>
        <div className="gov-card" style={{ borderTop: '3px solid var(--gov-orange)' }}>
          <div className="gov-card-header">
            <span className="gov-card-title">
              <Cpu size={16} color="var(--gov-orange)" /> Approval Workflow &amp; OCR
            </span>
            <span className="badge badge-success">Indra OCR Active</span>
          </div>
          <div className="gov-card-body">
            <p style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', lineHeight: 1.5, marginBottom: '1rem' }}>
              Statutory approval pipeline with deterministic APH scrutiny rules, document upload, and Indra OCR field extraction.
            </p>
            <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
              <Link to="/applications" className="btn btn-primary btn-sm" style={{ backgroundColor: 'var(--gov-navy)' }}>
                Open Applications &rarr;
              </Link>
              <Link to="/applications/bbbbbbbb-bbbb-bbbb-bbbb-000000000001" className="btn btn-secondary btn-sm">
                View OCR Sample (Delhi Tech)
              </Link>
            </div>
          </div>
        </div>

        <div className="gov-card">
          <div className="gov-card-header">
            <span className="gov-card-title">
              <Building2 size={16} /> Technical Institutions
            </span>
            <span className="badge badge-info">Multi-Tenant</span>
          </div>
          <div className="gov-card-body">
            <p style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', lineHeight: 1.5, marginBottom: '1rem' }}>
              Institutional registries with GIS coordinates, category classification, contact details, and affiliated authorized signatories.
            </p>
            <Link to="/institutions" className="btn btn-secondary btn-sm">
              Open Directory &rarr;
            </Link>
          </div>
        </div>

        <div className="gov-card">
          <div className="gov-card-header">
            <span className="gov-card-title">
              <Lock size={16} /> Tenancy Boundary Guard
            </span>
            <span className="badge badge-success">Active</span>
          </div>
          <div className="gov-card-body">
            <p style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', lineHeight: 1.5, marginBottom: '1rem' }}>
              Method-level security enforces zero cross-tenant data leakage. Probing unassociated records triggers HTTP 403 and records an audit event.
            </p>
            <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)' }}>
              Enforced by Spring Security + PostgreSQL RLS-compatible models.
            </div>
          </div>
        </div>

        <div className="gov-card">
          <div className="gov-card-header">
            <span className="gov-card-title">
              <Server size={16} /> Automated Probes
            </span>
            <span className="badge badge-success">Operational</span>
          </div>
          <div className="gov-card-body">
            <p style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', lineHeight: 1.5, marginBottom: '1rem' }}>
              Real-time liveness, readiness, and internal communication probes between Spring Boot backend and Python automation engine.
            </p>
            <Link to="/system-health" className="btn btn-secondary btn-sm">
              Inspect Probes &rarr;
            </Link>
          </div>
        </div>
      </div>
    </div>
  )
}
