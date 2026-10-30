import { apiClient } from './client'
import type { ApiResponseWrapper } from './authApi'

export interface CourseDetailsDto {
  id?: string
  applicationId?: string
  courseName: string
  programmeLevel: string
  degreeType: string
  courseDiscipline: string
  proposedIntake: number
  modeOfDelivery?: string
  durationYears?: number
  regulatoryClassification?: string
  proposedDepartment?: string
  existingRelatedCourses?: string
  availableClassrooms?: number
  laboratoriesCount?: number
  laboratoryDetails?: string
  equipmentDetails?: string
  libraryTitles?: number
  libraryVolumes?: number
  libraryJournals?: number
  professorsCount?: number
  associateProfessorsCount?: number
  assistantProfessorsCount?: number
  facultyStudentRatio?: string
  infrastructureNotes?: string
}

export interface BranchDetailsDto {
  id?: string
  applicationId?: string
  existingProgramme: string
  existingDepartment: string
  proposedBranchName: string
  proposedIntake: number
  existingApprovedIntake?: number
  facultyCount?: number
  staffCount?: number
  availableClassrooms?: number
  laboratoryCount?: number
  laboratoryRequirements?: string
  equipmentAvailability?: string
  institutionDeclarations?: string
}

export interface ApplicationDto {
  id: string
  applicationNumber: string
  institutionId: string
  institutionName: string
  academicYear: string
  category: string
  currentState: string
  workflowCode: string
  workflowName: string
  requiresEvc: boolean
  requiresShc: boolean
  requiresSac: boolean
  submittedAt?: string
  finalDecisionAt?: string
  version: number
  courseDetails?: CourseDetailsDto
  branchDetails?: BranchDetailsDto
  createdAt: string
  updatedAt: string
}

export interface ExtractedFieldDto {
  id: string
  fieldKey: string
  extractedValue: string
  normalizedValue?: string
  confidenceScore: number
  uncertain: boolean
  sourcePage: number
  boundingBoxJson?: string
  rawTextSnippet?: string
}

export interface DocumentDto {
  id: string
  applicationId: string
  institutionId: string
  documentType: string
  originalFilename: string
  contentType: string
  fileSizeBytes: number
  sha256Hash: string
  ocrStatus: 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED' | 'UNCERTAIN'
  ocrEngine?: string
  overallConfidence?: number
  uncertain: boolean
  extractedFields: ExtractedFieldDto[]
  createdAt: string
  updatedAt: string
}

export interface ScrutinyFindingDto {
  id: string
  evaluationId: string
  applicationId: string
  ruleId: string
  ruleName: string
  aphClause: string
  aphPageNumber?: number
  severity: string
  documentId?: string
  documentType?: string
  status: 'COMPLIANT' | 'DEFICIENT' | 'UNCERTAIN_OCR' | 'NOT_APPLICABLE'
  deficiencyType?: string
  explanation: string
  evidenceText?: string
  resolved: boolean
  resolutionNotes?: string
  resolvedByName?: string
  resolvedAt?: string
  createdAt: string
}

export interface ScrutinyEvaluationDto {
  id: string
  applicationId: string
  status: string
  summary: string
  evaluatedByName: string
  evaluatedAt: string
  score?: number
  recommendation?: string
  remarks?: string
  criteriaBreakdownJson?: string
  findings: ScrutinyFindingDto[]
}

export interface SubmitEvaluatorAssessmentRequest {
  score: number
  recommendation: string
  remarks: string
  summary?: string
  criteriaBreakdownJson?: string
}

export interface WorkflowTransitionDto {
  id: string
  applicationId: string
  fromState: string
  toState: string
  actorId: string
  actorName: string
  actorRole: string
  reason?: string
  evidenceSummary?: string
  createdAt: string
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

export const applicationsApi = {
  list: async (params?: {
    institutionId?: string
    state?: string
    category?: string
    page?: number
    size?: number
  }): Promise<PageResponse<ApplicationDto>> => {
    const query = new URLSearchParams()
    if (params?.institutionId) query.set('institutionId', params.institutionId)
    if (params?.state) query.set('state', params.state)
    if (params?.category) query.set('category', params.category)
    if (params?.page !== undefined) query.set('page', params.page.toString())
    if (params?.size !== undefined) query.set('size', params.size.toString())

    const res = await apiClient<ApiResponseWrapper<PageResponse<ApplicationDto>>>(
      `/api/v1/applications?${query.toString()}`,
      { method: 'GET' }
    )
    return res.data
  },

  get: async (id: string): Promise<ApplicationDto> => {
    const res = await apiClient<ApiResponseWrapper<ApplicationDto>>(
      `/api/v1/applications/${id}`,
      { method: 'GET' }
    )
    return res.data
  },

  create: async (data: {
    institutionId: string
    academicYear: string
    category: string
  }): Promise<ApplicationDto> => {
    const res = await apiClient<ApiResponseWrapper<ApplicationDto>>('/api/v1/applications', {
      method: 'POST',
      body: JSON.stringify(data),
    })
    return res.data
  },

  submit: async (id: string): Promise<ApplicationDto> => {
    const res = await apiClient<ApiResponseWrapper<ApplicationDto>>(
      `/api/v1/applications/${id}/submit`,
      { method: 'POST' }
    )
    return res.data
  },

  transition: async (
    id: string,
    data: { targetState: string; reason?: string; evidenceSummary?: string }
  ): Promise<ApplicationDto> => {
    const res = await apiClient<ApiResponseWrapper<ApplicationDto>>(
      `/api/v1/applications/${id}/transitions`,
      {
        method: 'POST',
        body: JSON.stringify(data),
      }
    )
    return res.data
  },

  getTransitions: async (id: string): Promise<WorkflowTransitionDto[]> => {
    const res = await apiClient<ApiResponseWrapper<WorkflowTransitionDto[]>>(
      `/api/v1/applications/${id}/transitions`,
      { method: 'GET' }
    )
    return res.data
  },

  listDocuments: async (applicationId: string): Promise<DocumentDto[]> => {
    const res = await apiClient<ApiResponseWrapper<DocumentDto[]>>(
      `/api/v1/applications/${applicationId}/documents`,
      { method: 'GET' }
    )
    return res.data
  },

  uploadDocument: async (
    applicationId: string,
    documentType: string,
    file: File
  ): Promise<DocumentDto> => {
    const formData = new FormData()
    formData.append('documentType', documentType)
    formData.append('file', file)
    const res = await apiClient<ApiResponseWrapper<DocumentDto>>(
      `/api/v1/applications/${applicationId}/documents`,
      {
        method: 'POST',
        body: formData,
      }
    )
    return res.data
  },

  reprocessOcr: async (applicationId: string, documentId: string): Promise<DocumentDto> => {
    const res = await apiClient<ApiResponseWrapper<DocumentDto>>(
      `/api/v1/applications/${applicationId}/documents/${documentId}/ocr`,
      { method: 'POST' }
    )
    return res.data
  },

  evaluateScrutiny: async (applicationId: string): Promise<ScrutinyEvaluationDto> => {
    const res = await apiClient<ApiResponseWrapper<ScrutinyEvaluationDto>>(
      `/api/v1/applications/${applicationId}/scrutiny/evaluate`,
      { method: 'POST' }
    )
    return res.data
  },

  getFindings: async (applicationId: string): Promise<ScrutinyFindingDto[]> => {
    const res = await apiClient<ApiResponseWrapper<ScrutinyFindingDto[]>>(
      `/api/v1/applications/${applicationId}/scrutiny/findings`,
      { method: 'GET' }
    )
    return res.data
  },

  resolveFinding: async (
    applicationId: string,
    findingId: string,
    data: { resolutionNotes: string }
  ): Promise<ScrutinyFindingDto> => {
    const res = await apiClient<ApiResponseWrapper<ScrutinyFindingDto>>(
      `/api/v1/applications/${applicationId}/scrutiny/findings/${findingId}/resolve`,
      {
        method: 'POST',
        body: JSON.stringify(data),
      }
    )
    return res.data
  },

  getEvaluations: async (applicationId: string): Promise<ScrutinyEvaluationDto[]> => {
    const res = await apiClient<ApiResponseWrapper<ScrutinyEvaluationDto[]>>(
      `/api/v1/applications/${applicationId}/scrutiny/evaluations`,
      { method: 'GET' }
    )
    return res.data
  },

  assessApplication: async (
    applicationId: string,
    data: SubmitEvaluatorAssessmentRequest
  ): Promise<ScrutinyEvaluationDto> => {
    const res = await apiClient<ApiResponseWrapper<ScrutinyEvaluationDto>>(
      `/api/v1/applications/${applicationId}/scrutiny/assess`,
      {
        method: 'POST',
        body: JSON.stringify(data),
      }
    )
    return res.data
  },

  saveCourseDetails: async (
    applicationId: string,
    data: CourseDetailsDto
  ): Promise<CourseDetailsDto> => {
    const res = await apiClient<ApiResponseWrapper<CourseDetailsDto>>(
      `/api/v1/applications/${applicationId}/course-details`,
      {
        method: 'POST',
        body: JSON.stringify(data),
      }
    )
    return res.data
  },

  getCourseDetails: async (applicationId: string): Promise<CourseDetailsDto> => {
    const res = await apiClient<ApiResponseWrapper<CourseDetailsDto>>(
      `/api/v1/applications/${applicationId}/course-details`,
      { method: 'GET' }
    )
    return res.data
  },

  saveBranchDetails: async (
    applicationId: string,
    data: BranchDetailsDto
  ): Promise<BranchDetailsDto> => {
    const res = await apiClient<ApiResponseWrapper<BranchDetailsDto>>(
      `/api/v1/applications/${applicationId}/branch-details`,
      {
        method: 'POST',
        body: JSON.stringify(data),
      }
    )
    return res.data
  },

  getBranchDetails: async (applicationId: string): Promise<BranchDetailsDto> => {
    const res = await apiClient<ApiResponseWrapper<BranchDetailsDto>>(
      `/api/v1/applications/${applicationId}/branch-details`,
      { method: 'GET' }
    )
    return res.data
  },
}
