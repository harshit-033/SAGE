package org.aicte.sage.modules.evc;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aicte.sage.modules.auth.dto.LoginRequest;
import org.aicte.sage.modules.evc.dto.SchedulingRequestDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EvcAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String DELHI_INSP_ID = "ffffffff-ffff-ffff-ffff-000000000001";
    private static final String MUMBAI_INSP_ID = "ffffffff-ffff-ffff-ffff-000000000002";

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
    @DisplayName("Authorization Test 1: EVC Coordinator CAN generate inspection schedules")
    void testEvcCoordinatorCanGenerateSchedule() throws Exception {
        String coordToken = loginAndGetToken("evc.coord@aicte.gov.in", "Password123!");

        SchedulingRequestDto req = new SchedulingRequestDto(
                List.of(UUID.fromString(DELHI_INSP_ID)),
                true,
                10
        );

        mockMvc.perform(post("/api/v1/evc/schedules")
                        .header("Authorization", "Bearer " + coordToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OPTIMAL"))
                .andExpect(jsonPath("$.scheduledInspections").isArray());
    }

    @Test
    @DisplayName("Authorization Test 2: AICTE Central Admin CAN generate inspection schedules")
    void testAdminCanGenerateSchedule() throws Exception {
        String adminToken = loginAndGetToken("admin@aicte.gov.in", "Password123!");

        SchedulingRequestDto req = new SchedulingRequestDto(
                List.of(UUID.fromString(DELHI_INSP_ID)),
                true,
                10
        );

        mockMvc.perform(post("/api/v1/evc/schedules")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OPTIMAL"));
    }

    @Test
    @DisplayName("Authorization Test 3: Institution Officer is FORBIDDEN from generating schedules (403)")
    void testInstitutionOfficerForbiddenFromGeneratingSchedule() throws Exception {
        String instToken = loginAndGetToken("officer@delhi-tech.edu", "Password123!");

        SchedulingRequestDto req = new SchedulingRequestDto(
                List.of(UUID.fromString(DELHI_INSP_ID)),
                false,
                10
        );

        mockMvc.perform(post("/api/v1/evc/schedules")
                        .header("Authorization", "Bearer " + instToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Authorization Test 4: Regulatory Auditor is FORBIDDEN from generating schedules (403)")
    void testAuditorForbiddenFromGeneratingSchedule() throws Exception {
        String auditorToken = loginAndGetToken("auditor@aicte.gov.in", "Password123!");

        SchedulingRequestDto req = new SchedulingRequestDto(
                List.of(UUID.fromString(DELHI_INSP_ID)),
                false,
                10
        );

        mockMvc.perform(post("/api/v1/evc/schedules")
                        .header("Authorization", "Bearer " + auditorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Authorization Test 5: Unauthenticated access is rejected with 401 Unauthorized")
    void testUnauthenticatedAccessRejected() throws Exception {
        mockMvc.perform(get("/api/v1/evc/inspections/pending"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Authorization Test 6: Institution Officer CAN view inspections for their own institution")
    void testInstitutionOfficerCanViewOwnInspection() throws Exception {
        String instToken = loginAndGetToken("officer@delhi-tech.edu", "Password123!");

        mockMvc.perform(get("/api/v1/evc/inspections/" + DELHI_INSP_ID)
                        .header("Authorization", "Bearer " + instToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(DELHI_INSP_ID))
                .andExpect(jsonPath("$.institutionName", containsString("Delhi")));
    }

    @Test
    @DisplayName("Authorization Test 7: Institution Officer CANNOT view inspections for another institution (403)")
    void testInstitutionOfficerCannotViewOtherInstitutionInspection() throws Exception {
        String instToken = loginAndGetToken("officer@delhi-tech.edu", "Password123!");

        mockMvc.perform(get("/api/v1/evc/inspections/" + MUMBAI_INSP_ID)
                        .header("Authorization", "Bearer " + instToken))
                .andExpect(status().isForbidden());
    }
}
