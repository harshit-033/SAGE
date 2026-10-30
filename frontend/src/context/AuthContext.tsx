import React, { useContext, useState, useEffect } from 'react'
import { authApi, type UserProfile } from '../api/authApi'
import { AuthContext, type AuthContextType } from './authContextDef'

export { AuthContext, type AuthContextType }

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<UserProfile | null>(null)
  const [token, setToken] = useState<string | null>(() => localStorage.getItem('sage_token'))
  const [isLoading, setIsLoading] = useState<boolean>(true)

  useEffect(() => {
    const initializeAuth = async () => {
      const storedToken = localStorage.getItem('sage_token')
      if (!storedToken) {
        setIsLoading(false)
        return
      }
      try {
        const profile = await authApi.getMe()
        setUser(profile)
      } catch (err) {
        console.warn('Session restoration failed:', err)
        localStorage.removeItem('sage_token')
        setToken(null)
        setUser(null)
      } finally {
        setIsLoading(false)
      }
    }

    initializeAuth()
  }, [])

  const login = async (username: string, password: string) => {
    setIsLoading(true)
    try {
      const resp = await authApi.login(username, password)
      localStorage.setItem('sage_token', resp.accessToken)
      setToken(resp.accessToken)
      setUser(resp.user)
    } finally {
      setIsLoading(false)
    }
  }

  const logout = async () => {
    try {
      await authApi.logout()
    } finally {
      localStorage.removeItem('sage_token')
      setToken(null)
      setUser(null)
    }
  }

  const hasRole = (role: string) => {
    if (!user) return false
    const checkRole = role.startsWith('ROLE_') ? role : `ROLE_${role}`
    return user.roles.includes(checkRole)
  }

  const hasAnyRole = (roles: string[]) => {
    if (!user) return false
    return roles.some((r) => hasRole(r))
  }

  const hasPermission = (permission: string) => {
    if (!user) return false
    if (isAdmin) return true
    return user.permissions?.includes(permission) ?? false
  }

  const isAdmin = hasRole('ROLE_AICTE_ADMIN')
  const isInstitutionOfficer = hasRole('ROLE_INSTITUTION_OFFICER')

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        isLoading,
        login,
        logout,
        hasRole,
        hasAnyRole,
        hasPermission,
        isAdmin,
        isInstitutionOfficer,
      }}
    >
      {children}
    </AuthContext.Provider>
  )
}

// eslint-disable-next-line react-refresh/only-export-components
export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return context
}
