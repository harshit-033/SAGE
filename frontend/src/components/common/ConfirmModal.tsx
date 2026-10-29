import React from 'react'
import { Modal } from './Modal'
import { AlertTriangle, Info } from 'lucide-react'

export interface ConfirmModalProps {
  isOpen: boolean
  title: string
  message: string
  description?: string
  confirmText?: string
  cancelText?: string
  confirmVariant?: 'primary' | 'danger' | 'warning'
  isLoading?: boolean
  onConfirm: () => void
  onCancel: () => void
}

export const ConfirmModal: React.FC<ConfirmModalProps> = ({
  isOpen,
  title,
  message,
  description,
  confirmText = 'Confirm',
  cancelText = 'Cancel',
  confirmVariant = 'primary',
  isLoading = false,
  onConfirm,
  onCancel,
}) => {
  const getButtonClass = () => {
    switch (confirmVariant) {
      case 'danger':
        return 'btn btn-danger'
      case 'warning':
        return 'btn btn-secondary'
      case 'primary':
      default:
        return 'btn btn-primary'
    }
  }

  const getIcon = () => {
    switch (confirmVariant) {
      case 'danger':
        return <AlertTriangle size={24} color="#dc2626" />
      case 'warning':
        return <AlertTriangle size={24} color="#d97706" />
      case 'primary':
      default:
        return <Info size={24} color="var(--gov-blue)" />
    }
  }

  return (
    <Modal
      isOpen={isOpen}
      title={title}
      onClose={isLoading ? () => {} : onCancel}
      maxWidth="480px"
      footer={
        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', width: '100%' }}>
          <button
            type="button"
            className="btn btn-secondary"
            onClick={onCancel}
            disabled={isLoading}
            id="confirm-modal-cancel-btn"
          >
            {cancelText}
          </button>
          <button
            type="button"
            className={getButtonClass()}
            onClick={onConfirm}
            disabled={isLoading}
            id="confirm-modal-confirm-btn"
          >
            {isLoading ? 'Processing...' : confirmText}
          </button>
        </div>
      }
    >
      <div style={{ display: 'flex', gap: '1rem', alignItems: 'flex-start', padding: '0.5rem 0' }}>
        <div style={{ flexShrink: 0, marginTop: '0.2rem' }}>{getIcon()}</div>
        <div>
          <p style={{ fontSize: '0.95rem', fontWeight: 600, color: 'var(--text-primary)', marginBottom: description ? '0.5rem' : 0 }}>
            {message}
          </p>
          {description && (
            <p style={{ fontSize: '0.825rem', color: 'var(--text-secondary)', lineHeight: 1.45 }}>
              {description}
            </p>
          )}
        </div>
      </div>
    </Modal>
  )
}
