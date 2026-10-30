package in.gov.aicte.evc.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;
import java.time.Instant;

/**
 * Returns structured JSON 401 Unauthorized response when an unauthenticated request attempts
 * to access a protected EVC Scheduling Engine endpoint.
 */
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        String json = String.format("""
                {
                  "timestamp": "%s",
                  "status": 401,
                  "error": "Unauthorized",
                  "message": "Authentication required. Please provide a valid API key via 'X-API-KEY' header or 'Authorization: Bearer <token>' header.",
                  "path": "%s"
                }
                """, Instant.now(), request.getRequestURI());
        response.getWriter().write(json);
    }
}

