package com.br.capoeira.eventos.notification.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventSaleItemRequestDto {
    private Long id;

    private String transactionId;

    private Long eventId;

    private String eventTransactionId;

    private String description;

    private Integer quantity;

    private BigDecimal value;

    private Boolean active;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
