import { apiClient } from './client'
import type { ApiResponseWrapper } from './authApi'
import type { PageResponse } from './institutionsApi'

export interface User {
  id: string
  username: string
  email: string
  fullName: string
  active: boolean
  roles: string[]
  institutionIds: string[]
  createdAt: string
  updatedAt: string
}

export interface Role {
  id: string
  code: string
  name: string
  description?: string
}

export interface CreateUserData {
  username: string
  email: string
  fullName: string
  password: string
  roles: string[]
  institutionId?: string
}

export interface UpdateUserData {
  fullName: string
  email: string
}

export interface UserSearchParams {
  page?: number
  size?: number
  query?: string
  active?: boolean
  sortBy?: string
  sortDir?: string
}

export const usersApi = {
  listUsers: async (params: UserSearchParams = {}): Promise<PageResponse<User>> => {
    const query = new URLSearchParams()
    if (params.page !== undefined) query.set('page', params.page.toString())
    if (params.size !== undefined) query.set('size', params.size.toString())
    if (params.query) query.set('query', params.query)
    if (params.active !== undefined) query.set('active', params.active.toString())
    if (params.sortBy) query.set('sortBy', params.sortBy)
    if (params.sortDir) query.set('sortDir', params.sortDir)

    const url = `/api/v1/users${query.toString() ? `?${query.toString()}` : ''}`
    const res = await apiClient<ApiResponseWrapper<PageResponse<User>>>(url, {
      method: 'GET',
    })
    return res.data
  },

  getUserById: async (id: string): Promise<User> => {
    const res = await apiClient<ApiResponseWrapper<User>>(`/api/v1/users/${id}`, {
      method: 'GET',
    })
    return res.data
  },

  createUser: async (data: CreateUserData): Promise<User> => {
    const res = await apiClient<ApiResponseWrapper<User>>('/api/v1/users', {
      method: 'POST',
      body: JSON.stringify(data),
    })
    return res.data
  },

  updateUser: async (id: string, data: UpdateUserData): Promise<User> => {
    const res = await apiClient<ApiResponseWrapper<User>>(`/api/v1/users/${id}`, {
      method: 'PUT',
      body: JSON.stringify(data),
    })
    return res.data
  },

  updateStatus: async (id: string, active: boolean, reason?: string): Promise<User> => {
    const res = await apiClient<ApiResponseWrapper<User>>(`/api/v1/users/${id}/status`, {
      method: 'PATCH',
      body: JSON.stringify({ active, reason }),
    })
    return res.data
  },

  assignRoles: async (id: string, roles: string[]): Promise<User> => {
    const res = await apiClient<ApiResponseWrapper<User>>(`/api/v1/users/${id}/roles`, {
      method: 'PUT',
      body: JSON.stringify({ roles }),
    })
    return res.data
  },

  getRoles: async (): Promise<Role[]> => {
    const res = await apiClient<ApiResponseWrapper<Role[]>>('/api/v1/users/roles', {
      method: 'GET',
    })
    return res.data
  },
}
