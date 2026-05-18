package com.br.capoeira.eventos.user_api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserReservationEventDeleteRequestDto {

    @NotNull(message = "User id is required")
    private Long userId;

    @NotNull(message = "Event sale id is required")
    private Long eventSaleId;
}
