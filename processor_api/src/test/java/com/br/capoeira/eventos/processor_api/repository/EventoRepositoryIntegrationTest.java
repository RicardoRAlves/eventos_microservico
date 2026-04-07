package com.br.capoeira.eventos.processor_api.repository;

import com.br.capoeira.eventos.processor_api.entities.Event;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
public class EventoRepositoryIntegrationTest {

    @Autowired
    private EventoRepository eventoRepository;

    @Test
    void shouldReturnLatestEventWhenTransactionIdExists() {
        var olderEvent = new Event();
        var title = "Newest Event";
        var id = "tx-123";
        olderEvent.setTransactionId(id);
        olderEvent.setTitle("Old Event");
        olderEvent.setCreateAt(LocalDateTime.now().minusHours(1));

        var latestEvent = new Event();
        latestEvent.setTransactionId(id);
        latestEvent.setTitle(title);
        latestEvent.setCreateAt(LocalDateTime.now());

        eventoRepository.save(olderEvent);
        eventoRepository.save(latestEvent);

        var result = eventoRepository.findTopByTransactionIdOrderByCreateAtDesc(id);

        assertTrue(result.isPresent());
        assertEquals(title, result.get().getTitle());
        assertEquals(id, result.get().getTransactionId());
    }

    @Test
    void shouldReturnEmptyWhenTransactionIdDoesNotExist() {
        var result = eventoRepository.findTopByTransactionIdOrderByCreateAtDesc("tx-inexistente");

        assertTrue(result.isEmpty());
    }
}
