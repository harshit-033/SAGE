import React, { useEffect, useRef } from 'react'

interface ModalProps {
  isOpen: boolean
  title: string
  onClose: () => void
  children: React.ReactNode
  footer?: React.ReactNode
  maxWidth?: string
}

export const Modal: React.FC<ModalProps> = ({
  isOpen,
  title,
  onClose,
  children,
  footer,
  maxWidth = '600px',
}) => {
  const dialogRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape' && isOpen) {
        onClose()
      }
    }
    if (isOpen) {
      window.addEventListener('keydown', handleKeyDown)
      document.body.style.overflow = 'hidden'
    }
    return () => {
      window.removeEventListener('keydown', handleKeyDown)
      document.body.style.overflow = 'unset'
    }
  }, [isOpen, onClose])

  if (!isOpen) {
    return null
  }

  return (
    <div
      className="modal-backdrop"
      role="presentation"
      onClick={(e) => {
        if (e.target === e.currentTarget) {
          onClose()
        }
      }}
    >
      <div
        className="modal-dialog"
        style={{ maxWidth }}
        role="dialog"
        aria-modal="true"
        aria-labelledby="modal-title-heading"
        ref={dialogRef}
      >
        <div className="modal-header">
          <h2 id="modal-title-heading" className="modal-title">
            {title}
          </h2>
          <button
            type="button"
            className="btn btn-secondary btn-sm"
            onClick={onClose}
            aria-label="Close dialog"
            style={{ padding: '0.2rem 0.5rem', border: 'none', fontSize: '1.25rem', lineHeight: 1 }}
          >
            &times;
          </button>
        </div>
        <div className="modal-body">{children}</div>
        {footer && <div className="modal-footer">{footer}</div>}
      </div>
    </div>
  )
}
