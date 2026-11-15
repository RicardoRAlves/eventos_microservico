package unit.com.br.capoeira.eventos.organization_api.service;

import com.br.capoeira.eventos.organization_api.config.exception.OrganizationKeyConflictException;
import com.br.capoeira.eventos.organization_api.config.exception.ValidationException;
import com.br.capoeira.eventos.organization_api.dto.OrganizationResponseDto;
import com.br.capoeira.eventos.organization_api.dto.PromoteToSuperAdminDtoRequest;
import com.br.capoeira.eventos.organization_api.dto.UserResponseDto;
import com.br.capoeira.eventos.organization_api.mapper.OrganizationMapper;
import com.br.capoeira.eventos.organization_api.model.Organization;
import com.br.capoeira.eventos.organization_api.model.OrganizationUnit;
import com.br.capoeira.eventos.organization_api.repository.OrganizationRepository;
import com.br.capoeira.eventos.organization_api.repository.OrganizationUnitRepository;
import com.br.capoeira.eventos.organization_api.restClient.UserClient;
import com.br.capoeira.eventos.organization_api.service.OrganizationKeyNormalizer;
import com.br.capoeira.eventos.organization_api.service.OrganizationService;
import com.br.capoeira.eventos.organization_api.service.JoinCodeGenerator;
import com.br.capoeira.eventos.organization_api.service.aws.S3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static utils.MockUtils.*;

@ExtendWith(MockitoExtension.class)
class OrganizationKeyServiceTest {

    @Mock
    private OrganizationMapper mapper;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private OrganizationUnitRepository organizationUnitRepository;

    @Mock
    private UserClient userClient;

    @Mock
    private S3Service s3Service;

    @Mock
    private JoinCodeGenerator joinCodeGenerator;

    private OrganizationService service;

    @BeforeEach
    void setUp() {
        service = new OrganizationService(
                mapper,
                organizationRepository,
                organizationUnitRepository,
                userClient,
                s3Service,
                new OrganizationKeyNormalizer(),
                joinCodeGenerator
        );
    }

    @Test
    void shouldGenerateAndPersistOrganizationKeyOnCreate() {
        var request = getMockOrganizationCreateRequestDto();
        var organization = getMockOrganization();
        organization.setId(null);
        organization.setName(request.getName());
        stubSuccessfulCreate(request.getUserId(), organization);

        service.createWithMainUnit(request);

        var captor = ArgumentCaptor.forClass(Organization.class);
        verify(organizationRepository).saveAndFlush(captor.capture());
        assertThat(captor.getValue().getOrganizationKey())
                .isEqualTo("grupo-de-capoeira-nosso-senhor-do-bonfim");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "GRUPO RAIZES",
            "Grupo Raízes",
            "  Grupo   Raizes  "
    })
    void shouldRejectEquivalentOrganizationNameOnCreate(String equivalentName) {
        var request = getMockOrganizationCreateRequestDto();
        request.setName(equivalentName);
        var organization = getMockOrganization();
        organization.setName(equivalentName);

        when(mapper.organizationDtoToOrganization(request)).thenReturn(organization);
        when(organizationRepository.existsByOrganizationKey("grupo-raizes")).thenReturn(true);

        assertThatThrownBy(() -> service.createWithMainUnit(request))
                .isInstanceOf(OrganizationKeyConflictException.class)
                .hasMessage(OrganizationKeyConflictException.DEFAULT_MESSAGE);

        verify(organizationRepository, never()).saveAndFlush(any());
        verifyNoInteractions(userClient);
    }

    @Test
    void shouldKeepInactiveOrganizationKeyReserved() {
        var request = getMockOrganizationCreateRequestDto();
        request.setName("Grupo Inativo");
        var organization = getMockOrganization();
        organization.setName("Grupo Inativo");
        organization.setActive(false);

        when(mapper.organizationDtoToOrganization(request)).thenReturn(organization);
        when(organizationRepository.existsByOrganizationKey("grupo-inativo")).thenReturn(true);

        assertThatThrownBy(() -> service.createWithMainUnit(request))
                .isInstanceOf(OrganizationKeyConflictException.class);

        verify(organizationRepository, never()).saveAndFlush(any());
    }

    @Test
    void shouldRejectNameThatProducesEmptyKeyOnCreate() {
        assertInvalidNameOnCreate("— & !!!");
    }

    @Test
    void shouldRejectExcessivelyLongKeyOnCreate() {
        assertInvalidNameOnCreate("a".repeat(OrganizationKeyNormalizer.MAX_KEY_LENGTH + 1));
    }

    private void assertInvalidNameOnCreate(String name) {
        var request = getMockOrganizationCreateRequestDto();
        request.setName(name);
        var organization = getMockOrganization();
        organization.setName(name);
        when(mapper.organizationDtoToOrganization(request)).thenReturn(organization);

        assertThatThrownBy(() -> service.createWithMainUnit(request))
                .isInstanceOf(ValidationException.class);

        verify(organizationRepository, never()).saveAndFlush(any());
    }

    @Test
    void shouldUpdateNameAndOrganizationKeyWhilePreservingId() {
        var organization = getMockOrganization();
        var originalId = organization.getId();
        var request = getMockOrganizationUpdateDto();
        request.setName("Grupo Raízes");
        stubSuccessfulUpdate(organization);

        service.update(request);

        assertThat(organization.getId()).isEqualTo(originalId);
        assertThat(organization.getName()).isEqualTo("Grupo Raízes");
        assertThat(organization.getOrganizationKey()).isEqualTo("grupo-raizes");
        verify(organizationRepository).existsByOrganizationKey("grupo-raizes");
    }

    @Test
    void shouldPreserveKeyWhenChangedNameNormalizesToSameValue() {
        var organization = getMockOrganization();
        organization.setName("Grupo Bonfim");
        organization.assignOrganizationKey("grupo-bonfim");
        var request = getMockOrganizationUpdateDto();
        request.setName("  GRUPO BÔNFIM  ");
        stubSuccessfulUpdate(organization);

        service.update(request);

        assertThat(organization.getName()).isEqualTo("  GRUPO BÔNFIM  ");
        assertThat(organization.getOrganizationKey()).isEqualTo("grupo-bonfim");
        verify(organizationRepository, never()).existsByOrganizationKey(any());
    }

    @Test
    void shouldPreserveKeyWhenOnlyDescriptionChanges() {
        var organization = getMockOrganization();
        var request = matchingUpdateRequest(organization);
        request.setDescription("Nova descrição");
        stubSuccessfulUpdate(organization);

        service.update(request);

        assertThat(organization.getDescription()).isEqualTo("Nova descrição");
        assertThat(organization.getOrganizationKey()).isEqualTo("grupo-bonfim");
    }

    @Test
    void shouldPreserveKeyWhenOnlyLogoChanges() {
        var organization = getMockOrganization();
        var request = matchingUpdateRequest(organization);
        request.setLogoUrl("https://example.test/new-logo.png");
        stubSuccessfulUpdate(organization);

        service.update(request);

        assertThat(organization.getLogoUrl()).isEqualTo("https://example.test/new-logo.png");
        assertThat(organization.getOrganizationKey()).isEqualTo("grupo-bonfim");
    }

    @Test
    void shouldPreserveKeyWhenOnlyActiveStatusChanges() {
        var organization = getMockOrganization();
        var request = matchingUpdateRequest(organization);
        request.setActive(false);
        stubSuccessfulUpdate(organization);

        service.update(request);

        assertThat(organization.getActive()).isFalse();
        assertThat(organization.getOrganizationKey()).isEqualTo("grupo-bonfim");
    }

    @Test
    void shouldRejectConflictingNameAndKeepOriginalNameAndKey() {
        var organization = getMockOrganization();
        var originalName = organization.getName();
        var originalKey = organization.getOrganizationKey();
        var request = matchingUpdateRequest(organization);
        request.setName("Outra Organização");

        when(organizationRepository.findById(organization.getId())).thenReturn(Optional.of(organization));
        when(organizationRepository.existsByOrganizationKey("outra-organizacao")).thenReturn(true);

        assertThatThrownBy(() -> service.update(request))
                .isInstanceOf(OrganizationKeyConflictException.class);

        assertThat(organization.getName()).isEqualTo(originalName);
        assertThat(organization.getOrganizationKey()).isEqualTo(originalKey);
        verify(mapper, never()).updateOrganizationFromDto(any(), any());
        verify(organizationRepository, never()).saveAndFlush(any());
    }

    private void stubSuccessfulCreate(Long userId, Organization organization) {
        when(mapper.organizationDtoToOrganization(any())).thenReturn(organization);
        when(organizationRepository.saveAndFlush(any(Organization.class))).thenAnswer(invocation -> {
            var saved = invocation.getArgument(0, Organization.class);
            saved.setId(10L);
            return saved;
        });
        when(joinCodeGenerator.generateUnique()).thenReturn("ABC123");
        when(organizationUnitRepository.save(any(OrganizationUnit.class))).thenAnswer(invocation -> {
            var unit = invocation.getArgument(0, OrganizationUnit.class);
            unit.setId(20L);
            return unit;
        });
        when(userClient.promoteToSuperAdmin(any(PromoteToSuperAdminDtoRequest.class)))
                .thenAnswer(invocation -> {
                    var input = invocation.getArgument(0, PromoteToSuperAdminDtoRequest.class);
                    var response = new UserResponseDto();
                    response.setId(userId);
                    response.setOrganizationId(input.organizationId());
                    response.setOrganizationUnitId(input.organizationUnitId());
                    return response;
                });
        when(mapper.organizationToResponseDto(any())).thenReturn(new OrganizationResponseDto());
    }

    private void stubSuccessfulUpdate(Organization organization) {
        when(organizationRepository.findById(organization.getId())).thenReturn(Optional.of(organization));
        doAnswer(invocation -> {
            var input = invocation.getArgument(0, com.br.capoeira.eventos.organization_api.dto.OrganizationUpdateDto.class);
            var target = invocation.getArgument(1, Organization.class);
            target.setName(input.getName());
            target.setDescription(input.getDescription());
            target.setLogoUrl(input.getLogoUrl());
            target.setActive(input.getActive());
            return null;
        }).when(mapper).updateOrganizationFromDto(any(), any());
        when(organizationRepository.saveAndFlush(organization)).thenReturn(organization);
        when(mapper.organizationToResponseDto(organization)).thenAnswer(invocation -> OrganizationResponseDto.builder()
                .id(organization.getId())
                .name(organization.getName())
                .organizationKey(organization.getOrganizationKey())
                .description(organization.getDescription())
                .logoUrl(organization.getLogoUrl())
                .active(organization.getActive())
                .build());
    }

    private com.br.capoeira.eventos.organization_api.dto.OrganizationUpdateDto matchingUpdateRequest(
            Organization organization) {
        return com.br.capoeira.eventos.organization_api.dto.OrganizationUpdateDto.builder()
                .id(organization.getId())
                .name(organization.getName())
                .description(organization.getDescription())
                .logoUrl(organization.getLogoUrl())
                .active(organization.getActive())
                .build();
    }
}
