package org.aicte.sage;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aicte.sage.modules.applications.dto.CreateApplicationRequest;
import org.aicte.sage.modules.applications.model.ApplicationCategory;
import org.aicte.sage.modules.applications.model.ApplicationState;
import org.aicte.sage.modules.applications.repository.ApplicationRepository;
import org.aicte.sage.modules.auth.dto.LoginRequest;
import org.aicte.sage.modules.documents.model.DocumentType;
import org.aicte.sage.modules.scrutiny.dto.ResolveFindingRequest;
import org.aicte.sage.modules.workflow.dto.TransitionRequest;
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

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class Phase3WorkflowAndScrutinyTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ApplicationRepository applicationRepository;

    private String adminToken;
    private String delhiOfficerToken;
    private String mumbaiOfficerToken;
    private String regionalToken;

    private static final String DELHI_INST_ID = "22222222-2222-2222-2222-222222222222";
    private static final String MUMBAI_INST_ID = "33333333-3333-3333-3333-333333333333";

    @BeforeEach
    void setUp() throws Exception {
        adminToken = obtainToken("admin@aicte.gov.in", "Password123!");
        delhiOfficerToken = obtainToken("officer@delhi-tech.edu", "Password123!");
        mumbaiOfficerToken = obtainToken("officer@mumbai-engg.edu", "Password123!");
        regionalToken = obtainToken("regional@aicte.gov.in", "Password123!");
    }

    private String obtainToken(String username, String password) throws Exception {
        LoginRequest req = new LoginRequest(username, password);
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .at("/data/accessToken").asText();
    }

    @Test
    @DisplayName("Stage B: Create application initializes in DRAFT state with unique application number")
    void testApplicationCreationDraftState() throws Exception {
        CreateApplicationRequest req = new CreateApplicationRequest(
                UUID.fromString(DELHI_INST_ID),
                "2024-25",
                ApplicationCategory.EXTENSION_OF_APPROVAL
        );

        mockMvc.perform(post("/api/v1/applications")
                        .header("Authorization", "Bearer " + delhiOfficerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.currentState").value("DRAFT"))
                .andExpect(jsonPath("$.data.applicationNumber", containsString("APP-2024")))
                .andExpect(jsonPath("$.data.institutionId").value(DELHI_INST_ID))
                .andExpect(jsonPath("$.data.category").value("EXTENSION_OF_APPROVAL"));
    }

    @Test
    @DisplayName("Stage B: Cross-tenant application access is strictly forbidden")
    void testCrossTenantApplicationAccessDenied() throws Exception {
        // Create an application for Delhi
        CreateApplicationRequest req = new CreateApplicationRequest(
                UUID.fromString(DELHI_INST_ID),
                "2024-25",
                ApplicationCategory.EXTENSION_OF_APPROVAL
        );

        MvcResult createResult = mockMvc.perform(post("/api/v1/applications")
                        .header("Authorization", "Bearer " + delhiOfficerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();

        String appId = objectMapper.readTree(createResult.getResponse().getContentAsString()).at("/data/id").asText();

        // Mumbai officer attempts to view Delhi's application -> 403 Forbidden
        mockMvc.perform(get("/api/v1/applications/" + appId)
                        .header("Authorization", "Bearer " + mumbaiOfficerToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail", containsString("Cross-tenant access violation")));
    }

    @Test
    @DisplayName("Stage B: Institution Officer can transition DRAFT -> SUBMITTED and audit history is recorded")
    void testApplicationSubmissionWorkflow() throws Exception {
        CreateApplicationRequest req = new CreateApplicationRequest(
                UUID.fromString(DELHI_INST_ID),
                "2024-25",
                ApplicationCategory.EXTENSION_OF_APPROVAL
        );

        MvcResult createResult = mockMvc.perform(post("/api/v1/applications")
                        .header("Authorization", "Bearer " + delhiOfficerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();

        String appId = objectMapper.readTree(createResult.getResponse().getContentAsString()).at("/data/id").asText();

        // Submit the application
        mockMvc.perform(post("/api/v1/applications/" + appId + "/submit")
                        .header("Authorization", "Bearer " + delhiOfficerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fromState").value("DRAFT"))
                .andExpect(jsonPath("$.data.toState").value("SUBMITTED"));

        // Check transition history
        mockMvc.perform(get("/api/v1/applications/" + appId + "/transitions")
                        .header("Authorization", "Bearer " + delhiOfficerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data[0].toState").value("SUBMITTED"));
    }

    @Test
    @DisplayName("Stage B: Illegal workflow transition directly to APPROVED is blocked")
    void testIllegalWorkflowTransitionBlocked() throws Exception {
        CreateApplicationRequest req = new CreateApplicationRequest(
                UUID.fromString(DELHI_INST_ID),
                "2024-25",
                ApplicationCategory.EXTENSION_OF_APPROVAL
        );

        MvcResult createResult = mockMvc.perform(post("/api/v1/applications")
                        .header("Authorization", "Bearer " + delhiOfficerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();

        String appId = objectMapper.readTree(createResult.getResponse().getContentAsString()).at("/data/id").asText();

        // Attempt illegal jump from DRAFT -> APPROVED
        TransitionRequest illegalReq = new TransitionRequest(ApplicationState.APPROVED, "Attempted illegal fast-track approval", null);

        mockMvc.perform(post("/api/v1/applications/" + appId + "/transitions")
                        .header("Authorization", "Bearer " + delhiOfficerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(illegalReq)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Stage C4: Secure document upload verifies magic bytes and stores file safely")
    void testDocumentUploadWithMagicByteVerification() throws Exception {
        // Seed application for Delhi
        String appId = "bbbbbbbb-bbbb-bbbb-bbbb-000000000001";

        // Valid PDF mock bytes (%PDF-1.4 header)
        byte[] validPdfBytes = "%PDF-1.4\n1 0 obj\n<< /Type /Catalog >>\nendobj\ntrailer\n<< >>\n%%EOF".getBytes();
        MockMultipartFile validFile = new MockMultipartFile(
                "file", "fire_safety_noc.pdf", "application/pdf", validPdfBytes
        );

        mockMvc.perform(multipart("/api/v1/applications/" + appId + "/documents")
                        .file(validFile)
                        .param("documentType", "FIRE_SAFETY_CERTIFICATE")
                        .header("Authorization", "Bearer " + delhiOfficerToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.documentType").value("FIRE_SAFETY_CERTIFICATE"))
                .andExpect(jsonPath("$.data.originalFilename").value("fire_safety_noc.pdf"))
                .andExpect(jsonPath("$.data.sha256Hash", notNullValue()));
    }

    @Test
    @DisplayName("Stage C4: Document upload rejects corrupt file with invalid signature")
    void testCorruptDocumentUploadRejected() throws Exception {
        String appId = "bbbbbbbb-bbbb-bbbb-bbbb-000000000001";

        // Fake PDF with invalid magic bytes
        byte[] fakePdfBytes = "MALICIOUS_EXE_CONTENT_NOT_A_PDF".getBytes();
        MockMultipartFile fakeFile = new MockMultipartFile(
                "file", "fake.pdf", "application/pdf", fakePdfBytes
        );

        mockMvc.perform(multipart("/api/v1/applications/" + appId + "/documents")
                        .file(fakeFile)
                        .param("documentType", "FIRE_SAFETY_CERTIFICATE")
                        .header("Authorization", "Bearer " + delhiOfficerToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", containsString("Invalid PDF file signature")));
    }

    @Test
    @DisplayName("Stage D: Scrutiny rule evaluation detects missing mandatory documents and flags DEFICIENT")
    void testScrutinyEvaluationDetectsMissingDocuments() throws Exception {
        // Create clean application with 0 documents
        CreateApplicationRequest req = new CreateApplicationRequest(
                UUID.fromString(DELHI_INST_ID),
                "2024-25",
                ApplicationCategory.NEW_INSTITUTION
        );

        MvcResult createResult = mockMvc.perform(post("/api/v1/applications")
                        .header("Authorization", "Bearer " + delhiOfficerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();

        String appId = objectMapper.readTree(createResult.getResponse().getContentAsString()).at("/data/id").asText();

        // Scrutiny evaluation by Regional Officer
        mockMvc.perform(post("/api/v1/applications/" + appId + "/scrutiny/evaluate")
                        .header("Authorization", "Bearer " + regionalToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("DEFICIENT"))
                .andExpect(jsonPath("$.data.findings", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data.findings[?(@.ruleId == 'RULE_APH_MANDATORY_DOCS')].status").value("DEFICIENT"));
    }

    @Test
    @DisplayName("Stage D & E: Authorized Scrutiny Officer resolves deficiency finding with audit justification")
    void testScrutinyFindingOfficerResolution() throws Exception {
        String appId = "bbbbbbbb-bbbb-bbbb-bbbb-000000000001";

        // Run scrutiny evaluation first
        MvcResult evalResult = mockMvc.perform(post("/api/v1/applications/" + appId + "/scrutiny/evaluate")
                        .header("Authorization", "Bearer " + regionalToken))
                .andExpect(status().isOk())
                .andReturn();

        String findingId = objectMapper.readTree(evalResult.getResponse().getContentAsString())
                .at("/data/findings/0/id").asText();

        // Resolve finding with justification notes
        ResolveFindingRequest resolveReq = new ResolveFindingRequest("Verified original physical certificate during committee inspection per APH norms");

        mockMvc.perform(post("/api/v1/applications/" + appId + "/scrutiny/findings/" + findingId + "/resolve")
                        .header("Authorization", "Bearer " + regionalToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resolveReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.resolved").value(true))
                .andExpect(jsonPath("$.data.resolutionNotes", containsString("Verified original physical certificate")));
    }
}
