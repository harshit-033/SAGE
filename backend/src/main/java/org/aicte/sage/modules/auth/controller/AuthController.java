package org.aicte.sage.modules.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.aicte.sage.common.api.ApiResponse;
import org.aicte.sage.common.audit.AuditService;
import org.aicte.sage.common.security.SecurityUtils;
import org.aicte.sage.common.security.UserPrincipal;
import org.aicte.sage.modules.auth.dto.LoginRequest;
import org.aicte.sage.modules.auth.dto.LoginResponse;
import org.aicte.sage.modules.auth.dto.UserProfileDto;
import org.aicte.sage.modules.auth.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Authentication and Identity Verification Endpoints")
public class AuthController {

    private final AuthService authService;
    private final AuditService auditService;

    public AuthController(AuthService authService, AuditService auditService) {
        this.authService = authService;
        this.auditService = auditService;
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate User", description = "Validates credentials and issues a signed JWT with role and tenancy claims")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest request) {
        String clientIp = request.getRemoteAddr();
        LoginResponse response = authService.authenticate(loginRequest, clientIp);
        return ResponseEntity.ok(ApiResponse.ok("Login successful", response));
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Current User Profile", description = "Retrieves the authenticated principal identity, roles, and institutional tenancy bindings")
    public ResponseEntity<ApiResponse<UserProfileDto>> getCurrentUser() {
        UserPrincipal principal = SecurityUtils.getCurrentUserPrincipal()
                .orElseThrow(() -> new IllegalStateException("SecurityContext contains no authenticated principal"));

        List<String> roles = new ArrayList<>();
        List<String> permissions = new ArrayList<>();

        for (GrantedAuthority ga : principal.getAuthorities()) {
            String authority = ga.getAuthority();
            if (authority.startsWith("ROLE_")) {
                roles.add(authority);
            } else {
                permissions.add(authority);
            }
        }

        UserProfileDto profile = new UserProfileDto(
                principal.getId(),
                principal.getUsername(),
                principal.getEmail(),
                principal.getFullName(),
                roles,
                permissions,
                new ArrayList<>(principal.getAuthorizedInstitutionIds())
        );

        return ResponseEntity.ok(ApiResponse.ok(profile));
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Logout User", description = "Invalidates session and logs the user sign-out audit event")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        UserPrincipal principal = SecurityUtils.getCurrentUserPrincipal().orElse(null);
        if (principal != null) {
            auditService.recordEvent(
                    "AUTH_LOGOUT",
                    principal.getUsername(),
                    null,
                    "USER",
                    principal.getId().toString(),
                    "LOGOUT",
                    request.getRemoteAddr(),
                    "User logged out successfully"
            );
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(ApiResponse.ok("Logged out successfully", null));
    }
}
