import { render, screen } from '@testing-library/react'
import { describe, it, expect } from 'vitest'
import { MemoryRouter } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { AuthProvider } from '../context/AuthContext'
import { AppContent } from '../App'

const queryClient = new QueryClient()

describe('Navigation and Protected Routing', () => {
  it('redirects unauthenticated user from protected route to login', async () => {
    render(
      <QueryClientProvider client={queryClient}>
        <AuthProvider>
          <MemoryRouter initialEntries={['/']}>
            <AppContent />
          </MemoryRouter>
        </AuthProvider>
      </QueryClientProvider>
    )

    // Should redirect to Login page
    expect(await screen.findByText('SAGE AICTE Portal')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /Authenticate & Enter/i })).toBeInTheDocument()
  })
})
