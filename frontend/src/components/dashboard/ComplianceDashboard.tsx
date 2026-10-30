import React, { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import { complianceApi, type ComplianceFindingDto, type ComplianceCheckRunDto } from '../../api/complianceApi'
import { deadlinesApi, type DeadlineDto } from '../../api/deadlinesApi'
import {
  Clock,
  AlertTriangle,
  CheckCircle2,
  Play,
  RotateCw,
  ShieldAlert,
} from 'lucide-react'

export const ComplianceDashboard: React.FC = () => {
  const { user } = useAuth()
  const [findings, setFindings] = useState<ComplianceFindingDto[]>([])
  const [checkRuns, setCheckRuns] = useState<ComplianceCheckRunDto[]>([])
  const [deadlines, setDeadlines] = useState<DeadlineDto[]>([])
  const [scanning, setScanning] = useState(false)
  const [scanMessage, setScanMessage] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)

  const loadData = async () => {
    try {
      setLoading(true)
      const [findingRes, runRes, deadlineRes] = await Promise.all([
        complianceApi.getFindings({ size: 15 }),
        complianceApi.getCheckRuns({ size: 5 }),
        deadlinesApi.list({ size: 10 }),
      ])
      setFindings(findingRes?.content || [])
      setCheckRuns(runRes?.content || [])
      setDeadlines(deadlineRes?.content || [])
    } catch (err) {
      console.error('Failed to load compliance dashboard data', err)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadData()
  }, [])

  const handleTriggerScan = async () => {
    try {
      setScanning(true)
      setScanMessage(null)
      const run = await complianceApi.triggerScan()
      setScanMessage(`Scan complete: Checked ${run.institutionsChecked} institutions, generated ${run.findingsGenerated} findings.`)
      await loadData()
    } catch {
      setScanMessage('Failed to trigger automated surveillance sweep.')
    } finally {
      setScanning(false)
    }
  }

  const openFindings = findings.filter((f) => f.status === 'OPEN' || f.status === 'UNDER_REVIEW')
  const criticalFindings = openFindings.filter((f) => f.severity === 'CRITICAL')

  return (
    <div className="fade-in">
      {/* Compliance Header */}
      <div className="gov-card" style={{ marginBottom: '1.5rem', borderLeft: '4px solid #0284c7' }}>
        <div className="gov-card-body" style={{ padding: '1.5rem 1.75rem' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '1rem' }}>
            <div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.65rem', marginBottom: '0.35rem' }}>
                <Clock size={22} color="#0284c7" />
                <h1 style={{ color: 'var(--gov-navy)', margin: 0, fontSize: '1.35rem' }}>
                  Regulatory Compliance &amp; Continuous Surveillance Dashboard
                </h1>
                <span className="badge badge-info" style={{ fontSize: '0.72rem' }}>
                  COMPLIANCE BUREAU
                </span>
              </div>
              <p style={{ color: 'var(--text-secondary)', fontSize: '0.85rem', margin: '0.25rem 0 0.5rem 0' }}>
                Designated Compliance Officer: <strong>{user?.fullName}</strong> &bull; Continuous Faculty, Cadre Ratio, and Fire Safety NOC Surveillance
              </p>
              <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                APH 2024–27 Automated Rule Engine &bull; Zero human bias in statutory deficiency generation.
              </div>
            </div>

            <div style={{ display: 'flex', gap: '0.65rem', flexWrap: 'wrap' }}>
              <button
                type="button"
                className="btn btn-primary btn-sm"
                onClick={handleTriggerScan}
                disabled={scanning}
                id="comp-dash-trigger-scan"
                style={{ backgroundColor: 'var(--gov-orange)' }}
              >
                {scanning ? <RotateCw size={14} className="spin" /> : <Play size={14} />}
                <span>{scanning ? 'Running Surveillance...' : 'Trigger Compliance Scan'}</span>
              </button>
              <Link to="/compliance" className="btn btn-secondary btn-sm" id="comp-dash-full-center">
                <span>Compliance Center &rarr;</span>
              </Link>
            </div>
          </div>

          {scanMessage && (
            <div
              style={{
                marginTop: '1rem',
                padding: '0.65rem 0.85rem',
                borderRadius: 'var(--radius-sm)',
                backgroundColor: 'var(--status-info-bg)',
                border: '1px solid var(--status-info-border)',
                color: 'var(--status-info-text)',
                fontSize: '0.825rem',
                fontWeight: 600,
              }}
            >
              {scanMessage}
            </div>
          )}
        </div>
      </div>

      {/* KPI Cards */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '1.25rem', marginBottom: '1.5rem' }}>
        <div className="gov-card" style={{ padding: '1.25rem', borderTop: '3px solid #0284c7' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
            <span style={{ fontSize: '0.8rem', fontWeight: 700, color: 'var(--text-muted)', textTransform: 'uppercase' }}>
              Open Deficiencies
            </span>
            <AlertTriangle size={18} color="#0284c7" />
          </div>
          <div style={{ fontSize: '1.75rem', fontWeight: 800, color: 'var(--gov-navy)', lineHeight: 1.2 }}>
            {loading ? '...' : openFindings.length}
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', marginTop: '0.4rem' }}>
            Under active surveillance
          </div>
        </div>

        <div className="gov-card" style={{ padding: '1.25rem', borderTop: '3px solid #dc2626' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
            <span style={{ fontSize: '0.8rem', fontWeight: 700, color: 'var(--text-muted)', textTransform: 'uppercase' }}>
              Critical Infractions
            </span>
            <ShieldAlert size={18} color="#dc2626" />
          </div>
          <div style={{ fontSize: '1.75rem', fontWeight: 800, color: criticalFindings.length > 0 ? '#dc2626' : 'var(--gov-green)', lineHeight: 1.2 }}>
            {loading ? '...' : criticalFindings.length}
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', marginTop: '0.4rem' }}>
            Fire NOC / Land statutory violations
          </div>
        </div>

        <div className="gov-card" style={{ padding: '1.25rem', borderTop: '3px solid var(--gov-green)' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
            <span style={{ fontSize: '0.8rem', fontWeight: 700, color: 'var(--text-muted)', textTransform: 'uppercase' }}>
              Completed Sweeps
            </span>
            <CheckCircle2 size={18} color="var(--gov-green)" />
          </div>
          <div style={{ fontSize: '1.75rem', fontWeight: 800, color: 'var(--gov-navy)', lineHeight: 1.2 }}>
            {loading ? '...' : checkRuns.length}
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', marginTop: '0.4rem' }}>
            Surveillance runs recorded
          </div>
        </div>

        <div className="gov-card" style={{ padding: '1.25rem', borderTop: '3px solid var(--gov-orange)' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
            <span style={{ fontSize: '0.8rem', fontWeight: 700, color: 'var(--text-muted)', textTransform: 'uppercase' }}>
              Tracked Deadlines
            </span>
            <Clock size={18} color="var(--gov-orange)" />
          </div>
          <div style={{ fontSize: '1.75rem', fontWeight: 800, color: 'var(--gov-navy)', lineHeight: 1.2 }}>
            {loading ? '...' : deadlines.length}
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', marginTop: '0.4rem' }}>
            Statutory cutoffs monitored
          </div>
        </div>
      </div>

      {/* Open Findings Queue */}
      <div className="gov-card" style={{ marginBottom: '1.5rem' }}>
        <div className="gov-card-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <span className="gov-card-title">
            <AlertTriangle size={16} color="var(--gov-orange)" /> Active Statutory Non-Compliance Findings
          </span>
          <Link to="/compliance" className="btn btn-secondary btn-sm" style={{ fontSize: '0.75rem' }}>
            Resolve / Inspect in Center &rarr;
          </Link>
        </div>
        <div className="gov-card-body" style={{ padding: '0.5rem 0' }}>
          {loading ? (
            <div style={{ padding: '2rem', textAlign: 'center', color: 'var(--text-muted)' }}>
              Loading compliance findings...
            </div>
          ) : openFindings.length === 0 ? (
            <div style={{ padding: '2rem', textAlign: 'center', color: 'var(--text-secondary)' }}>
              <CheckCircle2 size={32} color="var(--gov-green)" style={{ marginBottom: '0.5rem' }} />
              <p style={{ margin: 0, fontWeight: 600 }}>All institutions are in statutory compliance.</p>
              <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginTop: '0.25rem' }}>
                Zero unresolved non-compliance findings in active records.
              </p>
            </div>
          ) : (
            <div className="gov-table-container">
              <table className="gov-table" style={{ margin: 0 }}>
                <thead>
                  <tr>
                    <th>Severity</th>
                    <th>Institution</th>
                    <th>Finding Title</th>
                    <th>APH Clause</th>
                    <th>Status</th>
                    <th>Action</th>
                  </tr>
                </thead>
                <tbody>
                  {openFindings.slice(0, 6).map((finding) => (
                    <tr key={finding.id}>
                      <td>
                        <span className={`badge ${finding.severity === 'CRITICAL' ? 'badge-danger' : finding.severity === 'WARNING' ? 'badge-warning' : 'badge-info'}`}>
                          {finding.severity}
                        </span>
                      </td>
                      <td style={{ fontWeight: 600, color: 'var(--gov-navy)' }}>
                        {finding.institutionName}
                      </td>
                      <td>
                        <div style={{ fontWeight: 600 }}>{finding.title}</div>
                        <div style={{ fontSize: '0.72rem', color: 'var(--text-muted)' }}>{finding.findingType}</div>
                      </td>
                      <td>
                        <span className="badge badge-secondary" style={{ fontSize: '0.72rem' }}>
                          {finding.aphClause || 'APH 2024–27'}
                        </span>
                      </td>
                      <td>
                        <span className={`badge ${finding.status === 'OPEN' ? 'badge-warning' : 'badge-info'}`}>
                          {finding.status}
                        </span>
                      </td>
                      <td>
                        <Link
                          to="/compliance"
                          className="btn btn-secondary btn-sm"
                          style={{ fontSize: '0.72rem', padding: '0.2rem 0.5rem' }}
                        >
                          Resolve &rarr;
                        </Link>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
