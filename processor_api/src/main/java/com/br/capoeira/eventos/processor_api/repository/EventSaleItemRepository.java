package com.br.capoeira.eventos.processor_api.repository;

import com.br.capoeira.eventos.processor_api.entities.EventSaleItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EventSaleItemRepository
        extends JpaRepository<EventSaleItem, Long> {

    Optional<EventSaleItem> findByTransactionId(
            String transactionId
    );

    boolean existsByTransactionId(
            String transactionId
    );
}
