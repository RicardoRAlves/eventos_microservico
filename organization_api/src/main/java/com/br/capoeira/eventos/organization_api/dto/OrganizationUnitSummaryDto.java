package com.br.capoeira.eventos.organization_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationUnitSummaryDto {

    private Long id;
    private String name;
    private String slug;
    private String city;
    private String state;
    private String country;
    private String joinCode;
    private Boolean active;
}
