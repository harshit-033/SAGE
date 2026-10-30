package in.gov.aicte.evc.api;

import in.gov.aicte.evc.api.dto.ScheduleApiRequest;
import in.gov.aicte.evc.entity.*;
import in.gov.aicte.evc.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive integration tests for production EVC Scheduling REST API endpoints.
 * Validates endpoint security, validation rules, Sunday exclusion, CP-SAT orchestration,
 * persistence guarantees, and error handling.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class EvcScheduleApiControllerTests {

    private static final String API_KEY = "test-evc-secret-key-2026";
    private static final String INVALID_API_KEY = "invalid-key-xyz-999";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private InstitutionRepository institutionRepository;

    @Autowired
    private ExpertRepository expertRepository;

    @Autowired
    private ExpertAvailabilityRepository availabilityRepository;

    @Autowired
    private EvcInspectionRepository inspectionRepository;

    @Autowired
    private CommitteeAssignmentRepository assignmentRepository;

    @BeforeEach
    void setUp() {
        cleanDatabase();
    }

    @AfterEach
    void tearDown() {
        cleanDatabase();
    }

    private void cleanDatabase() {
        assignmentRepository.deleteAll();
        availabilityRepository.deleteAll();
        inspectionRepository.deleteAll();
        institutionRepository.deleteAll();
        expertRepository.deleteAll();
    }

    private void addAvailability(Expert expert, LocalDate start, LocalDate end) {
        LocalDate curr = start;
        while (!curr.isAfter(end)) {
            availabilityRepository.save(new ExpertAvailabilityRecord(expert, curr));
            curr = curr.plusDays(1);
        }
    }

    // -------------------------------------------------------------------------
    // 1. Health Probe & OpenAPI Documentation (Public Endpoints)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/v1/health is public, requires no auth, and returns operational UP status")
    void testHealthEndpointPublicAndReturnsUp() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("UP")))
                .andExpect(jsonPath("$.service", is("evc-scheduling-engine")))
                .andExpect(jsonPath("$.version", is("0.0.1-SNAPSHOT")))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    @DisplayName("GET /v3/api-docs is public and returns valid OpenAPI 3 specification")
    void testOpenApiDocsPublicAndValid() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.openapi", notNullValue()))
                .andExpect(jsonPath("$.paths['/api/v1/schedules']", notNullValue()))
                .andExpect(jsonPath("$.paths['/api/v1/schedules/{inspectionId}']", notNullValue()))
                .andExpect(jsonPath("$.components.securitySchemes.ApiKeyAuth", notNullValue()))
                .andExpect(jsonPath("$.components.securitySchemes.BearerAuth", notNullValue()));
    }

    // -------------------------------------------------------------------------
    // 2. Security & Authentication Tests (401, 403, and authorized access)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/v1/schedules without credentials returns 401 Unauthorized")
    void testScheduleWithoutAuthReturns401() throws Exception {
        ScheduleApiRequest request = new ScheduleApiRequest(List.of(1L), false, 10, null, "req-1");
        mockMvc.perform(post("/api/v1/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.error", is("Unauthorized")));
    }

    @Test
    @DisplayName("GET /api/v1/schedules/{id} without credentials returns 401 Unauthorized")
    void testGetScheduleWithoutAuthReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/schedules/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.error", is("Unauthorized")));
    }

    @Test
    @DisplayName("POST /api/v1/schedules with invalid X-API-KEY returns 403 Forbidden")
    void testScheduleWithInvalidApiKeyReturns403() throws Exception {
        ScheduleApiRequest request = new ScheduleApiRequest(List.of(1L), false, 10, null, "req-1");
        mockMvc.perform(post("/api/v1/schedules")
                        .header("X-API-KEY", INVALID_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(403)))
                .andExpect(jsonPath("$.error", is("Forbidden")));
    }

    @Test
    @DisplayName("GET /api/v1/schedules/{id} with invalid Bearer token returns 403 Forbidden")
    void testGetScheduleWithInvalidBearerReturns403() throws Exception {
        mockMvc.perform(get("/api/v1/schedules/1")
                        .header("Authorization", "Bearer " + INVALID_API_KEY))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(403)))
                .andExpect(jsonPath("$.error", is("Forbidden")));
    }

    // -------------------------------------------------------------------------
    // 3. Validation Tests (400 Bad Request)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/v1/schedules with empty inspectionIds returns 400 Bad Request")
    void testScheduleEmptyInspectionIdsReturns400() throws Exception {
        ScheduleApiRequest request = new ScheduleApiRequest(List.of(), false, 10, null, "req-empty");
        mockMvc.perform(post("/api/v1/schedules")
                        .header("X-API-KEY", API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.details", notNullValue()));
    }

    @Test
    @DisplayName("POST /api/v1/schedules with negative timeLimitSeconds returns 400 Bad Request")
    void testScheduleNegativeTimeLimitReturns400() throws Exception {
        ScheduleApiRequest request = new ScheduleApiRequest(List.of(1L), false, -5, null, "req-neg-time");
        mockMvc.perform(post("/api/v1/schedules")
                        .header("X-API-KEY", API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(400)));
    }

    // -------------------------------------------------------------------------
    // 4. Batch Scheduling Success with Sunday Exclusion (200 OK)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/v1/schedules generates schedule, excludes Sundays, and persists assignments")
    void testScheduleBatchSuccessWithSundayExclusion() throws Exception {
        Institution inst = institutionRepository.save(
                new Institution("1-TEST-001", "Institute of Technology", "New Delhi", "Delhi", "Northern"));

        // Setup 3 experts
        Expert e1 = expertRepository.save(new Expert("Dr. A. Sharma", "sharma@test.gov.in", "Computer Science", true));
        Expert e2 = expertRepository.save(new Expert("Dr. B. Verma", "verma@test.gov.in", "Computer Science", true));
        Expert e3 = expertRepository.save(new Expert("Dr. C. Patel", "patel@test.gov.in", "Computer Science", true));

        // Available across entire inspection window (Nov 2 to Nov 20, 2026)
        addAvailability(e1, LocalDate.of(2026, 11, 2), LocalDate.of(2026, 11, 20));
        addAvailability(e2, LocalDate.of(2026, 11, 2), LocalDate.of(2026, 11, 20));
        addAvailability(e3, LocalDate.of(2026, 11, 2), LocalDate.of(2026, 11, 20));

        // Create inspection: Duration 3 days, 3 members, window Nov 5 to Nov 15
        EvcInspection inspection = inspectionRepository.save(
                new EvcInspection(inst, 3, 3, LocalDate.of(2026, 11, 5), LocalDate.of(2026, 11, 15)));

        ScheduleApiRequest request = new ScheduleApiRequest(
                List.of(inspection.getId()),
                false,
                10,
                null,
                "batch-req-001"
        );

        MvcResult result = mockMvc.perform(post("/api/v1/schedules")
                        .header("X-API-KEY", API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.requestId", is("batch-req-001")))
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.persisted", is(true)))
                .andExpect(jsonPath("$.totalInspectionsRequested", is(1)))
                .andExpect(jsonPath("$.totalInspectionsScheduled", is(1)))
                .andExpect(jsonPath("$.scheduledInspections", hasSize(1)))
                .andExpect(jsonPath("$.scheduledInspections[0].inspectionId", is(inspection.getId().intValue())))
                .andExpect(jsonPath("$.scheduledInspections[0].institutionName", is("Institute of Technology")))
                .andExpect(jsonPath("$.scheduledInspections[0].durationInDays", is(3)))
                .andExpect(jsonPath("$.scheduledInspections[0].requiredMemberCount", is(3)))
                .andExpect(jsonPath("$.scheduledInspections[0].actualInspectionDays", hasSize(3)))
                .andExpect(jsonPath("$.scheduledInspections[0].assignedExperts", hasSize(3)))
                .andReturn();

        // Verify that NONE of the scheduled actualInspectionDays falls on Sunday
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        JsonNode actualDaysNode = root.path("scheduledInspections").get(0).path("actualInspectionDays");
        for (JsonNode dayNode : actualDaysNode) {
            LocalDate day = LocalDate.parse(dayNode.asText());
            assertThat(day.getDayOfWeek()).isNotEqualTo(DayOfWeek.SUNDAY);
        }

        // Verify database persistence
        EvcInspection persistedInspection = inspectionRepository.findById(inspection.getId()).orElseThrow();
        assertThat(persistedInspection.getStatus()).isEqualTo(InspectionStatus.SCHEDULED);
        assertThat(persistedInspection.getScheduledStartDate()).isNotNull();
        assertThat(persistedInspection.getScheduledEndDate()).isNotNull();

        List<CommitteeAssignment> assignments = assignmentRepository.findByInspectionId(inspection.getId());
        assertThat(assignments).hasSize(3);
    }

    // -------------------------------------------------------------------------
    // 5. GET Schedule Retrieval (200 OK & 404 Not Found)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/v1/schedules/{id} via Bearer token returns 200 OK with persisted committee")
    void testGetScheduleSuccess() throws Exception {
        Institution inst = institutionRepository.save(
                new Institution("1-GET-001", "Polytechnic College", "Pune", "Maharashtra", "Western"));

        Expert e1 = expertRepository.save(new Expert("Dr. Chair Person", "chair@test.gov.in", "Electrical", true));
        Expert e2 = expertRepository.save(new Expert("Dr. Member Person", "member@test.gov.in", "Electrical", true));

        // Create an already SCHEDULED inspection
        EvcInspection inspection = new EvcInspection(inst, 2, 2, LocalDate.of(2026, 11, 2), LocalDate.of(2026, 11, 10));
        inspection.setScheduledStartDate(LocalDate.of(2026, 11, 3));
        inspection.setStatus(InspectionStatus.SCHEDULED);
        inspection = inspectionRepository.save(inspection);

        assignmentRepository.save(new CommitteeAssignment(inspection, e1, AssignmentRole.CHAIRPERSON, AssignmentStatus.CONFIRMED));
        assignmentRepository.save(new CommitteeAssignment(inspection, e2, AssignmentRole.MEMBER, AssignmentStatus.CONFIRMED));

        mockMvc.perform(get("/api/v1/schedules/" + inspection.getId())
                        .header("Authorization", "Bearer " + API_KEY))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.inspectionId", is(inspection.getId().intValue())))
                .andExpect(jsonPath("$.institutionName", is("Polytechnic College")))
                .andExpect(jsonPath("$.durationInDays", is(2)))
                .andExpect(jsonPath("$.requiredMemberCount", is(2)))
                .andExpect(jsonPath("$.scheduledStartDate", is("2026-11-03")))
                .andExpect(jsonPath("$.actualInspectionDays", hasSize(2)))
                .andExpect(jsonPath("$.assignedExperts", hasSize(2)))
                .andExpect(jsonPath("$.assignedExperts[0].role", notNullValue()))
                .andExpect(jsonPath("$.assignedExperts[0].status", is("CONFIRMED")));
    }

    @Test
    @DisplayName("GET /api/v1/schedules/{id} for unscheduled inspection returns 404 Not Found")
    void testGetScheduleUnscheduledReturns404() throws Exception {
        Institution inst = institutionRepository.save(
                new Institution("1-UNSCH-01", "Pending College", "Mumbai", "Maharashtra", "Western"));
        EvcInspection unscheduled = inspectionRepository.save(
                new EvcInspection(inst, 3, 3, LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 15)));

        mockMvc.perform(get("/api/v1/schedules/" + unscheduled.getId())
                        .header("X-API-KEY", API_KEY))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", containsString("has not been scheduled yet")));
    }

    @Test
    @DisplayName("GET /api/v1/schedules/{id} for non-existent ID returns 404 Not Found")
    void testGetScheduleNonExistentReturns404() throws Exception {
        mockMvc.perform(get("/api/v1/schedules/999999")
                        .header("X-API-KEY", API_KEY))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", containsString("not found")));
    }

    // -------------------------------------------------------------------------
    // 6. Infeasibility (422 Unprocessable Entity) & Transactional Rollback
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/v1/schedules returns 422 Unprocessable Entity when problem is infeasible and does not persist")
    void testScheduleInfeasibleReturns422AndNoPersistence() throws Exception {
        Institution inst = institutionRepository.save(
                new Institution("1-INFEAS-01", "Infeasible Tech", "Bhopal", "MP", "Central"));

        // Only 1 expert available, but inspection requires 3 members
        Expert e1 = expertRepository.save(new Expert("Dr. Lone Expert", "lone@test.gov.in", "Civil", true));
        addAvailability(e1, LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 20));

        EvcInspection inspection = inspectionRepository.save(
                new EvcInspection(inst, 3, 3, LocalDate.of(2026, 11, 5), LocalDate.of(2026, 11, 15)));

        ScheduleApiRequest request = new ScheduleApiRequest(
                List.of(inspection.getId()),
                false,
                5,
                null,
                "req-infeasible"
        );

        mockMvc.perform(post("/api/v1/schedules")
                        .header("X-API-KEY", API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.persisted", is(false)))
                .andExpect(jsonPath("$.status", is("INFEASIBLE")));

        // Verify DB record remained untouched in PENDING state
        EvcInspection reloaded = inspectionRepository.findById(inspection.getId()).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(InspectionStatus.PENDING);
        assertThat(reloaded.getScheduledStartDate()).isNull();
        assertThat(assignmentRepository.findByInspectionId(inspection.getId())).isEmpty();
    }

    // -------------------------------------------------------------------------
    // 7. Rescheduling Conflict (409 Conflict) & Safety
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/v1/schedules returns 409 Conflict when inspection is already SCHEDULED and allowRescheduling=false")
    void testScheduleReschedulingConflictReturns409() throws Exception {
        Institution inst = institutionRepository.save(
                new Institution("1-CONF-01", "Conflict College", "Jaipur", "Rajasthan", "Northern"));
        EvcInspection scheduled = new EvcInspection(inst, 3, 3, LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 15));
        scheduled.setScheduledStartDate(LocalDate.of(2026, 11, 3));
        scheduled.setStatus(InspectionStatus.SCHEDULED);
        scheduled = inspectionRepository.save(scheduled);

        // Submit with allowRescheduling = false
        ScheduleApiRequest request = new ScheduleApiRequest(
                List.of(scheduled.getId()),
                false,
                10,
                null,
                "req-conflict"
        );

        mockMvc.perform(post("/api/v1/schedules")
                        .header("X-API-KEY", API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.persisted", is(false)))
                .andExpect(jsonPath("$.explanation", containsString("already SCHEDULED")));
    }

    @Test
    @DisplayName("POST /api/v1/schedules failed rescheduling preserves previous valid schedule in database")
    void testFailedReschedulingPreservesPreviousSchedule() throws Exception {
        Institution inst = institutionRepository.save(
                new Institution("1-PRESERVE-01", "Preservation College", "Ahmedabad", "Gujarat", "Western"));
        Expert expert = expertRepository.save(new Expert("Dr. Stable", "stable@test.gov.in", "Electrical", true));

        LocalDate originalStart = LocalDate.of(2026, 11, 4);
        EvcInspection scheduled = new EvcInspection(inst, 2, 1, LocalDate.of(2026, 11, 2), LocalDate.of(2026, 11, 10));
        scheduled.setScheduledStartDate(originalStart);
        scheduled.setStatus(InspectionStatus.SCHEDULED);
        scheduled = inspectionRepository.save(scheduled);

        assignmentRepository.save(new CommitteeAssignment(scheduled, expert, AssignmentRole.CHAIRPERSON, AssignmentStatus.CONFIRMED));

        // Attempt rescheduling with allowRescheduling = true, but NO experts are available (making it infeasible)
        availabilityRepository.deleteAll(); // expert has no availability

        ScheduleApiRequest request = new ScheduleApiRequest(
                List.of(scheduled.getId()),
                true,
                5,
                null,
                "req-fail-reschedule"
        );

        mockMvc.perform(post("/api/v1/schedules")
                        .header("X-API-KEY", API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity());

        // Previous schedule must be preserved!
        EvcInspection preserved = inspectionRepository.findById(scheduled.getId()).orElseThrow();
        assertThat(preserved.getStatus()).isEqualTo(InspectionStatus.SCHEDULED);
        assertThat(preserved.getScheduledStartDate()).isEqualTo(originalStart);
        assertThat(assignmentRepository.findByInspectionId(scheduled.getId())).hasSize(1);
    }

    // -------------------------------------------------------------------------
    // 8. Dynamic Durations & Committee Sizes
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/v1/schedules accurately schedules multiple inspections with different durations and committee sizes")
    void testDynamicDurationsAndSizes() throws Exception {
        Institution instA = institutionRepository.save(
                new Institution("1-DYN-A", "College of Engineering A", "Kolkata", "WB", "Eastern"));
        Institution instB = institutionRepository.save(
                new Institution("1-DYN-B", "College of Engineering B", "Lucknow", "UP", "Northern"));

        // 4 active experts
        Expert e1 = expertRepository.save(new Expert("Dr. Expert One", "exp1@test.gov.in", "Tech", true));
        Expert e2 = expertRepository.save(new Expert("Dr. Expert Two", "exp2@test.gov.in", "Tech", true));
        Expert e3 = expertRepository.save(new Expert("Dr. Expert Three", "exp3@test.gov.in", "Tech", true));
        Expert e4 = expertRepository.save(new Expert("Dr. Expert Four", "exp4@test.gov.in", "Tech", true));

        LocalDate winStart = LocalDate.of(2026, 11, 2);
        LocalDate winEnd = LocalDate.of(2026, 11, 30);
        addAvailability(e1, winStart, winEnd);
        addAvailability(e2, winStart, winEnd);
        addAvailability(e3, winStart, winEnd);
        addAvailability(e4, winStart, winEnd);

        // Inspection A: 2 days, 2 members
        EvcInspection inspA = inspectionRepository.save(
                new EvcInspection(instA, 2, 2, LocalDate.of(2026, 11, 2), LocalDate.of(2026, 11, 10)));

        // Inspection B: 4 days, 3 members
        EvcInspection inspB = inspectionRepository.save(
                new EvcInspection(instB, 4, 3, LocalDate.of(2026, 11, 12), LocalDate.of(2026, 11, 25)));

        ScheduleApiRequest request = new ScheduleApiRequest(
                List.of(inspA.getId(), inspB.getId()),
                false,
                10,
                null,
                "req-dynamic"
        );

        mockMvc.perform(post("/api/v1/schedules")
                        .header("X-API-KEY", API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.totalInspectionsScheduled", is(2)))
                .andExpect(jsonPath("$.scheduledInspections", hasSize(2)));

        EvcInspection savedA = inspectionRepository.findById(inspA.getId()).orElseThrow();
        EvcInspection savedB = inspectionRepository.findById(inspB.getId()).orElseThrow();

        assertThat(assignmentRepository.findByInspectionId(savedA.getId())).hasSize(2);
        assertThat(assignmentRepository.findByInspectionId(savedB.getId())).hasSize(3);
    }
}
