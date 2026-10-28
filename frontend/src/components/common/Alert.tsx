import React from 'react'

interface AlertProps {
  type?: 'info' | 'success' | 'warning' | 'danger'
  title?: string
  children: React.ReactNode
  onClose?: () => void
}

export const Alert: React.FC<AlertProps> = ({
  type = 'info',
  title,
  children,
  onClose,
}) => {
  return (
    <div className={`gov-alert gov-alert-${type}`} role="alert">
      <div style={{ flex: 1 }}>
        {title && <div style={{ fontWeight: 700, marginBottom: '0.2rem' }}>{title}</div>}
        <div>{children}</div>
      </div>
      {onClose && (
        <button
          type="button"
          onClick={onClose}
          aria-label="Dismiss alert"
          style={{
            background: 'transparent',
            border: 'none',
            cursor: 'pointer',
            fontSize: '1.1rem',
            lineHeight: 1,
            color: 'inherit',
          }}
        >
          &times;
        </button>
      )}
    </div>
  )
}
