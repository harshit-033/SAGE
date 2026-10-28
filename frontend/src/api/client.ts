export interface ProblemDetails {
  type?: string
  title: string
  status: number
  detail: string
  instance?: string
  correlationId?: string
  timestamp?: string
  fieldErrors?: Array<{ field: string; message: string; rejectedValue?: unknown }>
}

export class ApiError extends Error {
  public problem: ProblemDetails

  constructor(problem: ProblemDetails) {
    super(problem.detail || problem.title || 'An API error occurred')
    this.name = 'ApiError'
    this.problem = problem
  }
}

function generateCorrelationId(): string {
  if (typeof crypto !== 'undefined' && crypto.randomUUID) {
    return crypto.randomUUID()
  }
  return 'c-' + Math.random().toString(36).substring(2, 11) + '-' + Date.now()
}

export async function apiClient<T>(
  endpoint: string,
  options: RequestInit = {}
): Promise<T> {
  const token = localStorage.getItem('sage_token')
  const correlationId = generateCorrelationId()

  const headers = new Headers(options.headers || {})
  headers.set('X-Correlation-ID', correlationId)
  if (!headers.has('Content-Type') && !(options.body instanceof FormData)) {
    headers.set('Content-Type', 'application/json')
  }
  if (token && !headers.has('Authorization')) {
    headers.set('Authorization', `Bearer ${token}`)
  }

  // Base URL resolution
  let url = endpoint
  if (!endpoint.startsWith('http')) {
    const baseUrl = import.meta.env.VITE_API_BASE_URL || ''
    url = `${baseUrl}${endpoint}`
  }

  const response = await fetch(url, {
    ...options,
    headers,
  })

  if (!response.ok) {
    let errorProblem: ProblemDetails
    try {
      errorProblem = await response.json()
    } catch {
      errorProblem = {
        title: response.statusText || 'HTTP Error',
        status: response.status,
        detail: `Request failed with status code ${response.status}`,
        correlationId,
      }
    }
    throw new ApiError(errorProblem)
  }

  // If 204 No Content
  if (response.status === 204) {
    return {} as T
  }

  return response.json()
}
