package org.aicte.sage.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.aicte.sage.modules.auth.service.TokenRevocationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    public static final String INTERNAL_SERVICE_HEADER = "X-Internal-Service-Token";

    private final JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService userDetailsService;
    private final TokenRevocationService tokenRevocationService;
    private final String internalServiceToken;

    public JwtAuthenticationFilter(
            JwtTokenProvider tokenProvider,
            CustomUserDetailsService userDetailsService,
            TokenRevocationService tokenRevocationService,
            @Value("${sage.security.internal.service-token:}") String internalServiceToken) {
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
        this.tokenRevocationService = tokenRevocationService;
        this.internalServiceToken = internalServiceToken;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // 1. Check for Service-to-Service Machine Identity
            String serviceToken = request.getHeader(INTERNAL_SERVICE_HEADER);
            if (StringUtils.hasText(serviceToken) && StringUtils.hasText(internalServiceToken)
                    && serviceToken.equals(internalServiceToken)) {
                UserPrincipal servicePrincipal = new UserPrincipal(
                        UUID.fromString("00000000-0000-0000-0000-000000000099"),
                        "SERVICE_AUTOMATION_ENGINE",
                        "automation@internal.sage.aicte",
                        "",
                        "SAGE Automation Engine",
                        true,
                        List.of(new SimpleGrantedAuthority("ROLE_AUTOMATION_SERVICE")),
                        Collections.emptySet()
                );
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(servicePrincipal, null, servicePrincipal.getAuthorities());
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
                filterChain.doFilter(request, response);
                return;
            }

            // 2. Check for Human User Bearer Token
            String jwt = extractJwtFromRequest(request);
            if (StringUtils.hasText(jwt)) {
                if (tokenProvider.validateToken(jwt)) {
                    // Check if token has been revoked on server side
                    String jti = tokenProvider.getJtiFromToken(jwt);
                    if (jti != null && tokenRevocationService.isTokenRevoked(jti)) {
                        log.warn("Access attempt with revoked JWT [jti={}]", jti);
                        SecurityContextHolder.clearContext();
                        filterChain.doFilter(request, response);
                        return;
                    }

                    String username = tokenProvider.getUsernameFromToken(jwt);
                    if (StringUtils.hasText(username)) {
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                        // Strictly reject deactivated or locked users
                        if (!userDetails.isEnabled() || !userDetails.isAccountNonLocked()) {
                            log.warn("Access rejected for deactivated/locked user: {}", username);
                            SecurityContextHolder.clearContext();
                            filterChain.doFilter(request, response);
                            return;
                        }

                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                } else {
                    // Token invalid or expired: clear security context
                    SecurityContextHolder.clearContext();
                }
            }
        } catch (DataAccessException dae) {
            // Operational error: do not swallow as simple authentication failure
            log.error("Database operational error during user authentication lookup: {}", dae.getMessage());
            SecurityContextHolder.clearContext();
            throw dae;
        } catch (Exception ex) {
            log.warn("Authentication failed, clearing security context: {}", ex.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7).trim();
        }
        return null;
    }
}
