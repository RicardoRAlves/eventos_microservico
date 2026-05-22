package com.br.capoeira.eventos.organization_api.service;

import com.br.capoeira.eventos.organization_api.dto.*;
import com.br.capoeira.eventos.organization_api.config.exception.ValidationException;
import com.br.capoeira.eventos.organization_api.mapper.OrganizationMapper;
import com.br.capoeira.eventos.organization_api.model.Organization;
import com.br.capoeira.eventos.organization_api.model.OrganizationUnit;
import com.br.capoeira.eventos.organization_api.repository.OrganizationRepository;
import com.br.capoeira.eventos.organization_api.repository.OrganizationUnitRepository;
import com.br.capoeira.eventos.organization_api.restClient.UserClient;
import com.br.capoeira.eventos.organization_api.service.aws.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

import static org.springframework.util.ObjectUtils.isEmpty;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrganizationService {

    private static final String JOIN_CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int JOIN_CODE_LENGTH = 6;

    private final OrganizationMapper mapper;
    private final OrganizationRepository organizationRepository;
    private final OrganizationUnitRepository organizationUnitRepository;
    private final UserClient userClient;
    private final S3Service s3Service;

    public OrganizationResponseDto findOrganizationById(Long id) {
        if (id == null) {
            throw new ValidationException("Organization id must be informed");
        }

        var organization = organizationRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Organization not found"));

        return mapper.organizationToResponseDto(organization);
    }

    public OrganizationUnitResponseDto findUnitById(Long id) {
        if (id == null) {
            throw new ValidationException("Organization unit id must be informed");
        }

        return organizationUnitRepository.findById(id)
                .map(mapper::organizationUnitToResponseDto)
                .orElseThrow(() -> new ValidationException("Organization Unit not found"));
    }

    public PageResponseDto<OrganizationUnitResponseDto> findAllByOrganizationId(
            Long organizationId,
            int page,
            int size
    ) {
        if (organizationId == null) {
            throw new ValidationException("Organization id must be informed");
        }
        var pageable = PageRequest.of(page, size, Sort.by("id").ascending());

        var organizationPage = organizationUnitRepository.findAllByOrganization_IdOrderByIdAsc(organizationId, pageable);
        var content = organizationPage
                .stream()
                .map(mapper::organizationUnitToResponseDto)
                .toList();

        return new PageResponseDto<>(
                content,
                organizationPage.getNumber(),
                organizationPage.getSize(),
                organizationPage.getTotalElements(),
                organizationPage.getTotalPages(),
                organizationPage.isLast()
        );
    }

    public OrganizationUnitResponseDto findByJoinCode(String code) {
        if (code == null || code.isBlank()) {
            throw new ValidationException("Join code must be informed");
        }

        var unit = organizationUnitRepository.findByJoinCode(code)
                .orElseThrow(() -> new ValidationException("Organization Unit not found"));

        return mapper.organizationUnitToResponseDto(unit);
    }

    @Transactional
    public OrganizationResponseDto createWithMainUnit(OrganizationCreateRequestDto dto) {
        if (dto == null) {
            throw new ValidationException("Organization must be informed");
        }

        var organization = mapper.organizationDtoToOrganization(dto);

        checkOrganization(organization);

        var savedOrganization = organizationRepository.save(organization);

        var mainUnit = buildMainUnit(dto.getMainUnit(), savedOrganization);
        mainUnit.setJoinCode(generateUniqueJoinCode());

        checkOrganizationUnit(mainUnit);

        var savedUnit = organizationUnitRepository.save(mainUnit);

        savedOrganization.setUnits(List.of(savedUnit));

        changeUserRole(
                dto.getUserId(),
                savedOrganization.getId(),
                savedUnit.getId()
        );

        return mapper.organizationToResponseDto(savedOrganization);
    }

    public OrganizationUnitResponseDto create(OrganizationUnitDto dto) {
        if (dto == null) {
            throw new ValidationException("Organization unit must be informed");
        }

        var organization = organizationRepository.findById(dto.getOrganizationId())
                .orElseThrow(() -> new ValidationException("Organization not found"));

        var organizationUnit = mapper.organizationUnitDtoToOrganizationUnit(dto);
        organizationUnit.setOrganization(organization);
        organizationUnit.setJoinCode(generateUniqueJoinCode());

        checkOrganizationUnit(organizationUnit);

        var savedUnit = organizationUnitRepository.save(organizationUnit);

        return mapper.organizationUnitToResponseDto(savedUnit);
    }

    public OrganizationResponseDto update(OrganizationUpdateDto dto) {
        if (dto == null) {
            throw new ValidationException("Organization must be informed");
        }
        if (dto.getId() == null) {
            throw new ValidationException("Organization id must be informed");
        }

        var organization = organizationRepository.findById(dto.getId())
                .orElseThrow(() -> new ValidationException("Organization not found"));

        mapper.updateOrganizationFromDto(dto, organization);

        checkOrganization(organization);

        var savedOrganization = organizationRepository.save(organization);

        return mapper.organizationToResponseDto(savedOrganization);
    }

    public OrganizationUnitResponseDto update(OrganizationUnitUpdateDto dto) {
        if (dto == null) {
            throw new ValidationException("Organization unit must be informed");
        }
        if (dto.getId() == null) {
            throw new ValidationException("Organization unit id must be informed");
        }

        var organizationUnit = organizationUnitRepository.findById(dto.getId())
                .orElseThrow(() -> new ValidationException("Organization Unit not found"));

        mapper.updateOrganizationUnitFromDto(dto, organizationUnit);

        checkOrganizationUnit(organizationUnit);

        var savedUnit = organizationUnitRepository.save(organizationUnit);

        return mapper.organizationUnitToResponseDto(savedUnit);
    }

    public String updatePhoto(MultipartFile file, String organizationName) {
        if (file == null || file.isEmpty()) {
            throw new ValidationException("Image file must be provided");
        }

        if (organizationName == null || organizationName.isBlank()) {
            throw new ValidationException("Organization name must be provided");
        }

        log.info("Uploading photo to S3 for organization={}", organizationName);

        return s3Service
                .uploadOrganizationImage(file, organizationName)
                .toString();
    }

    private OrganizationUnit buildMainUnit(MainUnitDto dto, Organization organization) {
        if (dto == null) {
            throw new ValidationException("Main unit must be informed");
        }

        return OrganizationUnit.builder()
                .organization(organization)
                .name(dto.getName())
                .description(dto.getDescription())
                .city(dto.getCity())
                .state(dto.getState())
                .country(dto.getCountry())
                .address(dto.getAddress())
                .contactPhone(dto.getContactPhone())
                .contactEmail(dto.getContactEmail())
                .active(dto.getActive())
                .build();
    }

    private String generateUniqueJoinCode() {
        String code;
        int attempts = 0;

        do {
            code = generateJoinCode();
            attempts++;

            if (attempts > 20) {
                throw new ValidationException("Could not generate a unique join code");
            }
        } while (organizationUnitRepository.existsByJoinCode(code));

        return code;
    }

    private String generateJoinCode() {
        var sb = new StringBuilder(JOIN_CODE_LENGTH);

        for (int i = 0; i < JOIN_CODE_LENGTH; i++) {
            int index = ThreadLocalRandom.current().nextInt(JOIN_CODE_CHARS.length());
            sb.append(JOIN_CODE_CHARS.charAt(index));
        }

        return sb.toString();
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

    private void changeUserRole(Long userId, Long organizationId, Long unitId){
        log.info("change User Role={}", userId);
        var userRequest = new PromoteToSuperAdminDtoRequest(
                userId,
                organizationId,
                unitId
        );

        var userResponse = userClient.promoteToSuperAdmin(userRequest);

        if (!Objects.equals(userResponse.getId(), userId)
                || !Objects.equals(userResponse.getOrganizationId(), organizationId)
                || !Objects.equals(userResponse.getOrganizationUnitId(), unitId)) {

            throw new ValidationException("User not properly linked to organization");
        }
    }
}