package com.br.capoeira.eventos.user_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserReservationEventValidatedMessageDto {
    private Long userId;

    private Long eventSaleId;

    private Long eventId;

    private String eventSaleTransactionId;

    private String description;

    private Integer reservedQuantity;

    private BigDecimal value;
}
