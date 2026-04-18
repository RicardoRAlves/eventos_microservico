package com.br.capoeira.eventos.organization_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationResponseDto {

    private Long id;
    private String name;
    private String slug;
    private String description;
    private String logoUrl;
    private Boolean active;

    private List<OrganizationUnitSummaryDto> units;
}
