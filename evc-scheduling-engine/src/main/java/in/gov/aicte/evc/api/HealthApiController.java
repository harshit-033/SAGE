package in.gov.aicte.evc.api;

import in.gov.aicte.evc.api.dto.HealthResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

/**
 * Public health check and operational monitoring endpoint.
 * Does not expose sensitive environment variables, secrets, or internal details.
 */
@Tag(name = "Health", description = "Operational monitoring and service health probe")
@RestController
@RequestMapping("/api/v1/health")
public class HealthApiController {

    @Operation(summary = "Service Health Probe", description = "Returns the operational status of the EVC Scheduling Engine service.")
    @ApiResponse(responseCode = "200", description = "Service is operational and ready to accept requests")
    @GetMapping
    public ResponseEntity<HealthResponseDto> getHealth() {
        return ResponseEntity.ok(new HealthResponseDto(
                "UP",
                "evc-scheduling-engine",
                "0.0.1-SNAPSHOT",
                Instant.now()
        ));
    }
}

