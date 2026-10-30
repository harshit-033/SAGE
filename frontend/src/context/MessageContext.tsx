import React, { useContext, useState, useCallback, useRef } from 'react'
import {
  MessageContext,
  type MessageType,
  type MessageOptions,
  type MessageContextType,
} from './messageContextDef'
import {
  sanitizeApiError,
  type SanitizedError,
} from '../utils/messageSanitizer'

export {
  MessageContext,
  type MessageType,
  type MessageOptions,
  type MessageContextType,
  type SanitizedError,
}

export const MessageProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [activeMessage, setActiveMessage] = useState<MessageOptions | null>(null)
  const confirmationResolverRef = useRef<((value: boolean) => void) | null>(null)

  const closeMessage = useCallback(() => {
    setActiveMessage(null)
    if (confirmationResolverRef.current) {
      confirmationResolverRef.current(false)
      confirmationResolverRef.current = null
    }
  }, [])

  const showMessage = useCallback((options: MessageOptions) => {
    // If a previous confirmation is pending, resolve it to false before replacing
    if (confirmationResolverRef.current) {
      confirmationResolverRef.current(false)
      confirmationResolverRef.current = null
    }
    setActiveMessage(options)
  }, [])

  const showSuccess = useCallback((title: string, message: string, description?: string) => {
    showMessage({
      type: 'SUCCESS',
      title,
      message,
      description,
    })
  }, [showMessage])

  const showError = useCallback((title: string, message: string, description?: string, correlationId?: string) => {
    showMessage({
      type: 'ERROR',
      title,
      message,
      description,
      correlationId,
    })
  }, [showMessage])

  const showWarning = useCallback((title: string, message: string, description?: string) => {
    showMessage({
      type: 'WARNING',
      title,
      message,
      description,
    })
  }, [showMessage])

  const showInfo = useCallback((title: string, message: string, description?: string) => {
    showMessage({
      type: 'INFORMATION',
      title,
      message,
      description,
    })
  }, [showMessage])

  const showConfirmation = useCallback((options: {
    title: string
    message: string
    description?: string
    confirmText?: string
    cancelText?: string
    confirmVariant?: 'primary' | 'danger' | 'warning'
  }): Promise<boolean> => {
    return new Promise((resolve) => {
      if (confirmationResolverRef.current) {
        confirmationResolverRef.current(false)
        confirmationResolverRef.current = null
      }
      confirmationResolverRef.current = resolve

      setActiveMessage({
        type: 'CONFIRMATION',
        title: options.title,
        message: options.message,
        description: options.description,
        confirmText: options.confirmText || 'Confirm',
        cancelText: options.cancelText || 'Cancel',
        confirmVariant: options.confirmVariant || 'primary',
        onConfirm: () => {
          setActiveMessage(null)
          if (confirmationResolverRef.current) {
            confirmationResolverRef.current(true)
            confirmationResolverRef.current = null
          }
        },
        onCancel: () => {
          setActiveMessage(null)
          if (confirmationResolverRef.current) {
            confirmationResolverRef.current(false)
            confirmationResolverRef.current = null
          }
        },
      })
    })
  }, [])

  const handleApiError = useCallback((error: unknown, fallbackTitle?: string, defaultMessage?: string) => {
    const sanitized = sanitizeApiError(error, fallbackTitle, defaultMessage)
    showError(sanitized.title, sanitized.message, sanitized.description, sanitized.correlationId)
  }, [showError])

  return (
    <MessageContext.Provider
      value={{
        activeMessage,
        showMessage,
        showSuccess,
        showError,
        showWarning,
        showInfo,
        showConfirmation,
        handleApiError,
        closeMessage,
      }}
    >
      {children}
    </MessageContext.Provider>
  )
}

// eslint-disable-next-line react-refresh/only-export-components
export const useMessage = (): MessageContextType => {
  const context = useContext(MessageContext)
  if (!context) {
    throw new Error('useMessage must be used within a MessageProvider')
  }
  return context
}
