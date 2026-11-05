import React from 'react'

interface StatusBadgeProps {
  status: string
}

export const StatusBadge: React.FC<StatusBadgeProps> = ({ status }) => {
  let badgeClass: string
  let label = status

  switch (status.toUpperCase()) {
    case 'APPROVED':
      badgeClass = 'badge-success'
      label = 'Approved'
      break
    case 'ACTIVE':
    case 'UP':
      badgeClass = 'badge-success'
      label = status === 'UP' ? 'Operational' : 'Active'
      break
    case 'SCRUTINY_PASSED':
      badgeClass = 'badge-success'
      label = 'Scrutiny Passed'
      break
    case 'SUBMITTED':
      badgeClass = 'badge-primary'
      label = 'Submitted'
      break
    case 'UNDER_SCRUTINY':
      badgeClass = 'badge-info'
      label = 'Under Scrutiny'
      break
    case 'RESUBMITTED':
      badgeClass = 'badge-primary'
      label = 'Resubmitted'
      break
    case 'PENDING':
    case 'PENDING_APPROVAL':
    case 'PENDING_SCRUTINY':
    case 'IN_PROGRESS':
      badgeClass = 'badge-warning'
      label = status.toUpperCase() === 'PENDING' ? 'Pending' : 'Pending Review'
      break
    case 'DEFICIENT':
      badgeClass = 'badge-danger'
      label = 'Deficient'
      break
    case 'REJECTED':
      badgeClass = 'badge-danger'
      label = 'Rejected'
      break
    case 'WITHDRAWN':
      badgeClass = 'badge-secondary'
      label = 'Withdrawn'
      break
    case 'DOWN':
      badgeClass = 'badge-danger'
      label = 'Offline'
      break
    case 'SUSPENDED':
      badgeClass = 'badge-danger'
      label = 'Suspended'
      break
    case 'INACTIVE':
      badgeClass = 'badge-danger'
      label = 'Inactive'
      break
    case 'DEGRADED':
      badgeClass = 'badge-warning'
      label = 'Degraded'
      break
    default:
      badgeClass = 'badge-info'
      label = status.replace(/_/g, ' ')
  }

  return (
    <span className={`badge ${badgeClass}`}>
      <span
        style={{
          width: '6px',
          height: '6px',
          borderRadius: '50%',
          backgroundColor: 'currentColor',
          display: 'inline-block',
        }}
      />
      {label}
    </span>
  )
}
