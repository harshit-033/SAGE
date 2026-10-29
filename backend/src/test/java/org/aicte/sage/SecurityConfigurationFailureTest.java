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
}
