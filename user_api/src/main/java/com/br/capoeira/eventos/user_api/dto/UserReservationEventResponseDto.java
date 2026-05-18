package com.br.capoeira.eventos.user_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserReservationEventResponseDto {

    private Long id;

    private Long userId;

    private Long eventSaleId;

    private String eventSaleTransactionId;

    private String description;

    private Integer quantity;

    private BigDecimal value;

    private LocalDateTime createdAt;
}
