package com.br.capoeira.eventos.organization_api.seed;

import com.br.capoeira.eventos.organization_api.dto.Role;
import com.br.capoeira.eventos.organization_api.dto.UserResponseDto;
import com.br.capoeira.eventos.organization_api.repository.OrganizationRepository;
import com.br.capoeira.eventos.organization_api.repository.OrganizationUnitRepository;
import com.br.capoeira.eventos.organization_api.restClient.UserClient;
import com.br.capoeira.eventos.organization_api.service.JoinCodeGenerator;
import com.br.capoeira.eventos.organization_api.service.OrganizationKeyNormalizer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.flyway.enabled=true",
        "app.initial-data-seed.enabled=false",
        "app.initial-data-seed.resource=classpath:seed/bonfim-seed.json",
        "app.initial-data-seed.admin-name=Initial Administrator",
        "app.initial-data-seed.admin-email=seed@test.local",
        "app.initial-data-seed.admin-password=environment-secret",
        "app.initial-data-seed.admin-avatar-url=https://assets.test/admin.png",
        "app.initial-data-seed.organization-name=Associação de Capoeira Nosso Senhor do Bonfim",
        "app.initial-data-seed.organization-logo-url=https://assets.test/bonfim.png",
        "app.initial-data-seed.organization-unit-name=Jundiai - Mestre Kaue",
        "app.initial-data-seed.organization-unit-contact-phone=+55 11 4000-0000"
})
@Testcontainers
@Import({
        BonfimSeedService.class,
        BonfimSeedManifestLoader.class,
        OrganizationKeyNormalizer.class,
        JoinCodeGenerator.class,
        BonfimSeedPostgreSQLIntegrationTest.Config.class
})
class BonfimSeedPostgreSQLIntegrationTest {

    private static final String ORGANIZATION_KEY =
            "associacao-de-capoeira-nosso-senhor-do-bonfim";
    private static final String UNIT_NAME = "Jundiai - Mestre Kaue";

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("bonfim_seed_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
    }

    @TestConfiguration
    @EnableConfigurationProperties(BonfimSeedProperties.class)
    static class Config {
        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }

    @Autowired
    private BonfimSeedService seedService;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private OrganizationUnitRepository organizationUnitRepository;

    @MockitoBean
    private UserClient userClient;

    @AfterEach
    void cleanDatabase() {
        organizationUnitRepository.deleteAll();
        organizationRepository.deleteAll();
    }

    @Test
    void shouldPersistDocumentedDataOnceAndPreserveAdministrativeChangesOnRestart() {
        UserResponseDto unlinked = admin(Role.CLIENT, null, null);
        AtomicReference<UserResponseDto> currentAdmin = new AtomicReference<>(unlinked);
        configureStatefulUserService(currentAdmin);

        BonfimSeedResult first = seedService.seed();

        var organization = organizationRepository.findByOrganizationKey(ORGANIZATION_KEY).orElseThrow();
        var unit = organizationUnitRepository
                .findByOrganization_IdAndName(organization.getId(), UNIT_NAME)
                .orElseThrow();
        assertThat(organization.getName())
                .isEqualTo("Associação de Capoeira Nosso Senhor do Bonfim");
        assertThat(organization.getDescription()).hasSize(516);
        assertThat(unit.getDescription()).hasSize(459);
        assertThat(unit.getCity()).isEqualTo("Jundiaí");
        assertThat(unit.getState()).isEqualTo("SP");
        assertThat(unit.getCountry()).isEqualTo("Brasil");
        assertThat(unit.getAddress()).isEqualTo("Rua José Dainese, 105 — Vila Municipal");
        assertThat(unit.getContactEmail()).isEqualTo("capoeirabonfimjundiai@gmail.com");
        assertThat(unit.getContactPhone()).isEqualTo("+55 11 4000-0000");
        assertThat(unit.getJoinCode()).hasSize(6);

        organization.setDescription("Descrição alterada pelo administrador");
        organization.setLogoUrl("https://assets.test/custom.png");
        unit.setDescription("Descrição da unidade alterada");
        unit.setAddress("Endereço confirmado posteriormente");
        unit.setContactEmail("contato-atualizado@test.local");
        unit.setContactPhone("telefone atualizado");
        organizationRepository.saveAndFlush(organization);
        organizationUnitRepository.saveAndFlush(unit);

        BonfimSeedResult second = seedService.seed();

        assertThat(second.organizationId()).isEqualTo(first.organizationId());
        assertThat(second.organizationUnitId()).isEqualTo(first.organizationUnitId());
        assertThat(organizationRepository.countByOrganizationKey(ORGANIZATION_KEY)).isOne();
        assertThat(organizationUnitRepository.findAll()).hasSize(1);
        var preservedOrganization = organizationRepository.findById(first.organizationId()).orElseThrow();
        var preservedUnit = organizationUnitRepository.findById(first.organizationUnitId()).orElseThrow();
        assertThat(preservedOrganization.getDescription())
                .isEqualTo("Descrição alterada pelo administrador");
        assertThat(preservedOrganization.getLogoUrl()).isEqualTo("https://assets.test/custom.png");
        assertThat(preservedUnit.getDescription()).isEqualTo("Descrição da unidade alterada");
        assertThat(preservedUnit.getAddress()).isEqualTo("Endereço confirmado posteriormente");
        assertThat(preservedUnit.getContactEmail()).isEqualTo("contato-atualizado@test.local");
        assertThat(preservedUnit.getContactPhone()).isEqualTo("telefone atualizado");
        verify(userClient, times(1)).promoteToSuperAdmin(any());
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void shouldSerializeConcurrentSeedExecutionsWithoutDuplicates() throws Exception {
        AtomicReference<UserResponseDto> currentAdmin = new AtomicReference<>(admin(Role.CLIENT, null, null));
        configureStatefulUserService(currentAdmin);

        var start = new CountDownLatch(1);
        var executor = Executors.newFixedThreadPool(2);
        try {
            var first = executor.submit(() -> {
                start.await(10, TimeUnit.SECONDS);
                return seedService.seed();
            });
            var second = executor.submit(() -> {
                start.await(10, TimeUnit.SECONDS);
                return seedService.seed();
            });
            start.countDown();

            List<BonfimSeedResult> results = List.of(
                    first.get(20, TimeUnit.SECONDS),
                    second.get(20, TimeUnit.SECONDS)
            );

            assertThat(results).extracting(BonfimSeedResult::organizationId).containsOnly(results.get(0).organizationId());
            assertThat(results).extracting(BonfimSeedResult::organizationUnitId)
                    .containsOnly(results.get(0).organizationUnitId());
        } finally {
            executor.shutdownNow();
        }

        assertThat(organizationRepository.countByOrganizationKey(ORGANIZATION_KEY)).isOne();
        assertThat(organizationUnitRepository.findAll()).hasSize(1);
        verify(userClient, times(1)).promoteToSuperAdmin(any());
    }

    private void configureStatefulUserService(AtomicReference<UserResponseDto> state) {
        when(userClient.ensureInitialAdmin(any())).thenAnswer(invocation -> state.get());
        when(userClient.promoteToSuperAdmin(any())).thenAnswer(invocation -> {
            var request = invocation.getArgument(
                    0,
                    com.br.capoeira.eventos.organization_api.dto.PromoteToSuperAdminDtoRequest.class
            );
            UserResponseDto linked = admin(
                    Role.SUPER_ADMIN,
                    request.organizationId(),
                    request.organizationUnitId()
            );
            state.set(linked);
            return linked;
        });
    }

    private UserResponseDto admin(Role role, Long organizationId, Long unitId) {
        var result = new UserResponseDto();
        result.setId(1L);
        result.setName("Initial Administrator");
        result.setEmail("seed@test.local");
        result.setRole(role);
        result.setActive(true);
        result.setOrganizationId(organizationId);
        result.setOrganizationUnitId(unitId);
        result.setAvatarUrl("https://assets.test/admin.png");
        return result;
    }
}
