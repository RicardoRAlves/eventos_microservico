package com.br.capoeira.eventos.user_api.dto;

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
public class UserJoinCodeRequestDto {
    @NotNull(message = "User Id must be informed")
    private Long id;
    @NotBlank(message = "JoinCode must be informed")
    private String joinCode;
}
