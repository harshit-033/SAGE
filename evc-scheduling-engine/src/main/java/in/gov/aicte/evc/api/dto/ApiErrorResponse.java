package in.gov.aicte.evc.api.dto;

import java.time.Instant;
import java.util.List;

/**
 * Standard error response envelope returned on client validation or server errors.
 */
public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        List<String> details
) {
    public ApiErrorResponse(int status, String error, String message, String path) {
        this(Instant.now(), status, error, message, path, List.of());
    }

    public ApiErrorResponse(int status, String error, String message, String path, List<String> details) {
        this(Instant.now(), status, error, message, path, details != null ? details : List.of());
    }
}

