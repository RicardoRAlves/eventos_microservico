package com.br.capoeira.eventos.user_api.dto;

import com.br.capoeira.eventos.user_api.enums.Role;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserChangeRoleRequestDto {
    @NotNull(message = "User Id must be informed")
    private Long Id;
    @NotNull(message = "Role must be informed")
    private Role role;
}
