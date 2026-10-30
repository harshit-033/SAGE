import React, { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { applicationsApi } from '../api/applicationsApi'
import { institutionsApi } from '../api/institutionsApi'
import { useAuth } from '../context/AuthContext'
import { StatusBadge } from '../components/common/StatusBadge'
import { Pagination } from '../components/common/Pagination'
import { Modal } from '../components/common/Modal'
import { FileText, Plus, Filter, Eye, AlertCircle, FileCheck2 } from 'lucide-react'

interface ApplicationListItem {
  id: string
  applicationNumber: string
  institutionName: string
  category: string
  academicYear: string
  currentState: string
  submittedAt?: string
}

interface InstitutionOption {
  id: string
  name: string
  code: string
}

interface ApiErrorResponse {
  response?: {
    data?: {
      detail?: string
    }
  }
}

export const Applications: React.FC = () => {
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const { user, hasRole, isAdmin } = useAuth()

  const [page, setPage] = useState(0)
  const size = 10
  const [selectedState, setSelectedState] = useState<string>('')
  const [selectedCategory, setSelectedCategory] = useState<string>('')
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false)

  // Form State
  const [formData, setFormData] = useState({
    institutionId: '',
    academicYear: '2024-25',
    category: 'EXTENSION_OF_APPROVAL',
    courseName: '',
    programmeLevel: 'UNDERGRADUATE',
    degreeType: 'B.Tech',
    courseDiscipline: 'Computer Science and Engineering',
    proposedIntake: 60,
    existingProgramme: 'Engineering and Technology',
    existingDepartment: 'Computer Science',
    proposedBranchName: '',
    branchProposedIntake: 30,
  })
  const [formError, setFormError] = useState<string | null>(null)

  // Fetch applications list
  const { data: pageData, isLoading, error } = useQuery({
    queryKey: ['applications', { page, size, state: selectedState, category: selectedCategory }],
    queryFn: () => applicationsApi.list({
      page,
      size,
      state: selectedState || undefined,
      category: selectedCategory || undefined,
    }),
  })

  // Fetch user's institutions for dropdown if admin
  const { data: institutionsData } = useQuery({
    queryKey: ['institutions-dropdown'],
    queryFn: () => institutionsApi.listInstitutionsPaginated({ size: 100 }),
    enabled: isCreateModalOpen && (isAdmin || hasRole('ROLE_REGIONAL_OFFICER')),
  })

  // Create Application Mutation
  const createMutation = useMutation({
    mutationFn: async (data: typeof formData) => {
      const app = await applicationsApi.create({
        institutionId: data.institutionId,
        academicYear: data.academicYear,
        category: data.category,
      })
      if (data.category === 'NEW_COURSE_INTRODUCTION' && data.courseName) {
        await applicationsApi.saveCourseDetails(app.id, {
          courseName: data.courseName,
          programmeLevel: data.programmeLevel,
          degreeType: data.degreeType,
          courseDiscipline: data.courseDiscipline,
          proposedIntake: Number(data.proposedIntake) || 60,
          durationYears: 4,
          availableClassrooms: 2,
          laboratoriesCount: 2,
          professorsCount: 1,
          associateProfessorsCount: 2,
          assistantProfessorsCount: 4,
        })
      } else if (data.category === 'NEW_BRANCH_INTRODUCTION' && data.proposedBranchName) {
        await applicationsApi.saveBranchDetails(app.id, {
          existingProgramme: data.existingProgramme,
          existingDepartment: data.existingDepartment,
          proposedBranchName: data.proposedBranchName,
          proposedIntake: Number(data.branchProposedIntake) || 30,
          facultyCount: 4,
          laboratoryCount: 2,
          availableClassrooms: 2,
        })
      }
      return app
    },
    onSuccess: (newApp) => {
      queryClient.invalidateQueries({ queryKey: ['applications'] })
      setIsCreateModalOpen(false)
      navigate(`/applications/${newApp.id}`)
    },
    onError: (err: unknown) => {
      const e = err as ApiErrorResponse
      setFormError(e.response?.data?.detail || 'Failed to create application')
    },
  })

  const handleOpenCreateModal = () => {
    setFormError(null)
    const defaultInstId = user?.institutions?.[0] || ''
    setFormData({
      institutionId: defaultInstId,
      academicYear: '2024-25',
      category: 'EXTENSION_OF_APPROVAL',
      courseName: '',
      programmeLevel: 'UNDERGRADUATE',
      degreeType: 'B.Tech',
      courseDiscipline: 'Computer Science and Engineering',
      proposedIntake: 60,
      existingProgramme: 'Engineering and Technology',
      existingDepartment: 'Computer Science',
      proposedBranchName: '',
      branchProposedIntake: 30,
    })
    setIsCreateModalOpen(true)
  }

  const handleCreateSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    if (!formData.institutionId) {
      setFormError('Please select or specify an institution')
      return
    }
    createMutation.mutate(formData)
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
      {/* Page Header */}
      <div
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          flexWrap: 'wrap',
          gap: '1rem',
          backgroundColor: 'var(--bg-surface)',
          padding: '1.5rem',
          borderRadius: 'var(--radius-md)',
          border: '1px solid var(--border-medium)',
        }}
      >
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.65rem' }}>
            <FileText size={24} color="var(--gov-orange)" />
            <h1 style={{ fontSize: '1.5rem', fontWeight: 800, color: 'var(--gov-navy)' }}>
              Approval Applications
            </h1>
          </div>
          <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem', marginTop: '0.25rem' }}>
            End-to-end statutory AICTE approval workflow automation, document scrutiny &amp; EVC scheduling
          </p>
        </div>

        {(hasRole('ROLE_INSTITUTION_OFFICER') || isAdmin) && (
          <button
            onClick={handleOpenCreateModal}
            className="btn btn-primary"
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '0.5rem',
              backgroundColor: 'var(--gov-orange)',
              color: '#ffffff',
              padding: '0.65rem 1.25rem',
              borderRadius: 'var(--radius-sm)',
              fontWeight: 700,
              fontSize: '0.9rem',
              border: 'none',
              cursor: 'pointer',
            }}
          >
            <Plus size={16} />
            <span>New Approval Application</span>
          </button>
        )}
      </div>

      {/* Guidance Banner */}
      <div
        style={{
          display: 'flex',
          alignItems: 'center',
          backgroundColor: 'var(--gov-blue-light)',
          padding: '0.85rem 1.25rem',
          borderRadius: 'var(--radius-md)',
          border: '1px solid var(--border-medium)',
          gap: '0.75rem',
        }}
      >
        <FileCheck2 size={22} color="var(--gov-blue)" style={{ flexShrink: 0 }} />
        <div style={{ fontSize: '0.85rem', color: 'var(--gov-navy)', lineHeight: 1.5 }}>
          <strong>Statutory Review &amp; Evaluation:</strong> Select <strong>View Details</strong> on any listed application to review institutional specifications, verify uploaded compliance documents, and execute authoritative workflow transitions.
        </div>
      </div>

      {/* Filters Bar */}
      <div
        style={{
          display: 'flex',
          alignItems: 'center',
          gap: '1rem',
          flexWrap: 'wrap',
          backgroundColor: 'var(--bg-surface)',
          padding: '1rem',
          borderRadius: 'var(--radius-md)',
          border: '1px solid var(--border-light)',
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--text-secondary)' }}>
          <Filter size={16} />
          <span style={{ fontSize: '0.85rem', fontWeight: 600 }}>Filter by:</span>
        </div>

        <select
          value={selectedState}
          onChange={(e) => {
            setSelectedState(e.target.value)
            setPage(0)
          }}
          className="form-control"
          style={{ width: '200px', fontSize: '0.85rem' }}
          aria-label="Filter by Status"
        >
          <option value="">All Workflow States</option>
          <option value="DRAFT">DRAFT</option>
          <option value="SUBMITTED">SUBMITTED</option>
          <option value="UNDER_SCRUTINY">UNDER_SCRUTINY</option>
          <option value="DEFICIENT">DEFICIENT</option>
          <option value="RESUBMITTED">RESUBMITTED</option>
          <option value="SCRUTINY_PASSED">SCRUTINY_PASSED</option>
          <option value="EVC_SCHEDULED">EVC_SCHEDULED</option>
          <option value="SHC_REVIEW">SHC_REVIEW</option>
          <option value="APPROVED">APPROVED</option>
          <option value="REJECTED">REJECTED</option>
        </select>

        <select
          value={selectedCategory}
          onChange={(e) => {
            setSelectedCategory(e.target.value)
            setPage(0)
          }}
          className="form-control"
          style={{ width: '230px', fontSize: '0.85rem' }}
          aria-label="Filter by Category"
        >
          <option value="">All Application Categories</option>
          <option value="NEW_COURSE_INTRODUCTION">Introduction of New Course / Discipline</option>
          <option value="NEW_BRANCH_INTRODUCTION">Introduction of New Branch / Specialization</option>
          <option value="EXTENSION_OF_APPROVAL">Extension of Approval (EoA)</option>
          <option value="NEW_INSTITUTION">New Technical Institution</option>
          <option value="INCREASE_IN_INTAKE">Increase in Intake</option>
          <option value="NEW_PROGRAMME">New Programme / Division</option>
          <option value="CLOSURE">Closure of Institution</option>
        </select>
      </div>

      {/* Applications Table */}
      <div
        style={{
          backgroundColor: 'var(--bg-surface)',
          borderRadius: 'var(--radius-md)',
          border: '1px solid var(--border-medium)',
          overflow: 'hidden',
        }}
      >
        {isLoading ? (
          <div style={{ padding: '3rem', textAlign: 'center', color: 'var(--text-secondary)' }}>
            Loading applications and workflow status...
          </div>
        ) : error ? (
          <div style={{ padding: '2rem', textAlign: 'center', color: 'var(--status-danger-text)' }}>
            Error loading applications. Please refresh or verify permissions.
          </div>
        ) : !pageData?.content?.length ? (
          <div style={{ padding: '3rem', textAlign: 'center' }}>
            <FileText size={36} color="var(--text-muted)" style={{ margin: '0 auto 0.75rem' }} />
            <h3 style={{ fontSize: '1.1rem', fontWeight: 700, color: 'var(--text-primary)' }}>
              No applications found
            </h3>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.85rem', marginTop: '0.25rem' }}>
              Create an approval application draft to begin the APH statutory scrutiny workflow.
            </p>
          </div>
        ) : (
          <div style={{ overflowX: 'auto' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.85rem' }}>
              <thead>
                <tr
                  style={{
                    backgroundColor: 'var(--bg-surface-secondary)',
                    borderBottom: '1px solid var(--border-medium)',
                    color: 'var(--text-muted)',
                    fontWeight: 700,
                    textTransform: 'uppercase',
                    fontSize: '0.72rem',
                    letterSpacing: '0.05em',
                  }}
                >
                  <th style={{ padding: '0.85rem 1rem' }}>Application ID</th>
                  <th style={{ padding: '0.85rem 1rem' }}>Institution</th>
                  <th style={{ padding: '0.85rem 1rem' }}>Category</th>
                  <th style={{ padding: '0.85rem 1rem' }}>Academic Year</th>
                  <th style={{ padding: '0.85rem 1rem' }}>Current State</th>
                  <th style={{ padding: '0.85rem 1rem' }}>Created / Submitted</th>
                  <th style={{ padding: '0.85rem 1rem', textAlign: 'right' }}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {pageData.content.map((app: ApplicationListItem) => (
                  <tr
                    key={app.id}
                    style={{
                      borderBottom: '1px solid var(--border-light)',
                      transition: 'background-color 0.15s ease',
                    }}
                  >
                    <td style={{ padding: '0.85rem 1rem', fontWeight: 700, color: 'var(--gov-navy)' }}>
                      {app.applicationNumber}
                    </td>
                    <td style={{ padding: '0.85rem 1rem', color: 'var(--text-primary)' }}>
                      {app.institutionName}
                    </td>
                    <td style={{ padding: '0.85rem 1rem', color: 'var(--text-secondary)' }}>
                      {app.category.replace(/_/g, ' ')}
                    </td>
                    <td style={{ padding: '0.85rem 1rem', color: 'var(--text-secondary)' }}>
                      {app.academicYear}
                    </td>
                    <td style={{ padding: '0.85rem 1rem' }}>
                      <StatusBadge status={app.currentState} />
                    </td>
                    <td style={{ padding: '0.85rem 1rem', color: 'var(--text-muted)', fontSize: '0.8rem' }}>
                      {app.submittedAt ? new Date(app.submittedAt).toLocaleDateString() : 'Draft Mode'}
                    </td>
                    <td style={{ padding: '0.85rem 1rem', textAlign: 'right' }}>
                      <button
                        onClick={() => navigate(`/applications/${app.id}`)}
                        className="btn btn-outline"
                        style={{
                          display: 'inline-flex',
                          alignItems: 'center',
                          gap: '0.35rem',
                          padding: '0.4rem 0.75rem',
                          fontSize: '0.8rem',
                          fontWeight: 600,
                          borderRadius: 'var(--radius-xs)',
                          cursor: 'pointer',
                        }}
                      >
                        <Eye size={14} />
                        <span>View Details</span>
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {pageData && pageData.totalPages > 1 && (
          <div style={{ padding: '1rem', borderTop: '1px solid var(--border-light)' }}>
            <Pagination
              currentPage={page}
              totalPages={pageData.totalPages}
              totalElements={pageData.totalElements}
              pageSize={size}
              onPageChange={setPage}
            />
          </div>
        )}
      </div>

      {/* Create Application Modal */}
      <Modal
        isOpen={isCreateModalOpen}
        onClose={() => setIsCreateModalOpen(false)}
        title="Initiate AICTE Approval Application"
      >
        <form onSubmit={handleCreateSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          {formError && (
            <div
              style={{
                padding: '0.75rem',
                backgroundColor: 'var(--status-danger-bg)',
                color: 'var(--status-danger-text)',
                borderRadius: 'var(--radius-sm)',
                fontSize: '0.85rem',
                display: 'flex',
                alignItems: 'center',
                gap: '0.5rem',
              }}
            >
              <AlertCircle size={16} />
              <span>{formError}</span>
            </div>
          )}

          <div>
            <label style={{ display: 'block', fontSize: '0.82rem', fontWeight: 600, marginBottom: '0.35rem' }}>
              Institution
            </label>
            {isAdmin || hasRole('ROLE_REGIONAL_OFFICER') ? (
              <select
                className="form-control"
                value={formData.institutionId}
                onChange={(e) => setFormData({ ...formData, institutionId: e.target.value })}
                required
              >
                <option value="">Select Institution...</option>
                {institutionsData?.content?.map((inst: InstitutionOption) => (
                  <option key={inst.id} value={inst.id}>
                    {inst.name} ({inst.code})
                  </option>
                ))}
              </select>
            ) : (
              <input
                type="text"
                className="form-control"
                value={user?.institutions?.[0] || ''}
                disabled
              />
            )}
          </div>

          <div>
            <label style={{ display: 'block', fontSize: '0.82rem', fontWeight: 600, marginBottom: '0.35rem' }}>
              Academic Year
            </label>
            <select
              className="form-control"
              value={formData.academicYear}
              onChange={(e) => setFormData({ ...formData, academicYear: e.target.value })}
              required
            >
              <option value="2024-25">2024-25 (Current Handbook Session)</option>
              <option value="2025-26">2025-26</option>
              <option value="2026-27">2026-27</option>
            </select>
          </div>

          <div>
            <label style={{ display: 'block', fontSize: '0.82rem', fontWeight: 600, marginBottom: '0.35rem' }}>
              Application Category (APH 2024-27)
            </label>
            <select
              className="form-control"
              value={formData.category}
              onChange={(e) => setFormData({ ...formData, category: e.target.value })}
              required
            >
              <option value="NEW_COURSE_INTRODUCTION">Introduction of New Course / Discipline (APH Clause 2.4)</option>
              <option value="NEW_BRANCH_INTRODUCTION">Introduction of New Branch / Specialization (APH Clause 2.5)</option>
              <option value="EXTENSION_OF_APPROVAL">Extension of Approval (EoA - Existing Campus)</option>
              <option value="NEW_INSTITUTION">Setting up New Technical Institution</option>
              <option value="INCREASE_IN_INTAKE">Increase in Intake / Additional Course</option>
              <option value="NEW_PROGRAMME">New Programme (e.g. Engineering to Management)</option>
              <option value="CLOSURE">Closure of Programme / Institution</option>
            </select>
            <small style={{ color: 'var(--text-muted)', fontSize: '0.75rem', marginTop: '0.25rem', display: 'block' }}>
              Workflow definitions, mandatory document checklists, and committee approvals vary automatically by category.
            </small>
          </div>

          {/* Dynamic Section: New Course Introduction */}
          {formData.category === 'NEW_COURSE_INTRODUCTION' && (
            <div style={{ padding: '0.75rem', backgroundColor: 'var(--bg-page)', borderRadius: '4px', borderLeft: '3px solid var(--gov-blue)' }}>
              <div style={{ fontSize: '0.8rem', fontWeight: 700, color: 'var(--gov-navy)', marginBottom: '0.5rem' }}>
                Proposed New Course Specifications
              </div>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.5rem' }}>
                <div style={{ gridColumn: 'span 2' }}>
                  <label style={{ fontSize: '0.75rem', fontWeight: 600 }}>Proposed Course Name *</label>
                  <input
                    type="text"
                    className="form-control"
                    required
                    placeholder="e.g. Artificial Intelligence & Data Science"
                    value={formData.courseName}
                    onChange={(e) => setFormData({ ...formData, courseName: e.target.value })}
                  />
                </div>
                <div>
                  <label style={{ fontSize: '0.75rem', fontWeight: 600 }}>Programme Level</label>
                  <select
                    className="form-control"
                    value={formData.programmeLevel}
                    onChange={(e) => setFormData({ ...formData, programmeLevel: e.target.value })}
                  >
                    <option value="UNDERGRADUATE">Undergraduate (UG)</option>
                    <option value="POSTGRADUATE">Postgraduate (PG)</option>
                    <option value="DIPLOMA">Diploma</option>
                  </select>
                </div>
                <div>
                  <label style={{ fontSize: '0.75rem', fontWeight: 600 }}>Degree Type</label>
                  <input
                    type="text"
                    className="form-control"
                    value={formData.degreeType}
                    onChange={(e) => setFormData({ ...formData, degreeType: e.target.value })}
                  />
                </div>
                <div>
                  <label style={{ fontSize: '0.75rem', fontWeight: 600 }}>Course Discipline</label>
                  <input
                    type="text"
                    className="form-control"
                    value={formData.courseDiscipline}
                    onChange={(e) => setFormData({ ...formData, courseDiscipline: e.target.value })}
                  />
                </div>
                <div>
                  <label style={{ fontSize: '0.75rem', fontWeight: 600 }}>Proposed Intake</label>
                  <input
                    type="number"
                    min="1"
                    className="form-control"
                    value={formData.proposedIntake}
                    onChange={(e) => setFormData({ ...formData, proposedIntake: Number(e.target.value) })}
                  />
                </div>
              </div>
            </div>
          )}

          {/* Dynamic Section: New Branch Introduction */}
          {formData.category === 'NEW_BRANCH_INTRODUCTION' && (
            <div style={{ padding: '0.75rem', backgroundColor: 'var(--bg-page)', borderRadius: '4px', borderLeft: '3px solid var(--gov-orange)' }}>
              <div style={{ fontSize: '0.8rem', fontWeight: 700, color: 'var(--gov-navy)', marginBottom: '0.5rem' }}>
                Proposed New Branch / Specialization Specifications
              </div>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.5rem' }}>
                <div>
                  <label style={{ fontSize: '0.75rem', fontWeight: 600 }}>Existing Approved Programme</label>
                  <input
                    type="text"
                    className="form-control"
                    value={formData.existingProgramme}
                    onChange={(e) => setFormData({ ...formData, existingProgramme: e.target.value })}
                  />
                </div>
                <div>
                  <label style={{ fontSize: '0.75rem', fontWeight: 600 }}>Department</label>
                  <input
                    type="text"
                    className="form-control"
                    value={formData.existingDepartment}
                    onChange={(e) => setFormData({ ...formData, existingDepartment: e.target.value })}
                  />
                </div>
                <div>
                  <label style={{ fontSize: '0.75rem', fontWeight: 600 }}>Proposed Branch Name *</label>
                  <input
                    type="text"
                    className="form-control"
                    required
                    placeholder="e.g. Cyber Security & Digital Forensics"
                    value={formData.proposedBranchName}
                    onChange={(e) => setFormData({ ...formData, proposedBranchName: e.target.value })}
                  />
                </div>
                <div>
                  <label style={{ fontSize: '0.75rem', fontWeight: 600 }}>Proposed Intake</label>
                  <input
                    type="number"
                    min="1"
                    className="form-control"
                    value={formData.branchProposedIntake}
                    onChange={(e) => setFormData({ ...formData, branchProposedIntake: Number(e.target.value) })}
                  />
                </div>
              </div>
            </div>
          )}

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '1rem' }}>
            <button
              type="button"
              onClick={() => setIsCreateModalOpen(false)}
              className="btn btn-outline"
            >
              Cancel
            </button>
            <button
              type="submit"
              className="btn btn-primary"
              disabled={createMutation.isPending}
            >
              {createMutation.isPending ? 'Initializing...' : 'Create Draft Application'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  )
}
