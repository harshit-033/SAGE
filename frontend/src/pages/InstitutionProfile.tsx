import React, { useEffect, useState, useCallback } from 'react'
import { useParams, Link } from 'react-router-dom'
import { institutionsApi, type Institution, type InstitutionUser, type UpdateInstitutionData } from '../api/institutionsApi'
import { usersApi, type User } from '../api/usersApi'
import { useAuth } from '../context/AuthContext'
import { Breadcrumbs } from '../components/common/Breadcrumbs'
import { StatusBadge } from '../components/common/StatusBadge'
import { Modal } from '../components/common/Modal'
import { ConfirmModal } from '../components/common/ConfirmModal'
import { Alert } from '../components/common/Alert'
import {
  Building2,
  MapPin,
  Mail,
  Phone,
  Globe,
  Calendar,
  Users,
  Edit,
  UserPlus,
  Trash2,
  ShieldCheck,
  Compass,
} from 'lucide-react'

export const InstitutionProfile: React.FC = () => {
  const { id } = useParams<{ id: string }>()
  const { isAdmin, hasPermission, user: currentUser } = useAuth()

  const [institution, setInstitution] = useState<Institution | null>(null)
  const [affiliatedUsers, setAffiliatedUsers] = useState<InstitutionUser[]>([])
  const [allUsers, setAllUsers] = useState<User[]>([])
  const [activeTab, setActiveTab] = useState<'overview' | 'location' | 'officers'>('overview')
  const [loading, setLoading] = useState<boolean>(true)
  const [error, setError] = useState<string | null>(null)
  const [successMsg, setSuccessMsg] = useState<string | null>(null)

  // Edit Modal State
  const [isEditModalOpen, setIsEditModalOpen] = useState(false)
  const [editFormData, setEditFormData] = useState<UpdateInstitutionData>({
    name: '',
    category: 'ENGINEERING_AND_TECH',
    addressLine1: '',
    addressLine2: '',
    city: '',
    state: '',
    district: '',
    pincode: '',
    officialEmail: '',
    officialPhone: '',
    alternatePhone: '',
    websiteUrl: '',
    establishmentYear: 2000,
  })
  const [saving, setSaving] = useState(false)

  // Affiliate User Modal State
  const [isAffiliateModalOpen, setIsAffiliateModalOpen] = useState(false)
  const [selectedUserId, setSelectedUserId] = useState('')
  const [roleInInstitution, setRoleInInstitution] = useState('NODAL_OFFICER')
  const [isPrimaryContact, setIsPrimaryContact] = useState(false)
  const [affiliating, setAffiliating] = useState(false)
  const [userToDisaffiliate, setUserToDisaffiliate] = useState<string | null>(null)
  const [disaffiliating, setDisaffiliating] = useState(false)

  const loadData = useCallback(async () => {
    if (!id) return
    setLoading(true)
    setError(null)
    try {
      const [instData, usersData] = await Promise.all([
        institutionsApi.getInstitutionById(id),
        institutionsApi.getInstitutionUsers(id),
      ])
      setInstitution(instData)
      setAffiliatedUsers(usersData)

      setEditFormData({
        name: instData.name,
        category: instData.category || 'ENGINEERING_AND_TECH',
        addressLine1: instData.addressLine1 || '',
        addressLine2: instData.addressLine2 || '',
        city: instData.city || '',
        state: instData.state,
        district: instData.district,
        pincode: instData.pincode || '',
        officialEmail: instData.officialEmail || '',
        officialPhone: instData.officialPhone || '',
        alternatePhone: instData.alternatePhone || '',
        websiteUrl: instData.websiteUrl || '',
        establishmentYear: instData.establishmentYear || 2000,
        latitude: instData.latitude,
        longitude: instData.longitude,
      })

      if (isAdmin) {
        const uRes = await usersApi.listUsers({ size: 100 })
        setAllUsers(uRes.content)
      }
    } catch (err: unknown) {
      const errorObj = err as { problem?: { detail?: string }; message?: string }
      setError(errorObj?.problem?.detail || errorObj?.message || 'Failed to load institution profile')
    } finally {
      setLoading(false)
    }
  }, [id, isAdmin])

  useEffect(() => {
    loadData()
  }, [loadData])

  const handleUpdateProfile = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!id) return
    setSaving(true)
    setError(null)
    try {
      const updated = await institutionsApi.updateInstitution(id, editFormData)
      setInstitution(updated)
      setIsEditModalOpen(false)
      setSuccessMsg('Institution profile updated successfully.')
    } catch (err: unknown) {
      const errorObj = err as { problem?: { detail?: string }; message?: string }
      setError(errorObj?.problem?.detail || errorObj?.message || 'Failed to update profile')
    } finally {
      setSaving(false)
    }
  }

  const handleAffiliateUser = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!id || !selectedUserId) return
    setAffiliating(true)
    setError(null)
    try {
      await institutionsApi.addInstitutionUser(id, {
        userId: selectedUserId,
        roleInInstitution,
        primaryContact: isPrimaryContact,
      })
      setIsAffiliateModalOpen(false)
      setSelectedUserId('')
      setSuccessMsg('Officer successfully affiliated with institution.')
      await loadData()
    } catch (err: unknown) {
      const errorObj = err as { problem?: { detail?: string }; message?: string }
      setError(errorObj?.problem?.detail || errorObj?.message || 'Failed to affiliate user')
    } finally {
      setAffiliating(false)
    }
  }

  const handleRemoveUser = (userId: string) => {
    setUserToDisaffiliate(userId)
  }

  const confirmDisaffiliate = async () => {
    if (!id || !userToDisaffiliate) return
    try {
      setDisaffiliating(true)
      await institutionsApi.removeInstitutionUser(id, userToDisaffiliate)
      setSuccessMsg('Officer disaffiliated successfully.')
      setUserToDisaffiliate(null)
      await loadData()
    } catch (err: unknown) {
      const errorObj = err as { problem?: { detail?: string }; message?: string }
      setError(errorObj?.problem?.detail || errorObj?.message || 'Failed to disaffiliate user')
    } finally {
      setDisaffiliating(false)
    }
  }

  const canEdit = isAdmin || (hasPermission('INSTITUTION_WRITE') && currentUser?.institutions.includes(id || ''))

  if (loading) {
    return (
      <div style={{ padding: '3rem', textAlign: 'center', color: 'var(--text-muted)' }}>
        <p>Loading institutional profile and regulatory records...</p>
      </div>
    )
  }

  if (error && !institution) {
    return (
      <div>
        <Breadcrumbs items={[{ label: 'Institutions', to: '/institutions' }, { label: 'Error' }]} />
        <Alert type="danger" title="Regulatory Tenancy Restriction">
          {error}
        </Alert>
        <Link to="/institutions" className="btn btn-secondary">
          Return to Institutions Directory
        </Link>
      </div>
    )
  }

  if (!institution) return null

  return (
    <div>
      <Breadcrumbs
        items={[
          { label: 'Institutions', to: '/institutions' },
          { label: institution.name },
        ]}
      />

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

      {/* Profile Header Card */}
      <div className="gov-card">
        <div className="gov-card-body" style={{ padding: '1.5rem' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '1rem' }}>
            <div style={{ display: 'flex', alignItems: 'flex-start', gap: '1rem' }}>
              <div
                style={{
                  width: '54px',
                  height: '54px',
                  backgroundColor: 'var(--gov-blue-light)',
                  color: 'var(--gov-blue)',
                  borderRadius: 'var(--radius-sm)',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  flexShrink: 0,
                  border: '1px solid var(--border-medium)',
                }}
              >
                <Building2 size={28} />
              </div>

              <div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.65rem', flexWrap: 'wrap' }}>
                  <h1 style={{ fontSize: '1.4rem', color: 'var(--gov-navy)' }}>{institution.name}</h1>
                  <StatusBadge status={institution.active ? 'ACTIVE' : 'SUSPENDED'} />
                  <span className="badge badge-info">{institution.category || 'TECHNICAL'}</span>
                </div>
                <div style={{ display: 'flex', gap: '1rem', marginTop: '0.35rem', fontSize: '0.82rem', color: 'var(--text-secondary)' }}>
                  <span>
                    <strong>AICTE PID:</strong> {institution.code}
                  </span>
                  <span>&bull;</span>
                  <span>
                    <strong>Location:</strong> {institution.district}, {institution.state}
                  </span>
                  <span>&bull;</span>
                  <span>
                    <strong>Internal ID:</strong> <code style={{ fontSize: '0.75rem' }}>{institution.id}</code>
                  </span>
                </div>
              </div>
            </div>

            {canEdit && (
              <button
                type="button"
                className="btn btn-primary"
                onClick={() => setIsEditModalOpen(true)}
                id="edit-profile-btn"
              >
                <Edit size={15} />
                <span>Edit Profile</span>
              </button>
            )}
          </div>
        </div>
      </div>

      {/* Tabs */}
      <div className="gov-tabs" role="tablist">
        <button
          type="button"
          role="tab"
          aria-selected={activeTab === 'overview'}
          className={`gov-tab-item ${activeTab === 'overview' ? 'active' : ''}`}
          onClick={() => setActiveTab('overview')}
        >
          Institutional Overview
        </button>
        <button
          type="button"
          role="tab"
          aria-selected={activeTab === 'location'}
          className={`gov-tab-item ${activeTab === 'location' ? 'active' : ''}`}
          onClick={() => setActiveTab('location')}
        >
          Campus Location &amp; Coordinates
        </button>
        <button
          type="button"
          role="tab"
          aria-selected={activeTab === 'officers'}
          className={`gov-tab-item ${activeTab === 'officers' ? 'active' : ''}`}
          onClick={() => setActiveTab('officers')}
        >
          Authorized Signatories ({affiliatedUsers.length})
        </button>
      </div>

      {/* Tab 1: Overview */}
      {activeTab === 'overview' && (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '1.25rem' }}>
          <div className="gov-card">
            <div className="gov-card-header">
              <span className="gov-card-title">
                <Building2 size={16} /> Official Contact Details
              </span>
            </div>
            <div className="gov-card-body" style={{ display: 'flex', flexDirection: 'column', gap: '0.85rem' }}>
              <div>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', fontWeight: 600 }}>Official Email</div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginTop: '0.2rem' }}>
                  <Mail size={15} color="var(--gov-blue)" />
                  <a href={`mailto:${institution.officialEmail || ''}`}>{institution.officialEmail || 'Not provided'}</a>
                </div>
              </div>

              <div>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', fontWeight: 600 }}>Official Telephone</div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginTop: '0.2rem' }}>
                  <Phone size={15} color="var(--gov-blue)" />
                  <span>{institution.officialPhone || 'Not provided'}</span>
                </div>
              </div>

              <div>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', fontWeight: 600 }}>Alternate Helpline</div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginTop: '0.2rem' }}>
                  <Phone size={15} color="var(--text-muted)" />
                  <span>{institution.alternatePhone || 'None'}</span>
                </div>
              </div>

              <div>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', fontWeight: 600 }}>Website URL</div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginTop: '0.2rem' }}>
                  <Globe size={15} color="var(--gov-blue)" />
                  {institution.websiteUrl ? (
                    <a href={institution.websiteUrl} target="_blank" rel="noopener noreferrer">
                      {institution.websiteUrl}
                    </a>
                  ) : (
                    <span>Not provided</span>
                  )}
                </div>
              </div>
            </div>
          </div>

          <div className="gov-card">
            <div className="gov-card-header">
              <span className="gov-card-title">
                <ShieldCheck size={16} /> Statutory Registration
              </span>
            </div>
            <div className="gov-card-body" style={{ display: 'flex', flexDirection: 'column', gap: '0.85rem' }}>
              <div>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', fontWeight: 600 }}>Establishment Year</div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginTop: '0.2rem' }}>
                  <Calendar size={15} color="var(--gov-blue)" />
                  <span>{institution.establishmentYear || 'Not recorded'}</span>
                </div>
              </div>

              <div>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', fontWeight: 600 }}>Application Stream</div>
                <div style={{ marginTop: '0.2rem' }}>
                  <span className="badge badge-info">{institution.applicationType}</span>
                </div>
              </div>

              <div>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', fontWeight: 600 }}>Record Created</div>
                <div style={{ marginTop: '0.2rem', fontSize: '0.85rem' }}>
                  {new Date(institution.createdAt).toLocaleString()}
                </div>
              </div>

              <div>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', fontWeight: 600 }}>Last Regulatory Update</div>
                <div style={{ marginTop: '0.2rem', fontSize: '0.85rem' }}>
                  {new Date(institution.updatedAt).toLocaleString()}
                </div>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Tab 2: Location */}
      {activeTab === 'location' && (
        <div className="gov-card">
          <div className="gov-card-header">
            <span className="gov-card-title">
              <MapPin size={16} /> Campus Address &amp; Geographic Information
            </span>
          </div>
          <div className="gov-card-body">
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(260px, 1fr))', gap: '1.25rem' }}>
              <div>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', fontWeight: 600 }}>Address Line 1</div>
                <div style={{ marginTop: '0.2rem', fontSize: '0.9rem' }}>{institution.addressLine1 || 'Not specified'}</div>
              </div>

              <div>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', fontWeight: 600 }}>Address Line 2</div>
                <div style={{ marginTop: '0.2rem', fontSize: '0.9rem' }}>{institution.addressLine2 || 'None'}</div>
              </div>

              <div>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', fontWeight: 600 }}>City / Town</div>
                <div style={{ marginTop: '0.2rem', fontSize: '0.9rem' }}>{institution.city || 'Not specified'}</div>
              </div>

              <div>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', fontWeight: 600 }}>District</div>
                <div style={{ marginTop: '0.2rem', fontSize: '0.9rem' }}>{institution.district}</div>
              </div>

              <div>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', fontWeight: 600 }}>State / UT</div>
                <div style={{ marginTop: '0.2rem', fontSize: '0.9rem' }}>{institution.state}</div>
              </div>

              <div>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', fontWeight: 600 }}>Postal Pincode</div>
                <div style={{ marginTop: '0.2rem', fontSize: '0.9rem' }}>{institution.pincode || 'Not recorded'}</div>
              </div>

              <div>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', fontWeight: 600 }}>GIS Latitude</div>
                <div style={{ marginTop: '0.2rem', fontSize: '0.9rem' }}>{institution.latitude ?? 'Not calibrated'}</div>
              </div>

              <div>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', fontWeight: 600 }}>GIS Longitude</div>
                <div style={{ marginTop: '0.2rem', fontSize: '0.9rem' }}>{institution.longitude ?? 'Not calibrated'}</div>
              </div>
            </div>

            {institution.latitude && institution.longitude && (
              <div
                style={{
                  marginTop: '1.5rem',
                  padding: '1rem',
                  backgroundColor: 'var(--bg-surface-secondary)',
                  border: '1px solid var(--border-medium)',
                  borderRadius: 'var(--radius-sm)',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '0.75rem',
                }}
              >
                <Compass size={20} color="var(--gov-blue)" />
                <div style={{ fontSize: '0.85rem' }}>
                  Calibrated coordinates: <strong>{institution.latitude}&deg; N, {institution.longitude}&deg; E</strong>
                  <span style={{ color: 'var(--text-muted)', marginLeft: '0.5rem' }}>
                    (Ready for automated GIS scrutiny and spatial zone validation)
                  </span>
                </div>
              </div>
            )}
          </div>
        </div>
      )}

      {/* Tab 3: Officers */}
      {activeTab === 'officers' && (
        <div className="gov-card">
          <div className="gov-card-header">
            <span className="gov-card-title">
              <Users size={16} /> Authorized Representatives &amp; Signatories
            </span>
            {canEdit && (
              <button
                type="button"
                className="btn btn-secondary btn-sm"
                onClick={() => setIsAffiliateModalOpen(true)}
              >
                <UserPlus size={14} />
                <span>Affiliate Officer</span>
              </button>
            )}
          </div>
          <div className="gov-card-body" style={{ padding: 0 }}>
            {affiliatedUsers.length === 0 ? (
              <div style={{ padding: '2rem', textAlign: 'center', color: 'var(--text-muted)' }}>
                No institutional officers currently affiliated with this record.
              </div>
            ) : (
              <div className="gov-table-container">
                <table className="gov-table">
                  <thead>
                    <tr>
                      <th>Full Name</th>
                      <th>Account Username / Email</th>
                      <th>Role in Institution</th>
                      <th>Signatory Status</th>
                      <th>Affiliated Since</th>
                      {canEdit && <th>Action</th>}
                    </tr>
                  </thead>
                  <tbody>
                    {affiliatedUsers.map((u) => (
                      <tr key={u.id}>
                        <td style={{ fontWeight: 600 }}>{u.fullName}</td>
                        <td>{u.email}</td>
                        <td>
                          <span className="badge badge-info">{u.roleInInstitution}</span>
                        </td>
                        <td>
                          {u.primaryContact ? (
                            <span className="badge badge-success">Primary Signatory</span>
                          ) : (
                            <span style={{ color: 'var(--text-muted)' }}>Authorized Officer</span>
                          )}
                        </td>
                        <td>{new Date(u.createdAt).toLocaleDateString()}</td>
                        {canEdit && (
                          <td>
                            <button
                              type="button"
                              className="btn btn-danger btn-sm"
                              onClick={() => handleRemoveUser(u.userId)}
                              title="Disaffiliate Officer"
                            >
                              <Trash2 size={13} />
                              <span>Remove</span>
                            </button>
                          </td>
                        )}
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>
      )}

      {/* Edit Profile Modal */}
      <Modal
        isOpen={isEditModalOpen}
        title="Edit Institution Profile"
        onClose={() => setIsEditModalOpen(false)}
        maxWidth="700px"
        footer={
          <>
            <button type="button" className="btn btn-secondary" onClick={() => setIsEditModalOpen(false)}>
              Cancel
            </button>
            <button
              type="submit"
              form="edit-inst-form"
              className="btn btn-primary"
              disabled={saving}
            >
              {saving ? 'Saving Changes...' : 'Save Profile Changes'}
            </button>
          </>
        }
      >
        <form id="edit-inst-form" onSubmit={handleUpdateProfile}>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '0.85rem' }}>
            <div className="form-group" style={{ gridColumn: '1 / -1' }}>
              <label className="form-label" htmlFor="name">
                Institution Name <span className="required">*</span>
              </label>
              <input
                id="name"
                type="text"
                className="form-control"
                value={editFormData.name}
                onChange={(e) => setEditFormData({ ...editFormData, name: e.target.value })}
                required
              />
            </div>

            <div className="form-group">
              <label className="form-label" htmlFor="category">Category</label>
              <select
                id="category"
                className="form-control"
                value={editFormData.category}
                onChange={(e) => setEditFormData({ ...editFormData, category: e.target.value })}
              >
                <option value="ENGINEERING_AND_TECH">Engineering &amp; Technology</option>
                <option value="PHARMACY">Pharmacy</option>
                <option value="MANAGEMENT">Management (MBA / PGDM)</option>
                <option value="ARCHITECTURE">Architecture &amp; Town Planning</option>
                <option value="POLYTECHNIC">Polytechnic / Applied Arts</option>
              </select>
            </div>

            <div className="form-group">
              <label className="form-label" htmlFor="establishmentYear">Establishment Year</label>
              <input
                id="establishmentYear"
                type="number"
                className="form-control"
                value={editFormData.establishmentYear || ''}
                onChange={(e) => setEditFormData({ ...editFormData, establishmentYear: Number(e.target.value) })}
              />
            </div>

            <div className="form-group">
              <label className="form-label" htmlFor="officialEmail">Official Email</label>
              <input
                id="officialEmail"
                type="email"
                className="form-control"
                value={editFormData.officialEmail || ''}
                onChange={(e) => setEditFormData({ ...editFormData, officialEmail: e.target.value })}
              />
            </div>

            <div className="form-group">
              <label className="form-label" htmlFor="officialPhone">Official Phone</label>
              <input
                id="officialPhone"
                type="text"
                className="form-control"
                value={editFormData.officialPhone || ''}
                onChange={(e) => setEditFormData({ ...editFormData, officialPhone: e.target.value })}
              />
            </div>

            <div className="form-group">
              <label className="form-label" htmlFor="alternatePhone">Alternate Helpline</label>
              <input
                id="alternatePhone"
                type="text"
                className="form-control"
                value={editFormData.alternatePhone || ''}
                onChange={(e) => setEditFormData({ ...editFormData, alternatePhone: e.target.value })}
              />
            </div>

            <div className="form-group">
              <label className="form-label" htmlFor="websiteUrl">Website URL</label>
              <input
                id="websiteUrl"
                type="url"
                className="form-control"
                value={editFormData.websiteUrl || ''}
                onChange={(e) => setEditFormData({ ...editFormData, websiteUrl: e.target.value })}
              />
            </div>

            <div className="form-group" style={{ gridColumn: '1 / -1' }}>
              <label className="form-label" htmlFor="addressLine1">Address Line 1</label>
              <input
                id="addressLine1"
                type="text"
                className="form-control"
                value={editFormData.addressLine1 || ''}
                onChange={(e) => setEditFormData({ ...editFormData, addressLine1: e.target.value })}
              />
            </div>

            <div className="form-group" style={{ gridColumn: '1 / -1' }}>
              <label className="form-label" htmlFor="addressLine2">Address Line 2</label>
              <input
                id="addressLine2"
                type="text"
                className="form-control"
                value={editFormData.addressLine2 || ''}
                onChange={(e) => setEditFormData({ ...editFormData, addressLine2: e.target.value })}
              />
            </div>

            <div className="form-group">
              <label className="form-label" htmlFor="city">City</label>
              <input
                id="city"
                type="text"
                className="form-control"
                value={editFormData.city || ''}
                onChange={(e) => setEditFormData({ ...editFormData, city: e.target.value })}
              />
            </div>

            <div className="form-group">
              <label className="form-label" htmlFor="district">
                District <span className="required">*</span>
              </label>
              <input
                id="district"
                type="text"
                className="form-control"
                value={editFormData.district}
                onChange={(e) => setEditFormData({ ...editFormData, district: e.target.value })}
                required
              />
            </div>

            <div className="form-group">
              <label className="form-label" htmlFor="state">
                State <span className="required">*</span>
              </label>
              <input
                id="state"
                type="text"
                className="form-control"
                value={editFormData.state}
                onChange={(e) => setEditFormData({ ...editFormData, state: e.target.value })}
                required
              />
            </div>

            <div className="form-group">
              <label className="form-label" htmlFor="pincode">Pincode</label>
              <input
                id="pincode"
                type="text"
                className="form-control"
                value={editFormData.pincode || ''}
                onChange={(e) => setEditFormData({ ...editFormData, pincode: e.target.value })}
              />
            </div>

            <div className="form-group">
              <label className="form-label" htmlFor="latitude">Latitude (&deg; N)</label>
              <input
                id="latitude"
                type="number"
                step="any"
                className="form-control"
                value={editFormData.latitude ?? ''}
                onChange={(e) => setEditFormData({ ...editFormData, latitude: e.target.value ? Number(e.target.value) : undefined })}
              />
            </div>

            <div className="form-group">
              <label className="form-label" htmlFor="longitude">Longitude (&deg; E)</label>
              <input
                id="longitude"
                type="number"
                step="any"
                className="form-control"
                value={editFormData.longitude ?? ''}
                onChange={(e) => setEditFormData({ ...editFormData, longitude: e.target.value ? Number(e.target.value) : undefined })}
              />
            </div>
          </div>
        </form>
      </Modal>

      {/* Affiliate User Modal */}
      <Modal
        isOpen={isAffiliateModalOpen}
        title="Affiliate Institutional Officer"
        onClose={() => setIsAffiliateModalOpen(false)}
        footer={
          <>
            <button type="button" className="btn btn-secondary" onClick={() => setIsAffiliateModalOpen(false)}>
              Cancel
            </button>
            <button
              type="submit"
              form="affiliate-form"
              className="btn btn-primary"
              disabled={affiliating || !selectedUserId}
            >
              {affiliating ? 'Affiliating...' : 'Confirm Affiliation'}
            </button>
          </>
        }
      >
        <form id="affiliate-form" onSubmit={handleAffiliateUser}>
          <div className="form-group">
            <label className="form-label" htmlFor="user-select">
              Select User Account <span className="required">*</span>
            </label>
            <select
              id="user-select"
              className="form-control"
              value={selectedUserId}
              onChange={(e) => setSelectedUserId(e.target.value)}
              required
            >
              <option value="">-- Choose User --</option>
              {allUsers.map((u) => (
                <option key={u.id} value={u.id}>
                  {u.fullName} ({u.email})
                </option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="role-title">
              Role in Institution <span className="required">*</span>
            </label>
            <input
              id="role-title"
              type="text"
              className="form-control"
              value={roleInInstitution}
              onChange={(e) => setRoleInInstitution(e.target.value)}
              placeholder="e.g. DIRECTOR, PRINCIPAL, NODAL_OFFICER"
              required
            />
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginTop: '0.5rem' }}>
            <input
              id="is-primary"
              type="checkbox"
              checked={isPrimaryContact}
              onChange={(e) => setIsPrimaryContact(e.target.checked)}
            />
            <label htmlFor="is-primary" style={{ fontSize: '0.85rem', fontWeight: 600 }}>
              Primary Institutional Signatory (Authorized Representative)
            </label>
          </div>
        </form>
      </Modal>

      <ConfirmModal
        isOpen={!!userToDisaffiliate}
        title="Confirm Officer Disaffiliation"
        message="Are you sure you want to disaffiliate this officer from the institution?"
        description="This will revoke the officer's institutional permissions for this technical institution. The user account itself will remain active in the system."
        confirmText="Disaffiliate Officer"
        cancelText="Cancel"
        confirmVariant="danger"
        isLoading={disaffiliating}
        onConfirm={confirmDisaffiliate}
        onCancel={() => setUserToDisaffiliate(null)}
      />
    </div>
  )
}
