import React from 'react'
import { useAuth } from '../context/AuthContext'
import { Breadcrumbs } from '../components/common/Breadcrumbs'
import { AdminDashboard } from '../components/dashboard/AdminDashboard'
import { RegionalDashboard } from '../components/dashboard/RegionalDashboard'
import { ComplianceDashboard } from '../components/dashboard/ComplianceDashboard'
import { AuditorDashboard } from '../components/dashboard/AuditorDashboard'
import { InstitutionDashboard } from '../components/dashboard/InstitutionDashboard'

export const Dashboard: React.FC = () => {
  const { isAdmin, hasRole } = useAuth()

  const renderRoleDashboard = () => {
    if (isAdmin) {
      return <AdminDashboard />
    }
    if (hasRole('ROLE_REGIONAL_OFFICER')) {
      return <RegionalDashboard />
    }
    if (hasRole('ROLE_COMPLIANCE_OFFICER')) {
      return <ComplianceDashboard />
    }
    if (hasRole('ROLE_AUDITOR')) {
      return <AuditorDashboard />
    }
    // Default institutional signatory dashboard
    return <InstitutionDashboard />
  }

  return (
    <div>
      <Breadcrumbs items={[]} />
      {renderRoleDashboard()}
    </div>
  )
}
