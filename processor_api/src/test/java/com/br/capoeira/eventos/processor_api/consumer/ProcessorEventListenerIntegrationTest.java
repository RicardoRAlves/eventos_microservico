package com.br.capoeira.eventos.processor_api.consumer;

import com.br.capoeira.eventos.processor_api.service.ProcessorService;
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

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static unit.com.br.capoeira.eventos.processor_api.service.MockUtils.getMockEvent;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration"
})
@Testcontainers
public class ProcessorEventListenerIntegrationTest {

    @Container
    static RabbitMQContainer rabbitMQ =
            new RabbitMQContainer("rabbitmq:3.12-management");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.host", rabbitMQ::getHost);
        registry.add("spring.rabbitmq.port", rabbitMQ::getAmqpPort);
    }

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RabbitAdmin rabbitAdmin;

    @MockitoBean
    private ProcessorService service;

    @Value("${rabbitmq.create.queue.name}")
    private String createQueueName;

    @Value("${rabbitmq.get-all.queue.name}")
    private String getAllQueueName;

    @Value("${rabbitmq.update.queue.name}")
    private String updateQueueName;

    @BeforeEach
    void setUp() {
    }

    @Test
    public void shouldCreateNewEvent() {
        var createQueue = new Queue(createQueueName, true);
        rabbitAdmin.declareQueue(createQueue);
        var event = getMockEvent();
        event.setTransactionId("1xkdi2393cd");
        rabbitTemplate.convertAndSend(createQueueName, event);

        await().atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() ->
                        verify(service).createNewEvent(
                                argThat(e -> e.getTransactionId().equals("1xkdi2393cd"))
                        )
                );
    }

    @Test
    public void shouldGetAllEvents() {
        var getAllQueue = new Queue(getAllQueueName, true);
        rabbitAdmin.declareQueue(getAllQueue);

        rabbitTemplate.convertAndSend(getAllQueueName, "");

        await().atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() ->
                        verify(service).findAll()
                );
    }

    @Test
    public void shouldUpdateEvent() {
        var updateQueue = new Queue(updateQueueName, true);
        rabbitAdmin.declareQueue(updateQueue);
        var event = getMockEvent();
        event.setId(123L);
        rabbitTemplate.convertAndSend(updateQueueName, event);

        await().atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() ->
                        verify(service).updateEvent(
                                argThat(e -> e.getId().equals(123L))
                        )
                );
    }
}
