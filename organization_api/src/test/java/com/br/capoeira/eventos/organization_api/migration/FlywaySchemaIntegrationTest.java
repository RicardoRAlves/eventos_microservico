package com.br.capoeira.eventos.organization_api.migration;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Testcontainers
class FlywaySchemaIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("organization_migration_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.flyway.enabled", () -> "true");
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private Flyway flyway;

    @Test
    void shouldCreateCompleteSchemaAndApplyMigrationsInOrder() {
        List<String> tables = jdbcTemplate.queryForList(
                """
                SELECT table_name
                  FROM information_schema.tables
                 WHERE table_schema = 'public'
                   AND table_type = 'BASE TABLE'
                 ORDER BY table_name
                """,
                String.class
        );
        assertThat(tables).containsExactly("flyway_schema_history", "organization", "organization_unit");

        List<String> constraints = jdbcTemplate.queryForList(
                """
                SELECT conname
                  FROM pg_constraint
                 WHERE conrelid IN ('organization'::regclass, 'organization_unit'::regclass)
                 ORDER BY conname
                """,
                String.class
        );
        assertThat(constraints).containsExactlyInAnyOrder(
                "pk_organization",
                "pk_organization_unit",
                "uk_organization_organization_key",
                "uk_organization_unit_organization_name",
                "uk_organization_unit_join_code",
                "fk_organization_unit_organization"
        );

        Integer organizationKeyLength = jdbcTemplate.queryForObject(
                """
                SELECT character_maximum_length
                  FROM information_schema.columns
                 WHERE table_schema = 'public'
                   AND table_name = 'organization'
                   AND column_name = 'organization_key'
                   AND is_nullable = 'NO'
                """,
                Integer.class
        );
        assertThat(organizationKeyLength).isEqualTo(120);

        List<Integer> descriptionLengths = jdbcTemplate.queryForList(
                """
                SELECT character_maximum_length
                  FROM information_schema.columns
                 WHERE table_schema = 'public'
                   AND column_name = 'description'
                   AND table_name IN ('organization', 'organization_unit')
                 ORDER BY table_name
                """,
                Integer.class
        );
        assertThat(descriptionLengths).containsExactly(1000, 1000);

        List<String> indexes = jdbcTemplate.queryForList(
                """
                SELECT indexname
                  FROM pg_indexes
                 WHERE schemaname = 'public'
                   AND tablename = 'organization_unit'
                 ORDER BY indexname
                """,
                String.class
        );
        assertThat(indexes).contains(
                "pk_organization_unit",
                "uk_organization_unit_organization_name",
                "uk_organization_unit_join_code",
                "idx_organization_unit_organization_id"
        );

        List<String> appliedVersions = jdbcTemplate.queryForList(
                """
                SELECT version
                  FROM flyway_schema_history
                 WHERE version IS NOT NULL
                   AND success = TRUE
                 ORDER BY installed_rank
                """,
                String.class
        );
        assertThat(appliedVersions).containsExactly("1", "2", "3");
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void shouldPreserveDataWhenFlywayRunsAgain() {
        Long organizationId = insertOrganization("Restart preservation marker");

        try {
            var result = flyway.migrate();

            assertThat(result.migrationsExecuted).isZero();
            assertThat(jdbcTemplate.queryForObject(
                    "SELECT name FROM organization WHERE id = ?",
                    String.class,
                    organizationId
            )).isEqualTo("Restart preservation marker");
        } finally {
            jdbcTemplate.update("DELETE FROM organization WHERE id = ?", organizationId);
        }
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void shouldEnforceJoinCodeUniqueness() {
        Long organizationId = insertOrganization("Join code constraint marker");

        try {
            insertUnit(organizationId, "First unit", "UNIT01");

            assertThatThrownBy(() -> insertUnit(organizationId, "Second unit", "UNIT01"))
                    .isInstanceOf(DataAccessException.class)
                    .hasMessageContaining("uk_organization_unit_join_code");
        } finally {
            jdbcTemplate.update("DELETE FROM organization_unit WHERE organization_id = ?", organizationId);
            jdbcTemplate.update("DELETE FROM organization WHERE id = ?", organizationId);
        }
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void shouldEnforceUnitNameUniquenessInsideOrganization() {
        Long organizationId = insertOrganization("Unit name constraint marker");

        try {
            insertUnit(organizationId, "Jundiai - Mestre Kaue", "NAME01");

            assertThatThrownBy(() -> insertUnit(
                    organizationId,
                    "Jundiai - Mestre Kaue",
                    "NAME02"
            ))
                    .isInstanceOf(DataAccessException.class)
                    .hasMessageContaining("uk_organization_unit_organization_name");
        } finally {
            jdbcTemplate.update("DELETE FROM organization_unit WHERE organization_id = ?", organizationId);
            jdbcTemplate.update("DELETE FROM organization WHERE id = ?", organizationId);
        }
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void shouldEnforceOrganizationForeignKey() {
        assertThatThrownBy(() -> insertUnit(Long.MAX_VALUE, "Foreign key marker", "FKFAIL"))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("fk_organization_unit_organization");
    }

    private Long insertOrganization(String name) {
        return jdbcTemplate.queryForObject(
                """
                INSERT INTO organization (
                    name, organization_key, description, logo_url, active, created_at, updated_at
                ) VALUES (
                    ?, ?, 'test description', 'https://example.test/logo.png', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
                )
                RETURNING id
                """,
                Long.class,
                name,
                name.toLowerCase().replace(' ', '-')
        );
    }

    private void insertUnit(Long organizationId, String name, String joinCode) {
        jdbcTemplate.update(
                """
                INSERT INTO organization_unit (
                    organization_id, name, description, city, state, country, address,
                    contact_phone, contact_email, active, join_code, created_at, updated_at
                ) VALUES (
                    ?, ?, 'test description', 'Jundiai', 'SP', 'Brasil', 'Test address',
                    '+55 11 99999-9999', 'test@example.test', TRUE, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
                )
                """,
                organizationId,
                name,
                joinCode
        );
    }
}
