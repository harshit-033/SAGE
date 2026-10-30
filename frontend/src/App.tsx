import React from 'react'
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { AuthProvider, useAuth } from './context/AuthContext'
import { AppShell } from './components/layout/AppShell'
import { Login } from './pages/Login'
import { Dashboard } from './pages/Dashboard'
import { Institutions } from './pages/Institutions'
import { InstitutionProfile } from './pages/InstitutionProfile'
import { UserManagement } from './pages/UserManagement'
import { AuditLogViewer } from './pages/AuditLogViewer'
import { SystemHealth } from './pages/SystemHealth'
import { Applications } from './pages/Applications'
import { ApplicationDetails } from './pages/ApplicationDetails'
import { Alerts } from './pages/Alerts'
import { Compliance } from './pages/Compliance'
import { EvcScheduling } from './pages/EvcScheduling'
import { DocumentsCompliance } from './pages/DocumentsCompliance'
import { RequiredActions } from './pages/RequiredActions'
import { AccessDenied } from './pages/AccessDenied'
import { NotFound } from './pages/NotFound'

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      refetchOnWindowFocus: false,
    },
  },
})

const ProtectedRoute: React.FC<{
  children: React.ReactNode
  requiredRole?: string
}> = ({ children, requiredRole }) => {
  const { user, isLoading, hasRole } = useAuth()

  if (isLoading) {
    return (
      <div
        style={{
          minHeight: '100vh',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          backgroundColor: 'var(--bg-page)',
          color: 'var(--text-secondary)',
        }}
      >
        <p>Verifying regulatory security context...</p>
      </div>
    )
  }

  if (!user) {
    return <Navigate to="/login" replace />
  }

  if (requiredRole && !hasRole(requiredRole) && !hasRole('ROLE_AICTE_ADMIN')) {
    return <Navigate to="/access-denied" replace />
  }

  return <>{children}</>
}

export const AppContent: React.FC = () => {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />

      <Route
        element={
          <ProtectedRoute>
            <AppShell />
          </ProtectedRoute>
        }
      >
        <Route path="/" element={<Dashboard />} />
        <Route path="/dashboard" element={<Dashboard />} />
        <Route path="/applications" element={<Applications />} />
        <Route path="/applications/:id" element={<ApplicationDetails />} />
        <Route path="/documents-compliance" element={<DocumentsCompliance />} />
        <Route path="/required-actions" element={<RequiredActions />} />
        <Route path="/alerts" element={<Alerts />} />
        <Route path="/compliance" element={<Compliance />} />
        <Route path="/institutions" element={<Institutions />} />
        <Route path="/institutions/:id" element={<InstitutionProfile />} />
        <Route path="/evc" element={<EvcScheduling />} />
        <Route path="/evc/schedule" element={<EvcScheduling />} />

        {/* Administrative Only Routes */}
        <Route
          path="/admin/users"
          element={
            <ProtectedRoute requiredRole="ROLE_AICTE_ADMIN">
              <UserManagement />
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/audit"
          element={
            <ProtectedRoute requiredRole="ROLE_AUDITOR">
              <AuditLogViewer />
            </ProtectedRoute>
          }
        />

        <Route
          path="/system-health"
          element={
            <ProtectedRoute requiredRole="ROLE_AICTE_ADMIN">
              <SystemHealth />
            </ProtectedRoute>
          }
        />
        <Route path="/access-denied" element={<AccessDenied />} />
        <Route path="*" element={<NotFound />} />
      </Route>
    </Routes>
  )
}

export function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <BrowserRouter>
          <AppContent />
        </BrowserRouter>
      </AuthProvider>
    </QueryClientProvider>
  )
}

export default App
