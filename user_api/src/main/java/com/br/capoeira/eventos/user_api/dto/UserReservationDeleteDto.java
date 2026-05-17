package com.br.capoeira.eventos.user_api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserReservationDeleteDto {
    @NotNull(message = "User id must be informed")
    private Long userId;

    @NotNull(message = "Event Sale id must be informed")
    private Long eventSaleId;
}
