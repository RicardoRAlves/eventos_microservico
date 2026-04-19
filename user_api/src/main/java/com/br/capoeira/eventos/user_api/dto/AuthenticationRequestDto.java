package com.br.capoeira.eventos.user_api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationRequestDto {
    @Email(message = "Invalid email format")
    @NotBlank(message = "User email must be informed")
    private String email;
    @NotBlank(message = "Password must be informed")
    private String password;
}