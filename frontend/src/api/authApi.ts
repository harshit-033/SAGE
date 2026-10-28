import { apiClient } from './client'

export interface UserProfile {
  id: string
  username: string
  email: string
  fullName: string
  roles: string[]
  permissions?: string[]
  institutions: string[]
}

export interface LoginResponse {
  accessToken: string
  tokenType: string
  expiresInMs: number
  user: UserProfile
}

export interface ApiResponseWrapper<T> {
  success: boolean
  message: string
  data: T
  timestamp: string
}

export const authApi = {
  login: async (username: string, password: string): Promise<LoginResponse> => {
    const res = await apiClient<ApiResponseWrapper<LoginResponse>>('/api/v1/auth/login', {
      method: 'POST',
      body: JSON.stringify({ username, password }),
    })
    return res.data
  },

  getMe: async (): Promise<UserProfile> => {
    const res = await apiClient<ApiResponseWrapper<UserProfile>>('/api/v1/auth/me', {
      method: 'GET',
    })
    return res.data
  },

  logout: async (): Promise<void> => {
    try {
      await apiClient<ApiResponseWrapper<void>>('/api/v1/auth/logout', {
        method: 'POST',
      })
    } catch {
      // Local session termination continues even if network dropped
    }
  },
}
