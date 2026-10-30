package org.aicte.sage;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aicte.sage.modules.applications.dto.CreateApplicationRequest;
import org.aicte.sage.modules.applications.dto.SaveBranchDetailsRequest;
import org.aicte.sage.modules.applications.dto.SaveCourseDetailsRequest;
import org.aicte.sage.modules.applications.model.ApplicationCategory;
import org.aicte.sage.modules.applications.model.ApplicationState;
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

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class NewCourseAndBranchWorkflowTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String delhiOfficerToken;
    private String mumbaiOfficerToken;
    private String adminToken;

    private static final String DELHI_INST_ID = "22222222-2222-2222-2222-222222222222";
    private static final String MUMBAI_INST_ID = "33333333-3333-3333-3333-333333333333";

    @BeforeEach
    void setUp() throws Exception {
        delhiOfficerToken = obtainToken("officer@delhi-tech.edu", "Password123!");
        mumbaiOfficerToken = obtainToken("officer@mumbai-engg.edu", "Password123!");
        adminToken = obtainToken("admin@aicte.gov.in", "Password123!");
    }

    private String obtainToken(String username, String password) throws Exception {
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
    @DisplayName("Create and save New Course Introduction application draft with category details")
    void testNewCourseApplicationLifecycle() throws Exception {
        // 1. Create New Course draft
        CreateApplicationRequest createReq = new CreateApplicationRequest(
                UUID.fromString(DELHI_INST_ID),
                "2026-2027",
                ApplicationCategory.NEW_COURSE_INTRODUCTION
        );

        MvcResult createRes = mockMvc.perform(post("/api/v1/applications")
                        .header("Authorization", "Bearer " + delhiOfficerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.category").value("NEW_COURSE_INTRODUCTION"))
                .andExpect(jsonPath("$.data.currentState").value("DRAFT"))
                .andExpect(jsonPath("$.data.workflowCode").value("WF_NEW_COURSE_INTRODUCTION"))
                .andReturn();

        String appId = objectMapper.readTree(createRes.getResponse().getContentAsString())
                .path("data").path("id").asText();

        // 2. Save Course Details
        SaveCourseDetailsRequest courseDetails = new SaveCourseDetailsRequest(
                "Artificial Intelligence & Machine Learning",
                "UNDERGRADUATE",
                "B.Tech",
                "Computer Science and Engineering",
                60,
                "REGULAR",
                4,
                "EMERGING_AREA",
                "Department of CSE",
                "B.Tech Computer Science and Engineering",
                4,
                2,
                "Dedicated GPU AI Server Lab with 30 Workstations",
                "High performance computing clusters, NVIDIA RTX 4090",
                500,
                1500,
                15,
                2,
                3,
                6,
                "1:15",
                "Independent block with dedicated high-speed fiber internet"
        );

        mockMvc.perform(post("/api/v1/applications/" + appId + "/course-details")
                        .header("Authorization", "Bearer " + delhiOfficerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(courseDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.courseName").value("Artificial Intelligence & Machine Learning"))
                .andExpect(jsonPath("$.data.proposedIntake").value(60))
                .andExpect(jsonPath("$.data.laboratoriesCount").value(2))
                .andExpect(jsonPath("$.data.professorsCount").value(2));

        // 3. Retrieve application and ensure course details are embedded
        mockMvc.perform(get("/api/v1/applications/" + appId)
                        .header("Authorization", "Bearer " + delhiOfficerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(appId))
                .andExpect(jsonPath("$.data.courseDetails.courseName").value("Artificial Intelligence & Machine Learning"))
                .andExpect(jsonPath("$.data.courseDetails.proposedIntake").value(60));

        // 4. Test Cross-Tenant Access Isolation (Mumbai officer cannot read or modify Delhi application)
        mockMvc.perform(get("/api/v1/applications/" + appId)
                        .header("Authorization", "Bearer " + mumbaiOfficerToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/v1/applications/" + appId + "/course-details")
                        .header("Authorization", "Bearer " + mumbaiOfficerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(courseDetails)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Create and save New Branch Introduction application draft with branch details")
    void testNewBranchApplicationLifecycle() throws Exception {
        // 1. Create New Branch draft
        CreateApplicationRequest createReq = new CreateApplicationRequest(
                UUID.fromString(DELHI_INST_ID),
                "2026-2027",
                ApplicationCategory.NEW_BRANCH_INTRODUCTION
        );

        MvcResult createRes = mockMvc.perform(post("/api/v1/applications")
                        .header("Authorization", "Bearer " + delhiOfficerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.category").value("NEW_BRANCH_INTRODUCTION"))
                .andExpect(jsonPath("$.data.workflowCode").value("WF_NEW_BRANCH_INTRODUCTION"))
                .andReturn();

        String appId = objectMapper.readTree(createRes.getResponse().getContentAsString())
                .path("data").path("id").asText();

        // 2. Save Branch Details
        SaveBranchDetailsRequest branchDetails = new SaveBranchDetailsRequest(
                "Mechanical Engineering",
                "Department of Mechanical Engineering",
                "Robotics and Automation",
                30,
                120,
                4,
                2,
                2,
                2,
                "Dedicated Industrial Robotics Lab with Kuka/ABB arms",
                "PLC trainers, Pneumatic/Hydraulic kits, ROS workstations",
                "Certified compliance with AICTE model curriculum"
        );

        mockMvc.perform(post("/api/v1/applications/" + appId + "/branch-details")
                        .header("Authorization", "Bearer " + delhiOfficerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(branchDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.proposedBranchName").value("Robotics and Automation"))
                .andExpect(jsonPath("$.data.proposedIntake").value(30))
                .andExpect(jsonPath("$.data.facultyCount").value(4));

        // 3. Retrieve application and ensure branch details are populated
        mockMvc.perform(get("/api/v1/applications/" + appId)
                        .header("Authorization", "Bearer " + delhiOfficerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.branchDetails.proposedBranchName").value("Robotics and Automation"))
                .andExpect(jsonPath("$.data.branchDetails.existingProgramme").value("Mechanical Engineering"));
    }

    @Test
    @DisplayName("Scrutiny evaluation triggers category-aware APH rules for New Course")
    void testScrutinyEvaluationForNewCourse() throws Exception {
        CreateApplicationRequest createReq = new CreateApplicationRequest(
                UUID.fromString(DELHI_INST_ID),
                "2026-2027",
                ApplicationCategory.NEW_COURSE_INTRODUCTION
        );

        MvcResult createRes = mockMvc.perform(post("/api/v1/applications")
                        .header("Authorization", "Bearer " + delhiOfficerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andReturn();

        String appId = objectMapper.readTree(createRes.getResponse().getContentAsString())
                .path("data").path("id").asText();

        // Run scrutiny on draft application (missing mandatory documents and course details)
        mockMvc.perform(post("/api/v1/applications/" + appId + "/scrutiny/evaluate")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("DEFICIENT"))
                .andExpect(jsonPath("$.data.findings", hasSize(greaterThanOrEqualTo(1))));
    }
}
