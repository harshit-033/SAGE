import { render, screen, waitFor } from '@testing-library/react'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { MemoryRouter } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { AuthProvider } from '../context/AuthContext'
import { Alerts } from '../pages/Alerts'
import { Compliance } from '../pages/Compliance'
import { alertsApi } from '../api/alertsApi'
import { complianceApi } from '../api/complianceApi'
import { deadlinesApi } from '../api/deadlinesApi'

vi.mock('../api/alertsApi', () => ({
  alertsApi: {
    list: vi.fn(),
    acknowledge: vi.fn(),
    resolve: vi.fn(),
  },
}))

vi.mock('../api/complianceApi', () => ({
  complianceApi: {
    getFindings: vi.fn(),
    getCheckRuns: vi.fn(),
    triggerScan: vi.fn(),
    resolveFinding: vi.fn(),
  },
}))

vi.mock('../api/deadlinesApi', () => ({
  deadlinesApi: {
    list: vi.fn(),
    create: vi.fn(),
    recalculate: vi.fn(),
    resolve: vi.fn(),
  },
}))

const queryClient = new QueryClient({
  defaultOptions: {
    queries: { retry: false },
  },
})

describe('Phase 5: Frontend Dashboard State Handling (Loading, Empty, Error, Multi-Tenant)', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(deadlinesApi.list).mockResolvedValue({
      content: [],
      totalElements: 0,
      totalPages: 0,
      pageSize: 50,
      pageNumber: 0,
      first: true,
      last: true,
    })
  })

  it('Alerts dashboard displays loading state initially while fetching', async () => {
    vi.mocked(alertsApi.list).mockReturnValue(new Promise(() => {}))

    render(
      <QueryClientProvider client={queryClient}>
        <AuthProvider>
          <MemoryRouter>
            <Alerts />
          </MemoryRouter>
        </AuthProvider>
      </QueryClientProvider>
    )

    expect(screen.getByText(/Loading alerts from regulatory message bus/i)).toBeInTheDocument()
  })

  it('Alerts dashboard displays empty state when no alerts exist', async () => {
    vi.mocked(alertsApi.list).mockResolvedValue({
      content: [],
      totalElements: 0,
      totalPages: 0,
      pageSize: 50,
      pageNumber: 0,
      first: true,
      last: true,
    })

    render(
      <QueryClientProvider client={queryClient}>
        <AuthProvider>
          <MemoryRouter>
            <Alerts />
          </MemoryRouter>
        </AuthProvider>
      </QueryClientProvider>
    )

    await waitFor(() => {
      expect(screen.getByText(/No Active Alerts Matching Criteria/i)).toBeInTheDocument()
    })
  })

  it('Alerts dashboard displays error banner when backend API call fails', async () => {
    vi.mocked(alertsApi.list).mockRejectedValue(new Error('Network error connecting to SAGE backend'))

    render(
      <QueryClientProvider client={queryClient}>
        <AuthProvider>
          <MemoryRouter>
            <Alerts />
          </MemoryRouter>
        </AuthProvider>
      </QueryClientProvider>
    )

    await waitFor(() => {
      expect(screen.getByText(/Network error connecting to SAGE backend/i)).toBeInTheDocument()
    })
  })

  it('Compliance dashboard displays loading and empty state gracefully', async () => {
    vi.mocked(complianceApi.getFindings).mockResolvedValue({
      content: [],
      totalElements: 0,
      totalPages: 0,
      pageSize: 50,
      pageNumber: 0,
      first: true,
      last: true,
    })
    vi.mocked(complianceApi.getCheckRuns).mockResolvedValue({
      content: [],
      totalElements: 0,
      totalPages: 0,
      pageSize: 10,
      pageNumber: 0,
      first: true,
      last: true,
    })

    render(
      <QueryClientProvider client={queryClient}>
        <AuthProvider>
          <MemoryRouter>
            <Compliance />
          </MemoryRouter>
        </AuthProvider>
      </QueryClientProvider>
    )

    await waitFor(() => {
      expect(screen.getByText(/Continuous Compliance Monitor/i)).toBeInTheDocument()
    })
  })
})
