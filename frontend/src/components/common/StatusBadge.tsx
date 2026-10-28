import React from 'react'

interface StatusBadgeProps {
  status: string
}

export const StatusBadge: React.FC<StatusBadgeProps> = ({ status }) => {
  let badgeClass: string
  let label = status

  switch (status.toUpperCase()) {
    case 'ACTIVE':
    case 'UP':
    case 'APPROVED':
      badgeClass = 'badge-success'
      label = status === 'UP' ? 'Operational' : 'Active'
      break
    case 'PENDING_APPROVAL':
    case 'PENDING_SCRUTINY':
    case 'IN_PROGRESS':
      badgeClass = 'badge-warning'
      label = 'Pending Review'
      break
    case 'DEFICIENT':
    case 'DOWN':
    case 'REJECTED':
      badgeClass = 'badge-danger'
      label = status === 'DOWN' ? 'Offline' : 'Deficient'
      break
    case 'DEGRADED':
      badgeClass = 'badge-warning'
      label = 'Degraded'
      break
    default:
      badgeClass = 'badge-info'
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
