package com.br.capoeira.eventos.organization_api.mapper;

import com.br.capoeira.eventos.organization_api.dto.OrganizationDto;
import com.br.capoeira.eventos.organization_api.dto.OrganizationUnitDto;
import com.br.capoeira.eventos.organization_api.dto.OrganizationUnitResponseDto;
import com.br.capoeira.eventos.organization_api.model.Organization;
import com.br.capoeira.eventos.organization_api.model.OrganizationUnit;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring")
@Component
public interface OrganizationMapper {

    Organization organizationDtoToOrganization(OrganizationDto input);

    @Mapping(target = "organization", source = "organizationId")
    @Mapping(target = "id", ignore = true)
    OrganizationUnit organizationUnitDtoToOrganizationUnit(OrganizationUnitDto input);

    default Organization map(Long organizationId) {
        if (organizationId == null) {
            return null;
        }

        var organization = new Organization();
        organization.setId(organizationId);
        return organization;
    }

    @Mapping(target = "organizationId", source = "organization.id")
    OrganizationUnitResponseDto organizationUnitToResponseDto(OrganizationUnit input);
}
