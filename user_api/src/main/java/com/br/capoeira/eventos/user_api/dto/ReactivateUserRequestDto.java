package com.br.capoeira.eventos.user_api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReactivateUserRequestDto {

    @NotNull(message = "Id must be informed")
    private Long id;

}