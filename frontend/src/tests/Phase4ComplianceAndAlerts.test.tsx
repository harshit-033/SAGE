import { render, screen } from '@testing-library/react'
import { describe, it, expect } from 'vitest'
import { MemoryRouter } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { AuthProvider } from '../context/AuthContext'
import { AppContent } from '../App'
import { StatusBadge } from '../components/common/StatusBadge'

const queryClient = new QueryClient()

describe('Phase 4: Regulatory Deadlines and Compliance Route Security', () => {
  it('redirects unauthenticated user accessing /compliance to login', async () => {
    render(
      <QueryClientProvider client={queryClient}>
        <AuthProvider>
          <MemoryRouter initialEntries={['/compliance']}>
            <AppContent />
          </MemoryRouter>
        </AuthProvider>
      </QueryClientProvider>
    )

    expect(await screen.findByText(/SAGE Portal Login/i)).toBeInTheDocument()
  })

  it('redirects unauthenticated user accessing /alerts to login', async () => {
    render(
      <QueryClientProvider client={queryClient}>
        <AuthProvider>
          <MemoryRouter initialEntries={['/alerts']}>
            <AppContent />
          </MemoryRouter>
        </AuthProvider>
      </QueryClientProvider>
    )

    expect(await screen.findByText(/SAGE Portal Login/i)).toBeInTheDocument()
  })

  it('renders regulatory deadline statuses correctly in StatusBadge', () => {
    render(<StatusBadge status="ACTIVE" />)
    expect(screen.getByText('Active')).toBeInTheDocument()

    render(<StatusBadge status="PENDING" />)
    expect(screen.getByText('Pending')).toBeInTheDocument()
  })
})
