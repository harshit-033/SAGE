package org.aicte.sage.modules.auth.service;

import org.aicte.sage.common.audit.AuditService;
import org.aicte.sage.common.exception.InvalidCredentialsException;
import org.aicte.sage.common.security.JwtTokenProvider;
import org.aicte.sage.common.security.UserPrincipal;
import org.aicte.sage.modules.auth.dto.LoginRequest;
import org.aicte.sage.modules.auth.dto.LoginResponse;
import org.aicte.sage.modules.auth.dto.UserProfileDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final AuditService auditService;
    private final long expirationMs;

    public AuthService(
            AuthenticationManager authenticationManager,
            JwtTokenProvider tokenProvider,
            AuditService auditService,
            @Value("${sage.security.jwt.expiration-ms:86400000}") long expirationMs) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.auditService = auditService;
        this.expirationMs = expirationMs;
    }

    public LoginResponse authenticate(LoginRequest request, String clientIp) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
            String token = tokenProvider.generateToken(principal);

            List<String> roles = principal.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            UserProfileDto profile = new UserProfileDto(
                    principal.getId(),
                    principal.getUsername(),
                    principal.getEmail(),
                    principal.getFullName(),
                    roles,
                    new ArrayList<>(principal.getAuthorizedInstitutionIds())
            );

            auditService.recordEvent(
                    "AUTH_LOGIN_SUCCESS",
                    principal.getUsername(),
                    principal.getAuthorizedInstitutionIds().stream().findFirst().orElse(null),
                    "USER",
                    principal.getId().toString(),
                    "LOGIN",
                    clientIp,
                    "Authentication successful"
            );

            return new LoginResponse(token, expirationMs, profile);
        } catch (BadCredentialsException ex) {
            auditService.recordEvent(
                    "AUTH_LOGIN_FAILURE",
                    request.getUsername(),
                    null,
                    "USER",
                    request.getUsername(),
                    "LOGIN_FAILED",
                    clientIp,
                    "Bad credentials provided"
            );
            throw new InvalidCredentialsException("Invalid username or password");
        }
    }
}
