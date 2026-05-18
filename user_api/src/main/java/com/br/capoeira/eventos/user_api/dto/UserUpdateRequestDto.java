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
public class UserUpdateRequestDto {
    @NotNull(message = "User Id must be informed")
    private Long Id;
    @NotBlank(message = "User name must be informed")
    private String name;
    @NotBlank(message = "Avatar Url must be informed")
    private String avatarUrl;
}
