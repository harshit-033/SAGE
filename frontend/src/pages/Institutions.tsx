import React, { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { institutionsApi, type Institution } from '../api/institutionsApi'
import { StatusBadge } from '../components/common/StatusBadge'
import { useAuth } from '../context/AuthContext'
import { Building2, AlertTriangle, ShieldAlert, CheckCircle, RefreshCw } from 'lucide-react'

export const Institutions: React.FC = () => {
  const { isAdmin } = useAuth()
  const [crossTenantTestResult, setCrossTenantTestResult] = useState<string | null>(null)
  const [testingCrossTenant, setTestingCrossTenant] = useState(false)

  const {
    data: institutions,
    isLoading,
    error,
    refetch,
  } = useQuery<Institution[]>({
    queryKey: ['institutions'],
    queryFn: institutionsApi.listInstitutions,
  })

  // Security Verification Tool: Attempt to inspect another institution
  const handleTestCrossTenantViolation = async () => {
    setTestingCrossTenant(true)
    setCrossTenantTestResult(null)
    try {
      // Try fetching Mumbai Academy ID: '33333333-3333-3333-3333-333333333333'
      const mumbaiId = '33333333-3333-3333-3333-333333333333'
      await institutionsApi.getInstitutionById(mumbaiId)
      if (isAdmin) {
        setCrossTenantTestResult('Admin verified: AICTE Central Administrator has global regulatory authorization to inspect Mumbai Academy.')
      } else {
        setCrossTenantTestResult('Warning: Unexpected access granted.')
      }
    } catch (err: unknown) {
      const errorObj = err as { problem?: { status?: number; detail?: string }; message?: string }
      if (errorObj?.problem?.status === 403) {
        setCrossTenantTestResult(
          `Security Guard Verified: Backend rejected unauthorized cross-tenant inspection with HTTP 403 Forbidden. Details: "${errorObj.problem.detail}". Violation audit event recorded.`
        )
      } else {
        setCrossTenantTestResult(`Request failed: ${errorObj?.message || 'Unknown error'}`)
      }
    } finally {
      setTestingCrossTenant(false)
    }
  }

  return (
    <div className="fade-in" style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <h1 style={{ fontSize: '1.5rem', fontWeight: 800, color: '#ffffff', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <Building2 size={24} color="var(--primary-500)" />
            <span>Technical Institutions</span>
          </h1>
          <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', marginTop: '0.25rem' }}>
            {isAdmin
              ? 'Showing all technical institutions registered nationwide under AICTE jurisdiction.'
              : 'Scoped to your authorized institutional tenancy affiliation.'}
          </p>
        </div>

        <div style={{ display: 'flex', gap: '0.75rem' }}>
          <button
            onClick={() => refetch()}
            className="btn-secondary"
            title="Refresh list"
            id="refresh-institutions-btn"
          >
            <RefreshCw size={16} />
            <span>Refresh</span>
          </button>
        </div>
      </div>

      {/* Cross-Tenant Isolation Security Test Banner */}
      <div
        className="glass-panel"
        style={{
          padding: '1.25rem',
          background: 'rgba(31, 41, 55, 0.4)',
          borderLeft: '4px solid var(--info)',
        }}
      >
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '1rem' }}>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.25rem' }}>
              <ShieldAlert size={18} color="var(--info)" />
              <span style={{ fontWeight: 700, fontSize: '0.9rem', color: '#ffffff' }}>
                Tenancy Security Verification Tool
              </span>
            </div>
            <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
              Test backend enforcement of multi-tenant isolation by attempting to access a foreign institution ID.
            </p>
          </div>

          <button
            onClick={handleTestCrossTenantViolation}
            className="btn-secondary"
            style={{ fontSize: '0.8rem', padding: '0.5rem 0.85rem' }}
            disabled={testingCrossTenant}
            id="test-tenancy-guard-btn"
          >
            {testingCrossTenant ? 'Probing...' : 'Probe Cross-Tenant Access'}
          </button>
        </div>

        {crossTenantTestResult && (
          <div
            style={{
              marginTop: '1rem',
              padding: '0.75rem',
              borderRadius: 'var(--radius-md)',
              background: crossTenantTestResult.includes('Security Guard Verified')
                ? 'var(--success-bg)'
                : 'var(--info-bg)',
              border: '1px solid var(--border-subtle)',
              fontSize: '0.8rem',
              color: crossTenantTestResult.includes('Security Guard Verified') ? '#34d399' : '#60a5fa',
              display: 'flex',
              alignItems: 'flex-start',
              gap: '0.5rem',
            }}
          >
            <CheckCircle size={16} style={{ flexShrink: 0, marginTop: '2px' }} />
            <span>{crossTenantTestResult}</span>
          </div>
        )}
      </div>

      {/* Main Table View */}
      {isLoading ? (
        <div className="glass-panel" style={{ padding: '3rem', textAlign: 'center', color: 'var(--text-muted)' }}>
          <p>Loading institutional records...</p>
        </div>
      ) : error ? (
        <div className="glass-panel" style={{ padding: '2rem', textAlign: 'center', color: '#f87171' }}>
          <AlertTriangle size={32} style={{ margin: '0 auto 1rem' }} />
          <p>Failed to retrieve institutions: {error.message}</p>
        </div>
      ) : (
        <div className="glass-panel" style={{ overflow: 'hidden' }}>
          <table className="data-table" id="institutions-table">
            <thead>
              <tr>
                <th>AICTE PID</th>
                <th>Institution Name</th>
                <th>Discipline Type</th>
                <th>State / District</th>
                <th>Regulatory Status</th>
                <th>Lock Version</th>
              </tr>
            </thead>
            <tbody>
              {institutions && institutions.length > 0 ? (
                institutions.map((inst) => (
                  <tr key={inst.id}>
                    <td style={{ fontFamily: 'var(--font-mono)', fontWeight: 600, color: 'var(--text-primary)' }}>
                      {inst.code}
                    </td>
                    <td style={{ fontWeight: 600, color: '#ffffff' }}>
                      {inst.name}
                    </td>
                    <td>
                      <span style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
                        {inst.applicationType}
                      </span>
                    </td>
                    <td>
                      {inst.district}, {inst.state}
                    </td>
                    <td>
                      <StatusBadge status={inst.status} />
                    </td>
                    <td style={{ fontFamily: 'var(--font-mono)', color: 'var(--text-muted)' }}>
                      v{inst.version}
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan={6} style={{ textAlign: 'center', padding: '2rem', color: 'var(--text-muted)' }}>
                    No institutions mapped to your security credentials.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}
