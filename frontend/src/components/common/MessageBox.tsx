import React, { useEffect, useRef } from 'react'
import { useMessage } from '../../context/MessageContext'
import { CheckCircle2, AlertOctagon, AlertTriangle, Info, HelpCircle } from 'lucide-react'

export const MessageBox: React.FC = () => {
  const { activeMessage, closeMessage } = useMessage()
  const primaryButtonRef = useRef<HTMLButtonElement>(null)

  useEffect(() => {
    if (activeMessage) {
      // Focus primary button when dialog opens for accessibility
      const timer = setTimeout(() => {
        primaryButtonRef.current?.focus()
      }, 50)

      const handleKeyDown = (e: KeyboardEvent) => {
        if (e.key === 'Escape') {
          if (activeMessage.type === 'CONFIRMATION') {
            activeMessage.onCancel ? activeMessage.onCancel() : closeMessage()
          } else {
            activeMessage.onOk ? activeMessage.onOk() : closeMessage()
          }
        }
      }

      window.addEventListener('keydown', handleKeyDown)
      document.body.style.overflow = 'hidden'

      return () => {
        clearTimeout(timer)
        window.removeEventListener('keydown', handleKeyDown)
        document.body.style.overflow = 'unset'
      }
    }
  }, [activeMessage, closeMessage])

  if (!activeMessage) {
    return null
  }

  const {
    type,
    title,
    message,
    description,
    correlationId,
    confirmText = 'Confirm',
    cancelText = 'Cancel',
    confirmVariant = 'primary',
    onConfirm,
    onCancel,
    onOk,
  } = activeMessage

  const getIconAndColors = () => {
    switch (type) {
      case 'SUCCESS':
        return {
          icon: <CheckCircle2 size={28} color="#16a34a" />,
          bgColor: '#f0fdf4',
          borderColor: '#86efac',
          titleColor: '#166534',
        }
      case 'ERROR':
        return {
          icon: <AlertOctagon size={28} color="#dc2626" />,
          bgColor: '#fef2f2',
          borderColor: '#fca5a5',
          titleColor: '#991b1b',
        }
      case 'WARNING':
        return {
          icon: <AlertTriangle size={28} color="#d97706" />,
          bgColor: '#fefce8',
          borderColor: '#fde047',
          titleColor: '#854d0e',
        }
      case 'CONFIRMATION':
        if (confirmVariant === 'danger') {
          return {
            icon: <AlertTriangle size={28} color="#dc2626" />,
            bgColor: '#fef2f2',
            borderColor: '#fca5a5',
            titleColor: '#991b1b',
          }
        }
        return {
          icon: <HelpCircle size={28} color="#005aaa" />,
          bgColor: '#eff6ff',
          borderColor: '#bfdbfe',
          titleColor: '#1e40af',
        }
      case 'INFORMATION':
      default:
        return {
          icon: <Info size={28} color="#005aaa" />,
          bgColor: '#eff6ff',
          borderColor: '#bfdbfe',
          titleColor: '#1e40af',
        }
    }
  }

  const { icon, bgColor, borderColor, titleColor } = getIconAndColors()

  const handleConfirm = () => {
    if (onConfirm) {
      onConfirm()
    } else {
      closeMessage()
    }
  }

  const handleCancel = () => {
    if (onCancel) {
      onCancel()
    } else {
      closeMessage()
    }
  }

  const handleOk = () => {
    if (onOk) {
      onOk()
    } else {
      closeMessage()
    }
  }

  return (
    <div
      className="modal-backdrop"
      role="presentation"
      onClick={(e) => {
        if (e.target === e.currentTarget) {
          if (type === 'CONFIRMATION') {
            handleCancel()
          } else {
            handleOk()
          }
        }
      }}
      style={{ zIndex: 9999 }}
    >
      <div
        className="modal-dialog"
        role="alertdialog"
        aria-modal="true"
        aria-labelledby="msgbox-title"
        aria-describedby="msgbox-desc"
        style={{
          maxWidth: '520px',
          boxShadow: '0 20px 25px -5px rgba(0, 0, 0, 0.2), 0 10px 10px -5px rgba(0, 0, 0, 0.08)',
          borderRadius: 'var(--radius-md)',
          overflow: 'hidden',
        }}
      >
        {/* Color accent header strip */}
        <div
          style={{
            height: '4px',
            backgroundColor:
              type === 'SUCCESS'
                ? '#16a34a'
                : type === 'ERROR'
                ? '#dc2626'
                : type === 'WARNING'
                ? '#d97706'
                : 'var(--gov-blue)',
          }}
        />

        <div className="modal-header" style={{ padding: '1rem 1.25rem', backgroundColor: 'var(--bg-surface)' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
            <div
              style={{
                width: '38px',
                height: '38px',
                borderRadius: '50%',
                backgroundColor: bgColor,
                border: `1px solid ${borderColor}`,
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                flexShrink: 0,
              }}
            >
              {icon}
            </div>
            <h2 id="msgbox-title" className="modal-title" style={{ color: titleColor, fontSize: '1.1rem', margin: 0 }}>
              {title}
            </h2>
          </div>
          <button
            type="button"
            className="btn btn-secondary btn-sm"
            onClick={type === 'CONFIRMATION' ? handleCancel : handleOk}
            aria-label="Close message"
            style={{ padding: '0.2rem 0.5rem', border: 'none', fontSize: '1.25rem', lineHeight: 1 }}
          >
            &times;
          </button>
        </div>

        <div className="modal-body" style={{ padding: '1.25rem' }}>
          <p id="msgbox-desc" style={{ fontSize: '0.95rem', color: 'var(--text-primary)', margin: 0, lineHeight: 1.5, fontWeight: 500 }}>
            {message}
          </p>

          {description && (
            <p style={{ marginTop: '0.75rem', fontSize: '0.85rem', color: 'var(--text-secondary)', lineHeight: 1.5, marginBottom: 0 }}>
              {description}
            </p>
          )}

          {correlationId && (
            <div
              style={{
                marginTop: '1rem',
                padding: '0.4rem 0.65rem',
                backgroundColor: 'var(--bg-surface-secondary)',
                borderRadius: 'var(--radius-sm)',
                border: '1px solid var(--border-light)',
                display: 'inline-flex',
                alignItems: 'center',
                gap: '0.4rem',
                fontSize: '0.75rem',
                color: 'var(--text-muted)',
                fontFamily: 'var(--font-mono)',
              }}
            >
              <span>Reference ID:</span>
              <strong style={{ color: 'var(--text-secondary)' }}>{correlationId}</strong>
            </div>
          )}
        </div>

        <div className="modal-footer" style={{ padding: '0.85rem 1.25rem' }}>
          {type === 'CONFIRMATION' ? (
            <>
              <button
                type="button"
                className="btn btn-secondary"
                onClick={handleCancel}
                id="msgbox-cancel-btn"
              >
                {cancelText}
              </button>
              <button
                ref={primaryButtonRef}
                type="button"
                className={confirmVariant === 'danger' ? 'btn btn-danger' : confirmVariant === 'warning' ? 'btn btn-secondary' : 'btn btn-primary'}
                onClick={handleConfirm}
                id="msgbox-confirm-btn"
              >
                {confirmText}
              </button>
            </>
          ) : (
            <button
              ref={primaryButtonRef}
              type="button"
              className="btn btn-primary"
              onClick={handleOk}
              id="msgbox-ok-btn"
            >
              Acknowledge
            </button>
          )}
        </div>
      </div>
    </div>
  )
}
