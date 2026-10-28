import React from 'react'

interface PaginationProps {
  currentPage: number
  totalPages: number
  totalElements: number
  pageSize: number
  onPageChange: (newPage: number) => void
}

export const Pagination: React.FC<PaginationProps> = ({
  currentPage,
  totalPages,
  totalElements,
  pageSize,
  onPageChange,
}) => {
  if (totalElements <= 0) {
    return null
  }

  const startIdx = currentPage * pageSize + 1
  const endIdx = Math.min((currentPage + 1) * pageSize, totalElements)

  return (
    <nav className="gov-pagination" aria-label="Pagination Navigation">
      <div style={{ fontWeight: 500 }}>
        Showing <strong>{startIdx}</strong> to <strong>{endIdx}</strong> of <strong>{totalElements}</strong> records
      </div>
      <div className="gov-pagination-pages">
        <button
          type="button"
          className="pagination-btn"
          disabled={currentPage <= 0}
          onClick={() => onPageChange(0)}
          aria-label="First page"
        >
          &laquo; First
        </button>
        <button
          type="button"
          className="pagination-btn"
          disabled={currentPage <= 0}
          onClick={() => onPageChange(currentPage - 1)}
          aria-label="Previous page"
        >
          &lsaquo; Prev
        </button>

        <span style={{ margin: '0 0.5rem', fontWeight: 600 }}>
          Page {currentPage + 1} of {Math.max(1, totalPages)}
        </span>

        <button
          type="button"
          className="pagination-btn"
          disabled={currentPage >= totalPages - 1}
          onClick={() => onPageChange(currentPage + 1)}
          aria-label="Next page"
        >
          Next &rsaquo;
        </button>
        <button
          type="button"
          className="pagination-btn"
          disabled={currentPage >= totalPages - 1}
          onClick={() => onPageChange(totalPages - 1)}
          aria-label="Last page"
        >
          Last &raquo;
        </button>
      </div>
    </nav>
  )
}
