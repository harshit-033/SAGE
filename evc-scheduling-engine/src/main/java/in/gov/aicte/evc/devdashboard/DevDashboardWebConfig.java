package in.gov.aicte.evc.devdashboard;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web configuration active exclusively under the 'dev-dashboard' Spring profile.
 * Registers dashboard resource paths and forwards.
 */
@Profile("dev-dashboard")
@Configuration
public class DevDashboardWebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/dev-dashboard/**")
                .addResourceLocations("classpath:/static/dev-dashboard/");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/dev-dashboard").setViewName("forward:/dev-dashboard/index.html");
        registry.addViewController("/dev-dashboard/").setViewName("forward:/dev-dashboard/index.html");
    }
}

