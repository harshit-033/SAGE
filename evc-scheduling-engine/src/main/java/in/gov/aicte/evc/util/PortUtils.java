package in.gov.aicte.evc.util;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Utility for network port availability detection and automatic fallback scanning.
 */
public final class PortUtils {

    public static final int DEFAULT_PORT = 8081;
    public static final int MAX_PORT_SCAN_RANGE = 100;

    private PortUtils() {
    }

    /**
     * Checks if a specified TCP port is currently free and can be bound on the local host.
     *
     * @param port the port number to test
     * @return true if available, false if occupied or invalid
     */
    public static boolean isPortAvailable(int port) {
        if (port <= 0 || port > 65535) {
            return false;
        }
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            serverSocket.setReuseAddress(true);
            return true;
        } catch (IOException | SecurityException e) {
            return false;
        }
    }

    /**
     * Scans sequentially starting from {@code startPort} for the next available TCP port.
     *
     * @param startPort the first port number to test
     * @param maxScan the maximum number of consecutive ports to scan
     * @return the first available port number found
     * @throws IllegalStateException if all scanned ports are occupied
     */
    public static int findNextAvailablePort(int startPort, int maxScan) {
        int end = Math.min(startPort + maxScan, 65535);
        for (int p = startPort; p <= end; p++) {
            if (isPortAvailable(p)) {
                return p;
            }
        }
        throw new IllegalStateException(String.format(
                "No available network port found in range [%d, %d]. Please set SERVER_PORT manually.",
                startPort, end));
    }
}

