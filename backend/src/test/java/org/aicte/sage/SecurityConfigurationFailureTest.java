package org.aicte.sage;

import org.aicte.sage.common.security.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SecurityConfigurationFailureTest {

    @Test
    @DisplayName("Should reject startup when JWT secret is null or blank")
    void testBlankJwtSecretRejected() {
        assertThrows(IllegalStateException.class, () -> new JwtTokenProvider("", 3600000));
        assertThrows(IllegalStateException.class, () -> new JwtTokenProvider(null, 3600000));
        assertThrows(IllegalStateException.class, () -> new JwtTokenProvider("   ", 3600000));
    }

    @Test
    @DisplayName("Should reject startup when JWT secret is weak without padding or stretching")
    void testWeakJwtSecretRejectedWithoutPadding() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new JwtTokenProvider("weak_secret_under_32_bytes", 3600000)
        );
        assertTrue(ex.getMessage().contains("must be at least 32 characters"));
    }

    @Test
    @DisplayName("Should initialize successfully when JWT secret is 32 or more bytes")
    void testStrongJwtSecretAccepted() {
        JwtTokenProvider provider = new JwtTokenProvider(
                "cryptographically_strong_signing_key_32_chars_minimum",
                3600000
        );
        assertNotNull(provider);
    }

    @Test
    @DisplayName("Should reject DocumentOcrClient startup in production when internal token is missing or blank")
    void testDocumentOcrClientRejectsBlankTokenInProduction() {
        assertThrows(IllegalStateException.class, () ->
                new org.aicte.sage.modules.documents.service.DocumentOcrClient("http://localhost:8000", "", "prod")
        );
        assertThrows(IllegalStateException.class, () ->
                new org.aicte.sage.modules.documents.service.DocumentOcrClient("http://localhost:8000", null, "prod")
        );
    }

    @Test
    @DisplayName("Should reject DocumentOcrClient startup in production when internal token uses development defaults")
    void testDocumentOcrClientRejectsDevDefaultInProduction() {
        assertThrows(IllegalStateException.class, () ->
                new org.aicte.sage.modules.documents.service.DocumentOcrClient(
                        "http://localhost:8000",
                        "sage_internal_dev_service_secret_token_12345",
                        "production"
                )
        );
    }

    @Test
    @DisplayName("Should reject DocumentOcrClient startup in production when internal token is under 32 characters")
    void testDocumentOcrClientRejectsWeakTokenInProduction() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                new org.aicte.sage.modules.documents.service.DocumentOcrClient(
                        "http://localhost:8000",
                        "weak_token_under_32",
                        "prod"
                )
        );
        assertTrue(ex.getMessage().contains("must be at least 32 characters"));
    }

    @Test
    @DisplayName("Should accept strong 32+ character internal token in production")
    void testDocumentOcrClientAcceptsStrongTokenInProduction() {
        var client = new org.aicte.sage.modules.documents.service.DocumentOcrClient(
                "http://localhost:8000",
                "production_strong_secret_key_minimum_32_chars_verified",
                "prod"
        );
        assertNotNull(client);
    }

    @Test
    @DisplayName("Should permit development default token in dev/test profiles")
    void testDocumentOcrClientAllowsDevDefaultsInDevMode() {
        var client = new org.aicte.sage.modules.documents.service.DocumentOcrClient(
                "http://localhost:8000",
                "sage_internal_dev_service_secret_token_12345",
                "dev"
        );
        assertNotNull(client);
    }
}
