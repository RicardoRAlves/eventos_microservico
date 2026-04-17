package com.br.capoeira.eventos.organization_api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrganizationDto {
    private String name;
    private String slug;
    private String description;
    private String logoUrl;
    private Boolean active;
}
