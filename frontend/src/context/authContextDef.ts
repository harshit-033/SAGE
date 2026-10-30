import { createContext } from 'react'
import type { UserProfile } from '../api/authApi'

export interface AuthContextType {
  user: UserProfile | null
  token: string | null
  isLoading: boolean
  login: (username: string, password: string) => Promise<void>
  logout: () => Promise<void>
  hasRole: (role: string) => boolean
  hasAnyRole: (roles: string[]) => boolean
  hasPermission: (permission: string) => boolean
  isAdmin: boolean
  isInstitutionOfficer: boolean
}

export const AuthContext = createContext<AuthContextType | undefined>(undefined)
