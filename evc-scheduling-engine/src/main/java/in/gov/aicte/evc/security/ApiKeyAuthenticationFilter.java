package in.gov.aicte.evc.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

/**
 * Service-to-service authentication filter for SAGE backend client calls.
 * Evaluates 'X-API-KEY' or 'Authorization: Bearer <key>' against the configured application secret.
 */
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    public static final String HEADER_API_KEY = "X-API-KEY";
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String BEARER_PREFIX = "bearer ";

    private final String expectedApiKey;

    public ApiKeyAuthenticationFilter(String expectedApiKey) {
        this.expectedApiKey = expectedApiKey;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String clientKey = extractApiKey(request);

        if (clientKey != null) {
            if (expectedApiKey != null && expectedApiKey.equals(clientKey)) {
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        "SAGE_BACKEND",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_SAGE_CLIENT"), new SimpleGrantedAuthority("ROLE_SERVICE"))
                );
                SecurityContextHolder.getContext().setAuthentication(auth);
            } else {
                SecurityContextHolder.clearContext();
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                String json = String.format("""
                        {
                          "timestamp": "%s",
                          "status": 403,
                          "error": "Forbidden",
                          "message": "Invalid API key provided for EVC Scheduling Engine",
                          "path": "%s"
                        }
                        """, Instant.now(), request.getRequestURI());
                response.getWriter().write(json);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractApiKey(HttpServletRequest request) {
        String apiKeyHeader = request.getHeader(HEADER_API_KEY);
        if (apiKeyHeader != null && !apiKeyHeader.isBlank()) {
            return apiKeyHeader.trim();
        }

        String authHeader = request.getHeader(HEADER_AUTHORIZATION);
        if (authHeader != null && authHeader.toLowerCase().startsWith(BEARER_PREFIX)) {
            return authHeader.substring(BEARER_PREFIX.length()).trim();
        }

        return null;
    }
}

