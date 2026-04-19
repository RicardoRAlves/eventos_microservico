package com.br.capoeira.eventos.user_api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReactivateUserRequestDto {

    @NotBlank(message = "Email must be informed")
    private String email;

}