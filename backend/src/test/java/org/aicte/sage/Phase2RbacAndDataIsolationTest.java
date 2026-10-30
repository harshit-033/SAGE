package org.aicte.sage;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aicte.sage.common.security.UserPrincipal;
import org.aicte.sage.modules.applications.service.ApplicationService;
import org.aicte.sage.modules.auth.dto.LoginRequest;
import org.aicte.sage.modules.documents.service.DocumentComplianceService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class Phase2RbacAndDataIsolationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private DocumentComplianceService documentComplianceService;

    private static final String DELHI_INST_ID = "22222222-2222-2222-2222-222222222222";
    private static final String MUMBAI_INST_ID = "33333333-3333-3333-3333-333333333333";
    private static final String DELHI_APP_ID = "bbbbbbbb-0001-0000-0000-000000000101";

    private String loginAndGetToken(String username, String password) throws Exception {
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
    @DisplayName("6.1 Fail-Closed: Institution Officer with empty authorized institutions gets 0 applications")
    void testInstitutionOfficerWithNoAssignedInstitutionsReturnsEmpty() {
        UserPrincipal unassignedOfficer = new UserPrincipal(
                UUID.randomUUID(),
                "unassigned@officer.edu",
                "unassigned@officer.edu",
                "hash",
                "Unassigned Officer",
                true,
                List.of(new SimpleGrantedAuthority("ROLE_INSTITUTION_OFFICER")),
                Set.of() // Empty institutions
        );

        var response = applicationService.listApplications(null, null, null, 0, 10, unassignedOfficer);
        assertNotNull(response);
        assertEquals(0, response.getTotalElements(), "Unassigned officer must get 0 applications (fail-closed)");
        assertTrue(response.getContent().isEmpty());
    }

    @Test
    @DisplayName("6.1 Fail-Closed: Institution Officer with empty authorized institutions gets 0 documents")
    void testInstitutionOfficerWithNoAssignedInstitutionsReturnsEmptyDocuments() {
        UserPrincipal unassignedOfficer = new UserPrincipal(
                UUID.randomUUID(),
                "unassigned@officer.edu",
                "unassigned@officer.edu",
                "hash",
                "Unassigned Officer",
                true,
                List.of(new SimpleGrantedAuthority("ROLE_INSTITUTION_OFFICER")),
                Set.of() // Empty institutions
        );

        var response = documentComplianceService.listDocuments(null, null, null, null, null, null, 0, 10, unassignedOfficer);
        assertNotNull(response);
        assertEquals(0, response.getTotalElements(), "Unassigned officer must get 0 documents (fail-closed)");
        assertTrue(response.getContent().isEmpty());
    }

    @Test
    @DisplayName("6.1 Isolation: Institution Officer listing applications sees only own institution")
    void testInstitutionOfficerSeesOnlyOwnApplications() throws Exception {
        String token = loginAndGetToken("officer@delhi-tech.edu", "Password123!");

        MvcResult result = mockMvc.perform(get("/api/v1/applications")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        var root = objectMapper.readTree(json);
        var content = root.at("/data/content");

        assertTrue(content.isArray());
        for (var appNode : content) {
            assertEquals(DELHI_INST_ID, appNode.at("/institutionId").asText(),
                    "Institution officer must only receive applications from their authorized institution");
        }
    }

    @Test
    @DisplayName("6.1 Cross-Institution Access Denied: Institution Officer cannot request another institutionId query param")
    void testInstitutionOfficerCannotQueryForeignInstitutionApplications() throws Exception {
        String token = loginAndGetToken("officer@delhi-tech.edu", "Password123!");

        mockMvc.perform(get("/api/v1/applications?institutionId=" + MUMBAI_INST_ID)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.type").value("https://api.aicte.gov.in/errors/tenant-access-denied"));
    }

    @Test
    @DisplayName("6.1 Cross-Institution Detail Access Denied: Officer cannot view foreign application detail")
    void testInstitutionOfficerCannotAccessForeignApplication() throws Exception {
        String mumbaiOfficerToken = loginAndGetToken("officer@mumbai-engg.edu", "Password123!");

        mockMvc.perform(get("/api/v1/applications/" + DELHI_APP_ID)
                        .header("Authorization", "Bearer " + mumbaiOfficerToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.type").value("https://api.aicte.gov.in/errors/tenant-access-denied"));
    }

    @Test
    @DisplayName("6.2 Regional Jurisdiction: Northern Regional Officer only sees applications in Northern region")
    void testRegionalOfficerSeesOnlyJurisdictionApplications() throws Exception {
        String northRegionalToken = loginAndGetToken("regional.north@aicte-demo.gov.in", "Password123!");

        MvcResult result = mockMvc.perform(get("/api/v1/applications")
                        .header("Authorization", "Bearer " + northRegionalToken))
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        var root = objectMapper.readTree(json);
        var content = root.at("/data/content");

        assertTrue(content.isArray());
        for (var appNode : content) {
            String instState = appNode.at("/institutionState").asText("");
            if (!instState.isBlank()) {
                assertTrue(instState.equalsIgnoreCase("Delhi") ||
                           instState.equalsIgnoreCase("Uttar Pradesh") ||
                           instState.equalsIgnoreCase("Uttarakhand") ||
                           instState.equalsIgnoreCase("Haryana"),
                        "Northern Regional officer must not see application from state: " + instState);
            }
        }
    }

    @Test
    @DisplayName("6.2 Regional Jurisdiction Bypass Rejected: Regional Officer cannot query unauthorized regional institution")
    void testRegionalOfficerCannotBypassJurisdictionParam() throws Exception {
        String northRegionalToken = loginAndGetToken("regional.north@aicte-demo.gov.in", "Password123!");

        // Western region institution in Mumbai
        mockMvc.perform(get("/api/v1/applications?institutionId=" + MUMBAI_INST_ID)
                        .header("Authorization", "Bearer " + northRegionalToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("6.3 EVC Coordinator Access Consistency: EVC Coordinator can list and read inspections")
    void testEvcCoordinatorCanAccessInspectionsConsistently() throws Exception {
        String evcToken = loginAndGetToken("evc.coord1@aicte-demo.gov.in", "Password123!");

        mockMvc.perform(get("/api/v1/evc/inspections/pending")
                        .header("Authorization", "Bearer " + evcToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/evc/inspections/scheduled")
                        .header("Authorization", "Bearer " + evcToken))
                .andExpect(status().isOk());
    }
}
