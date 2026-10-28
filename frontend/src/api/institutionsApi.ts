import { apiClient } from './client'
import type { ApiResponseWrapper } from './authApi'

export interface Institution {
  id: string
  code: string
  name: string
  applicationType: string
  state: string
  district: string
  status: string
  version: number
  createdAt: string
  updatedAt: string
}

export const institutionsApi = {
  listInstitutions: async (): Promise<Institution[]> => {
    const res = await apiClient<ApiResponseWrapper<Institution[]>>('/api/v1/institutions', {
      method: 'GET',
    })
    return res.data
  },

  getInstitutionById: async (id: string): Promise<Institution> => {
    const res = await apiClient<ApiResponseWrapper<Institution>>(`/api/v1/institutions/${id}`, {
      method: 'GET',
    })
    return res.data
  },
}
