package com.br.capoeira.eventos.event_api.repository;

import com.br.capoeira.eventos.event_api.model.Event;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface EventRepository extends MongoRepository<Event, Long> {

    Optional<Event> findByTransactionId(String transactionId);

    boolean existsByTransactionId(String transactionId);
}
