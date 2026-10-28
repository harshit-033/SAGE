import React from 'react'
import { Link } from 'react-router-dom'

export interface BreadcrumbItem {
  label: string
  to?: string
}

interface BreadcrumbsProps {
  items: BreadcrumbItem[]
}

export const Breadcrumbs: React.FC<BreadcrumbsProps> = ({ items }) => {
  return (
    <nav aria-label="Breadcrumb" className="gov-breadcrumbs">
      <Link to="/dashboard">Dashboard</Link>
      {items.map((item, index) => {
        const isLast = index === items.length - 1
        return (
          <React.Fragment key={index}>
            <span className="gov-breadcrumbs-separator" aria-hidden="true">/</span>
            {isLast || !item.to ? (
              <span aria-current="page" style={{ color: 'var(--text-primary)', fontWeight: 600 }}>
                {item.label}
              </span>
            ) : (
              <Link to={item.to}>{item.label}</Link>
            )}
          </React.Fragment>
        )
      })}
    </nav>
  )
}
