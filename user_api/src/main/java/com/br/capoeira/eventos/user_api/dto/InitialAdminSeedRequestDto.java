package com.br.capoeira.eventos.user_api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record InitialAdminSeedRequestDto(
        @NotBlank(message = "Initial administrator name must be informed") String name,
        @Email(message = "Invalid initial administrator email")
        @NotBlank(message = "Initial administrator email must be informed") String email,
        @NotBlank(message = "Initial administrator password must be informed") String password,
        @NotBlank(message = "Initial administrator avatar URL must be informed") String avatarUrl
) {
}
