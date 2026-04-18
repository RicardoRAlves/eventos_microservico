package com.br.capoeira.eventos.organization_api.mapper;

import com.br.capoeira.eventos.organization_api.dto.OrganizationCreateRequestDto;
import com.br.capoeira.eventos.organization_api.dto.OrganizationResponseDto;
import com.br.capoeira.eventos.organization_api.dto.OrganizationUnitDto;
import com.br.capoeira.eventos.organization_api.dto.OrganizationUnitResponseDto;
import com.br.capoeira.eventos.organization_api.dto.OrganizationUnitSummaryDto;
import com.br.capoeira.eventos.organization_api.dto.OrganizationUnitUpdateDto;
import com.br.capoeira.eventos.organization_api.dto.OrganizationUpdateDto;
import com.br.capoeira.eventos.organization_api.model.Organization;
import com.br.capoeira.eventos.organization_api.model.OrganizationUnit;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface OrganizationMapper {

    Organization organizationDtoToOrganization(OrganizationCreateRequestDto input);

    @Mapping(target = "units", source = "units")
    OrganizationResponseDto organizationToResponseDto(Organization input);

    @Mapping(target = "joinCode", source = "joinCode")
    OrganizationUnitSummaryDto organizationUnitToSummaryDto(OrganizationUnit input);

    @Mapping(target = "organization", source = "organizationId")
    @Mapping(target = "id", ignore = true)
    OrganizationUnit organizationUnitDtoToOrganizationUnit(OrganizationUnitDto input);

    @Mapping(target = "organizationId", source = "organization.id")
    OrganizationUnitResponseDto organizationUnitToResponseDto(OrganizationUnit input);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "units", ignore = true)
    void updateOrganizationFromDto(OrganizationUpdateDto input, @MappingTarget Organization organization);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "organization", ignore = true)
    void updateOrganizationUnitFromDto(OrganizationUnitUpdateDto input, @MappingTarget OrganizationUnit organizationUnit);

    default Organization map(Long organizationId) {
        if (organizationId == null) {
            return null;
        }

        var organization = new Organization();
        organization.setId(organizationId);
        return organization;
    }
}
