package com.br.capoeira.eventos.event_api.producer;

import com.br.capoeira.eventos.event_api.model.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static unit.com.br.capoeira.eventos.event_api.utils.MockUtils.getMockEvent;

@SpringBootTest
@Testcontainers
public class EventProducerIntegrationTest {

    @Value("${rabbitmq.exchange.create.name}")
    private String createExchange;

    @Value("${rabbitmq.exchange.get-all.name}")
    private String getAllExchange;

    @Value("${rabbitmq.exchange.error.create.notification.name}")
    private String errorNotificationExchange;

    @Value("${rabbitmq.exchange.update.name}")
    private String updateExchange;

    @Container
    static RabbitMQContainer rabbitMQ =
            new RabbitMQContainer("rabbitmq:3.12-management");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.host", rabbitMQ::getHost);
        registry.add("spring.rabbitmq.port", rabbitMQ::getAmqpPort);
    }

    @Autowired
    private EventProducer producer;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RabbitAdmin rabbitAdmin;

    @BeforeEach
    void setUp() {
        declareQueueAndBind(createExchange, "test.queue.create");
        declareQueueAndBind(getAllExchange, "test.queue.get-all");
        declareQueueAndBind(errorNotificationExchange, "test.queue.error");
        declareQueueAndBind(updateExchange, "test.queue.update");
    }

    private void declareQueueAndBind(String exchangeName, String queueName) {
        var exchange = new FanoutExchange(exchangeName);
        var queue = new Queue(queueName, false);
        var binding = BindingBuilder.bind(queue).to(exchange);

        rabbitAdmin.declareExchange(exchange);
        rabbitAdmin.declareQueue(queue);
        rabbitAdmin.declareBinding(binding);
    }

    @Test
    void sendingNewEventToProcessor_shouldDeliverMessageToExchange() {
        var event = getMockEvent();

        producer.sendingNewEventToProcessor(event);

        var received = rabbitTemplate.receiveAndConvert("test.queue.create", 3000);
        assertThat(received).isNotNull().isInstanceOf(Event.class);

        var receivedEvent = (Event) received;
        assertThat(receivedEvent.getTitle()).isEqualTo(event.getTitle());
    }

    @Test
    void askingForSendingAllEvents_shouldDeliverMessageToExchange() {
        producer.askingForSendingAllEvents();

        var received = rabbitTemplate.receiveAndConvert("test.queue.get-all", 3000);
        assertThat(received).isNotNull();
    }

    @Test
    void sendingErrorCreateEventToNotification_shouldDeliverMessageToExchange() {
        var event = getMockEvent();

        producer.sendingErrorCreateEventToNotification(event);

        var received = rabbitTemplate.receiveAndConvert("test.queue.error", 3000);
        assertThat(received).isNotNull().isInstanceOf(Event.class);
    }

    @Test
    void sendingEventUpdatedToProcessor_shouldDeliverMessageToExchange() {
        var event = getMockEvent();

        producer.sendingEventUpdatedToProcessor(event);

        var received = rabbitTemplate.receiveAndConvert("test.queue.update", 3000);
        assertThat(received).isNotNull().isInstanceOf(Event.class);

        var receivedEvent = (Event) received;
        assertThat(receivedEvent.getTitle()).isEqualTo(event.getTitle());
    }
}
