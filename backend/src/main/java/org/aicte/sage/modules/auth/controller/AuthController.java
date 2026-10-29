package org.aicte.sage.modules.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.aicte.sage.common.api.ApiResponse;
import org.aicte.sage.common.audit.AuditService;
import org.aicte.sage.common.security.JwtTokenProvider;
import org.aicte.sage.common.security.SecurityUtils;
import org.aicte.sage.common.security.UserPrincipal;
import org.aicte.sage.modules.auth.dto.LoginRequest;
import org.aicte.sage.modules.auth.dto.LoginResponse;
import org.aicte.sage.modules.auth.dto.UserProfileDto;
import org.aicte.sage.modules.auth.service.AuthService;
import org.aicte.sage.modules.auth.service.TokenRevocationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Authentication and Identity Verification Endpoints")
public class AuthController {

    private final AuthService authService;
    private final AuditService auditService;
    private final JwtTokenProvider tokenProvider;
    private final TokenRevocationService tokenRevocationService;

    public AuthController(AuthService authService,
                          AuditService auditService,
                          JwtTokenProvider tokenProvider,
                          TokenRevocationService tokenRevocationService) {
        this.authService = authService;
        this.auditService = auditService;
        this.tokenProvider = tokenProvider;
        this.tokenRevocationService = tokenRevocationService;
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
    @Operation(summary = "Logout User", description = "Invalidates session, registers server-side token revocation, and logs the sign-out event")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        UserPrincipal principal = SecurityUtils.getCurrentUserPrincipal().orElse(null);

        // Server-side token revocation
        String bearerToken = extractBearerToken(request);
        if (StringUtils.hasText(bearerToken)) {
            try {
                String jti = tokenProvider.getJtiFromToken(bearerToken);
                Date expiration = tokenProvider.getExpirationDateFromToken(bearerToken);
                tokenRevocationService.revokeToken(
                        jti,
                        principal != null ? principal.getId() : UUID.fromString("00000000-0000-0000-0000-000000000000"),
                        expiration != null ? expiration.toInstant() : Instant.now().plusSeconds(86400),
                        "User explicit server-side logout"
                );
            } catch (Exception e) {
                // If token extraction fails, proceed with context clearing
            }
        }

        if (principal != null) {
            auditService.recordEvent(
                    "AUTH_LOGOUT",
                    principal.getUsername(),
                    null,
                    "USER",
                    principal.getId().toString(),
                    "LOGOUT",
                    request.getRemoteAddr(),
                    "User logged out with server-side token revocation"
            );
        }

        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(ApiResponse.ok("Logged out successfully", null));
    }

    private String extractBearerToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7).trim();
        }
        return null;
    }
}
