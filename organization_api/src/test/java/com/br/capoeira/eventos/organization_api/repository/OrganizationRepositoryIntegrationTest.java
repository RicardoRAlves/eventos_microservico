package com.br.capoeira.eventos.organization_api.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
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
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
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
        assertThat(saved.getSlug()).isEqualTo("grupo-bonfim");
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
    void shouldFindOrganizationBySlug() {
        var organization = getMockOrganization();
        organization.setId(null);

        organizationRepository.save(organization);

        var result = organizationRepository.findBySlug("grupo-bonfim");

        assertThat(result).isPresent();
        assertThat(result.get().getSlug()).isEqualTo("grupo-bonfim");
    }

    @Test
    void shouldReturnTrueWhenExistsBySlug() {
        var organization = getMockOrganization();
        organization.setId(null);

        organizationRepository.save(organization);

        var exists = organizationRepository.existsBySlug("grupo-bonfim");

        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnFalseWhenSlugDoesNotExist() {
        var exists = organizationRepository.existsBySlug("slug-inexistente");

        assertThat(exists).isFalse();
    }
}
