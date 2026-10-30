import { apiClient } from './client'

export type InspectionStatus = 'PENDING' | 'SCHEDULED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED'
export type AssignmentRole = 'CHAIRPERSON' | 'MEMBER' | 'MEMBER_SECRETARY'
export type AssignmentStatus = 'PROPOSED' | 'NOTIFIED' | 'CONFIRMED' | 'DECLINED'
export type SchedulingSolverStatus = 'OPTIMAL' | 'FEASIBLE' | 'INFEASIBLE' | 'TIME_LIMIT_REACHED' | 'INVALID_INPUT' | 'ERROR'

export interface AssignedExpertDto {
  expertId: string
  name: string
  discipline: string
  role: AssignmentRole
  status: AssignmentStatus
}

export interface EvcInspectionDto {
  id: string
  applicationId: string
  applicationNumber: string
  institutionId: string
  institutionName: string
  durationInDays: number
  requiredMemberCount: number
  earliestStartDate: string
  latestStartDate: string
  scheduledStartDate?: string
  scheduledEndDate?: string
  status: InspectionStatus
  assignedCommittee: AssignedExpertDto[]
  specialInstructions?: string
}

export interface ExpertDto {
  id: string
  name: string
  email: string
  phone?: string
  designation?: string
  discipline: string
  organization?: string
  affiliatedInstitutionId?: string
  active: boolean
  availableDates?: string[]
}

export interface ScheduledInspectionResultDto {
  inspectionId: string
  institutionId?: string
  institutionName: string
  applicationId?: string
  applicationNumber: string
  scheduledStartDate: string
  scheduledEndDate: string
  durationInDays?: number
  requiredMemberCount?: number
  assignedExperts: AssignedExpertDto[]
}

export interface SchedulingRequestDto {
  inspectionIds: string[]
  allowRescheduling: boolean
  timeLimitSeconds: number
}

export interface SchedulingResponseDto {
  status: SchedulingSolverStatus
  totalInspections: number
  solveDurationMs: number
  explanation?: string
  scheduledInspections: ScheduledInspectionResultDto[]
}

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}

export const evcApi = {
  getPendingInspections: (): Promise<EvcInspectionDto[]> => {
    return apiClient<EvcInspectionDto[]>('/api/v1/evc/inspections/pending')
  },

  getScheduledInspections: (): Promise<EvcInspectionDto[]> => {
    return apiClient<EvcInspectionDto[]>('/api/v1/evc/inspections/scheduled')
  },

  getInspectionById: (id: string): Promise<EvcInspectionDto> => {
    return apiClient<EvcInspectionDto>(`/api/v1/evc/inspections/${id}`)
  },

  getCommitteeAssignments: (inspectionId: string): Promise<AssignedExpertDto[]> => {
    return apiClient<AssignedExpertDto[]>(`/api/v1/evc/inspections/${inspectionId}/committee`)
  },

  generateSchedule: (request: SchedulingRequestDto): Promise<SchedulingResponseDto> => {
    return apiClient<SchedulingResponseDto>('/api/v1/evc/schedules', {
      method: 'POST',
      body: JSON.stringify(request),
    })
  },

  getActiveExperts: (page = 0, size = 20): Promise<PageResponse<ExpertDto>> => {
    return apiClient<PageResponse<ExpertDto>>(`/api/v1/evc/experts?page=${page}&size=${size}`)
  },

  cancelInspection: (id: string, reason: string): Promise<{ message: string }> => {
    return apiClient<{ message: string }>(`/api/v1/evc/inspections/${id}/cancel?reason=${encodeURIComponent(reason)}`, {
      method: 'POST',
    })
  },
}
