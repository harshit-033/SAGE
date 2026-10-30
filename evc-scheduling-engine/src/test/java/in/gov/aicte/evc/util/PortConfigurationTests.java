package in.gov.aicte.evc.util;

import in.gov.aicte.evc.EvcSchedulingEngineApplication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.ServerSocket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests verifying port detection, availability checking, and dynamic fallback.
 */
public class PortConfigurationTests {

    @Test
    @DisplayName("PortUtils detects available port on open ephemeral socket")
    void testPortIsAvailable() throws IOException {
        int port;
        try (ServerSocket socket = new ServerSocket(0)) {
            port = socket.getLocalPort();
        }

        assertTrue(PortUtils.isPortAvailable(port), "Port should be detected as available once socket is closed");
    }

    @Test
    @DisplayName("PortUtils detects occupied port when ServerSocket is actively listening")
    void testPortIsOccupied() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            int port = socket.getLocalPort();
            assertFalse(PortUtils.isPortAvailable(port), "Port should be detected as unavailable when socket is bound");
        }
    }

    @Test
    @DisplayName("PortUtils.findNextAvailablePort scans and returns next free port when starting port is busy")
    void testFindNextAvailablePort() throws IOException {
        try (ServerSocket socket1 = new ServerSocket(0)) {
            int busyPort = socket1.getLocalPort();

            int nextPort = PortUtils.findNextAvailablePort(busyPort, 10);
            assertThat(nextPort).isGreaterThan(busyPort);
            assertTrue(PortUtils.isPortAvailable(nextPort), "Returned fallback port must be available");
        }
    }

    @Test
    @DisplayName("EvcSchedulingEngineApplication resolves default port 8081 when environment property is not set")
    void testDefaultConfiguredPortResolution() {
        String original = System.getProperty("server.port");
        try {
            System.clearProperty("server.port");
            int resolved = EvcSchedulingEngineApplication.resolveConfiguredPort();
            String envPort = System.getenv("SERVER_PORT");
            if (envPort == null || envPort.isBlank()) {
                assertThat(resolved).isEqualTo(8081);
            } else {
                assertThat(resolved).isEqualTo(Integer.parseInt(envPort));
            }
        } finally {
            if (original != null) {
                System.setProperty("server.port", original);
            }
        }
    }

    @Test
    @DisplayName("EvcSchedulingEngineApplication resolves custom system property server.port")
    void testSystemPropertyPortResolution() {
        String original = System.getProperty("server.port");
        try {
            System.setProperty("server.port", "9090");
            int resolved = EvcSchedulingEngineApplication.resolveConfiguredPort();
            assertThat(resolved).isEqualTo(9090);
        } finally {
            if (original != null) {
                System.setProperty("server.port", original);
            } else {
                System.clearProperty("server.port");
            }
        }
    }
}
