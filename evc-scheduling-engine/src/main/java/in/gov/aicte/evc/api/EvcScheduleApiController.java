package in.gov.aicte.evc.api;

import in.gov.aicte.evc.api.dto.*;
import in.gov.aicte.evc.entity.CommitteeAssignment;
import in.gov.aicte.evc.entity.EvcInspection;
import in.gov.aicte.evc.entity.InspectionStatus;
import in.gov.aicte.evc.repository.CommitteeAssignmentRepository;
import in.gov.aicte.evc.repository.EvcInspectionRepository;
import in.gov.aicte.evc.scheduler.model.SchedulingStatus;
import in.gov.aicte.evc.service.EvcSchedulingService;
import in.gov.aicte.evc.service.dto.ScheduledInspectionResultDto;
import in.gov.aicte.evc.service.dto.SchedulingRequest;
import in.gov.aicte.evc.service.dto.SchedulingServiceResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDate;
import java.util.*;

/**
 * Production REST controller exposing scheduling endpoints to the main SAGE platform.
 * HTTP concerns are strictly decoupled from solver formulation and database persistence.
 */
@Tag(name = "Schedules", description = "Production EVC scheduling orchestration and schedule retrieval API for SAGE")
@SecurityRequirement(name = "ApiKeyAuth")
@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping("/api/v1/schedules")
public class EvcScheduleApiController {

    private static final Logger log = LoggerFactory.getLogger(EvcScheduleApiController.class);

    private final EvcSchedulingService schedulingService;
    private final EvcInspectionRepository inspectionRepository;
    private final CommitteeAssignmentRepository assignmentRepository;

    public EvcScheduleApiController(EvcSchedulingService schedulingService,
                                   EvcInspectionRepository inspectionRepository,
                                   CommitteeAssignmentRepository assignmentRepository) {
        this.schedulingService = schedulingService;
        this.inspectionRepository = inspectionRepository;
        this.assignmentRepository = assignmentRepository;
    }

    @Operation(summary = "Submit batch scheduling request", description = """
            Schedules one or more inspections using the Google OR-Tools CP-SAT solver.
            Inspection durations, committee member counts, start date windows, and expert eligibility
            are loaded authoritatively from the database.
            Sunday is strictly excluded from inspection activities.
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Schedule generated and persisted successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error or invalid request parameters"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid service authentication key"),
            @ApiResponse(responseCode = "409", description = "Rescheduling conflict: inspection is already scheduled and allowRescheduling=false"),
            @ApiResponse(responseCode = "422", description = "Mathematically infeasible under constraints (no persistence occurred)"),
            @ApiResponse(responseCode = "500", description = "Internal solver or database persistence error")
    })
    @PostMapping
    public ResponseEntity<ScheduleApiResponse> schedule(@Valid @RequestBody ScheduleApiRequest request) {
        SchedulingRequest.Builder builder = SchedulingRequest.builder()
                .inspectionIds(request.inspectionIds())
                .allowRescheduling(request.allowRescheduling());

        if (request.timeLimitSeconds() != null && request.timeLimitSeconds() > 0) {
            builder.timeLimit(Duration.ofSeconds(request.timeLimitSeconds()));
        }

        if (request.eligibleExpertOverrides() != null) {
            request.eligibleExpertOverrides().forEach(builder::eligibleExperts);
        }

        SchedulingRequest serviceRequest = builder.build();

        log.info("Received scheduling request [requestId={}] for {} inspections (allowRescheduling={})",
                request.requestId(), request.inspectionIds().size(), request.allowRescheduling());

        SchedulingServiceResult serviceResult = schedulingService.schedule(serviceRequest);

        // Map scheduled inspections with actual non-Sunday inspection days
        List<ScheduledInspectionApiDto> scheduledDtos = new ArrayList<>();
        if (serviceResult.isSuccess() && serviceResult.getScheduledInspections() != null) {
            for (ScheduledInspectionResultDto dto : serviceResult.getScheduledInspections()) {
                List<LocalDate> actualDays = EvcInspection.calculateInspectionDays(
                        dto.scheduledStartDate(), dto.durationInDays());

                List<AssignedExpertApiDto> experts = dto.assignedExperts().stream()
                        .map(e -> new AssignedExpertApiDto(
                                e.expertId(), e.expertName(), e.discipline(), e.role(), e.status()))
                        .toList();

                scheduledDtos.add(new ScheduledInspectionApiDto(
                        dto.inspectionId(),
                        dto.institutionId(),
                        dto.institutionName(),
                        dto.scheduledStartDate(),
                        dto.scheduledEndDate(),
                        dto.durationInDays(),
                        dto.requiredMemberCount(),
                        actualDays,
                        experts
                ));
            }
        }

        ScheduleApiResponse response = new ScheduleApiResponse(
                request.requestId(),
                serviceResult.getStatus(),
                serviceResult.isSuccess(),
                serviceResult.isPersisted(),
                serviceResult.getExplanation(),
                serviceResult.totalInspectionsRequested(),
                serviceResult.totalInspectionsScheduled(),
                serviceResult.getSolveDurationMs(),
                scheduledDtos
        );

        if (serviceResult.isSuccess() && serviceResult.isPersisted()) {
            return ResponseEntity.ok(response);
        } else if (serviceResult.getStatus() == SchedulingStatus.INFEASIBLE ||
                serviceResult.getStatus() == SchedulingStatus.TIME_LIMIT_REACHED) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
        } else if (serviceResult.getExplanation() != null && serviceResult.getExplanation().contains("already SCHEDULED")) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        } else if (serviceResult.getStatus() == SchedulingStatus.INVALID_INPUT) {
            return ResponseEntity.badRequest().body(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Operation(summary = "Retrieve persisted inspection schedule", description = """
            Fetches the currently persisted schedule for an individual inspection.
            Reads persisted database state only; never triggers the CP-SAT solver.
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Schedule retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid service authentication key"),
            @ApiResponse(responseCode = "404", description = "Inspection not found or not yet scheduled")
    })
    @GetMapping("/{inspectionId}")
    @Transactional(readOnly = true)
    public ResponseEntity<ScheduledInspectionApiDto> getSchedule(
            @Parameter(description = "ID of the inspection to retrieve", required = true)
            @PathVariable Long inspectionId) {
        EvcInspection inspection = inspectionRepository.findById(inspectionId)
                .orElseThrow(() -> new NoSuchElementException("Inspection with ID " + inspectionId + " not found"));

        if (inspection.getStatus() != InspectionStatus.SCHEDULED || inspection.getScheduledStartDate() == null) {
            throw new NoSuchElementException(String.format(
                    "Inspection %d has not been scheduled yet (current status: %s)",
                    inspectionId, inspection.getStatus()));
        }

        List<CommitteeAssignment> assignments = assignmentRepository.findByInspection(inspection);
        List<AssignedExpertApiDto> assignedExperts = assignments.stream()
                .map(a -> new AssignedExpertApiDto(
                        a.getExpert().getId(),
                        a.getExpert().getName(),
                        a.getExpert().getDiscipline(),
                        a.getRole(),
                        a.getStatus()
                ))
                .toList();

        List<LocalDate> actualDays = EvcInspection.calculateInspectionDays(
                inspection.getScheduledStartDate(), inspection.getDurationInDays());

        String instName = inspection.getInstitution() != null ? inspection.getInstitution().getName() : "Unknown";
        Long instId = inspection.getInstitution() != null ? inspection.getInstitution().getId() : null;

        ScheduledInspectionApiDto dto = new ScheduledInspectionApiDto(
                inspection.getId(),
                instId,
                instName,
                inspection.getScheduledStartDate(),
                inspection.getScheduledEndDate(),
                inspection.getDurationInDays(),
                inspection.getRequiredMemberCount(),
                actualDays,
                assignedExperts
        );

        return ResponseEntity.ok(dto);
    }
}
