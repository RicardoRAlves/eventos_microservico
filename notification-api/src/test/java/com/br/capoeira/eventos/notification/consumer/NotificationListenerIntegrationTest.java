package com.br.capoeira.eventos.notification.consumer;

import com.br.capoeira.eventos.notification.model.Event;
import com.br.capoeira.eventos.notification.service.NotificationService;
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

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static unit.com.br.capoeira.eventos.notification.utils.MockUtils.getMockEvent;

@SpringBootTest
@Testcontainers
public class NotificationListenerIntegrationTest {

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

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RabbitAdmin rabbitAdmin;

    @MockitoBean
    private NotificationService service;

    @Value("${rabbitmq.queue.create-notification.name}")
    private String createNotificationName;

    @Value("${rabbitmq.queue.get-all-notification.name}")
    private String getAllNotificationName;

    @Value("${rabbitmq.queue.update-notification.name}")
    private String updateNotificationName;

    @Value("${rabbitmq.queue.error.create.notification.name}")
    private String errorCreateNotificationName;

    @Value("${rabbitmq.queue.update-error-notification.name}")
    private String errorUpdateNotificationName;

    private final Event event = getMockEvent();

    @Test
    public void shouldCreateNewEvent() {
        var createQueue = new Queue(createNotificationName, true);
        rabbitAdmin.declareQueue(createQueue);

        event.setTransactionId("1xkdi2393cd");

        rabbitTemplate.convertAndSend(createNotificationName, event);

        await().atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() ->
                        verify(service).createNewEvent(
                                argThat(e -> e.getTransactionId().equals("1xkdi2393cd"))
                        )
                );
    }

    @Test
    public void shouldGetAllEvents() {
        var getAllQueue = new Queue(getAllNotificationName, true);
        rabbitAdmin.declareQueue(getAllQueue);

        var events = List.of(event);

        rabbitTemplate.convertAndSend(getAllNotificationName, events);

        await().atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() ->
                        verify(service).getAllEvents(
                                argThat(list ->
                                        list.size() == 1
                                )
                        )
                );
    }

    @Test
    public void shouldUpdateEvent() {
        var updateQueue = new Queue(updateNotificationName, true);
        rabbitAdmin.declareQueue(updateQueue);

        event.setId(123L);

        rabbitTemplate.convertAndSend(updateNotificationName, event);

        await().atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() ->
                        verify(service).updateEvent(
                                argThat(e -> e.getId().equals(123L))
                        )
                );
    }

    @Test
    public void shouldNotCreateEvent() {
        var createErrorQueue = new Queue(errorCreateNotificationName, true);
        rabbitAdmin.declareQueue(createErrorQueue);

        event.setId(123L);

        rabbitTemplate.convertAndSend(errorCreateNotificationName, event);

        await().atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() ->
                        verify(service).createErrorEvent(
                                argThat(e -> e.getId().equals(123L))
                        )
                );
    }

    @Test
    public void shouldNotUpdateEvent() {
        var createErrorQueue = new Queue(errorUpdateNotificationName, true);
        rabbitAdmin.declareQueue(createErrorQueue);

        event.setId(123L);

        rabbitTemplate.convertAndSend(errorUpdateNotificationName, event);

        await().atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() ->
                        verify(service).updateErrorEvent(
                                argThat(e -> e.getId().equals(123L))
                        )
                );
    }
}
