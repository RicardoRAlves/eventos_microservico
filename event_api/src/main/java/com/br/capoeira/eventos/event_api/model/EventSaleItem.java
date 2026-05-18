package com.br.capoeira.eventos.event_api.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "event_sale_items")
public class EventSaleItem {

    @Id
    private String id;

    @Indexed(unique = true)
    private String transactionId;

    @Indexed
    private String eventTransactionId;

    private String description;

    private Integer quantity;

    private BigDecimal value;

    private Boolean active;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}