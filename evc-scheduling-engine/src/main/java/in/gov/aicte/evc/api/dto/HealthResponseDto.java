package in.gov.aicte.evc.api.dto;

import java.time.Instant;

/**
 * Health check response payload for infrastructure and operational monitoring.
 */
public record HealthResponseDto(
        String status,
        String service,
        String version,
        Instant timestamp
) {
}

