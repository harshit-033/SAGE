package org.aicte.sage.modules.documents.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.aicte.sage.common.api.ApiResponse;
import org.aicte.sage.common.security.SecurityUtils;
import org.aicte.sage.common.security.UserPrincipal;
import org.aicte.sage.modules.documents.dto.DocumentDto;
import org.aicte.sage.modules.documents.model.DocumentType;
import org.aicte.sage.modules.documents.service.DocumentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/applications/{applicationId}/documents")
@Tag(name = "Document Management & OCR Scrutiny", description = "Upload, verify, and OCR regulatory compliance documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    private UserPrincipal getCurrentPrincipal() {
        return SecurityUtils.getCurrentUserPrincipal()
                .orElseThrow(() -> new IllegalStateException("Unauthenticated request"));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('DOCUMENT_UPLOAD')")
    @Operation(summary = "Upload compliance document", description = "Uploads and triggers automated Indra OCR analysis with field extraction")
    public ResponseEntity<ApiResponse<DocumentDto>> uploadDocument(
            @PathVariable UUID applicationId,
            @RequestParam("documentType") DocumentType documentType,
            @RequestParam("file") MultipartFile file
    ) {
        DocumentDto dto = documentService.uploadDocument(applicationId, documentType, file, getCurrentPrincipal());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Document uploaded and processed successfully", dto));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('DOCUMENT_READ')")
    @Operation(summary = "List documents for application", description = "Returns all uploaded documents with OCR results and extracted fields")
    public ResponseEntity<ApiResponse<List<DocumentDto>>> listDocuments(
            @PathVariable UUID applicationId
    ) {
        List<DocumentDto> docs = documentService.listDocumentsForApplication(applicationId, getCurrentPrincipal());
        return ResponseEntity.ok(ApiResponse.ok(docs));
    }

    @GetMapping("/{documentId}")
    @PreAuthorize("hasAuthority('DOCUMENT_READ')")
    @Operation(summary = "Get document details", description = "Retrieves document metadata, OCR lines, and extracted fields")
    public ResponseEntity<ApiResponse<DocumentDto>> getDocument(
            @PathVariable UUID applicationId,
            @PathVariable UUID documentId
    ) {
        DocumentDto doc = documentService.getDocument(documentId, getCurrentPrincipal());
        return ResponseEntity.ok(ApiResponse.ok(doc));
    }

    @PostMapping("/{documentId}/ocr")
    @PreAuthorize("hasAuthority('SCRUTINY_EXECUTE')")
    @Operation(summary = "Trigger OCR reprocessing", description = "Reruns Indra OCR analysis and re-extracts structured fields")
    public ResponseEntity<ApiResponse<DocumentDto>> reprocessOcr(
            @PathVariable UUID applicationId,
            @PathVariable UUID documentId
    ) {
        DocumentDto doc = documentService.reprocessOcr(documentId, getCurrentPrincipal());
        return ResponseEntity.ok(ApiResponse.ok("OCR reprocessing completed", doc));
    }
}
