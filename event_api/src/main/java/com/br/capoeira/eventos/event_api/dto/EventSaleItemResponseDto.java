package com.br.capoeira.eventos.event_api.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventSaleItemResponseDto {

    private String transactionId;

    private String eventTransactionId;

    private String description;

    private Integer quantity;

    private BigDecimal value;

    private Boolean active;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
