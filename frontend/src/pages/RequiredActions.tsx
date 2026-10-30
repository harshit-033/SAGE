import React, { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { documentComplianceApi, type DocumentCorrectiveActionDto } from '../api/documentComplianceApi'
import { useAuth } from '../context/AuthContext'
import { useMessage } from '../context/MessageContext'
import { Modal } from '../components/common/Modal'
import {
  CheckCircle2,
  Filter,
  ShieldAlert,
  Send,
} from 'lucide-react'

export const RequiredActions: React.FC = () => {
  const queryClient = useQueryClient()
  const { user, hasRole, isAdmin } = useAuth()
  const { showSuccess, handleApiError } = useMessage()
  const institutionId = user?.institutions?.[0]
  const isOfficer = isAdmin || hasRole('ROLE_REGIONAL_OFFICER') || hasRole('ROLE_COMPLIANCE_OFFICER')
  const isInstitution = hasRole('ROLE_INSTITUTION_OFFICER') && !isAdmin

  const [statusFilter, setStatusFilter] = useState<string>('')
  const [page, setPage] = useState(0)
  const size = 10

  // Respond Modal State
  const [selectedAction, setSelectedAction] = useState<DocumentCorrectiveActionDto | null>(null)
  const [responseNotes, setResponseNotes] = useState('')
  const [isRespondModalOpen, setIsRespondModalOpen] = useState(false)
  const [respondError, setRespondError] = useState<string | null>(null)

  // Resolve Modal State (Officer)
  const [isResolveModalOpen, setIsResolveModalOpen] = useState(false)
  const [resolveStatus, setResolveStatus] = useState<string>('RESOLVED')
  const [officerNotes, setOfficerNotes] = useState('')

  const { data: actionsPage, isLoading } = useQuery({
    queryKey: ['required-actions', { institutionId, statusFilter, page, size }],
    queryFn: () => documentComplianceApi.listCorrectiveActions({
      institutionId: isOfficer ? undefined : institutionId,
      status: statusFilter || undefined,
      page,
      size,
    }),
  })

  const respondMutation = useMutation({
    mutationFn: async () => {
      if (!selectedAction || !responseNotes.trim()) throw new Error('Response explanation is required')
      return documentComplianceApi.respondCorrectiveAction(selectedAction.id, {
        institutionResponse: responseNotes.trim(),
      })
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['required-actions'] })
      queryClient.invalidateQueries({ queryKey: ['document-compliance-summary'] })
      setIsRespondModalOpen(false)
      setSelectedAction(null)
      setResponseNotes('')
      setRespondError(null)
      showSuccess('Response Submitted', 'Your clarification response has been submitted for regulatory review.')
    },
    onError: (err: unknown) => {
      handleApiError(err, 'Submission Notice', 'Failed to submit response')
    },
  })

  const resolveMutation = useMutation({
    mutationFn: async () => {
      if (!selectedAction) throw new Error('Action ID required')
      return documentComplianceApi.resolveCorrectiveAction(selectedAction.id, {
        status: resolveStatus,
        officerNotes: officerNotes.trim(),
      })
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['required-actions'] })
      queryClient.invalidateQueries({ queryKey: ['document-compliance-summary'] })
      setIsResolveModalOpen(false)
      setSelectedAction(null)
      setOfficerNotes('')
      showSuccess('Action Resolved', 'The corrective action status has been authoritatively updated.')
    },
    onError: (err: unknown) => {
      handleApiError(err, 'Resolution Notice', 'Failed to resolve action')
    },
  })

  const getPriorityBadge = (priority: string) => {
    switch (priority) {
      case 'CRITICAL':
        return <span className="badge badge-danger" style={{ fontSize: '0.72rem' }}>CRITICAL</span>
      case 'HIGH':
        return <span className="badge badge-warning" style={{ fontSize: '0.72rem' }}>HIGH</span>
      default:
        return <span className="badge badge-info" style={{ fontSize: '0.72rem' }}>MEDIUM</span>
    }
  }

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'OPEN':
        return <span className="badge badge-danger" style={{ fontSize: '0.72rem' }}>OPEN</span>
      case 'IN_PROGRESS':
        return <span className="badge badge-warning" style={{ fontSize: '0.72rem' }}>IN PROGRESS</span>
      case 'SUBMITTED_FOR_REVIEW':
        return <span className="badge badge-info" style={{ fontSize: '0.72rem' }}>SUBMITTED FOR REVIEW</span>
      case 'RESOLVED':
        return <span className="badge badge-success" style={{ fontSize: '0.72rem' }}>RESOLVED</span>
      case 'OVERDUE':
        return <span className="badge badge-danger" style={{ fontSize: '0.72rem', backgroundColor: '#991b1b', color: '#fff' }}>OVERDUE</span>
      default:
        return <span className="badge badge-secondary" style={{ fontSize: '0.72rem' }}>{status}</span>
    }
  }

  const actions = actionsPage?.content || []
  const openCount = actions.filter(a => a.status === 'OPEN').length
  const submittedCount = actions.filter(a => a.status === 'SUBMITTED_FOR_REVIEW').length
  const overdueCount = actions.filter(a => a.status === 'OVERDUE').length

  return (
    <div className="fade-in" style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
      {/* Header */}
      <div
        className="gov-card"
        style={{
          padding: '1.5rem',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          flexWrap: 'wrap',
          gap: '1rem',
          borderLeft: '4px solid var(--gov-orange)',
        }}
      >
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.65rem' }}>
            <ShieldAlert size={24} color="var(--gov-orange)" />
            <h1 style={{ fontSize: '1.4rem', fontWeight: 800, color: 'var(--gov-navy)', margin: 0 }}>
              Consolidated Required Actions
            </h1>
          </div>
          <p style={{ color: 'var(--text-secondary)', fontSize: '0.85rem', margin: '0.35rem 0 0 0' }}>
            Centralized register of statutory application deficiencies, document findings, expiring certifications &amp; rectifications
          </p>
        </div>
      </div>

      {/* Metric Cards */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1rem' }}>
        <div className="gov-card" style={{ padding: '1rem', borderTop: '3px solid #ef4444' }}>
          <span style={{ fontSize: '0.75rem', fontWeight: 700, color: 'var(--text-muted)', textTransform: 'uppercase' }}>
            Open Deficiencies
          </span>
          <div style={{ fontSize: '1.6rem', fontWeight: 800, color: '#dc2626', marginTop: '0.25rem' }}>
            {isLoading ? '...' : openCount}
          </div>
          <span style={{ fontSize: '0.72rem', color: 'var(--text-secondary)' }}>Pending institutional response</span>
        </div>

        <div className="gov-card" style={{ padding: '1rem', borderTop: '3px solid #0284c7' }}>
          <span style={{ fontSize: '0.75rem', fontWeight: 700, color: 'var(--text-muted)', textTransform: 'uppercase' }}>
            Submitted For Review
          </span>
          <div style={{ fontSize: '1.6rem', fontWeight: 800, color: '#0284c7', marginTop: '0.25rem' }}>
            {isLoading ? '...' : submittedCount}
          </div>
          <span style={{ fontSize: '0.72rem', color: 'var(--text-secondary)' }}>Awaiting officer verification</span>
        </div>

        <div className="gov-card" style={{ padding: '1rem', borderTop: '3px solid #7c3aed' }}>
          <span style={{ fontSize: '0.75rem', fontWeight: 700, color: 'var(--text-muted)', textTransform: 'uppercase' }}>
            Overdue Deadlines
          </span>
          <div style={{ fontSize: '1.6rem', fontWeight: 800, color: '#7c3aed', marginTop: '0.25rem' }}>
            {isLoading ? '...' : overdueCount}
          </div>
          <span style={{ fontSize: '0.72rem', color: 'var(--text-secondary)' }}>Past statutory resolution window</span>
        </div>
      </div>

      {/* Filter Bar */}
      <div
        className="gov-card"
        style={{
          padding: '1rem',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          flexWrap: 'wrap',
          gap: '1rem',
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <Filter size={16} color="var(--text-muted)" />
          <span style={{ fontSize: '0.85rem', fontWeight: 600 }}>Filter by Status:</span>
        </div>

        <select
          className="form-control"
          style={{ width: '220px', fontSize: '0.85rem' }}
          value={statusFilter}
          onChange={(e) => { setStatusFilter(e.target.value); setPage(0); }}
          id="select-filter-action-status"
        >
          <option value="">All Action States</option>
          <option value="OPEN">OPEN (Pending Response)</option>
          <option value="IN_PROGRESS">IN PROGRESS</option>
          <option value="SUBMITTED_FOR_REVIEW">SUBMITTED FOR REVIEW</option>
          <option value="RESOLVED">RESOLVED</option>
          <option value="REJECTED">REJECTED</option>
          <option value="OVERDUE">OVERDUE</option>
        </select>
      </div>

      {/* Actions Table */}
      <div className="gov-card">
        <div className="table-responsive">
          <table className="gov-table" style={{ width: '100%', fontSize: '0.85rem' }} id="table-required-actions">
            <thead>
              <tr>
                <th>Action Details</th>
                <th>Priority</th>
                <th>Related Target</th>
                <th>Due Date</th>
                <th>Status</th>
                <th>Institution Response</th>
                <th style={{ textAlign: 'right' }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {isLoading ? (
                <tr>
                  <td colSpan={7} style={{ textAlign: 'center', padding: '2rem', color: 'var(--text-muted)' }}>
                    Loading pending corrective actions...
                  </td>
                </tr>
              ) : actions.length === 0 ? (
                <tr>
                  <td colSpan={7} style={{ textAlign: 'center', padding: '3rem' }}>
                    <CheckCircle2 size={36} color="#16a34a" style={{ marginBottom: '0.5rem' }} />
                    <p style={{ color: 'var(--text-secondary)', margin: 0, fontWeight: 600 }}>
                      No pending corrective actions or outstanding deficiencies!
                    </p>
                  </td>
                </tr>
              ) : (
                actions.map((act) => (
                  <tr key={act.id}>
                    <td>
                      <div style={{ fontWeight: 700, color: 'var(--gov-navy)' }}>{act.actionType}</div>
                      <div style={{ fontSize: '0.78rem', color: 'var(--text-secondary)' }}>{act.description}</div>
                    </td>
                    <td>{getPriorityBadge(act.priority)}</td>
                    <td>
                      {act.documentName && (
                        <div style={{ fontSize: '0.78rem' }}>
                          <strong>Doc:</strong> {act.documentName}
                        </div>
                      )}
                      {act.applicationNumber && (
                        <div style={{ fontSize: '0.72rem', color: 'var(--text-muted)' }}>
                          <strong>App:</strong> {act.applicationNumber}
                        </div>
                      )}
                    </td>
                    <td>
                      {act.dueDate ? (
                        <span style={{ fontSize: '0.78rem' }}>
                          {new Date(act.dueDate).toLocaleDateString()}
                        </span>
                      ) : (
                        <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Immediate</span>
                      )}
                    </td>
                    <td>{getStatusBadge(act.status)}</td>
                    <td>
                      {act.institutionResponse ? (
                        <div style={{ fontSize: '0.78rem', color: 'var(--gov-navy)', maxWidth: '240px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                          {act.institutionResponse}
                        </div>
                      ) : (
                        <span style={{ fontSize: '0.72rem', color: 'var(--text-muted)' }}>Awaiting Response</span>
                      )}
                    </td>
                    <td style={{ textAlign: 'right', whiteSpace: 'nowrap' }}>
                      {isInstitution && (act.status === 'OPEN' || act.status === 'IN_PROGRESS' || act.status === 'OVERDUE') && (
                        <button
                          onClick={() => {
                            setSelectedAction(act)
                            setResponseNotes(act.institutionResponse || '')
                            setIsRespondModalOpen(true)
                          }}
                          className="btn btn-primary btn-sm"
                          style={{ padding: '0.35rem 0.65rem', marginRight: '0.35rem', backgroundColor: 'var(--gov-orange)' }}
                          id={`btn-respond-action-${act.id}`}
                        >
                          <Send size={13} /> Respond
                        </button>
                      )}

                      {isOfficer && (
                        <button
                          onClick={() => {
                            setSelectedAction(act)
                            setResolveStatus('RESOLVED')
                            setOfficerNotes(act.officerNotes || '')
                            setIsResolveModalOpen(true)
                          }}
                          className="btn btn-outline btn-sm"
                          style={{ padding: '0.35rem 0.65rem' }}
                          id={`btn-officer-resolve-${act.id}`}
                        >
                          Verify Action
                        </button>
                      )}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Institution Respond Modal */}
      {selectedAction && (
        <Modal
          isOpen={isRespondModalOpen}
          onClose={() => setIsRespondModalOpen(false)}
          title={`Respond to Corrective Action: ${selectedAction.actionType}`}
        >
          <form
            onSubmit={(e) => {
              e.preventDefault()
              respondMutation.mutate()
            }}
            style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}
          >
            {respondError && (
              <div className="alert alert-danger" style={{ fontSize: '0.8rem' }}>
                {respondError}
              </div>
            )}

            <div style={{ backgroundColor: 'var(--bg-page)', padding: '0.75rem', borderRadius: '4px' }}>
              <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Deficiency / Required Action</div>
              <div style={{ fontSize: '0.85rem', fontWeight: 600, color: 'var(--gov-navy)' }}>{selectedAction.description}</div>
            </div>

            <div>
              <label style={{ display: 'block', fontSize: '0.82rem', fontWeight: 600, marginBottom: '0.35rem' }}>
                Institutional Explanation / Remediation Notes *
              </label>
              <textarea
                className="form-control"
                rows={4}
                required
                placeholder="Describe corrective steps taken, certificate renewal details, or justification..."
                value={responseNotes}
                onChange={(e) => setResponseNotes(e.target.value)}
                id="input-action-response-notes"
              />
            </div>

            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem' }}>
              <button type="button" onClick={() => setIsRespondModalOpen(false)} className="btn btn-outline">
                Cancel
              </button>
              <button type="submit" className="btn btn-primary" disabled={respondMutation.isPending} id="btn-submit-action-response">
                {respondMutation.isPending ? 'Submitting...' : 'Submit Response For Review'}
              </button>
            </div>
          </form>
        </Modal>
      )}

      {/* Officer Resolve Modal */}
      {selectedAction && (
        <Modal
          isOpen={isResolveModalOpen}
          onClose={() => setIsResolveModalOpen(false)}
          title="Review & Resolve Corrective Action"
        >
          <form
            onSubmit={(e) => {
              e.preventDefault()
              resolveMutation.mutate()
            }}
            style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}
          >
            <div style={{ backgroundColor: 'var(--bg-page)', padding: '0.75rem', borderRadius: '4px' }}>
              <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Institution Response</div>
              <div style={{ fontSize: '0.85rem', fontWeight: 600 }}>{selectedAction.institutionResponse || 'No response recorded'}</div>
            </div>

            <div>
              <label style={{ display: 'block', fontSize: '0.82rem', fontWeight: 600, marginBottom: '0.35rem' }}>
                Resolution Outcome *
              </label>
              <select
                className="form-control"
                value={resolveStatus}
                onChange={(e) => setResolveStatus(e.target.value)}
                required
                id="select-action-resolve-status"
              >
                <option value="RESOLVED">RESOLVED (Deficiency Satisfied)</option>
                <option value="REJECTED">REJECTED (Inadequate Resolution)</option>
                <option value="IN_PROGRESS">KEEP IN PROGRESS</option>
              </select>
            </div>

            <div>
              <label style={{ display: 'block', fontSize: '0.82rem', fontWeight: 600, marginBottom: '0.35rem' }}>
                Officer Notes / Instructions
              </label>
              <textarea
                className="form-control"
                rows={3}
                placeholder="Authoritative feedback or reasons..."
                value={officerNotes}
                onChange={(e) => setOfficerNotes(e.target.value)}
                id="input-action-officer-notes"
              />
            </div>

            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem' }}>
              <button type="button" onClick={() => setIsResolveModalOpen(false)} className="btn btn-outline">
                Cancel
              </button>
              <button type="submit" className="btn btn-primary" disabled={resolveMutation.isPending} id="btn-submit-action-resolve">
                {resolveMutation.isPending ? 'Recording...' : 'Record Outcome'}
              </button>
            </div>
          </form>
        </Modal>
      )}
    </div>
  )
}
