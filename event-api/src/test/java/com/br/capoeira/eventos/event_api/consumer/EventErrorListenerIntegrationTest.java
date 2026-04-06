package com.br.capoeira.eventos.event_api.consumer;

import com.br.capoeira.eventos.event_api.service.EventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatNoException;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static unit.com.br.capoeira.eventos.event_api.utils.MockUtils.getMockEvent;

@SpringBootTest
@Testcontainers
public class EventErrorListenerIntegrationTest {

    @Container
    static RabbitMQContainer rabbitMQ =
            new RabbitMQContainer("rabbitmq:3.12-management");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.host", rabbitMQ::getHost);
        registry.add("spring.rabbitmq.port", rabbitMQ::getAmqpPort);
    }

    @Value("${rabbitmq.create.error.queue.name}")
    private String errorQueueName;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RabbitAdmin rabbitAdmin;

    @MockitoBean
    private EventService eventService; // mock para verificar se foi chamado

    @BeforeEach
    void setUp() {
        // declara a fila que o @RabbitListener está escutando
        var queue = new Queue(errorQueueName, true);
        rabbitAdmin.declareQueue(queue);
    }

    @Test
    void errorCreateEvent_shouldCallSendingCreateErrorToNotification() throws InterruptedException {
        var event = getMockEvent();
        event.setTransactionId("1xkdi2393cd");

        // envia a mensagem para a fila que o listener está escutando
        rabbitTemplate.convertAndSend(errorQueueName, event);

        // aguarda o listener processar — ele é assíncrono
        await().atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() ->
                        verify(eventService).sendingCreateErrorToNotification(
                                argThat(e -> e.getTransactionId().equals("1xkdi2393cd"))
                        )
                );
    }

    @Test
    void errorCreateEvent_shouldLogAndNotThrow_whenEventHasNoTransactionId() {
        var event = getMockEvent();
        event.setTransactionId(null);

        assertThatNoException().isThrownBy(() ->
                rabbitTemplate.convertAndSend(errorQueueName, event)
        );

        await().atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() ->
                        verify(eventService).sendingCreateErrorToNotification(any())
                );
    }
}
