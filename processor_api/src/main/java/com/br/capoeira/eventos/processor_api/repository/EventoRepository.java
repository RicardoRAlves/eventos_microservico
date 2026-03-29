package com.br.capoeira.eventos.processor_api.repository;

import com.br.capoeira.eventos.processor_api.entities.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EventoRepository extends JpaRepository<Event, Long> {

    Optional<Event> findTopByTransactionIdOrderByCreateAtDesc(String transactionId);

}
