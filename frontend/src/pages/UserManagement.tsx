import React, { useState, useEffect, useCallback } from 'react'
import { usersApi, type User, type CreateUserData, type Role } from '../api/usersApi'
import { institutionsApi, type Institution } from '../api/institutionsApi'
import { useAuth } from '../context/AuthContext'
import { Breadcrumbs } from '../components/common/Breadcrumbs'
import { Pagination } from '../components/common/Pagination'
import { Modal } from '../components/common/Modal'
import { Alert } from '../components/common/Alert'
import {
  Users,
  Search,
  UserPlus,
  Shield,
  RefreshCw,
  Power,
} from 'lucide-react'

export const UserManagement: React.FC = () => {
  const { user: currentUser } = useAuth()

  const [users, setUsers] = useState<User[]>([])
  const [roles, setRoles] = useState<Role[]>([])
  const [institutions, setInstitutions] = useState<Institution[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [successMsg, setSuccessMsg] = useState<string | null>(null)

  // Filters & Pagination
  const [query, setQuery] = useState('')
  const [selectedActive, setSelectedActive] = useState<string>('')
  const [page, setPage] = useState(0)
  const [pageSize] = useState(10)
  const [totalPages, setTotalPages] = useState(1)
  const [totalElements, setTotalElements] = useState(0)

  // Create User Modal State
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false)
  const [createData, setCreateData] = useState<CreateUserData>({
    username: '',
    email: '',
    fullName: '',
    password: '',
    roles: ['ROLE_INSTITUTION_OFFICER'],
    institutionId: undefined,
  })
  const [creating, setCreating] = useState(false)

  // Manage Roles Modal State
  const [roleModalUser, setRoleModalUser] = useState<User | null>(null)
  const [selectedRoles, setSelectedRoles] = useState<string[]>([])
  const [updatingRoles, setUpdatingRoles] = useState(false)

  // Status Modal State
  const [statusModalUser, setStatusModalUser] = useState<User | null>(null)
  const [statusReason, setStatusReason] = useState('')
  const [togglingStatus, setTogglingStatus] = useState(false)

  const fetchUsers = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const activeFilter = selectedActive === 'true' ? true : selectedActive === 'false' ? false : undefined
      const data = await usersApi.listUsers({
        page,
        size: pageSize,
        query: query || undefined,
        active: activeFilter,
      })
      setUsers(data.content)
      setTotalPages(data.totalPages)
      setTotalElements(data.totalElements)
    } catch (err: unknown) {
      const errorObj = err as { problem?: { detail?: string }; message?: string }
      setError(errorObj?.problem?.detail || errorObj?.message || 'Failed to load users')
    } finally {
      setLoading(false)
    }
  }, [page, pageSize, query, selectedActive])

  useEffect(() => {
    fetchUsers()
  }, [fetchUsers])

  useEffect(() => {
    const loadMetadata = async () => {
      try {
        const [rolesData, instsData] = await Promise.all([
          usersApi.getRoles(),
          institutionsApi.listInstitutions(),
        ])
        setRoles(rolesData)
        setInstitutions(instsData)
      } catch {
        // Fallback for metadata
      }
    }
    loadMetadata()
  }, [])

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    setPage(0)
    fetchUsers()
  }

  const handleCreateUser = async (e: React.FormEvent) => {
    e.preventDefault()
    setCreating(true)
    setError(null)
    try {
      await usersApi.createUser(createData)
      setIsCreateModalOpen(false)
      setCreateData({
        username: '',
        email: '',
        fullName: '',
        password: '',
        roles: ['ROLE_INSTITUTION_OFFICER'],
        institutionId: undefined,
      })
      setSuccessMsg('User account created and roles assigned successfully.')
      await fetchUsers()
    } catch (err: unknown) {
      const errorObj = err as { problem?: { detail?: string }; message?: string }
      setError(errorObj?.problem?.detail || errorObj?.message || 'Failed to create user')
    } finally {
      setCreating(false)
    }
  }

  const openRoleModal = (u: User) => {
    setRoleModalUser(u)
    setSelectedRoles([...u.roles])
  }

  const handleAssignRoles = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!roleModalUser) return
    setUpdatingRoles(true)
    setError(null)
    try {
      await usersApi.assignRoles(roleModalUser.id, selectedRoles)
      setRoleModalUser(null)
      setSuccessMsg(`Roles updated for user: ${roleModalUser.username}`)
      await fetchUsers()
    } catch (err: unknown) {
      const errorObj = err as { problem?: { detail?: string }; message?: string }
      setError(errorObj?.problem?.detail || errorObj?.message || 'Failed to assign roles')
    } finally {
      setUpdatingRoles(false)
    }
  }

  const handleToggleStatus = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!statusModalUser) return
    setTogglingStatus(true)
    setError(null)
    try {
      const nextActive = !statusModalUser.active
      await usersApi.updateStatus(statusModalUser.id, nextActive, statusReason)
      setStatusModalUser(null)
      setStatusReason('')
      setSuccessMsg(`User status changed to ${nextActive ? 'ACTIVE' : 'SUSPENDED'}.`)
      await fetchUsers()
    } catch (err: unknown) {
      const errorObj = err as { problem?: { detail?: string }; message?: string }
      setError(errorObj?.problem?.detail || errorObj?.message || 'Failed to update user status')
    } finally {
      setTogglingStatus(false)
    }
  }

  const toggleRoleSelection = (roleCode: string) => {
    if (selectedRoles.includes(roleCode)) {
      if (selectedRoles.length === 1) {
        alert('User must have at least one role assigned.')
        return
      }
      setSelectedRoles(selectedRoles.filter((r) => r !== roleCode))
    } else {
      setSelectedRoles([...selectedRoles, roleCode])
    }
  }

  return (
    <div>
      <Breadcrumbs items={[{ label: 'User Management' }]} />

      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '1rem', marginBottom: '1.25rem' }}>
        <div>
          <h1 style={{ color: 'var(--gov-navy)', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <Users size={24} color="var(--gov-blue)" />
            <span>Identity &amp; Role-Based Authorization</span>
          </h1>
          <p style={{ fontSize: '0.825rem', color: 'var(--text-secondary)', marginTop: '0.2rem' }}>
            Manage administrative personnel, regional officers, scrutiny committees, and institutional signatories.
          </p>
        </div>

        <div style={{ display: 'flex', gap: '0.75rem', alignItems: 'center' }}>
          <button
            type="button"
            className="btn btn-secondary btn-sm"
            onClick={fetchUsers}
            title="Refresh Users"
            id="refresh-users-btn"
          >
            <RefreshCw size={14} />
            <span>Refresh</span>
          </button>

          <button
            type="button"
            className="btn btn-primary btn-sm"
            onClick={() => setIsCreateModalOpen(true)}
            id="register-user-btn"
          >
            <UserPlus size={14} />
            <span>Register User</span>
          </button>
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

      {/* Search and Filters */}
      <div className="gov-card" style={{ marginBottom: '1.25rem' }}>
        <div className="gov-card-body" style={{ padding: '1rem 1.25rem' }}>
          <form onSubmit={handleSearchSubmit} style={{ display: 'flex', gap: '0.85rem', flexWrap: 'wrap', alignItems: 'flex-end' }}>
            <div style={{ flex: '2', minWidth: '220px' }}>
              <label className="form-label" htmlFor="user-search" style={{ fontSize: '0.78rem' }}>
                Search by Name, Username, or Email
              </label>
              <input
                id="user-search"
                type="text"
                className="form-control"
                placeholder="e.g. Ramesh Sharma or officer@aicte.gov.in"
                value={query}
                onChange={(e) => setQuery(e.target.value)}
              />
            </div>

            <div style={{ flex: '1', minWidth: '150px' }}>
              <label className="form-label" htmlFor="user-status-filter" style={{ fontSize: '0.78rem' }}>
                Account Status
              </label>
              <select
                id="user-status-filter"
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

      {/* Users Table */}
      <div className="gov-card">
        <div className="gov-card-body" style={{ padding: 0 }}>
          {loading ? (
            <div style={{ padding: '3rem', textAlign: 'center', color: 'var(--text-muted)' }}>
              Loading user credentials and security roles...
            </div>
          ) : users.length === 0 ? (
            <div style={{ padding: '2.5rem', textAlign: 'center', color: 'var(--text-muted)' }}>
              No user accounts found matching query.
            </div>
          ) : (
            <div className="gov-table-container">
              <table className="gov-table" id="users-table">
                <thead>
                  <tr>
                    <th>Full Name</th>
                    <th>Username / Email</th>
                    <th>Assigned Roles</th>
                    <th>Institutions</th>
                    <th>Status</th>
                    <th style={{ textAlign: 'right' }}>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {users.map((u) => {
                    const isSelf = currentUser?.id === u.id
                    return (
                      <tr key={u.id}>
                        <td>
                          <div style={{ fontWeight: 600, color: 'var(--gov-navy)' }}>{u.fullName}</div>
                          {isSelf && (
                            <span className="badge badge-info" style={{ fontSize: '0.65rem' }}>
                              Current Session
                            </span>
                          )}
                        </td>
                        <td>
                          <div>{u.email}</div>
                          <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>@{u.username}</div>
                        </td>
                        <td>
                          <div style={{ display: 'flex', gap: '0.3rem', flexWrap: 'wrap' }}>
                            {u.roles.map((r) => (
                              <span key={r} className="badge badge-info" style={{ fontSize: '0.72rem' }}>
                                {r.replace('ROLE_', '')}
                              </span>
                            ))}
                          </div>
                        </td>
                        <td>
                          {u.institutionIds && u.institutionIds.length > 0 ? (
                            <span className="badge badge-success">
                              {u.institutionIds.length} Institution{u.institutionIds.length > 1 ? 's' : ''}
                            </span>
                          ) : (
                            <span style={{ color: 'var(--text-muted)', fontSize: '0.8rem' }}>None (Global)</span>
                          )}
                        </td>
                        <td>
                          <span className={`badge ${u.active ? 'badge-success' : 'badge-danger'}`}>
                            {u.active ? 'Active' : 'Suspended'}
                          </span>
                        </td>
                        <td style={{ textAlign: 'right' }}>
                          <div style={{ display: 'inline-flex', gap: '0.4rem' }}>
                            <button
                              type="button"
                              className="btn btn-secondary btn-sm"
                              onClick={() => openRoleModal(u)}
                              title="Assign Security Roles"
                            >
                              <Shield size={13} />
                              <span>Roles</span>
                            </button>

                            <button
                              type="button"
                              className={`btn btn-sm ${u.active ? 'btn-danger' : 'btn-primary'}`}
                              disabled={isSelf && u.active}
                              onClick={() => setStatusModalUser(u)}
                              title={
                                isSelf && u.active
                                  ? 'Cannot suspend own active account'
                                  : u.active
                                  ? 'Suspend Account'
                                  : 'Activate Account'
                              }
                            >
                              <Power size={13} />
                              <span>{u.active ? 'Suspend' : 'Activate'}</span>
                            </button>
                          </div>
                        </td>
                      </tr>
                    )
                  })}
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

      {/* Register User Modal */}
      <Modal
        isOpen={isCreateModalOpen}
        title="Register New User Account"
        onClose={() => setIsCreateModalOpen(false)}
        footer={
          <>
            <button type="button" className="btn btn-secondary" onClick={() => setIsCreateModalOpen(false)}>
              Cancel
            </button>
            <button
              type="submit"
              form="create-user-form"
              className="btn btn-primary"
              disabled={creating}
            >
              {creating ? 'Registering...' : 'Register User'}
            </button>
          </>
        }
      >
        <form id="create-user-form" onSubmit={handleCreateUser}>
          <div className="form-group">
            <label className="form-label" htmlFor="create-fullname">
              Full Official Name <span className="required">*</span>
            </label>
            <input
              id="create-fullname"
              type="text"
              className="form-control"
              placeholder="e.g. Dr. Rajesh Kumar"
              value={createData.fullName}
              onChange={(e) => setCreateData({ ...createData, fullName: e.target.value })}
              required
            />
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem' }}>
            <div className="form-group">
              <label className="form-label" htmlFor="create-username">
                Username <span className="required">*</span>
              </label>
              <input
                id="create-username"
                type="text"
                className="form-control"
                placeholder="rajesh.kumar"
                value={createData.username}
                onChange={(e) => setCreateData({ ...createData, username: e.target.value })}
                required
              />
            </div>

            <div className="form-group">
              <label className="form-label" htmlFor="create-email">
                Email Address <span className="required">*</span>
              </label>
              <input
                id="create-email"
                type="email"
                className="form-control"
                placeholder="rajesh@aicte.gov.in"
                value={createData.email}
                onChange={(e) => setCreateData({ ...createData, email: e.target.value })}
                required
              />
            </div>
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="create-password">
              Initial Password <span className="required">*</span>
            </label>
            <input
              id="create-password"
              type="password"
              className="form-control"
              placeholder="Minimum 8 characters with upper, lower, and symbols"
              value={createData.password}
              onChange={(e) => setCreateData({ ...createData, password: e.target.value })}
              required
            />
          </div>

          <div className="form-group">
            <label className="form-label">
              Primary Role Assignment <span className="required">*</span>
            </label>
            <select
              className="form-control"
              value={createData.roles[0] || 'ROLE_INSTITUTION_OFFICER'}
              onChange={(e) => setCreateData({ ...createData, roles: [e.target.value] })}
            >
              <option value="ROLE_AICTE_ADMIN">AICTE Central Administrator</option>
              <option value="ROLE_REGIONAL_OFFICER">AICTE Regional Officer</option>
              <option value="ROLE_SCRUTINY_OFFICER">Scrutiny Officer / Expert</option>
              <option value="ROLE_EVC_COORDINATOR">EVC Coordinator</option>
              <option value="ROLE_EVC_MEMBER">EVC Committee Member</option>
              <option value="ROLE_COMMITTEE_MEMBER">Standing Committee Member</option>
              <option value="ROLE_COMPLIANCE_OFFICER">Compliance Officer</option>
              <option value="ROLE_AUDITOR">Regulatory Auditor</option>
              <option value="ROLE_INSTITUTION_OFFICER">Institution Authorized Signatory</option>
            </select>
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="create-inst-bind">
              Optional Institutional Affiliation
            </label>
            <select
              id="create-inst-bind"
              className="form-control"
              value={createData.institutionId || ''}
              onChange={(e) => setCreateData({ ...createData, institutionId: e.target.value || undefined })}
            >
              <option value="">-- None (Global AICTE Regulatory Jurisdiction) --</option>
              {institutions.map((i) => (
                <option key={i.id} value={i.id}>
                  {i.name} (PID: {i.code})
                </option>
              ))}
            </select>
          </div>
        </form>
      </Modal>

      {/* Manage Roles Modal */}
      {roleModalUser && (
        <Modal
          isOpen={!!roleModalUser}
          title={`Assign Roles: ${roleModalUser.fullName}`}
          onClose={() => setRoleModalUser(null)}
          footer={
            <>
              <button type="button" className="btn btn-secondary" onClick={() => setRoleModalUser(null)}>
                Cancel
              </button>
              <button
                type="submit"
                form="assign-roles-form"
                className="btn btn-primary"
                disabled={updatingRoles || selectedRoles.length === 0}
              >
                {updatingRoles ? 'Saving Roles...' : 'Save Role Assignments'}
              </button>
            </>
          }
        >
          <form id="assign-roles-form" onSubmit={handleAssignRoles}>
            <p style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', marginBottom: '1rem' }}>
              Select the statutory regulatory roles assigned to <strong>@{roleModalUser.username}</strong>:
            </p>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.65rem' }}>
              {roles.map((r) => {
                const checked = selectedRoles.includes(r.code)
                return (
                  <label
                    key={r.id}
                    style={{
                      display: 'flex',
                      alignItems: 'flex-start',
                      gap: '0.65rem',
                      padding: '0.65rem',
                      borderRadius: 'var(--radius-sm)',
                      backgroundColor: checked ? 'var(--gov-blue-light)' : 'var(--bg-surface-secondary)',
                      border: `1px solid ${checked ? 'var(--gov-blue)' : 'var(--border-light)'}`,
                      cursor: 'pointer',
                    }}
                  >
                    <input
                      type="checkbox"
                      checked={checked}
                      onChange={() => toggleRoleSelection(r.code)}
                      style={{ marginTop: '0.2rem' }}
                    />
                    <div>
                      <div style={{ fontWeight: 600, fontSize: '0.875rem', color: 'var(--gov-navy)' }}>
                        {r.name}
                      </div>
                      <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)' }}>
                        {r.description || r.code}
                      </div>
                    </div>
                  </label>
                )
              })}
            </div>
          </form>
        </Modal>
      )}

      {/* Suspend / Activate User Modal */}
      {statusModalUser && (
        <Modal
          isOpen={!!statusModalUser}
          title={`${statusModalUser.active ? 'Suspend' : 'Activate'} User Account`}
          onClose={() => setStatusModalUser(null)}
          footer={
            <>
              <button type="button" className="btn btn-secondary" onClick={() => setStatusModalUser(null)}>
                Cancel
              </button>
              <button
                type="submit"
                form="user-status-form"
                className={`btn ${statusModalUser.active ? 'btn-danger' : 'btn-primary'}`}
                disabled={togglingStatus}
              >
                {togglingStatus
                  ? 'Processing...'
                  : statusModalUser.active
                  ? 'Confirm Suspension'
                  : 'Confirm Activation'}
              </button>
            </>
          }
        >
          <form id="user-status-form" onSubmit={handleToggleStatus}>
            <p style={{ fontSize: '0.875rem', marginBottom: '1rem', color: 'var(--text-secondary)' }}>
              Are you sure you want to {statusModalUser.active ? 'suspend' : 'reactivate'} the account for{' '}
              <strong>{statusModalUser.fullName}</strong> (@{statusModalUser.username})?
            </p>

            <div className="form-group">
              <label className="form-label" htmlFor="user-status-reason">
                Statutory Reason for Action
              </label>
              <textarea
                id="user-status-reason"
                className="form-control"
                rows={3}
                placeholder="Reason will be persisted to the regulatory audit log"
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
