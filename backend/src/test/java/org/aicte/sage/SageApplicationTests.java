package org.aicte.sage;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class SageApplicationTests {

    @Test
    @DisplayName("Verify Spring Boot context loads and Flyway migrations execute successfully")
    void contextLoads() {
        // Verifies complete dependency injection, JPA entities, and Flyway migration execution
    }
}
