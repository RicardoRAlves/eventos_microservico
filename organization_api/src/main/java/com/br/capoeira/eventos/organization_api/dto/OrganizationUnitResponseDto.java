package com.br.capoeira.eventos.organization_api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrganizationUnitResponseDto {
    private Long id;
    private Long organizationId;
    private String name;
    private String slug;
    private String description;
    private String city;
    private String state;
    private String country;
    private String address;
    private String contactPhone;
    private String contactEmail;
    private Boolean active;
}
