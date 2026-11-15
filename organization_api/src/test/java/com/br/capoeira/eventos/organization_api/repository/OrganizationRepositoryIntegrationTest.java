package com.br.capoeira.eventos.organization_api.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.dao.DataIntegrityViolationException;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static utils.MockUtils.getMockOrganization;

@DataJpaTest
@Testcontainers
public class OrganizationRepositoryIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("organization_test")
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
    private OrganizationRepository organizationRepository;

    @Test
    void shouldSaveOrganization() {
        var organization = getMockOrganization();
        organization.setId(null);

        var saved = organizationRepository.save(organization);

        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Grupo Bonfim");
    }

    @Test
    void shouldFindOrganizationById() {
        var organization = getMockOrganization();
        organization.setId(null);

        var saved = organizationRepository.save(organization);

        var result = organizationRepository.findById(saved.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(saved.getId());
        assertThat(result.get().getName()).isEqualTo(saved.getName());
    }

    @Test
    void shouldKeepOrganizationKeyUniqueWhenExistingOrganizationIsInactive() {
        var inactiveOrganization = getMockOrganization();
        inactiveOrganization.setId(null);
        inactiveOrganization.setActive(false);
        inactiveOrganization.assignOrganizationKey("reserved-key");
        organizationRepository.saveAndFlush(inactiveOrganization);

        assertThat(organizationRepository.existsByOrganizationKey("reserved-key")).isTrue();

        var duplicate = getMockOrganization();
        duplicate.setId(null);
        duplicate.setName("Equivalent name");
        duplicate.assignOrganizationKey("reserved-key");

        assertThatThrownBy(() -> organizationRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("uk_organization_organization_key");
    }
}
