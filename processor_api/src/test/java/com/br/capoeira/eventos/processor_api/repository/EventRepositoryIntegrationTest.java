package com.br.capoeira.eventos.processor_api.repository;

import com.br.capoeira.eventos.processor_api.entities.Category;
import com.br.capoeira.eventos.processor_api.entities.Event;
import com.br.capoeira.eventos.processor_api.entities.enums.EventScope;
import com.br.capoeira.eventos.processor_api.entities.enums.TypeContact;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class EventRepositoryIntegrationTest {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void shouldReturnEventByTransactionId() {
        var transactionId = "tx-123";

        var category = new Category();
        category.setName("Capoeira");
        category.setActive(true);
        category = categoryRepository.saveAndFlush(category);

        var event = buildEvent(transactionId, "Old Event", category);

        assertNull(event.getId());
        assertNotNull(category.getId());

        var savedEvent = eventRepository.saveAndFlush(event);

        var result = eventRepository.findTopByTransactionIdOrderByCreateAtDesc(transactionId);

        assertTrue(result.isPresent());
        assertEquals(savedEvent.getId(), result.get().getId());
        assertEquals("Old Event", result.get().getTitle());
        assertEquals(transactionId, result.get().getTransactionId());
    }

    @Test
    void shouldReturnEmptyWhenTransactionIdDoesNotExist() {
        var result = eventRepository.findTopByTransactionIdOrderByCreateAtDesc("tx-inexistente");

        assertTrue(result.isEmpty());
    }

    private Event buildEvent(String transactionId, String title, Category category) {
        var event = new Event();
        event.setTransactionId(transactionId);
        event.setTitle(title);
        event.setDescription("desc");
        event.setDateStarted(LocalDateTime.now());
        event.setDateFinished(LocalDateTime.now().plusHours(1));
        event.setLocationName("loc");
        event.setAddress("addr");
        event.setTypeContact(TypeContact.INSTAGRAM);
        event.setContact("@instagra");
        event.setActive(true);
        event.setScope(EventScope.PUBLIC);
        event.setCategory(category);
        return event;
    }
}