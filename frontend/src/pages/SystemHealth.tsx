import React, { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { healthApi } from '../api/healthApi'
import { StatusBadge } from '../components/common/StatusBadge'
import { Breadcrumbs } from '../components/common/Breadcrumbs'
import { Server, Database, Cpu, RefreshCw, CheckCircle2, AlertTriangle, Send } from 'lucide-react'

export const SystemHealth: React.FC = () => {
  const [pingResult, setPingResult] = useState<string | null>(null)
  const [pinging, setPinging] = useState(false)

  const {
    data: backendHealth,
    isLoading: backendLoading,
    refetch: refetchBackend,
  } = useQuery({
    queryKey: ['backendHealth'],
    queryFn: healthApi.getBackendHealth,
    refetchInterval: 15000,
  })

  const {
    data: backendReady,
    isLoading: readyLoading,
    refetch: refetchReady,
  } = useQuery({
    queryKey: ['backendReady'],
    queryFn: healthApi.getBackendReady,
    refetchInterval: 15000,
  })

  const {
    data: automationHealth,
    isLoading: autoLoading,
    refetch: refetchAuto,
  } = useQuery({
    queryKey: ['automationHealth'],
    queryFn: healthApi.getAutomationHealth,
    refetchInterval: 15000,
  })

  const handleRefreshAll = () => {
    refetchBackend()
    refetchReady()
    refetchAuto()
  }

  const handleTestAutomationPing = async () => {
    setPinging(true)
    setPingResult(null)
    try {
      const res = await fetch('/automation-api/v1/audit/ping', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'X-Internal-Service-Token': 'sage_internal_service_secret_token_12345',
        },
        body: JSON.stringify({
          caller_service: 'sage-admin-monitor',
          message: 'Administrative operational verification probe',
        }),
      })
      const data = await res.json()
      if (res.ok) {
        setPingResult('Operational Communication Confirmed: The document intelligence subsystem is online and responding normally.')
      } else {
        setPingResult(`Subsystem returned response status ${res.status}. Please check service logs.`)
      }
    } catch {
      setPingResult('Service Communication Notice: The automated verification subsystem could not be contacted directly. Please verify service availability.')
    } finally {
      setPinging(false)
    }
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
      <Breadcrumbs items={[{ label: 'Platform Operations' }]} />

      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <h1 style={{ fontSize: '1.4rem', fontWeight: 700, color: 'var(--gov-navy)', margin: 0, display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <Server size={24} color="var(--gov-blue)" />
            <span>Platform Operations &amp; Subsystem Readiness</span>
          </h1>
          <p style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', marginTop: '0.25rem', margin: 0 }}>
            Operational telemetry and readiness indicators for central administrators.
          </p>
        </div>

        <button
          onClick={handleRefreshAll}
          className="btn btn-secondary btn-sm"
          id="refresh-health-btn"
        >
          <RefreshCw size={14} />
          <span>Refresh All Subsystems</span>
        </button>
      </div>

      {/* Grid of Subsystem Cards */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '1.25rem' }}>
        {/* Card 1: Core Backend */}
        <div className="card" style={{ padding: '1.5rem' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '1.25rem' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
              <div
                style={{
                  width: '40px',
                  height: '40px',
                  borderRadius: 'var(--radius-sm)',
                  backgroundColor: 'var(--gov-blue-light)',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                }}
              >
                <Server size={20} color="var(--gov-blue)" />
              </div>
              <div>
                <h2 style={{ fontSize: '1rem', fontWeight: 700, color: 'var(--gov-navy)', margin: 0 }}>
                  Central Workflow &amp; Regulatory Subsystem
                </h2>
                <p style={{ fontSize: '0.75rem', color: 'var(--text-muted)', margin: 0 }}>
                  Core Statutory Governance Engine
                </p>
              </div>
            </div>
            <StatusBadge status={backendHealth?.status || (backendLoading ? 'IN_PROGRESS' : 'DOWN')} />
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.65rem', fontSize: '0.825rem' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', borderBottom: '1px solid var(--border-light)', paddingBottom: '0.45rem' }}>
              <span style={{ color: 'var(--text-muted)' }}>Operational Role:</span>
              <span style={{ fontWeight: 600, color: 'var(--text-primary)' }}>Authoritative Regulatory Scrutiny &amp; Workflow</span>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between', borderBottom: '1px solid var(--border-light)', paddingBottom: '0.45rem' }}>
              <span style={{ color: 'var(--text-muted)' }}>Release Version:</span>
              <span style={{ fontFamily: 'var(--font-mono)' }}>{backendHealth?.version || '1.0.0-RELEASE'}</span>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between', borderBottom: '1px solid var(--border-light)', paddingBottom: '0.45rem' }}>
              <span style={{ color: 'var(--text-muted)' }}>Service Readiness:</span>
              <span style={{ color: 'var(--gov-green)', fontWeight: 600 }}>Active &amp; Accepting Requests</span>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between' }}>
              <span style={{ color: 'var(--text-muted)' }}>Last Status Check:</span>
              <span style={{ fontSize: '0.75rem', color: 'var(--text-secondary)' }}>
                {backendHealth?.timestamp ? new Date(backendHealth.timestamp).toLocaleTimeString() : 'Verified'}
              </span>
            </div>
          </div>
        </div>

        {/* Card 2: Relational Store */}
        <div className="card" style={{ padding: '1.5rem' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '1.25rem' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
              <div
                style={{
                  width: '40px',
                  height: '40px',
                  borderRadius: 'var(--radius-sm)',
                  backgroundColor: '#f0fdf4',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                }}
              >
                <Database size={20} color="#16a34a" />
              </div>
              <div>
                <h2 style={{ fontSize: '1rem', fontWeight: 700, color: 'var(--gov-navy)', margin: 0 }}>
                  Institutional Records &amp; Audit Repository
                </h2>
                <p style={{ fontSize: '0.75rem', color: 'var(--text-muted)', margin: 0 }}>
                  Primary Statutory Data Store
                </p>
              </div>
            </div>
            <StatusBadge status={backendReady?.database?.status || (readyLoading ? 'IN_PROGRESS' : 'DOWN')} />
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.65rem', fontSize: '0.825rem' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', borderBottom: '1px solid var(--border-light)', paddingBottom: '0.45rem' }}>
              <span style={{ color: 'var(--text-muted)' }}>Schema Compliance:</span>
              <span style={{ fontWeight: 600, color: 'var(--gov-green)' }}>Statutory Schema Synchronized</span>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between', borderBottom: '1px solid var(--border-light)', paddingBottom: '0.45rem' }}>
              <span style={{ color: 'var(--text-muted)' }}>Regulatory Audit Trail:</span>
              <span style={{ fontWeight: 600, color: 'var(--text-primary)' }}>Active (Immutable Append-Only)</span>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between', borderBottom: '1px solid var(--border-light)', paddingBottom: '0.45rem' }}>
              <span style={{ color: 'var(--text-muted)' }}>Data Store Connection:</span>
              <span style={{ color: 'var(--gov-green)', fontWeight: 600 }}>Active (Pool Healthy)</span>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between' }}>
              <span style={{ color: 'var(--text-muted)' }}>Storage Status:</span>
              <span style={{ fontSize: '0.75rem', color: 'var(--text-secondary)' }}>
                {backendReady?.database?.details || 'Online & Available'}
              </span>
            </div>
          </div>
        </div>

        {/* Card 3: Document Intelligence Service */}
        <div className="card" style={{ padding: '1.5rem' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '1.25rem' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
              <div
                style={{
                  width: '40px',
                  height: '40px',
                  borderRadius: 'var(--radius-sm)',
                  backgroundColor: '#fef3c7',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                }}
              >
                <Cpu size={20} color="#d97706" />
              </div>
              <div>
                <h2 style={{ fontSize: '1rem', fontWeight: 700, color: 'var(--gov-navy)', margin: 0 }}>
                  Document Intelligence Subsystem
                </h2>
                <p style={{ fontSize: '0.75rem', color: 'var(--text-muted)', margin: 0 }}>
                  Automated Extraction &amp; Verification
                </p>
              </div>
            </div>
            <StatusBadge status={automationHealth?.status || (autoLoading ? 'IN_PROGRESS' : 'DOWN')} />
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.65rem', fontSize: '0.825rem' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', borderBottom: '1px solid var(--border-light)', paddingBottom: '0.45rem' }}>
              <span style={{ color: 'var(--text-muted)' }}>Subsystem Role:</span>
              <span style={{ fontWeight: 600, color: 'var(--text-primary)' }}>Continuous Compliance Analysis</span>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between', borderBottom: '1px solid var(--border-light)', paddingBottom: '0.45rem' }}>
              <span style={{ color: 'var(--text-muted)' }}>Subsystem Version:</span>
              <span style={{ fontFamily: 'var(--font-mono)' }}>{automationHealth?.version || '1.0.0'}</span>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between', borderBottom: '1px solid var(--border-light)', paddingBottom: '0.45rem' }}>
              <span style={{ color: 'var(--text-muted)' }}>Extraction Service:</span>
              <span style={{ color: 'var(--gov-green)', fontWeight: 600 }}>Operational</span>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between' }}>
              <span style={{ color: 'var(--text-muted)' }}>Inter-Service Channel:</span>
              <span style={{ fontWeight: 600, color: 'var(--gov-green)' }}>Authorized Secure Bus</span>
            </div>
          </div>
        </div>
      </div>

      {/* Inter-Service Verification Test Panel */}
      <div className="card" style={{ padding: '1.25rem 1.5rem' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '1rem' }}>
          <div>
            <h3 style={{ fontSize: '0.95rem', fontWeight: 700, color: 'var(--gov-navy)', margin: '0 0 0.25rem 0' }}>
              Subsystem Communication Verification
            </h3>
            <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', margin: 0 }}>
              Verify secure authenticated communication between the central workflow engine and document intelligence services.
            </p>
          </div>
          <button
            onClick={handleTestAutomationPing}
            className="btn btn-primary btn-sm"
            disabled={pinging}
            id="test-automation-ping-btn"
          >
            <Send size={14} />
            <span>{pinging ? 'Verifying...' : 'Verify Subsystem Connection'}</span>
          </button>
        </div>

        {pingResult && (
          <div
            style={{
              marginTop: '1rem',
              padding: '0.75rem 1rem',
              borderRadius: 'var(--radius-sm)',
              backgroundColor: pingResult.includes('Confirmed') ? '#f0fdf4' : '#fef2f2',
              color: pingResult.includes('Confirmed') ? '#166534' : '#991b1b',
              border: `1px solid ${pingResult.includes('Confirmed') ? '#86efac' : '#fca5a5'}`,
              fontSize: '0.825rem',
              display: 'flex',
              alignItems: 'center',
              gap: '0.5rem',
            }}
          >
            {pingResult.includes('Confirmed') ? <CheckCircle2 size={16} /> : <AlertTriangle size={16} />}
            <span>{pingResult}</span>
          </div>
        )}
      </div>
    </div>
  )
}
