package org.aicte.sage.modules.health;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/health")
@Tag(name = "System Health", description = "Liveness and Readiness Probes")
public class HealthController {

    private final DataSource dataSource;

    public HealthController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping
    @Operation(summary = "Liveness Probe", description = "Verifies that the Spring Boot process is responsive")
    public ResponseEntity<Map<String, Object>> liveness() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "sage-backend");
        response.put("version", "1.0.0-SNAPSHOT");
        response.put("timestamp", Instant.now().toString());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/ready")
    @Operation(summary = "Readiness Probe", description = "Verifies that the database connection is operational")
    public ResponseEntity<Map<String, Object>> readiness() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "sage-backend");
        response.put("timestamp", Instant.now().toString());

        try (Connection conn = dataSource.getConnection()) {
            boolean isValid = conn.isValid(2);
            if (isValid) {
                response.put("status", "UP");
                response.put("database", Map.of("status", "UP", "details", "Database connection established"));
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "DOWN");
                response.put("database", Map.of("status", "DOWN", "details", "Connection invalid"));
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
            }
        } catch (Exception ex) {
            response.put("status", "DOWN");
            response.put("database", Map.of("status", "DOWN", "error", ex.getMessage()));
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
        }
    }
}
