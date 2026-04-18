package unit.com.br.capoeira.eventos.organization_api.service;

import com.br.capoeira.eventos.organization_api.dto.OrganizationCreateRequestDto;
import com.br.capoeira.eventos.organization_api.exception.ValidationException;
import com.br.capoeira.eventos.organization_api.mapper.OrganizationMapper;
import com.br.capoeira.eventos.organization_api.model.Organization;
import com.br.capoeira.eventos.organization_api.model.OrganizationUnit;
import com.br.capoeira.eventos.organization_api.repository.OrganizationRepository;
import com.br.capoeira.eventos.organization_api.repository.OrganizationUnitRepository;
import com.br.capoeira.eventos.organization_api.service.OrganizationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    @InjectMocks
    private OrganizationService service;

    @Test
    void shouldCreateOrganizationWithMainUnit() {
        var requestDto = getMockOrganizationCreateRequestDto();
        var organization = getMockOrganization();
        var savedOrganization = getMockOrganization();
        savedOrganization.setId(1L);

        var organizationResponseDto = getMockOrganizationResponseDto();

        when(mapper.organizationDtoToOrganization(any(OrganizationCreateRequestDto.class))).thenReturn(organization);
        when(organizationRepository.existsBySlug(anyString())).thenReturn(false);
        when(organizationRepository.save(any(Organization.class))).thenReturn(savedOrganization);
        when(organizationUnitRepository.existsByJoinCode(anyString())).thenReturn(false);
        when(organizationUnitRepository.existsByOrganization_IdAndSlug(anyLong(), anyString())).thenReturn(false);
        when(organizationUnitRepository.save(any(OrganizationUnit.class))).thenAnswer(invocation -> {
            var unit = invocation.getArgument(0, OrganizationUnit.class);
            unit.setId(1L);
            return unit;
        });
        //when(organizationRepository.findById(anyLong())).thenReturn(Optional.of(savedOrganization));
        when(mapper.organizationToResponseDto(any(Organization.class))).thenReturn(organizationResponseDto);

        var result = service.createWithMainUnit(requestDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(organizationResponseDto.getId());

        verify(mapper).organizationDtoToOrganization(any(OrganizationCreateRequestDto.class));
        verify(organizationRepository).existsBySlug(anyString());
        verify(organizationRepository).save(any(Organization.class));
        verify(organizationUnitRepository).existsByJoinCode(anyString());
        verify(organizationUnitRepository).existsByOrganization_IdAndSlug(anyLong(), anyString());
        verify(organizationUnitRepository).save(any(OrganizationUnit.class));
        //verify(organizationRepository).findById(anyLong());
        verify(mapper).organizationToResponseDto(any(Organization.class));
    }

    @Test
    void shouldNotCreateOrganizationWithMainUnitWhenSlugAlreadyExists() {
        var requestDto = getMockOrganizationCreateRequestDto();
        var organization = getMockOrganization();

        when(mapper.organizationDtoToOrganization(any(OrganizationCreateRequestDto.class))).thenReturn(organization);
        when(organizationRepository.existsBySlug(anyString())).thenReturn(true);

        assertThatThrownBy(() -> service.createWithMainUnit(requestDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Organization slug already exists");

        verify(mapper).organizationDtoToOrganization(any(OrganizationCreateRequestDto.class));
        verify(organizationRepository).existsBySlug(anyString());
        verify(organizationRepository, never()).save(any(Organization.class));
        verify(organizationUnitRepository, never()).save(any(OrganizationUnit.class));
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
        when(organizationUnitRepository.existsByOrganization_IdAndSlug(anyLong(), anyString())).thenReturn(false);
        when(organizationUnitRepository.save(any(OrganizationUnit.class))).thenReturn(organizationUnit);
        when(mapper.organizationUnitToResponseDto(any(OrganizationUnit.class))).thenReturn(responseDto);

        var result = service.create(dto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(responseDto.getId());

        verify(organizationRepository).findById(anyLong());
        verify(mapper).organizationUnitDtoToOrganizationUnit(any());
        verify(organizationUnitRepository).existsByJoinCode(anyString());
        verify(organizationUnitRepository).existsByOrganization_IdAndSlug(anyLong(), anyString());
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
    void shouldNotCreateOrganizationUnitWhenSlugAlreadyExistsForOrganization() {
        var dto = getMockOrganizationUnitDto();
        var organization = getMockOrganization();
        var organizationUnit = getMockOrganizationUnit();

        when(organizationRepository.findById(anyLong())).thenReturn(Optional.of(organization));
        when(mapper.organizationUnitDtoToOrganizationUnit(any())).thenReturn(organizationUnit);
        when(organizationUnitRepository.existsByJoinCode(anyString())).thenReturn(false);
        when(organizationUnitRepository.existsByOrganization_IdAndSlug(anyLong(), anyString())).thenReturn(true);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Organization unit slug already exists for this organization");

        verify(organizationRepository).findById(anyLong());
        verify(mapper).organizationUnitDtoToOrganizationUnit(any());
        verify(organizationUnitRepository).existsByJoinCode(anyString());
        verify(organizationUnitRepository).existsByOrganization_IdAndSlug(anyLong(), anyString());
        verify(organizationUnitRepository, never()).save(any());
    }

    @Test
    void shouldUpdateOrganization() {
        var dto = getMockOrganizationUpdateDto();
        var organization = getMockOrganization();
        var responseDto = getMockOrganizationResponseDto();

        when(organizationRepository.findById(anyLong())).thenReturn(Optional.of(organization));
        when(organizationRepository.findBySlug(anyString())).thenReturn(Optional.of(organization));
        when(organizationRepository.save(any(Organization.class))).thenReturn(organization);
        when(mapper.organizationToResponseDto(any(Organization.class))).thenReturn(responseDto);

        var result = service.update(dto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(responseDto.getId());

        verify(organizationRepository).findById(anyLong());
        verify(mapper).updateOrganizationFromDto(eq(dto), any(Organization.class));
        verify(organizationRepository).findBySlug(anyString());
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
    void shouldNotUpdateOrganizationWhenSlugAlreadyExistsForAnotherOrganization() {
        var dto = getMockOrganizationUpdateDto();
        var organization = getMockOrganization();
        var anotherOrganization = getMockOrganization();
        anotherOrganization.setId(2L);

        when(organizationRepository.findById(anyLong())).thenReturn(Optional.of(organization));
        when(organizationRepository.findBySlug(anyString())).thenReturn(Optional.of(anotherOrganization));

        assertThatThrownBy(() -> service.update(dto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Organization slug already exists");

        verify(organizationRepository).findById(anyLong());
        verify(mapper).updateOrganizationFromDto(eq(dto), any(Organization.class));
        verify(organizationRepository).findBySlug(anyString());
        verify(organizationRepository, never()).save(any());
    }

    @Test
    void shouldUpdateOrganizationUnit() {
        var dto = getMockOrganizationUnitUpdateDto();
        var organizationUnit = getMockOrganizationUnit();
        var responseDto = getMockOrganizationUnitResponseDto();

        when(organizationUnitRepository.findById(anyLong())).thenReturn(Optional.of(organizationUnit));
        when(organizationUnitRepository.findByOrganization_IdAndSlug(anyLong(), anyString()))
                .thenReturn(Optional.of(organizationUnit));
        when(organizationUnitRepository.save(any(OrganizationUnit.class))).thenReturn(organizationUnit);
        when(mapper.organizationUnitToResponseDto(any(OrganizationUnit.class))).thenReturn(responseDto);

        var result = service.update(dto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(responseDto.getId());

        verify(organizationUnitRepository).findById(anyLong());
        verify(mapper).updateOrganizationUnitFromDto(eq(dto), any(OrganizationUnit.class));
        verify(organizationUnitRepository).findByOrganization_IdAndSlug(anyLong(), anyString());
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
    void shouldNotUpdateOrganizationUnitWhenSlugAlreadyExistsForAnotherUnit() {
        var dto = getMockOrganizationUnitUpdateDto();
        var organizationUnit = getMockOrganizationUnit();
        var anotherUnit = getMockOrganizationUnit();
        anotherUnit.setId(2L);

        when(organizationUnitRepository.findById(anyLong())).thenReturn(Optional.of(organizationUnit));
        when(organizationUnitRepository.findByOrganization_IdAndSlug(anyLong(), anyString()))
                .thenReturn(Optional.of(anotherUnit));

        assertThatThrownBy(() -> service.update(dto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Organization unit slug already exists for this organization");

        verify(organizationUnitRepository).findById(anyLong());
        verify(mapper).updateOrganizationUnitFromDto(eq(dto), any(OrganizationUnit.class));
        verify(organizationUnitRepository).findByOrganization_IdAndSlug(anyLong(), anyString());
        verify(organizationUnitRepository, never()).save(any());
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

        when(organizationUnitRepository.findAllByOrganization_IdOrderByIdAsc(anyLong()))
                .thenReturn(List.of(organizationUnit));
        when(mapper.organizationUnitToResponseDto(any())).thenReturn(responseDto);

        var result = service.findAllByOrganizationId(1L);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(responseDto.getId());

        verify(organizationUnitRepository).findAllByOrganization_IdOrderByIdAsc(anyLong());
        verify(mapper).organizationUnitToResponseDto(any());
    }

    @Test
    void shouldNotFindOrganizationUnitsByOrganizationIdWhenIdIsNull() {
        assertThatThrownBy(() -> service.findAllByOrganizationId(null))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Organization id must be informed");

        verify(organizationUnitRepository, never()).findAllByOrganization_IdOrderByIdAsc(anyLong());
        verify(mapper, never()).organizationUnitToResponseDto(any());
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

