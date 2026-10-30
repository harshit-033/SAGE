import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import { describe, it, expect, vi } from 'vitest'
import { MemoryRouter } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { MessageProvider, useMessage } from '../context/MessageContext'
import { sanitizeApiError, sanitizeTechnicalMessage } from '../utils/messageSanitizer'
import { MessageBox } from '../components/common/MessageBox'
import { Sidebar } from '../components/layout/Sidebar'
import { Navbar } from '../components/layout/Navbar'
import { Login } from '../pages/Login'
import { EvcCoordinatorDashboard } from '../components/dashboard/EvcCoordinatorDashboard'
import { AuthContext } from '../context/AuthContext'
import { ApiError } from '../api/client'

const queryClient = new QueryClient({
  defaultOptions: { queries: { retry: false } },
})

// Helper component to trigger messages in tests
const TestMessageTrigger: React.FC = () => {
  const { showSuccess, showError, showWarning, showInfo, showConfirmation } = useMessage()

  return (
    <div>
      <button
        onClick={() => showSuccess('Application Submitted', 'Your application has been submitted successfully.')}
        id="trigger-success-btn"
      >
        Trigger Success
      </button>
      <button
        onClick={() => showError('Document Upload Failed', 'The document could not be uploaded.', 'Please try again.', 'c-abc-123')}
        id="trigger-error-btn"
      >
        Trigger Error
      </button>
      <button
        onClick={() => showWarning('Replace Document?', 'A new version of this document will be submitted.')}
        id="trigger-warning-btn"
      >
        Trigger Warning
      </button>
      <button
        onClick={() => showInfo('Policy Update', 'New AICTE APH guidelines take effect next month.')}
        id="trigger-info-btn"
      >
        Trigger Info
      </button>
      <button
        onClick={async () => {
          const confirmed = await showConfirmation({
            title: 'Withdraw Application?',
            message: 'Are you sure you want to withdraw this application?',
            confirmText: 'Confirm Withdrawal',
            cancelText: 'Cancel',
            confirmVariant: 'danger',
          })
          const el = document.getElementById('confirmation-result')
          if (el) el.textContent = confirmed ? 'CONFIRMED' : 'CANCELLED'
        }}
        id="trigger-confirm-btn"
      >
        Trigger Confirm
      </button>
      <div id="confirmation-result">PENDING</div>
    </div>
  )
}

describe('Centralized Message Box System', () => {
  it('renders SUCCESS message box with appropriate heading, body, and dismiss action', async () => {
    render(
      <MessageProvider>
        <TestMessageTrigger />
        <MessageBox />
      </MessageProvider>
    )

    fireEvent.click(screen.getByRole('button', { name: 'Trigger Success' }))

    expect(screen.getByRole('alertdialog')).toBeInTheDocument()
    expect(screen.getByText('Application Submitted')).toBeInTheDocument()
    expect(screen.getByText('Your application has been submitted successfully.')).toBeInTheDocument()

    // Dismiss dialog
    fireEvent.click(screen.getByRole('button', { name: 'Acknowledge' }))
    expect(screen.queryByRole('alertdialog')).not.toBeInTheDocument()
  })

  it('renders ERROR message box with polite text and support reference ID', async () => {
    render(
      <MessageProvider>
        <TestMessageTrigger />
        <MessageBox />
      </MessageProvider>
    )

    fireEvent.click(screen.getByRole('button', { name: 'Trigger Error' }))

    expect(screen.getByRole('alertdialog')).toBeInTheDocument()
    expect(screen.getByText('Document Upload Failed')).toBeInTheDocument()
    expect(screen.getByText('The document could not be uploaded.')).toBeInTheDocument()
    expect(screen.getByText('c-abc-123')).toBeInTheDocument()

    fireEvent.click(screen.getByRole('button', { name: 'Acknowledge' }))
    expect(screen.queryByRole('alertdialog')).not.toBeInTheDocument()
  })

  it('handles CONFIRMATION dialog with asynchronous confirm and cancel resolutions', async () => {
    render(
      <MessageProvider>
        <TestMessageTrigger />
        <MessageBox />
      </MessageProvider>
    )

    // 1. Cancel flow
    fireEvent.click(screen.getByRole('button', { name: 'Trigger Confirm' }))
    expect(screen.getByText('Withdraw Application?')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Confirm Withdrawal' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Cancel' })).toBeInTheDocument()

    fireEvent.click(screen.getByRole('button', { name: 'Cancel' }))
    expect(screen.queryByRole('alertdialog')).not.toBeInTheDocument()
    await waitFor(() => {
      expect(screen.getByText('CANCELLED')).toBeInTheDocument()
    })

    // 2. Confirm flow
    fireEvent.click(screen.getByRole('button', { name: 'Trigger Confirm' }))
    fireEvent.click(screen.getByRole('button', { name: 'Confirm Withdrawal' }))
    expect(screen.queryByRole('alertdialog')).not.toBeInTheDocument()
    await waitFor(() => {
      expect(screen.getByText('CONFIRMED')).toBeInTheDocument()
    })
  })

  it('sanitizes technical errors into polite institutional descriptions', () => {
    expect(sanitizeTechnicalMessage('backend service unavailable')).toBe(
      'The service is temporarily unavailable. Please try again later.'
    )
    expect(sanitizeTechnicalMessage('API request failed')).toBe(
      'We could not complete your request. Please try again.'
    )
    expect(sanitizeTechnicalMessage('database connection error')).toBe(
      'The system is temporarily unable to retrieve this information.'
    )
    expect(sanitizeTechnicalMessage('Internal server error')).toBe(
      'Something went wrong while processing your request.'
    )
    expect(sanitizeTechnicalMessage('Indra OCR analysis completed')).toBe(
      'Automated Document Verification analysis completed'
    )

    // ApiError mapping
    const forbiddenError = new ApiError({
      status: 403,
      title: 'Forbidden',
      detail: 'Access denied',
      correlationId: 'c-test-99',
    })
    const sanitized403 = sanitizeApiError(forbiddenError)
    expect(sanitized403.title).toBe('Authorization Required')
    expect(sanitized403.message).toBe('You do not have permission to perform this regulatory action.')
    expect(sanitized403.correlationId).toBe('c-test-99')

    const notFoundError = new ApiError({
      status: 404,
      title: 'Not Found',
      detail: 'Entity not found',
      correlationId: 'c-test-44',
    })
    const sanitized404 = sanitizeApiError(notFoundError)
    expect(sanitized404.title).toBe('Record Not Found')
    expect(sanitized404.message).toBe('The requested institutional record or document could not be found.')
  })
})

describe('Role-Specific Sidebar Navigation Matrix', () => {
  const renderSidebarWithUser = (user: {
    id: string
    username: string
    roles: string[]
    institutions: string[]
  }) => {
    const authValue = {
      user: {
        id: user.id,
        username: user.username,
        email: user.username,
        fullName: 'Test Officer',
        roles: user.roles,
        institutions: user.institutions,
        permissions: [],
      },
      token: 'mock-token',
      isLoading: false,
      login: vi.fn(),
      logout: vi.fn(),
      hasRole: (role: string) => user.roles.includes(role),
      hasAnyRole: (roles: string[]) => roles.some((r) => user.roles.includes(r)),
      hasPermission: () => true,
      isAdmin: user.roles.includes('ROLE_AICTE_ADMIN'),
      isInstitutionOfficer: user.roles.includes('ROLE_INSTITUTION_OFFICER'),
    }

    return render(
      <AuthContext.Provider value={authValue}>
        <MemoryRouter>
          <Sidebar />
        </MemoryRouter>
      </AuthContext.Provider>
    )
  }

  it('renders full governance navigation for ROLE_AICTE_ADMIN', () => {
    renderSidebarWithUser({
      id: 'admin-1',
      username: 'admin@aicte.gov.in',
      roles: ['ROLE_AICTE_ADMIN'],
      institutions: [],
    })

    expect(screen.getByText('Central Governance')).toBeInTheDocument()
    expect(screen.getByText('Dashboard')).toBeInTheDocument()
    expect(screen.getByText('Applications & Scrutiny')).toBeInTheDocument()
    expect(screen.getByText('Documents & Compliance')).toBeInTheDocument()
    expect(screen.getByText('Required Actions')).toBeInTheDocument()
    expect(screen.getByText('Compliance & Deadlines')).toBeInTheDocument()
    expect(screen.getByText('Regulatory Alerts')).toBeInTheDocument()
    expect(screen.getByText('Institutions Directory')).toBeInTheDocument()
    expect(screen.getByText('EVC Scheduling')).toBeInTheDocument()
    expect(screen.getByText('Users & Roles')).toBeInTheDocument()
    expect(screen.getByText('Regulatory Audit Trail')).toBeInTheDocument()
    expect(screen.getByText('Platform Operations')).toBeInTheDocument()

    // Strictly hidden: My Institution tab is NOT shown to Admin
    expect(screen.queryByText('My Institution')).not.toBeInTheDocument()
  })

  it('restricts navigation for ROLE_INSTITUTION_OFFICER to institutional scope', () => {
    renderSidebarWithUser({
      id: 'officer-1',
      username: 'officer@delhi-tech.edu',
      roles: ['ROLE_INSTITUTION_OFFICER'],
      institutions: ['22222222-2222-2222-2222-222222222222'],
    })

    expect(screen.getByText('Institutional Portal')).toBeInTheDocument()
    expect(screen.getByText('Institution Dashboard')).toBeInTheDocument()
    expect(screen.getByText('My Institution')).toBeInTheDocument()
    expect(screen.getByText('My Applications')).toBeInTheDocument()
    expect(screen.getByText('Document Compliance')).toBeInTheDocument()
    expect(screen.getByText('Required Actions')).toBeInTheDocument()
    expect(screen.getByText('Regulatory Alerts')).toBeInTheDocument()

    // STRICTLY HIDDEN for Institution Officers
    expect(screen.queryByText('Institutions Directory')).not.toBeInTheDocument()
    expect(screen.queryByText('EVC Scheduling')).not.toBeInTheDocument()
    expect(screen.queryByText('Compliance & Deadlines')).not.toBeInTheDocument()
    expect(screen.queryByText('Users & Roles')).not.toBeInTheDocument()
    expect(screen.queryByText('Regulatory Audit Trail')).not.toBeInTheDocument()
    expect(screen.queryByText('Platform Operations')).not.toBeInTheDocument()
  })

  it('restricts navigation for ROLE_REGIONAL_OFFICER to regional jurisdiction', () => {
    renderSidebarWithUser({
      id: 'regional-1',
      username: 'regional@aicte.gov.in',
      roles: ['ROLE_REGIONAL_OFFICER'],
      institutions: [],
    })

    expect(screen.getByText('Regional Directorate')).toBeInTheDocument()
    expect(screen.getByText('Regional Dashboard')).toBeInTheDocument()
    expect(screen.getByText('Regional Applications')).toBeInTheDocument()
    expect(screen.getByText('Documents & Compliance')).toBeInTheDocument()
    expect(screen.getByText('Required Actions')).toBeInTheDocument()
    expect(screen.getByText('Regulatory Alerts')).toBeInTheDocument()
    expect(screen.getByText('Regional Institutions')).toBeInTheDocument()
    expect(screen.getByText('EVC Scheduling')).toBeInTheDocument()

    // Strictly hidden: nationwide user management, system operations, and institutional tabs
    expect(screen.queryByText('My Institution')).not.toBeInTheDocument()
    expect(screen.queryByText('Compliance & Deadlines')).not.toBeInTheDocument()
    expect(screen.queryByText('Users & Roles')).not.toBeInTheDocument()
    expect(screen.queryByText('Platform Operations')).not.toBeInTheDocument()
  })

  it('restricts navigation for ROLE_AUDITOR to read-only oversight', () => {
    renderSidebarWithUser({
      id: 'auditor-1',
      username: 'auditor@aicte.gov.in',
      roles: ['ROLE_AUDITOR'],
      institutions: [],
    })

    expect(screen.getByText('Statutory Oversight')).toBeInTheDocument()
    expect(screen.getByText('Audit Dashboard')).toBeInTheDocument()
    expect(screen.getByText('Regulatory Audit Trail')).toBeInTheDocument()
    expect(screen.getByText('Applications Review')).toBeInTheDocument()
    expect(screen.getByText('Document Registry')).toBeInTheDocument()
    expect(screen.getByText('Compliance Surveillance')).toBeInTheDocument()
    expect(screen.getByText('Institutions Directory')).toBeInTheDocument()

    // Strictly hidden: EVC scheduling, user management, required actions, and My Institution
    expect(screen.queryByText('My Institution')).not.toBeInTheDocument()
    expect(screen.queryByText('Required Actions')).not.toBeInTheDocument()
    expect(screen.queryByText('EVC Scheduling')).not.toBeInTheDocument()
    expect(screen.queryByText('Users & Roles')).not.toBeInTheDocument()
    expect(screen.queryByText('Platform Operations')).not.toBeInTheDocument()
  })

  it('restricts navigation for ROLE_EVC_COORDINATOR to inspection operations', () => {
    renderSidebarWithUser({
      id: 'evc-1',
      username: 'evc@aicte.gov.in',
      roles: ['ROLE_EVC_COORDINATOR'],
      institutions: [],
    })

    expect(screen.getByText('Inspection Operations')).toBeInTheDocument()
    expect(screen.getByText('EVC Dashboard')).toBeInTheDocument()
    expect(screen.getByText('Panel Allocation')).toBeInTheDocument()
    expect(screen.getByText('Regulatory Alerts')).toBeInTheDocument()

    // Strictly hidden: applications, documents, compliance, nationwide institutions, user management, My Institution
    expect(screen.queryByText('My Institution')).not.toBeInTheDocument()
    expect(screen.queryByText('Applications & Scrutiny')).not.toBeInTheDocument()
    expect(screen.queryByText('Documents & Compliance')).not.toBeInTheDocument()
    expect(screen.queryByText('Required Actions')).not.toBeInTheDocument()
    expect(screen.queryByText('Compliance & Deadlines')).not.toBeInTheDocument()
    expect(screen.queryByText('Institutions Directory')).not.toBeInTheDocument()
    expect(screen.queryByText('Users & Roles')).not.toBeInTheDocument()
  })

  it('Navbar strictly filters navigation links by role (Admin vs Institution)', () => {
    // 1. Admin Navbar
    const { unmount } = render(
      <AuthContext.Provider
        value={{
          user: { id: 'admin-1', username: 'admin@aicte.gov.in', roles: ['ROLE_AICTE_ADMIN'], institutions: [] },
          token: 'mock-token',
          isAuthenticated: true,
          isLoading: false,
          isAdmin: true,
          hasRole: (r: string) => r === 'ROLE_AICTE_ADMIN',
          login: vi.fn(),
          logout: vi.fn(),
        }}
      >
        <MemoryRouter>
          <Navbar />
        </MemoryRouter>
      </AuthContext.Provider>
    )

    // Admin MUST NOT see My Institution
    expect(screen.queryByText('My Institution')).not.toBeInTheDocument()
    expect(screen.getByText('Audit Trail')).toBeInTheDocument()
    expect(screen.getByText('System Diagnostics')).toBeInTheDocument()
    unmount()

    // 2. Institution Navbar
    render(
      <AuthContext.Provider
        value={{
          user: { id: 'inst-1', username: 'inst@test.edu', roles: ['ROLE_INSTITUTION_OFFICER'], institutions: ['inst-id'] },
          token: 'mock-token',
          isAuthenticated: true,
          isLoading: false,
          isAdmin: false,
          hasRole: (r: string) => r === 'ROLE_INSTITUTION_OFFICER',
          login: vi.fn(),
          logout: vi.fn(),
        }}
      >
        <MemoryRouter>
          <Navbar />
        </MemoryRouter>
      </AuthContext.Provider>
    )

    // Institution MUST see My Institution, but NOT Admin modules
    expect(screen.getByText('My Institution')).toBeInTheDocument()
    expect(screen.queryByText('System Diagnostics')).not.toBeInTheDocument()
    expect(screen.queryByText('Audit Trail')).not.toBeInTheDocument()
    expect(screen.queryByText('Compliance')).not.toBeInTheDocument()
    expect(screen.queryByText('Institutions')).not.toBeInTheDocument()
  })
})

describe('Dedicated EvcCoordinatorDashboard', () => {
  it('renders statutory EVC metrics and action shortcuts', async () => {
    render(
      <QueryClientProvider client={queryClient}>
        <MemoryRouter>
          <EvcCoordinatorDashboard />
        </MemoryRouter>
      </QueryClientProvider>
    )

    expect(screen.getByText('Expert Visit Committee (EVC) Operations')).toBeInTheDocument()
    expect(screen.getByText('Pending Allocation')).toBeInTheDocument()
    expect(screen.getByText('Scheduled Visits')).toBeInTheDocument()
    expect(screen.getByText('Panels Formed')).toBeInTheDocument()
    expect(screen.getByText('Panel Quorum Deficit')).toBeInTheDocument()
    expect(screen.getByText('Statutory Panel Allocation')).toBeInTheDocument()
  })
})

describe('Brand Logo & Full Form Presentation', () => {
  it('renders official SAGE logo and System for Approval Governance and Evaluation in Navbar', () => {
    const mockAuthContext = {
      user: { id: 'admin-1', username: 'admin@aicte.gov.in', roles: ['ROLE_AICTE_ADMIN'], institutions: [] },
      token: 'mock-token',
      isAuthenticated: true,
      isLoading: false,
      login: vi.fn(),
      logout: vi.fn(),
      isAdmin: true,
      isInstitutionalSignatory: false,
      hasRole: vi.fn().mockReturnValue(true),
    }

    render(
      <AuthContext.Provider value={mockAuthContext as unknown as React.ComponentProps<typeof AuthContext.Provider>['value']}>
        <MemoryRouter>
          <Navbar />
        </MemoryRouter>
      </AuthContext.Provider>
    )

    const logo = screen.getByAltText('SAGE Logo')
    expect(logo).toBeInTheDocument()
    expect(logo).toHaveAttribute('src', '/sage-logo.png')
    expect(screen.getByText('System for Approval Governance and Evaluation')).toBeInTheDocument()
  })

  it('renders official SAGE logo and System for Approval Governance and Evaluation in Login page', () => {
    const mockAuthContext = {
      user: null,
      token: null,
      isAuthenticated: false,
      isLoading: false,
      login: vi.fn(),
      logout: vi.fn(),
      isAdmin: false,
      isInstitutionalSignatory: false,
      hasRole: vi.fn().mockReturnValue(false),
    }

    render(
      <AuthContext.Provider value={mockAuthContext as unknown as React.ComponentProps<typeof AuthContext.Provider>['value']}>
        <MemoryRouter>
          <Login />
        </MemoryRouter>
      </AuthContext.Provider>
    )

    const logo = screen.getByAltText('SAGE Logo')
    expect(logo).toBeInTheDocument()
    expect(logo).toHaveAttribute('src', '/sage-logo.png')
    expect(screen.getByText('System for Approval Governance and Evaluation')).toBeInTheDocument()
  })
})

