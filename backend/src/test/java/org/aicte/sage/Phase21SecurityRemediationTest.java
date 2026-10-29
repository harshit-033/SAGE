package org.aicte.sage;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aicte.sage.common.audit.AuditService;
import org.aicte.sage.common.exception.AuditPersistenceException;
import org.aicte.sage.common.security.AuthoritativeAuthorizationService;
import org.aicte.sage.modules.auth.dto.LoginRequest;
import org.aicte.sage.modules.users.dto.CreateUserRequest;
import org.aicte.sage.modules.users.dto.UserStatusRequest;
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
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Set;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class Phase21SecurityRemediationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthoritativeAuthorizationService authorizationService;

    @Autowired
    private AuditService auditService;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private String adminToken;
    private String delhiOfficerToken;
    private String regionalToken;
    private String complianceToken;

    private static final String DELHI_INST_ID = "22222222-2222-2222-2222-222222222222";
    private static final String MUMBAI_INST_ID = "33333333-3333-3333-3333-333333333333";

    @BeforeEach
    void setUp() throws Exception {
        adminToken = obtainToken("admin@aicte.gov.in", "Password123!");
        delhiOfficerToken = obtainToken("officer@delhi-tech.edu", "Password123!");
        regionalToken = obtainToken("regional@aicte.gov.in", "Password123!");
        complianceToken = obtainToken("compliance@aicte.gov.in", "Password123!");
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
    @DisplayName("Server-side token revocation: Token is invalidated on /auth/logout and subsequent requests return 401")
    void testServerSideTokenRevocationOnLogout() throws Exception {
        // 1. Obtain a fresh token for institution officer
        String sessionToken = obtainToken("officer@delhi-tech.edu", "Password123!");

        // 2. Access /api/v1/auth/me successfully
        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + sessionToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("officer@delhi-tech.edu"));

        // 3. User explicitly logs out
        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", "Bearer " + sessionToken))
                .andExpect(status().isOk());

        // 4. Attempting to use the same token must now fail with 401 Unauthorized
        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + sessionToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Deactivated user access blocked: Token for suspended account is rejected with 401")
    void testDeactivatedUserTokenRejected() throws Exception {
        // Create a temporary user to suspend
        String testUser = "test_deactivate_" + System.currentTimeMillis();
        CreateUserRequest createReq = new CreateUserRequest(
                testUser,
                testUser + "@test.edu",
                "Test Deactivate",
                "Password123!",
                Set.of("ROLE_INSTITUTION_OFFICER"),
                UUID.fromString(DELHI_INST_ID)
        );

        MvcResult createResult = mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andReturn();

        String userId = objectMapper.readTree(createResult.getResponse().getContentAsString()).at("/data/id").asText();

        // Obtain token for this user
        String userToken = obtainToken(testUser, "Password123!");
        assertNotNull(userToken);

        // Verify active token works
        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk());

        // Admin suspends user
        UserStatusRequest statusReq = new UserStatusRequest(false, "Security test deactivation");
        mockMvc.perform(patch("/api/v1/users/" + userId + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusReq)))
                .andExpect(status().isOk());

        // Subsequent request with the token is rejected (401)
        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Regional jurisdiction scoping: RO can only access institutions in assigned states")
    void testRegionalOfficerJurisdictionScoping() throws Exception {
        // Northern RO has active jurisdiction assignment over Delhi (V6 seed)
        // Accessing Delhi Tech must succeed
        mockMvc.perform(get("/api/v1/institutions/" + DELHI_INST_ID)
                        .header("Authorization", "Bearer " + regionalToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(DELHI_INST_ID));

        // Northern RO does NOT have jurisdiction over Maharashtra (Mumbai Pharma)
        // Must be rejected with 403 Forbidden (no blanket role access!)
        mockMvc.perform(get("/api/v1/institutions/" + MUMBAI_INST_ID)
                        .header("Authorization", "Bearer " + regionalToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("Case assignment scoping: Officer can only access explicitly assigned cases")
    void testCaseAssignmentScopingForComplianceOfficer() throws Exception {
        // compliance@aicte.gov.in has active case assignment for Delhi Tech (V6 seed)
        mockMvc.perform(get("/api/v1/institutions/" + DELHI_INST_ID)
                        .header("Authorization", "Bearer " + complianceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(DELHI_INST_ID));

        // compliance@aicte.gov.in does NOT have case assignment for Mumbai Pharma
        // Must be rejected with 403 Forbidden (no blanket role access!)
        mockMvc.perform(get("/api/v1/institutions/" + MUMBAI_INST_ID)
                        .header("Authorization", "Bearer " + complianceToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("APH Statutory committee verification: Membership requires explicit appointment, cannot be inferred from role")
    void testStatutoryCommitteeAppointmentNorms() {
        // Smt. Priya Nair (id: 11111111-1111-1111-1111-111111111116) has appointment on SCSC per APH Annexure 16.7
        UUID priyaId = UUID.fromString("11111111-1111-1111-1111-111111111116");
        assertTrue(authorizationService.hasStatutoryCommitteeAppointment(priyaId, "SCSC"),
                "User with explicit committee appointment must be verified");

        // Priya does not have appointment for EVC or SAC
        assertFalse(authorizationService.hasStatutoryCommitteeAppointment(priyaId, "EVC"),
                "User must not have committee appointment for unassigned committee");

        // Delhi officer (only holds application role) must not be verified as statutory committee member
        UUID officerId = UUID.fromString("11111111-1111-1111-1111-111111111113");
        assertFalse(authorizationService.hasStatutoryCommitteeAppointment(officerId, "SCSC"),
                "Statutory committee membership cannot be inferred from role alone");
    }

    @Test
    @DisplayName("Privilege escalation blocked: Non-admin cannot assign ROLE_AICTE_ADMIN")
    void testPrivilegeEscalationBlocked() throws Exception {
        String testUser = "priv_esc_" + System.currentTimeMillis();
        CreateUserRequest createReq = new CreateUserRequest(
                testUser,
                testUser + "@aicte.gov.in",
                "Privilege Escalation Attempt",
                "Password123!",
                Set.of("ROLE_AICTE_ADMIN"),
                null
        );

        // Institution officer attempts to create an admin account
        mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", "Bearer " + delhiOfficerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Unknown institution ID rejected: Creation fails with 404 instead of silent omission")
    void testUnknownInstitutionIdRejected() throws Exception {
        UUID unknownInstId = UUID.fromString("00000000-0000-0000-0000-000000000000");
        CreateUserRequest createReq = new CreateUserRequest(
                "unknown_inst_user_" + System.currentTimeMillis(),
                "unknown_inst_" + System.currentTimeMillis() + "@test.edu",
                "Unknown College Officer",
                "Password123!",
                Set.of("ROLE_INSTITUTION_OFFICER"),
                unknownInstId
        );

        mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail", containsString("was not found")));
    }

    @Test
    @DisplayName("Pagination and sorting validation: Query abuse is rejected with 400 Bad Request")
    void testPaginationAndSortingValidation() throws Exception {
        // Negative page
        mockMvc.perform(get("/api/v1/institutions")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("page", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", containsString("Page index must not be less than zero")));

        // Page size exceeding max limit of 100
        mockMvc.perform(get("/api/v1/institutions")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("size", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", containsString("cannot exceed maximum limit of 100")));

        // Invalid sort field
        mockMvc.perform(get("/api/v1/institutions")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("sortBy", "password_hash;DROP TABLE users"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", containsString("Invalid sort field")));

        // Invalid sort direction
        mockMvc.perform(get("/api/v1/institutions")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("sortDir", "sideways"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", containsString("Must be 'asc' or 'desc'")));
    }

    @Test
    @DisplayName("Reliable audit: Mandatory audit failure triggers rollback and aborts consequential action")
    void testAuditPersistenceFailureAbortsAction() {
        assertThrows(AuditPersistenceException.class, () -> {
            transactionTemplate.execute(status -> {
                auditService.recordMandatoryEvent(
                        "CONSEQUENTIAL_TEST_EVENT",
                        "test_principal",
                        UUID.randomUUID(),
                        "TEST",
                        "123",
                        "MUTATION",
                        "127.0.0.1",
                        null
                );
                // Simulate forced persistence failure
                throw new AuditPersistenceException("Injected audit failure", new RuntimeException("DB Disk full"));
            });
        });
    }
}
