package com.br.capoeira.eventos.event_api.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static unit.com.br.capoeira.eventos.event_api.utils.MockUtils.getMockEvent;

@DataMongoTest
@Testcontainers
public class EventRepositoryIntegrationTest {
    @Container
    @ServiceConnection
    static MongoDBContainer mongoDB = new MongoDBContainer("mongo:6.0");

    @Autowired
    private EventRepository repository;

    @AfterEach
    void tearDown() {
        repository.deleteAll();
    }

    @Test
    void shouldSaveAndFindByTransactionId() {
        var event = getMockEvent();
        event.setTransactionId("txn-123");
        repository.save(event);

        var result = repository.findByTransactionId("txn-123");

        assertThat(result).isPresent();
        assertThat(result.get().getTransactionId()).isEqualTo("txn-123");
    }

    @Test
    void shouldReturnTrueWhenTransactionIdExists() {
        var event = getMockEvent();
        event.setTransactionId("txn-456");
        repository.save(event);

        assertThat(repository.existsByTransactionId("txn-456")).isTrue();
    }

    @Test
    void shouldReturnFalseWhenTransactionIdNotExists() {
        assertThat(repository.existsByTransactionId("nao-existe")).isFalse();
    }

    @Test
    void shouldReturnEmptyWhenTransactionIdNotFound() {
        var result = repository.findByTransactionId("nao-existe");
        assertThat(result).isEmpty();
    }
}
