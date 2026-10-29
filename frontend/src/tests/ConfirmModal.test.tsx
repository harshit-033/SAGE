import { render, screen, fireEvent } from '@testing-library/react'
import { describe, it, expect, vi } from 'vitest'
import { ConfirmModal } from '../components/common/ConfirmModal'

describe('ConfirmModal Component (Government Dialog System)', () => {
  it('renders confirmation dialog when isOpen is true', () => {
    const onConfirm = vi.fn()
    const onCancel = vi.fn()

    render(
      <ConfirmModal
        isOpen={true}
        title="Supervisory Alert Escalation"
        message="Escalate alert to Regional HQ supervisory queue?"
        description="This will elevate priority and notify Regional Oversight Officers."
        confirmText="Escalate to Regional HQ"
        cancelText="Cancel"
        confirmVariant="warning"
        onConfirm={onConfirm}
        onCancel={onCancel}
      />
    )

    expect(screen.getByRole('dialog')).toBeInTheDocument()
    expect(screen.getByText('Supervisory Alert Escalation')).toBeInTheDocument()
    expect(screen.getByText('Escalate alert to Regional HQ supervisory queue?')).toBeInTheDocument()
    expect(screen.getByText('This will elevate priority and notify Regional Oversight Officers.')).toBeInTheDocument()

    const confirmBtn = screen.getByRole('button', { name: /Escalate to Regional HQ/i })
    expect(confirmBtn).toBeInTheDocument()
    fireEvent.click(confirmBtn)
    expect(onConfirm).toHaveBeenCalledTimes(1)

    const cancelBtn = screen.getByRole('button', { name: /Cancel/i })
    expect(cancelBtn).toBeInTheDocument()
    fireEvent.click(cancelBtn)
    expect(onCancel).toHaveBeenCalledTimes(1)
  })

  it('renders destructive danger styling when confirmVariant is danger', () => {
    render(
      <ConfirmModal
        isOpen={true}
        title="Confirm Officer Disaffiliation"
        message="Are you sure you want to disaffiliate this officer?"
        confirmText="Disaffiliate Officer"
        confirmVariant="danger"
        onConfirm={vi.fn()}
        onCancel={vi.fn()}
      />
    )

    const confirmBtn = screen.getByRole('button', { name: /Disaffiliate Officer/i })
    expect(confirmBtn.className).toContain('btn-danger')
  })

  it('disables buttons and shows processing text when isLoading is true', () => {
    const onConfirm = vi.fn()
    const onCancel = vi.fn()

    render(
      <ConfirmModal
        isOpen={true}
        title="Processing Dialog"
        message="Executing statutory action..."
        isLoading={true}
        onConfirm={onConfirm}
        onCancel={onCancel}
      />
    )

    const confirmBtn = screen.getByRole('button', { name: /Processing.../i })
    expect(confirmBtn).toBeDisabled()

    const cancelBtn = screen.getByRole('button', { name: /Cancel/i })
    expect(cancelBtn).toBeDisabled()
  })

  it('does not render dialog when isOpen is false', () => {
    const { container } = render(
      <ConfirmModal
        isOpen={false}
        title="Hidden Dialog"
        message="Should not be visible"
        onConfirm={vi.fn()}
        onCancel={vi.fn()}
      />
    )

    expect(container).toBeEmptyDOMElement()
  })
})
