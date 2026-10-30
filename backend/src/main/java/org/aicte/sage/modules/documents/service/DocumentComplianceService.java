package org.aicte.sage.modules.documents.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aicte.sage.common.api.PageResponse;
import org.aicte.sage.common.api.PaginationUtils;
import org.aicte.sage.common.audit.AuditService;
import org.aicte.sage.common.exception.ResourceNotFoundException;
import org.aicte.sage.common.security.AuthoritativeAuthorizationService;
import org.aicte.sage.common.security.UserPrincipal;
import org.aicte.sage.modules.applications.model.Application;
import org.aicte.sage.modules.applications.repository.ApplicationRepository;
import org.aicte.sage.modules.documents.dto.*;
import org.aicte.sage.modules.documents.model.*;
import org.aicte.sage.modules.documents.repository.DocumentCorrectiveActionRepository;
import org.aicte.sage.modules.documents.repository.DocumentExtractedFieldRepository;
import org.aicte.sage.modules.documents.repository.DocumentRepository;
import org.aicte.sage.modules.institutions.entity.Institution;
import org.aicte.sage.modules.institutions.repository.InstitutionRepository;
import org.aicte.sage.modules.notifications.entity.AlertCategory;
import org.aicte.sage.modules.notifications.entity.AlertPriority;
import org.aicte.sage.modules.notifications.entity.AlertSeverity;
import org.aicte.sage.modules.notifications.service.AlertService;
import org.aicte.sage.modules.scrutiny.dto.ScrutinyFindingDto;
import org.aicte.sage.modules.scrutiny.model.ScrutinyFinding;
import org.aicte.sage.modules.scrutiny.repository.ScrutinyFindingRepository;
import org.aicte.sage.modules.users.entity.User;
import org.aicte.sage.modules.users.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@Transactional
public class DocumentComplianceService {

    private static final Logger log = LoggerFactory.getLogger(DocumentComplianceService.class);

    private final DocumentRepository documentRepository;
    private final DocumentCorrectiveActionRepository correctiveActionRepository;
    private final DocumentExtractedFieldRepository extractedFieldRepository;
    private final ScrutinyFindingRepository scrutinyFindingRepository;
    private final ApplicationRepository applicationRepository;
    private final InstitutionRepository institutionRepository;
    private final UserRepository userRepository;
    private final DocumentStorageService storageService;
    private final DocumentOcrClient ocrClient;
    private final AuthoritativeAuthorizationService authService;
    private final AuditService auditService;
    private final AlertService alertService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final Set<String> ALLOWED_DOC_SORT_FIELDS = Set.of("createdAt", "updatedAt", "originalFilename", "expiryDate", "documentCategory");
    private static final Set<String> ALLOWED_ACTION_SORT_FIELDS = Set.of("createdAt", "updatedAt", "priority", "dueDate", "status");

    public DocumentComplianceService(
            DocumentRepository documentRepository,
            DocumentCorrectiveActionRepository correctiveActionRepository,
            DocumentExtractedFieldRepository extractedFieldRepository,
            ScrutinyFindingRepository scrutinyFindingRepository,
            ApplicationRepository applicationRepository,
            InstitutionRepository institutionRepository,
            UserRepository userRepository,
            DocumentStorageService storageService,
            DocumentOcrClient ocrClient,
            AuthoritativeAuthorizationService authService,
            AuditService auditService,
            AlertService alertService
    ) {
        this.documentRepository = documentRepository;
        this.correctiveActionRepository = correctiveActionRepository;
        this.extractedFieldRepository = extractedFieldRepository;
        this.scrutinyFindingRepository = scrutinyFindingRepository;
        this.applicationRepository = applicationRepository;
        this.institutionRepository = institutionRepository;
        this.userRepository = userRepository;
        this.storageService = storageService;
        this.ocrClient = ocrClient;
        this.authService = authService;
        this.auditService = auditService;
        this.alertService = alertService;
    }

    // =========================================================================
    // 1. Document Registry & Search
    // =========================================================================

    @Transactional(readOnly = true)
    public PageResponse<DocumentDto> listDocuments(
            UUID requestedInstitutionId,
            UUID applicationId,
            DocumentCategory category,
            DocumentVerificationStatus verificationStatus,
            DocumentComplianceStatus complianceStatus,
            String search,
            int page,
            int size,
            UserPrincipal principal
    ) {
        UUID effectiveInstId = requestedInstitutionId;
        if (principal.hasRole("ROLE_INSTITUTION_OFFICER") && !principal.hasRole("ROLE_AICTE_ADMIN") && !principal.hasRole("ROLE_REGIONAL_OFFICER")) {
            effectiveInstId = principal.getAuthorizedInstitutionIds().stream().findFirst().orElse(null);
        } else if (effectiveInstId != null) {
            authService.checkAccessToInstitution(principal, effectiveInstId, "LIST_DOCUMENTS");
        }

        Pageable pageable = PaginationUtils.validateAndBuildPageable(page, size, "createdAt", "desc", ALLOWED_DOC_SORT_FIELDS, "createdAt");
        Page<Document> docPage = documentRepository.searchDocuments(
                effectiveInstId,
                applicationId,
                category,
                verificationStatus,
                complianceStatus,
                search != null && !search.isBlank() ? search.trim() : null,
                pageable
        );

        List<DocumentDto> dtos = docPage.getContent().stream().map(this::toEnrichedDto).toList();
        return PageResponse.from(docPage, dtos);
    }

    @Transactional(readOnly = true)
    public DocumentDetailResponse getDocumentDetail(UUID documentId, UserPrincipal principal) {
        Document doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document with ID '" + documentId + "' not found"));

        authService.checkAccessToInstitution(principal, doc.getInstitution().getId(), "GET_DOCUMENT_DETAIL");

        DocumentDto docDto = toEnrichedDto(doc);
        List<DocumentExtractedField> fields = extractedFieldRepository.findByDocumentId(doc.getId());
        List<ExtractedFieldDto> fieldDtos = fields.stream().map(ExtractedFieldDto::fromEntity).toList();

        // Trace version hierarchy
        UUID rootId = doc.getRootDocument() != null ? doc.getRootDocument().getId() : doc.getId();
        List<Document> versionEntities = documentRepository.findByRootDocumentIdOrderByVersionNumberDesc(rootId);
        if (versionEntities.isEmpty()) {
            versionEntities = List.of(doc);
        }
        List<DocumentDto> versionDtos = versionEntities.stream().map(this::toEnrichedDto).toList();

        // Findings for this document or versions in same line
        List<UUID> versionDocIds = versionEntities.stream().map(Document::getId).toList();
        List<ScrutinyFinding> findings = scrutinyFindingRepository.findByDocumentIdIn(versionDocIds);
        List<ScrutinyFindingDto> findingDtos = findings.stream().map(ScrutinyFindingDto::fromEntity).toList();

        // Corrective actions
        List<DocumentCorrectiveAction> actions = correctiveActionRepository.findByDocumentId(doc.getId());
        List<DocumentCorrectiveActionDto> actionDtos = actions.stream().map(DocumentCorrectiveActionDto::fromEntity).toList();

        return new DocumentDetailResponse(docDto, fieldDtos, findingDtos, actionDtos, versionDtos);
    }

    // =========================================================================
    // 2. Upload Institution Document
    // =========================================================================

    public DocumentDto uploadInstitutionDocument(
            UUID institutionId,
            UUID applicationId,
            String documentName,
            DocumentType documentType,
            DocumentCategory documentCategory,
            Instant validFrom,
            Instant expiryDate,
            MultipartFile file,
            UserPrincipal principal
    ) {
        UUID effectiveInstId = institutionId;
        if (principal.hasRole("ROLE_INSTITUTION_OFFICER") && !principal.hasRole("ROLE_AICTE_ADMIN") && !principal.hasRole("ROLE_REGIONAL_OFFICER")) {
            effectiveInstId = principal.getAuthorizedInstitutionIds().stream().findFirst()
                    .orElseThrow(() -> new AccessDeniedException("No institution bound to user"));
        } else {
            authService.checkAccessToInstitution(principal, effectiveInstId, "DOCUMENT_UPLOAD");
        }

        final UUID targetInstId = effectiveInstId;
        Institution institution = institutionRepository.findById(targetInstId)
                .orElseThrow(() -> new ResourceNotFoundException("Institution '" + targetInstId + "' not found"));

        Application application = null;
        if (applicationId != null) {
            application = applicationRepository.findById(applicationId)
                    .orElseThrow(() -> new ResourceNotFoundException("Application '" + applicationId + "' not found"));
        }

        User uploader = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User '" + principal.getId() + "' not found"));

        // Store file securely
        DocumentStorageService.StoredFileMetadata meta = storageService.storeFile(file);

        Document doc = new Document();
        doc.setInstitution(institution);
        doc.setApplication(application);
        doc.setDocumentName(documentName != null && !documentName.isBlank() ? documentName : meta.originalFilename());
        doc.setDocumentType(documentType != null ? documentType : DocumentType.OTHER_SUPPORTING);
        doc.setDocumentCategory(documentCategory != null ? documentCategory : DocumentCategory.GENERAL);
        doc.setOriginalFilename(meta.originalFilename());
        doc.setStoredFilename(meta.storedFilename());
        doc.setFilePath(meta.filePath());
        doc.setContentType(meta.contentType());
        doc.setFileSizeBytes(meta.fileSizeBytes());
        doc.setSha256Hash(meta.sha256Hash());
        doc.setOcrStatus(OcrStatus.PROCESSING);
        doc.setUploadedBy(uploader);
        doc.setValidFrom(validFrom);
        doc.setExpiryDate(expiryDate);
        doc.setVersionNumber(1);

        // Expiry compliance state
        if (expiryDate != null) {
            Instant now = Instant.now();
            if (expiryDate.isBefore(now)) {
                doc.setComplianceStatus(DocumentComplianceStatus.EXPIRED);
            } else if (expiryDate.isBefore(now.plus(30, ChronoUnit.DAYS))) {
                doc.setComplianceStatus(DocumentComplianceStatus.EXPIRING_SOON);
            } else {
                doc.setComplianceStatus(DocumentComplianceStatus.COMPLIANT);
            }
        } else {
            doc.setComplianceStatus(DocumentComplianceStatus.COMPLIANT);
        }

        Document savedDoc = documentRepository.save(doc);
        savedDoc.setRootDocument(savedDoc);
        documentRepository.save(savedDoc);

        // Trigger OCR
        executeOcrProcessing(savedDoc);

        auditService.recordEvent(
                "DOCUMENT_UPLOADED",
                principal.getUsername(),
                institution.getId(),
                "DOCUMENT",
                savedDoc.getId().toString(),
                "UPLOAD",
                null,
                "Uploaded document '" + savedDoc.getDocumentName() + "' (Type: " + savedDoc.getDocumentType() + ")"
        );

        return toEnrichedDto(savedDoc);
    }

    // =========================================================================
    // 3. Document Replacement (Immutable Versioning)
    // =========================================================================

    public DocumentDto replaceDocument(
            UUID documentId,
            MultipartFile file,
            String replacementNotes,
            UserPrincipal principal
    ) {
        Document previousDoc = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document with ID '" + documentId + "' not found"));

        authService.checkAccessToInstitution(principal, previousDoc.getInstitution().getId(), "DOCUMENT_REPLACE");

        User uploader = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User '" + principal.getId() + "' not found"));

        DocumentStorageService.StoredFileMetadata meta = storageService.storeFile(file);

        Document rootDoc = previousDoc.getRootDocument() != null ? previousDoc.getRootDocument() : previousDoc;

        Document newDoc = new Document();
        newDoc.setInstitution(previousDoc.getInstitution());
        newDoc.setApplication(previousDoc.getApplication());
        newDoc.setDocumentName(previousDoc.getDocumentName());
        newDoc.setDocumentType(previousDoc.getDocumentType());
        newDoc.setDocumentCategory(previousDoc.getDocumentCategory());
        newDoc.setOriginalFilename(meta.originalFilename());
        newDoc.setStoredFilename(meta.storedFilename());
        newDoc.setFilePath(meta.filePath());
        newDoc.setContentType(meta.contentType());
        newDoc.setFileSizeBytes(meta.fileSizeBytes());
        newDoc.setSha256Hash(meta.sha256Hash());
        newDoc.setOcrStatus(OcrStatus.PROCESSING);
        newDoc.setUploadedBy(uploader);
        newDoc.setValidFrom(previousDoc.getValidFrom());
        newDoc.setExpiryDate(previousDoc.getExpiryDate());
        newDoc.setVersionNumber(previousDoc.getVersionNumber() + 1);
        newDoc.setPreviousVersion(previousDoc);
        newDoc.setRootDocument(rootDoc);
        newDoc.setVerificationStatus(DocumentVerificationStatus.PENDING_REVIEW);
        newDoc.setComplianceStatus(DocumentComplianceStatus.COMPLIANT);
        newDoc.setClarificationText(replacementNotes);

        Document savedNewDoc = documentRepository.save(newDoc);

        // Run OCR on replacement
        executeOcrProcessing(savedNewDoc);

        // Update pending corrective actions associated with previous version
        List<DocumentCorrectiveAction> openActions = correctiveActionRepository.findByDocumentId(previousDoc.getId());
        for (DocumentCorrectiveAction action : openActions) {
            if (action.getStatus() == CorrectiveActionStatus.OPEN || action.getStatus() == CorrectiveActionStatus.IN_PROGRESS || action.getStatus() == CorrectiveActionStatus.OVERDUE) {
                action.setStatus(CorrectiveActionStatus.SUBMITTED_FOR_REVIEW);
                action.setResolutionEvidenceDoc(savedNewDoc);
                action.setInstitutionResponse(replacementNotes != null ? replacementNotes : "Replacement document uploaded (v" + savedNewDoc.getVersionNumber() + ")");
                correctiveActionRepository.save(action);
            }
        }

        auditService.recordEvent(
                "DOCUMENT_REPLACED",
                principal.getUsername(),
                previousDoc.getInstitution().getId(),
                "DOCUMENT",
                savedNewDoc.getId().toString(),
                "REPLACE",
                previousDoc.getId().toString(),
                "Replaced document with version " + savedNewDoc.getVersionNumber() + ": " + replacementNotes
        );

        return toEnrichedDto(savedNewDoc);
    }

    // =========================================================================
    // 4. Clarifications & Officer Review
    // =========================================================================

    public DocumentDto submitClarification(UUID documentId, SubmitClarificationRequest req, UserPrincipal principal) {
        Document doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document with ID '" + documentId + "' not found"));

        authService.checkAccessToInstitution(principal, doc.getInstitution().getId(), "SUBMIT_CLARIFICATION");

        doc.setClarificationText(req.clarificationText());
        if (doc.getVerificationStatus() == DocumentVerificationStatus.REQUIRES_CLARIFICATION) {
            doc.setVerificationStatus(DocumentVerificationStatus.UNDER_REVIEW);
        }

        Document saved = documentRepository.save(doc);

        auditService.recordEvent(
                "DOCUMENT_CLARIFICATION_SUBMITTED",
                principal.getUsername(),
                doc.getInstitution().getId(),
                "DOCUMENT",
                doc.getId().toString(),
                "CLARIFICATION",
                null,
                "Clarification submitted for document " + doc.getDocumentName() + ": " + req.clarificationText()
        );

        return toEnrichedDto(saved);
    }

    public DocumentDto reviewDocument(UUID documentId, ReviewDocumentRequest req, UserPrincipal principal) {
        if (!principal.hasAnyRole("ROLE_AICTE_ADMIN", "ROLE_REGIONAL_OFFICER", "ROLE_COMPLIANCE_OFFICER", "ROLE_SCRUTINY_OFFICER")) {
            throw new AccessDeniedException("Only authorized AICTE officers can review documents");
        }

        Document doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document with ID '" + documentId + "' not found"));

        authService.checkAccessToInstitution(principal, doc.getInstitution().getId(), "REVIEW_DOCUMENT");

        doc.setVerificationStatus(req.verificationStatus());
        if (req.complianceStatus() != null) {
            doc.setComplianceStatus(req.complianceStatus());
        }
        if (req.officerComments() != null) {
            doc.setOfficerComments(req.officerComments());
        }

        Document saved = documentRepository.save(doc);

        auditService.recordEvent(
                "DOCUMENT_REVIEWED",
                principal.getUsername(),
                doc.getInstitution().getId(),
                "DOCUMENT",
                doc.getId().toString(),
                req.verificationStatus().name(),
                null,
                "Document " + doc.getDocumentName() + " reviewed as " + req.verificationStatus() + ". Comments: " + req.officerComments()
        );

        return toEnrichedDto(saved);
    }

    // =========================================================================
    // 5. Compliance Summary Metrics
    // =========================================================================

    @Transactional(readOnly = true)
    public DocumentComplianceSummaryDto getComplianceSummary(UUID requestedInstitutionId, UserPrincipal principal) {
        UUID effectiveInstId = requestedInstitutionId;
        if (principal.hasRole("ROLE_INSTITUTION_OFFICER") && !principal.hasRole("ROLE_AICTE_ADMIN") && !principal.hasRole("ROLE_REGIONAL_OFFICER")) {
            effectiveInstId = principal.getAuthorizedInstitutionIds().stream().findFirst().orElse(null);
        } else if (effectiveInstId != null) {
            authService.checkAccessToInstitution(principal, effectiveInstId, "GET_COMPLIANCE_SUMMARY");
        }

        if (effectiveInstId == null) {
            return new DocumentComplianceSummaryDto(0, 0, 0, 0, 0, 0, 0);
        }

        long total = documentRepository.countByInstitutionId(effectiveInstId);
        long verified = documentRepository.countByInstitutionIdAndVerificationStatus(effectiveInstId, DocumentVerificationStatus.VERIFIED)
                + documentRepository.countByInstitutionIdAndVerificationStatus(effectiveInstId, DocumentVerificationStatus.VERIFIED_WITH_WARNINGS);
        long pendingReview = documentRepository.countByInstitutionIdAndVerificationStatus(effectiveInstId, DocumentVerificationStatus.PENDING_REVIEW)
                + documentRepository.countByInstitutionIdAndVerificationStatus(effectiveInstId, DocumentVerificationStatus.UNDER_REVIEW)
                + documentRepository.countByInstitutionIdAndVerificationStatus(effectiveInstId, DocumentVerificationStatus.REPROCESSING);
        long expiringSoon = documentRepository.countByInstitutionIdAndComplianceStatus(effectiveInstId, DocumentComplianceStatus.EXPIRING_SOON);
        long expired = documentRepository.countByInstitutionIdAndComplianceStatus(effectiveInstId, DocumentComplianceStatus.EXPIRED);
        long deficient = documentRepository.countByInstitutionIdAndComplianceStatus(effectiveInstId, DocumentComplianceStatus.DEFICIENT);
        long pendingActions = correctiveActionRepository.countPendingActionsByInstitution(effectiveInstId);

        return new DocumentComplianceSummaryDto(total, verified, pendingReview, expiringSoon, expired, deficient, pendingActions);
    }

    // =========================================================================
    // 6. Corrective Actions Lifecycle
    // =========================================================================

    @Transactional(readOnly = true)
    public PageResponse<DocumentCorrectiveActionDto> listCorrectiveActions(
            UUID requestedInstitutionId,
            CorrectiveActionStatus status,
            int page,
            int size,
            UserPrincipal principal
    ) {
        UUID effectiveInstId = requestedInstitutionId;
        if (principal.hasRole("ROLE_INSTITUTION_OFFICER") && !principal.hasRole("ROLE_AICTE_ADMIN") && !principal.hasRole("ROLE_REGIONAL_OFFICER")) {
            effectiveInstId = principal.getAuthorizedInstitutionIds().stream().findFirst().orElse(null);
        } else if (effectiveInstId != null) {
            authService.checkAccessToInstitution(principal, effectiveInstId, "LIST_CORRECTIVE_ACTIONS");
        }

        Pageable pageable = PaginationUtils.validateAndBuildPageable(page, size, "createdAt", "desc", ALLOWED_ACTION_SORT_FIELDS, "createdAt");
        Page<DocumentCorrectiveAction> actionPage = correctiveActionRepository.searchActions(effectiveInstId, status, pageable);
        List<DocumentCorrectiveActionDto> dtos = actionPage.getContent().stream().map(DocumentCorrectiveActionDto::fromEntity).toList();

        return PageResponse.from(actionPage, dtos);
    }

    @Transactional(readOnly = true)
    public DocumentCorrectiveActionDto getCorrectiveAction(UUID actionId, UserPrincipal principal) {
        DocumentCorrectiveAction action = correctiveActionRepository.findById(actionId)
                .orElseThrow(() -> new ResourceNotFoundException("Corrective action with ID '" + actionId + "' not found"));

        authService.checkAccessToInstitution(principal, action.getInstitution().getId(), "GET_CORRECTIVE_ACTION");
        return DocumentCorrectiveActionDto.fromEntity(action);
    }

    public DocumentCorrectiveActionDto respondCorrectiveAction(
            UUID actionId,
            RespondCorrectiveActionRequest req,
            UserPrincipal principal
    ) {
        DocumentCorrectiveAction action = correctiveActionRepository.findById(actionId)
                .orElseThrow(() -> new ResourceNotFoundException("Corrective action with ID '" + actionId + "' not found"));

        authService.checkAccessToInstitution(principal, action.getInstitution().getId(), "RESPOND_CORRECTIVE_ACTION");

        action.setInstitutionResponse(req.institutionResponse());
        action.setStatus(CorrectiveActionStatus.SUBMITTED_FOR_REVIEW);

        if (req.resolutionEvidenceDocId() != null) {
            Document evidenceDoc = documentRepository.findById(req.resolutionEvidenceDocId())
                    .orElseThrow(() -> new ResourceNotFoundException("Evidence document not found"));
            action.setResolutionEvidenceDoc(evidenceDoc);
        }

        DocumentCorrectiveAction saved = correctiveActionRepository.save(action);

        auditService.recordEvent(
                "CORRECTIVE_ACTION_RESPONDED",
                principal.getUsername(),
                action.getInstitution().getId(),
                "CORRECTIVE_ACTION",
                action.getId().toString(),
                "RESPOND",
                null,
                "Institution responded to action '" + action.getActionType() + "': " + req.institutionResponse()
        );

        return DocumentCorrectiveActionDto.fromEntity(saved);
    }

    public DocumentCorrectiveActionDto resolveCorrectiveAction(
            UUID actionId,
            ResolveCorrectiveActionRequest req,
            UserPrincipal principal
    ) {
        if (!principal.hasAnyRole("ROLE_AICTE_ADMIN", "ROLE_REGIONAL_OFFICER", "ROLE_COMPLIANCE_OFFICER")) {
            throw new AccessDeniedException("Only authorized officers can resolve or reject corrective actions");
        }

        DocumentCorrectiveAction action = correctiveActionRepository.findById(actionId)
                .orElseThrow(() -> new ResourceNotFoundException("Corrective action with ID '" + actionId + "' not found"));

        authService.checkAccessToInstitution(principal, action.getInstitution().getId(), "RESOLVE_CORRECTIVE_ACTION");

        User officer = userRepository.findById(principal.getId()).orElse(null);

        action.setStatus(req.status() != null ? req.status() : CorrectiveActionStatus.RESOLVED);
        action.setOfficerNotes(req.officerNotes());
        action.setResolvedBy(officer);
        action.setResolvedAt(Instant.now());

        // Side-effect: update document compliance status if resolved
        if (action.getStatus() == CorrectiveActionStatus.RESOLVED && action.getDocument() != null) {
            Document doc = action.getDocument();
            if (doc.getComplianceStatus() == DocumentComplianceStatus.DEFICIENT || doc.getComplianceStatus() == DocumentComplianceStatus.ACTION_REQUIRED) {
                doc.setComplianceStatus(DocumentComplianceStatus.COMPLIANT);
                documentRepository.save(doc);
            }
        }

        DocumentCorrectiveAction saved = correctiveActionRepository.save(action);

        auditService.recordEvent(
                "CORRECTIVE_ACTION_RESOLVED",
                principal.getUsername(),
                action.getInstitution().getId(),
                "CORRECTIVE_ACTION",
                action.getId().toString(),
                action.getStatus().name(),
                null,
                "Corrective action marked as " + action.getStatus() + ". Notes: " + req.officerNotes()
        );

        return DocumentCorrectiveActionDto.fromEntity(saved);
    }

    // =========================================================================
    // 7. Continuous Expiry Monitoring Sweep
    // =========================================================================

    public int sweepExpiringDocuments() {
        Instant now = Instant.now();
        Instant sixtyDays = now.plus(60, ChronoUnit.DAYS);
        Instant thirtyDays = now.plus(30, ChronoUnit.DAYS);
        Instant sevenDays = now.plus(7, ChronoUnit.DAYS);

        int processed = 0;

        // 1. Mark already expired documents
        List<Document> expiredDocs = documentRepository.findExpiredDocuments(now);
        for (Document doc : expiredDocs) {
            doc.setComplianceStatus(DocumentComplianceStatus.EXPIRED);
            documentRepository.save(doc);

            // Create high priority alert
            alertService.createAlert(
                    "ALERT_DOC_EXPIRED_" + doc.getId(),
                    "CRITICAL: Document Expired (" + doc.getDocumentName() + ")",
                    "Document '" + doc.getDocumentName() + "' (" + doc.getDocumentType() + ") expired on " + doc.getExpiryDate() + ". Immediate replacement upload required.",
                    AlertCategory.COMPLIANCE,
                    AlertSeverity.CRITICAL,
                    AlertPriority.URGENT,
                    "DOCUMENT",
                    doc.getId(),
                    doc.getInstitution().getId(),
                    null,
                    "ROLE_INSTITUTION_OFFICER",
                    doc.getInstitution().getState()
            );

            // Ensure corrective action exists
            ensureCorrectiveAction(doc, "DOCUMENT_EXPIRED", "Document has expired. Upload renewal or replacement certificate.", "CRITICAL", now.plus(15, ChronoUnit.DAYS));
            processed++;
        }

        // 2. Mark documents expiring soon (within 30 days)
        List<Document> expiringDocs = documentRepository.findExpiringDocuments(thirtyDays);
        for (Document doc : expiringDocs) {
            if (doc.getExpiryDate() != null && doc.getExpiryDate().isAfter(now)) {
                doc.setComplianceStatus(DocumentComplianceStatus.EXPIRING_SOON);
                documentRepository.save(doc);

                long daysRemaining = ChronoUnit.DAYS.between(now, doc.getExpiryDate());
                AlertPriority priority = daysRemaining <= 7 ? AlertPriority.URGENT : AlertPriority.HIGH;
                AlertSeverity severity = daysRemaining <= 7 ? AlertSeverity.CRITICAL : AlertSeverity.WARNING;

                alertService.createAlert(
                        "ALERT_DOC_EXPIRING_" + doc.getId() + "_" + daysRemaining + "D",
                        "Document Expiring in " + daysRemaining + " Days (" + doc.getDocumentName() + ")",
                        "Document '" + doc.getDocumentName() + "' expires on " + doc.getExpiryDate() + ". Renewal required.",
                        AlertCategory.COMPLIANCE,
                        severity,
                        priority,
                        "DOCUMENT",
                        doc.getId(),
                        doc.getInstitution().getId(),
                        null,
                        "ROLE_INSTITUTION_OFFICER",
                        doc.getInstitution().getState()
                );

                if (daysRemaining <= 7) {
                    ensureCorrectiveAction(doc, "EXPIRING_SOON", "Document expiring within 7 days. Upload renewal certificate.", "HIGH", doc.getExpiryDate());
                }
                processed++;
            }
        }

        return processed;
    }

    private void ensureCorrectiveAction(Document doc, String actionType, String description, String priority, Instant dueDate) {
        Optional<DocumentCorrectiveAction> existing = correctiveActionRepository
                .findByDocumentIdAndActionTypeAndStatus(doc.getId(), actionType, CorrectiveActionStatus.OPEN);

        if (existing.isEmpty()) {
            DocumentCorrectiveAction action = new DocumentCorrectiveAction();
            action.setInstitution(doc.getInstitution());
            action.setApplication(doc.getApplication());
            action.setDocument(doc);
            action.setActionType(actionType);
            action.setDescription(description);
            action.setPriority(priority);
            action.setResponsibleParty("INSTITUTION_OFFICER");
            action.setDueDate(dueDate);
            action.setStatus(CorrectiveActionStatus.OPEN);
            correctiveActionRepository.save(action);
        }
    }

    // =========================================================================
    // Helpers
    // =========================================================================

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

    private DocumentDto toEnrichedDto(Document doc) {
        DocumentDto dto = DocumentDto.fromEntity(doc);
        List<DocumentExtractedField> fields = extractedFieldRepository.findByDocumentId(doc.getId());
        dto.setExtractedFields(fields.stream().map(ExtractedFieldDto::fromEntity).toList());
        return dto;
    }
}
