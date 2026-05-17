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
import org.springframework.test.context.ActiveProfiles;
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
import static unit.com.br.capoeira.eventos.event_api.utils.MockUtils.getMockEventResponseDto;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = {
                "security.jwt.secret=U2VncmVkby1NdWl0by1Gb3J0ZS1QYXJhLU9zLVRlc3Rlcy1Db20tMzItQnl0ZXM=",
                "organization.api.url.base=http://localhost:8081"
        }
)
@Testcontainers
@ActiveProfiles("test")
class EventErrorListenerIntegrationTest {

    @Container
    static RabbitMQContainer rabbitMQ =
            new RabbitMQContainer("rabbitmq:3.12-management");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.host", rabbitMQ::getHost);
        registry.add("spring.rabbitmq.port", rabbitMQ::getAmqpPort);
        registry.add("spring.rabbitmq.username", rabbitMQ::getAdminUsername);
        registry.add("spring.rabbitmq.password", rabbitMQ::getAdminPassword);
    }

    @Value("${rabbitmq.create.error.queue.name}")
    private String errorQueueName;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RabbitAdmin rabbitAdmin;

    @MockitoBean
    private EventService eventService;

    @BeforeEach
    void setUp() {
        rabbitAdmin.declareQueue(new Queue(errorQueueName, true));
    }

    @Test
    void errorCreateEvent_shouldCallSendingCreateErrorToNotification() {
        var eventResponseDto = getMockEventResponseDto();
        eventResponseDto.setTransactionId("1xkdi2393cd");

        rabbitTemplate.convertAndSend(errorQueueName, eventResponseDto);

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