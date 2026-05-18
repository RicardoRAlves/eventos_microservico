package com.br.capoeira.eventos.organization_api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDto {
    private Long id;
    private String name;
    private String email;
    private Role role;
    private Boolean active;
    private Long organizationId;
    private Long organizationUnitId;
    private String avatarUrl;
}