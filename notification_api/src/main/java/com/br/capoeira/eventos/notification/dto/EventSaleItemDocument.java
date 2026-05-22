package com.br.capoeira.eventos.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventSaleItemDocument {

    private Long id;

    private String transactionId;

    private Long eventId;

    private String eventTransactionId;

    private String description;

    private Integer quantity;

    private Double value;

    private Boolean active;

    private Date createdAt;

    private Date updatedAt;
}