package com.br.capoeira.eventos.organization_api.service;

import com.br.capoeira.eventos.organization_api.dto.OrganizationDto;
import com.br.capoeira.eventos.organization_api.dto.OrganizationUnitDto;
import com.br.capoeira.eventos.organization_api.dto.OrganizationUnitResponseDto;
import com.br.capoeira.eventos.organization_api.exception.ValidationException;
import com.br.capoeira.eventos.organization_api.mapper.OrganizationMapper;
import com.br.capoeira.eventos.organization_api.model.Organization;
import com.br.capoeira.eventos.organization_api.model.OrganizationUnit;
import com.br.capoeira.eventos.organization_api.repository.OrganizationRepository;
import com.br.capoeira.eventos.organization_api.repository.OrganizationUnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static org.springframework.util.ObjectUtils.isEmpty;

@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationMapper mapper;
    private final OrganizationRepository organizationRepository;
    private final OrganizationUnitRepository organizationUnitRepository;

    public Optional<Organization> findById(Long id) {
        if (id == null) {
            throw new ValidationException("Organization id must be informed");
        }

        return organizationRepository.findById(id);
    }

    public OrganizationUnitResponseDto findUnitById(Long id) {
        if (id == null) {
            throw new ValidationException("Organization unit id must be informed");
        }

        return organizationUnitRepository.findById(id)
                .map(mapper::organizationUnitToResponseDto)
                .orElseThrow(() -> new ValidationException("Organization Unit not found"));
    }

    public List<OrganizationUnitResponseDto> findAllByOrganizationId(Long organizationId) {
        if (organizationId == null) {
            throw new ValidationException("Organization id must be informed");
        }

        return organizationUnitRepository.findAllByOrganization_Id(organizationId)
                .stream()
                .map(mapper::organizationUnitToResponseDto)
                .toList();
    }

    public Organization create(OrganizationDto dto) {
        var organization = mapper.organizationDtoToOrganization(dto);

        checkOrganization(organization);
        checkOrganizationSlug(organization);

        return organizationRepository.save(organization);
    }

    public OrganizationUnitResponseDto create(OrganizationUnitDto dto) {
        var organizationUnit = mapper.organizationUnitDtoToOrganizationUnit(dto);

        checkOrganizationUnit(organizationUnit);
        checkOrganizationExists(organizationUnit.getOrganization().getId());
        checkOrganizationUnitSlug(organizationUnit);

        return mapper.organizationUnitToResponseDto(
                organizationUnitRepository.save(organizationUnit)
        );
    }

    public Organization update(Organization organization) {
        if (organization == null) {
            throw new ValidationException("Organization must be informed");
        }
        if (organization.getId() == null) {
            throw new ValidationException("Organization id must be informed");
        }

        var savedOrganization = organizationRepository.findById(organization.getId());
        if (savedOrganization.isEmpty()) {
            throw new ValidationException("Organization not found");
        }

        checkOrganization(organization);
        checkOrganizationSlugForUpdate(organization);

        return organizationRepository.save(organization);
    }

    public OrganizationUnitResponseDto update(OrganizationUnit organizationUnit) {
        if (organizationUnit == null) {
            throw new ValidationException("Organization unit must be informed");
        }
        if (organizationUnit.getId() == null) {
            throw new ValidationException("Organization unit id must be informed");
        }

        var savedOrganizationUnit = organizationUnitRepository.findById(organizationUnit.getId());
        if (savedOrganizationUnit.isEmpty()) {
            throw new ValidationException("Organization Unit not found");
        }

        checkOrganizationUnit(organizationUnit);
        checkOrganizationExists(organizationUnit.getOrganization().getId());
        checkOrganizationUnitSlugForUpdate(organizationUnit);

        return mapper.organizationUnitToResponseDto(
                organizationUnitRepository.save(organizationUnit)
        );
    }

    private void checkOrganizationUnit(OrganizationUnit organizationUnit) {
        if (organizationUnit == null) {
            throw new ValidationException("Organization unit must be informed");
        }
        if (organizationUnit.getOrganization() == null || organizationUnit.getOrganization().getId() == null) {
            throw new ValidationException("Organization must be informed");
        }
        if (isEmpty(organizationUnit.getName())) {
            throw new ValidationException("Organization unit name must be informed");
        }
        if (isEmpty(organizationUnit.getSlug())) {
            throw new ValidationException("Organization unit slug must be informed");
        }
        if (isEmpty(organizationUnit.getDescription())) {
            throw new ValidationException("Organization unit description must be informed");
        }
        if (isEmpty(organizationUnit.getCity())) {
            throw new ValidationException("Organization unit city must be informed");
        }
        if (isEmpty(organizationUnit.getState())) {
            throw new ValidationException("Organization unit state must be informed");
        }
        if (isEmpty(organizationUnit.getCountry())) {
            throw new ValidationException("Organization unit country must be informed");
        }
        if (isEmpty(organizationUnit.getAddress())) {
            throw new ValidationException("Organization unit address must be informed");
        }
        if (isEmpty(organizationUnit.getContactPhone())) {
            throw new ValidationException("Organization unit contact phone must be informed");
        }
        if (isEmpty(organizationUnit.getContactEmail())) {
            throw new ValidationException("Organization unit contact email must be informed");
        }
        if (!organizationUnit.getContactEmail().contains("@")) {
            throw new ValidationException("Organization unit contact email is invalid");
        }
        if (organizationUnit.getActive() == null) {
            throw new ValidationException("Organization unit active status must be informed");
        }
    }

    private void checkOrganization(Organization organization) {
        if (organization == null) {
            throw new ValidationException("Organization must be informed");
        }
        if (isEmpty(organization.getName())) {
            throw new ValidationException("Organization name must be informed");
        }
        if (isEmpty(organization.getSlug())) {
            throw new ValidationException("Organization slug must be informed");
        }
        if (isEmpty(organization.getDescription())) {
            throw new ValidationException("Organization description must be informed");
        }
        if (isEmpty(organization.getLogoUrl())) {
            throw new ValidationException("Organization logo url must be informed");
        }
        if (organization.getActive() == null) {
            throw new ValidationException("Organization active status must be informed");
        }
    }

    private void checkOrganizationExists(Long organizationId) {
        if (!organizationRepository.existsById(organizationId)) {
            throw new ValidationException("Organization not found");
        }
    }

    private void checkOrganizationSlug(Organization organization) {
        if (organizationRepository.existsBySlug(organization.getSlug())) {
            throw new ValidationException("Organization slug already exists");
        }
    }

    private void checkOrganizationSlugForUpdate(Organization organization) {
        var savedOrganization = organizationRepository.findBySlug(organization.getSlug());

        if (savedOrganization.isPresent() && !savedOrganization.get().getId().equals(organization.getId())) {
            throw new ValidationException("Organization slug already exists");
        }
    }

    private void checkOrganizationUnitSlug(OrganizationUnit organizationUnit) {
        if (organizationUnitRepository.existsByOrganization_IdAndSlug(
                organizationUnit.getOrganization().getId(),
                organizationUnit.getSlug())) {
            throw new ValidationException("Organization unit slug already exists for this organization");
        }
    }

    private void checkOrganizationUnitSlugForUpdate(OrganizationUnit organizationUnit) {
        var savedOrganizationUnit = organizationUnitRepository.findByOrganization_IdAndSlug(
                organizationUnit.getOrganization().getId(),
                organizationUnit.getSlug());

        if (savedOrganizationUnit.isPresent() &&
                !savedOrganizationUnit.get().getId().equals(organizationUnit.getId())) {
            throw new ValidationException("Organization unit slug already exists for this organization");
        }
    }
}
