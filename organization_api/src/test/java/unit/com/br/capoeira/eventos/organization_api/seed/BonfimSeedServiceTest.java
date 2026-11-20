package unit.com.br.capoeira.eventos.organization_api.seed;

import com.br.capoeira.eventos.organization_api.config.exception.BonfimSeedException;
import com.br.capoeira.eventos.organization_api.dto.InitialAdminSeedRequestDto;
import com.br.capoeira.eventos.organization_api.dto.PromoteToSuperAdminDtoRequest;
import com.br.capoeira.eventos.organization_api.dto.Role;
import com.br.capoeira.eventos.organization_api.dto.UserResponseDto;
import com.br.capoeira.eventos.organization_api.model.Organization;
import com.br.capoeira.eventos.organization_api.model.OrganizationUnit;
import com.br.capoeira.eventos.organization_api.repository.OrganizationRepository;
import com.br.capoeira.eventos.organization_api.repository.OrganizationUnitRepository;
import com.br.capoeira.eventos.organization_api.restClient.UserClient;
import com.br.capoeira.eventos.organization_api.seed.BonfimSeedManifest;
import com.br.capoeira.eventos.organization_api.seed.BonfimSeedManifestLoader;
import com.br.capoeira.eventos.organization_api.seed.BonfimSeedProperties;
import com.br.capoeira.eventos.organization_api.seed.BonfimSeedService;
import com.br.capoeira.eventos.organization_api.service.JoinCodeGenerator;
import com.br.capoeira.eventos.organization_api.service.OrganizationKeyNormalizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BonfimSeedServiceTest {

    @Mock
    private BonfimSeedManifestLoader manifestLoader;
    @Mock
    private OrganizationRepository organizationRepository;
    @Mock
    private OrganizationUnitRepository organizationUnitRepository;
    @Mock
    private JoinCodeGenerator joinCodeGenerator;
    @Mock
    private UserClient userClient;
    @Mock
    private JdbcTemplate jdbcTemplate;

    private BonfimSeedProperties properties;
    private BonfimSeedService seedService;

    @BeforeEach
    void setUp() {
        properties = validProperties();
        seedService = new BonfimSeedService(
                properties,
                manifestLoader,
                new OrganizationKeyNormalizer(),
                organizationRepository,
                organizationUnitRepository,
                joinCodeGenerator,
                userClient,
                jdbcTemplate
        );
        when(manifestLoader.load("classpath:seed/bonfim-seed.json")).thenReturn(manifest());
    }

    @Test
    void shouldCreateApprovedOrganizationUnitAndInitialAdminLink() {
        when(userClient.ensureInitialAdmin(any())).thenReturn(admin(Role.CLIENT, null, null));
        when(organizationRepository.findByOrganizationKey(expectedKey())).thenReturn(Optional.empty());
        when(organizationRepository.saveAndFlush(any())).thenAnswer(invocation -> {
            Organization organization = invocation.getArgument(0);
            organization.setId(10L);
            return organization;
        });
        when(organizationUnitRepository.findByOrganization_IdAndName(10L, unitName()))
                .thenReturn(Optional.empty());
        when(joinCodeGenerator.generateUnique()).thenReturn("ABC123");
        when(organizationUnitRepository.saveAndFlush(any())).thenAnswer(invocation -> {
            OrganizationUnit unit = invocation.getArgument(0);
            unit.setId(20L);
            return unit;
        });
        when(userClient.promoteToSuperAdmin(new PromoteToSuperAdminDtoRequest(1L, 10L, 20L)))
                .thenReturn(admin(Role.SUPER_ADMIN, 10L, 20L));

        var result = seedService.seed();

        assertThat(result.organizationCreated()).isTrue();
        assertThat(result.organizationUnitCreated()).isTrue();
        assertThat(result.userLinkUpdated()).isTrue();
        assertThat(result.organizationId()).isEqualTo(10L);
        assertThat(result.organizationUnitId()).isEqualTo(20L);

        var organizationCaptor = ArgumentCaptor.forClass(Organization.class);
        verify(organizationRepository).saveAndFlush(organizationCaptor.capture());
        assertThat(organizationCaptor.getValue().getName()).isEqualTo(organizationName());
        assertThat(organizationCaptor.getValue().getOrganizationKey()).isEqualTo(expectedKey());
        assertThat(organizationCaptor.getValue().getDescription())
                .isEqualTo(manifest().organization().description());
        assertThat(organizationCaptor.getValue().getLogoUrl()).isEqualTo("https://assets.test/bonfim.png");

        var unitCaptor = ArgumentCaptor.forClass(OrganizationUnit.class);
        verify(organizationUnitRepository).saveAndFlush(unitCaptor.capture());
        OrganizationUnit unit = unitCaptor.getValue();
        assertThat(unit.getName()).isEqualTo(unitName());
        assertThat(unit.getDescription()).isEqualTo(manifest().unit().description());
        assertThat(unit.getCity()).isEqualTo("Jundiaí");
        assertThat(unit.getState()).isEqualTo("SP");
        assertThat(unit.getCountry()).isEqualTo("Brasil");
        assertThat(unit.getAddress()).isEqualTo("Rua José Dainese, 105 — Vila Municipal");
        assertThat(unit.getContactEmail()).isEqualTo("capoeirabonfimjundiai@gmail.com");
        assertThat(unit.getContactPhone()).isEqualTo("+55 11 4000-0000");
        assertThat(unit.getJoinCode()).isEqualTo("ABC123");

        var adminRequest = ArgumentCaptor.forClass(InitialAdminSeedRequestDto.class);
        verify(userClient).ensureInitialAdmin(adminRequest.capture());
        assertThat(adminRequest.getValue().email()).isEqualTo("admin@test.local");
        assertThat(adminRequest.getValue().password()).isEqualTo("environment-secret");
    }

    @Test
    void shouldBeIdempotentAndPreserveAdministratorChanges() {
        Organization organization = existingOrganization();
        organization.setName("Nome alterado pelo administrador");
        organization.setDescription("Descrição alterada pelo administrador");
        organization.setLogoUrl("https://assets.test/custom-logo.png");
        organization.setActive(false);

        OrganizationUnit unit = existingUnit(organization);
        unit.setDescription("Descrição local alterada");
        unit.setAddress("Endereço atualizado pelo administrador");
        unit.setContactEmail("novo-contato@test.local");
        unit.setContactPhone("telefone atualizado");
        unit.setActive(false);

        when(userClient.ensureInitialAdmin(any())).thenReturn(admin(Role.SUPER_ADMIN, 10L, 20L));
        when(organizationRepository.findByOrganizationKey(expectedKey())).thenReturn(Optional.of(organization));
        when(organizationUnitRepository.findByOrganization_IdAndName(10L, unitName()))
                .thenReturn(Optional.of(unit));

        var result = seedService.seed();

        assertThat(result.organizationCreated()).isFalse();
        assertThat(result.organizationUnitCreated()).isFalse();
        assertThat(result.userLinkUpdated()).isFalse();
        assertThat(organization.getId()).isEqualTo(10L);
        assertThat(organization.getDescription()).isEqualTo("Descrição alterada pelo administrador");
        assertThat(organization.getLogoUrl()).isEqualTo("https://assets.test/custom-logo.png");
        assertThat(organization.getActive()).isFalse();
        assertThat(unit.getId()).isEqualTo(20L);
        assertThat(unit.getDescription()).isEqualTo("Descrição local alterada");
        assertThat(unit.getAddress()).isEqualTo("Endereço atualizado pelo administrador");
        assertThat(unit.getContactEmail()).isEqualTo("novo-contato@test.local");
        assertThat(unit.getContactPhone()).isEqualTo("telefone atualizado");
        assertThat(unit.getActive()).isFalse();
        assertThat(unit.getJoinCode()).isEqualTo("ABC123");

        verify(organizationRepository, never()).saveAndFlush(any());
        verify(organizationUnitRepository, never()).saveAndFlush(any());
        verifyNoInteractions(joinCodeGenerator);
        verify(userClient, never()).promoteToSuperAdmin(any());
    }

    @Test
    void shouldCompleteOnlyMissingUnitAndUserLink() {
        Organization organization = existingOrganization();
        when(userClient.ensureInitialAdmin(any())).thenReturn(admin(Role.CLIENT, null, null));
        when(organizationRepository.findByOrganizationKey(expectedKey())).thenReturn(Optional.of(organization));
        when(organizationUnitRepository.findByOrganization_IdAndName(10L, unitName()))
                .thenReturn(Optional.empty());
        when(joinCodeGenerator.generateUnique()).thenReturn("ABC123");
        when(organizationUnitRepository.saveAndFlush(any())).thenAnswer(invocation -> {
            OrganizationUnit unit = invocation.getArgument(0);
            unit.setId(20L);
            return unit;
        });
        when(userClient.promoteToSuperAdmin(any())).thenReturn(admin(Role.SUPER_ADMIN, 10L, 20L));

        var result = seedService.seed();

        assertThat(result.organizationCreated()).isFalse();
        assertThat(result.organizationUnitCreated()).isTrue();
        assertThat(result.userLinkUpdated()).isTrue();
        verify(organizationRepository, never()).saveAndFlush(any());
        verify(organizationUnitRepository).saveAndFlush(any());
    }

    @Test
    void shouldGenerateJoinCodeForPartiallyCreatedUnitWithoutOverwritingItsData() {
        Organization organization = existingOrganization();
        OrganizationUnit unit = existingUnit(organization);
        unit.setJoinCode(null);
        unit.setDescription("Descrição preservada");

        when(userClient.ensureInitialAdmin(any())).thenReturn(admin(Role.SUPER_ADMIN, 10L, 20L));
        when(organizationRepository.findByOrganizationKey(expectedKey())).thenReturn(Optional.of(organization));
        when(organizationUnitRepository.findByOrganization_IdAndName(10L, unitName()))
                .thenReturn(Optional.of(unit));
        when(joinCodeGenerator.generateUnique()).thenReturn("NEW123");
        when(organizationUnitRepository.saveAndFlush(unit)).thenReturn(unit);

        seedService.seed();

        assertThat(unit.getJoinCode()).isEqualTo("NEW123");
        assertThat(unit.getDescription()).isEqualTo("Descrição preservada");
        verify(organizationUnitRepository).saveAndFlush(unit);
        verify(userClient, never()).promoteToSuperAdmin(any());
    }

    @Test
    void shouldRejectMissingDocumentedMandatoryEnvironmentDataBeforeWriting() {
        properties.setOrganizationUnitContactPhone(" ");

        assertThatThrownBy(() -> seedService.seed())
                .isInstanceOf(BonfimSeedException.class)
                .hasMessageContaining("INITIAL_ORGANIZATION_UNIT_CONTACT_PHONE");

        verifyNoInteractions(userClient, organizationRepository, organizationUnitRepository, jdbcTemplate);
    }

    private BonfimSeedProperties validProperties() {
        var result = new BonfimSeedProperties();
        result.setEnabled(true);
        result.setResource("classpath:seed/bonfim-seed.json");
        result.setAdminName("Initial Administrator");
        result.setAdminEmail("admin@test.local");
        result.setAdminPassword("environment-secret");
        result.setAdminAvatarUrl("https://assets.test/admin.png");
        result.setOrganizationName(organizationName());
        result.setOrganizationLogoUrl("https://assets.test/bonfim.png");
        result.setOrganizationUnitName(unitName());
        result.setOrganizationUnitContactPhone("+55 11 4000-0000");
        return result;
    }

    private BonfimSeedManifest manifest() {
        return new BonfimSeedManifest(
                "Dossie_Bonfim.pdf",
                new BonfimSeedManifest.OrganizationData(
                        organizationName(),
                        expectedKey(),
                        "Descrição histórica da organização"
                ),
                new BonfimSeedManifest.UnitData(
                        unitName(),
                        "Descrição histórica da unidade",
                        "Jundiaí",
                        "SP",
                        "Brasil",
                        "Rua José Dainese, 105 — Vila Municipal",
                        "capoeirabonfimjundiai@gmail.com",
                        "Carlos Alberto da Silva — Mestre Kauê",
                        true
                )
        );
    }

    private Organization existingOrganization() {
        Organization organization = Organization.builder()
                .id(10L)
                .name(organizationName())
                .description("Descrição existente")
                .logoUrl("https://assets.test/existing.png")
                .active(true)
                .build();
        organization.assignOrganizationKey(expectedKey());
        return organization;
    }

    private OrganizationUnit existingUnit(Organization organization) {
        return OrganizationUnit.builder()
                .id(20L)
                .organization(organization)
                .name(unitName())
                .description("Descrição existente")
                .city("Jundiaí")
                .state("SP")
                .country("Brasil")
                .address("Endereço existente")
                .contactPhone("Telefone existente")
                .contactEmail("existente@test.local")
                .active(true)
                .joinCode("ABC123")
                .build();
    }

    private UserResponseDto admin(Role role, Long organizationId, Long unitId) {
        var result = new UserResponseDto();
        result.setId(1L);
        result.setName("Initial Administrator");
        result.setEmail("admin@test.local");
        result.setRole(role);
        result.setActive(true);
        result.setOrganizationId(organizationId);
        result.setOrganizationUnitId(unitId);
        result.setAvatarUrl("https://assets.test/admin.png");
        return result;
    }

    private String organizationName() {
        return "Associação de Capoeira Nosso Senhor do Bonfim";
    }

    private String expectedKey() {
        return "associacao-de-capoeira-nosso-senhor-do-bonfim";
    }

    private String unitName() {
        return "Jundiai - Mestre Kaue";
    }
}
