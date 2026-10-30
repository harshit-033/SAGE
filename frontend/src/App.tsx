import React from 'react'
import { BrowserRouter, Routes, Route, Navigate, useParams } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { AuthProvider, useAuth } from './context/AuthContext'
import { MessageProvider } from './context/MessageContext'
import { MessageBox } from './components/common/MessageBox'
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

export const ProtectedRoute: React.FC<{
  children: React.ReactNode
  requiredRole?: string
  allowedRoles?: string[]
}> = ({ children, requiredRole, allowedRoles }) => {
  const { user, isLoading, hasRole, isAdmin } = useAuth()

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

  if (requiredRole && !hasRole(requiredRole) && !isAdmin) {
    return <Navigate to="/access-denied" replace />
  }

  if (allowedRoles && !allowedRoles.some((r) => hasRole(r)) && !isAdmin) {
    return <Navigate to="/access-denied" replace />
  }

  return <>{children}</>
}

/**
 * Route wrapper for /institutions directory.
 * If logged-in as an Institution Officer, redirects directly to own institutional profile.
 */
const InstitutionsRoute: React.FC = () => {
  const { user, hasRole, isAdmin } = useAuth()
  if (hasRole('ROLE_INSTITUTION_OFFICER') && !isAdmin) {
    const primaryInst = user?.institutions && user.institutions.length > 0 ? user.institutions[0] : null
    if (primaryInst) {
      return <Navigate to={`/institutions/${primaryInst}`} replace />
    }
    return <Navigate to="/dashboard" replace />
  }
  return <Institutions />
}

/**
 * Route wrapper for /institutions/:id profile.
 * Strictly verifies that Institution Officers only access their authorized institution.
 */
const InstitutionProfileRoute: React.FC = () => {
  const { id } = useParams<{ id: string }>()
  const { user, hasRole, isAdmin } = useAuth()
  if (hasRole('ROLE_INSTITUTION_OFFICER') && !isAdmin) {
    if (!id || !user?.institutions || !user.institutions.includes(id)) {
      return <Navigate to="/access-denied" replace />
    }
  }
  return <InstitutionProfile />
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

        {/* Applications & Scrutiny: Admin, Regional, Institution, Auditor */}
        <Route
          path="/applications"
          element={
            <ProtectedRoute allowedRoles={['ROLE_AICTE_ADMIN', 'ROLE_REGIONAL_OFFICER', 'ROLE_INSTITUTION_OFFICER', 'ROLE_AUDITOR']}>
              <Applications />
            </ProtectedRoute>
          }
        />
        <Route
          path="/applications/:id"
          element={
            <ProtectedRoute allowedRoles={['ROLE_AICTE_ADMIN', 'ROLE_REGIONAL_OFFICER', 'ROLE_INSTITUTION_OFFICER', 'ROLE_AUDITOR']}>
              <ApplicationDetails />
            </ProtectedRoute>
          }
        />

        {/* Documents & Compliance: Admin, Regional, Institution, Compliance, Auditor */}
        <Route
          path="/documents-compliance"
          element={
            <ProtectedRoute allowedRoles={['ROLE_AICTE_ADMIN', 'ROLE_REGIONAL_OFFICER', 'ROLE_INSTITUTION_OFFICER', 'ROLE_COMPLIANCE_OFFICER', 'ROLE_AUDITOR']}>
              <DocumentsCompliance />
            </ProtectedRoute>
          }
        />

        {/* Required Actions: Admin, Regional, Institution, Compliance */}
        <Route
          path="/required-actions"
          element={
            <ProtectedRoute allowedRoles={['ROLE_AICTE_ADMIN', 'ROLE_REGIONAL_OFFICER', 'ROLE_INSTITUTION_OFFICER', 'ROLE_COMPLIANCE_OFFICER']}>
              <RequiredActions />
            </ProtectedRoute>
          }
        />

        {/* Regulatory Alerts: All authenticated roles */}
        <Route path="/alerts" element={<Alerts />} />

        {/* Compliance & Surveillance: Admin, Compliance, Auditor */}
        <Route
          path="/compliance"
          element={
            <ProtectedRoute allowedRoles={['ROLE_AICTE_ADMIN', 'ROLE_COMPLIANCE_OFFICER', 'ROLE_AUDITOR']}>
              <Compliance />
            </ProtectedRoute>
          }
        />

        {/* Institutions Directory: Admin, Regional, Compliance, Auditor, Institution (redirected) */}
        <Route
          path="/institutions"
          element={
            <ProtectedRoute allowedRoles={['ROLE_AICTE_ADMIN', 'ROLE_REGIONAL_OFFICER', 'ROLE_COMPLIANCE_OFFICER', 'ROLE_AUDITOR', 'ROLE_INSTITUTION_OFFICER']}>
              <InstitutionsRoute />
            </ProtectedRoute>
          }
        />
        <Route
          path="/institutions/:id"
          element={
            <ProtectedRoute allowedRoles={['ROLE_AICTE_ADMIN', 'ROLE_REGIONAL_OFFICER', 'ROLE_COMPLIANCE_OFFICER', 'ROLE_AUDITOR', 'ROLE_INSTITUTION_OFFICER']}>
              <InstitutionProfileRoute />
            </ProtectedRoute>
          }
        />

        {/* EVC Panel Allocation: Admin, Regional, EVC Coordinator */}
        <Route
          path="/evc"
          element={
            <ProtectedRoute allowedRoles={['ROLE_AICTE_ADMIN', 'ROLE_REGIONAL_OFFICER', 'ROLE_EVC_COORDINATOR']}>
              <EvcScheduling />
            </ProtectedRoute>
          }
        />
        <Route
          path="/evc/schedule"
          element={
            <ProtectedRoute allowedRoles={['ROLE_AICTE_ADMIN', 'ROLE_REGIONAL_OFFICER', 'ROLE_EVC_COORDINATOR']}>
              <EvcScheduling />
            </ProtectedRoute>
          }
        />

        {/* Central Administrative Governance */}
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
            <ProtectedRoute allowedRoles={['ROLE_AICTE_ADMIN', 'ROLE_AUDITOR']}>
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
        <MessageProvider>
          <BrowserRouter>
            <AppContent />
            <MessageBox />
          </BrowserRouter>
        </MessageProvider>
      </AuthProvider>
    </QueryClientProvider>
  )
}

export default App
