package unit.com.br.capoeira.eventos.organization_api.service;

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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static utils.MockUtils.*;

@ExtendWith(MockitoExtension.class)
public class OrganizationServiceTest {

    @Mock
    private OrganizationMapper mapper;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private OrganizationUnitRepository organizationUnitRepository;

    @InjectMocks
    private OrganizationService service;

    @Test
    public void shouldCreateOrganization() {
        var organizationDto = getMockOrganizationDto();
        var organization = getMockOrganization();

        when(mapper.organizationDtoToOrganization(any())).thenReturn(organization);
        when(organizationRepository.existsBySlug(anyString())).thenReturn(false);
        when(organizationRepository.save(any())).thenReturn(organization);

        service.create(organizationDto);

        verify(mapper).organizationDtoToOrganization(any());
        verify(organizationRepository).existsBySlug(anyString());
        verify(organizationRepository).save(any());
    }

    @Test
    public void shouldCreateOrganizationUnit() {
        var organizationUnitDto = getMockOrganizationUnitDto();
        var organizationUnit = getMockOrganizationUnit();

        when(mapper.organizationUnitDtoToOrganizationUnit(any())).thenReturn(organizationUnit);
        when(organizationRepository.existsById(anyLong())).thenReturn(true);
        when(organizationUnitRepository.existsByOrganization_IdAndSlug(anyLong(), anyString())).thenReturn(false);
        when(organizationUnitRepository.save(any())).thenReturn(organizationUnit);

        service.create(organizationUnitDto);

        verify(mapper).organizationUnitDtoToOrganizationUnit(any());
        verify(organizationRepository).existsById(anyLong());
        verify(organizationUnitRepository).existsByOrganization_IdAndSlug(anyLong(), anyString());
        verify(organizationUnitRepository).save(any());
    }

    @Test
    public void shouldUpdateOrganization() {
        var organization = getMockOrganization();

        when(organizationRepository.findById(anyLong())).thenReturn(Optional.of(organization));
        when(organizationRepository.findBySlug(anyString())).thenReturn(Optional.of(organization));
        when(organizationRepository.save(any())).thenReturn(organization);

        service.update(organization);

        verify(organizationRepository).findById(anyLong());
        verify(organizationRepository).findBySlug(anyString());
        verify(organizationRepository).save(any());
    }

    @Test
    public void shouldNotUpdateOrganizationWhenIdNotFound() {
        var organization = getMockOrganization();

        when(organizationRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(organization))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Organization not found");

        verify(organizationRepository).findById(anyLong());
        verify(organizationRepository, never()).save(any());
    }

    @Test
    public void shouldUpdateOrganizationUnit() {
        var organizationUnit = getMockOrganizationUnit();

        when(organizationUnitRepository.findById(anyLong())).thenReturn(Optional.of(organizationUnit));
        when(organizationRepository.existsById(anyLong())).thenReturn(true);
        when(organizationUnitRepository.findByOrganization_IdAndSlug(anyLong(), anyString()))
                .thenReturn(Optional.of(organizationUnit));
        when(organizationUnitRepository.save(any())).thenReturn(organizationUnit);

        service.update(organizationUnit);

        verify(organizationUnitRepository).findById(anyLong());
        verify(organizationRepository).existsById(anyLong());
        verify(organizationUnitRepository).findByOrganization_IdAndSlug(anyLong(), anyString());
        verify(organizationUnitRepository).save(any());
    }

    @Test
    public void shouldNotUpdateOrganizationUnitWhenIdNotFound() {
        var organizationUnit = getMockOrganizationUnit();

        when(organizationUnitRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(organizationUnit))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Organization Unit not found");

        verify(organizationUnitRepository).findById(anyLong());
        verify(organizationUnitRepository, never()).save(any());
    }

    @Test
    public void shouldFindOrganizationById() {
        var id = 1L;
        var organization = getMockOrganization();

        when(organizationRepository.findById(anyLong())).thenReturn(Optional.of(organization));

        var result = service.findById(id);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(organization.getId());

        verify(organizationRepository).findById(anyLong());
    }

    @Test
    public void shouldFindOrganizationUnitById() {
        var id = 1L;
        var organizationUnit = getMockOrganizationUnit();
        var responseDto = getMockOrganizationUnitResponseDto();

        when(organizationUnitRepository.findById(anyLong())).thenReturn(Optional.of(organizationUnit));
        when(mapper.organizationUnitToResponseDto(any())).thenReturn(responseDto);

        var result = service.findUnitById(id);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(responseDto.getId());
        assertThat(result.getName()).isEqualTo(responseDto.getName());

        verify(organizationUnitRepository).findById(anyLong());
        verify(mapper).organizationUnitToResponseDto(any());
    }

    @Test
    public void shouldFindOrganizationUnitByOrganizationId() {
        var id = 1L;
        var organizationUnit = getMockOrganizationUnit();
        var responseDto = getMockOrganizationUnitResponseDto();

        when(organizationUnitRepository.findAllByOrganization_Id(anyLong()))
                .thenReturn(List.of(organizationUnit));
        when(mapper.organizationUnitToResponseDto(any()))
                .thenReturn(responseDto);

        var result = service.findAllByOrganizationId(id);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(responseDto.getId());
        assertThat(result.get(0).getName()).isEqualTo(responseDto.getName());

        verify(organizationUnitRepository).findAllByOrganization_Id(anyLong());
        verify(mapper).organizationUnitToResponseDto(any());
    }

    @Test
    public void shouldNotFindOrganizationByIdWhenIdIsNull() {
        assertThatThrownBy(() -> service.findById(null))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Organization id must be informed");

        verify(organizationRepository, never()).findById(anyLong());
    }

    @Test
    public void shouldNotFindOrganizationUnitByIdWhenIdIsNull() {
        assertThatThrownBy(() -> service.findUnitById(null))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Organization unit id must be informed");

        verify(organizationUnitRepository, never()).findById(anyLong());
        verify(mapper, never()).organizationUnitToResponseDto(any());
    }

    @Test
    public void shouldNotFindOrganizationUnitByIdWhenNotFound() {
        when(organizationUnitRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findUnitById(1L))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Organization Unit not found");

        verify(organizationUnitRepository).findById(anyLong());
        verify(mapper, never()).organizationUnitToResponseDto(any());
    }

    @Test
    public void shouldNotFindOrganizationUnitsByOrganizationIdWhenIdIsNull() {
        assertThatThrownBy(() -> service.findAllByOrganizationId(null))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Organization id must be informed");

        verify(organizationUnitRepository, never()).findAllByOrganization_Id(anyLong());
        verify(mapper, never()).organizationUnitToResponseDto(any());
    }

    @Test
    public void shouldNotUpdateOrganizationWhenOrganizationIsNull() {
        assertThatThrownBy(() -> service.update((Organization) null))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Organization must be informed");

        verify(organizationRepository, never()).findById(anyLong());
        verify(organizationRepository, never()).save(any());
    }

    @Test
    public void shouldNotUpdateOrganizationWhenIdIsNull() {
        var organization = getMockOrganization();
        organization.setId(null);

        assertThatThrownBy(() -> service.update(organization))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Organization id must be informed");

        verify(organizationRepository, never()).findById(anyLong());
        verify(organizationRepository, never()).save(any());
    }

    @Test
    public void shouldNotUpdateOrganizationUnitWhenOrganizationUnitIsNull() {
        assertThatThrownBy(() -> service.update((OrganizationUnit) null))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Organization unit must be informed");

        verify(organizationUnitRepository, never()).findById(anyLong());
        verify(organizationUnitRepository, never()).save(any());
    }

    @Test
    public void shouldNotUpdateOrganizationUnitWhenIdIsNull() {
        var organizationUnit = getMockOrganizationUnit();
        organizationUnit.setId(null);

        assertThatThrownBy(() -> service.update(organizationUnit))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Organization unit id must be informed");

        verify(organizationUnitRepository, never()).findById(anyLong());
        verify(organizationUnitRepository, never()).save(any());
    }

    @Test
    public void shouldNotCreateOrganizationWhenNameIsInvalid() {
        var dto = getMockOrganizationDto();
        var organization = getMockOrganization();
        organization.setName(null);

        when(mapper.organizationDtoToOrganization(any())).thenReturn(organization);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Organization name must be informed");

        verify(mapper).organizationDtoToOrganization(any());
        verify(organizationRepository, never()).save(any());
    }

    @Test
    public void shouldNotCreateOrganizationWhenSlugIsInvalid() {
        var dto = getMockOrganizationDto();
        var organization = getMockOrganization();
        organization.setSlug(null);

        when(mapper.organizationDtoToOrganization(any())).thenReturn(organization);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Organization slug must be informed");

        verify(mapper).organizationDtoToOrganization(any());
        verify(organizationRepository, never()).save(any());
    }

    @Test
    public void shouldNotCreateOrganizationWhenDescriptionIsInvalid() {
        var dto = getMockOrganizationDto();
        var organization = getMockOrganization();
        organization.setDescription(null);

        when(mapper.organizationDtoToOrganization(any())).thenReturn(organization);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Organization description must be informed");

        verify(mapper).organizationDtoToOrganization(any());
        verify(organizationRepository, never()).save(any());
    }

    @Test
    public void shouldNotCreateOrganizationWhenLogoUrlIsInvalid() {
        var dto = getMockOrganizationDto();
        var organization = getMockOrganization();
        organization.setLogoUrl(null);

        when(mapper.organizationDtoToOrganization(any())).thenReturn(organization);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Organization logo url must be informed");

        verify(mapper).organizationDtoToOrganization(any());
        verify(organizationRepository, never()).save(any());
    }

    @Test
    public void shouldNotCreateOrganizationWhenActiveIsInvalid() {
        var dto = getMockOrganizationDto();
        var organization = getMockOrganization();
        organization.setActive(null);

        when(mapper.organizationDtoToOrganization(any())).thenReturn(organization);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Organization active status must be informed");

        verify(mapper).organizationDtoToOrganization(any());
        verify(organizationRepository, never()).save(any());
    }

    @Test
    public void shouldNotCreateOrganizationWhenSlugAlreadyExists() {
        var dto = getMockOrganizationDto();
        var organization = getMockOrganization();

        when(mapper.organizationDtoToOrganization(any())).thenReturn(organization);
        when(organizationRepository.existsBySlug(anyString())).thenReturn(true);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Organization slug already exists");

        verify(mapper).organizationDtoToOrganization(any());
        verify(organizationRepository).existsBySlug(anyString());
        verify(organizationRepository, never()).save(any());
    }

    @Test
    public void shouldNotUpdateOrganizationWhenSlugAlreadyExistsForAnotherOrganization() {
        var organization = getMockOrganization();
        var anotherOrganization = getMockOrganization();
        anotherOrganization.setId(2L);

        when(organizationRepository.findById(anyLong())).thenReturn(Optional.of(organization));
        when(organizationRepository.findBySlug(anyString())).thenReturn(Optional.of(anotherOrganization));

        assertThatThrownBy(() -> service.update(organization))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Organization slug already exists");

        verify(organizationRepository).findById(anyLong());
        verify(organizationRepository).findBySlug(anyString());
        verify(organizationRepository, never()).save(any());
    }

    @Test
    public void shouldNotCreateOrganizationUnitWhenOrganizationIsInvalid() {
        var dto = getMockOrganizationUnitDto();
        var organizationUnit = getMockOrganizationUnit();
        organizationUnit.setOrganization(null);

        when(mapper.organizationUnitDtoToOrganizationUnit(any())).thenReturn(organizationUnit);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Organization must be informed");

        verify(mapper).organizationUnitDtoToOrganizationUnit(any());
        verify(organizationUnitRepository, never()).save(any());
    }

    @Test
    public void shouldNotCreateOrganizationUnitWhenOrganizationIdIsInvalid() {
        var dto = getMockOrganizationUnitDto();
        var organizationUnit = getMockOrganizationUnit();
        organizationUnit.getOrganization().setId(null);

        when(mapper.organizationUnitDtoToOrganizationUnit(any())).thenReturn(organizationUnit);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Organization must be informed");

        verify(mapper).organizationUnitDtoToOrganizationUnit(any());
        verify(organizationUnitRepository, never()).save(any());
    }

    @Test
    public void shouldNotCreateOrganizationUnitWhenOrganizationDoesNotExist() {
        var dto = getMockOrganizationUnitDto();
        var organizationUnit = getMockOrganizationUnit();

        when(mapper.organizationUnitDtoToOrganizationUnit(any())).thenReturn(organizationUnit);
        when(organizationRepository.existsById(anyLong())).thenReturn(false);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Organization not found");

        verify(mapper).organizationUnitDtoToOrganizationUnit(any());
        verify(organizationRepository).existsById(anyLong());
        verify(organizationUnitRepository, never()).save(any());
    }

    @Test
    public void shouldNotUpdateOrganizationUnitWhenOrganizationDoesNotExist() {
        var organizationUnit = getMockOrganizationUnit();

        when(organizationUnitRepository.findById(anyLong())).thenReturn(Optional.of(organizationUnit));
        when(organizationRepository.existsById(anyLong())).thenReturn(false);

        assertThatThrownBy(() -> service.update(organizationUnit))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Organization not found");

        verify(organizationUnitRepository).findById(anyLong());
        verify(organizationRepository).existsById(anyLong());
        verify(organizationUnitRepository, never()).save(any());
    }

    @Test
    public void shouldNotCreateOrganizationUnitWhenNameIsInvalid() {
        var dto = getMockOrganizationUnitDto();
        var organizationUnit = getMockOrganizationUnit();
        organizationUnit.setName(null);

        when(mapper.organizationUnitDtoToOrganizationUnit(any())).thenReturn(organizationUnit);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Organization unit name must be informed");

        verify(mapper).organizationUnitDtoToOrganizationUnit(any());
        verify(organizationUnitRepository, never()).save(any());
    }

    @Test
    public void shouldNotCreateOrganizationUnitWhenSlugIsInvalid() {
        var dto = getMockOrganizationUnitDto();
        var organizationUnit = getMockOrganizationUnit();
        organizationUnit.setSlug(null);

        when(mapper.organizationUnitDtoToOrganizationUnit(any())).thenReturn(organizationUnit);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Organization unit slug must be informed");

        verify(mapper).organizationUnitDtoToOrganizationUnit(any());
        verify(organizationUnitRepository, never()).save(any());
    }

    @Test
    public void shouldNotCreateOrganizationUnitWhenDescriptionIsInvalid() {
        var dto = getMockOrganizationUnitDto();
        var organizationUnit = getMockOrganizationUnit();
        organizationUnit.setDescription(null);

        when(mapper.organizationUnitDtoToOrganizationUnit(any())).thenReturn(organizationUnit);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Organization unit description must be informed");

        verify(mapper).organizationUnitDtoToOrganizationUnit(any());
        verify(organizationUnitRepository, never()).save(any());
    }

    @Test
    public void shouldNotCreateOrganizationUnitWhenCityIsInvalid() {
        var dto = getMockOrganizationUnitDto();
        var organizationUnit = getMockOrganizationUnit();
        organizationUnit.setCity(null);

        when(mapper.organizationUnitDtoToOrganizationUnit(any())).thenReturn(organizationUnit);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Organization unit city must be informed");

        verify(mapper).organizationUnitDtoToOrganizationUnit(any());
        verify(organizationUnitRepository, never()).save(any());
    }

    @Test
    public void shouldNotCreateOrganizationUnitWhenStateIsInvalid() {
        var dto = getMockOrganizationUnitDto();
        var organizationUnit = getMockOrganizationUnit();
        organizationUnit.setState(null);

        when(mapper.organizationUnitDtoToOrganizationUnit(any())).thenReturn(organizationUnit);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Organization unit state must be informed");

        verify(mapper).organizationUnitDtoToOrganizationUnit(any());
        verify(organizationUnitRepository, never()).save(any());
    }

    @Test
    public void shouldNotCreateOrganizationUnitWhenCountryIsInvalid() {
        var dto = getMockOrganizationUnitDto();
        var organizationUnit = getMockOrganizationUnit();
        organizationUnit.setCountry(null);

        when(mapper.organizationUnitDtoToOrganizationUnit(any())).thenReturn(organizationUnit);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Organization unit country must be informed");

        verify(mapper).organizationUnitDtoToOrganizationUnit(any());
        verify(organizationUnitRepository, never()).save(any());
    }

    @Test
    public void shouldNotCreateOrganizationUnitWhenAddressIsInvalid() {
        var dto = getMockOrganizationUnitDto();
        var organizationUnit = getMockOrganizationUnit();
        organizationUnit.setAddress(null);

        when(mapper.organizationUnitDtoToOrganizationUnit(any())).thenReturn(organizationUnit);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Organization unit address must be informed");

        verify(mapper).organizationUnitDtoToOrganizationUnit(any());
        verify(organizationUnitRepository, never()).save(any());
    }

    @Test
    public void shouldNotCreateOrganizationUnitWhenContactPhoneIsInvalid() {
        var dto = getMockOrganizationUnitDto();
        var organizationUnit = getMockOrganizationUnit();
        organizationUnit.setContactPhone(null);

        when(mapper.organizationUnitDtoToOrganizationUnit(any())).thenReturn(organizationUnit);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Organization unit contact phone must be informed");

        verify(mapper).organizationUnitDtoToOrganizationUnit(any());
        verify(organizationUnitRepository, never()).save(any());
    }

    @Test
    public void shouldNotCreateOrganizationUnitWhenContactEmailIsInvalid() {
        var dto = getMockOrganizationUnitDto();
        var organizationUnit = getMockOrganizationUnit();
        organizationUnit.setContactEmail(null);

        when(mapper.organizationUnitDtoToOrganizationUnit(any())).thenReturn(organizationUnit);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Organization unit contact email must be informed");

        verify(mapper).organizationUnitDtoToOrganizationUnit(any());
        verify(organizationUnitRepository, never()).save(any());
    }

    @Test
    public void shouldNotCreateOrganizationUnitWhenContactEmailFormatIsInvalid() {
        var dto = getMockOrganizationUnitDto();
        var organizationUnit = getMockOrganizationUnit();
        organizationUnit.setContactEmail("email-invalido");

        when(mapper.organizationUnitDtoToOrganizationUnit(any())).thenReturn(organizationUnit);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Organization unit contact email is invalid");

        verify(mapper).organizationUnitDtoToOrganizationUnit(any());
        verify(organizationUnitRepository, never()).save(any());
    }

    @Test
    public void shouldNotCreateOrganizationUnitWhenActiveIsInvalid() {
        var dto = getMockOrganizationUnitDto();
        var organizationUnit = getMockOrganizationUnit();
        organizationUnit.setActive(null);

        when(mapper.organizationUnitDtoToOrganizationUnit(any())).thenReturn(organizationUnit);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Organization unit active status must be informed");

        verify(mapper).organizationUnitDtoToOrganizationUnit(any());
        verify(organizationUnitRepository, never()).save(any());
    }

    @Test
    public void shouldNotCreateOrganizationUnitWhenSlugAlreadyExistsForOrganization() {
        var dto = getMockOrganizationUnitDto();
        var organizationUnit = getMockOrganizationUnit();

        when(mapper.organizationUnitDtoToOrganizationUnit(any())).thenReturn(organizationUnit);
        when(organizationRepository.existsById(anyLong())).thenReturn(true);
        when(organizationUnitRepository.existsByOrganization_IdAndSlug(anyLong(), anyString())).thenReturn(true);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Organization unit slug already exists for this organization");

        verify(mapper).organizationUnitDtoToOrganizationUnit(any());
        verify(organizationRepository).existsById(anyLong());
        verify(organizationUnitRepository).existsByOrganization_IdAndSlug(anyLong(), anyString());
        verify(organizationUnitRepository, never()).save(any());
    }

    @Test
    public void shouldNotUpdateOrganizationUnitWhenSlugAlreadyExistsForAnotherUnit() {
        var organizationUnit = getMockOrganizationUnit();
        var anotherOrganizationUnit = getMockOrganizationUnit();
        anotherOrganizationUnit.setId(2L);

        when(organizationUnitRepository.findById(anyLong())).thenReturn(Optional.of(organizationUnit));
        when(organizationRepository.existsById(anyLong())).thenReturn(true);
        when(organizationUnitRepository.findByOrganization_IdAndSlug(anyLong(), anyString()))
                .thenReturn(Optional.of(anotherOrganizationUnit));

        assertThatThrownBy(() -> service.update(organizationUnit))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Organization unit slug already exists for this organization");

        verify(organizationUnitRepository).findById(anyLong());
        verify(organizationRepository).existsById(anyLong());
        verify(organizationUnitRepository).findByOrganization_IdAndSlug(anyLong(), anyString());
        verify(organizationUnitRepository, never()).save(any());
    }
}

