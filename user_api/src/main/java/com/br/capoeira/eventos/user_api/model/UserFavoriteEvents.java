package com.br.capoeira.eventos.user_api.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "user_favorite_events",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_favorite_event",
                        columnNames = {
                                "user_id",
                                "event_id"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_event_transaction_id",
                        columnList = "event_transaction_id"
                )
        }
)
public class UserFavoriteEvents {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "event_transaction_id", nullable = false)
    private String eventTransactionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_scope", nullable = false)
    private EventScope eventScope;

    @Column(name = "organization_id")
    private Long organizationId;

    @Column(name = "organization_unit_id")
    private Long organizationUnitId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
