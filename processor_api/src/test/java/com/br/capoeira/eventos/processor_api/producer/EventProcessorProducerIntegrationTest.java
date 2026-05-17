package com.br.capoeira.eventos.processor_api.producer;

import com.br.capoeira.eventos.processor_api.consumer.ProcessorEventListener;
import com.br.capoeira.eventos.processor_api.consumer.ProcessorEventSaleListener;
import com.br.capoeira.eventos.processor_api.dto.EventRequestDto;
import com.br.capoeira.eventos.processor_api.dto.EventResponseDto;
import com.br.capoeira.eventos.processor_api.repository.EventSaleItemRepository;
import com.br.capoeira.eventos.processor_api.service.EventProcessorService;
import com.br.capoeira.eventos.processor_api.service.EventSaleProcessorService;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static unit.com.br.capoeira.eventos.processor_api.service.MockUtils.getMockEventRequestDto;
import static unit.com.br.capoeira.eventos.processor_api.service.MockUtils.getMockEventResponseDto;

@SpringBootTest(
        properties = {
                "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration"
        }
)
@Testcontainers
@ActiveProfiles("test")
public class EventProcessorProducerIntegrationTest {

    @Value("${rabbitmq.exchange.create-notification.name}")
    private String createNotificationExchange;

    @Value("${rabbitmq.exchange.error.create.name}")
    private String errorCreateExchange;

    @Value("${rabbitmq.exchange.get-all-notification.name}")
    private String getAllNotificationExchange;

    @Value("${rabbitmq.exchange.update-notification.name}")
    private String updateNotificationExchange;

    @Value("${rabbitmq.exchange.update-error-notification.name}")
    private String updateErrorNotificationExchange;

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

    @Autowired
    private EventProcessorProducer producer;

    @MockitoBean
    private ProcessorEventListener processorEventListener;

    @MockitoBean
    private EventProcessorService eventProcessorService;

    @MockitoBean
    private EventSaleProcessorService eventSaleProcessorService;

    @MockitoBean
    private EventSaleItemRepository eventSaleItemRepository;

    @MockitoBean
    private ProcessorEventSaleListener processorEventSaleListener;

    private final String createQueueName = "test.queue.create";

    private final String getAllQueueName = "test.queue.get-all";

    private final String errorCreateQueueName = "test.queue.error-create";

    private final String updateQueueName = "test.queue.update";

    private final String errorUpdateQueueName = "test.queue.update-error";

    @BeforeEach
    void setUp() {
        declareQueueAndBind(createNotificationExchange, createQueueName );
        declareQueueAndBind(getAllNotificationExchange, getAllQueueName);
        declareQueueAndBind(errorCreateExchange, errorCreateQueueName);
        declareQueueAndBind(updateNotificationExchange, updateQueueName);
        declareQueueAndBind(updateErrorNotificationExchange, errorUpdateQueueName);
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
    void shouldCreateNewEvent() {
        var event = getMockEventResponseDto();

        producer.sendEventForSuccessQueue(event);

        var received = rabbitTemplate.receiveAndConvert(createQueueName, 3000);
        assertThat(received).isNotNull().isInstanceOf(EventResponseDto.class);

        var receivedEvent = (EventResponseDto) received;
        assertThat(receivedEvent.getTitle()).isEqualTo(event.getTitle());
    }

    @Test
    void shouldGetAllEvents() {
        var event = getMockEventResponseDto();

        producer.sendAllEvents(List.of(event));

        var received = rabbitTemplate.receiveAndConvert(getAllQueueName, 3000);
        assertThat(received).isNotNull().isInstanceOf(List.class);

        var receivedEvent = (List) received;
        assertThat(!receivedEvent.isEmpty());
    }

    @Test
    void shouldUpdateEvent() {
        var event = getMockEventResponseDto();

        producer.sendEventForUpdateQueue(event);

        var received = rabbitTemplate.receiveAndConvert(updateQueueName, 3000);
        assertThat(received).isNotNull().isInstanceOf(EventResponseDto.class);

        var receivedEvent = (EventResponseDto) received;
        assertThat(receivedEvent.getTitle()).isEqualTo(event.getTitle());
    }

    @Test
    void shouldSendCreateErrorEvent() {
        var event = getMockEventRequestDto();

        producer.sendEventForFailQueue(event);

        var received = rabbitTemplate.receiveAndConvert(errorCreateQueueName, 3000);
        assertThat(received).isNotNull().isInstanceOf(EventRequestDto.class);

        var receivedEvent = (EventRequestDto) received;
        assertThat(receivedEvent.getTitle()).isEqualTo(event.getTitle());
    }

    @Test
    void shouldSendUpdateErrorEvent() {
        var event = getMockEventRequestDto();

        producer.sendEventForUpdateErrorQueue(event);

        var received = rabbitTemplate.receiveAndConvert(errorUpdateQueueName, 3000);
        assertThat(received).isNotNull().isInstanceOf(EventRequestDto.class);

        var receivedEvent = (EventRequestDto) received;
        assertThat(receivedEvent.getTitle()).isEqualTo(event.getTitle());
    }
}
