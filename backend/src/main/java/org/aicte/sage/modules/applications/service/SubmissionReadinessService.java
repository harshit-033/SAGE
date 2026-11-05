package org.aicte.sage.modules.applications.service;

import org.aicte.sage.modules.applications.dto.StatutoryRequirementDefinitionDto;
import org.aicte.sage.modules.applications.dto.SubmissionReadinessDto;
import org.aicte.sage.modules.applications.dto.SubmissionReadinessDto.RequiredDocumentItemDto;
import org.aicte.sage.modules.applications.model.Application;
import org.aicte.sage.modules.applications.model.ApplicationCategory;
import org.aicte.sage.modules.documents.model.Document;
import org.aicte.sage.modules.documents.model.DocumentComplianceStatus;
import org.aicte.sage.modules.documents.model.DocumentType;
import org.aicte.sage.modules.documents.repository.DocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Evaluates mandatory statutory document completeness and AI integrity (forgery risk)
 * to govern institutional application submission readiness under AICTE APH 2024-27 norms.
 */
@Service
@Transactional(readOnly = true)
public class SubmissionReadinessService {

    private static final Logger log = LoggerFactory.getLogger(SubmissionReadinessService.class);

    private final DocumentRepository documentRepository;

    public SubmissionReadinessService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    public List<StatutoryRequirementDefinitionDto> getCategoryRequirements(ApplicationCategory category) {
        List<StatutoryRequirementDefinitionDto> list = new ArrayList<>();
        if (category == null) {
            category = ApplicationCategory.NEW_COURSE_INTRODUCTION;
        }

        switch (category) {
            case NEW_COURSE_INTRODUCTION -> {
                list.add(new StatutoryRequirementDefinitionDto(
                        DocumentType.FIRE_SAFETY_CERTIFICATE,
                        "Fire Safety Certificate (NOC)",
                        "Valid fire safety clearance certificate from State / Municipal Fire Service Directorate.",
                        "APH Clause 2.4 / Annexure 4",
                        true
                ));
                list.add(new StatutoryRequirementDefinitionDto(
                        DocumentType.OCCUPANCY_CERTIFICATE,
                        "Building Occupancy / Completion Certificate",
                        "Sanctioned occupancy certificate issued by the competent town planning / municipal corporation.",
                        "APH Clause 2.4 / APH Chapter I",
                        true
                ));
                list.add(new StatutoryRequirementDefinitionDto(
                        DocumentType.BOARD_RESOLUTION,
                        "Governing Body / Board Resolution",
                        "Resolution passed by the applicant trust / society approving new course introduction and budget allocation.",
                        "APH Clause 2.4(a)",
                        true
                ));
                list.add(new StatutoryRequirementDefinitionDto(
                        DocumentType.FACULTY_LIST_QUALIFICATION,
                        "Faculty Cadre & Qualification Matrix",
                        "Roster of identified qualified faculty conforming to prescribed AICTE cadre ratio and eligibility.",
                        "APH Clause 2.4(c)",
                        true
                ));
                list.add(new StatutoryRequirementDefinitionDto(
                        DocumentType.LABORATORY_INFRASTRUCTURE_REPORT,
                        "Laboratory & Infrastructure Readiness Report",
                        "Readiness report detailing dedicated laboratories, software licenses, and computational equipment.",
                        "APH Clause 2.4(d)",
                        true
                ));
                list.add(new StatutoryRequirementDefinitionDto(
                        DocumentType.SYLLABUS_CURRICULUM_APPROVAL,
                        "University Syllabus & Affiliation Approval",
                        "Sanction / syllabus approval from the affiliating university / state technical examination board.",
                        "APH Clause 2.4(e)",
                        true
                ));
            }
            case NEW_INSTITUTION -> {
                list.add(new StatutoryRequirementDefinitionDto(
                        DocumentType.LAND_REGISTRATION,
                        "Land Title & Registration Deed",
                        "Registered ownership deed / 30-year lease deed in name of trust/society for contiguous campus land.",
                        "APH Chapter I, Section 1.3",
                        true
                ));
                list.add(new StatutoryRequirementDefinitionDto(
                        DocumentType.BUILDING_PLAN_APPROVAL,
                        "Approved Architectural Building Plan",
                        "Sanctioned architectural building plan by the competent municipal authority / urban development body.",
                        "APH Chapter I, Section 1.4",
                        true
                ));
                list.add(new StatutoryRequirementDefinitionDto(
                        DocumentType.OCCUPANCY_CERTIFICATE,
                        "Building Occupancy Certificate",
                        "Occupancy / completion certificate covering academic, administrative, and amenity built-up areas.",
                        "APH Chapter I, Section 1.5",
                        true
                ));
                list.add(new StatutoryRequirementDefinitionDto(
                        DocumentType.FIRE_SAFETY_CERTIFICATE,
                        "Fire Safety Certificate (NOC)",
                        "Comprehensive fire safety certificate covering all building blocks.",
                        "APH Chapter I, Section 1.6",
                        true
                ));
                list.add(new StatutoryRequirementDefinitionDto(
                        DocumentType.BOARD_RESOLUTION,
                        "Sponsoring Society / Trust Resolution",
                        "Governing body resolution resolving to establish the new technical institution.",
                        "APH Chapter I, Section 1.2",
                        true
                ));
                list.add(new StatutoryRequirementDefinitionDto(
                        DocumentType.AFFIDAVIT_FORMAT_1,
                        "Statutory AICTE Affidavit (Format 1)",
                        "Notarized statutory affidavit on non-judicial stamp paper as prescribed in AICTE APH Format 1.",
                        "APH Format 1",
                        true
                ));
            }
            case NEW_BRANCH_INTRODUCTION -> {
                list.add(new StatutoryRequirementDefinitionDto(
                        DocumentType.FIRE_SAFETY_CERTIFICATE,
                        "Fire Safety Certificate (NOC)",
                        "Valid fire safety clearance certificate covering the department premises.",
                        "APH Clause 2.5",
                        true
                ));
                list.add(new StatutoryRequirementDefinitionDto(
                        DocumentType.OCCUPANCY_CERTIFICATE,
                        "Building Occupancy Certificate",
                        "Municipal occupancy certificate verifying adequate built-up instructional space.",
                        "APH Clause 2.5",
                        true
                ));
                list.add(new StatutoryRequirementDefinitionDto(
                        DocumentType.BOARD_RESOLUTION,
                        "Board Resolution for New Specialization",
                        "Trust/governing board resolution resolving to start the new specialized branch.",
                        "APH Clause 2.5(a)",
                        true
                ));
                list.add(new StatutoryRequirementDefinitionDto(
                        DocumentType.FACULTY_LIST_QUALIFICATION,
                        "Department Faculty Cadre Matrix",
                        "List of qualified faculty identified for the proposed specialization conforming to AICTE norms.",
                        "APH Clause 2.5(c)",
                        true
                ));
                list.add(new StatutoryRequirementDefinitionDto(
                        DocumentType.LABORATORY_INFRASTRUCTURE_REPORT,
                        "Specialized Laboratory Infrastructure Report",
                        "Report certifying dedicated specialized equipment, hardware, and software packages.",
                        "APH Clause 2.5(d)",
                        true
                ));
            }
            case INCREASE_IN_INTAKE -> {
                list.add(new StatutoryRequirementDefinitionDto(
                        DocumentType.FIRE_SAFETY_CERTIFICATE,
                        "Fire Safety Certificate (NOC)",
                        "Current valid fire safety clearance certificate.",
                        "APH Clause 2.6",
                        true
                ));
                list.add(new StatutoryRequirementDefinitionDto(
                        DocumentType.OCCUPANCY_CERTIFICATE,
                        "Building Occupancy Certificate",
                        "Municipal occupancy certificate verifying adequate carpet area for expanded student capacity.",
                        "APH Clause 2.6",
                        true
                ));
                list.add(new StatutoryRequirementDefinitionDto(
                        DocumentType.BOARD_RESOLUTION,
                        "Governing Board Resolution for Intake Expansion",
                        "Resolution passed by the governing board sanctioning additional student intake.",
                        "APH Clause 2.6(a)",
                        true
                ));
                list.add(new StatutoryRequirementDefinitionDto(
                        DocumentType.LABORATORY_INFRASTRUCTURE_REPORT,
                        "Laboratory & Classroom Expansion Report",
                        "Infrastructure readiness report demonstrating expanded batch capacities.",
                        "APH Clause 2.6(b)",
                        true
                ));
            }
            case EXTENSION_OF_APPROVAL -> {
                list.add(new StatutoryRequirementDefinitionDto(
                        DocumentType.FIRE_SAFETY_CERTIFICATE,
                        "Fire Safety Certificate (NOC)",
                        "Current valid fire safety certificate / annual renewal receipt.",
                        "APH Chapter II",
                        true
                ));
                list.add(new StatutoryRequirementDefinitionDto(
                        DocumentType.OCCUPANCY_CERTIFICATE,
                        "Building Occupancy Certificate",
                        "Valid occupancy certificate for the existing operational campus.",
                        "APH Chapter II",
                        true
                ));
                list.add(new StatutoryRequirementDefinitionDto(
                        DocumentType.AFFIDAVIT_FORMAT_1,
                        "Annual AICTE Compliance Affidavit (Format 1)",
                        "Annual compliance affidavit on non-judicial stamp paper.",
                        "APH Format 1",
                        true
                ));
            }
            case NEW_PROGRAMME -> {
                list.add(new StatutoryRequirementDefinitionDto(
                        DocumentType.LAND_REGISTRATION,
                        "Land Title Deed / Master Plan",
                        "Registered land documents showing campus master planning for new programme.",
                        "APH Clause 2.3",
                        true
                ));
                list.add(new StatutoryRequirementDefinitionDto(
                        DocumentType.BUILDING_PLAN_APPROVAL,
                        "Approved Building Plan",
                        "Sanctioned architectural building plan for new programme block.",
                        "APH Clause 2.3",
                        true
                ));
                list.add(new StatutoryRequirementDefinitionDto(
                        DocumentType.OCCUPANCY_CERTIFICATE,
                        "Building Occupancy Certificate",
                        "Occupancy certificate for programme building.",
                        "APH Clause 2.3",
                        true
                ));
                list.add(new StatutoryRequirementDefinitionDto(
                        DocumentType.FIRE_SAFETY_CERTIFICATE,
                        "Fire Safety Certificate (NOC)",
                        "Fire safety clearance.",
                        "APH Clause 2.3",
                        true
                ));
                list.add(new StatutoryRequirementDefinitionDto(
                        DocumentType.BOARD_RESOLUTION,
                        "Governing Board Resolution",
                        "Board resolution approving expansion into new programme.",
                        "APH Clause 2.3(a)",
                        true
                ));
                list.add(new StatutoryRequirementDefinitionDto(
                        DocumentType.FACULTY_LIST_QUALIFICATION,
                        "Faculty Cadre & Qualification Matrix",
                        "Roster of faculty recruited/allocated for the new programme.",
                        "APH Clause 2.3(b)",
                        true
                ));
                list.add(new StatutoryRequirementDefinitionDto(
                        DocumentType.LABORATORY_INFRASTRUCTURE_REPORT,
                        "Laboratory Infrastructure Report",
                        "Technical report on new programme laboratory readiness.",
                        "APH Clause 2.3(c)",
                        true
                ));
            }
            case CHANGE_OF_SITE -> {
                list.add(new StatutoryRequirementDefinitionDto(
                        DocumentType.LAND_REGISTRATION,
                        "New Campus Land Title Deed",
                        "Registered title deed in trust's name for new site.",
                        "APH Clause 2.10",
                        true
                ));
                list.add(new StatutoryRequirementDefinitionDto(
                        DocumentType.BUILDING_PLAN_APPROVAL,
                        "Approved Architectural Building Plan",
                        "Sanctioned building plans for new campus location.",
                        "APH Clause 2.10",
                        true
                ));
                list.add(new StatutoryRequirementDefinitionDto(
                        DocumentType.OCCUPANCY_CERTIFICATE,
                        "New Site Occupancy Certificate",
                        "Municipal occupancy certificate for new campus buildings.",
                        "APH Clause 2.10",
                        true
                ));
                list.add(new StatutoryRequirementDefinitionDto(
                        DocumentType.FIRE_SAFETY_CERTIFICATE,
                        "Fire Safety Certificate (NOC)",
                        "Fire NOC for new premises.",
                        "APH Clause 2.10",
                        true
                ));
                list.add(new StatutoryRequirementDefinitionDto(
                        DocumentType.BOARD_RESOLUTION,
                        "Board Resolution for Site Change",
                        "Trust resolution approving relocation of institution.",
                        "APH Clause 2.10(a)",
                        true
                ));
            }
            case CLOSURE -> {
                list.add(new StatutoryRequirementDefinitionDto(
                        DocumentType.BOARD_RESOLUTION,
                        "Governing Board Closure Resolution",
                        "Resolution approving closure of programme/institution.",
                        "APH Clause 2.15",
                        true
                ));
                list.add(new StatutoryRequirementDefinitionDto(
                        DocumentType.AFFIDAVIT_FORMAT_1,
                        "AICTE Affidavit Format 1 (No Student Pendency)",
                        "Affidavit certifying no ongoing students are displaced without transfer arrangement.",
                        "APH Clause 2.15(a)",
                        true
                ));
                list.add(new StatutoryRequirementDefinitionDto(
                        DocumentType.FINANCIAL_AUDIT_REPORT,
                        "Statutory Financial Audit Report",
                        "Audited balance sheet and financial clearance statement.",
                        "APH Clause 2.15(b)",
                        true
                ));
            }
        }
        return list;
    }

    public SubmissionReadinessDto evaluateSubmissionReadiness(Application application) {
        List<StatutoryRequirementDefinitionDto> requirements = getCategoryRequirements(application.getCategory());
        List<Document> uploadedDocs = documentRepository.findByApplicationIdOrderByCreatedAtDesc(application.getId());

        // Map latest version by document type
        Map<DocumentType, Document> docByType = new HashMap<>();
        for (Document d : uploadedDocs) {
            if (d.getDocumentType() != null && !docByType.containsKey(d.getDocumentType())) {
                docByType.put(d.getDocumentType(), d);
            }
        }

        List<String> missingTypes = new ArrayList<>();
        List<String> missingNames = new ArrayList<>();
        List<String> highRiskDocs = new ArrayList<>();
        List<RequiredDocumentItemDto> checklist = new ArrayList<>();

        int totalUploaded = 0;

        for (StatutoryRequirementDefinitionDto req : requirements) {
            Document doc = docByType.get(req.documentType());
            boolean isUploaded = (doc != null);
            if (isUploaded) {
                totalUploaded++;
            } else if (req.isMandatory()) {
                missingTypes.add(req.documentType().name());
                missingNames.add(req.displayName());
            }

            boolean isHighRisk = false;
            String forgeryRisk = "LOW";
            String complianceStatus = "COMPLIANT";

            if (doc != null) {
                isHighRisk = isDocumentHighRisk(doc);
                forgeryRisk = extractForgeryRisk(doc, isHighRisk);
                if (doc.getComplianceStatus() != null) {
                    complianceStatus = doc.getComplianceStatus().name();
                }
                if (isHighRisk) {
                    highRiskDocs.add(req.displayName() + " (" + doc.getOriginalFilename() + ")");
                }
            }

            checklist.add(new RequiredDocumentItemDto(
                    req.documentType(),
                    req.displayName(),
                    req.description(),
                    req.aphClause(),
                    req.isMandatory(),
                    isUploaded,
                    doc != null ? doc.getId() : null,
                    doc != null ? doc.getDocumentName() : null,
                    doc != null ? doc.getOriginalFilename() : null,
                    forgeryRisk,
                    isHighRisk,
                    complianceStatus,
                    doc != null && doc.getCreatedAt() != null ? doc.getCreatedAt().toString() : null
            ));
        }

        // Also check any extra supporting documents (OTHER_SUPPORTING) for high risk
        for (Document d : uploadedDocs) {
            if (d.getDocumentType() == DocumentType.OTHER_SUPPORTING && isDocumentHighRisk(d)) {
                String label = d.getDocumentName() + " (" + d.getOriginalFilename() + ")";
                if (!highRiskDocs.contains(label)) {
                    highRiskDocs.add(label);
                }
            }
        }

        boolean canSubmit = missingNames.isEmpty() && highRiskDocs.isEmpty();
        String blockingReason = null;

        if (!missingNames.isEmpty() && !highRiskDocs.isEmpty()) {
            blockingReason = "Cannot submit: " + missingNames.size() + " mandatory document(s) missing ("
                    + String.join(", ", missingNames) + ") AND high AI forgery/tampering risk detected in: "
                    + String.join(", ", highRiskDocs) + ".";
        } else if (!missingNames.isEmpty()) {
            blockingReason = "Cannot submit: " + missingNames.size() + " mandatory statutory document(s) missing: "
                    + String.join(", ", missingNames) + ". Please upload all required certificates before submission.";
        } else if (!highRiskDocs.isEmpty()) {
            blockingReason = "Cannot submit: SAGE AI detected high digital tampering / forgery risk in: "
                    + String.join(", ", highRiskDocs) + ". Please replace flagged documents with authentic certified copies.";
        }

        return new SubmissionReadinessDto(
                application.getId(),
                application.getApplicationNumber(),
                application.getCategory(),
                formatCategoryName(application.getCategory()),
                application.getCurrentState(),
                canSubmit,
                requirements.size(),
                totalUploaded,
                missingTypes,
                missingNames,
                highRiskDocs,
                checklist,
                blockingReason
        );
    }

    public void validateForSubmission(Application application) {
        SubmissionReadinessDto readiness = evaluateSubmissionReadiness(application);
        if (!readiness.canSubmit()) {
            log.warn("Application {} failed submission gate: {}", application.getApplicationNumber(), readiness.blockingReason());
            throw new IllegalArgumentException(readiness.blockingReason());
        }
    }

    public static boolean isDocumentHighRisk(Document doc) {
        if (doc == null) return false;

        // Check compliance status
        if (doc.getComplianceStatus() == DocumentComplianceStatus.EXPIRED) {
            return true;
        }

        // Check AI analysis report
        String report = doc.getAiAnalysisReport();
        if (report != null && !report.isBlank()) {
            String upper = report.toUpperCase();
            if (upper.contains("\"FORGERYRISK\": \"HIGH\"") ||
                upper.contains("\"FORGERYRISK\":\"HIGH\"") ||
                upper.contains("HIGH POTENTIAL RISK") ||
                upper.contains("HIGH RISK OF DIGITAL MODIFICATION") ||
                upper.contains("POTENTIAL FORGERY") ||
                upper.contains("REVIEW RECOMMENDED (HIGH") ||
                upper.contains("FORGERY RISK: HIGH") ||
                upper.contains("INTEGRITY STATUS: REVIEW RECOMMENDED")) {
                return true;
            }
        }

        // Local filename heuristics
        String fn = doc.getOriginalFilename();
        if (fn != null) {
            String lower = fn.toLowerCase();
            if (lower.contains("fake") || lower.contains("tamper") || lower.contains("photoshop")
                    || lower.contains("modified_seal") || lower.contains("altered") || lower.contains("test_forgery")) {
                return true;
            }
        }

        return false;
    }

    private String extractForgeryRisk(Document doc, boolean isHigh) {
        if (isHigh) return "HIGH";
        String report = doc.getAiAnalysisReport();
        if (report != null) {
            String upper = report.toUpperCase();
            if (upper.contains("\"FORGERYRISK\": \"MEDIUM\"") || upper.contains("\"FORGERYRISK\":\"MEDIUM\"")) {
                return "MEDIUM";
            }
        }
        return "LOW";
    }

    private String formatCategoryName(ApplicationCategory category) {
        if (category == null) return "Approval Application";
        return switch (category) {
            case NEW_COURSE_INTRODUCTION -> "Introduction of New Course / Discipline";
            case NEW_BRANCH_INTRODUCTION -> "Introduction of New Branch / Specialization";
            case NEW_INSTITUTION -> "Setting up New Technical Institution";
            case EXTENSION_OF_APPROVAL -> "Extension of Approval (EoA)";
            case INCREASE_IN_INTAKE -> "Increase in Intake / Additional Division";
            case NEW_PROGRAMME -> "Introduction of New Programme";
            case CHANGE_OF_SITE -> "Change of Site / Location";
            case CLOSURE -> "Closure of Institution / Programme";
        };
    }
}
