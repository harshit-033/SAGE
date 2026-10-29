package org.aicte.sage.modules.documents.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aicte.sage.common.audit.AuditService;
import org.aicte.sage.common.exception.ResourceNotFoundException;
import org.aicte.sage.common.security.AuthoritativeAuthorizationService;
import org.aicte.sage.common.security.UserPrincipal;
import org.aicte.sage.modules.applications.model.Application;
import org.aicte.sage.modules.applications.repository.ApplicationRepository;
import org.aicte.sage.modules.documents.dto.DocumentDto;
import org.aicte.sage.modules.documents.dto.ExtractedFieldDto;
import org.aicte.sage.modules.documents.model.Document;
import org.aicte.sage.modules.documents.model.DocumentExtractedField;
import org.aicte.sage.modules.documents.model.DocumentType;
import org.aicte.sage.modules.documents.model.OcrStatus;
import org.aicte.sage.modules.documents.repository.DocumentExtractedFieldRepository;
import org.aicte.sage.modules.documents.repository.DocumentRepository;
import org.aicte.sage.modules.users.entity.User;
import org.aicte.sage.modules.users.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
@Transactional
public class DocumentService {

    private static final Logger log = LoggerFactory.getLogger(DocumentService.class);

    private final DocumentRepository documentRepository;
    private final DocumentExtractedFieldRepository extractedFieldRepository;
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final DocumentStorageService storageService;
    private final DocumentOcrClient ocrClient;
    private final AuthoritativeAuthorizationService authService;
    private final AuditService auditService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DocumentService(
            DocumentRepository documentRepository,
            DocumentExtractedFieldRepository extractedFieldRepository,
            ApplicationRepository applicationRepository,
            UserRepository userRepository,
            DocumentStorageService storageService,
            DocumentOcrClient ocrClient,
            AuthoritativeAuthorizationService authService,
            AuditService auditService
    ) {
        this.documentRepository = documentRepository;
        this.extractedFieldRepository = extractedFieldRepository;
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
        this.storageService = storageService;
        this.ocrClient = ocrClient;
        this.authService = authService;
        this.auditService = auditService;
    }

    public DocumentDto uploadDocument(
            UUID applicationId,
            DocumentType documentType,
            MultipartFile file,
            UserPrincipal principal
    ) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application with ID '" + applicationId + "' not found"));

        authService.checkAccessToInstitution(principal, application.getInstitution().getId(), "DOCUMENT_UPLOAD");

        User uploader = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User '" + principal.getId() + "' not found"));

        // 1. Store file securely
        DocumentStorageService.StoredFileMetadata meta = storageService.storeFile(file);

        // 2. Persist Document record
        Document doc = new Document();
        doc.setApplication(application);
        doc.setInstitution(application.getInstitution());
        doc.setDocumentType(documentType);
        doc.setOriginalFilename(meta.originalFilename());
        doc.setStoredFilename(meta.storedFilename());
        doc.setFilePath(meta.filePath());
        doc.setContentType(meta.contentType());
        doc.setFileSizeBytes(meta.fileSizeBytes());
        doc.setSha256Hash(meta.sha256Hash());
        doc.setOcrStatus(OcrStatus.PROCESSING);
        doc.setUploadedBy(uploader);

        Document savedDoc = documentRepository.save(doc);

        // 3. Trigger OCR Processing via Indra OCR (with fallback)
        executeOcrProcessing(savedDoc);

        // 4. Audit Log
        auditService.recordEvent(
                "DOCUMENT_UPLOADED",
                principal.getUsername(),
                application.getInstitution().getId(),
                "DOCUMENT",
                savedDoc.getId().toString(),
                "UPLOAD",
                null,
                "Document " + savedDoc.getOriginalFilename() + " (" + documentType.name() + ") uploaded for application " + application.getApplicationNumber()
        );

        return getDocumentDtoWithFields(savedDoc);
    }

    public DocumentDto reprocessOcr(UUID documentId, UserPrincipal principal) {
        Document doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document with ID '" + documentId + "' not found"));

        authService.checkAccessToInstitution(principal, doc.getInstitution().getId(), "DOCUMENT_REPROCESS_OCR");

        // Clear existing extracted fields
        extractedFieldRepository.deleteByDocumentId(documentId);

        doc.setOcrStatus(OcrStatus.PROCESSING);
        executeOcrProcessing(doc);

        return getDocumentDtoWithFields(doc);
    }

    private void executeOcrProcessing(Document doc) {
        try {
            Optional<DocumentOcrClient.OcrResponse> ocrResultOpt = ocrClient.processDocument(
                    doc.getFilePath(),
                    doc.getDocumentType().name(),
                    doc.getId().toString()
            );

            if (ocrResultOpt.isPresent() && ocrResultOpt.get().success()) {
                DocumentOcrClient.OcrResponse ocr = ocrResultOpt.get();
                doc.setOcrEngine(ocr.engine());
                doc.setOverallConfidence(ocr.overallConfidence());
                doc.setUncertain(ocr.uncertain());
                doc.setOcrStatus(ocr.uncertain() ? OcrStatus.UNCERTAIN : OcrStatus.COMPLETED);

                // Save extracted fields
                if (ocr.extractedFields() != null) {
                    for (Map.Entry<String, DocumentOcrClient.ExtractedFieldResponse> entry : ocr.extractedFields().entrySet()) {
                        DocumentOcrClient.ExtractedFieldResponse f = entry.getValue();
                        DocumentExtractedField field = new DocumentExtractedField();
                        field.setDocument(doc);
                        field.setFieldKey(f.fieldKey());
                        field.setExtractedValue(f.extractedValue() != null ? f.extractedValue() : "");
                        field.setNormalizedValue(f.normalizedValue());
                        field.setConfidenceScore(f.confidenceScore() != null ? f.confidenceScore() : 1.0);
                        field.setUncertain(f.uncertain());
                        field.setSourcePage(f.sourcePage() != null ? f.sourcePage() : 1);
                        field.setRawTextSnippet(f.rawTextSnippet());
                        if (f.boundingBox() != null) {
                            try {
                                field.setBoundingBoxJson(objectMapper.writeValueAsString(f.boundingBox()));
                            } catch (JsonProcessingException ignored) {}
                        }
                        extractedFieldRepository.save(field);
                    }
                }
            } else {
                doc.setOcrStatus(OcrStatus.FAILED);
            }
        } catch (Exception e) {
            log.error("OCR execution exception for document {}: {}", doc.getId(), e.getMessage());
            doc.setOcrStatus(OcrStatus.FAILED);
        }
        documentRepository.save(doc);
    }

    @Transactional(readOnly = true)
    public List<DocumentDto> listDocumentsForApplication(UUID applicationId, UserPrincipal principal) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application with ID '" + applicationId + "' not found"));

        authService.checkAccessToInstitution(principal, application.getInstitution().getId(), "LIST_DOCUMENTS");

        List<Document> docs = documentRepository.findByApplicationIdOrderByCreatedAtDesc(applicationId);
        return docs.stream().map(this::getDocumentDtoWithFields).toList();
    }

    @Transactional(readOnly = true)
    public DocumentDto getDocument(UUID documentId, UserPrincipal principal) {
        Document doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document with ID '" + documentId + "' not found"));

        authService.checkAccessToInstitution(principal, doc.getInstitution().getId(), "GET_DOCUMENT");
        return getDocumentDtoWithFields(doc);
    }

    private DocumentDto getDocumentDtoWithFields(Document doc) {
        DocumentDto dto = DocumentDto.fromEntity(doc);
        List<DocumentExtractedField> fields = extractedFieldRepository.findByDocumentId(doc.getId());
        dto.setExtractedFields(fields.stream().map(ExtractedFieldDto::fromEntity).toList());
        return dto;
    }
}
