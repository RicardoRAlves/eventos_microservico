package unit.com.br.capoeira.eventos.event_api.producer;

import com.br.capoeira.eventos.event_api.model.Event;
import com.br.capoeira.eventos.event_api.producer.EventProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static unit.com.br.capoeira.eventos.event_api.utils.MockUtils.getMockEvent;

@ExtendWith(MockitoExtension.class)
public class EventProducerTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private EventProducer producer;

    private final Event event = getMockEvent();

    @BeforeEach
    public void setUp(){
        ReflectionTestUtils.setField(producer, "createExchange", "exchange1");
        ReflectionTestUtils.setField(producer, "getAllExchange", "exchange2");
        ReflectionTestUtils.setField(producer, "createErrorNotificationExchange", "exchange3");
        ReflectionTestUtils.setField(producer, "exchangeUpdate", "exchange4");
    }


    @Test
    public void whenSendingNewEventToProcessorShouldAddToQueue(){
        doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Event.class));
        producer.sendingNewEventToProcessor(event);
        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Event.class));
    }

    @Test
    public void whenSendingErrorCreateEventToNotificationShouldAddToQueue(){
        doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Event.class));
        producer.sendingErrorCreateEventToNotification(event);
        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Event.class));
    }

    @Test
    public void whenAskingForSendingAllEventsShouldAddToQueue(){
        doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), anyString());
        producer.askingForSendingAllEvents();
        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), anyString());
    }

    @Test
    public void whenSendingEventUpdatedToProcessorShouldAddToQueue(){
        doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Event.class));
        producer.sendingEventUpdatedToProcessor(event);
        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Event.class));
    }
}
