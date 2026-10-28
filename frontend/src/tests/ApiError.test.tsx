import { describe, it, expect } from 'vitest'
import { ApiError, type ProblemDetails } from '../api/client'

describe('ApiError and ProblemDetails Model', () => {
  it('correctly constructs ApiError from RFC 7807 problem details', () => {
    const problem: ProblemDetails = {
      title: 'Tenant Access Denied',
      status: 403,
      detail: 'Principal is not authorized to access foreign institution',
      instance: '/api/v1/institutions/1234',
      correlationId: 'c-test-999',
    }

    const error = new ApiError(problem)
    expect(error.name).toBe('ApiError')
    expect(error.message).toBe('Principal is not authorized to access foreign institution')
    expect(error.problem.status).toBe(403)
    expect(error.problem.correlationId).toBe('c-test-999')
  })
})
