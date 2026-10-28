package org.aicte.sage;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aicte.sage.modules.auth.dto.LoginRequest;
import org.aicte.sage.modules.users.dto.AssignRolesRequest;
import org.aicte.sage.modules.users.dto.CreateUserRequest;
import org.aicte.sage.modules.users.dto.UpdateUserRequest;
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

import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UserManagementTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;
    private String officerToken;

    @BeforeEach
    void setUp() throws Exception {
        adminToken = obtainToken("admin@aicte.gov.in", "Password123!");
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
    @DisplayName("Admin can retrieve paginated users list")
    void testListUsersPaginated() throws Exception {
        mockMvc.perform(get("/api/v1/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(greaterThanOrEqualTo(4)));
    }

    @Test
    @DisplayName("Regular institution officer is forbidden from accessing user management")
    void testOfficerForbiddenFromUserManagement() throws Exception {
        mockMvc.perform(get("/api/v1/users")
                        .header("Authorization", "Bearer " + officerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Admin can create new user, update, and manage roles")
    void testUserLifecycle() throws Exception {
        String uniqueUser = "testuser_" + System.currentTimeMillis();
        CreateUserRequest createReq = new CreateUserRequest(
                uniqueUser,
                uniqueUser + "@aicte.gov.in",
                "Prof. Testing User",
                "SecureSecret123!",
                Set.of("ROLE_SCRUTINY_OFFICER"),
                null
        );

        MvcResult createResult = mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.username").value(uniqueUser))
                .andExpect(jsonPath("$.data.roles", hasItem("ROLE_SCRUTINY_OFFICER")))
                .andReturn();

        String userId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .path("data").path("id").asText();

        // Update profile
        UpdateUserRequest updateReq = new UpdateUserRequest("Prof. Testing User (Updated)", uniqueUser + "_alt@aicte.gov.in");
        mockMvc.perform(put("/api/v1/users/" + userId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fullName").value("Prof. Testing User (Updated)"));

        // Assign additional roles
        AssignRolesRequest assignReq = new AssignRolesRequest(Set.of("ROLE_SCRUTINY_OFFICER", "ROLE_EVC_MEMBER"));
        mockMvc.perform(put("/api/v1/users/" + userId + "/roles")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roles", hasItem("ROLE_EVC_MEMBER")));

        // Suspend user
        UserStatusRequest statusReq = new UserStatusRequest(false, "Routine offboarding");
        mockMvc.perform(patch("/api/v1/users/" + userId + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.active").value(false));
    }

    @Test
    @DisplayName("Admin cannot suspend their own active account")
    void testSelfSuspensionPrevented() throws Exception {
        UserStatusRequest statusReq = new UserStatusRequest(false, "Accidental lockout attempt");
        // Admin user ID is 11111111-1111-1111-1111-111111111111
        mockMvc.perform(patch("/api/v1/users/11111111-1111-1111-1111-111111111111/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", containsString("cannot suspend their own active session")));
    }
}
