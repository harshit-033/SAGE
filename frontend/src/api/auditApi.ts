import { apiClient } from './client'
import type { ApiResponseWrapper } from './authApi'
import type { PageResponse } from './institutionsApi'

export interface AuditEvent {
  id: string
  eventType: string
  principal: string
  institutionId?: string
  resourceType: string
  resourceId: string
  action: string
  clientIp?: string
  correlationId?: string
  details?: string
  createdAt: string
}

export interface AuditSearchParams {
  page?: number
  size?: number
  principal?: string
  eventType?: string
  resourceType?: string
  correlationId?: string
  sortBy?: string
  sortDir?: string
}

export const auditApi = {
  listAuditEvents: async (params: AuditSearchParams = {}): Promise<PageResponse<AuditEvent>> => {
    const query = new URLSearchParams()
    if (params.page !== undefined) query.set('page', params.page.toString())
    if (params.size !== undefined) query.set('size', params.size.toString())
    if (params.principal) query.set('principal', params.principal)
    if (params.eventType) query.set('eventType', params.eventType)
    if (params.resourceType) query.set('resourceType', params.resourceType)
    if (params.correlationId) query.set('correlationId', params.correlationId)
    if (params.sortBy) query.set('sortBy', params.sortBy)
    if (params.sortDir) query.set('sortDir', params.sortDir)

    const url = `/api/v1/audit${query.toString() ? `?${query.toString()}` : ''}`
    const res = await apiClient<ApiResponseWrapper<PageResponse<AuditEvent>>>(url, {
      method: 'GET',
    })
    return res.data
  },
}
