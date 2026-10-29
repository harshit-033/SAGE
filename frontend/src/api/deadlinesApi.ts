import { apiClient } from './client'
import type { ApiResponseWrapper } from './authApi'
import type { PageResponse } from './applicationsApi'

export type DeadlineType = 'STATUTORY' | 'OFFICIALLY_NOTIFIED' | 'INSTITUTION_CONFIGURED' | 'OPERATIONAL_POLICY'
export type DeadlineStatus = 'PENDING' | 'DUE_SOON' | 'DUE_TODAY' | 'OVERDUE' | 'RESOLVED' | 'CANCELLED'

export interface DeadlineDto {
  id: string
  deadlineCode: string
  title: string
  description?: string
  deadlineType: DeadlineType
  statutoryClause?: string
  entityType: string
  entityId: string
  institutionId: string
  institutionName?: string
  targetDate: string
  gracePeriodDays: number
  status: DeadlineStatus
  resolvedAt?: string
  resolvedByName?: string
  resolutionNotes?: string
  reminderScheduleJson?: string
  version: number
  createdAt: string
  updatedAt: string
}

export interface DeadlineHistoryDto {
  id: string
  deadlineId: string
  previousTargetDate: string
  newTargetDate: string
  reason: string
  changedByUserId: string
  changedByName: string
  changedAt: string
}

export interface CreateDeadlineRequest {
  deadlineCode: string
  title: string
  description?: string
  deadlineType: DeadlineType
  statutoryClause?: string
  entityType: string
  entityId: string
  institutionId: string
  targetDate: string
  gracePeriodDays?: number
  reminderScheduleJson?: string
}

export interface RecalculateDeadlineRequest {
  newTargetDate: string
  reason: string
}

export interface ResolveDeadlineRequest {
  resolutionNotes: string
}

export const deadlinesApi = {
  list: async (params?: {
    institutionId?: string
    status?: DeadlineStatus
    page?: number
    size?: number
    sortBy?: string
    sortDir?: string
  }): Promise<PageResponse<DeadlineDto>> => {
    const query = new URLSearchParams()
    if (params?.institutionId) query.set('institutionId', params.institutionId)
    if (params?.status) query.set('status', params.status)
    if (params?.page !== undefined) query.set('page', params.page.toString())
    if (params?.size !== undefined) query.set('size', params.size.toString())
    if (params?.sortBy) query.set('sortBy', params.sortBy)
    if (params?.sortDir) query.set('sortDir', params.sortDir)

    const res = await apiClient<ApiResponseWrapper<PageResponse<DeadlineDto>>>(
      `/api/v1/deadlines?${query.toString()}`,
      { method: 'GET' }
    )
    return res.data
  },

  get: async (id: string): Promise<DeadlineDto> => {
    const res = await apiClient<ApiResponseWrapper<DeadlineDto>>(
      `/api/v1/deadlines/${id}`,
      { method: 'GET' }
    )
    return res.data
  },

  getHistory: async (id: string): Promise<DeadlineHistoryDto[]> => {
    const res = await apiClient<ApiResponseWrapper<DeadlineHistoryDto[]>>(
      `/api/v1/deadlines/${id}/history`,
      { method: 'GET' }
    )
    return res.data
  },

  create: async (data: CreateDeadlineRequest): Promise<DeadlineDto> => {
    const res = await apiClient<ApiResponseWrapper<DeadlineDto>>(
      '/api/v1/deadlines',
      {
        method: 'POST',
        body: JSON.stringify(data),
      }
    )
    return res.data
  },

  recalculate: async (id: string, data: RecalculateDeadlineRequest): Promise<DeadlineDto> => {
    const res = await apiClient<ApiResponseWrapper<DeadlineDto>>(
      `/api/v1/deadlines/${id}/recalculate`,
      {
        method: 'POST',
        body: JSON.stringify(data),
      }
    )
    return res.data
  },

  resolve: async (id: string, data: ResolveDeadlineRequest): Promise<DeadlineDto> => {
    const res = await apiClient<ApiResponseWrapper<DeadlineDto>>(
      `/api/v1/deadlines/${id}/resolve`,
      {
        method: 'POST',
        body: JSON.stringify(data),
      }
    )
    return res.data
  },
}
