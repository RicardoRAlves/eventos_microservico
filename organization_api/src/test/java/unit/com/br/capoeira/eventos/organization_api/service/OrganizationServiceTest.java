package unit.com.br.capoeira.eventos.organization_api.service;

import com.br.capoeira.eventos.organization_api.config.exception.ValidationException;
import com.br.capoeira.eventos.organization_api.dto.OrganizationCreateRequestDto;
import com.br.capoeira.eventos.organization_api.dto.PromoteToSuperAdminDtoRequest;
import com.br.capoeira.eventos.organization_api.dto.UserResponseDto;
import com.br.capoeira.eventos.organization_api.mapper.OrganizationMapper;
import com.br.capoeira.eventos.organization_api.model.Organization;
import com.br.capoeira.eventos.organization_api.model.OrganizationUnit;
import com.br.capoeira.eventos.organization_api.repository.OrganizationRepository;
import com.br.capoeira.eventos.organization_api.repository.OrganizationUnitRepository;
import com.br.capoeira.eventos.organization_api.restClient.UserClient;
import com.br.capoeira.eventos.organization_api.service.OrganizationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static utils.MockUtils.*;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceTest {

    @Mock
    private OrganizationMapper mapper;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private OrganizationUnitRepository organizationUnitRepository;

    @Mock
    private UserClient userClient;

    @InjectMocks
    private OrganizationService service;

    @Test
    void shouldCreateOrganizationWithMainUnit() {
        var requestDto = getMockOrganizationCreateRequestDto();

        var organization = getMockOrganization();

        var savedOrganization = getMockOrganization();
        savedOrganization.setId(1L);

        var organizationResponseDto = getMockOrganizationResponseDto();

        when(mapper.organizationDtoToOrganization(any(OrganizationCreateRequestDto.class)))
                .thenReturn(organization);

        when(organizationRepository.save(any(Organization.class)))
                .thenReturn(savedOrganization);

        when(organizationUnitRepository.existsByJoinCode(anyString()))
                .thenReturn(false);

        when(organizationUnitRepository.save(any(OrganizationUnit.class)))
                .thenAnswer(invocation -> {
                    var unit = invocation.getArgument(0, OrganizationUnit.class);
                    unit.setId(1L);
                    return unit;
                });

        when(userClient.promoteToSuperAdmin(any(PromoteToSuperAdminDtoRequest.class)))
                .thenAnswer(invocation -> {
                    var request = invocation.getArgument(
                            0,
                            PromoteToSuperAdminDtoRequest.class
                    );

                    var user = new UserResponseDto();
                    user.setId(request.userId());
                    user.setOrganizationId(request.organizationId());
                    user.setOrganizationUnitId(request.organizationUnitId());

                    return user;
                });

        when(mapper.organizationToResponseDto(any(Organization.class)))
                .thenReturn(organizationResponseDto);

        var result = service.createWithMainUnit(requestDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(
                organizationResponseDto.getId()
        );

        verify(mapper).organizationDtoToOrganization(
                any(OrganizationCreateRequestDto.class)
        );

        verify(organizationRepository).save(
                any(Organization.class)
        );

        verify(organizationUnitRepository).existsByJoinCode(
                anyString()
        );

        verify(organizationUnitRepository).save(
                any(OrganizationUnit.class)
        );

        verify(userClient).promoteToSuperAdmin(
                any(PromoteToSuperAdminDtoRequest.class)
        );

        verify(mapper).organizationToResponseDto(
                any(Organization.class)
        );
    }

    @Test
    void shouldFindOrganizationResponseById() {
        var organization = getMockOrganization();
        var responseDto = getMockOrganizationResponseDto();

        when(organizationRepository.findById(anyLong())).thenReturn(Optional.of(organization));
        when(mapper.organizationToResponseDto(any(Organization.class))).thenReturn(responseDto);

        var result = service.findOrganizationById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(responseDto.getId());

        verify(organizationRepository).findById(anyLong());
        verify(mapper).organizationToResponseDto(any(Organization.class));
    }

    @Test
    void shouldNotFindOrganizationResponseByIdWhenIdIsNull() {
        assertThatThrownBy(() -> service.findOrganizationById(null))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Organization id must be informed");

        verify(organizationRepository, never()).findById(anyLong());
        verify(mapper, never()).organizationToResponseDto(any());
    }

    @Test
    void shouldNotFindOrganizationResponseByIdWhenNotFound() {
        when(organizationRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findOrganizationById(1L))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Organization not found");

        verify(organizationRepository).findById(anyLong());
        verify(mapper, never()).organizationToResponseDto(any());
    }

    @Test
    void shouldCreateOrganizationUnit() {
        var dto = getMockOrganizationUnitDto();
        var organization = getMockOrganization();
        var organizationUnit = getMockOrganizationUnit();
        var responseDto = getMockOrganizationUnitResponseDto();

        when(organizationRepository.findById(anyLong())).thenReturn(Optional.of(organization));
        when(mapper.organizationUnitDtoToOrganizationUnit(any())).thenReturn(organizationUnit);
        when(organizationUnitRepository.existsByJoinCode(anyString())).thenReturn(false);
        when(organizationUnitRepository.save(any(OrganizationUnit.class))).thenReturn(organizationUnit);
        when(mapper.organizationUnitToResponseDto(any(OrganizationUnit.class))).thenReturn(responseDto);

        var result = service.create(dto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(responseDto.getId());

        verify(organizationRepository).findById(anyLong());
        verify(mapper).organizationUnitDtoToOrganizationUnit(any());
        verify(organizationUnitRepository).existsByJoinCode(anyString());
        verify(organizationUnitRepository).save(any(OrganizationUnit.class));
        verify(mapper).organizationUnitToResponseDto(any(OrganizationUnit.class));
    }

    @Test
    void shouldNotCreateOrganizationUnitWhenOrganizationDoesNotExist() {
        var dto = getMockOrganizationUnitDto();

        when(organizationRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Organization not found");

        verify(organizationRepository).findById(anyLong());
        verify(organizationUnitRepository, never()).save(any());
        verify(mapper, never()).organizationUnitDtoToOrganizationUnit(any());
    }

    @Test
    void shouldUpdateOrganization() {
        var dto = getMockOrganizationUpdateDto();
        var organization = getMockOrganization();
        var responseDto = getMockOrganizationResponseDto();

        when(organizationRepository.findById(anyLong())).thenReturn(Optional.of(organization));
        when(organizationRepository.save(any(Organization.class))).thenReturn(organization);
        when(mapper.organizationToResponseDto(any(Organization.class))).thenReturn(responseDto);

        var result = service.update(dto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(responseDto.getId());

        verify(organizationRepository).findById(anyLong());
        verify(mapper).updateOrganizationFromDto(eq(dto), any(Organization.class));
        verify(organizationRepository).save(any(Organization.class));
        verify(mapper).organizationToResponseDto(any(Organization.class));
    }

    @Test
    void shouldNotUpdateOrganizationWhenIdNotFound() {
        var dto = getMockOrganizationUpdateDto();

        when(organizationRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(dto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Organization not found");

        verify(organizationRepository).findById(anyLong());
        verify(organizationRepository, never()).save(any());
        verify(mapper, never()).updateOrganizationFromDto(any(), any());
    }

    @Test
    void shouldUpdateOrganizationUnit() {
        var dto = getMockOrganizationUnitUpdateDto();
        var organizationUnit = getMockOrganizationUnit();
        var responseDto = getMockOrganizationUnitResponseDto();

        when(organizationUnitRepository.findById(anyLong())).thenReturn(Optional.of(organizationUnit));
        when(organizationUnitRepository.save(any(OrganizationUnit.class))).thenReturn(organizationUnit);
        when(mapper.organizationUnitToResponseDto(any(OrganizationUnit.class))).thenReturn(responseDto);

        var result = service.update(dto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(responseDto.getId());

        verify(organizationUnitRepository).findById(anyLong());
        verify(mapper).updateOrganizationUnitFromDto(eq(dto), any(OrganizationUnit.class));
        verify(organizationUnitRepository).save(any(OrganizationUnit.class));
        verify(mapper).organizationUnitToResponseDto(any(OrganizationUnit.class));
    }

    @Test
    void shouldNotUpdateOrganizationUnitWhenIdNotFound() {
        var dto = getMockOrganizationUnitUpdateDto();

        when(organizationUnitRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(dto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Organization Unit not found");

        verify(organizationUnitRepository).findById(anyLong());
        verify(organizationUnitRepository, never()).save(any());
        verify(mapper, never()).updateOrganizationUnitFromDto(any(), any());
    }

    @Test
    void shouldFindOrganizationUnitById() {
        var organizationUnit = getMockOrganizationUnit();
        var responseDto = getMockOrganizationUnitResponseDto();

        when(organizationUnitRepository.findById(anyLong())).thenReturn(Optional.of(organizationUnit));
        when(mapper.organizationUnitToResponseDto(any())).thenReturn(responseDto);

        var result = service.findUnitById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(responseDto.getId());

        verify(organizationUnitRepository).findById(anyLong());
        verify(mapper).organizationUnitToResponseDto(any());
    }

    @Test
    void shouldNotFindOrganizationUnitByIdWhenIdIsNull() {
        assertThatThrownBy(() -> service.findUnitById(null))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Organization unit id must be informed");

        verify(organizationUnitRepository, never()).findById(anyLong());
        verify(mapper, never()).organizationUnitToResponseDto(any());
    }

    @Test
    void shouldNotFindOrganizationUnitByIdWhenNotFound() {
        when(organizationUnitRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findUnitById(1L))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Organization Unit not found");

        verify(organizationUnitRepository).findById(anyLong());
        verify(mapper, never()).organizationUnitToResponseDto(any());
    }

    @Test
    void shouldFindOrganizationUnitsByOrganizationId() {
        var organizationUnit = getMockOrganizationUnit();
        var responseDto = getMockOrganizationUnitResponseDto();

        var organizationUnitPage = new PageImpl<>(
                List.of(organizationUnit),
                PageRequest.of(0, 10, Sort.by("id").ascending()),
                1
        );

        when(organizationUnitRepository.findAllByOrganization_IdOrderByIdAsc(eq(1L), any(Pageable.class)))
                .thenReturn(organizationUnitPage);
        when(mapper.organizationUnitToResponseDto(any())).thenReturn(responseDto);

        var result = service.findAllByOrganizationId(1L, 0, 10);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(responseDto.getId());

        verify(organizationUnitRepository).findAllByOrganization_IdOrderByIdAsc(eq(1L), any(Pageable.class));
        verify(mapper).organizationUnitToResponseDto(any());
    }

    @Test
    void shouldNotFindOrganizationUnitsByOrganizationIdWhenIdIsNull() {
        assertThatThrownBy(() -> service.findAllByOrganizationId(null, 0, 10))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Organization id must be informed");

        verifyNoInteractions(organizationUnitRepository, mapper);
    }

    @Test
    void shouldFindOrganizationUnitByJoinCode() {
        var organizationUnit = getMockOrganizationUnit();
        organizationUnit.setJoinCode("ABC123");
        var responseDto = getMockOrganizationUnitResponseDto();
        responseDto.setJoinCode("ABC123");

        when(organizationUnitRepository.findByJoinCode(anyString())).thenReturn(Optional.of(organizationUnit));
        when(mapper.organizationUnitToResponseDto(any())).thenReturn(responseDto);

        var result = service.findByJoinCode("ABC123");

        assertThat(result).isNotNull();
        assertThat(result.getJoinCode()).isEqualTo("ABC123");

        verify(organizationUnitRepository).findByJoinCode(anyString());
        verify(mapper).organizationUnitToResponseDto(any());
    }

    @Test
    void shouldNotFindOrganizationUnitByJoinCodeWhenCodeIsBlank() {
        assertThatThrownBy(() -> service.findByJoinCode(" "))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Join code must be informed");

        verify(organizationUnitRepository, never()).findByJoinCode(anyString());
        verify(mapper, never()).organizationUnitToResponseDto(any());
    }

    @Test
    void shouldNotFindOrganizationUnitByJoinCodeWhenNotFound() {
        when(organizationUnitRepository.findByJoinCode(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByJoinCode("ABC123"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Organization Unit not found");

        verify(organizationUnitRepository).findByJoinCode(anyString());
        verify(mapper, never()).organizationUnitToResponseDto(any());
    }
}

