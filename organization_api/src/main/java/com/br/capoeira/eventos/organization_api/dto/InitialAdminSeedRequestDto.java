package com.br.capoeira.eventos.organization_api.dto;

public record InitialAdminSeedRequestDto(
        String name,
        String email,
        String password,
        String avatarUrl
) {
}
