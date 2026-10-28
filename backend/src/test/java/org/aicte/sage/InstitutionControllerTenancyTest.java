package org.aicte.sage;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aicte.sage.modules.auth.dto.LoginRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class InstitutionControllerTenancyTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String DELHI_INST_ID = "22222222-2222-2222-2222-222222222222";
    private static final String MUMBAI_INST_ID = "33333333-3333-3333-3333-333333333333";

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
    @DisplayName("Institution Officer CAN access their own institution record")
    void testTenantCanAccessOwnInstitution() throws Exception {
        String delhiOfficerToken = loginAndGetToken("officer@delhi-tech.edu", "Password123!");

        mockMvc.perform(get("/api/v1/institutions/" + DELHI_INST_ID)
                        .header("Authorization", "Bearer " + delhiOfficerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(DELHI_INST_ID))
                .andExpect(jsonPath("$.data.code").value("1-500123498"));
    }

    @Test
    @DisplayName("CRITICAL SECURITY: Institution Officer CANNOT access another institution's record (returns 403 Forbidden)")
    void testTenantCannotAccessAnotherInstitution() throws Exception {
        // Delhi Tech officer attempts to inspect Mumbai Academy's confidential record
        String delhiOfficerToken = loginAndGetToken("officer@delhi-tech.edu", "Password123!");

        mockMvc.perform(get("/api/v1/institutions/" + MUMBAI_INST_ID)
                        .header("Authorization", "Bearer " + delhiOfficerToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.title").value("Tenant Access Denied"));
    }

    @Test
    @DisplayName("AICTE Central Administrator CAN access any institution")
    void testAdminCanAccessAnyInstitution() throws Exception {
        String adminToken = loginAndGetToken("admin@aicte.gov.in", "Password123!");

        mockMvc.perform(get("/api/v1/institutions/" + MUMBAI_INST_ID)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(MUMBAI_INST_ID));
    }

    @Test
    @DisplayName("Unauthenticated request to institutions is rejected with 401")
    void testUnauthenticatedAccessRejected() throws Exception {
        mockMvc.perform(get("/api/v1/institutions"))
                .andExpect(status().isUnauthorized());
    }
}
