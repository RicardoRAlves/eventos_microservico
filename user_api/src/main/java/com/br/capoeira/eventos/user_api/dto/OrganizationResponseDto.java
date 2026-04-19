package com.br.capoeira.eventos.user_api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrganizationResponseDto {
    private Long organizationId;
    @JsonProperty("id")
    private Long organizationUnitId;
}