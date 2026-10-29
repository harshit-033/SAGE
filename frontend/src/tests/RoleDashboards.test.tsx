import { render, screen, waitFor } from '@testing-library/react'
import { describe, it, expect, vi } from 'vitest'
import { MemoryRouter } from 'react-router-dom'
import { AdminDashboard } from '../components/dashboard/AdminDashboard'
import { RegionalDashboard } from '../components/dashboard/RegionalDashboard'
import { InstitutionDashboard } from '../components/dashboard/InstitutionDashboard'
import { ComplianceDashboard } from '../components/dashboard/ComplianceDashboard'
import { AuditorDashboard } from '../components/dashboard/AuditorDashboard'

// Mock useAuth
const mockUser = {
  id: '11111111-1111-1111-1111-111111111111',
  username: 'admin@aicte.gov.in',
  fullName: 'AICTE Central Administrator',
  email: 'admin@aicte.gov.in',
  roles: ['ROLE_AICTE_ADMIN'],
  institutions: [],
  permissions: ['ALL'],
}

vi.mock('../context/AuthContext', () => ({
  useAuth: () => ({
    user: mockUser,
    isAdmin: true,
    hasRole: (r: string) => mockUser.roles.includes(r),
    hasPermission: () => true,
  }),
}))

// Mock APIs
vi.mock('../api/applicationsApi', () => ({
  applicationsApi: {
    list: vi.fn().mockResolvedValue({
      content: [
        {
          id: 'app-1',
          applicationNumber: 'APP-2026-0001',
          institutionName: 'Delhi Institute of Technology',
          category: 'ENGINEERING_AND_TECH',
          academicYear: '2026-2027',
          currentState: 'SUBMITTED',
        },
      ],
      totalElements: 1,
      totalPages: 1,
    }),
  },
}))

vi.mock('../api/institutionsApi', () => ({
  institutionsApi: {
    listInstitutionsPaginated: vi.fn().mockResolvedValue({
      content: [
        {
          id: '22222222-2222-2222-2222-222222222222',
          code: '1-500123498',
          name: 'Delhi Institute of Advanced Engineering',
          category: 'ENGINEERING_AND_TECH',
          city: 'New Delhi',
          state: 'Delhi',
        },
      ],
      totalElements: 1,
    }),
    getInstitutionById: vi.fn().mockResolvedValue({
      id: '22222222-2222-2222-2222-222222222222',
      code: '1-500123498',
      name: 'Delhi Institute of Advanced Engineering',
      category: 'ENGINEERING_AND_TECH',
      city: 'New Delhi',
      state: 'Delhi',
    }),
  },
}))

vi.mock('../api/usersApi', () => ({
  usersApi: {
    listUsers: vi.fn().mockResolvedValue({
      content: [],
      totalElements: 12,
    }),
  },
}))

vi.mock('../api/alertsApi', () => ({
  alertsApi: {
    list: vi.fn().mockResolvedValue({
      content: [
        {
          id: 'alert-1',
          title: 'Fire Safety Certificate Due',
          message: 'Annual Fire Safety NOC requires renewal within 30 days',
          severity: 'WARNING',
          status: 'UNREAD',
          isEscalated: false,
        },
      ],
      totalElements: 1,
    }),
  },
}))

vi.mock('../api/deadlinesApi', () => ({
  deadlinesApi: {
    list: vi.fn().mockResolvedValue({
      content: [
        {
          id: 'dl-1',
          title: 'Annual Mandatory Disclosure Submission',
          targetDate: '2026-11-15T00:00:00Z',
          statutoryClause: 'APH Clause 3.2',
          status: 'DUE_SOON',
        },
      ],
      totalElements: 1,
    }),
  },
}))

vi.mock('../api/complianceApi', () => ({
  complianceApi: {
    getFindings: vi.fn().mockResolvedValue({
      content: [
        {
          id: 'find-1',
          institutionName: 'Delhi Institute of Technology',
          title: 'Cadre Ratio Imbalance in Computer Engineering',
          findingType: 'POTENTIAL_ISSUE',
          severity: 'WARNING',
          aphClause: 'Clause 7.1',
          status: 'OPEN',
        },
      ],
      totalElements: 1,
    }),
    getCheckRuns: vi.fn().mockResolvedValue({
      content: [
        {
          id: 'run-1',
          runType: 'NIGHTLY_SCHEDULED',
          triggerSource: 'CRON_SCHEDULER',
          institutionsChecked: 45,
          findingsGenerated: 2,
          completedAt: '2026-10-30T00:00:00Z',
        },
      ],
      totalElements: 1,
    }),
    triggerScan: vi.fn().mockResolvedValue({
      institutionsChecked: 45,
      findingsGenerated: 0,
    }),
  },
}))

vi.mock('../api/auditApi', () => ({
  auditApi: {
    listAuditEvents: vi.fn().mockResolvedValue({
      content: [
        {
          id: 'audit-1',
          eventType: 'AUTH_LOGIN',
          principal: 'admin@aicte.gov.in',
          resourceType: 'AUTH_SESSION',
          resourceId: 'sess-1',
          action: 'LOGIN_SUCCESS',
          createdAt: '2026-10-30T00:15:00Z',
        },
      ],
      totalElements: 1,
    }),
  },
}))

describe('Role-Specific Dashboards & Cross-Role Isolation', () => {
  it('renders Admin Dashboard with global regulatory oversight controls', async () => {
    render(
      <MemoryRouter>
        <AdminDashboard />
      </MemoryRouter>
    )

    expect(screen.getByText(/Central Regulatory Administrator Portal/i)).toBeInTheDocument()
    expect(screen.getByText(/GLOBAL REGULATORY AUTHORITY/i)).toBeInTheDocument()
    expect(screen.getByText(/Manage Users & Roles/i)).toBeInTheDocument()
    expect(screen.getByText(/Registered Institutions/i)).toBeInTheDocument()
    expect(screen.getByText(/National Approval Pipeline/i)).toBeInTheDocument()

    await waitFor(() => {
      expect(screen.getByText('APP-2026-0001')).toBeInTheDocument()
    })
  })

  it('renders Regional Dashboard with jurisdiction scrutiny queue', async () => {
    render(
      <MemoryRouter>
        <RegionalDashboard />
      </MemoryRouter>
    )

    expect(screen.getByText(/Regional Jurisdiction Oversight Dashboard/i)).toBeInTheDocument()
    expect(screen.getByText(/Pending Scrutiny Queue/i)).toBeInTheDocument()
    expect(screen.getAllByText(/Supervisory Escalation/i).length).toBeGreaterThanOrEqual(1)
    expect(screen.getAllByText(/Regional Scrutiny Queue/i).length).toBeGreaterThanOrEqual(1)

    await waitFor(() => {
      expect(screen.getByText(/Conduct Scrutiny/i)).toBeInTheDocument()
    })
  })

  it('renders Institution Dashboard with scoped institutional profile', async () => {
    render(
      <MemoryRouter>
        <InstitutionDashboard />
      </MemoryRouter>
    )

    expect(screen.getByText(/My Applications/i)).toBeInTheDocument()
    expect(screen.getByText(/Compliance Health/i)).toBeInTheDocument()
    expect(screen.getAllByText(/Statutory Deadlines/i).length).toBeGreaterThanOrEqual(1)
    expect(screen.getAllByText(/Regulatory Notices/i).length).toBeGreaterThanOrEqual(1)

    await waitFor(() => {
      expect(screen.getByText('APP-2026-0001')).toBeInTheDocument()
    })
  })

  it('renders Compliance Dashboard with surveillance sweep controls', async () => {
    render(
      <MemoryRouter>
        <ComplianceDashboard />
      </MemoryRouter>
    )

    expect(screen.getByText(/Regulatory Compliance & Continuous Surveillance Dashboard/i)).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /Trigger Compliance Scan/i })).toBeInTheDocument()
    expect(screen.getByText(/Active Statutory Non-Compliance Findings/i)).toBeInTheDocument()

    await waitFor(() => {
      expect(screen.getByText(/Cadre Ratio Imbalance in Computer Engineering/i)).toBeInTheDocument()
    })
  })

  it('renders Auditor Dashboard with immutable ledger entries', async () => {
    render(
      <MemoryRouter>
        <AuditorDashboard />
      </MemoryRouter>
    )

    expect(screen.getByText(/Regulatory Reviewer & Independent Audit Dashboard/i)).toBeInTheDocument()
    expect(screen.getByText(/Immutable Audit Events/i)).toBeInTheDocument()
    expect(screen.getByText(/Recent Immutable Regulatory Ledger Entries/i)).toBeInTheDocument()

    await waitFor(() => {
      expect(screen.getByText('LOGIN_SUCCESS')).toBeInTheDocument()
    })
  })
})
