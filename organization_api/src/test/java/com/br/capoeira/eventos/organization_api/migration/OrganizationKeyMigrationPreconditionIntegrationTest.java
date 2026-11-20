package com.br.capoeira.eventos.organization_api.migration;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.DriverManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers
class OrganizationKeyMigrationPreconditionIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("organization_v2_precondition_test")
            .withUsername("test")
            .withPassword("test");

    @Test
    void shouldRefuseV2AndPreserveExistingOrganization() throws Exception {
        var v1Only = Flyway.configure()
                .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
                .locations("classpath:db/migration")
                .target("1")
                .load();
        v1Only.migrate();

        try (var connection = DriverManager.getConnection(
                postgres.getJdbcUrl(),
                postgres.getUsername(),
                postgres.getPassword()
        ); var statement = connection.prepareStatement(
                """
                INSERT INTO organization (name, description, logo_url, active, created_at, updated_at)
                VALUES ('Legacy organization', 'must be preserved', 'https://example.test/logo.png',
                        TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """
        )) {
            statement.executeUpdate();
        }

        var allMigrations = Flyway.configure()
                .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
                .locations("classpath:db/migration")
                .load();

        assertThatThrownBy(allMigrations::migrate)
                .hasStackTraceContaining("V2 requires an empty organization table");

        try (var connection = DriverManager.getConnection(
                postgres.getJdbcUrl(),
                postgres.getUsername(),
                postgres.getPassword()
        ); var statement = connection.createStatement()) {
            try (var result = statement.executeQuery("SELECT count(*) FROM organization")) {
                result.next();
                assertThat(result.getLong(1)).isEqualTo(1);
            }
            try (var result = statement.executeQuery(
                    "SELECT count(*) FROM flyway_schema_history WHERE version = '2' AND success = TRUE"
            )) {
                result.next();
                assertThat(result.getLong(1)).isZero();
            }
        }
    }
}
