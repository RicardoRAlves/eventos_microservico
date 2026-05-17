package com.br.capoeira.eventos.user_api.model;

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
        name = "user_reservation_events",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_reservation_event",
                        columnNames = {
                                "user_id",
                                "event_sale_id"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_user_reservation_event_sale_transaction_id",
                        columnList = "event_sale_transaction_id"
                ),
                @Index(
                        name = "idx_user_reservation_user_id",
                        columnList = "user_id"
                ),
                @Index(
                        name = "idx_user_reservation_event_id",
                        columnList = "event_id"
                ),
                @Index(
                        name = "idx_user_reservation_event_sale_id",
                        columnList = "event_sale_id"
                )
        }
)
public class UserReservationEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "event_sale_id", nullable = false)
    private Long eventSaleId;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "event_sale_transaction_id", nullable = false)
    private String eventSaleTransactionId;

    @Column(name = "description", nullable = false, length = 150)
    private String description;

    @Column(name = "reserved_quantity", nullable = false)
    private Integer reservedQuantity;

    @Column(name = "value", nullable = false, precision = 10, scale = 2)
    private BigDecimal value;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}