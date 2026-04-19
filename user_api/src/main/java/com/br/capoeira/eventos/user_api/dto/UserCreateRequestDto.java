package com.br.capoeira.eventos.user_api.dto;

import com.br.capoeira.eventos.user_api.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateRequestDto {
    @NotBlank(message = "User name must be informed")
    private String name;
    @Email(message = "Invalid email format")
    @NotBlank(message = "User email must be informed")
    private String email;
    @NotBlank(message = "Password must be informed")
    private String password;
    @NotNull(message = "Role must be informed")
    private Role role;
    @NotBlank(message = "Avatar Url must be informed")
    private String avatarUrl;
    private String joinCode;
}
