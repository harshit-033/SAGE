import React, { useState, useEffect, useCallback } from 'react'
import { Link } from 'react-router-dom'
import { institutionsApi, type Institution, type CreateInstitutionData } from '../api/institutionsApi'
import { StatusBadge } from '../components/common/StatusBadge'
import { Breadcrumbs } from '../components/common/Breadcrumbs'
import { Pagination } from '../components/common/Pagination'
import { Modal } from '../components/common/Modal'
import { Alert } from '../components/common/Alert'
import { useAuth } from '../context/AuthContext'
import {
  Building2,
  Search,
  PlusCircle,
  RefreshCw,
  Eye,
  Power,
} from 'lucide-react'

export const Institutions: React.FC = () => {
  const { isAdmin } = useAuth()

  const [institutions, setInstitutions] = useState<Institution[]>([])
  const [loading, setLoading] = useState<boolean>(true)
  const [error, setError] = useState<string | null>(null)
  const [successMsg, setSuccessMsg] = useState<string | null>(null)

  // Search & Filter State
  const [searchQuery, setSearchQuery] = useState('')
  const [selectedCategory, setSelectedCategory] = useState('')
  const [selectedState, setSelectedState] = useState('')
  const [selectedActive, setSelectedActive] = useState<string>('')
  const [page, setPage] = useState(0)
  const [pageSize] = useState(8)
  const [totalPages, setTotalPages] = useState(1)
  const [totalElements, setTotalElements] = useState(0)

  // Create Institution Modal State
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false)
  const [createData, setCreateData] = useState<CreateInstitutionData>({
    code: '',
    name: '',
    applicationType: 'ENGINEERING_AND_TECH',
    state: '',
    district: '',
  })
  const [creating, setCreating] = useState(false)

  // Status Toggle Modal State
  const [statusModalInst, setStatusModalInst] = useState<Institution | null>(null)
  const [statusReason, setStatusReason] = useState('')
  const [togglingStatus, setTogglingStatus] = useState(false)

  const fetchInstitutions = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const activeFilter = selectedActive === 'true' ? true : selectedActive === 'false' ? false : undefined
      const data = await institutionsApi.listInstitutionsPaginated({
        page,
        size: pageSize,
        query: searchQuery || undefined,
        category: selectedCategory || undefined,
        state: selectedState || undefined,
        active: activeFilter,
      })
      setInstitutions(data.content)
      setTotalPages(data.totalPages)
      setTotalElements(data.totalElements)
    } catch (err: unknown) {
      const errorObj = err as { problem?: { detail?: string }; message?: string }
      setError(errorObj?.problem?.detail || errorObj?.message || 'Failed to load institutions')
    } finally {
      setLoading(false)
    }
  }, [page, pageSize, searchQuery, selectedCategory, selectedState, selectedActive])

  useEffect(() => {
    fetchInstitutions()
  }, [fetchInstitutions])

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    setPage(0)
    fetchInstitutions()
  }

  const handleCreateInstitution = async (e: React.FormEvent) => {
    e.preventDefault()
    setCreating(true)
    setError(null)
    try {
      await institutionsApi.createInstitution(createData)
      setIsCreateModalOpen(false)
      setCreateData({
        code: '',
        name: '',
        applicationType: 'ENGINEERING_AND_TECH',
        state: '',
        district: '',
      })
      setSuccessMsg('New technical institution registered under AICTE jurisdiction.')
      await fetchInstitutions()
    } catch (err: unknown) {
      const errorObj = err as { problem?: { detail?: string }; message?: string }
      setError(errorObj?.problem?.detail || errorObj?.message || 'Failed to create institution')
    } finally {
      setCreating(false)
    }
  }

  const handleToggleStatus = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!statusModalInst) return
    setTogglingStatus(true)
    setError(null)
    try {
      const nextActive = !statusModalInst.active
      await institutionsApi.updateStatus(statusModalInst.id, nextActive, statusReason)
      setStatusModalInst(null)
      setStatusReason('')
      setSuccessMsg(`Institution status changed to ${nextActive ? 'ACTIVE' : 'SUSPENDED'}.`)
      await fetchInstitutions()
    } catch (err: unknown) {
      const errorObj = err as { problem?: { detail?: string }; message?: string }
      setError(errorObj?.problem?.detail || errorObj?.message || 'Failed to change institution status')
    } finally {
      setTogglingStatus(false)
    }
  }

  return (
    <div>
      <Breadcrumbs items={[{ label: 'Institutions' }]} />

      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '1rem', marginBottom: '1.25rem' }}>
        <div>
          <h1 style={{ color: 'var(--gov-navy)', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <Building2 size={24} color="var(--gov-blue)" />
            <span>Technical Institutions Directory</span>
          </h1>
          <p style={{ fontSize: '0.825rem', color: 'var(--text-secondary)', marginTop: '0.2rem' }}>
            {isAdmin
              ? 'Nationwide directory of technical institutions under AICTE regulatory oversight.'
              : 'Technical institutions within your authorized institutional jurisdiction.'}
          </p>
        </div>

        <div style={{ display: 'flex', gap: '0.75rem', alignItems: 'center' }}>
          <button
            type="button"
            className="btn btn-secondary btn-sm"
            onClick={fetchInstitutions}
            title="Refresh Directory"
            id="refresh-institutions-btn"
          >
            <RefreshCw size={14} />
            <span>Refresh</span>
          </button>

          {isAdmin && (
            <button
              type="button"
              className="btn btn-primary btn-sm"
              onClick={() => setIsCreateModalOpen(true)}
              id="create-inst-btn"
            >
              <PlusCircle size={14} />
              <span>Register Institution</span>
            </button>
          )}
        </div>
      </div>

      {successMsg && (
        <Alert type="success" onClose={() => setSuccessMsg(null)}>
          {successMsg}
        </Alert>
      )}

      {error && (
        <Alert type="danger" onClose={() => setError(null)}>
          {error}
        </Alert>
      )}

      {/* Search and Filters Form */}
      <div className="gov-card" style={{ marginBottom: '1.25rem' }}>
        <div className="gov-card-body" style={{ padding: '1rem 1.25rem' }}>
          <form onSubmit={handleSearchSubmit} style={{ display: 'flex', gap: '0.85rem', flexWrap: 'wrap', alignItems: 'flex-end' }}>
            <div style={{ flex: '2', minWidth: '220px' }}>
              <label className="form-label" htmlFor="search-input" style={{ fontSize: '0.78rem' }}>
                Search by PID, Name, or City
              </label>
              <div style={{ position: 'relative' }}>
                <input
                  id="search-input"
                  type="text"
                  className="form-control"
                  placeholder="e.g. 1-500123498 or Delhi Tech"
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                />
              </div>
            </div>

            <div style={{ flex: '1', minWidth: '160px' }}>
              <label className="form-label" htmlFor="cat-filter" style={{ fontSize: '0.78rem' }}>
                Discipline / Category
              </label>
              <select
                id="cat-filter"
                className="form-control"
                value={selectedCategory}
                onChange={(e) => {
                  setSelectedCategory(e.target.value)
                  setPage(0)
                }}
              >
                <option value="">All Categories</option>
                <option value="ENGINEERING_AND_TECH">Engineering &amp; Tech</option>
                <option value="PHARMACY">Pharmacy</option>
                <option value="MANAGEMENT">Management</option>
                <option value="ARCHITECTURE">Architecture</option>
                <option value="POLYTECHNIC">Polytechnic</option>
              </select>
            </div>

            <div style={{ flex: '1', minWidth: '140px' }}>
              <label className="form-label" htmlFor="state-filter" style={{ fontSize: '0.78rem' }}>
                State / UT
              </label>
              <select
                id="state-filter"
                className="form-control"
                value={selectedState}
                onChange={(e) => {
                  setSelectedState(e.target.value)
                  setPage(0)
                }}
              >
                <option value="">All States</option>
                <option value="Delhi">Delhi</option>
                <option value="Maharashtra">Maharashtra</option>
                <option value="Karnataka">Karnataka</option>
                <option value="Tamil Nadu">Tamil Nadu</option>
              </select>
            </div>

            <div style={{ flex: '1', minWidth: '140px' }}>
              <label className="form-label" htmlFor="status-filter" style={{ fontSize: '0.78rem' }}>
                Regulatory Status
              </label>
              <select
                id="status-filter"
                className="form-control"
                value={selectedActive}
                onChange={(e) => {
                  setSelectedActive(e.target.value)
                  setPage(0)
                }}
              >
                <option value="">All Statuses</option>
                <option value="true">Active Only</option>
                <option value="false">Suspended Only</option>
              </select>
            </div>

            <button type="submit" className="btn btn-primary" style={{ padding: '0.55rem 1rem' }}>
              <Search size={15} />
              <span>Filter</span>
            </button>
          </form>
        </div>
      </div>

      {/* Main Institutions Data Table */}
      <div className="gov-card">
        <div className="gov-card-body" style={{ padding: 0 }}>
          {loading ? (
            <div style={{ padding: '3rem', textAlign: 'center', color: 'var(--text-muted)' }}>
              Loading institutions registry...
            </div>
          ) : institutions.length === 0 ? (
            <div style={{ padding: '2.5rem', textAlign: 'center', color: 'var(--text-muted)' }}>
              No institutions matched the specified filter criteria.
            </div>
          ) : (
            <div className="gov-table-container">
              <table className="gov-table" id="institutions-table">
                <thead>
                  <tr>
                    <th>AICTE PID</th>
                    <th>Institution Name</th>
                    <th>Category</th>
                    <th>State / City</th>
                    <th>Status</th>
                    <th style={{ textAlign: 'right' }}>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {institutions.map((inst) => (
                    <tr key={inst.id}>
                      <td style={{ fontFamily: 'var(--font-mono)', fontWeight: 600, color: 'var(--gov-blue)' }}>
                        {inst.code}
                      </td>
                      <td>
                        <Link
                          to={`/institutions/${inst.id}`}
                          style={{ fontWeight: 600, color: 'var(--gov-navy)' }}
                          title="View Institutional Profile"
                        >
                          {inst.name}
                        </Link>
                        {inst.city && (
                          <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>{inst.city}</div>
                        )}
                      </td>
                      <td>
                        <span className="badge badge-info">{inst.category || inst.applicationType}</span>
                      </td>
                      <td>
                        {inst.district}, {inst.state}
                      </td>
                      <td>
                        <StatusBadge status={inst.active ? 'ACTIVE' : 'SUSPENDED'} />
                      </td>
                      <td style={{ textAlign: 'right' }}>
                        <div style={{ display: 'inline-flex', gap: '0.4rem' }}>
                          <Link
                            to={`/institutions/${inst.id}`}
                            className="btn btn-secondary btn-sm"
                            title="Inspect Profile"
                          >
                            <Eye size={13} />
                            <span>Profile</span>
                          </Link>

                          {isAdmin && (
                            <button
                              type="button"
                              className={`btn btn-sm ${inst.active ? 'btn-danger' : 'btn-primary'}`}
                              onClick={() => setStatusModalInst(inst)}
                              title={inst.active ? 'Suspend Institution' : 'Activate Institution'}
                            >
                              <Power size={13} />
                              <span>{inst.active ? 'Suspend' : 'Activate'}</span>
                            </button>
                          )}
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}

          <div style={{ padding: '0.5rem 1.25rem' }}>
            <Pagination
              currentPage={page}
              totalPages={totalPages}
              totalElements={totalElements}
              pageSize={pageSize}
              onPageChange={(p) => setPage(p)}
            />
          </div>
        </div>
      </div>

      {/* Create Institution Modal */}
      <Modal
        isOpen={isCreateModalOpen}
        title="Register Technical Institution"
        onClose={() => setIsCreateModalOpen(false)}
        footer={
          <>
            <button type="button" className="btn btn-secondary" onClick={() => setIsCreateModalOpen(false)}>
              Cancel
            </button>
            <button
              type="submit"
              form="create-inst-form"
              className="btn btn-primary"
              disabled={creating}
            >
              {creating ? 'Registering...' : 'Register Institution'}
            </button>
          </>
        }
      >
        <form id="create-inst-form" onSubmit={handleCreateInstitution}>
          <div className="form-group">
            <label className="form-label" htmlFor="new-code">
              AICTE Permanent ID (PID) <span className="required">*</span>
            </label>
            <input
              id="new-code"
              type="text"
              className="form-control"
              placeholder="e.g. 1-500998877"
              value={createData.code}
              onChange={(e) => setCreateData({ ...createData, code: e.target.value })}
              required
            />
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="new-name">
              Institution Name <span className="required">*</span>
            </label>
            <input
              id="new-name"
              type="text"
              className="form-control"
              placeholder="Full official institution name"
              value={createData.name}
              onChange={(e) => setCreateData({ ...createData, name: e.target.value })}
              required
            />
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="new-type">
              Discipline Stream <span className="required">*</span>
            </label>
            <select
              id="new-type"
              className="form-control"
              value={createData.applicationType}
              onChange={(e) => setCreateData({ ...createData, applicationType: e.target.value })}
            >
              <option value="ENGINEERING_AND_TECH">Engineering &amp; Technology</option>
              <option value="PHARMACY">Pharmacy</option>
              <option value="MANAGEMENT">Management</option>
              <option value="ARCHITECTURE">Architecture</option>
              <option value="POLYTECHNIC">Polytechnic</option>
            </select>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem' }}>
            <div className="form-group">
              <label className="form-label" htmlFor="new-district">
                District <span className="required">*</span>
              </label>
              <input
                id="new-district"
                type="text"
                className="form-control"
                placeholder="District name"
                value={createData.district}
                onChange={(e) => setCreateData({ ...createData, district: e.target.value })}
                required
              />
            </div>

            <div className="form-group">
              <label className="form-label" htmlFor="new-state">
                State / UT <span className="required">*</span>
              </label>
              <input
                id="new-state"
                type="text"
                className="form-control"
                placeholder="State or Union Territory"
                value={createData.state}
                onChange={(e) => setCreateData({ ...createData, state: e.target.value })}
                required
              />
            </div>
          </div>
        </form>
      </Modal>

      {/* Activate / Suspend Institution Status Modal */}
      {statusModalInst && (
        <Modal
          isOpen={!!statusModalInst}
          title={`${statusModalInst.active ? 'Suspend' : 'Activate'} Institution`}
          onClose={() => setStatusModalInst(null)}
          footer={
            <>
              <button type="button" className="btn btn-secondary" onClick={() => setStatusModalInst(null)}>
                Cancel
              </button>
              <button
                type="submit"
                form="status-toggle-form"
                className={`btn ${statusModalInst.active ? 'btn-danger' : 'btn-primary'}`}
                disabled={togglingStatus}
              >
                {togglingStatus
                  ? 'Processing...'
                  : statusModalInst.active
                  ? 'Confirm Suspension'
                  : 'Confirm Activation'}
              </button>
            </>
          }
        >
          <form id="status-toggle-form" onSubmit={handleToggleStatus}>
            <p style={{ fontSize: '0.9rem', marginBottom: '1rem', color: 'var(--text-secondary)' }}>
              Are you sure you want to {statusModalInst.active ? 'suspend' : 'reactivate'}{' '}
              <strong>{statusModalInst.name}</strong> (PID: {statusModalInst.code})?
            </p>

            <div className="form-group">
              <label className="form-label" htmlFor="status-reason">
                Reason / Regulatory Justification
              </label>
              <textarea
                id="status-reason"
                className="form-control"
                rows={3}
                placeholder="Provide statutory reason for audit logging"
                value={statusReason}
                onChange={(e) => setStatusReason(e.target.value)}
              />
            </div>
          </form>
        </Modal>
      )}
    </div>
  )
}
