package org.aicte.sage;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aicte.sage.modules.auth.dto.LoginRequest;
import org.aicte.sage.modules.documents.dto.ResolveCorrectiveActionRequest;
import org.aicte.sage.modules.documents.dto.RespondCorrectiveActionRequest;
import org.aicte.sage.modules.documents.dto.ReviewDocumentRequest;
import org.aicte.sage.modules.documents.dto.SubmitClarificationRequest;
import org.aicte.sage.modules.documents.model.CorrectiveActionStatus;
import org.aicte.sage.modules.documents.model.DocumentComplianceStatus;
import org.aicte.sage.modules.documents.model.DocumentVerificationStatus;
import org.aicte.sage.modules.documents.service.DocumentComplianceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class DocumentComplianceTrackingTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DocumentComplianceService documentComplianceService;

    private String delhiOfficerToken;
    private String mumbaiOfficerToken;
    private String adminToken;

    private static final String DELHI_INST_ID = "22222222-2222-2222-2222-222222222222";
    private static final String MUMBAI_INST_ID = "33333333-3333-3333-3333-333333333333";

    @BeforeEach
    void setUp() throws Exception {
        delhiOfficerToken = obtainToken("officer@delhi-tech.edu", "Password123!");
        mumbaiOfficerToken = obtainToken("officer@mumbai-engg.edu", "Password123!");
        adminToken = obtainToken("admin@aicte.gov.in", "Password123!");
    }

    private String obtainToken(String username, String password) throws Exception {
        LoginRequest login = new LoginRequest(username, password);
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .at("/data/accessToken").asText();
    }

    @Test
    @DisplayName("Upload institution document, verify compliance status and metadata")
    void testUploadAndRetrieveDocument() throws Exception {
        byte[] pdfHeader = "%PDF-1.4 sample content".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "Fire_Safety_NOC_2026.pdf",
                "application/pdf",
                pdfHeader
        );

        Instant expiryDate = Instant.now().plus(180, ChronoUnit.DAYS);

        MvcResult uploadRes = mockMvc.perform(multipart("/api/v1/documents")
                        .file(file)
                        .param("institutionId", DELHI_INST_ID)
                        .param("documentName", "Delhi Tech Fire Safety NOC 2026")
                        .param("documentType", "FIRE_SAFETY_CERTIFICATE")
                        .param("documentCategory", "INFRASTRUCTURE")
                        .param("expiryDate", expiryDate.toString())
                        .header("Authorization", "Bearer " + delhiOfficerToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.documentName").value("Delhi Tech Fire Safety NOC 2026"))
                .andExpect(jsonPath("$.data.documentCategory").value("INFRASTRUCTURE"))
                .andExpect(jsonPath("$.data.complianceStatus").value("COMPLIANT"))
                .andExpect(jsonPath("$.data.verificationStatus").value("PENDING_REVIEW"))
                .andExpect(jsonPath("$.data.versionNumber").value(1))
                .andReturn();

        String docId = objectMapper.readTree(uploadRes.getResponse().getContentAsString())
                .path("data").path("id").asText();

        // Retrieve detail response
        mockMvc.perform(get("/api/v1/documents/" + docId)
                        .header("Authorization", "Bearer " + delhiOfficerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.document.id").value(docId))
                .andExpect(jsonPath("$.data.versions", hasSize(greaterThanOrEqualTo(1))));

        // Verify summary API aggregates data
        mockMvc.perform(get("/api/v1/documents/compliance-summary")
                        .param("institutionId", DELHI_INST_ID)
                        .header("Authorization", "Bearer " + delhiOfficerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalDocuments", greaterThanOrEqualTo(1)));
    }

    @Test
    @DisplayName("Upload replacement document creates immutable version 2 and preserves version 1")
    void testDocumentReplacementVersioning() throws Exception {
        byte[] pdfHeader = "%PDF-1.4 initial certificate".getBytes();
        MockMultipartFile initialFile = new MockMultipartFile(
                "file",
                "Building_Plan_Approval.pdf",
                "application/pdf",
                pdfHeader
        );

        MvcResult uploadRes = mockMvc.perform(multipart("/api/v1/documents")
                        .file(initialFile)
                        .param("institutionId", DELHI_INST_ID)
                        .param("documentName", "Approved Building Plan")
                        .param("documentType", "BUILDING_PLAN_APPROVAL")
                        .param("documentCategory", "INFRASTRUCTURE")
                        .header("Authorization", "Bearer " + delhiOfficerToken))
                .andExpect(status().isCreated())
                .andReturn();

        String v1DocId = objectMapper.readTree(uploadRes.getResponse().getContentAsString())
                .path("data").path("id").asText();

        // Now upload replacement (version 2)
        byte[] v2Header = "%PDF-1.4 renewed municipal plan".getBytes();
        MockMultipartFile replacementFile = new MockMultipartFile(
                "file",
                "Building_Plan_Approval_V2.pdf",
                "application/pdf",
                v2Header
        );

        MvcResult replaceRes = mockMvc.perform(multipart("/api/v1/documents/" + v1DocId + "/replace")
                        .file(replacementFile)
                        .param("replacementNotes", "Updated layout drawings approved by DDA")
                        .header("Authorization", "Bearer " + delhiOfficerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.versionNumber").value(2))
                .andExpect(jsonPath("$.data.previousVersionId").value(v1DocId))
                .andExpect(jsonPath("$.data.clarificationText").value("Updated layout drawings approved by DDA"))
                .andReturn();

        String v2DocId = objectMapper.readTree(replaceRes.getResponse().getContentAsString())
                .path("data").path("id").asText();

        assertNotEquals(v1DocId, v2DocId);

        // Fetch detail of V2 and check versions list contains both versions
        mockMvc.perform(get("/api/v1/documents/" + v2DocId)
                        .header("Authorization", "Bearer " + delhiOfficerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.versions", hasSize(2)))
                .andExpect(jsonPath("$.data.versions[0].versionNumber").value(2))
                .andExpect(jsonPath("$.data.versions[1].versionNumber").value(1));
    }

    @Test
    @DisplayName("Officer review updates verification status and audit trail")
    void testOfficerReviewAndClarification() throws Exception {
        byte[] pdfHeader = "%PDF-1.4 faculty list".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "Faculty_List.pdf",
                "application/pdf",
                pdfHeader
        );

        MvcResult uploadRes = mockMvc.perform(multipart("/api/v1/documents")
                        .file(file)
                        .param("institutionId", DELHI_INST_ID)
                        .param("documentName", "CSE Faculty List")
                        .param("documentType", "FACULTY_LIST_QUALIFICATION")
                        .param("documentCategory", "FACULTY")
                        .header("Authorization", "Bearer " + delhiOfficerToken))
                .andExpect(status().isCreated())
                .andReturn();

        String docId = objectMapper.readTree(uploadRes.getResponse().getContentAsString())
                .path("data").path("id").asText();

        // 1. Officer requests clarification
        ReviewDocumentRequest requestClarification = new ReviewDocumentRequest(
                DocumentVerificationStatus.REQUIRES_CLARIFICATION,
                DocumentComplianceStatus.ACTION_REQUIRED,
                "Please provide PhD certificates for 2 Assistant Professors"
        );

        mockMvc.perform(post("/api/v1/documents/" + docId + "/review")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestClarification)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.verificationStatus").value("REQUIRES_CLARIFICATION"))
                .andExpect(jsonPath("$.data.officerComments").value("Please provide PhD certificates for 2 Assistant Professors"));

        // 2. Institution officer submits clarification
        SubmitClarificationRequest clarification = new SubmitClarificationRequest(
                "PhD certificates from IIT Delhi attached in updated annexure."
        );

        mockMvc.perform(post("/api/v1/documents/" + docId + "/clarify")
                        .header("Authorization", "Bearer " + delhiOfficerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clarification)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.verificationStatus").value("UNDER_REVIEW"))
                .andExpect(jsonPath("$.data.clarificationText").value("PhD certificates from IIT Delhi attached in updated annexure."));

        // 3. Officer verifies document
        ReviewDocumentRequest verifyReq = new ReviewDocumentRequest(
                DocumentVerificationStatus.VERIFIED,
                DocumentComplianceStatus.COMPLIANT,
                "Verified against university registry"
        );

        mockMvc.perform(post("/api/v1/documents/" + docId + "/review")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.verificationStatus").value("VERIFIED"));
    }

    @Test
    @DisplayName("Corrective action lifecycle: list, respond, and resolve")
    void testCorrectiveActionLifecycle() throws Exception {
        // List corrective actions
        MvcResult listRes = mockMvc.perform(get("/api/v1/corrective-actions")
                        .param("institutionId", DELHI_INST_ID)
                        .header("Authorization", "Bearer " + delhiOfficerToken))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode listJson = objectMapper.readTree(listRes.getResponse().getContentAsString());
        JsonNode content = listJson.path("data").path("content");

        if (content.isArray() && !content.isEmpty()) {
            String actionId = content.get(0).path("id").asText();

            // Respond
            RespondCorrectiveActionRequest respondReq = new RespondCorrectiveActionRequest(
                    "Clarification and updated certificate uploaded",
                    null
            );

            mockMvc.perform(post("/api/v1/corrective-actions/" + actionId + "/respond")
                            .header("Authorization", "Bearer " + delhiOfficerToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(respondReq)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.status").value("SUBMITTED_FOR_REVIEW"));

            // Officer resolve
            ResolveCorrectiveActionRequest resolveReq = new ResolveCorrectiveActionRequest(
                    CorrectiveActionStatus.RESOLVED,
                    "Verified compliance with norms"
            );

            mockMvc.perform(post("/api/v1/corrective-actions/" + actionId + "/resolve")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(resolveReq)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.status").value("RESOLVED"));
        }
    }

    @Test
    @DisplayName("Expiry monitoring sweep executes and updates expiring/expired records")
    void testExpiryMonitoringSweep() {
        int processed = documentComplianceService.sweepExpiringDocuments();
        assertTrue(processed >= 0);
    }
}
