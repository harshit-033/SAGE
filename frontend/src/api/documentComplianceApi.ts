import { apiClient } from './client'
import type { ApiResponseWrapper } from './authApi'
import type { ExtractedFieldDto, ScrutinyFindingDto } from './applicationsApi'

export interface DocumentDto {
  id: string
  applicationId?: string
  institutionId: string
  institutionName?: string
  documentName: string
  documentType: string
  documentCategory: 'INFRASTRUCTURE' | 'FACULTY' | 'FINANCIAL' | 'LEGAL' | 'ACADEMIC' | 'GENERAL'
  originalFilename: string
  contentType: string
  fileSizeBytes: number
  sha256Hash: string
  ocrStatus: 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED' | 'UNCERTAIN'
  ocrEngine?: string
  overallConfidence?: number
  uncertain: boolean
  validFrom?: string
  expiryDate?: string
  daysUntilExpiry?: number
  expiryIndicator: 'VALID' | 'EXPIRING_SOON' | 'EXPIRED' | 'NO_EXPIRY'
  verificationStatus: 'PENDING_REVIEW' | 'UNDER_REVIEW' | 'VERIFIED' | 'VERIFIED_WITH_WARNINGS' | 'REJECTED' | 'REQUIRES_CLARIFICATION' | 'REPROCESSING'
  complianceStatus: 'COMPLIANT' | 'EXPIRING_SOON' | 'EXPIRED' | 'DEFICIENT' | 'ACTION_REQUIRED' | 'NOT_APPLICABLE'
  versionNumber: number
  previousVersionId?: string
  rootDocumentId?: string
  officerComments?: string
  clarificationText?: string
  aiAnalysisReport?: string
  extractedFields?: ExtractedFieldDto[]
  createdAt: string
  updatedAt: string
}

export interface DocumentAiValidationResult {
  hasIssues: boolean
  forgeryRisk: 'LOW' | 'MEDIUM' | 'HIGH'
  formatStatus: 'VALID' | 'MINOR_IRREGULARITY' | 'DEFICIENT'
  title: string
  summary: string
  formatObservations: string[]
  forgeryObservations: string[]
  recommendations: string[]
  fullReport: string
  evaluatedAt?: string
}

export interface DocumentCorrectiveActionDto {
  id: string
  institutionId: string
  institutionName?: string
  applicationId?: string
  applicationNumber?: string
  documentId?: string
  documentName?: string
  documentType?: string
  findingId?: string
  actionType: string
  description: string
  priority: string
  responsibleParty: string
  dueDate?: string
  status: 'OPEN' | 'IN_PROGRESS' | 'SUBMITTED_FOR_REVIEW' | 'RESOLVED' | 'REJECTED' | 'OVERDUE' | 'CANCELLED'
  institutionResponse?: string
  resolutionEvidenceDocId?: string
  resolutionEvidenceDocName?: string
  officerNotes?: string
  resolvedByName?: string
  resolvedAt?: string
  createdAt: string
  updatedAt: string
}

export interface DocumentDetailResponse {
  document: DocumentDto
  extractedFields: ExtractedFieldDto[]
  findings: ScrutinyFindingDto[]
  correctiveActions: DocumentCorrectiveActionDto[]
  versions: DocumentDto[]
}

export interface DocumentComplianceSummaryDto {
  totalDocuments: number
  verifiedDocuments: number
  pendingReviewDocuments: number
  expiringSoonDocuments: number
  expiredDocuments: number
  deficientDocuments: number
  pendingActionsCount: number
}

export interface PageResponse<T> {
  content: T[]
  pageNumber: number
  pageSize: number
  totalElements: number
  totalPages: number
  first: boolean
  last: boolean
}

export const documentComplianceApi = {
  listDocuments: async (params?: {
    institutionId?: string
    applicationId?: string
    category?: string
    verificationStatus?: string
    complianceStatus?: string
    search?: string
    page?: number
    size?: number
  }): Promise<PageResponse<DocumentDto>> => {
    const q = new URLSearchParams()
    if (params?.institutionId) q.append('institutionId', params.institutionId)
    if (params?.applicationId) q.append('applicationId', params.applicationId)
    if (params?.category) q.append('category', params.category)
    if (params?.verificationStatus) q.append('verificationStatus', params.verificationStatus)
    if (params?.complianceStatus) q.append('complianceStatus', params.complianceStatus)
    if (params?.search) q.append('search', params.search)
    if (params?.page !== undefined) q.append('page', params.page.toString())
    if (params?.size !== undefined) q.append('size', params.size.toString())

    const res = await apiClient<ApiResponseWrapper<PageResponse<DocumentDto>>>(
      `/api/v1/documents?${q.toString()}`,
      { method: 'GET' }
    )
    return res.data
  },

  getDocumentDetail: async (id: string): Promise<DocumentDetailResponse> => {
    const res = await apiClient<ApiResponseWrapper<DocumentDetailResponse>>(
      `/api/v1/documents/${id}`,
      { method: 'GET' }
    )
    return res.data
  },

  validateDocumentWithAi: async (formData: FormData): Promise<DocumentAiValidationResult> => {
    const res = await apiClient<ApiResponseWrapper<DocumentAiValidationResult>>('/api/v1/documents/validate-ai', {
      method: 'POST',
      body: formData,
    })
    return res.data
  },

  uploadDocument: async (formData: FormData): Promise<DocumentDto> => {
    const res = await apiClient<ApiResponseWrapper<DocumentDto>>('/api/v1/documents', {
      method: 'POST',
      body: formData,
    })
    return res.data
  },

  replaceDocument: async (id: string, formData: FormData): Promise<DocumentDto> => {
    const res = await apiClient<ApiResponseWrapper<DocumentDto>>(`/api/v1/documents/${id}/replace`, {
      method: 'POST',
      body: formData,
    })
    return res.data
  },

  submitClarification: async (id: string, clarificationText: string): Promise<DocumentDto> => {
    const res = await apiClient<ApiResponseWrapper<DocumentDto>>(`/api/v1/documents/${id}/clarify`, {
      method: 'POST',
      body: JSON.stringify({ clarificationText }),
    })
    return res.data
  },

  reviewDocument: async (
    id: string,
    body: {
      verificationStatus: string
      complianceStatus?: string
      officerComments?: string
    }
  ): Promise<DocumentDto> => {
    const res = await apiClient<ApiResponseWrapper<DocumentDto>>(`/api/v1/documents/${id}/review`, {
      method: 'POST',
      body: JSON.stringify(body),
    })
    return res.data
  },

  getComplianceSummary: async (institutionId?: string): Promise<DocumentComplianceSummaryDto> => {
    const q = institutionId ? `?institutionId=${institutionId}` : ''
    const res = await apiClient<ApiResponseWrapper<DocumentComplianceSummaryDto>>(
      `/api/v1/documents/compliance-summary${q}`,
      { method: 'GET' }
    )
    return res.data
  },

  listCorrectiveActions: async (params?: {
    institutionId?: string
    status?: string
    page?: number
    size?: number
  }): Promise<PageResponse<DocumentCorrectiveActionDto>> => {
    const q = new URLSearchParams()
    if (params?.institutionId) q.append('institutionId', params.institutionId)
    if (params?.status) q.append('status', params.status)
    if (params?.page !== undefined) q.append('page', params.page.toString())
    if (params?.size !== undefined) q.append('size', params.size.toString())

    const res = await apiClient<ApiResponseWrapper<PageResponse<DocumentCorrectiveActionDto>>>(
      `/api/v1/corrective-actions?${q.toString()}`,
      { method: 'GET' }
    )
    return res.data
  },

  getCorrectiveAction: async (id: string): Promise<DocumentCorrectiveActionDto> => {
    const res = await apiClient<ApiResponseWrapper<DocumentCorrectiveActionDto>>(
      `/api/v1/corrective-actions/${id}`,
      { method: 'GET' }
    )
    return res.data
  },

  respondCorrectiveAction: async (
    id: string,
    body: {
      institutionResponse: string
      resolutionEvidenceDocId?: string
    }
  ): Promise<DocumentCorrectiveActionDto> => {
    const res = await apiClient<ApiResponseWrapper<DocumentCorrectiveActionDto>>(
      `/api/v1/corrective-actions/${id}/respond`,
      {
        method: 'POST',
        body: JSON.stringify(body),
      }
    )
    return res.data
  },

  resolveCorrectiveAction: async (
    id: string,
    body: {
      status?: string
      officerNotes?: string
    }
  ): Promise<DocumentCorrectiveActionDto> => {
    const res = await apiClient<ApiResponseWrapper<DocumentCorrectiveActionDto>>(
      `/api/v1/corrective-actions/${id}/resolve`,
      {
        method: 'POST',
        body: JSON.stringify(body),
      }
    )
    return res.data
  },

  uploadInstitutionDocument: async (formData: FormData): Promise<DocumentDto> => {
    return documentComplianceApi.uploadDocument(formData)
  },

  getPreviewUrl: (id: string): string => {
    return `/api/v1/documents/${id}/preview`
  },

  getDownloadUrl: (id: string): string => {
    return `/api/v1/documents/${id}/download`
  },
}
