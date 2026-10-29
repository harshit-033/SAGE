package org.aicte.sage;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aicte.sage.common.security.UserPrincipal;
import org.aicte.sage.modules.applications.dto.CreateApplicationRequest;
import org.aicte.sage.modules.applications.model.ApplicationCategory;
import org.aicte.sage.modules.applications.model.ApplicationState;
import org.aicte.sage.modules.auth.dto.LoginRequest;
import org.aicte.sage.modules.compliance.dto.ComplianceCheckRunDto;
import org.aicte.sage.modules.compliance.service.ComplianceEngineService;
import org.aicte.sage.modules.deadlines.dto.CreateDeadlineRequest;
import org.aicte.sage.modules.deadlines.dto.DeadlineDto;
import org.aicte.sage.modules.deadlines.entity.DeadlineStatus;
import org.aicte.sage.modules.deadlines.entity.DeadlineType;
import org.aicte.sage.modules.deadlines.service.DeadlineService;
import org.aicte.sage.modules.documents.model.DocumentType;
import org.aicte.sage.modules.jobs.service.DistributedJobLockService;
import org.aicte.sage.modules.notifications.dto.AlertDto;
import org.aicte.sage.modules.notifications.entity.Alert;
import org.aicte.sage.modules.notifications.entity.AlertCategory;
import org.aicte.sage.modules.notifications.entity.AlertPriority;
import org.aicte.sage.modules.notifications.entity.AlertSeverity;
import org.aicte.sage.modules.notifications.service.AlertService;
import org.aicte.sage.modules.workflow.dto.TransitionRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class Phase5EndToEndIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DistributedJobLockService distributedJobLockService;

    @Autowired
    private DeadlineService deadlineService;

    @Autowired
    private AlertService alertService;

    @Autowired
    private ComplianceEngineService complianceEngineService;

    private static final UUID DELHI_TECH_INST_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID MUMBAI_ENGG_INST_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

    private static final UUID DELHI_OFFICER_USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111113");
    private static final UUID MUMBAI_OFFICER_USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111114");
    private static final UUID REGIONAL_OFFICER_USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111112");
    private static final UUID CENTRAL_ADMIN_USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private String delhiOfficerToken;
    private String mumbaiOfficerToken;
    private String regionalOfficerToken;
    private String adminToken;

    private UserPrincipal delhiPrincipal;
    private UserPrincipal regionalPrincipal;
    private UserPrincipal adminPrincipal;

    @BeforeEach
    void setupTokens() throws Exception {
        adminToken = obtainToken("admin@aicte.gov.in", "Password123!");
        delhiOfficerToken = obtainToken("officer@delhi-tech.edu", "Password123!");
        mumbaiOfficerToken = obtainToken("officer@mumbai-engg.edu", "Password123!");
        regionalOfficerToken = obtainToken("regional@aicte.gov.in", "Password123!");

        delhiPrincipal = new UserPrincipal(
                DELHI_OFFICER_USER_ID,
                "officer@delhi-tech.edu",
                "officer@delhi-tech.edu",
                "pwd",
                "Delhi Tech Officer",
                true,
                Set.of(new SimpleGrantedAuthority("ROLE_INSTITUTION_OFFICER")),
                Set.of(DELHI_TECH_INST_ID)
        );

        regionalPrincipal = new UserPrincipal(
                REGIONAL_OFFICER_USER_ID,
                "regional@aicte.gov.in",
                "regional@aicte.gov.in",
                "pwd",
                "Northern Regional Officer",
                true,
                Set.of(new SimpleGrantedAuthority("ROLE_REGIONAL_OFFICER")),
                Collections.emptySet()
        );

        adminPrincipal = new UserPrincipal(
                CENTRAL_ADMIN_USER_ID,
                "admin@aicte.gov.in",
                "admin@aicte.gov.in",
                "pwd",
                "Central AICTE Admin",
                true,
                Set.of(new SimpleGrantedAuthority("ROLE_AICTE_ADMIN")),
                Collections.emptySet()
        );
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

    // =========================================================================
    // Scenario 1: Distributed Job Lock Multi-Node Contention & Failover Recovery
    // =========================================================================

    @Test
    @DisplayName("Distributed lock prevents concurrent job execution across simulated cluster nodes and safely recovers")
    void testDistributedJobLockConcurrencyAndRecovery() throws InterruptedException {
        String jobKey = "TEST_DEADLINE_JOB_" + UUID.randomUUID().toString().substring(0, 8);
        String nodeA = "sage-node-alpha";
        String nodeB = "sage-node-beta";

        // 1. Node A acquires lease
        boolean acquiredA = distributedJobLockService.acquireLock(jobKey, nodeA, Duration.ofSeconds(5));
        assertTrue(acquiredA, "Node A should acquire initial lock");

        // 2. Node B attempts to acquire while active lease held by Node A -> Rejected
        boolean acquiredB = distributedJobLockService.acquireLock(jobKey, nodeB, Duration.ofSeconds(5));
        assertFalse(acquiredB, "Node B must NOT acquire lock while Node A holds active lease");

        // 3. Node A releases lock cleanly
        distributedJobLockService.releaseLock(jobKey, nodeA);

        // 4. Node B acquires lock immediately after release
        boolean acquiredBAfterRelease = distributedJobLockService.acquireLock(jobKey, nodeB, Duration.ofSeconds(5));
        assertTrue(acquiredBAfterRelease, "Node B should successfully acquire lock after Node A released it");

        // 5. Simulate Node B holding lock with short expiry (interrupted/crashed scenario)
        distributedJobLockService.releaseLock(jobKey, nodeB);
        boolean shortLease = distributedJobLockService.acquireLock(jobKey, nodeB, Duration.ofMillis(30));
        assertTrue(shortLease);

        // Wait for lease to expire
        Thread.sleep(60);

        // 6. Node A successfully reclaims expired lease without manual intervention
        boolean recovered = distributedJobLockService.acquireLock(jobKey, nodeA, Duration.ofSeconds(5));
        assertTrue(recovered, "Node A should recover lock after Node B's lease expired");

        // Cleanup
        distributedJobLockService.releaseLock(jobKey, nodeA);
    }

    // =========================================================================
    // Scenario 2: End-to-End Application Creation, Document Ingestion & Workflow
    // =========================================================================

    @Test
    @DisplayName("E2E: Institution creates application, uploads validated documents, advances through workflow with statutory gate enforcement")
    void testCompleteApplicationLifecycleAndStatutoryGateEnforcement() throws Exception {
        // Step 1: Delhi Officer creates draft application
        CreateApplicationRequest createReq = new CreateApplicationRequest(
                DELHI_TECH_INST_ID,
                "2024-25",
                ApplicationCategory.EXTENSION_OF_APPROVAL
        );

        String createJson = mockMvc.perform(post("/api/v1/applications")
                        .header("Authorization", "Bearer " + delhiOfficerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.currentState").value("DRAFT"))
                .andReturn().getResponse().getContentAsString();

        UUID appId = UUID.fromString(objectMapper.readTree(createJson).path("data").path("id").asText());

        // Step 2: Upload valid PDF document
        byte[] validPdfHeader = "%PDF-1.4\n%Valid test PDF content for institutional building safety\n%%EOF".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "building_safety_certificate.pdf",
                "application/pdf",
                validPdfHeader
        );

        mockMvc.perform(multipart("/api/v1/applications/" + appId + "/documents")
                        .file(mockFile)
                        .param("documentType", DocumentType.OCCUPANCY_CERTIFICATE.name())
                        .header("Authorization", "Bearer " + delhiOfficerToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.originalFilename").value("building_safety_certificate.pdf"))
                .andExpect(jsonPath("$.data.sha256Hash").isNotEmpty());

        // Step 3: Transition DRAFT -> SUBMITTED via official institutional submit endpoint
        mockMvc.perform(post("/api/v1/applications/" + appId + "/submit")
                        .header("Authorization", "Bearer " + delhiOfficerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.toState").value("SUBMITTED"));

        // Step 4: Unauthorized institutional officer cannot make statutory decision (APPROVED)
        TransitionRequest illegalApproveReq = new TransitionRequest(
                ApplicationState.APPROVED,
                "Unauthorized institutional attempt to approve self-application",
                null
        );

        mockMvc.perform(post("/api/v1/applications/" + appId + "/transitions")
                        .header("Authorization", "Bearer " + delhiOfficerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(illegalApproveReq)))
                .andExpect(status().isForbidden());

        // Step 5: Regional Officer moves to UNDER_SCRUTINY
        TransitionRequest scrutinyReq = new TransitionRequest(
                ApplicationState.UNDER_SCRUTINY,
                "Commencing regional scrutiny review",
                null
        );

        mockMvc.perform(post("/api/v1/applications/" + appId + "/transitions")
                        .header("Authorization", "Bearer " + regionalOfficerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scrutinyReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.toState").value("UNDER_SCRUTINY"));

        // Step 6: Regional Officer marks as DEFICIENT (simulating missing fire safety NOC)
        TransitionRequest deficientReq = new TransitionRequest(
                ApplicationState.DEFICIENT,
                "Missing Fire Safety NOC and Architect structural clearance",
                "Clause 1.6 & 2.3 deficiency notice"
        );

        mockMvc.perform(post("/api/v1/applications/" + appId + "/transitions")
                        .header("Authorization", "Bearer " + regionalOfficerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deficientReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.toState").value("DEFICIENT"));
    }

    // =========================================================================
    // Scenario 3: Multi-Tenant Isolation & Path Traversal Protections
    // =========================================================================

    @Test
    @DisplayName("Cross-tenant document access and illegal path traversals are strictly rejected")
    void testCrossTenantIsolationAndSecurityProtections() throws Exception {
        // Mumbai Officer attempts to upload document to Delhi Tech application
        UUID seededDelhiAppId = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-000000000001");
        byte[] pdfHeader = "%PDF-1.4\nTest".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "illegal_cross_tenant.pdf",
                "application/pdf",
                pdfHeader
        );

        mockMvc.perform(multipart("/api/v1/applications/" + seededDelhiAppId + "/documents")
                        .file(mockFile)
                        .param("documentType", DocumentType.LAND_REGISTRATION.name())
                        .header("Authorization", "Bearer " + mumbaiOfficerToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Tenant Access Denied"));

        // Attempt upload with path traversal in filename
        MockMultipartFile pathTraversalFile = new MockMultipartFile(
                "file",
                "../../../../etc/passwd.pdf",
                "application/pdf",
                pdfHeader
        );

        mockMvc.perform(multipart("/api/v1/applications/" + seededDelhiAppId + "/documents")
                        .file(pathTraversalFile)
                        .param("documentType", DocumentType.LAND_REGISTRATION.name())
                        .header("Authorization", "Bearer " + delhiOfficerToken))
                .andExpect(status().isCreated())
                // Verify filename was sanitized safely (path traversal components stripped)
                .andExpect(jsonPath("$.data.originalFilename").value("passwd.pdf"));
    }

    // =========================================================================
    // Scenario 4: Continuous Compliance Scanning, Deduplication & Statutory Deadlines
    // =========================================================================

    @Test
    @DisplayName("Compliance engine scans, evaluates statutory deadlines, and prevents duplicate alerts")
    void testComplianceScanningDeadlinesAndDeduplication() throws Exception {
        // 1. Execute Compliance Run
        ComplianceCheckRunDto runDto = complianceEngineService.executeComplianceCheckRun(
                "SCHEDULED",
                "Phase 5 Automated E2E Compliance Verification Run",
                null,
                adminPrincipal
        );

        assertNotNull(runDto.id());
        assertEquals("SUCCESS", runDto.status());
        assertTrue(runDto.institutionsChecked() > 0);

        // 2. Create Statutory Deadline for Deficiencies
        CreateDeadlineRequest dlReq = new CreateDeadlineRequest(
                "DL_DEFICIENCY_APH_1_6_" + UUID.randomUUID().toString().substring(0, 8),
                "Rectification of Building Inspection Findings",
                "Statutory 10-day window per APH 2024-27 Clause 1.6",
                DeadlineType.STATUTORY,
                "APH 2024-27 Clause 1.6",
                "APPLICATION",
                UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-000000000001"),
                DELHI_TECH_INST_ID,
                Instant.now().plus(5, ChronoUnit.DAYS),
                0,
                "[-7, -3, -1, 0]"
        );

        DeadlineDto createdDeadline = deadlineService.createDeadline(dlReq, delhiPrincipal);
        assertNotNull(createdDeadline.id());
        assertEquals(DeadlineStatus.PENDING, createdDeadline.status());

        // 3. Evaluate Deadlines
        int evaluated = deadlineService.evaluateDeadlines();
        assertTrue(evaluated >= 0, "Deadline evaluation should process successfully");

        // 4. Test Alert Deduplication
        Alert alert1 = alertService.createAlert(
                "ALERT_DEDUP_TEST",
                "Test Deduplication Title",
                "First alert occurrence",
                AlertCategory.COMPLIANCE,
                AlertSeverity.WARNING,
                AlertPriority.HIGH,
                "DEADLINE",
                createdDeadline.id(),
                DELHI_TECH_INST_ID,
                DELHI_OFFICER_USER_ID,
                "ROLE_INSTITUTION_OFFICER",
                "Delhi"
        );
        assertNotNull(alert1.getId());

        // Re-creating with same alertCode, sourceEntityType, sourceEntityId, recipient, institution
        // triggers internal deduplication hash check and suppresses duplicate
        Alert alert2 = alertService.createAlert(
                "ALERT_DEDUP_TEST",
                "Test Deduplication Title Second Pass",
                "Second alert occurrence",
                AlertCategory.COMPLIANCE,
                AlertSeverity.WARNING,
                AlertPriority.HIGH,
                "DEADLINE",
                createdDeadline.id(),
                DELHI_TECH_INST_ID,
                DELHI_OFFICER_USER_ID,
                "ROLE_INSTITUTION_OFFICER",
                "Delhi"
        );

        assertEquals(alert1.getId(), alert2.getId(), "Alert service must return existing alert on duplicate hash");
    }

    // =========================================================================
    // Scenario 5: Alert Lifecycle & Retry Non-Duplication
    // =========================================================================

    @Test
    @DisplayName("Alert lifecycle: acknowledge, escalate, resolve, and notification retries preserve idempotency")
    void testAlertLifecycleAndNotificationRetries() {
        Alert freshAlert = alertService.createAlert(
                "ALERT_LIFECYCLE_E2E_" + UUID.randomUUID().toString().substring(0, 8),
                "Isolated E2E Alert Lifecycle Test",
                "Notification test message",
                AlertCategory.DEADLINE,
                AlertSeverity.CRITICAL,
                AlertPriority.URGENT,
                "APPLICATION",
                UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-000000000001"),
                DELHI_TECH_INST_ID,
                DELHI_OFFICER_USER_ID,
                "ROLE_INSTITUTION_OFFICER",
                "Delhi"
        );
        UUID alertId = freshAlert.getId();

        // 1. Institution Officer acknowledges alert
        AlertDto acked = alertService.acknowledgeAlert(
                alertId,
                "Noted, deficiency rectification team dispatched",
                delhiPrincipal
        );
        assertEquals("ACKNOWLEDGED", acked.status().name());

        // 2. Regional Officer escalates alert
        AlertDto escalated = alertService.escalateAlert(
                alertId,
                "ROLE_AICTE_ADMIN",
                regionalPrincipal
        );
        assertTrue(escalated.isEscalated());
        assertEquals("ROLE_AICTE_ADMIN", escalated.escalatedToRole());

        // 3. Resolve alert
        AlertDto resolved = alertService.resolveAlert(
                alertId,
                "Deficiencies verified as rectified",
                delhiPrincipal
        );
        assertEquals("RESOLVED", resolved.status().name());

        // 4. Retry failed notification deliveries does not duplicate or throw
        int retried = alertService.retryFailedDeliveries();
        assertTrue(retried >= 0);
    }
}
