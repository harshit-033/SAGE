package org.aicte.sage.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.aicte.sage.common.api.CorrelationIdFilter;
import org.aicte.sage.common.api.ProblemDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(TenantIsolationException.class)
    public ResponseEntity<ProblemDetails> handleTenantIsolation(TenantIsolationException ex, HttpServletRequest request) {
        String correlationId = CorrelationIdFilter.getCurrentCorrelationId();
        log.warn("Cross-tenant isolation violation [correlationId={}]: {}", correlationId, ex.getMessage());

        ProblemDetails details = new ProblemDetails(
                URI.create("https://api.aicte.gov.in/errors/tenant-access-denied"),
                "Tenant Access Denied",
                HttpStatus.FORBIDDEN.value(),
                ex.getMessage(),
                request.getRequestURI(),
                correlationId
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(details);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetails> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        String correlationId = CorrelationIdFilter.getCurrentCorrelationId();
        log.warn("Access denied [correlationId={}]: {}", correlationId, ex.getMessage());

        ProblemDetails details = new ProblemDetails(
                URI.create("https://api.aicte.gov.in/errors/forbidden"),
                "Access Forbidden",
                HttpStatus.FORBIDDEN.value(),
                "You do not possess the required regulatory role or permission for this operation.",
                request.getRequestURI(),
                correlationId
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(details);
    }

    @ExceptionHandler({AuthenticationException.class, BadCredentialsException.class, InvalidCredentialsException.class})
    public ResponseEntity<ProblemDetails> handleAuthenticationException(Exception ex, HttpServletRequest request) {
        String correlationId = CorrelationIdFilter.getCurrentCorrelationId();
        log.warn("Authentication failure [correlationId={}]: {}", correlationId, ex.getMessage());

        ProblemDetails details = new ProblemDetails(
                URI.create("https://api.aicte.gov.in/errors/unauthorized"),
                "Authentication Failed",
                HttpStatus.UNAUTHORIZED.value(),
                "Invalid credentials or expired session.",
                request.getRequestURI(),
                correlationId
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(details);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ProblemDetails> handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        String correlationId = CorrelationIdFilter.getCurrentCorrelationId();
        log.info("Resource not found [correlationId={}]: {}", correlationId, ex.getMessage());

        ProblemDetails details = new ProblemDetails(
                URI.create("https://api.aicte.gov.in/errors/not-found"),
                "Resource Not Found",
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                request.getRequestURI(),
                correlationId
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(details);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetails> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String correlationId = CorrelationIdFilter.getCurrentCorrelationId();
        List<ProblemDetails.FieldErrorDetail> fieldErrors = new ArrayList<>();

        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.add(new ProblemDetails.FieldErrorDetail(
                    fe.getField(),
                    fe.getRejectedValue(),
                    fe.getDefaultMessage()
            ));
        }

        ProblemDetails details = new ProblemDetails(
                URI.create("https://api.aicte.gov.in/errors/validation-failed"),
                "Request Validation Error",
                HttpStatus.BAD_REQUEST.value(),
                "One or more request parameters failed validation.",
                request.getRequestURI(),
                correlationId
        );
        details.setFieldErrors(fieldErrors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(details);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetails> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        String correlationId = CorrelationIdFilter.getCurrentCorrelationId();
        ProblemDetails details = new ProblemDetails(
                URI.create("https://api.aicte.gov.in/errors/bad-request"),
                "Bad Request",
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                request.getRequestURI(),
                correlationId
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(details);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetails> handleGeneralException(Exception ex, HttpServletRequest request) {
        String correlationId = CorrelationIdFilter.getCurrentCorrelationId();
        log.error("Unhandled internal server exception [correlationId={}]", correlationId, ex);

        // Safe failure: never expose stack traces or system internals to the caller
        ProblemDetails details = new ProblemDetails(
                URI.create("https://api.aicte.gov.in/errors/internal-server-error"),
                "Internal Server Error",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected regulatory processing error occurred. Reference correlation ID for support.",
                request.getRequestURI(),
                correlationId
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(details);
    }
}
