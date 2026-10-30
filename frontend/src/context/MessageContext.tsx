import React, { createContext, useContext, useState, useCallback, useRef } from 'react'
import { ApiError } from '../api/client'

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

export interface SanitizedError {
  title: string
  message: string
  description?: string
  correlationId?: string
}

/**
 * Maps raw backend/network errors to clear, dignified institutional language
 * without leaking database tables, stack traces, framework names, or internal services.
 */
export function sanitizeApiError(
  error: unknown,
  fallbackTitle = 'Request Processing Notice',
  fallbackMessage = 'We could not complete your request. Please try again.'
): SanitizedError {
  if (error instanceof ApiError && error.problem) {
    const { status, title, detail, correlationId } = error.problem

    // Dedicated status-code institutional phrasing
    if (status === 401) {
      return {
        title: 'Session Expired',
        message: 'Your regulatory session has expired. Please sign in again to continue.',
        correlationId,
      }
    }

    if (status === 403) {
      return {
        title: 'Authorization Required',
        message: 'You do not have permission to perform this regulatory action.',
        description: 'If you believe this is an error, please contact your Regional AICTE Directorate.',
        correlationId,
      }
    }

    if (status === 404) {
      return {
        title: 'Record Not Found',
        message: 'The requested institutional record or document could not be found.',
        correlationId,
      }
    }

    if (status === 409) {
      return {
        title: 'Regulatory Record Conflict',
        message: sanitizeTechnicalMessage(detail || title || 'The submitted details conflict with an existing record.'),
        correlationId,
      }
    }

    if (status === 422 || status === 400) {
      return {
        title: 'Submission Requirements Incomplete',
        message: sanitizeTechnicalMessage(detail || title || 'Please ensure all statutory requirements are satisfied before proceeding.'),
        correlationId,
      }
    }

    if (status >= 500) {
      return {
        title: 'Service Temporarily Unavailable',
        message: 'Something went wrong while processing your request. The service is temporarily unavailable.',
        description: 'Please try again in a few moments. If the issue persists, quote the reference ID below.',
        correlationId,
      }
    }

    return {
      title: sanitizeTechnicalMessage(title || fallbackTitle),
      message: sanitizeTechnicalMessage(detail || fallbackMessage),
      correlationId,
    }
  }

  if (error instanceof Error) {
    const rawMsg = error.message || ''
    if (rawMsg.includes('Failed to fetch') || rawMsg.includes('NetworkError') || rawMsg.includes('Load failed')) {
      return {
        title: 'Connection Unavailable',
        message: 'The system is temporarily unable to connect to the central registry. Please verify your internet connection.',
      }
    }
    return {
      title: fallbackTitle,
      message: sanitizeTechnicalMessage(rawMsg || fallbackMessage),
    }
  }

  return {
    title: fallbackTitle,
    message: fallbackMessage,
  }
}

/**
 * Cleanses technical jargon from user-facing text
 */
export function sanitizeTechnicalMessage(input: string): string {
  if (!input) return ''

  let text = input

  // Specific phrases
  const replacements: Array<[RegExp, string]> = [
    [/backend service unavailable/gi, 'The service is temporarily unavailable. Please try again later.'],
    [/api request failed/gi, 'We could not complete your request. Please try again.'],
    [/internal server error/gi, 'Something went wrong while processing your request.'],
    [/database connection error/gi, 'The system is temporarily unable to retrieve this information.'],
    [/connection refused/gi, 'The central regulatory service is currently unreachable. Please try again.'],
    [/ocr processing failed/gi, 'We could not extract information from this document. Please upload a clearer copy or enter details manually.'],
    [/ocr service (processing|unavailable|error)/gi, 'Automated document extraction service'],
    [/indra ocr/gi, 'Automated Document Verification'],
    [/tesseract/gi, 'Document Analysis Subsystem'],
    [/spring boot|fastapi|vite|react/gi, 'regulatory system'],
    [/postgresql|flyway|sql exception/gi, 'institutional record repository'],
    [/magic bytes/gi, 'statutory format standards'],
    [/sha-256|sha256/gi, 'Digital Verification Hash'],
    [/403 forbidden/gi, 'You do not have permission to perform this action.'],
    [/404 not found/gi, 'The requested record could not be found.'],
  ]

  for (const [regex, rep] of replacements) {
    text = text.replace(regex, rep)
  }

  // If text contains Java/Python exception trace fragments, mask them
  if (text.includes('Exception:') || text.includes('Error at ') || text.includes('org.aicte.')) {
    return 'An internal processing notice was generated while handling your request. Please contact support.'
  }

  return text
}

interface MessageContextType {
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
      title: sanitizeTechnicalMessage(title),
      message: sanitizeTechnicalMessage(message),
      description: description ? sanitizeTechnicalMessage(description) : undefined,
    })
  }, [showMessage])

  const showError = useCallback((title: string, message: string, description?: string, correlationId?: string) => {
    showMessage({
      type: 'ERROR',
      title: sanitizeTechnicalMessage(title),
      message: sanitizeTechnicalMessage(message),
      description: description ? sanitizeTechnicalMessage(description) : undefined,
      correlationId,
    })
  }, [showMessage])

  const showWarning = useCallback((title: string, message: string, description?: string) => {
    showMessage({
      type: 'WARNING',
      title: sanitizeTechnicalMessage(title),
      message: sanitizeTechnicalMessage(message),
      description: description ? sanitizeTechnicalMessage(description) : undefined,
    })
  }, [showMessage])

  const showInfo = useCallback((title: string, message: string, description?: string) => {
    showMessage({
      type: 'INFORMATION',
      title: sanitizeTechnicalMessage(title),
      message: sanitizeTechnicalMessage(message),
      description: description ? sanitizeTechnicalMessage(description) : undefined,
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
        title: sanitizeTechnicalMessage(options.title),
        message: sanitizeTechnicalMessage(options.message),
        description: options.description ? sanitizeTechnicalMessage(options.description) : undefined,
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
