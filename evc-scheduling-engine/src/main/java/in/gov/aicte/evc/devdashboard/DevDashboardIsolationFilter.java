package in.gov.aicte.evc.devdashboard;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Development-only isolation filter.
 * Ensures that neither dashboard APIs nor static assets are reachable
 * unless the 'dev-dashboard' Spring profile is explicitly active.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DevDashboardIsolationFilter implements Filter {

    private final boolean isDevDashboardActive;

    public DevDashboardIsolationFilter(Environment environment) {
        this.isDevDashboardActive = environment.acceptsProfiles(Profiles.of("dev-dashboard"));
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String path = httpRequest.getRequestURI();

        if (path.startsWith("/dev-dashboard") || path.startsWith("/dev/dashboard")) {
            if (!isDevDashboardActive) {
                ((HttpServletResponse) response).sendError(HttpServletResponse.SC_NOT_FOUND, "Resource not found");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}

