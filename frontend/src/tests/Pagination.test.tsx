import { render, screen, fireEvent } from '@testing-library/react'
import { describe, it, expect, vi } from 'vitest'
import { Pagination } from '../components/common/Pagination'

describe('Pagination Component', () => {
  it('renders correct record range and navigation controls', () => {
    const onPageChange = vi.fn()
    render(
      <Pagination
        currentPage={0}
        totalPages={3}
        totalElements={25}
        pageSize={10}
        onPageChange={onPageChange}
      />
    )

    expect(screen.getByText(/Showing/i)).toHaveTextContent('Showing 1 to 10 of 25 records')
    expect(screen.getByText(/Page 1 of 3/i)).toBeInTheDocument()

    const prevBtn = screen.getByRole('button', { name: /Previous page/i })
    const nextBtn = screen.getByRole('button', { name: /Next page/i })

    expect(prevBtn).toBeDisabled()
    expect(nextBtn).not.toBeDisabled()

    fireEvent.click(nextBtn)
    expect(onPageChange).toHaveBeenCalledWith(1)
  })

  it('disables next button on the final page', () => {
    const onPageChange = vi.fn()
    render(
      <Pagination
        currentPage={2}
        totalPages={3}
        totalElements={25}
        pageSize={10}
        onPageChange={onPageChange}
      />
    )

    expect(screen.getByText(/Showing/i)).toHaveTextContent('Showing 21 to 25 of 25 records')
    const nextBtn = screen.getByRole('button', { name: /Next page/i })
    expect(nextBtn).toBeDisabled()
  })

  it('returns null if totalElements is 0', () => {
    const { container } = render(
      <Pagination
        currentPage={0}
        totalPages={0}
        totalElements={0}
        pageSize={10}
        onPageChange={vi.fn()}
      />
    )
    expect(container.firstChild).toBeNull()
  })
})
