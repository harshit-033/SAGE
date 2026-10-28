package org.aicte.sage;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aicte.sage.modules.auth.dto.LoginRequest;
import org.aicte.sage.modules.institutions.dto.AddInstitutionUserRequest;
import org.aicte.sage.modules.institutions.dto.InstitutionStatusRequest;
import org.aicte.sage.modules.institutions.dto.UpdateInstitutionRequest;
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

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class InstitutionManagementTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;
    private String delhiOfficerToken;

    private static final String DELHI_TECH_ID = "22222222-2222-2222-2222-222222222222";
    private static final String MUMBAI_PHARMA_ID = "33333333-3333-3333-3333-333333333333";

    @BeforeEach
    void setUp() throws Exception {
        adminToken = obtainToken("admin@aicte.gov.in", "Password123!");
        delhiOfficerToken = obtainToken("officer@delhi-tech.edu", "Password123!");
    }

    private String obtainToken(String username, String password) throws Exception {
        LoginRequest req = new LoginRequest(username, password);
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        return objectMapper.readTree(body).at("/data/accessToken").asText();
    }

    @Test
    @DisplayName("Admin can retrieve paginated institutions with category filter")
    void testPaginatedInstitutionsWithFilter() throws Exception {
        mockMvc.perform(get("/api/v1/institutions")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("page", "0")
                        .param("size", "10")
                        .param("category", "ENGINEERING_AND_TECH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].category").value("ENGINEERING_AND_TECH"))
                .andExpect(jsonPath("$.data.totalElements").value(greaterThanOrEqualTo(1)));
    }

    @Test
    @DisplayName("Affiliated officer can update their institution profile")
    void testAffiliatedOfficerCanUpdateOwnInstitution() throws Exception {
        UpdateInstitutionRequest updateReq = new UpdateInstitutionRequest();
        updateReq.setName("Delhi Institute of Advanced Technology (Updated)");
        updateReq.setCategory("ENGINEERING_AND_TECH");
        updateReq.setAddressLine1("Plot 14, Institutional Area, Phase II");
        updateReq.setCity("New Delhi");
        updateReq.setState("Delhi");
        updateReq.setDistrict("North West Delhi");
        updateReq.setPincode("110085");
        updateReq.setOfficialEmail("director@delhi-tech.edu");
        updateReq.setOfficialPhone("+91-11-27891234");
        updateReq.setEstablishmentYear(1998);

        mockMvc.perform(put("/api/v1/institutions/" + DELHI_TECH_ID)
                        .header("Authorization", "Bearer " + delhiOfficerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Delhi Institute of Advanced Technology (Updated)"))
                .andExpect(jsonPath("$.data.addressLine1").value("Plot 14, Institutional Area, Phase II"));
    }

    @Test
    @DisplayName("Cross-tenant institution update attempt is rejected with 403 Forbidden")
    void testCrossTenantUpdateForbidden() throws Exception {
        UpdateInstitutionRequest updateReq = new UpdateInstitutionRequest();
        updateReq.setName("Malicious Takeover Attempt");
        updateReq.setState("Maharashtra");
        updateReq.setDistrict("Mumbai Suburban");

        mockMvc.perform(put("/api/v1/institutions/" + MUMBAI_PHARMA_ID)
                        .header("Authorization", "Bearer " + delhiOfficerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("Admin can toggle institution active status but institution officer cannot")
    void testInstitutionStatusToggleAuthorization() throws Exception {
        InstitutionStatusRequest statusReq = new InstitutionStatusRequest(false, "Statutory compliance audit pending");

        // Officer cannot change status
        mockMvc.perform(patch("/api/v1/institutions/" + DELHI_TECH_ID + "/status")
                        .header("Authorization", "Bearer " + delhiOfficerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusReq)))
                .andExpect(status().isForbidden());

        // Admin can change status
        mockMvc.perform(patch("/api/v1/institutions/" + DELHI_TECH_ID + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.active").value(false));

        // Restore active status
        statusReq.setActive(true);
        mockMvc.perform(patch("/api/v1/institutions/" + DELHI_TECH_ID + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.active").value(true));
    }

    @Test
    @DisplayName("Admin can view and affiliate user with institution")
    void testInstitutionUserAffiliation() throws Exception {
        // List users for Delhi Tech
        mockMvc.perform(get("/api/v1/institutions/" + DELHI_TECH_ID + "/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].roleInInstitution").value("HEAD_OF_INSTITUTION"));

        // Affiliate regional officer as external observer
        AddInstitutionUserRequest addReq = new AddInstitutionUserRequest(
                UUID.fromString("11111111-1111-1111-1111-111111111112"), // regional officer ID
                "REGIONAL_OBSERVER",
                false
        );

        mockMvc.perform(post("/api/v1/institutions/" + DELHI_TECH_ID + "/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.roleInInstitution").value("REGIONAL_OBSERVER"));

        // Clean up / disaffiliate
        mockMvc.perform(delete("/api/v1/institutions/" + DELHI_TECH_ID + "/users/11111111-1111-1111-1111-111111111112")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }
}
