package com.br.capoeira.eventos.user_api.repository;

import com.br.capoeira.eventos.user_api.model.UserFavoriteEvents;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserFavoriteEventsRepository
        extends JpaRepository<UserFavoriteEvents, Long> {

    List<UserFavoriteEvents> findAllByUserId(Long userId);

    boolean existsByUserIdAndEventId(Long userId, Long eventId);

    void deleteByUserIdAndEventId(Long userId, Long eventId);

    boolean existsByEventTransactionId(String eventTransactionId);

    @Modifying
    @Query("""
            DELETE FROM UserFavoriteEvents favorite
            WHERE favorite.eventTransactionId = :eventTransactionId
            """)
    int deleteAllByEventTransactionId(
            @Param("eventTransactionId") String eventTransactionId
    );
}