package com.br.capoeira.eventos.processor_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserReservationEventRequestDto {

    private Long userId;

    private Long eventSaleId;

    private String eventSaleTransactionId;
}