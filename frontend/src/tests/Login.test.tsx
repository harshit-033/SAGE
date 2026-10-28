import { render, screen, fireEvent } from '@testing-library/react'
import { describe, it, expect } from 'vitest'
import { BrowserRouter } from 'react-router-dom'
import { AuthProvider } from '../context/AuthContext'
import { Login } from '../pages/Login'

describe('Login Screen', () => {
  it('renders login form and synthetic demo accounts', () => {
    render(
      <AuthProvider>
        <BrowserRouter>
          <Login />
        </BrowserRouter>
      </AuthProvider>
    )

    expect(screen.getByText(/SAGE Portal Login/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/Official Email or Username/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/Security Password/i)).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /Sign In to Portal/i })).toBeInTheDocument()

    // Demo account presets
    expect(screen.getByText(/Central Admin/i)).toBeInTheDocument()
    expect(screen.getByText(/Institution Officer/i)).toBeInTheDocument()
  })

  it('populates fields when a preset button is clicked', () => {
    render(
      <AuthProvider>
        <BrowserRouter>
          <Login />
        </BrowserRouter>
      </AuthProvider>
    )

    const adminPresetBtn = screen.getByText(/Central Admin/i)
    fireEvent.click(adminPresetBtn)

    const usernameInput = screen.getByLabelText(/Official Email or Username/i) as HTMLInputElement
    const passwordInput = screen.getByLabelText(/Security Password/i) as HTMLInputElement

    expect(usernameInput.value).toBe('admin@aicte.gov.in')
    expect(passwordInput.value).toBe('Password123!')
  })
})
