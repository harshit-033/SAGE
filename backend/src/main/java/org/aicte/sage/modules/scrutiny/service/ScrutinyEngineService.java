package org.aicte.sage.modules.scrutiny.service;

import org.aicte.sage.common.audit.AuditService;
import org.aicte.sage.common.exception.ResourceNotFoundException;
import org.aicte.sage.common.security.AuthoritativeAuthorizationService;
import org.aicte.sage.common.security.UserPrincipal;
import org.aicte.sage.modules.applications.model.Application;
import org.aicte.sage.modules.applications.model.ApplicationBranchDetails;
import org.aicte.sage.modules.applications.model.ApplicationCategory;
import org.aicte.sage.modules.applications.model.ApplicationCourseDetails;
import org.aicte.sage.modules.applications.model.ApplicationState;
import org.aicte.sage.modules.applications.repository.ApplicationBranchDetailsRepository;
import org.aicte.sage.modules.applications.repository.ApplicationCourseDetailsRepository;
import org.aicte.sage.modules.applications.repository.ApplicationRepository;
import org.aicte.sage.modules.documents.model.Document;
import org.aicte.sage.modules.documents.model.DocumentExtractedField;
import org.aicte.sage.modules.documents.model.DocumentType;
import org.aicte.sage.modules.documents.repository.DocumentExtractedFieldRepository;
import org.aicte.sage.modules.documents.repository.DocumentRepository;
import org.aicte.sage.modules.notifications.entity.AlertCategory;
import org.aicte.sage.modules.notifications.entity.AlertPriority;
import org.aicte.sage.modules.notifications.entity.AlertSeverity;
import org.aicte.sage.modules.notifications.service.AlertService;
import org.aicte.sage.modules.scrutiny.dto.ResolveFindingRequest;
import org.aicte.sage.modules.scrutiny.dto.ScrutinyEvaluationDto;
import org.aicte.sage.modules.scrutiny.dto.ScrutinyFindingDto;
import org.aicte.sage.modules.scrutiny.dto.SubmitEvaluatorAssessmentRequest;
import org.aicte.sage.modules.scrutiny.model.*;
import org.aicte.sage.modules.scrutiny.repository.ScrutinyEvaluationRepository;
import org.aicte.sage.modules.scrutiny.repository.ScrutinyFindingRepository;
import org.aicte.sage.modules.scrutiny.repository.ScrutinyRuleRepository;
import org.aicte.sage.modules.users.entity.User;
import org.aicte.sage.modules.users.repository.UserRepository;
import org.aicte.sage.modules.workflow.dto.TransitionRequest;
import org.aicte.sage.modules.workflow.service.WorkflowEngineService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Instant;
import java.util.*;

@Service
@Transactional
public class ScrutinyEngineService {

    private static final Logger log = LoggerFactory.getLogger(ScrutinyEngineService.class);

    private final ApplicationRepository applicationRepository;
    private final DocumentRepository documentRepository;
    private final DocumentExtractedFieldRepository extractedFieldRepository;
    private final ScrutinyRuleRepository ruleRepository;
    private final ScrutinyEvaluationRepository evaluationRepository;
    private final ScrutinyFindingRepository findingRepository;
    private final ApplicationCourseDetailsRepository courseDetailsRepository;
    private final ApplicationBranchDetailsRepository branchDetailsRepository;
    private final UserRepository userRepository;
    private final WorkflowEngineService workflowEngineService;
    private final AuthoritativeAuthorizationService authService;
    private final AuditService auditService;
    private final AlertService alertService;

    @org.springframework.beans.factory.annotation.Value("${sage.scrutiny.ocr-confidence-threshold:0.70}")
    private double ocrConfidenceThreshold = 0.70;

    public ScrutinyEngineService(
            ApplicationRepository applicationRepository,
            DocumentRepository documentRepository,
            DocumentExtractedFieldRepository extractedFieldRepository,
            ScrutinyRuleRepository ruleRepository,
            ScrutinyEvaluationRepository evaluationRepository,
            ScrutinyFindingRepository findingRepository,
            ApplicationCourseDetailsRepository courseDetailsRepository,
            ApplicationBranchDetailsRepository branchDetailsRepository,
            UserRepository userRepository,
            WorkflowEngineService workflowEngineService,
            AuthoritativeAuthorizationService authService,
            AuditService auditService,
            AlertService alertService
    ) {
        this.applicationRepository = applicationRepository;
        this.documentRepository = documentRepository;
        this.extractedFieldRepository = extractedFieldRepository;
        this.ruleRepository = ruleRepository;
        this.evaluationRepository = evaluationRepository;
        this.findingRepository = findingRepository;
        this.courseDetailsRepository = courseDetailsRepository;
        this.branchDetailsRepository = branchDetailsRepository;
        this.userRepository = userRepository;
        this.workflowEngineService = workflowEngineService;
        this.authService = authService;
        this.auditService = auditService;
        this.alertService = alertService;
    }

    public ScrutinyEvaluationDto evaluateApplication(UUID applicationId, UserPrincipal principal) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application with ID '" + applicationId + "' not found"));

        authService.checkAccessToInstitution(principal, application.getInstitution().getId(), "SCRUTINY_EVALUATE");

        User evaluator = userRepository.findById(principal.getId()).orElse(null);

        // Fetch application documents and extracted fields
        List<Document> documents = documentRepository.findByApplicationIdOrderByCreatedAtDesc(applicationId);
        Map<DocumentType, Document> docByType = new HashMap<>();
        Map<UUID, Map<String, DocumentExtractedField>> fieldsByDoc = new HashMap<>();

        for (Document doc : documents) {
            docByType.putIfAbsent(doc.getDocumentType(), doc);
            List<DocumentExtractedField> fields = extractedFieldRepository.findByDocumentId(doc.getId());
            Map<String, DocumentExtractedField> fMap = new HashMap<>();
            for (DocumentExtractedField f : fields) {
                fMap.put(f.getFieldKey(), f);
            }
            fieldsByDoc.put(doc.getId(), fMap);
        }

        // Initialize evaluation record
        ScrutinyEvaluation evaluation = new ScrutinyEvaluation();
        evaluation.setApplication(application);
        evaluation.setEvaluatedByUser(evaluator);
        evaluation.setStatus("PROCESSING");
        ScrutinyEvaluation savedEval = evaluationRepository.save(evaluation);

        List<ScrutinyFinding> generatedFindings = new ArrayList<>();

        // 1. RULE: Mandatory Document Checklist per Category
        evaluateMandatoryDocsRule(savedEval, application, docByType, generatedFindings);

        // 2. RULE: Fire Safety Certificate Validity & Expiry
        evaluateFireSafetyRule(savedEval, application, docByType, fieldsByDoc, generatedFindings);

        // 3. RULE: Building Occupancy & Built-up Area
        evaluateOccupancyRule(savedEval, application, docByType, fieldsByDoc, generatedFindings);

        // 4. RULE: Land Area Norms (New Institution)
        evaluateLandAreaRule(savedEval, application, docByType, fieldsByDoc, generatedFindings);

        // 5. RULE: Institution Name Concordance
        evaluateNameConcordanceRule(savedEval, application, documents, fieldsByDoc, generatedFindings);

        // 6. RULE: OCR Confidence Gate
        evaluateConfidenceGateRule(savedEval, application, documents, generatedFindings);

        // 7. Category-specific rules for New Course & New Branch
        if (application.getCategory() == ApplicationCategory.NEW_COURSE_INTRODUCTION) {
            evaluateNewCourseRules(savedEval, application, docByType, generatedFindings);
        } else if (application.getCategory() == ApplicationCategory.NEW_BRANCH_INTRODUCTION) {
            evaluateNewBranchRules(savedEval, application, docByType, generatedFindings);
        }

        // Save all findings
        findingRepository.saveAll(generatedFindings);

        // Compute overall outcome
        boolean hasDeficiencies = generatedFindings.stream().anyMatch(f -> f.getStatus() == FindingStatus.DEFICIENT);
        boolean hasUncertainOcr = generatedFindings.stream().anyMatch(f -> f.getStatus() == FindingStatus.UNCERTAIN_OCR);

        String finalStatus;
        String summary;
        if (hasDeficiencies) {
            finalStatus = "DEFICIENT";
            summary = "Application scrutiny detected " + generatedFindings.stream().filter(f -> f.getStatus() == FindingStatus.DEFICIENT).count() + " statutory deficiencies requiring applicant rectification.";
        } else if (hasUncertainOcr) {
            finalStatus = "REQUIRES_HUMAN_REVIEW";
            summary = "Automated OCR extraction uncertainty detected. Routed to Scrutiny Officer for human document verification.";
        } else {
            finalStatus = "PASSED";
            summary = "All automated APH 2024-27 statutory document rules verified successfully with high confidence.";
        }

        savedEval.setStatus(finalStatus);
        savedEval.setSummary(summary);
        evaluationRepository.save(savedEval);

        // State transition side effect
        if ("DEFICIENT".equals(finalStatus) && (application.getCurrentState() == ApplicationState.SUBMITTED || application.getCurrentState() == ApplicationState.UNDER_SCRUTINY || application.getCurrentState() == ApplicationState.RESUBMITTED)) {
            application.setCurrentState(ApplicationState.DEFICIENT);
            applicationRepository.save(application);
        } else if ("PASSED".equals(finalStatus) && (application.getCurrentState() == ApplicationState.SUBMITTED || application.getCurrentState() == ApplicationState.UNDER_SCRUTINY || application.getCurrentState() == ApplicationState.RESUBMITTED)) {
            application.setCurrentState(ApplicationState.SCRUTINY_PASSED);
            applicationRepository.save(application);
        } else if ("REQUIRES_HUMAN_REVIEW".equals(finalStatus) && (application.getCurrentState() == ApplicationState.SUBMITTED || application.getCurrentState() == ApplicationState.RESUBMITTED)) {
            application.setCurrentState(ApplicationState.UNDER_SCRUTINY);
            applicationRepository.save(application);
        }

        // Audit Event
        auditService.recordEvent(
                "SCRUTINY_EVALUATION_COMPLETED",
                principal.getUsername(),
                application.getInstitution().getId(),
                "SCRUTINY_EVALUATION",
                savedEval.getId().toString(),
                finalStatus,
                null,
                "Scrutiny evaluation completed for " + application.getApplicationNumber() + ": " + finalStatus
        );

        // Dispatch Scrutiny Notification
        try {
            if ("DEFICIENT".equals(finalStatus)) {
                alertService.createAlert(
                        "ALERT_SCRUTINY_DEFICIENT_" + application.getId(),
                        "Statutory Deficiencies Flagged: " + application.getApplicationNumber(),
                        summary,
                        AlertCategory.WORKFLOW,
                        AlertSeverity.WARNING,
                        AlertPriority.HIGH,
                        "APPLICATION",
                        application.getId(),
                        application.getInstitution().getId(),
                        null,
                        "ROLE_INSTITUTION_OFFICER",
                        null
                );
            } else if ("PASSED".equals(finalStatus)) {
                alertService.createAlert(
                        "ALERT_SCRUTINY_PASSED_" + application.getId(),
                        "Scrutiny Verified: " + application.getApplicationNumber(),
                        summary,
                        AlertCategory.WORKFLOW,
                        AlertSeverity.INFO,
                        AlertPriority.MEDIUM,
                        "APPLICATION",
                        application.getId(),
                        application.getInstitution().getId(),
                        null,
                        "ROLE_REGIONAL_OFFICER",
                        null
                );
            }
        } catch (Exception e) {
            log.warn("Failed to dispatch scrutiny alert for application {}: {}", application.getId(), e.getMessage());
        }

        ScrutinyEvaluationDto dto = ScrutinyEvaluationDto.fromEntity(savedEval);
        dto.setFindings(generatedFindings.stream().map(ScrutinyFindingDto::fromEntity).toList());
        return dto;
    }

    private void evaluateMandatoryDocsRule(
            ScrutinyEvaluation eval,
            Application app,
            Map<DocumentType, Document> docByType,
            List<ScrutinyFinding> findings
    ) {
        ScrutinyRule rule = ruleRepository.findById("RULE_APH_MANDATORY_DOCS").orElse(null);
        if (rule == null) return;

        Set<DocumentType> requiredTypes = new HashSet<>();
        if (app.getCategory() == ApplicationCategory.NEW_INSTITUTION) {
            requiredTypes.add(DocumentType.LAND_REGISTRATION);
            requiredTypes.add(DocumentType.OCCUPANCY_CERTIFICATE);
            requiredTypes.add(DocumentType.FIRE_SAFETY_CERTIFICATE);
            requiredTypes.add(DocumentType.BOARD_RESOLUTION);
            requiredTypes.add(DocumentType.AFFIDAVIT_FORMAT_1);
        } else if (app.getCategory() == ApplicationCategory.EXTENSION_OF_APPROVAL) {
            requiredTypes.add(DocumentType.FIRE_SAFETY_CERTIFICATE);
            requiredTypes.add(DocumentType.OCCUPANCY_CERTIFICATE);
            requiredTypes.add(DocumentType.AFFIDAVIT_FORMAT_1);
        } else if (app.getCategory() == ApplicationCategory.INCREASE_IN_INTAKE) {
            requiredTypes.add(DocumentType.FIRE_SAFETY_CERTIFICATE);
            requiredTypes.add(DocumentType.OCCUPANCY_CERTIFICATE);
            requiredTypes.add(DocumentType.BOARD_RESOLUTION);
        } else if (app.getCategory() == ApplicationCategory.NEW_COURSE_INTRODUCTION) {
            requiredTypes.add(DocumentType.FIRE_SAFETY_CERTIFICATE);
            requiredTypes.add(DocumentType.OCCUPANCY_CERTIFICATE);
            requiredTypes.add(DocumentType.BOARD_RESOLUTION);
            requiredTypes.add(DocumentType.FACULTY_LIST_QUALIFICATION);
            requiredTypes.add(DocumentType.LABORATORY_INFRASTRUCTURE_REPORT);
            requiredTypes.add(DocumentType.SYLLABUS_CURRICULUM_APPROVAL);
        } else if (app.getCategory() == ApplicationCategory.NEW_BRANCH_INTRODUCTION) {
            requiredTypes.add(DocumentType.FIRE_SAFETY_CERTIFICATE);
            requiredTypes.add(DocumentType.OCCUPANCY_CERTIFICATE);
            requiredTypes.add(DocumentType.BOARD_RESOLUTION);
            requiredTypes.add(DocumentType.FACULTY_LIST_QUALIFICATION);
            requiredTypes.add(DocumentType.LABORATORY_INFRASTRUCTURE_REPORT);
        } else {
            requiredTypes.add(DocumentType.FIRE_SAFETY_CERTIFICATE);
            requiredTypes.add(DocumentType.AFFIDAVIT_FORMAT_1);
        }

        List<DocumentType> missingTypes = new ArrayList<>();
        for (DocumentType t : requiredTypes) {
            if (!docByType.containsKey(t)) {
                missingTypes.add(t);
            }
        }

        if (!missingTypes.isEmpty()) {
            ScrutinyFinding f = new ScrutinyFinding();
            f.setEvaluation(eval);
            f.setApplication(app);
            f.setRule(rule);
            f.setStatus(FindingStatus.DEFICIENT);
            f.setDeficiencyType(DeficiencyType.MISSING_MANDATORY_DOCUMENT);
            f.setExplanation("Mandatory statutory documents missing for " + app.getCategory() + ": " + missingTypes);
            f.setEvidenceText("Required by APH Clause 1.4 & 2.1: " + requiredTypes);
            findings.add(f);
        } else {
            ScrutinyFinding f = new ScrutinyFinding();
            f.setEvaluation(eval);
            f.setApplication(app);
            f.setRule(rule);
            f.setStatus(FindingStatus.COMPLIANT);
            f.setExplanation("All mandatory statutory documents for category " + app.getCategory() + " are uploaded and verified.");
            f.setEvidenceText("Uploaded: " + requiredTypes);
            findings.add(f);
        }
    }

    private void evaluateFireSafetyRule(
            ScrutinyEvaluation eval,
            Application app,
            Map<DocumentType, Document> docByType,
            Map<UUID, Map<String, DocumentExtractedField>> fieldsByDoc,
            List<ScrutinyFinding> findings
    ) {
        ScrutinyRule rule = ruleRepository.findById("RULE_APH_FIRE_SAFETY").orElse(null);
        if (rule == null) return;

        Document doc = docByType.get(DocumentType.FIRE_SAFETY_CERTIFICATE);
        if (doc == null) {
            return; // Handled under mandatory docs checklist
        }

        Map<String, DocumentExtractedField> fMap = fieldsByDoc.getOrDefault(doc.getId(), Collections.emptyMap());
        DocumentExtractedField expiryField = fMap.get("expiry_date");
        DocumentExtractedField certNoField = fMap.get("certificate_number");

        if (certNoField == null || certNoField.getExtractedValue().isBlank()) {
            ScrutinyFinding f = new ScrutinyFinding();
            f.setEvaluation(eval);
            f.setApplication(app);
            f.setRule(rule);
            f.setDocument(doc);
            f.setStatus(FindingStatus.DEFICIENT);
            f.setDeficiencyType(DeficiencyType.FIELD_MISSING);
            f.setExplanation("Fire Safety Certificate number could not be extracted from document.");
            f.setEvidenceText("Raw text: " + (doc.getOverallConfidence() != null ? "Confidence: " + doc.getOverallConfidence() : "Unparsed"));
            findings.add(f);
            return;
        }

        if (expiryField == null || expiryField.getNormalizedValue() == null) {
            ScrutinyFinding f = new ScrutinyFinding();
            f.setEvaluation(eval);
            f.setApplication(app);
            f.setRule(rule);
            f.setDocument(doc);
            f.setStatus(FindingStatus.UNCERTAIN_OCR);
            f.setDeficiencyType(DeficiencyType.FIELD_MISSING);
            f.setExplanation("Fire Safety Certificate expiry date is unextracted or ambiguous. Requires human officer verification.");
            f.setEvidenceText("Certificate No: " + certNoField.getExtractedValue());
            findings.add(f);
            return;
        }

        // Compare expiry date with current academic session start
        try {
            LocalDate expiry = LocalDate.parse(expiryField.getNormalizedValue());
            LocalDate minAcceptableDate = LocalDate.of(2024, 6, 1);
            if (expiry.isBefore(minAcceptableDate)) {
                ScrutinyFinding f = new ScrutinyFinding();
                f.setEvaluation(eval);
                f.setApplication(app);
                f.setRule(rule);
                f.setDocument(doc);
                f.setStatus(FindingStatus.DEFICIENT);
                f.setDeficiencyType(DeficiencyType.EXPIRED_CERTIFICATE);
                f.setExplanation("Fire Safety Certificate expired on " + expiry + ". AICTE norms require valid NOC throughout academic session.");
                f.setEvidenceText("Extracted expiry date: " + expiry + " (Cert: " + certNoField.getExtractedValue() + ")");
                findings.add(f);
                return;
            }
        } catch (Exception ignored) {}

        ScrutinyFinding f = new ScrutinyFinding();
        f.setEvaluation(eval);
        f.setApplication(app);
        f.setRule(rule);
        f.setDocument(doc);
        f.setStatus(FindingStatus.COMPLIANT);
        f.setExplanation("Fire Safety NOC verified. Certificate " + certNoField.getExtractedValue() + " is valid until " + expiryField.getExtractedValue());
        f.setEvidenceText("Issuing Authority: " + (fMap.containsKey("issuing_authority") ? fMap.get("issuing_authority").getExtractedValue() : "Competent Authority"));
        findings.add(f);
    }

    private void evaluateOccupancyRule(
            ScrutinyEvaluation eval,
            Application app,
            Map<DocumentType, Document> docByType,
            Map<UUID, Map<String, DocumentExtractedField>> fieldsByDoc,
            List<ScrutinyFinding> findings
    ) {
        ScrutinyRule rule = ruleRepository.findById("RULE_APH_OCCUPANCY").orElse(null);
        if (rule == null) return;

        Document doc = docByType.get(DocumentType.OCCUPANCY_CERTIFICATE);
        if (doc == null) return;

        Map<String, DocumentExtractedField> fMap = fieldsByDoc.getOrDefault(doc.getId(), Collections.emptyMap());
        DocumentExtractedField certNoField = fMap.get("certificate_number");
        DocumentExtractedField builtUpField = fMap.get("built_up_area_sqm");

        ScrutinyFinding f = new ScrutinyFinding();
        f.setEvaluation(eval);
        f.setApplication(app);
        f.setRule(rule);
        f.setDocument(doc);

        if (certNoField != null && certNoField.getExtractedValue() != null) {
            f.setStatus(FindingStatus.COMPLIANT);
            f.setExplanation("Building Completion/Occupancy Certificate verified: " + certNoField.getExtractedValue());
            f.setEvidenceText("Built-up Area: " + (builtUpField != null ? builtUpField.getExtractedValue() + " Sq.M." : "Declared"));
        } else {
            f.setStatus(FindingStatus.DEFICIENT);
            f.setDeficiencyType(DeficiencyType.FIELD_MISSING);
            f.setExplanation("Occupancy Certificate number could not be extracted.");
            f.setEvidenceText("Document: " + doc.getOriginalFilename());
        }
        findings.add(f);
    }

    private void evaluateLandAreaRule(
            ScrutinyEvaluation eval,
            Application app,
            Map<DocumentType, Document> docByType,
            Map<UUID, Map<String, DocumentExtractedField>> fieldsByDoc,
            List<ScrutinyFinding> findings
    ) {
        ScrutinyRule rule = ruleRepository.findById("RULE_APH_LAND_AREA").orElse(null);
        if (rule == null || app.getCategory() != ApplicationCategory.NEW_INSTITUTION) return;

        Document doc = docByType.get(DocumentType.LAND_REGISTRATION);
        if (doc == null) return;

        Map<String, DocumentExtractedField> fMap = fieldsByDoc.getOrDefault(doc.getId(), Collections.emptyMap());
        DocumentExtractedField landAreaField = fMap.get("land_area_acres");

        ScrutinyFinding f = new ScrutinyFinding();
        f.setEvaluation(eval);
        f.setApplication(app);
        f.setRule(rule);
        f.setDocument(doc);

        if (landAreaField != null) {
            try {
                double acres = Double.parseDouble(landAreaField.getExtractedValue());
                if (acres < 2.50) {
                    f.setStatus(FindingStatus.DEFICIENT);
                    f.setDeficiencyType(DeficiencyType.AREA_BELOW_NORMS);
                    f.setExplanation("Contiguous land area " + acres + " acres is below the statutory requirement of 2.50 acres for Technical Institutions per APH Chapter IV.");
                    f.setEvidenceText("Extracted land area: " + acres + " Acres");
                } else {
                    f.setStatus(FindingStatus.COMPLIANT);
                    f.setExplanation("Contiguous land area " + acres + " acres meets APH 2024-27 statutory norms (>= 2.50 acres).");
                    f.setEvidenceText("Deed: " + (fMap.containsKey("certificate_number") ? fMap.get("certificate_number").getExtractedValue() : "Verified"));
                }
            } catch (NumberFormatException e) {
                f.setStatus(FindingStatus.UNCERTAIN_OCR);
                f.setExplanation("Extracted land area string '" + landAreaField.getExtractedValue() + "' requires manual verification.");
            }
        } else {
            f.setStatus(FindingStatus.DEFICIENT);
            f.setDeficiencyType(DeficiencyType.FIELD_MISSING);
            f.setExplanation("Land acreage could not be extracted from title deed schedule.");
        }
        findings.add(f);
    }

    private void evaluateNameConcordanceRule(
            ScrutinyEvaluation eval,
            Application app,
            List<Document> documents,
            Map<UUID, Map<String, DocumentExtractedField>> fieldsByDoc,
            List<ScrutinyFinding> findings
    ) {
        ScrutinyRule rule = ruleRepository.findById("RULE_APH_INST_NAME").orElse(null);
        if (rule == null) return;

        String registeredName = app.getInstitution().getName().toLowerCase();
        boolean anyMismatch = false;
        StringBuilder evidence = new StringBuilder();

        for (Document doc : documents) {
            Map<String, DocumentExtractedField> fMap = fieldsByDoc.getOrDefault(doc.getId(), Collections.emptyMap());
            DocumentExtractedField nameField = fMap.get("institution_name");
            if (nameField != null && !nameField.getExtractedValue().isBlank()) {
                String extracted = nameField.getExtractedValue().toLowerCase();
                // Basic word overlap test
                String[] registeredWords = registeredName.split("\\s+");
                int matchingWords = 0;
                for (String w : registeredWords) {
                    if (w.length() > 3 && extracted.contains(w)) {
                        matchingWords++;
                    }
                }
                if (matchingWords == 0 && registeredWords.length > 1) {
                    anyMismatch = true;
                    evidence.append("[").append(doc.getDocumentType()).append(": '").append(nameField.getExtractedValue()).append("'] ");
                }
            }
        }

        ScrutinyFinding f = new ScrutinyFinding();
        f.setEvaluation(eval);
        f.setApplication(app);
        f.setRule(rule);

        if (anyMismatch) {
            f.setStatus(FindingStatus.DEFICIENT);
            f.setDeficiencyType(DeficiencyType.NAME_MISMATCH);
            f.setExplanation("Institution name mismatch detected between registered profile ('" + app.getInstitution().getName() + "') and uploaded documents.");
            f.setEvidenceText("Mismatched documents: " + evidence);
        } else {
            f.setStatus(FindingStatus.COMPLIANT);
            f.setExplanation("Institution name across all uploaded certificates matches the registered profile ('" + app.getInstitution().getName() + "').");
            f.setEvidenceText("Profile Concordance: High");
        }
        findings.add(f);
    }

    private void evaluateConfidenceGateRule(
            ScrutinyEvaluation eval,
            Application app,
            List<Document> documents,
            List<ScrutinyFinding> findings
    ) {
        ScrutinyRule rule = ruleRepository.findById("RULE_APH_CONFIDENCE_GATE").orElse(null);
        if (rule == null) return;

        int thresholdPct = (int) Math.round(ocrConfidenceThreshold * 100);
        List<String> lowConfDocs = new ArrayList<>();
        for (Document d : documents) {
            if (d.isUncertain() || (d.getOverallConfidence() != null && d.getOverallConfidence() < ocrConfidenceThreshold)) {
                lowConfDocs.add(d.getOriginalFilename() + " (" + (d.getOverallConfidence() != null ? Math.round(d.getOverallConfidence() * 100) + "%" : "Uncertain") + ")");
            }
        }

        ScrutinyFinding f = new ScrutinyFinding();
        f.setEvaluation(eval);
        f.setApplication(app);
        f.setRule(rule);

        if (!lowConfDocs.isEmpty()) {
            f.setStatus(FindingStatus.UNCERTAIN_OCR);
            f.setDeficiencyType(DeficiencyType.LOW_CONFIDENCE_EXTRACTION);
            f.setExplanation("SAGE Governance Policy (Safeguarding Human Review per APH Clause 1.6): OCR extraction confidence fell below configured project threshold (" + thresholdPct + "%). Routed to Scrutiny Officer for human document verification.");
            f.setEvidenceText("Low confidence documents: " + lowConfDocs);
        } else {
            f.setStatus(FindingStatus.COMPLIANT);
            f.setExplanation("All documents processed with OCR confidence exceeding project governance threshold (" + thresholdPct + "%).");
            f.setEvidenceText("Confidence Score: > " + thresholdPct + "% across all pages");
        }
        findings.add(f);
    }

    public ScrutinyFindingDto resolveFinding(
            UUID findingId,
            ResolveFindingRequest request,
            UserPrincipal principal
    ) {
        ScrutinyFinding finding = findingRepository.findById(findingId)
                .orElseThrow(() -> new ResourceNotFoundException("Scrutiny finding with ID '" + findingId + "' not found"));

        authService.checkAccessToInstitution(principal, finding.getApplication().getInstitution().getId(), "RESOLVE_DEFICIENCY");

        boolean canResolve = principal.hasRole("ROLE_AICTE_ADMIN") ||
                principal.hasRole("ROLE_REGIONAL_OFFICER") ||
                principal.hasRole("ROLE_COMMITTEE_MEMBER");

        if (!canResolve) {
            throw new AccessDeniedException("Only authorized AICTE Scrutiny Officers, Regional Officers, or Committee Members can resolve deficiency findings.");
        }

        User resolver = userRepository.findById(principal.getId()).orElse(null);

        finding.setResolved(true);
        finding.setResolutionNotes(request.getResolutionNotes());
        finding.setResolvedBy(resolver);
        finding.setResolvedAt(Instant.now());

        ScrutinyFinding saved = findingRepository.save(finding);

        // Check if all findings for this application are now resolved
        List<ScrutinyFinding> unresolved = findingRepository.findByApplicationIdAndResolvedFalse(finding.getApplication().getId());
        boolean hasUnresolvedDeficiencies = unresolved.stream().anyMatch(f -> f.getStatus() == FindingStatus.DEFICIENT);

        if (!hasUnresolvedDeficiencies && finding.getApplication().getCurrentState() == ApplicationState.DEFICIENT) {
            finding.getApplication().setCurrentState(ApplicationState.SCRUTINY_PASSED);
            applicationRepository.save(finding.getApplication());

            auditService.recordEvent(
                    "WORKFLOW_STATE_TRANSITION",
                    principal.getUsername(),
                    finding.getApplication().getInstitution().getId(),
                    "APPLICATION",
                    finding.getApplication().getId().toString(),
                    "DEFICIENT -> SCRUTINY_PASSED",
                    null,
                    "Application state transitioned from DEFICIENT to SCRUTINY_PASSED following resolution of all statutory findings by " + principal.getUsername()
            );
        }

        // Audit Event
        auditService.recordEvent(
                "SCRUTINY_FINDING_RESOLVED",
                principal.getUsername(),
                finding.getApplication().getInstitution().getId(),
                "SCRUTINY_FINDING",
                finding.getId().toString(),
                "RESOLVE",
                null,
                "Scrutiny finding " + finding.getRule().getId() + " resolved by " + principal.getUsername() + ". Notes: " + request.getResolutionNotes()
        );

        return ScrutinyFindingDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<ScrutinyFindingDto> getFindingsForApplication(UUID applicationId, UserPrincipal principal) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application with ID '" + applicationId + "' not found"));

        authService.checkAccessToInstitution(principal, application.getInstitution().getId(), "GET_FINDINGS");

        return findingRepository.findByApplicationId(applicationId).stream()
                .map(ScrutinyFindingDto::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ScrutinyEvaluationDto> getEvaluationsForApplication(UUID applicationId, UserPrincipal principal) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application with ID '" + applicationId + "' not found"));

        authService.checkAccessToInstitution(principal, application.getInstitution().getId(), "GET_EVALUATIONS");

        List<ScrutinyEvaluation> evals = evaluationRepository.findByApplicationIdOrderByEvaluatedAtDesc(applicationId);
        return evals.stream().map(e -> {
            ScrutinyEvaluationDto dto = ScrutinyEvaluationDto.fromEntity(e);
            List<ScrutinyFinding> findings = findingRepository.findByEvaluationId(e.getId());
            dto.setFindings(findings.stream().map(ScrutinyFindingDto::fromEntity).toList());
            return dto;
        }).toList();
    }

    private void evaluateNewCourseRules(
            ScrutinyEvaluation eval,
            Application app,
            Map<DocumentType, Document> docByType,
            List<ScrutinyFinding> findings
    ) {
        ApplicationCourseDetails courseDetails = courseDetailsRepository.findByApplicationId(app.getId()).orElse(null);
        Document facultyDoc = docByType.get(DocumentType.FACULTY_LIST_QUALIFICATION);
        Document labDoc = docByType.get(DocumentType.LABORATORY_INFRASTRUCTURE_REPORT);
        Document syllabusDoc = docByType.get(DocumentType.SYLLABUS_CURRICULUM_APPROVAL);

        // Rule 1: Faculty Ratio & Cadre Norms
        ScrutinyRule facultyRatioRule = ruleRepository.findById("RULE_APH_COURSE_FACULTY_RATIO").orElse(null);
        if (facultyRatioRule != null) {
            if (courseDetails == null) {
                ScrutinyFinding f = new ScrutinyFinding();
                f.setEvaluation(eval);
                f.setApplication(app);
                f.setRule(facultyRatioRule);
                f.setDocument(facultyDoc);
                f.setStatus(FindingStatus.DEFICIENT);
                f.setDeficiencyType(DeficiencyType.INSUFFICIENT_DATA);
                f.setExplanation("Proposed course infrastructure & faculty details not provided.");
                f.setEvidenceText("ApplicationCourseDetails record is missing");
                findings.add(f);
            } else {
                int totalFaculty = (courseDetails.getProfessorsCount() != null ? courseDetails.getProfessorsCount() : 0) +
                        (courseDetails.getAssociateProfessorsCount() != null ? courseDetails.getAssociateProfessorsCount() : 0) +
                        (courseDetails.getAssistantProfessorsCount() != null ? courseDetails.getAssistantProfessorsCount() : 0);

                if (totalFaculty < 3 || (courseDetails.getProfessorsCount() != null && courseDetails.getProfessorsCount() < 1)) {
                    ScrutinyFinding f = new ScrutinyFinding();
                    f.setEvaluation(eval);
                    f.setApplication(app);
                    f.setRule(facultyRatioRule);
                    f.setDocument(facultyDoc);
                    f.setStatus(FindingStatus.DEFICIENT);
                    f.setDeficiencyType(DeficiencyType.FACULTY_SHORTFALL);
                    f.setExplanation("Faculty cadre does not satisfy minimum APH norms for proposed course (Requires at least 1 Professor and minimum 3 core faculty members). Current: " + totalFaculty + " total, " + courseDetails.getProfessorsCount() + " Professors.");
                    f.setEvidenceText("Professors: " + courseDetails.getProfessorsCount() + ", Associate: " + courseDetails.getAssociateProfessorsCount() + ", Assistant: " + courseDetails.getAssistantProfessorsCount());
                    findings.add(f);
                } else {
                    ScrutinyFinding f = new ScrutinyFinding();
                    f.setEvaluation(eval);
                    f.setApplication(app);
                    f.setRule(facultyRatioRule);
                    f.setDocument(facultyDoc);
                    f.setStatus(FindingStatus.COMPLIANT);
                    f.setExplanation("Faculty cadre satisfies APH 2024-27 norms: " + totalFaculty + " faculty members (Professors: " + courseDetails.getProfessorsCount() + ", Ratio: " + (courseDetails.getFacultyStudentRatio() != null ? courseDetails.getFacultyStudentRatio() : "1:20") + ").");
                    f.setEvidenceText("Total Core Faculty: " + totalFaculty);
                    findings.add(f);
                }
            }
        }

        // Rule 2: Laboratories & Classrooms
        ScrutinyRule labRule = ruleRepository.findById("RULE_APH_COURSE_LABORATORY").orElse(null);
        if (labRule != null && courseDetails != null) {
            int labs = courseDetails.getLaboratoriesCount() != null ? courseDetails.getLaboratoriesCount() : 0;
            int classrooms = courseDetails.getAvailableClassrooms() != null ? courseDetails.getAvailableClassrooms() : 0;

            if (labs < 1 || classrooms < 1) {
                ScrutinyFinding f = new ScrutinyFinding();
                f.setEvaluation(eval);
                f.setApplication(app);
                f.setRule(labRule);
                f.setDocument(labDoc);
                f.setStatus(FindingStatus.DEFICIENT);
                f.setDeficiencyType(DeficiencyType.INADEQUATE_INFRASTRUCTURE);
                f.setExplanation("Insufficient laboratory/classroom infrastructure for proposed course. Minimum required: 1 lab and 1 classroom. Available: " + labs + " labs, " + classrooms + " classrooms.");
                f.setEvidenceText("Laboratories: " + labs + ", Classrooms: " + classrooms);
                findings.add(f);
            } else {
                ScrutinyFinding f = new ScrutinyFinding();
                f.setEvaluation(eval);
                f.setApplication(app);
                f.setRule(labRule);
                f.setDocument(labDoc);
                f.setStatus(FindingStatus.COMPLIANT);
                f.setExplanation("Infrastructure norms satisfied: " + labs + " dedicated laboratories and " + classrooms + " classrooms allocated for " + courseDetails.getCourseName());
                f.setEvidenceText("Laboratories: " + labs + ", Classrooms: " + classrooms);
                findings.add(f);
            }
        }

        // Rule 3: Curriculum & Affiliation
        ScrutinyRule affRule = ruleRepository.findById("RULE_APH_COURSE_CURRICULUM_AFFILIATION").orElse(null);
        if (affRule != null) {
            if (syllabusDoc == null) {
                ScrutinyFinding f = new ScrutinyFinding();
                f.setEvaluation(eval);
                f.setApplication(app);
                f.setRule(affRule);
                f.setStatus(FindingStatus.DEFICIENT);
                f.setDeficiencyType(DeficiencyType.MISSING_MANDATORY_DOCUMENT);
                f.setExplanation("Syllabus & University Curriculum Affiliation document is required for new course introduction.");
                f.setEvidenceText("Missing document type: SYLLABUS_CURRICULUM_APPROVAL");
                findings.add(f);
            } else {
                ScrutinyFinding f = new ScrutinyFinding();
                f.setEvaluation(eval);
                f.setApplication(app);
                f.setRule(affRule);
                f.setDocument(syllabusDoc);
                f.setStatus(FindingStatus.COMPLIANT);
                f.setExplanation("University curriculum approval verified: " + syllabusDoc.getOriginalFilename());
                f.setEvidenceText("Document: " + syllabusDoc.getOriginalFilename() + " (v" + syllabusDoc.getVersionNumber() + ")");
                findings.add(f);
            }
        }
    }

    private void evaluateNewBranchRules(
            ScrutinyEvaluation eval,
            Application app,
            Map<DocumentType, Document> docByType,
            List<ScrutinyFinding> findings
    ) {
        ApplicationBranchDetails branchDetails = branchDetailsRepository.findByApplicationId(app.getId()).orElse(null);
        Document facultyDoc = docByType.get(DocumentType.FACULTY_LIST_QUALIFICATION);
        Document labDoc = docByType.get(DocumentType.LABORATORY_INFRASTRUCTURE_REPORT);

        // Rule 1: Intake Cap
        ScrutinyRule intakeCapRule = ruleRepository.findById("RULE_APH_BRANCH_INTAKE_CAP").orElse(null);
        if (intakeCapRule != null) {
            if (branchDetails == null) {
                ScrutinyFinding f = new ScrutinyFinding();
                f.setEvaluation(eval);
                f.setApplication(app);
                f.setRule(intakeCapRule);
                f.setStatus(FindingStatus.DEFICIENT);
                f.setDeficiencyType(DeficiencyType.INSUFFICIENT_DATA);
                f.setExplanation("Proposed branch/specialization intake details not provided.");
                f.setEvidenceText("ApplicationBranchDetails record is missing");
                findings.add(f);
            } else {
                int proposedIntake = branchDetails.getProposedIntake() != null ? branchDetails.getProposedIntake() : 0;
                if (proposedIntake > 120 || proposedIntake < 15) {
                    ScrutinyFinding f = new ScrutinyFinding();
                    f.setEvaluation(eval);
                    f.setApplication(app);
                    f.setRule(intakeCapRule);
                    f.setStatus(FindingStatus.DEFICIENT);
                    f.setDeficiencyType(DeficiencyType.POLICY_VIOLATION);
                    f.setExplanation("Proposed intake of " + proposedIntake + " exceeds standard APH single-division ceiling (15 to 120 seats per branch).");
                    f.setEvidenceText("Proposed intake: " + proposedIntake);
                    findings.add(f);
                } else {
                    ScrutinyFinding f = new ScrutinyFinding();
                    f.setEvaluation(eval);
                    f.setApplication(app);
                    f.setRule(intakeCapRule);
                    f.setStatus(FindingStatus.COMPLIANT);
                    f.setExplanation("Proposed intake of " + proposedIntake + " is within statutory limits for branch " + branchDetails.getProposedBranchName());
                    f.setEvidenceText("Intake: " + proposedIntake);
                    findings.add(f);
                }
            }
        }

        // Rule 2: Core Faculty & Lab Availability
        ScrutinyRule coreFacultyRule = ruleRepository.findById("RULE_APH_BRANCH_CORE_FACULTY").orElse(null);
        if (coreFacultyRule != null && branchDetails != null) {
            int faculty = branchDetails.getFacultyCount() != null ? branchDetails.getFacultyCount() : 0;
            int labs = branchDetails.getLaboratoryCount() != null ? branchDetails.getLaboratoryCount() : 0;

            if (faculty < 3 || labs < 1) {
                ScrutinyFinding f = new ScrutinyFinding();
                f.setEvaluation(eval);
                f.setApplication(app);
                f.setRule(coreFacultyRule);
                f.setDocument(facultyDoc != null ? facultyDoc : labDoc);
                f.setStatus(FindingStatus.DEFICIENT);
                f.setDeficiencyType(DeficiencyType.FACULTY_SHORTFALL);
                f.setExplanation("Inadequate core faculty or laboratory infrastructure for proposed branch. Minimum required: 3 faculty, 1 lab. Available: " + faculty + " faculty, " + labs + " labs.");
                f.setEvidenceText("Faculty: " + faculty + ", Labs: " + labs);
                findings.add(f);
            } else {
                ScrutinyFinding f = new ScrutinyFinding();
                f.setEvaluation(eval);
                f.setApplication(app);
                f.setRule(coreFacultyRule);
                f.setDocument(facultyDoc);
                f.setStatus(FindingStatus.COMPLIANT);
                f.setExplanation("Core faculty (" + faculty + ") and laboratory resources (" + labs + ") satisfied for " + branchDetails.getProposedBranchName());
                f.setEvidenceText("Faculty: " + faculty + ", Labs: " + labs);
                findings.add(f);
            }
        }
    }

    public ScrutinyEvaluationDto submitEvaluatorAssessment(
            UUID applicationId,
            SubmitEvaluatorAssessmentRequest request,
            UserPrincipal principal
    ) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application with ID '" + applicationId + "' not found"));

        authService.checkAccessToInstitution(principal, application.getInstitution().getId(), "SCRUTINY_EVALUATE");

        User evaluator = userRepository.findById(principal.getId()).orElse(null);

        ScrutinyEvaluation evaluation = new ScrutinyEvaluation();
        evaluation.setApplication(application);
        evaluation.setEvaluatedByUser(evaluator);
        evaluation.setScore(request.score());
        evaluation.setRecommendation(request.recommendation());
        evaluation.setRemarks(request.remarks());
        evaluation.setCriteriaBreakdownJson(request.criteriaBreakdownJson());

        String status = "PASSED";
        if (request.score() < 50 || "DEFICIENCY_RECTIFICATION".equalsIgnoreCase(request.recommendation()) || "REJECTED".equalsIgnoreCase(request.recommendation())) {
            status = "DEFICIENT";
        } else if ("REQUIRES_HUMAN_REVIEW".equalsIgnoreCase(request.recommendation())) {
            status = "REQUIRES_HUMAN_REVIEW";
        }
        evaluation.setStatus(status);
        evaluation.setSummary(request.summary() != null && !request.summary().isBlank()
                ? request.summary()
                : "Evaluator " + principal.getUsername() + " assessment: " + request.recommendation() + " (Score: " + request.score() + "/100). " + request.remarks());

        ScrutinyEvaluation saved = evaluationRepository.save(evaluation);

        // State transition side effect if application is in appropriate states
        if ("DEFICIENT".equals(status)) {
            if (application.getCurrentState() == ApplicationState.SUBMITTED || application.getCurrentState() == ApplicationState.UNDER_SCRUTINY || application.getCurrentState() == ApplicationState.RESUBMITTED) {
                application.setCurrentState(ApplicationState.DEFICIENT);
                applicationRepository.save(application);
            }
        } else if ("PASSED".equals(status)) {
            if (application.getCurrentState() == ApplicationState.SUBMITTED || application.getCurrentState() == ApplicationState.UNDER_SCRUTINY || application.getCurrentState() == ApplicationState.RESUBMITTED) {
                application.setCurrentState(ApplicationState.SCRUTINY_PASSED);
                applicationRepository.save(application);
            }
        }

        // Record Audit Event
        auditService.recordEvent(
                "EVALUATOR_ASSESSMENT_SUBMITTED",
                principal.getUsername(),
                application.getInstitution().getId(),
                "SCRUTINY_EVALUATION",
                saved.getId().toString(),
                status,
                null,
                "Evaluator assessment submitted by " + principal.getUsername() + ": " + request.recommendation() + " (Score: " + request.score() + "/100)"
        );

        // Alert Institution
        try {
            alertService.createAlert(
                    "ALERT_EVAL_ASSESSED_" + application.getId(),
                    "Application Evaluator Assessment Completed: " + application.getApplicationNumber(),
                    "Evaluator assessment completed for application " + application.getApplicationNumber() + ". Recommendation: " + request.recommendation() + " (Score: " + request.score() + "/100)",
                    AlertCategory.WORKFLOW,
                    "DEFICIENT".equals(status) ? AlertSeverity.WARNING : AlertSeverity.INFO,
                    AlertPriority.HIGH,
                    "APPLICATION",
                    application.getId(),
                    application.getInstitution().getId(),
                    null,
                    "ROLE_INSTITUTION_OFFICER",
                    null
            );
        } catch (Exception e) {
            log.warn("Failed to dispatch evaluator assessment alert for application {}: {}", application.getId(), e.getMessage());
        }

        return ScrutinyEvaluationDto.fromEntity(saved);
    }
}
