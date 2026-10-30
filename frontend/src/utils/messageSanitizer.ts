import { ApiError } from '../api/client'

export interface SanitizedError {
  title: string
  message: string
  description?: string
  correlationId?: string
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
