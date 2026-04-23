package com.br.capoeira.eventos.organization_api.repository;

import com.br.capoeira.eventos.organization_api.model.OrganizationUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static utils.MockUtils.getMockOrganization;
import static utils.MockUtils.getMockOrganizationUnit;
@DataJpaTest
@Testcontainers
public class OrganizationUnitRepositoryIntegrationTest {
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

    @Autowired
    private OrganizationUnitRepository organizationUnitRepository;

    @Test
    void shouldSaveOrganizationUnit() {
        var organization = getMockOrganization();
        organization.setId(null);
        var savedOrganization = organizationRepository.save(organization);

        var unit = getMockOrganizationUnit();
        unit.setId(null);
        unit.setOrganization(savedOrganization);

        var saved = organizationUnitRepository.save(unit);

        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Bonfim Jundiaí");
        assertThat(saved.getOrganization().getId()).isEqualTo(savedOrganization.getId());
    }

    @Test
    void shouldFindOrganizationUnitById() {
        var organization = getMockOrganization();
        organization.setId(null);
        var savedOrganization = organizationRepository.save(organization);

        var unit = getMockOrganizationUnit();
        unit.setId(null);
        unit.setOrganization(savedOrganization);
        var savedUnit = organizationUnitRepository.save(unit);

        var result = organizationUnitRepository.findById(savedUnit.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(savedUnit.getId());
        assertThat(result.get().getName()).isEqualTo(savedUnit.getName());
    }

    @Test
    void shouldFindAllByOrganizationId() {
        var organization = getMockOrganization();
        organization.setId(null);
        var savedOrganization = organizationRepository.save(organization);

        var unit1 = getMockOrganizationUnit();
        unit1.setId(null);
        unit1.setName("Bonfim Jundiaí");
        unit1.setSlug("bonfim-jundiai");
        unit1.setOrganization(savedOrganization);

        var unit2 = getMockOrganizationUnit();
        unit2.setId(null);
        unit2.setName("Bonfim Passos");
        unit2.setSlug("bonfim-passos");
        unit2.setOrganization(savedOrganization);

        organizationUnitRepository.save(unit1);
        organizationUnitRepository.save(unit2);

        var result = organizationUnitRepository.findAllByOrganization_IdOrderByIdAsc(
                savedOrganization.getId(),
                PageRequest.of(0, 10)
        );

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .extracting(OrganizationUnit::getName)
                .containsExactly("Bonfim Jundiaí", "Bonfim Passos");
        assertThat(result.getNumber()).isZero();
        assertThat(result.getSize()).isEqualTo(10);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.isLast()).isTrue();
    }

    @Test
    void shouldReturnTrueWhenExistsByOrganizationIdAndSlug() {
        var organization = getMockOrganization();
        organization.setId(null);
        var savedOrganization = organizationRepository.save(organization);

        var unit = getMockOrganizationUnit();
        unit.setId(null);
        unit.setOrganization(savedOrganization);
        unit.setSlug("bonfim-jundiai");

        organizationUnitRepository.save(unit);

        var exists = organizationUnitRepository.existsByOrganization_IdAndSlug(
                savedOrganization.getId(),
                "bonfim-jundiai"
        );

        assertThat(exists).isTrue();
    }

    @Test
    void shouldFindByOrganizationIdAndSlug() {
        var organization = getMockOrganization();
        organization.setId(null);
        var savedOrganization = organizationRepository.save(organization);

        var unit = getMockOrganizationUnit();
        unit.setId(null);
        unit.setOrganization(savedOrganization);
        unit.setSlug("bonfim-jundiai");

        organizationUnitRepository.save(unit);

        var result = organizationUnitRepository.findByOrganization_IdAndSlug(
                savedOrganization.getId(),
                "bonfim-jundiai"
        );

        assertThat(result).isPresent();
        assertThat(result.get().getSlug()).isEqualTo("bonfim-jundiai");
        assertThat(result.get().getOrganization().getId()).isEqualTo(savedOrganization.getId());
    }

    @Test
    void shouldReturnFalseWhenExistsByOrganizationIdAndSlugDoesNotExist() {
        var exists = organizationUnitRepository.existsByOrganization_IdAndSlug(1L, "slug-inexistente");

        assertThat(exists).isFalse();
    }
}
