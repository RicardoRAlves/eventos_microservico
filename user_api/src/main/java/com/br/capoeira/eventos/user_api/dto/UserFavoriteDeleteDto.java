package com.br.capoeira.eventos.user_api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserFavoriteDeleteDto {
    @NotNull(message = "User id must be informed")
    private Long userId;

    @NotNull(message = "Event id must be informed")
    private Long eventId;
}
