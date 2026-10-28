import React, { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { healthApi } from '../api/healthApi'
import { StatusBadge } from '../components/common/StatusBadge'
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
          caller_service: 'sage-frontend-monitor',
          message: 'Frontend automated health heartbeat',
        }),
      })
      const data = await res.json()
      if (res.ok) {
        setPingResult(`Success: Automation engine acknowledged ping with message: "${data.echo}"`)
      } else {
        setPingResult(`Ping failed with status ${res.status}: ${data.detail || 'Unknown error'}`)
      }
    } catch (err: unknown) {
      const errorObj = err as { message?: string }
      setPingResult(`Connection error: ${errorObj?.message || 'Failed to connect'}`)
    } finally {
      setPinging(false)
    }
  }

  return (
    <div className="fade-in" style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <h1 style={{ fontSize: '1.5rem', fontWeight: 800, color: '#ffffff', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <Server size={24} color="var(--primary-500)" />
            <span>Service Health & Readiness Monitor</span>
          </h1>
          <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', marginTop: '0.25rem' }}>
            Real-time infrastructure and multi-service telemetry dashboard for Phase 1.
          </p>
        </div>

        <button
          onClick={handleRefreshAll}
          className="btn-secondary"
          id="refresh-health-btn"
        >
          <RefreshCw size={16} />
          <span>Refresh All Probes</span>
        </button>
      </div>

      {/* Grid of Microservice Status Cards */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(350px, 1fr))', gap: '1.5rem' }}>
        {/* Card 1: Core Backend */}
        <div className="glass-panel" style={{ padding: '1.75rem' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '1.25rem' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
              <div
                style={{
                  width: '42px',
                  height: '42px',
                  borderRadius: 'var(--radius-md)',
                  background: 'rgba(99, 102, 241, 0.15)',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                }}
              >
                <Server size={22} color="var(--primary-500)" />
              </div>
              <div>
                <h2 style={{ fontSize: '1.05rem', fontWeight: 700, color: '#ffffff' }}>
                  Spring Boot Core Engine
                </h2>
                <p style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                  Port 8080 · Java 21 LTS
                </p>
              </div>
            </div>
            <StatusBadge status={backendHealth?.status || (backendLoading ? 'IN_PROGRESS' : 'DOWN')} />
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem', fontSize: '0.85rem' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', borderBottom: '1px solid var(--border-subtle)', paddingBottom: '0.5rem' }}>
              <span style={{ color: 'var(--text-muted)' }}>Service Role:</span>
              <span style={{ fontWeight: 600 }}>Authoritative Business & Security</span>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between', borderBottom: '1px solid var(--border-subtle)', paddingBottom: '0.5rem' }}>
              <span style={{ color: 'var(--text-muted)' }}>API Version:</span>
              <span style={{ fontFamily: 'var(--font-mono)' }}>{backendHealth?.version || '1.0.0-SNAPSHOT'}</span>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between', borderBottom: '1px solid var(--border-subtle)', paddingBottom: '0.5rem' }}>
              <span style={{ color: 'var(--text-muted)' }}>Liveness Endpoint:</span>
              <span style={{ fontFamily: 'var(--font-mono)', color: 'var(--primary-100)' }}>/api/v1/health</span>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between' }}>
              <span style={{ color: 'var(--text-muted)' }}>Last Probe:</span>
              <span style={{ fontSize: '0.75rem', color: 'var(--text-secondary)' }}>
                {backendHealth?.timestamp ? new Date(backendHealth.timestamp).toLocaleTimeString() : 'Awaiting...'}
              </span>
            </div>
          </div>
        </div>

        {/* Card 2: Relational Store */}
        <div className="glass-panel" style={{ padding: '1.75rem' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '1.25rem' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
              <div
                style={{
                  width: '42px',
                  height: '42px',
                  borderRadius: 'var(--radius-md)',
                  background: 'rgba(16, 185, 129, 0.15)',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                }}
              >
                <Database size={22} color="var(--success)" />
              </div>
              <div>
                <h2 style={{ fontSize: '1.05rem', fontWeight: 700, color: '#ffffff' }}>
                  PostgreSQL & Flyway
                </h2>
                <p style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                  Port 5432 · Relational Store
                </p>
              </div>
            </div>
            <StatusBadge status={backendReady?.database?.status || (readyLoading ? 'IN_PROGRESS' : 'DOWN')} />
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem', fontSize: '0.85rem' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', borderBottom: '1px solid var(--border-subtle)', paddingBottom: '0.5rem' }}>
              <span style={{ color: 'var(--text-muted)' }}>Migration State:</span>
              <span style={{ fontWeight: 600, color: '#34d399' }}>Flyway V1 & V2 Applied</span>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between', borderBottom: '1px solid var(--border-subtle)', paddingBottom: '0.5rem' }}>
              <span style={{ color: 'var(--text-muted)' }}>Audit Log Table:</span>
              <span style={{ fontWeight: 600 }}>Active (Immutable Append)</span>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between', borderBottom: '1px solid var(--border-subtle)', paddingBottom: '0.5rem' }}>
              <span style={{ color: 'var(--text-muted)' }}>Readiness Probe:</span>
              <span style={{ fontFamily: 'var(--font-mono)', color: 'var(--primary-100)' }}>/api/v1/health/ready</span>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between' }}>
              <span style={{ color: 'var(--text-muted)' }}>Connection Details:</span>
              <span style={{ fontSize: '0.75rem', color: 'var(--text-secondary)' }}>
                {backendReady?.database?.details || 'Validating connection'}
              </span>
            </div>
          </div>
        </div>

        {/* Card 3: Python Automation Service */}
        <div className="glass-panel" style={{ padding: '1.75rem' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '1.25rem' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
              <div
                style={{
                  width: '42px',
                  height: '42px',
                  borderRadius: 'var(--radius-md)',
                  background: 'rgba(6, 182, 212, 0.15)',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                }}
              >
                <Cpu size={22} color="#06b6d4" />
              </div>
              <div>
                <h2 style={{ fontSize: '1.05rem', fontWeight: 700, color: '#ffffff' }}>
                  FastAPI Automation
                </h2>
                <p style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                  Port 8000 · Python 3.14
                </p>
              </div>
            </div>
            <StatusBadge status={automationHealth?.status || (autoLoading ? 'IN_PROGRESS' : 'DOWN')} />
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem', fontSize: '0.85rem' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', borderBottom: '1px solid var(--border-subtle)', paddingBottom: '0.5rem' }}>
              <span style={{ color: 'var(--text-muted)' }}>Role:</span>
              <span style={{ fontWeight: 600 }}>Non-Authoritative Automation</span>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between', borderBottom: '1px solid var(--border-subtle)', paddingBottom: '0.5rem' }}>
              <span style={{ color: 'var(--text-muted)' }}>Version:</span>
              <span style={{ fontFamily: 'var(--font-mono)' }}>{automationHealth?.version || '1.0.0'}</span>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between', borderBottom: '1px solid var(--border-subtle)', paddingBottom: '0.5rem' }}>
              <span style={{ color: 'var(--text-muted)' }}>OpenAPI Docs:</span>
              <span style={{ fontFamily: 'var(--font-mono)', color: 'var(--primary-100)' }}>/docs</span>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between' }}>
              <span style={{ color: 'var(--text-muted)' }}>Inter-Service Token:</span>
              <span style={{ fontWeight: 600, color: '#34d399' }}>Configured</span>
            </div>
          </div>
        </div>
      </div>

      {/* Inter-Service RPC Ping Test Panel */}
      <div className="glass-panel" style={{ padding: '1.5rem' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '1rem' }}>
          <div>
            <h3 style={{ fontSize: '1rem', fontWeight: 700, color: '#ffffff', marginBottom: '0.25rem' }}>
              Inter-Service RPC Verification
            </h3>
            <p style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>
              Test authenticated machine-to-machine HTTP communication between services using the internal service token.
            </p>
          </div>
          <button
            onClick={handleTestAutomationPing}
            className="btn-primary"
            disabled={pinging}
            id="test-automation-ping-btn"
          >
            <Send size={15} />
            <span>{pinging ? 'Pinging...' : 'Dispatch Inter-Service Ping'}</span>
          </button>
        </div>

        {pingResult && (
          <div
            style={{
              marginTop: '1rem',
              padding: '0.85rem',
              borderRadius: 'var(--radius-md)',
              background: pingResult.startsWith('Success') ? 'var(--success-bg)' : 'var(--danger-bg)',
              color: pingResult.startsWith('Success') ? '#34d399' : '#f87171',
              fontSize: '0.825rem',
              display: 'flex',
              alignItems: 'center',
              gap: '0.5rem',
            }}
          >
            {pingResult.startsWith('Success') ? <CheckCircle2 size={16} /> : <AlertTriangle size={16} />}
            <span>{pingResult}</span>
          </div>
        )}
      </div>
    </div>
  )
}
