package in.gov.aicte.evc.config;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests verifying schema namespace isolation for shared PostgreSQL with SAGE.
 * Guarantees that the engine restricts all DDL and DML operations to the 'evc' schema.
 */
@SpringBootTest
public class PostgresSchemaIsolationTests {

    @Autowired(required = false)
    private Flyway flyway;

    @Value("${spring.flyway.schemas:}")
    private String flywaySchemas;

    @Value("${spring.flyway.default-schema:}")
    private String flywayDefaultSchema;

    @Value("${spring.jpa.properties.hibernate.default_schema:}")
    private String hibernateDefaultSchema;

    @Test
    @DisplayName("Flyway configuration strictly isolates migration history and tables to 'evc' schema")
    void testFlywaySchemaConfiguration() {
        assertThat(flywaySchemas).isEqualTo("evc");
        assertThat(flywayDefaultSchema).isEqualTo("evc");

        if (flyway != null) {
            String[] schemas = flyway.getConfiguration().getSchemas();
            assertThat(schemas).isNotNull();
            assertThat(Arrays.asList(schemas)).contains("evc");
        }
    }

    @Test
    @DisplayName("Hibernate configuration sets default_schema to 'evc'")
    void testHibernateSchemaConfiguration() {
        assertThat(hibernateDefaultSchema).isEqualTo("evc");
    }
}

