package com.br.capoeira.eventos.banco_api.repository;

import com.br.capoeira.eventos.banco_api.entities.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventoRepository extends JpaRepository<Event, Long> {
}
