package in.gov.aicte.evc.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 3 specification configuration for the EVC Scheduling Engine REST API.
 */
@Configuration
public class OpenApiConfig {

    public static final String API_KEY_SCHEME = "ApiKeyAuth";
    public static final String BEARER_SCHEME = "BearerAuth";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AICTE EVC Scheduling Engine REST API")
                        .version("1.0.0")
                        .description("""
                                Standalone mathematical scheduling engine for AICTE's SAGE platform.
                                Solves optimal inspection committee assignments and scheduling windows using Google OR-Tools CP-SAT.
                                
                                ### Key Scheduling Rules:
                                - **Dynamic Durations & Committee Sizes:** SAGE configures required days and committee sizes per inspection.
                                - **Sunday Exclusion:** Sundays are strictly excluded from inspection activities. Saturday is an active working day.
                                - **Inclusive End Dates:** Derived strictly from the actual non-Sunday inspection calendar.
                                - **Expert Availability:** Enforced on every scheduled non-Sunday inspection day. No availability required on Sunday.
                                - **Overlap Prevention:** Shared committee members cannot be assigned to overlapping inspections on any actual inspection day.
                                """)
                        .contact(new Contact()
                                .name("AICTE SAGE Platform Engineering")
                                .url("https://aicte.gov.in"))
                        .license(new License().name("Proprietary AICTE / SAGE License")))
                .addSecurityItem(new SecurityRequirement().addList(API_KEY_SCHEME).addList(BEARER_SCHEME))
                .components(new Components()
                        .addSecuritySchemes(API_KEY_SCHEME, new SecurityScheme()
                                .name("X-API-KEY")
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .description("Service-to-service API key header for SAGE backend client calls"))
                        .addSecuritySchemes(BEARER_SCHEME, new SecurityScheme()
                                .name("Authorization")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("API-KEY")
                                .description("Bearer token alternative for SAGE client authorization")));
    }
}

