import { render, screen, fireEvent } from '@testing-library/react'
import { describe, it, expect } from 'vitest'
import { TopGovBar } from '../components/common/TopGovBar'

describe('TopGovBar Accessibility Component', () => {
  it('renders government portal branding and prototype badge', () => {
    render(<TopGovBar />)
    expect(screen.getByText('GOVERNMENT OF INDIA')).toBeInTheDocument()
    expect(screen.getByText('ALL INDIA COUNCIL FOR TECHNICAL EDUCATION')).toBeInTheDocument()
    expect(screen.getByText('SIH 2026 PROTOTYPE')).toBeInTheDocument()
  })

  it('toggles font size scaling attributes on documentElement', () => {
    render(<TopGovBar />)
    const largeBtn = screen.getByRole('button', { name: /Increase text size/i })
    fireEvent.click(largeBtn)
    expect(document.documentElement.dataset.fontSize).toBe('large')

    const smallBtn = screen.getByRole('button', { name: /Decrease text size/i })
    fireEvent.click(smallBtn)
    expect(document.documentElement.dataset.fontSize).toBe('small')
  })

  it('toggles high contrast mode on documentElement', () => {
    render(<TopGovBar />)
    const contrastBtn = screen.getByRole('button', { name: /Toggle high contrast/i })
    fireEvent.click(contrastBtn)
    expect(document.documentElement.dataset.contrast).toBe('high')

    fireEvent.click(contrastBtn)
    expect(document.documentElement.dataset.contrast).toBeUndefined()
  })
})
