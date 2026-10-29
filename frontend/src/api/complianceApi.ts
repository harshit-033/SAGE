import { apiClient } from './client'
import type { ApiResponseWrapper } from './authApi'
import type { PageResponse } from './applicationsApi'
import type { AlertSeverity } from './alertsApi'

export type FindingType =
  | 'CONFIRMED_FAILURE'
  | 'POTENTIAL_ISSUE'
  | 'MISSING_EVIDENCE'
  | 'OCR_UNCERTAINTY'
  | 'AWAITING_VERIFICATION'

export type ComplianceFindingStatus =
  | 'OPEN'
  | 'ACKNOWLEDGED'
  | 'UNDER_REVIEW'
  | 'RESOLVED'
  | 'WAIVED'

export interface ComplianceFindingDto {
  id: string
  checkRunId: string
  institutionId: string
  institutionName: string
  applicationId?: string
  applicationNumber?: string
  documentId?: string
  documentFilename?: string
  ruleCode: string
  aphClause: string
  findingType: FindingType
  severity: AlertSeverity
  title: string
  description: string
  evidenceSummary: string
  status: ComplianceFindingStatus
  resolvedAt?: string
  resolvedByName?: string
  resolutionNotes?: string
  createdAt: string
  updatedAt: string
}

export interface ComplianceCheckRunDto {
  id: string
  runType: string
  triggerSource: string
  institutionId?: string
  institutionName?: string
  startedAt: string
  completedAt?: string
  status: string
  institutionsChecked: number
  findingsGenerated: number
  alertsCreated: number
  deadlinesCreated: number
  summaryNotes: string
  createdByName?: string
}

export interface ResolveComplianceFindingRequest {
  resolutionNotes: string
}

export const complianceApi = {
  getFindings: async (params?: {
    institutionId?: string
    status?: ComplianceFindingStatus
    page?: number
    size?: number
    sortBy?: string
    sortDir?: string
  }): Promise<PageResponse<ComplianceFindingDto>> => {
    const query = new URLSearchParams()
    if (params?.institutionId) query.set('institutionId', params.institutionId)
    if (params?.status) query.set('status', params.status)
    if (params?.page !== undefined) query.set('page', params.page.toString())
    if (params?.size !== undefined) query.set('size', params.size.toString())
    if (params?.sortBy) query.set('sortBy', params.sortBy)
    if (params?.sortDir) query.set('sortDir', params.sortDir)

    const res = await apiClient<ApiResponseWrapper<PageResponse<ComplianceFindingDto>>>(
      `/api/v1/compliance/findings?${query.toString()}`,
      { method: 'GET' }
    )
    return res.data
  },

  resolveFinding: async (
    id: string,
    data: ResolveComplianceFindingRequest
  ): Promise<ComplianceFindingDto> => {
    const res = await apiClient<ApiResponseWrapper<ComplianceFindingDto>>(
      `/api/v1/compliance/findings/${id}/resolve`,
      {
        method: 'POST',
        body: JSON.stringify(data),
      }
    )
    return res.data
  },

  getCheckRuns: async (params?: {
    institutionId?: string
    page?: number
    size?: number
    sortBy?: string
    sortDir?: string
  }): Promise<PageResponse<ComplianceCheckRunDto>> => {
    const query = new URLSearchParams()
    if (params?.institutionId) query.set('institutionId', params.institutionId)
    if (params?.page !== undefined) query.set('page', params.page.toString())
    if (params?.size !== undefined) query.set('size', params.size.toString())
    if (params?.sortBy) query.set('sortBy', params.sortBy)
    if (params?.sortDir) query.set('sortDir', params.sortDir)

    const res = await apiClient<ApiResponseWrapper<PageResponse<ComplianceCheckRunDto>>>(
      `/api/v1/compliance/runs?${query.toString()}`,
      { method: 'GET' }
    )
    return res.data
  },

  triggerScan: async (institutionId?: string): Promise<ComplianceCheckRunDto> => {
    const query = new URLSearchParams()
    if (institutionId) query.set('institutionId', institutionId)

    const res = await apiClient<ApiResponseWrapper<ComplianceCheckRunDto>>(
      `/api/v1/compliance/scan?${query.toString()}`,
      { method: 'POST' }
    )
    return res.data
  },
}
