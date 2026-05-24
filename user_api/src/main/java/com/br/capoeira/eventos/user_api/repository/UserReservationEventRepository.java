package com.br.capoeira.eventos.user_api.repository;

import com.br.capoeira.eventos.user_api.model.UserReservationEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface UserReservationEventRepository
        extends JpaRepository<UserReservationEvent, Long> {

    List<UserReservationEvent> findAllByUserId(Long userId);

    List<UserReservationEvent> findAllByEventId(Long eventId);

    List<UserReservationEvent> findAllByEventSaleId(Long eventSaleId);

    long countByEventSaleId(Long eventSaleId);

    @Query("""
    SELECT COUNT(DISTINCT r.userId)
    FROM UserReservationEvent r
    WHERE r.eventId = :eventId
""")
    long countDistinctUsersByEventId(
            @Param("eventId") Long eventId
    );

    boolean existsByUserIdAndEventSaleId(
            Long userId,
            Long eventSaleId
    );

    boolean existsByEventSaleTransactionId(
            String eventSaleTransactionId
    );

    @Transactional
    void deleteByUserIdAndEventSaleId(
            Long userId,
            Long eventSaleId
    );

    @Transactional
    @Modifying
    @Query("""
        DELETE FROM UserReservationEvent reservation
        WHERE reservation.eventSaleTransactionId = :eventSaleTransactionId
        """)
    int deleteAllByEventSaleTransactionId(
            @Param("eventSaleTransactionId") String eventSaleTransactionId
    );
}