import { render, screen } from '@testing-library/react'
import { describe, it, expect } from 'vitest'
import { StatusBadge } from '../components/common/StatusBadge'

// Test status badge mapping for Phase 3 application statuses
describe('Phase 3 Application Statuses in StatusBadge', () => {
  it('renders PENDING_SCRUTINY as Pending Review', () => {
    render(<StatusBadge status="PENDING_SCRUTINY" />)
    expect(screen.getByText('Pending Review')).toBeInTheDocument()
  })

  it('renders DEFICIENT correctly', () => {
    render(<StatusBadge status="DEFICIENT" />)
    expect(screen.getByText('Deficient')).toBeInTheDocument()
  })

  it('renders APPROVED correctly', () => {
    render(<StatusBadge status="APPROVED" />)
    expect(screen.getByText('Active')).toBeInTheDocument()
  })
})
