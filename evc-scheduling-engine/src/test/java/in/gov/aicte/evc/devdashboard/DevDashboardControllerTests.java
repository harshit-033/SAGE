package in.gov.aicte.evc.devdashboard;

import in.gov.aicte.evc.devdashboard.dto.DashboardExpertAvailabilityDto;
import in.gov.aicte.evc.devdashboard.dto.DashboardInspectionDto;
import in.gov.aicte.evc.devdashboard.dto.DashboardScheduleRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class DevDashboardControllerTests {

    @Nested
    @SpringBootTest
    @AutoConfigureMockMvc
    @ActiveProfiles("dev-dashboard")
    class ActiveProfileTests {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ApplicationContext applicationContext;

        @Autowired
        private ObjectMapper objectMapper;

        @Test
        @DisplayName("Controller bean is registered when 'dev-dashboard' profile is active")
        void testControllerBeanIsPresent() {
            assertTrue(applicationContext.containsBean("devDashboardController"),
                    "DevDashboardController must be registered when dev-dashboard profile is active");
        }

        @Test
        @DisplayName("GET /dev/dashboard/api/scenarios returns all 5 demo scenarios")
        void testGetScenariosReturnsFiveItems() throws Exception {
            mockMvc.perform(get("/dev/dashboard/api/scenarios"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(5)))
                    .andExpect(jsonPath("$[0].id", is("scenario-1")))
                    .andExpect(jsonPath("$[0].name", containsString("Dynamic Durations")))
                    .andExpect(jsonPath("$[1].id", is("scenario-2")))
                    .andExpect(jsonPath("$[2].id", is("scenario-3")))
                    .andExpect(jsonPath("$[3].id", is("scenario-4")))
                    .andExpect(jsonPath("$[4].id", is("scenario-5")));
        }

        @Test
        @DisplayName("POST /dev/dashboard/api/schedule solves a feasible scenario via real CP-SAT solver")
        void testScheduleFeasibleScenario() throws Exception {
            DashboardInspectionDto i1 = new DashboardInspectionDto(
                    "INSP-ENGG-01",
                    3,
                    2,
                    LocalDate.of(2026, 11, 10),
                    LocalDate.of(2026, 11, 15),
                    List.of("EXP-01", "EXP-02", "EXP-03")
            );
            DashboardInspectionDto i2 = new DashboardInspectionDto(
                    "INSP-POLY-02",
                    4,
                    3,
                    LocalDate.of(2026, 11, 10),
                    LocalDate.of(2026, 11, 15),
                    List.of("EXP-04", "EXP-05", "EXP-06", "EXP-07")
            );

            List<DashboardExpertAvailabilityDto> avails = new ArrayList<>();
            for (int i = 1; i <= 7; i++) {
                List<LocalDate> dates = new ArrayList<>();
                LocalDate d = LocalDate.of(2026, 11, 10);
                while (!d.isAfter(LocalDate.of(2026, 11, 25))) {
                    dates.add(d);
                    d = d.plusDays(1);
                }
                avails.add(new DashboardExpertAvailabilityDto("EXP-0" + i, dates));
            }

            DashboardScheduleRequest request = new DashboardScheduleRequest(List.of(i1, i2), avails, 5);
            String payload = objectMapper.writeValueAsString(request);

            mockMvc.perform(post("/dev/dashboard/api/schedule")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.status", anyOf(is("OPTIMAL"), is("FEASIBLE"))))
                    .andExpect(jsonPath("$.totalInspectionsRequested", is(2)))
                    .andExpect(jsonPath("$.totalInspectionsScheduled", is(2)))
                    .andExpect(jsonPath("$.scheduledInspections", hasSize(2)))
                    .andExpect(jsonPath("$.scheduledInspections[0].assignedExpertIds", hasSize(2)))
                    .andExpect(jsonPath("$.scheduledInspections[1].assignedExpertIds", hasSize(3)));
        }

        @Test
        @DisplayName("POST /dev/dashboard/api/schedule detects pool deficiency infeasibility")
        void testScheduleInfeasiblePoolDeficiency() throws Exception {
            // Needs 3 experts but only 2 exist
            DashboardInspectionDto insp = new DashboardInspectionDto(
                    "INSP-DEFICIENT",
                    3,
                    3,
                    LocalDate.of(2026, 11, 10),
                    LocalDate.of(2026, 11, 15),
                    List.of("EXP-01", "EXP-02")
            );

            List<DashboardExpertAvailabilityDto> avails = List.of(
                    new DashboardExpertAvailabilityDto("EXP-01", List.of(LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 11), LocalDate.of(2026, 11, 12))),
                    new DashboardExpertAvailabilityDto("EXP-02", List.of(LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 11), LocalDate.of(2026, 11, 12)))
            );

            DashboardScheduleRequest request = new DashboardScheduleRequest(List.of(insp), avails, 5);
            String payload = objectMapper.writeValueAsString(request);

            mockMvc.perform(post("/dev/dashboard/api/schedule")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.status", is("INFEASIBLE")))
                    .andExpect(jsonPath("$.totalInspectionsScheduled", is(0)))
                    .andExpect(jsonPath("$.explanation", notNullValue()));
        }

        @Test
        @DisplayName("POST /dev/dashboard/api/schedule rejects inverted date window with 400 Bad Request")
        void testScheduleInvertedDatesValidation() throws Exception {
            DashboardInspectionDto invalid = new DashboardInspectionDto(
                    "INSP-INVALID",
                    3,
                    2,
                    LocalDate.of(2026, 11, 20), // earliest is after latest!
                    LocalDate.of(2026, 11, 10),
                    List.of("EXP-01", "EXP-02")
            );

            DashboardScheduleRequest request = new DashboardScheduleRequest(List.of(invalid), List.of(), 5);
            String payload = objectMapper.writeValueAsString(request);

            mockMvc.perform(post("/dev/dashboard/api/schedule")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error", containsString("is after latest start date")));
        }

        @Test
        @DisplayName("POST /dev/dashboard/api/schedule rejects negative duration with 400 Bad Request")
        void testScheduleNegativeDurationValidation() throws Exception {
            DashboardInspectionDto invalid = new DashboardInspectionDto(
                    "INSP-INVALID",
                    -1, // negative duration!
                    2,
                    LocalDate.of(2026, 11, 10),
                    LocalDate.of(2026, 11, 15),
                    List.of("EXP-01", "EXP-02")
            );

            DashboardScheduleRequest request = new DashboardScheduleRequest(List.of(invalid), List.of(), 5);
            String payload = objectMapper.writeValueAsString(request);

            mockMvc.perform(post("/dev/dashboard/api/schedule")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error", containsString("Duration must be positive")));
        }

        @Test
        @DisplayName("POST /dev/dashboard/api/schedule rejects empty inspection list with 400 Bad Request")
        void testScheduleEmptyListValidation() throws Exception {
            DashboardScheduleRequest request = new DashboardScheduleRequest(List.of(), List.of(), 5);
            String payload = objectMapper.writeValueAsString(request);

            mockMvc.perform(post("/dev/dashboard/api/schedule")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error", containsString("must contain at least one inspection")));
        }

        @Test
        @DisplayName("Static dev-dashboard asset is served when profile is active")
        void testStaticDashboardServedWhenActive() throws Exception {
            mockMvc.perform(get("/dev-dashboard/index.html"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("EVC Scheduling Engine")));
        }
    }

    @Nested
    @SpringBootTest
    @AutoConfigureMockMvc
    class InactiveProfileTests {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ApplicationContext applicationContext;

        @Test
        @DisplayName("Controller bean is NOT registered when 'dev-dashboard' profile is inactive")
        void testControllerBeanIsAbsent() {
            assertFalse(applicationContext.containsBean("devDashboardController"),
                    "DevDashboardController must NOT be registered in production or default profiles");
        }

        @Test
        @DisplayName("Access to /dev/dashboard/api/scenarios is blocked with 404 Not Found")
        void testScenariosBlocked() throws Exception {
            mockMvc.perform(get("/dev/dashboard/api/scenarios"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Access to /dev/dashboard/api/schedule is blocked with 404 Not Found")
        void testScheduleApiBlocked() throws Exception {
            mockMvc.perform(post("/dev/dashboard/api/schedule")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Access to /dev-dashboard/index.html is blocked with 404 Not Found")
        void testStaticAssetsBlocked() throws Exception {
            mockMvc.perform(get("/dev-dashboard/index.html"))
                    .andExpect(status().isNotFound());
        }
    }
}
