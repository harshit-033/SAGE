import { describe, it, expect } from 'vitest'
import { render, screen } from '@testing-library/react'
import { StatusBadge } from '../components/common/StatusBadge'
import { documentComplianceApi } from '../api/documentComplianceApi'
import { applicationsApi } from '../api/applicationsApi'

describe('Document Compliance and Application Workflows Unit Tests', () => {
  it('exposes all required DocumentCompliance API methods', () => {
    expect(typeof documentComplianceApi.listDocuments).toBe('function')
    expect(typeof documentComplianceApi.getDocumentDetail).toBe('function')
    expect(typeof documentComplianceApi.uploadInstitutionDocument).toBe('function')
    expect(typeof documentComplianceApi.replaceDocument).toBe('function')
    expect(typeof documentComplianceApi.submitClarification).toBe('function')
    expect(typeof documentComplianceApi.reviewDocument).toBe('function')
    expect(typeof documentComplianceApi.getComplianceSummary).toBe('function')
    expect(typeof documentComplianceApi.listCorrectiveActions).toBe('function')
    expect(typeof documentComplianceApi.getCorrectiveAction).toBe('function')
    expect(typeof documentComplianceApi.respondCorrectiveAction).toBe('function')
    expect(typeof documentComplianceApi.resolveCorrectiveAction).toBe('function')
    expect(typeof documentComplianceApi.getPreviewUrl).toBe('function')
    expect(typeof documentComplianceApi.getDownloadUrl).toBe('function')
  })

  it('exposes Course and Branch details API methods on applicationsApi', () => {
    expect(typeof applicationsApi.saveCourseDetails).toBe('function')
    expect(typeof applicationsApi.getCourseDetails).toBe('function')
    expect(typeof applicationsApi.saveBranchDetails).toBe('function')
    expect(typeof applicationsApi.getBranchDetails).toBe('function')
  })

  it('renders status badges for compliance and workflow states', () => {
    const { unmount } = render(<StatusBadge status="PENDING_SCRUTINY" />)
    expect(screen.getByText('Pending Review')).toBeInTheDocument()
    unmount()

    const { unmount: unmount2 } = render(<StatusBadge status="DEFICIENT" />)
    expect(screen.getByText('Deficient')).toBeInTheDocument()
    unmount2()

    const { unmount: unmount3 } = render(<StatusBadge status="APPROVED" />)
    expect(screen.getByText('Active')).toBeInTheDocument()
    unmount3()
  })

  it('constructs correct preview and download URLs with base prefix', () => {
    const docId = 'test-doc-uuid-123'
    const previewUrl = documentComplianceApi.getPreviewUrl(docId)
    const downloadUrl = documentComplianceApi.getDownloadUrl(docId)

    expect(previewUrl).toBe('/api/v1/documents/test-doc-uuid-123/preview')
    expect(downloadUrl).toBe('/api/v1/documents/test-doc-uuid-123/download')
  })
})
