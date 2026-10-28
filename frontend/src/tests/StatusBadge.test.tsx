import { render, screen } from '@testing-library/react'
import { describe, it, expect } from 'vitest'
import { StatusBadge } from '../components/common/StatusBadge'

describe('StatusBadge Component', () => {
  it('renders Active badge correctly', () => {
    render(<StatusBadge status="ACTIVE" />)
    expect(screen.getByText('Active')).toBeInTheDocument()
  })

  it('renders Operational for UP status', () => {
    render(<StatusBadge status="UP" />)
    expect(screen.getByText('Operational')).toBeInTheDocument()
  })

  it('renders Pending Review for PENDING_APPROVAL', () => {
    render(<StatusBadge status="PENDING_APPROVAL" />)
    expect(screen.getByText('Pending Review')).toBeInTheDocument()
  })

  it('renders Offline for DOWN status', () => {
    render(<StatusBadge status="DOWN" />)
    expect(screen.getByText('Offline')).toBeInTheDocument()
  })
})
