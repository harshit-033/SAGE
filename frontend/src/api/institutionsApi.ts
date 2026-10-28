import { apiClient } from './client'
import type { ApiResponseWrapper } from './authApi'

export interface PageResponse<T> {
  content: T[]
  pageNumber: number
  pageSize: number
  totalElements: number
  totalPages: number
  first: boolean
  last: boolean
}

export interface Institution {
  id: string
  code: string
  name: string
  applicationType: string
  category: string
  addressLine1?: string
  addressLine2?: string
  city?: string
  state: string
  district: string
  pincode?: string
  officialEmail?: string
  officialPhone?: string
  alternatePhone?: string
  websiteUrl?: string
  establishmentYear?: number
  latitude?: number
  longitude?: number
  status: string
  active: boolean
  version: number
  createdAt: string
  updatedAt: string
}

export interface InstitutionUser {
  id: string
  institutionId: string
  userId: string
  username: string
  email: string
  fullName: string
  roleInInstitution: string
  primaryContact: boolean
  createdAt: string
}

export interface UpdateInstitutionData {
  name: string
  category?: string
  addressLine1?: string
  addressLine2?: string
  city?: string
  state: string
  district: string
  pincode?: string
  officialEmail?: string
  officialPhone?: string
  alternatePhone?: string
  websiteUrl?: string
  establishmentYear?: number
  latitude?: number
  longitude?: number
}

export interface CreateInstitutionData {
  code: string
  name: string
  applicationType: string
  state: string
  district: string
}

export interface InstitutionSearchParams {
  page?: number
  size?: number
  query?: string
  state?: string
  category?: string
  active?: boolean
  sortBy?: string
  sortDir?: string
}

export const institutionsApi = {
  listInstitutionsPaginated: async (
    params: InstitutionSearchParams = {}
  ): Promise<PageResponse<Institution>> => {
    const query = new URLSearchParams()
    if (params.page !== undefined) query.set('page', params.page.toString())
    if (params.size !== undefined) query.set('size', params.size.toString())
    if (params.query) query.set('query', params.query)
    if (params.state) query.set('state', params.state)
    if (params.category) query.set('category', params.category)
    if (params.active !== undefined) query.set('active', params.active.toString())
    if (params.sortBy) query.set('sortBy', params.sortBy)
    if (params.sortDir) query.set('sortDir', params.sortDir)

    const url = `/api/v1/institutions${query.toString() ? `?${query.toString()}` : ''}`
    const res = await apiClient<ApiResponseWrapper<PageResponse<Institution>>>(url, {
      method: 'GET',
    })
    return res.data
  },

  listInstitutions: async (): Promise<Institution[]> => {
    const pageRes = await institutionsApi.listInstitutionsPaginated({ size: 100 })
    return pageRes.content
  },

  getInstitutionById: async (id: string): Promise<Institution> => {
    const res = await apiClient<ApiResponseWrapper<Institution>>(`/api/v1/institutions/${id}`, {
      method: 'GET',
    })
    return res.data
  },

  createInstitution: async (data: CreateInstitutionData): Promise<Institution> => {
    const res = await apiClient<ApiResponseWrapper<Institution>>('/api/v1/institutions', {
      method: 'POST',
      body: JSON.stringify(data),
    })
    return res.data
  },

  updateInstitution: async (id: string, data: UpdateInstitutionData): Promise<Institution> => {
    const res = await apiClient<ApiResponseWrapper<Institution>>(`/api/v1/institutions/${id}`, {
      method: 'PUT',
      body: JSON.stringify(data),
    })
    return res.data
  },

  updateStatus: async (id: string, active: boolean, reason?: string): Promise<Institution> => {
    const res = await apiClient<ApiResponseWrapper<Institution>>(`/api/v1/institutions/${id}/status`, {
      method: 'PATCH',
      body: JSON.stringify({ active, reason }),
    })
    return res.data
  },

  getInstitutionUsers: async (id: string): Promise<InstitutionUser[]> => {
    const res = await apiClient<ApiResponseWrapper<InstitutionUser[]>>(`/api/v1/institutions/${id}/users`, {
      method: 'GET',
    })
    return res.data
  },

  addInstitutionUser: async (
    id: string,
    data: { userId: string; roleInInstitution: string; primaryContact: boolean }
  ): Promise<InstitutionUser> => {
    const res = await apiClient<ApiResponseWrapper<InstitutionUser>>(`/api/v1/institutions/${id}/users`, {
      method: 'POST',
      body: JSON.stringify(data),
    })
    return res.data
  },

  removeInstitutionUser: async (id: string, userId: string): Promise<void> => {
    await apiClient<ApiResponseWrapper<void>>(`/api/v1/institutions/${id}/users/${userId}`, {
      method: 'DELETE',
    })
  },
}
