package org.aicte.sage;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aicte.sage.modules.auth.dto.LoginRequest;
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

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuditEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;
    private String auditorToken;
    private String officerToken;

    @BeforeEach
    void setUp() throws Exception {
        adminToken = obtainToken("admin@aicte.gov.in", "Password123!");
        auditorToken = obtainToken("auditor@aicte.gov.in", "Password123!");
        officerToken = obtainToken("officer@delhi-tech.edu", "Password123!");
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
    @DisplayName("Admin and Auditor can access audit events log with pagination")
    void testAuditTrailAccess() throws Exception {
        mockMvc.perform(get("/api/v1/audit")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(greaterThanOrEqualTo(1)));

        // Auditor also has authorized read access
        mockMvc.perform(get("/api/v1/audit")
                        .header("Authorization", "Bearer " + auditorToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Institution officer is forbidden from viewing regulatory audit logs")
    void testOfficerForbiddenFromAuditTrail() throws Exception {
        mockMvc.perform(get("/api/v1/audit")
                        .header("Authorization", "Bearer " + officerToken))
                .andExpect(status().isForbidden());
    }
}
