package org.aicte.sage.modules.documents.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.aicte.sage.common.api.ApiResponse;
import org.aicte.sage.common.api.PageResponse;
import org.aicte.sage.common.exception.ResourceNotFoundException;
import org.aicte.sage.common.security.AuthoritativeAuthorizationService;
import org.aicte.sage.common.security.SecurityUtils;
import org.aicte.sage.common.security.UserPrincipal;
import org.aicte.sage.modules.documents.dto.*;
import org.aicte.sage.modules.documents.model.*;
import org.aicte.sage.modules.documents.repository.DocumentRepository;
import org.aicte.sage.modules.documents.service.DocumentComplianceService;
import org.aicte.sage.modules.documents.service.DocumentStorageService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/documents")
@Tag(name = "Document Compliance & Tracking", description = "Institution document registry, continuous compliance monitoring, and immutable versioning")
public class DocumentComplianceController {

    private final DocumentComplianceService complianceService;
    private final DocumentRepository documentRepository;
    private final DocumentStorageService storageService;
    private final AuthoritativeAuthorizationService authService;

    public DocumentComplianceController(
            DocumentComplianceService complianceService,
            DocumentRepository documentRepository,
            DocumentStorageService storageService,
            AuthoritativeAuthorizationService authService
    ) {
        this.complianceService = complianceService;
        this.documentRepository = documentRepository;
        this.storageService = storageService;
        this.authService = authService;
    }

    private UserPrincipal getCurrentPrincipal() {
        return SecurityUtils.getCurrentUserPrincipal()
                .orElseThrow(() -> new IllegalStateException("Unauthenticated request"));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('DOCUMENT_READ')")
    @Operation(summary = "Search institution document registry", description = "Filter documents by institution, application, category, verification and compliance status")
    public ResponseEntity<ApiResponse<PageResponse<DocumentDto>>> listDocuments(
            @RequestParam(required = false) UUID institutionId,
            @RequestParam(required = false) UUID applicationId,
            @RequestParam(required = false) DocumentCategory category,
            @RequestParam(required = false) DocumentVerificationStatus verificationStatus,
            @RequestParam(required = false) DocumentComplianceStatus complianceStatus,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PageResponse<DocumentDto> res = complianceService.listDocuments(
                institutionId, applicationId, category, verificationStatus, complianceStatus, search, page, size, getCurrentPrincipal()
        );
        return ResponseEntity.ok(ApiResponse.ok(res));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('DOCUMENT_READ')")
    @Operation(summary = "Get document detail", description = "Retrieves complete document metadata, extracted fields, findings, corrective actions, and version history")
    public ResponseEntity<ApiResponse<DocumentDetailResponse>> getDocumentDetail(
            @PathVariable UUID id
    ) {
        DocumentDetailResponse res = complianceService.getDocumentDetail(id, getCurrentPrincipal());
        return ResponseEntity.ok(ApiResponse.ok(res));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('DOCUMENT_UPLOAD')")
    @Operation(summary = "Upload institution document", description = "Uploads a new document to the institutional compliance registry")
    public ResponseEntity<ApiResponse<DocumentDto>> uploadDocument(
            @RequestParam(required = false) UUID institutionId,
            @RequestParam(required = false) UUID applicationId,
            @RequestParam(required = false) String documentName,
            @RequestParam(required = false) DocumentType documentType,
            @RequestParam(required = false) DocumentCategory documentCategory,
            @RequestParam(required = false) Instant validFrom,
            @RequestParam(required = false) Instant expiryDate,
            @RequestParam("file") MultipartFile file
    ) {
        DocumentDto dto = complianceService.uploadInstitutionDocument(
                institutionId, applicationId, documentName, documentType, documentCategory, validFrom, expiryDate, file, getCurrentPrincipal()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Document uploaded successfully", dto));
    }

    @PostMapping(value = "/{id}/replace", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('DOCUMENT_UPLOAD')")
    @Operation(summary = "Upload replacement document (new version)", description = "Creates a new immutable document version linked to the prior version, preserving audit trail")
    public ResponseEntity<ApiResponse<DocumentDto>> replaceDocument(
            @PathVariable UUID id,
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String replacementNotes
    ) {
        DocumentDto dto = complianceService.replaceDocument(id, file, replacementNotes, getCurrentPrincipal());
        return ResponseEntity.ok(ApiResponse.ok("Document replacement uploaded successfully", dto));
    }

    @PostMapping("/{id}/clarify")
    @PreAuthorize("hasAuthority('DOCUMENT_UPLOAD')")
    @Operation(summary = "Submit clarification for document", description = "Provides institutional response to scrutiny notes or officer comments")
    public ResponseEntity<ApiResponse<DocumentDto>> submitClarification(
            @PathVariable UUID id,
            @Valid @RequestBody SubmitClarificationRequest request
    ) {
        DocumentDto dto = complianceService.submitClarification(id, request, getCurrentPrincipal());
        return ResponseEntity.ok(ApiResponse.ok("Clarification submitted successfully", dto));
    }

    @PostMapping("/{id}/review")
    @PreAuthorize("hasAnyRole('ROLE_AICTE_ADMIN', 'ROLE_REGIONAL_OFFICER', 'ROLE_COMPLIANCE_OFFICER', 'ROLE_SCRUTINY_OFFICER')")
    @Operation(summary = "Officer review and verification", description = "Updates document verification and compliance status with authoritative officer comments")
    public ResponseEntity<ApiResponse<DocumentDto>> reviewDocument(
            @PathVariable UUID id,
            @Valid @RequestBody ReviewDocumentRequest request
    ) {
        DocumentDto dto = complianceService.reviewDocument(id, request, getCurrentPrincipal());
        return ResponseEntity.ok(ApiResponse.ok("Document review updated successfully", dto));
    }

    @GetMapping("/compliance-summary")
    @PreAuthorize("hasAuthority('DOCUMENT_READ')")
    @Operation(summary = "Get institution compliance summary", description = "Calculates authoritative document metrics: total, verified, pending, expiring soon, expired, deficient, and pending actions")
    public ResponseEntity<ApiResponse<DocumentComplianceSummaryDto>> getComplianceSummary(
            @RequestParam(required = false) UUID institutionId
    ) {
        DocumentComplianceSummaryDto summary = complianceService.getComplianceSummary(institutionId, getCurrentPrincipal());
        return ResponseEntity.ok(ApiResponse.ok(summary));
    }

    @GetMapping("/{id}/preview")
    @PreAuthorize("hasAuthority('DOCUMENT_READ')")
    @Operation(summary = "Secure document preview", description = "Streams document content inline with strict multi-tenant authorization")
    public ResponseEntity<Resource> previewDocument(@PathVariable UUID id) {
        Document doc = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document with ID '" + id + "' not found"));
        authService.checkAccessToInstitution(getCurrentPrincipal(), doc.getInstitution().getId(), "PREVIEW_DOCUMENT");

        Resource resource = storageService.loadFileAsResource(doc.getFilePath());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(doc.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + doc.getOriginalFilename() + "\"")
                .body(resource);
    }

    @GetMapping("/{id}/download")
    @PreAuthorize("hasAuthority('DOCUMENT_READ')")
    @Operation(summary = "Secure document download", description = "Downloads original document file with strict multi-tenant authorization")
    public ResponseEntity<Resource> downloadDocument(@PathVariable UUID id) {
        Document doc = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document with ID '" + id + "' not found"));
        authService.checkAccessToInstitution(getCurrentPrincipal(), doc.getInstitution().getId(), "DOWNLOAD_DOCUMENT");

        Resource resource = storageService.loadFileAsResource(doc.getFilePath());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(doc.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + doc.getOriginalFilename() + "\"")
                .body(resource);
    }
}
