package com.br.capoeira.eventos.organization_api.seed;

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
import com.br.capoeira.eventos.organization_api.service.JoinCodeGenerator;
import com.br.capoeira.eventos.organization_api.service.OrganizationKeyNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class BonfimSeedService {

    private static final long SEED_LOCK_ID = 2_026_111_402L;
    private static final int DESCRIPTION_MAX_LENGTH = 1000;

    private final BonfimSeedProperties properties;
    private final BonfimSeedManifestLoader manifestLoader;
    private final OrganizationKeyNormalizer organizationKeyNormalizer;
    private final OrganizationRepository organizationRepository;
    private final OrganizationUnitRepository organizationUnitRepository;
    private final JoinCodeGenerator joinCodeGenerator;
    private final UserClient userClient;
    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public BonfimSeedResult seed() {
        BonfimSeedManifest manifest = manifestLoader.load(properties.getResource());
        validateConfiguration(manifest);

        jdbcTemplate.execute("SELECT pg_advisory_xact_lock(" + SEED_LOCK_ID + ")");

        UserResponseDto admin = userClient.ensureInitialAdmin(new InitialAdminSeedRequestDto(
                properties.getAdminName(),
                properties.getAdminEmail(),
                properties.getAdminPassword(),
                properties.getAdminAvatarUrl()
        ));
        validateAdminResponse(admin);

        String organizationKey = organizationKeyNormalizer.normalize(properties.getOrganizationName());
        var organizationLookup = organizationRepository.findByOrganizationKey(organizationKey);
        boolean organizationCreated = organizationLookup.isEmpty();
        Organization organization = organizationLookup.orElseGet(() -> createOrganization(manifest, organizationKey));

        var unitLookup = organizationUnitRepository.findByOrganization_IdAndName(
                organization.getId(),
                properties.getOrganizationUnitName()
        );
        boolean unitCreated = unitLookup.isEmpty();
        OrganizationUnit unit = unitLookup.orElseGet(() -> createUnit(manifest, organization));

        if (!StringUtils.hasText(unit.getJoinCode())) {
            unit.setJoinCode(joinCodeGenerator.generateUnique());
            unit = organizationUnitRepository.saveAndFlush(unit);
        }

        boolean userLinkUpdated = admin.getRole() != Role.SUPER_ADMIN
                || !Objects.equals(admin.getOrganizationId(), organization.getId())
                || !Objects.equals(admin.getOrganizationUnitId(), unit.getId());

        if (userLinkUpdated) {
            UserResponseDto linkedAdmin = userClient.promoteToSuperAdmin(new PromoteToSuperAdminDtoRequest(
                    admin.getId(),
                    organization.getId(),
                    unit.getId()
            ));
            validateLinkedAdmin(linkedAdmin, admin.getId(), organization.getId(), unit.getId());
        }

        return new BonfimSeedResult(
                admin.getId(),
                organization.getId(),
                unit.getId(),
                organizationCreated,
                unitCreated,
                userLinkUpdated
        );
    }

    private Organization createOrganization(BonfimSeedManifest manifest, String organizationKey) {
        Organization organization = Organization.builder()
                .name(properties.getOrganizationName())
                .description(manifest.organization().description())
                .logoUrl(properties.getOrganizationLogoUrl())
                .active(true)
                .build();
        organization.assignOrganizationKey(organizationKey);
        return organizationRepository.saveAndFlush(organization);
    }

    private OrganizationUnit createUnit(BonfimSeedManifest manifest, Organization organization) {
        OrganizationUnit unit = OrganizationUnit.builder()
                .organization(organization)
                .name(properties.getOrganizationUnitName())
                .description(manifest.unit().description())
                .city(manifest.unit().city())
                .state(manifest.unit().state())
                .country(manifest.unit().country())
                .address(manifest.unit().address())
                .contactPhone(properties.getOrganizationUnitContactPhone())
                .contactEmail(manifest.unit().contactEmail())
                .active(true)
                .joinCode(joinCodeGenerator.generateUnique())
                .build();
        return organizationUnitRepository.saveAndFlush(unit);
    }

    private void validateConfiguration(BonfimSeedManifest manifest) {
        List<String> missing = new ArrayList<>();
        requireText(properties.getResource(), "INITIAL_DATA_SEED_RESOURCE", missing);
        requireText(properties.getAdminName(), "INITIAL_ADMIN_NAME", missing);
        requireText(properties.getAdminEmail(), "INITIAL_ADMIN_EMAIL", missing);
        requireText(properties.getAdminPassword(), "INITIAL_ADMIN_PASSWORD", missing);
        requireText(properties.getAdminAvatarUrl(), "INITIAL_ADMIN_AVATAR_URL", missing);
        requireText(properties.getOrganizationName(), "INITIAL_ORGANIZATION_NAME", missing);
        requireText(properties.getOrganizationLogoUrl(), "INITIAL_ORGANIZATION_LOGO_URL", missing);
        requireText(properties.getOrganizationUnitName(), "INITIAL_ORGANIZATION_UNIT_NAME", missing);
        requireText(
                properties.getOrganizationUnitContactPhone(),
                "INITIAL_ORGANIZATION_UNIT_CONTACT_PHONE",
                missing
        );

        if (!missing.isEmpty()) {
            throw new BonfimSeedException(
                    "Missing required initial seed configuration: " + String.join(", ", missing)
            );
        }
        if (manifest == null || manifest.organization() == null || manifest.unit() == null) {
            throw new BonfimSeedException("Bonfim seed resource is incomplete");
        }
        if (!properties.getAdminEmail().contains("@")) {
            throw new BonfimSeedException("INITIAL_ADMIN_EMAIL is invalid");
        }
        validateHttpUrl(properties.getAdminAvatarUrl(), "INITIAL_ADMIN_AVATAR_URL");
        validateHttpUrl(properties.getOrganizationLogoUrl(), "INITIAL_ORGANIZATION_LOGO_URL");
        if (!manifest.organization().name().equals(properties.getOrganizationName())) {
            throw new BonfimSeedException("INITIAL_ORGANIZATION_NAME does not match the versioned seed");
        }
        if (!manifest.unit().name().equals(properties.getOrganizationUnitName())) {
            throw new BonfimSeedException("INITIAL_ORGANIZATION_UNIT_NAME does not match the versioned seed");
        }

        String normalizedKey = organizationKeyNormalizer.normalize(properties.getOrganizationName());
        if (!normalizedKey.equals(manifest.organization().expectedKey())) {
            throw new BonfimSeedException("The official organization name does not generate the expected key");
        }
        if (manifest.organization().description().length() > DESCRIPTION_MAX_LENGTH
                || manifest.unit().description().length() > DESCRIPTION_MAX_LENGTH) {
            throw new BonfimSeedException("Bonfim seed description exceeds the supported schema length");
        }
        requireManifestText(manifest.source(), "source");
        requireManifestText(manifest.organization().description(), "organization.description");
        requireManifestText(manifest.unit().description(), "unit.description");
        requireManifestText(manifest.unit().city(), "unit.city");
        requireManifestText(manifest.unit().state(), "unit.state");
        requireManifestText(manifest.unit().country(), "unit.country");
        requireManifestText(manifest.unit().address(), "unit.address");
        requireManifestText(manifest.unit().contactEmail(), "unit.contactEmail");
        requireManifestText(manifest.unit().documentedResponsible(), "unit.documentedResponsible");
        if (!manifest.unit().contactDataRequiresCurrentConfirmation()) {
            throw new BonfimSeedException("Documented contact data must retain its confirmation warning");
        }
    }

    private void validateAdminResponse(UserResponseDto admin) {
        if (admin == null || admin.getId() == null || !properties.getAdminEmail().equals(admin.getEmail())) {
            throw new BonfimSeedException("User service returned an invalid initial administrator");
        }
    }

    private void validateLinkedAdmin(
            UserResponseDto admin,
            Long userId,
            Long organizationId,
            Long unitId
    ) {
        if (admin == null
                || !Objects.equals(userId, admin.getId())
                || admin.getRole() != Role.SUPER_ADMIN
                || !Objects.equals(organizationId, admin.getOrganizationId())
                || !Objects.equals(unitId, admin.getOrganizationUnitId())) {
            throw new BonfimSeedException("Initial administrator was not linked to the Bonfim organization");
        }
    }

    private void validateHttpUrl(String value, String propertyName) {
        try {
            URI uri = URI.create(value);
            if (!("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))
                    || !StringUtils.hasText(uri.getHost())) {
                throw new IllegalArgumentException();
            }
        } catch (IllegalArgumentException e) {
            throw new BonfimSeedException(propertyName + " must be an absolute HTTP(S) URL");
        }
    }

    private void requireText(String value, String propertyName, List<String> missing) {
        if (!StringUtils.hasText(value)) {
            missing.add(propertyName);
        }
    }

    private void requireManifestText(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new BonfimSeedException("Bonfim seed resource is missing " + fieldName);
        }
    }
}
