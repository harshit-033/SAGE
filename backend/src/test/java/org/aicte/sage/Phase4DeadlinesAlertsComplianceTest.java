package org.aicte.sage;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aicte.sage.modules.auth.dto.LoginRequest;
import org.aicte.sage.modules.compliance.dto.ResolveComplianceFindingRequest;
import org.aicte.sage.modules.deadlines.dto.CreateDeadlineRequest;
import org.aicte.sage.modules.deadlines.dto.RecalculateDeadlineRequest;
import org.aicte.sage.modules.deadlines.dto.ResolveDeadlineRequest;
import org.aicte.sage.modules.deadlines.entity.DeadlineType;
import org.aicte.sage.modules.notifications.dto.AcknowledgeAlertRequest;
import org.aicte.sage.modules.notifications.dto.ResolveAlertRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class Phase4DeadlinesAlertsComplianceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;
    private String delhiOfficerToken;
    private String mumbaiOfficerToken;
    private String regionalToken;

    private static final String DELHI_INST_ID = "22222222-2222-2222-2222-222222222222";
    private static final String MUMBAI_INST_ID = "33333333-3333-3333-3333-333333333333";
    private static final String DELHI_DEADLINE_ID = "dddddddd-dddd-dddd-dddd-000000000001";
    private static final String DELHI_ALERT_ID = "ffffffff-ffff-ffff-ffff-000000000001";
    private static final String SEEDED_FINDING_ID = "30303030-3030-3030-3030-000000000001";

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
    @DisplayName("Stage C: Create, recalculate and resolve statutory deadline with full audit history")
    void testDeadlineLifecycleAndCalculations() throws Exception {
        Instant initialTarget = Instant.now().plus(10, ChronoUnit.DAYS);
        CreateDeadlineRequest createReq = new CreateDeadlineRequest(
                "DL_SHC_REPRESENTATION_TEST",
                "Standing Hearing Committee Representation Submission",
                "Mandatory 10-calendar day statutory window for applicant representation per APH 1.8.1.",
                DeadlineType.STATUTORY,
                "APH 2024-27 Clause 1.8.1",
                "APPLICATION",
                UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-000000000001"),
                UUID.fromString(DELHI_INST_ID),
                initialTarget,
                0,
                "[-7, -3, -1, 0]"
        );

        MvcResult createResult = mockMvc.perform(post("/api/v1/deadlines")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.deadlineCode").value("DL_SHC_REPRESENTATION_TEST"))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.statutoryClause").value("APH 2024-27 Clause 1.8.1"))
                .andReturn();

        String deadlineId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .at("/data/id").asText();

        // Recalculate target date (with statutory justification)
        Instant extendedTarget = initialTarget.plus(5, ChronoUnit.DAYS);
        RecalculateDeadlineRequest recalcReq = new RecalculateDeadlineRequest(
                extendedTarget,
                "Standing Hearing Committee sitting adjourned by regional committee"
        );

        mockMvc.perform(post("/api/v1/deadlines/" + deadlineId + "/recalculate")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recalcReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PENDING"));

        // Verify history recorded the previous and new target dates
        mockMvc.perform(get("/api/v1/deadlines/" + deadlineId + "/history")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data[0].reason").value("Standing Hearing Committee sitting adjourned by regional committee"));

        // Resolve deadline
        ResolveDeadlineRequest resolveReq = new ResolveDeadlineRequest(
                "Physical hearing completed and written representation received by hearing officer"
        );

        mockMvc.perform(post("/api/v1/deadlines/" + deadlineId + "/resolve")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resolveReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("RESOLVED"))
                .andExpect(jsonPath("$.data.resolvedAt").isNotEmpty())
                .andExpect(jsonPath("$.data.resolutionNotes").value("Physical hearing completed and written representation received by hearing officer"));
    }

    @Test
    @DisplayName("Stage D: Retrieve alerts, acknowledge, resolve and trigger supervisory escalation")
    void testAlertLifecycleAndEscalation() throws Exception {
        // Retrieve seeded Delhi Tech alert
        mockMvc.perform(get("/api/v1/alerts/" + DELHI_ALERT_ID)
                        .header("Authorization", "Bearer " + delhiOfficerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.alertCode").value("ALERT_SCRUTINY_DEFICIENCY"))
                .andExpect(jsonPath("$.data.severity").value("CRITICAL"))
                .andExpect(jsonPath("$.data.status").value("UNREAD"));

        // Acknowledge alert
        AcknowledgeAlertRequest ackReq = new AcknowledgeAlertRequest(
                "Deficiency notification reviewed by Principal. Action initiated with civil engineer."
        );

        mockMvc.perform(post("/api/v1/alerts/" + DELHI_ALERT_ID + "/acknowledge")
                        .header("Authorization", "Bearer " + delhiOfficerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ackReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACKNOWLEDGED"))
                .andExpect(jsonPath("$.data.acknowledgedAt").isNotEmpty())
                .andExpect(jsonPath("$.data.acknowledgementNotes").value("Deficiency notification reviewed by Principal. Action initiated with civil engineer."));

        // Supervisory escalation by Regional Officer
        mockMvc.perform(post("/api/v1/alerts/" + DELHI_ALERT_ID + "/escalate")
                        .header("Authorization", "Bearer " + regionalToken)
                        .param("escalateToRole", "ROLE_REGIONAL_OFFICER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isEscalated").value(true));

        // Resolve alert
        ResolveAlertRequest resolveReq = new ResolveAlertRequest(
                "Rectified certificate verified and approved in portal."
        );

        mockMvc.perform(post("/api/v1/alerts/" + DELHI_ALERT_ID + "/resolve")
                        .header("Authorization", "Bearer " + delhiOfficerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resolveReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("RESOLVED"))
                .andExpect(jsonPath("$.data.resolvedAt").isNotEmpty());
    }

    @Test
    @DisplayName("Stage E: Execute compliance scan and verify findings & resolution")
    void testComplianceScanAndFindingsResolution() throws Exception {
        // Execute manual compliance scan for Delhi Tech
        MvcResult scanResult = mockMvc.perform(post("/api/v1/compliance/scan")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("institutionId", DELHI_INST_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.institutionsChecked").value(1))
                .andReturn();

        // Retrieve findings for Delhi Tech
        mockMvc.perform(get("/api/v1/compliance/findings")
                        .header("Authorization", "Bearer " + delhiOfficerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data.content[0].institutionId").value(DELHI_INST_ID));

        // Resolve seeded compliance finding
        ResolveComplianceFindingRequest resolveFindingReq = new ResolveComplianceFindingRequest(
                "Rectified fire safety renewal uploaded to statutory portal under review."
        );

        mockMvc.perform(post("/api/v1/compliance/findings/" + SEEDED_FINDING_ID + "/resolve")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resolveFindingReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("RESOLVED"))
                .andExpect(jsonPath("$.data.resolutionNotes").value("Rectified fire safety renewal uploaded to statutory portal under review."));
    }

    @Test
    @DisplayName("Security & Tenancy: Mumbai officer cannot access or modify Delhi Tech deadlines or alerts")
    void testCrossTenantIsolationOnDeadlinesAndAlerts() throws Exception {
        // Mumbai officer attempting to view Delhi Tech's deadline -> 403
        mockMvc.perform(get("/api/v1/deadlines/" + DELHI_DEADLINE_ID)
                        .header("Authorization", "Bearer " + mumbaiOfficerToken))
                .andExpect(status().isForbidden());

        // Mumbai officer attempting to recalculate Delhi Tech's deadline -> 403
        RecalculateDeadlineRequest recalcReq = new RecalculateDeadlineRequest(
                Instant.now().plus(20, ChronoUnit.DAYS),
                "Unauthorized attempt"
        );
        mockMvc.perform(post("/api/v1/deadlines/" + DELHI_DEADLINE_ID + "/recalculate")
                        .header("Authorization", "Bearer " + mumbaiOfficerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recalcReq)))
                .andExpect(status().isForbidden());

        // Mumbai officer attempting to view Delhi Tech's alert -> 403
        mockMvc.perform(get("/api/v1/alerts/" + DELHI_ALERT_ID)
                        .header("Authorization", "Bearer " + mumbaiOfficerToken))
                .andExpect(status().isForbidden());

        // Mumbai officer attempting to acknowledge Delhi Tech's alert -> 403
        AcknowledgeAlertRequest ackReq = new AcknowledgeAlertRequest("Unauthorized ack");
        mockMvc.perform(post("/api/v1/alerts/" + DELHI_ALERT_ID + "/acknowledge")
                        .header("Authorization", "Bearer " + mumbaiOfficerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ackReq)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Stage G: Admin Job endpoints are secured and callable only by AICTE Admin")
    void testAdminJobEndpointsSecurity() throws Exception {
        // Officer trying to evaluate deadlines -> 403 Forbidden
        mockMvc.perform(post("/api/v1/admin/jobs/deadlines/evaluate")
                        .header("Authorization", "Bearer " + delhiOfficerToken))
                .andExpect(status().isForbidden());

        // Officer trying to retry notifications -> 403 Forbidden
        mockMvc.perform(post("/api/v1/admin/jobs/notifications/retry")
                        .header("Authorization", "Bearer " + delhiOfficerToken))
                .andExpect(status().isForbidden());

        // Admin triggering deadline evaluation -> 200 OK
        mockMvc.perform(post("/api/v1/admin/jobs/deadlines/evaluate")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SUCCESS"));

        // Admin triggering notification delivery retries -> 200 OK
        mockMvc.perform(post("/api/v1/admin/jobs/notifications/retry")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SUCCESS"));

        // Admin triggering compliance job -> 200 OK
        mockMvc.perform(post("/api/v1/admin/jobs/compliance/run")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SUCCESS"));
    }
}
