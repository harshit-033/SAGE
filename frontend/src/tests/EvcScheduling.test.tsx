import { render, screen, waitFor, fireEvent } from '@testing-library/react'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { MemoryRouter } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { AuthContext } from '../context/AuthContext'
import { EvcScheduling } from '../pages/EvcScheduling'
import { evcApi } from '../api/evcApi'
import type { EvcInspectionDto, ExpertDto, SchedulingResponseDto } from '../api/evcApi'

vi.mock('../api/evcApi', () => ({
  evcApi: {
    getPendingInspections: vi.fn(),
    getScheduledInspections: vi.fn(),
    getInspectionById: vi.fn(),
    getCommitteeAssignments: vi.fn(),
    generateSchedule: vi.fn(),
    getActiveExperts: vi.fn(),
    cancelInspection: vi.fn(),
  },
}))

const mockPendingInspections: EvcInspectionDto[] = [
  {
    id: 'ffffffff-ffff-ffff-ffff-000000000001',
    applicationId: '22222222-2222-2222-2222-222222222221',
    applicationNumber: 'APP-202425-DELHI-001',
    institutionId: '11111111-1111-1111-1111-222222222221',
    institutionName: 'Delhi Institute of Advanced Engineering',
    durationInDays: 2,
    requiredMemberCount: 3,
    earliestStartDate: '2026-11-02',
    latestStartDate: '2026-11-15',
    status: 'PENDING',
    assignedCommittee: [],
  },
]

const mockScheduledInspections: EvcInspectionDto[] = [
  {
    id: 'ffffffff-ffff-ffff-ffff-000000000002',
    applicationId: '22222222-2222-2222-2222-222222222222',
    applicationNumber: 'APP-202425-MUMBAI-002',
    institutionId: '11111111-1111-1111-1111-222222222222',
    institutionName: 'Mumbai College of Pharmacy',
    durationInDays: 3,
    requiredMemberCount: 3,
    earliestStartDate: '2026-11-05',
    latestStartDate: '2026-11-20',
    scheduledStartDate: '2026-11-06',
    scheduledEndDate: '2026-11-09',
    status: 'SCHEDULED',
    assignedCommittee: [
      {
        expertId: 'aaaaaaaa-aaaa-aaaa-aaaa-000000000001',
        name: 'Dr. Ramesh Sharma',
        discipline: 'Pharmacy',
        role: 'CHAIRPERSON',
        status: 'PROPOSED',
      },
      {
        expertId: 'aaaaaaaa-aaaa-aaaa-aaaa-000000000002',
        name: 'Dr. Sunita Patel',
        discipline: 'Pharmacy',
        role: 'MEMBER',
        status: 'PROPOSED',
      },
      {
        expertId: 'aaaaaaaa-aaaa-aaaa-aaaa-000000000003',
        name: 'Dr. Vikramaditya Rao',
        discipline: 'Management',
        role: 'MEMBER',
        status: 'PROPOSED',
      },
    ],
  },
]

const mockExperts: ExpertDto[] = [
  {
    id: 'aaaaaaaa-aaaa-aaaa-aaaa-000000000001',
    name: 'Dr. Ramesh Sharma',
    email: 'ramesh.sharma@iitd.ac.in',
    discipline: 'Computer Science & Engineering',
    organization: 'IIT Delhi',
    active: true,
    availableDates: ['2026-11-02', '2026-11-03', '2026-11-04'],
  },
]

function renderWithProviders(ui: React.ReactElement, authOverrides: Record<string, unknown> = {}) {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  })

  const authValue = {
    user: {
      id: '1',
      username: 'admin@aicte.gov.in',
      fullName: 'AICTE Administrator',
      roles: ['ROLE_AICTE_ADMIN'],
    },
    token: 'test-token',
    login: vi.fn(),
    logout: vi.fn(),
    isLoading: false,
    isAdmin: true,
    hasRole: (r: string) => r === 'ROLE_AICTE_ADMIN',
    hasAnyRole: (roles: string[]) => roles.includes('ROLE_AICTE_ADMIN'),
    ...authOverrides,
  }

  return render(
    <QueryClientProvider client={queryClient}>
      <AuthContext.Provider value={authValue as unknown as React.ComponentProps<typeof AuthContext.Provider>['value']}>
        <MemoryRouter>{ui}</MemoryRouter>
      </AuthContext.Provider>
    </QueryClientProvider>
  )
}

describe('EVC Scheduling Engine Frontend Workflow', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(evcApi.getPendingInspections).mockResolvedValue(mockPendingInspections)
    vi.mocked(evcApi.getScheduledInspections).mockResolvedValue(mockScheduledInspections)
    vi.mocked(evcApi.getActiveExperts).mockResolvedValue({
      content: mockExperts,
      totalElements: 1,
      totalPages: 1,
      number: 0,
      size: 50,
    })
  })

  it('renders EVC Scheduling page header, statutory badge, and KPI cards', async () => {
    renderWithProviders(<EvcScheduling />)

    expect(
      screen.getByText('Expert Visit Committee (EVC) Scheduling Engine')
    ).toBeInTheDocument()
    expect(
      screen.getByText('AICTE APH 2024–27 Statutory Verification')
    ).toBeInTheDocument()
    expect(screen.getByText('Statutory Allocation Engine')).toBeInTheDocument()

    await waitFor(() => {
      expect(screen.getByText('Delhi Institute of Advanced Engineering')).toBeInTheDocument()
    })
  })

  it('allows selection of pending inspections and displays count on action button', async () => {
    renderWithProviders(<EvcScheduling />)

    await waitFor(() => {
      expect(screen.getByText('Delhi Institute of Advanced Engineering')).toBeInTheDocument()
    })

    const selectAllCheckbox = screen.getByLabelText('Select all pending inspections')
    fireEvent.click(selectAllCheckbox)

    const scheduleBtn = screen.getByRole('button', {
      name: /Generate Committee Schedule \(1\)/i,
    })
    expect(scheduleBtn).toBeInTheDocument()
    expect(scheduleBtn).not.toBeDisabled()
  })

  it('executes CP-SAT scheduler and displays success banner upon optimal outcome', async () => {
    const optimalResponse: SchedulingResponseDto = {
      status: 'OPTIMAL',
      totalInspections: 1,
      solveDurationMs: 42,
      explanation: 'Optimal schedule determined within 42 ms.',
      scheduledInspections: [],
    }

    vi.mocked(evcApi.generateSchedule).mockResolvedValue(optimalResponse)

    renderWithProviders(<EvcScheduling />)

    await waitFor(() => {
      expect(screen.getByText('Delhi Institute of Advanced Engineering')).toBeInTheDocument()
    })

    const selectAllCheckbox = screen.getByLabelText('Select all pending inspections')
    fireEvent.click(selectAllCheckbox)

    const scheduleBtn = screen.getByRole('button', {
      name: /Generate Committee Schedule/i,
    })
    fireEvent.click(scheduleBtn)

    await waitFor(() => {
      expect(screen.getByText(/Solver Result: OPTIMAL/i)).toBeInTheDocument()
      expect(screen.getAllByText(/42 ms/i).length).toBeGreaterThanOrEqual(1)
    })
  })

  it('displays infeasible explanation banner when CP-SAT solver cannot find a valid schedule', async () => {
    const infeasibleResponse: SchedulingResponseDto = {
      status: 'INFEASIBLE',
      totalInspections: 1,
      solveDurationMs: 15,
      explanation: 'Infeasible: Insufficient eligible experts with verified calendar availability.',
      scheduledInspections: [],
    }

    vi.mocked(evcApi.generateSchedule).mockResolvedValue(infeasibleResponse)

    renderWithProviders(<EvcScheduling />)

    await waitFor(() => {
      expect(screen.getByText('Delhi Institute of Advanced Engineering')).toBeInTheDocument()
    })

    const selectAllCheckbox = screen.getByLabelText('Select all pending inspections')
    fireEvent.click(selectAllCheckbox)

    const scheduleBtn = screen.getByRole('button', {
      name: /Generate Committee Schedule/i,
    })
    fireEvent.click(scheduleBtn)

    await waitFor(() => {
      expect(screen.getByText(/Solver Result: INFEASIBLE/i)).toBeInTheDocument()
      expect(
        screen.getByText(/Insufficient eligible experts with verified calendar availability/i)
      ).toBeInTheDocument()
    })
  })

  it('switches between tabs and shows confirmed schedules and accredited experts', async () => {
    renderWithProviders(<EvcScheduling />)

    // Switch to Scheduled tab
    const scheduledTab = screen.getByRole('tab', { name: /Confirmed Inspection Schedules/i })
    fireEvent.click(scheduledTab)

    await waitFor(() => {
      expect(screen.getByText('Mumbai College of Pharmacy')).toBeInTheDocument()
      expect(screen.getByText(/2026-11-06 → 2026-11-09/i)).toBeInTheDocument()
    })

    // Switch to Experts tab
    const expertsTab = screen.getByRole('tab', { name: /Accredited Experts Registry/i })
    fireEvent.click(expertsTab)

    await waitFor(() => {
      expect(screen.getByText('IIT Delhi')).toBeInTheDocument()
      expect(screen.getByText('3 days verified')).toBeInTheDocument()
    })
  })

  it('restricts schedule generation action for institutional officer role (read-only)', async () => {
    renderWithProviders(<EvcScheduling />, {
      user: {
        id: '2',
        username: 'officer@delhi-tech.edu',
        fullName: 'Institutional Officer',
        roles: ['ROLE_INSTITUTION_OFFICER'],
      },
      isAdmin: false,
      hasRole: (r: string) => r === 'ROLE_INSTITUTION_OFFICER',
      hasAnyRole: (roles: string[]) => roles.includes('ROLE_INSTITUTION_OFFICER'),
    })

    await waitFor(() => {
      expect(screen.getByText('Delhi Institute of Advanced Engineering')).toBeInTheDocument()
    })

    // Confirm that Generate button and checkboxes are not rendered for institutional officer
    expect(screen.queryByRole('button', { name: /Generate Committee Schedule/i })).toBeNull()
    expect(screen.queryByLabelText('Select all pending inspections')).toBeNull()
  })
})
