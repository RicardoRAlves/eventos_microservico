package com.br.capoeira.eventos.processor_api.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "event_sale_items",
        indexes = {
                @Index(
                        name = "idx_event_sale_transaction_id",
                        columnList = "transaction_id",
                        unique = true
                ),
                @Index(
                        name = "idx_event_sale_event_transaction_id",
                        columnList = "event_transaction_id"
                ),
                @Index(
                        name = "idx_event_sale_event_id",
                        columnList = "event_id"
                )
        }
)
public class EventSaleItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "transaction_id",
            nullable = false,
            unique = true,
            length = 120
    )
    private String transactionId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "event_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_event_sale_item_event")
    )
    private Event event;

    @Column(
            name = "event_transaction_id",
            nullable = false,
            length = 120
    )
    private String eventTransactionId;

    @Column(
            name = "description",
            nullable = false,
            length = 150
    )
    private String description;

    @Column(
            name = "quantity",
            nullable = false
    )
    private Integer quantity;

    @Column(
            name = "value",
            nullable = false,
            precision = 10,
            scale = 2
    )
    private BigDecimal value;

    @Column(
            name = "active",
            nullable = false
    )
    private Boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        var now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;

        if (active == null) {
            active = true;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}