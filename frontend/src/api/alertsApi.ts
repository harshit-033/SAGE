import { apiClient } from './client'
import type { ApiResponseWrapper } from './authApi'
import type { PageResponse } from './applicationsApi'

export type AlertCategory = 'DEADLINE' | 'COMPLIANCE' | 'DOCUMENT' | 'WORKFLOW' | 'SYSTEM'
export type AlertSeverity = 'CRITICAL' | 'WARNING' | 'INFO'
export type AlertPriority = 'URGENT' | 'HIGH' | 'MEDIUM' | 'LOW'
export type AlertStatus = 'UNREAD' | 'READ' | 'ACKNOWLEDGED' | 'RESOLVED' | 'DISMISSED'
export type DeliveryChannel = 'IN_APP' | 'EMAIL' | 'SMS' | 'WHATSAPP' | 'WEBHOOK'
export type DeliveryStatus = 'PENDING' | 'SENT' | 'DELIVERED' | 'FAILED' | 'RETRY_SCHEDULED'

export interface AlertDeliveryDto {
  id: string
  channel: DeliveryChannel
  recipientAddress: string
  status: DeliveryStatus
  attemptCount: number
  lastAttemptAt?: string
  deliveredAt?: string
  errorMessage?: string
}

export interface AlertDto {
  id: string
  alertCode: string
  title: string
  message: string
  category: AlertCategory
  severity: AlertSeverity
  priority: AlertPriority
  sourceEntityType: string
  sourceEntityId: string
  institutionId: string
  institutionName?: string
  recipientUserId?: string
  recipientName?: string
  targetRole?: string
  jurisdictionState?: string
  status: AlertStatus
  isEscalated: boolean
  escalatedToRole?: string
  escalatedAt?: string
  acknowledgedAt?: string
  acknowledgedByName?: string
  acknowledgementNotes?: string
  resolvedAt?: string
  resolvedByName?: string
  resolutionNotes?: string
  deduplicationHash: string
  deliveries: AlertDeliveryDto[]
  createdAt: string
  updatedAt: string
}

export interface AcknowledgeAlertRequest {
  acknowledgementNotes: string
}

export interface ResolveAlertRequest {
  resolutionNotes: string
}

export const alertsApi = {
  list: async (params?: {
    institutionId?: string
    status?: AlertStatus
    severity?: AlertSeverity
    page?: number
    size?: number
    sortBy?: string
    sortDir?: string
  }): Promise<PageResponse<AlertDto>> => {
    const query = new URLSearchParams()
    if (params?.institutionId) query.set('institutionId', params.institutionId)
    if (params?.status) query.set('status', params.status)
    if (params?.severity) query.set('severity', params.severity)
    if (params?.page !== undefined) query.set('page', params.page.toString())
    if (params?.size !== undefined) query.set('size', params.size.toString())
    if (params?.sortBy) query.set('sortBy', params.sortBy)
    if (params?.sortDir) query.set('sortDir', params.sortDir)

    const res = await apiClient<ApiResponseWrapper<PageResponse<AlertDto>>>(
      `/api/v1/alerts?${query.toString()}`,
      { method: 'GET' }
    )
    return res.data
  },

  get: async (id: string): Promise<AlertDto> => {
    const res = await apiClient<ApiResponseWrapper<AlertDto>>(
      `/api/v1/alerts/${id}`,
      { method: 'GET' }
    )
    return res.data
  },

  acknowledge: async (id: string, data: AcknowledgeAlertRequest): Promise<AlertDto> => {
    const res = await apiClient<ApiResponseWrapper<AlertDto>>(
      `/api/v1/alerts/${id}/acknowledge`,
      {
        method: 'POST',
        body: JSON.stringify(data),
      }
    )
    return res.data
  },

  resolve: async (id: string, data: ResolveAlertRequest): Promise<AlertDto> => {
    const res = await apiClient<ApiResponseWrapper<AlertDto>>(
      `/api/v1/alerts/${id}/resolve`,
      {
        method: 'POST',
        body: JSON.stringify(data),
      }
    )
    return res.data
  },

  escalate: async (id: string, escalateToRole = 'ROLE_REGIONAL_OFFICER'): Promise<AlertDto> => {
    const res = await apiClient<ApiResponseWrapper<AlertDto>>(
      `/api/v1/alerts/${id}/escalate?escalateToRole=${encodeURIComponent(escalateToRole)}`,
      {
        method: 'POST',
      }
    )
    return res.data
  },
}
