package in.gov.aicte.evc;

import in.gov.aicte.evc.util.PortUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.server.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;

import java.util.Collections;

@SpringBootApplication
public class EvcSchedulingEngineApplication implements ApplicationListener<WebServerInitializedEvent> {

    private static final Logger log = LoggerFactory.getLogger(EvcSchedulingEngineApplication.class);

    public static void main(String[] args) {
        int configuredPort = resolveConfiguredPort();
        int selectedPort = configuredPort;

        if (configuredPort > 0) {
            if (!PortUtils.isPortAvailable(configuredPort)) {
                log.warn("Port {} is currently occupied. Initiating automatic port scan...", configuredPort);
                selectedPort = PortUtils.findNextAvailablePort(configuredPort + 1, PortUtils.MAX_PORT_SCAN_RANGE);
                log.warn("Auto-selected next available port {} for EVC Scheduling Engine.", selectedPort);
                System.setProperty("server.port", String.valueOf(selectedPort));
            } else {
                log.info("Port {} is verified available for EVC Scheduling Engine.", configuredPort);
            }
        }

        SpringApplication app = new SpringApplication(EvcSchedulingEngineApplication.class);
        if (selectedPort != configuredPort) {
            app.setDefaultProperties(Collections.singletonMap("server.port", String.valueOf(selectedPort)));
        }
        app.run(args);
    }

    public static int resolveConfiguredPort() {
        String serverPortProp = System.getProperty("server.port");
        if (serverPortProp != null && !serverPortProp.isBlank()) {
            try {
                return Integer.parseInt(serverPortProp.trim());
            } catch (NumberFormatException ignored) {
            }
        }
        String serverPortEnv = System.getenv("SERVER_PORT");
        if (serverPortEnv != null && !serverPortEnv.isBlank()) {
            try {
                return Integer.parseInt(serverPortEnv.trim());
            } catch (NumberFormatException ignored) {
            }
        }
        return PortUtils.DEFAULT_PORT;
    }

    @Override
    public void onApplicationEvent(WebServerInitializedEvent event) {
        int actualPort = event.getWebServer().getPort();
        log.info("================================================================================");
        log.info("  EVC Scheduling Engine successfully bound and listening on HTTP port: {}", actualPort);
        log.info("  Health endpoint: http://localhost:{}/api/v1/health", actualPort);
        log.info("  OpenAPI UI:      http://localhost:{}/swagger-ui.html", actualPort);
        log.info("================================================================================");
    }
}
