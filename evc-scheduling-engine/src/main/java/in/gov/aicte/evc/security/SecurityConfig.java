package in.gov.aicte.evc.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security configuration establishing service-to-service authentication
 * between the main SAGE platform and the standalone EVC Scheduling Engine.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final String expectedApiKey;

    public SecurityConfig(@Value("${evc.security.api-key:evc-dev-secret-key-2026}") String expectedApiKey) {
        this.expectedApiKey = expectedApiKey;
    }

    @Bean
    public ApiKeyAuthenticationFilter apiKeyAuthenticationFilter() {
        return new ApiKeyAuthenticationFilter(expectedApiKey);
    }

    @Bean
    public RestAuthenticationEntryPoint restAuthenticationEntryPoint() {
        return new RestAuthenticationEntryPoint();
    }

    @Bean
    public RestAccessDeniedHandler restAccessDeniedHandler() {
        return new RestAccessDeniedHandler();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(restAuthenticationEntryPoint())
                        .accessDeniedHandler(restAccessDeniedHandler())
                )
                .authorizeHttpRequests(auth -> auth
                        // Public operational health probe endpoint
                        .requestMatchers("/api/v1/health").permitAll()
                        // OpenAPI / Swagger documentation endpoints
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        // Development dashboard routes (active only when 'dev-dashboard' profile is active)
                        .requestMatchers("/dev-dashboard/**", "/dev/dashboard/api/**").permitAll()
                        // Production scheduling API endpoints require authenticated SAGE service client
                        .requestMatchers("/api/v1/schedules/**").hasAnyRole("SAGE_CLIENT", "SERVICE")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(apiKeyAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

