package com.br.capoeira.eventos.user_api.repository;

import com.br.capoeira.eventos.user_api.model.UserFavoriteEvents;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserFavoriteEventsRepository extends JpaRepository<UserFavoriteEvents, Long> {

    List<UserFavoriteEvents> findAllByUserId(Long userId);

    boolean existsByUserIdAndEventId(Long userId, Long eventId);

    void deleteByUserIdAndEventId(Long userId, Long eventId);
}