package com.br.capoeira.eventos.user_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserReservationEventRequestDto {

    @NotNull(message = "User id is required")
    private Long userId;

    @NotNull(message = "Event Sale id is required")
    private Long eventSaleId;

    @NotBlank(message = "Event sale transaction id is required")
    private String eventSaleTransactionId;
}