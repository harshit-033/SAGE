package org.aicte.sage.modules.applications;

import org.aicte.sage.modules.applications.dto.StatutoryRequirementDefinitionDto;
import org.aicte.sage.modules.applications.dto.SubmissionReadinessDto;
import org.aicte.sage.modules.applications.model.Application;
import org.aicte.sage.modules.applications.model.ApplicationCategory;
import org.aicte.sage.modules.applications.model.ApplicationState;
import org.aicte.sage.modules.applications.service.SubmissionReadinessService;
import org.aicte.sage.modules.documents.model.Document;
import org.aicte.sage.modules.documents.model.DocumentType;
import org.aicte.sage.modules.documents.repository.DocumentRepository;
import org.aicte.sage.modules.institutions.entity.Institution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubmissionReadinessServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    private SubmissionReadinessService service;

    private Application testApp;
    private final UUID appId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new SubmissionReadinessService(documentRepository);

        Institution inst = new Institution();
        inst.setId(UUID.randomUUID());
        inst.setName("Delhi Institute of Advanced Engineering & Technology");

        testApp = new Application();
        testApp.setId(appId);
        testApp.setApplicationNumber("APP-2024-TEST-001");
        testApp.setInstitution(inst);
        testApp.setCategory(ApplicationCategory.NEW_COURSE_INTRODUCTION);
        testApp.setCurrentState(ApplicationState.DRAFT);
    }

    @Test
    @DisplayName("Should return statutory requirements checklist for New Course Introduction")
    void testCategoryRequirementsForNewCourse() {
        List<StatutoryRequirementDefinitionDto> reqs = service.getCategoryRequirements(ApplicationCategory.NEW_COURSE_INTRODUCTION);
        assertNotNull(reqs);
        assertEquals(6, reqs.size());

        List<DocumentType> types = reqs.stream().map(StatutoryRequirementDefinitionDto::documentType).toList();
        assertTrue(types.contains(DocumentType.FIRE_SAFETY_CERTIFICATE));
        assertTrue(types.contains(DocumentType.OCCUPANCY_CERTIFICATE));
        assertTrue(types.contains(DocumentType.BOARD_RESOLUTION));
        assertTrue(types.contains(DocumentType.FACULTY_LIST_QUALIFICATION));
        assertTrue(types.contains(DocumentType.LABORATORY_INFRASTRUCTURE_REPORT));
        assertTrue(types.contains(DocumentType.SYLLABUS_CURRICULUM_APPROVAL));
    }

    @Test
    @DisplayName("Should block submission when mandatory documents are missing")
    void testBlocksSubmissionWhenDocsMissing() {
        when(documentRepository.findByApplicationIdOrderByCreatedAtDesc(appId)).thenReturn(Collections.emptyList());

        SubmissionReadinessDto readiness = service.evaluateSubmissionReadiness(testApp);
        assertFalse(readiness.canSubmit());
        assertEquals(6, readiness.totalRequired());
        assertEquals(0, readiness.totalUploaded());
        assertEquals(6, readiness.missingDocumentTypes().size());
        assertNotNull(readiness.blockingReason());
        assertTrue(readiness.blockingReason().contains("6 mandatory statutory document(s) missing"));

        assertThrows(IllegalArgumentException.class, () -> service.validateForSubmission(testApp));
    }

    @Test
    @DisplayName("Should block submission when a document has HIGH forgery / tampering risk")
    void testBlocksSubmissionWhenDocumentHasHighForgeryRisk() {
        Document d1 = createCleanDoc(DocumentType.FIRE_SAFETY_CERTIFICATE, "fire_noc.pdf");
        Document d2 = createCleanDoc(DocumentType.OCCUPANCY_CERTIFICATE, "occupancy.pdf");
        Document d3 = createCleanDoc(DocumentType.BOARD_RESOLUTION, "resolution.pdf");
        Document d4 = createCleanDoc(DocumentType.FACULTY_LIST_QUALIFICATION, "faculty.pdf");
        // Tampered doc
        Document d5 = createDoc(DocumentType.LABORATORY_INFRASTRUCTURE_REPORT, "lab_report_fake.pdf",
                "### SAGE AI findings\n**Integrity Status:** REVIEW RECOMMENDED (High Potential Risk)\nHigh risk of digital modification");
        Document d6 = createCleanDoc(DocumentType.SYLLABUS_CURRICULUM_APPROVAL, "syllabus.pdf");

        when(documentRepository.findByApplicationIdOrderByCreatedAtDesc(appId)).thenReturn(List.of(d1, d2, d3, d4, d5, d6));

        SubmissionReadinessDto readiness = service.evaluateSubmissionReadiness(testApp);
        assertFalse(readiness.canSubmit());
        assertEquals(0, readiness.missingDocumentNames().size());
        assertEquals(1, readiness.highRiskDocumentNames().size());
        assertTrue(readiness.blockingReason().contains("high digital tampering / forgery risk"));

        assertThrows(IllegalArgumentException.class, () -> service.validateForSubmission(testApp));
    }

    @Test
    @DisplayName("Should permit submission when all mandatory documents are uploaded with clean low risk")
    void testPermitsSubmissionWhenAllDocsUploadedAndClean() {
        Document d1 = createCleanDoc(DocumentType.FIRE_SAFETY_CERTIFICATE, "fire_noc.pdf");
        Document d2 = createCleanDoc(DocumentType.OCCUPANCY_CERTIFICATE, "occupancy.pdf");
        Document d3 = createCleanDoc(DocumentType.BOARD_RESOLUTION, "resolution.pdf");
        Document d4 = createCleanDoc(DocumentType.FACULTY_LIST_QUALIFICATION, "faculty.pdf");
        Document d5 = createCleanDoc(DocumentType.LABORATORY_INFRASTRUCTURE_REPORT, "lab_report.pdf");
        Document d6 = createCleanDoc(DocumentType.SYLLABUS_CURRICULUM_APPROVAL, "syllabus.pdf");

        when(documentRepository.findByApplicationIdOrderByCreatedAtDesc(appId)).thenReturn(List.of(d1, d2, d3, d4, d5, d6));

        SubmissionReadinessDto readiness = service.evaluateSubmissionReadiness(testApp);
        assertTrue(readiness.canSubmit());
        assertEquals(6, readiness.totalUploaded());
        assertTrue(readiness.missingDocumentNames().isEmpty());
        assertTrue(readiness.highRiskDocumentNames().isEmpty());
        assertNull(readiness.blockingReason());

        assertDoesNotThrow(() -> service.validateForSubmission(testApp));
    }

    private Document createCleanDoc(DocumentType type, String filename) {
        return createDoc(type, filename, "### SAGE AI findings\n**Integrity Status:** VERIFIED (Low Risk)\n**NO FORGERY INDICATORS DETECTED**");
    }

    private Document createDoc(DocumentType type, String filename, String aiReport) {
        Document doc = new Document();
        doc.setId(UUID.randomUUID());
        doc.setDocumentType(type);
        doc.setOriginalFilename(filename);
        doc.setDocumentName(filename);
        doc.setAiAnalysisReport(aiReport);
        doc.setApplication(testApp);
        return doc;
    }
}
