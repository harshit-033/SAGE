package org.aicte.sage.modules.compliance.service;

import org.aicte.sage.common.audit.AuditService;
import org.aicte.sage.common.exception.ResourceNotFoundException;
import org.aicte.sage.common.security.AuthoritativeAuthorizationService;
import org.aicte.sage.common.security.UserPrincipal;
import org.aicte.sage.modules.applications.model.Application;
import org.aicte.sage.modules.applications.model.ApplicationState;
import org.aicte.sage.modules.applications.repository.ApplicationRepository;
import org.aicte.sage.modules.compliance.dto.ComplianceCheckRunDto;
import org.aicte.sage.modules.compliance.dto.ComplianceFindingDto;
import org.aicte.sage.modules.compliance.entity.ComplianceCheckRun;
import org.aicte.sage.modules.compliance.entity.ComplianceFinding;
import org.aicte.sage.modules.compliance.entity.ComplianceFindingStatus;
import org.aicte.sage.modules.compliance.entity.FindingType;
import org.aicte.sage.modules.compliance.repository.ComplianceCheckRunRepository;
import org.aicte.sage.modules.compliance.repository.ComplianceFindingRepository;
import org.aicte.sage.modules.deadlines.dto.CreateDeadlineRequest;
import org.aicte.sage.modules.deadlines.entity.DeadlineType;
import org.aicte.sage.modules.deadlines.service.DeadlineService;
import org.aicte.sage.modules.documents.model.Document;
import org.aicte.sage.modules.documents.model.DocumentExtractedField;
import org.aicte.sage.modules.documents.model.DocumentType;
import org.aicte.sage.modules.documents.repository.DocumentExtractedFieldRepository;
import org.aicte.sage.modules.documents.repository.DocumentRepository;
import org.aicte.sage.modules.institutions.entity.Institution;
import org.aicte.sage.modules.institutions.repository.InstitutionRepository;
import org.aicte.sage.modules.notifications.entity.AlertCategory;
import org.aicte.sage.modules.notifications.entity.AlertPriority;
import org.aicte.sage.modules.notifications.entity.AlertSeverity;
import org.aicte.sage.modules.notifications.service.AlertService;
import org.aicte.sage.modules.scrutiny.model.ScrutinyFinding;
import org.aicte.sage.modules.scrutiny.repository.ScrutinyFindingRepository;
import org.aicte.sage.modules.users.entity.User;
import org.aicte.sage.modules.users.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@Transactional
public class ComplianceEngineService {

    private static final Logger log = LoggerFactory.getLogger(ComplianceEngineService.class);

    private final ComplianceCheckRunRepository checkRunRepository;
    private final ComplianceFindingRepository findingRepository;
    private final InstitutionRepository institutionRepository;
    private final ApplicationRepository applicationRepository;
    private final DocumentRepository documentRepository;
    private final DocumentExtractedFieldRepository fieldRepository;
    private final ScrutinyFindingRepository scrutinyFindingRepository;
    private final DeadlineService deadlineService;
    private final AlertService alertService;
    private final UserRepository userRepository;
    private final AuthoritativeAuthorizationService authService;
    private final AuditService auditService;

    @Value("${sage.scrutiny.ocr-confidence-threshold:0.70}")
    private double ocrConfidenceThreshold;

    public ComplianceEngineService(
            ComplianceCheckRunRepository checkRunRepository,
            ComplianceFindingRepository findingRepository,
            InstitutionRepository institutionRepository,
            ApplicationRepository applicationRepository,
            DocumentRepository documentRepository,
            DocumentExtractedFieldRepository fieldRepository,
            ScrutinyFindingRepository scrutinyFindingRepository,
            DeadlineService deadlineService,
            AlertService alertService,
            UserRepository userRepository,
            AuthoritativeAuthorizationService authService,
            AuditService auditService) {
        this.checkRunRepository = checkRunRepository;
        this.findingRepository = findingRepository;
        this.institutionRepository = institutionRepository;
        this.applicationRepository = applicationRepository;
        this.documentRepository = documentRepository;
        this.fieldRepository = fieldRepository;
        this.scrutinyFindingRepository = scrutinyFindingRepository;
        this.deadlineService = deadlineService;
        this.alertService = alertService;
        this.userRepository = userRepository;
        this.authService = authService;
        this.auditService = auditService;
    }

    /**
     * Executes a full continuous compliance check scan.
     */
    public ComplianceCheckRunDto executeComplianceCheckRun(
            String runType,
            String triggerSource,
            UUID targetInstitutionId,
            UserPrincipal principal) {

        User user = principal != null ? userRepository.findById(principal.getId()).orElse(null) : null;
        Institution scopedInst = targetInstitutionId != null
                ? institutionRepository.findById(targetInstitutionId).orElse(null)
                : null;

        ComplianceCheckRun run = new ComplianceCheckRun(runType, triggerSource, scopedInst, user);
        run = checkRunRepository.save(run);

        List<Institution> institutionsToCheck = scopedInst != null
                ? List.of(scopedInst)
                : institutionRepository.findAll();

        int institutionsCount = institutionsToCheck.size();
        int findingsCount = 0;
        int alertsCount = 0;
        int deadlinesCount = 0;

        LocalDate today = LocalDate.now(ZoneOffset.UTC);

        for (Institution inst : institutionsToCheck) {
            // 1. Certificate Expiry Check (Fire Safety)
            List<Document> docs = documentRepository.findByInstitutionId(inst.getId());
            for (Document doc : docs) {
                if (doc.getDocumentType() == DocumentType.FIRE_SAFETY_CERTIFICATE) {
                    List<DocumentExtractedField> fields = fieldRepository.findByDocumentId(doc.getId());
                    Optional<DocumentExtractedField> expiryOpt = fields.stream()
                            .filter(f -> "expiry_date".equalsIgnoreCase(f.getFieldKey()))
                            .findFirst();

                    if (expiryOpt.isPresent()) {
                        try {
                            String val = expiryOpt.get().getNormalizedValue() != null
                                    ? expiryOpt.get().getNormalizedValue()
                                    : expiryOpt.get().getExtractedValue();
                            LocalDate exp = LocalDate.parse(val.trim());

                            if (exp.isBefore(today)) {
                                // Expired!
                                ComplianceFinding finding = createOrUpdateFinding(
                                        run, inst, doc.getApplication(), doc,
                                        "CMP_FIRE_SAFETY_EXPIRED",
                                        "APH 2024-27 Clause 2.1.2 & Annexure 16",
                                        FindingType.CONFIRMED_FAILURE,
                                        AlertSeverity.CRITICAL,
                                        "Fire Safety Certificate Expired",
                                        "Fire Safety NOC expired on " + exp + ". Operating without a valid NOC violates APH safety norms.",
                                        "Extracted expiry date: " + exp + " (Doc ID: " + doc.getId() + ")"
                                );
                                findingsCount++;

                                // Deadline for renewal upload
                                deadlineService.createDeadline(new CreateDeadlineRequest(
                                        "DL_FIRE_SAFETY_RENEWAL",
                                        "Mandatory Fire Safety Certificate Renewal (" + inst.getName() + ")",
                                        "Certificate expired. 15-day statutory window to upload renewed NOC.",
                                        DeadlineType.STATUTORY,
                                        "APH 2024-27 Clause 2.1.2",
                                        "INSTITUTION",
                                        inst.getId(),
                                        inst.getId(),
                                        Instant.now().plus(15, ChronoUnit.DAYS),
                                        0,
                                        "[-7, -3, 0]"
                                ), principal != null ? principal : createSystemPrincipal());
                                deadlinesCount++;

                                alertService.createAlert(
                                        "ALERT_FIRE_SAFETY_EXPIRED",
                                        "CRITICAL: Fire Safety NOC Expired for " + inst.getName(),
                                        "Fire Safety NOC expired on " + exp + ". Urgent submission of renewed NOC is mandatory.",
                                        AlertCategory.COMPLIANCE,
                                        AlertSeverity.CRITICAL,
                                        AlertPriority.URGENT,
                                        "DOCUMENT",
                                        doc.getId(),
                                        inst.getId(),
                                        null,
                                        "ROLE_INSTITUTION_OFFICER",
                                        inst.getState()
                                );
                                alertsCount++;
                            } else if (exp.isBefore(today.plusDays(60))) {
                                // Expiring within 60 days
                                ComplianceFinding finding = createOrUpdateFinding(
                                        run, inst, doc.getApplication(), doc,
                                        "CMP_FIRE_SAFETY_EXPIRING_SOON",
                                        "APH 2024-27 Clause 2.1.2 & Annexure 16",
                                        FindingType.POTENTIAL_ISSUE,
                                        AlertSeverity.WARNING,
                                        "Fire Safety Certificate Expiring Soon (" + exp + ")",
                                        "Fire Safety NOC expires on " + exp + " (within 60 days). Renewal process should be initiated.",
                                        "Extracted expiry date: " + exp + " (Doc ID: " + doc.getId() + ")"
                                );
                                findingsCount++;

                                deadlineService.createDeadline(new CreateDeadlineRequest(
                                        "DL_FIRE_SAFETY_RENEWAL",
                                        "Fire Safety Certificate Upcoming Renewal (" + inst.getName() + ")",
                                        "Renewal NOC required before expiration on " + exp,
                                        DeadlineType.STATUTORY,
                                        "APH 2024-27 Clause 2.1.2",
                                        "INSTITUTION",
                                        inst.getId(),
                                        inst.getId(),
                                        exp.atStartOfDay().toInstant(ZoneOffset.UTC),
                                        0,
                                        "[-30, -15, -7, 0]"
                                ), principal != null ? principal : createSystemPrincipal());
                                deadlinesCount++;
                            }
                        } catch (Exception ex) {
                            log.warn("Could not parse expiry date for document {}: {}", doc.getId(), ex.getMessage());
                        }
                    }
                }

                // 2. OCR Low Confidence / Uncertainty Check
                if (doc.isUncertain() || (doc.getOverallConfidence() != null && doc.getOverallConfidence() < ocrConfidenceThreshold)) {
                    createOrUpdateFinding(
                            run, inst, doc.getApplication(), doc,
                            "CMP_OCR_UNCERTAINTY",
                            "APH 2024-27 Clause 1.6 Governance Integrity",
                            FindingType.OCR_UNCERTAINTY,
                            AlertSeverity.INFO,
                            "OCR Extraction Uncertainty (" + doc.getOriginalFilename() + ")",
                            "Document processed with OCR confidence below policy threshold (" + Math.round((doc.getOverallConfidence() != null ? doc.getOverallConfidence() : 0.0) * 100) + "%). Human verification required.",
                            "Overall confidence score: " + doc.getOverallConfidence()
                    );
                    findingsCount++;
                }
            }

            // 3. Applications Missing Mandatory Documents & Scrutiny Deficiencies
            List<Application> apps = applicationRepository.findByInstitutionId(inst.getId(), Pageable.unpaged()).getContent();
            for (Application app : apps) {
                if (app.getCurrentState() == ApplicationState.SUBMITTED || app.getCurrentState() == ApplicationState.UNDER_SCRUTINY) {
                    List<Document> appDocs = documentRepository.findByApplicationIdOrderByCreatedAtDesc(app.getId());
                    boolean hasFireSafety = appDocs.stream().anyMatch(d -> d.getDocumentType() == DocumentType.FIRE_SAFETY_CERTIFICATE);
                    boolean hasOccupancy = appDocs.stream().anyMatch(d -> d.getDocumentType() == DocumentType.OCCUPANCY_CERTIFICATE);

                    if (!hasFireSafety) {
                        createOrUpdateFinding(
                                run, inst, app, null,
                                "CMP_MISSING_FIRE_SAFETY_DOC",
                                "APH 2024-27 Clause 1.4 & Clause 2.1",
                                FindingType.MISSING_EVIDENCE,
                                AlertSeverity.WARNING,
                                "Missing Fire Safety Certificate on Application " + app.getApplicationNumber(),
                                "Submitted application is missing statutory Fire Safety Certificate / NOC.",
                                "Checked uploaded documents: Fire Safety Certificate not present."
                        );
                        findingsCount++;
                    }

                    if (!hasOccupancy) {
                        createOrUpdateFinding(
                                run, inst, app, null,
                                "CMP_MISSING_OCCUPANCY_DOC",
                                "APH 2024-27 Clause 1.4 & Clause 2.1",
                                FindingType.MISSING_EVIDENCE,
                                AlertSeverity.WARNING,
                                "Missing Building Occupancy Certificate on Application " + app.getApplicationNumber(),
                                "Submitted application is missing statutory Building Completion / Occupancy Certificate.",
                                "Checked uploaded documents: Occupancy Certificate not present."
                        );
                        findingsCount++;
                    }

                    // 4. Unresolved Scrutiny Deficiencies
                    List<ScrutinyFinding> unresolved = scrutinyFindingRepository.findByApplicationIdAndResolvedFalse(app.getId());
                    if (!unresolved.isEmpty()) {
                        createOrUpdateFinding(
                                run, inst, app, null,
                                "CMP_UNRESOLVED_SCRUTINY_DEFICIENCY",
                                "APH 2024-27 Clause 1.6 & Clause 2.3",
                                FindingType.CONFIRMED_FAILURE,
                                AlertSeverity.CRITICAL,
                                "Unresolved Scrutiny Deficiencies (" + unresolved.size() + ") on " + app.getApplicationNumber(),
                                "Application has " + unresolved.size() + " unresolved scrutiny findings requiring physical/documentary rectification.",
                                "Unresolved findings count: " + unresolved.size()
                        );
                        findingsCount++;

                        // Statutory 10-day rectification window per APH Clause 1.6
                        deadlineService.createDeadline(new CreateDeadlineRequest(
                                "DL_SCRUTINY_DEFICIENCY_RECTIFICATION",
                                "Scrutiny Deficiency Rectification (" + app.getApplicationNumber() + ")",
                                "Statutory 10-day window to rectify recorded scrutiny deficiencies.",
                                DeadlineType.STATUTORY,
                                "APH 2024-27 Clause 1.6",
                                "APPLICATION",
                                app.getId(),
                                inst.getId(),
                                Instant.now().plus(10, ChronoUnit.DAYS),
                                0,
                                "[-7, -3, -1, 0]"
                        ), principal != null ? principal : createSystemPrincipal());
                        deadlinesCount++;
                    }
                }
            }
        }

        run.setStatus("SUCCESS");
        run.setCompletedAt(Instant.now());
        run.setInstitutionsChecked(institutionsCount);
        run.setFindingsGenerated(findingsCount);
        run.setAlertsCreated(alertsCount);
        run.setDeadlinesCreated(deadlinesCount);
        run.setSummaryNotes("Continuous compliance monitoring run completed successfully. Checked " +
                institutionsCount + " institutions, generated " + findingsCount + " findings.");

        ComplianceCheckRun savedRun = checkRunRepository.save(run);

        auditService.recordEvent(
                "COMPLIANCE_RUN_COMPLETED",
                principal != null ? principal.getUsername() : "SYSTEM",
                targetInstitutionId,
                "COMPLIANCE_RUN",
                savedRun.getId().toString(),
                "COMPLIANCE_SCAN",
                null,
                "Compliance run completed. Checked " + institutionsCount + " institutions, findings=" + findingsCount
        );

        return ComplianceCheckRunDto.fromEntity(savedRun);
    }

    private ComplianceFinding createOrUpdateFinding(
            ComplianceCheckRun run,
            Institution institution,
            Application application,
            Document document,
            String ruleCode,
            String aphClause,
            FindingType findingType,
            AlertSeverity severity,
            String title,
            String description,
            String evidenceSummary) {

        Optional<ComplianceFinding> existing = findingRepository.findByInstitutionIdAndRuleCodeAndStatus(
                institution.getId(), ruleCode, ComplianceFindingStatus.OPEN);

        if (existing.isPresent()) {
            ComplianceFinding f = existing.get();
            f.setDescription(description);
            f.setEvidenceSummary(evidenceSummary);
            return findingRepository.save(f);
        }

        ComplianceFinding finding = new ComplianceFinding(
                run, institution, application, document,
                ruleCode, aphClause, findingType, severity,
                title, description, evidenceSummary
        );
        return findingRepository.save(finding);
    }

    /**
     * Resolve a compliance finding.
     */
    public ComplianceFindingDto resolveFinding(UUID findingId, String resolutionNotes, UserPrincipal principal) {
        ComplianceFinding finding = findingRepository.findById(findingId)
                .orElseThrow(() -> new ResourceNotFoundException("Compliance finding not found with ID: " + findingId));

        authService.checkAccessToInstitution(principal, finding.getInstitution().getId(), "RESOLVE_COMPLIANCE_FINDING");

        User user = userRepository.findById(principal.getId()).orElse(null);
        finding.setStatus(ComplianceFindingStatus.RESOLVED);
        finding.setResolvedAt(Instant.now());
        finding.setResolvedByUser(user);
        finding.setResolutionNotes(resolutionNotes);

        ComplianceFinding updated = findingRepository.save(finding);

        auditService.recordEvent(
                "COMPLIANCE_FINDING_RESOLVED",
                principal.getUsername(),
                finding.getInstitution().getId(),
                "COMPLIANCE_FINDING",
                finding.getId().toString(),
                "RESOLVE_FINDING",
                null,
                "Compliance finding resolved with notes: " + resolutionNotes
        );

        return ComplianceFindingDto.fromEntity(updated);
    }

    @Transactional(readOnly = true)
    public Page<ComplianceFindingDto> getFindings(
            UUID institutionId,
            ComplianceFindingStatus status,
            Pageable pageable,
            UserPrincipal principal) {

        if (principal.hasRole("ROLE_AICTE_ADMIN")) {
            if (institutionId != null) {
                if (status != null) {
                    return findingRepository.findByInstitutionIdAndStatus(institutionId, status, pageable).map(ComplianceFindingDto::fromEntity);
                }
                return findingRepository.findByInstitutionId(institutionId, pageable).map(ComplianceFindingDto::fromEntity);
            }
            if (status != null) {
                return findingRepository.findByStatus(status, pageable).map(ComplianceFindingDto::fromEntity);
            }
            return findingRepository.findAll(pageable).map(ComplianceFindingDto::fromEntity);
        }

        // Institution Representative: strictly isolated to authorized institution
        Set<UUID> myInsts = principal.getAuthorizedInstitutionIds();
        if (myInsts.isEmpty()) {
            return Page.empty(pageable);
        }

        UUID targetInst = (institutionId != null && myInsts.contains(institutionId)) ? institutionId : myInsts.iterator().next();
        if (status != null) {
            return findingRepository.findByInstitutionIdAndStatus(targetInst, status, pageable).map(ComplianceFindingDto::fromEntity);
        }
        return findingRepository.findByInstitutionId(targetInst, pageable).map(ComplianceFindingDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<ComplianceCheckRunDto> getCheckRuns(UUID institutionId, Pageable pageable, UserPrincipal principal) {
        if (principal.hasRole("ROLE_AICTE_ADMIN")) {
            if (institutionId != null) {
                return checkRunRepository.findByInstitutionId(institutionId, pageable).map(ComplianceCheckRunDto::fromEntity);
            }
            return checkRunRepository.findAll(pageable).map(ComplianceCheckRunDto::fromEntity);
        }

        Set<UUID> myInsts = principal.getAuthorizedInstitutionIds();
        if (myInsts.isEmpty()) {
            return Page.empty(pageable);
        }

        UUID targetInst = (institutionId != null && myInsts.contains(institutionId)) ? institutionId : myInsts.iterator().next();
        return checkRunRepository.findByInstitutionId(targetInst, pageable).map(ComplianceCheckRunDto::fromEntity);
    }

    private UserPrincipal createSystemPrincipal() {
        return new UserPrincipal(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                "system@aicte.gov.in",
                "system@aicte.gov.in",
                "Password123!",
                "System Compliance Monitor",
                true,
                List.of(
                        new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_AICTE_ADMIN"),
                        new org.springframework.security.core.authority.SimpleGrantedAuthority("PERM_SYSTEM_ADMIN"),
                        new org.springframework.security.core.authority.SimpleGrantedAuthority("PERM_DEADLINE_MANAGE")
                ),
                Collections.emptySet()
        );
    }
}
