package com.br.capoeira.eventos.event_api.repository;

import com.br.capoeira.eventos.event_api.model.EventSaleItem;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface EventSaleItemRepository extends MongoRepository<EventSaleItem, String> {

    Optional<EventSaleItem> findByTransactionId(String transactionId);

    boolean existsByTransactionId(String transactionId);
}
