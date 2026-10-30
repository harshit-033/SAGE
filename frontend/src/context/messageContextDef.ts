import { createContext } from 'react'

export type MessageType = 'SUCCESS' | 'ERROR' | 'WARNING' | 'INFORMATION' | 'CONFIRMATION'

export interface MessageOptions {
  type: MessageType
  title: string
  message: string
  description?: string
  correlationId?: string
  confirmText?: string
  cancelText?: string
  confirmVariant?: 'primary' | 'danger' | 'warning'
  onConfirm?: () => void | Promise<void>
  onCancel?: () => void
  onOk?: () => void
}

export interface MessageContextType {
  activeMessage: MessageOptions | null
  showMessage: (options: MessageOptions) => void
  showSuccess: (title: string, message: string, description?: string) => void
  showError: (title: string, message: string, description?: string, correlationId?: string) => void
  showWarning: (title: string, message: string, description?: string) => void
  showInfo: (title: string, message: string, description?: string) => void
  showConfirmation: (options: {
    title: string
    message: string
    description?: string
    confirmText?: string
    cancelText?: string
    confirmVariant?: 'primary' | 'danger' | 'warning'
  }) => Promise<boolean>
  handleApiError: (error: unknown, fallbackTitle?: string, defaultMessage?: string) => void
  closeMessage: () => void
}

export const MessageContext = createContext<MessageContextType | undefined>(undefined)
