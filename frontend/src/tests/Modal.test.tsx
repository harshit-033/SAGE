import { render, screen, fireEvent } from '@testing-library/react'
import { describe, it, expect, vi } from 'vitest'
import { Modal } from '../components/common/Modal'

describe('Modal Component', () => {
  it('renders modal dialog when isOpen is true', () => {
    const onClose = vi.fn()
    render(
      <Modal isOpen={true} title="Statutory Confirmation" onClose={onClose}>
        <p>Modal body content</p>
      </Modal>
    )

    expect(screen.getByRole('dialog')).toBeInTheDocument()
    expect(screen.getByText('Statutory Confirmation')).toBeInTheDocument()
    expect(screen.getByText('Modal body content')).toBeInTheDocument()

    const closeBtn = screen.getByRole('button', { name: /Close dialog/i })
    fireEvent.click(closeBtn)
    expect(onClose).toHaveBeenCalled()
  })

  it('closes on Escape key press', () => {
    const onClose = vi.fn()
    render(
      <Modal isOpen={true} title="Accessible Dialog" onClose={onClose}>
        <div>Content</div>
      </Modal>
    )

    fireEvent.keyDown(window, { key: 'Escape', code: 'Escape' })
    expect(onClose).toHaveBeenCalled()
  })

  it('does not render when isOpen is false', () => {
    const { container } = render(
      <Modal isOpen={false} title="Hidden Modal" onClose={vi.fn()}>
        <div>Hidden</div>
      </Modal>
    )
    expect(container.firstChild).toBeNull()
  })
})
