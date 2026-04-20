package unit.com.br.capoeira.eventos.processor_api.producer;

import com.br.capoeira.eventos.processor_api.dto.EventRequestDto;
import com.br.capoeira.eventos.processor_api.dto.EventResponseDto;
import com.br.capoeira.eventos.processor_api.producer.ProcessorProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static unit.com.br.capoeira.eventos.processor_api.service.MockUtils.getMockEventRequestDto;
import static unit.com.br.capoeira.eventos.processor_api.service.MockUtils.getMockEventResponseDto;

@ExtendWith(MockitoExtension.class)
public class ProcessorProducerTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private ProcessorProducer producer;

    private final EventResponseDto event = getMockEventResponseDto();
    private final EventRequestDto eventRequest = getMockEventRequestDto();

    @BeforeEach
    public void setUp(){
        ReflectionTestUtils.setField(producer, "createNotificationExchange", "exchange1");
        ReflectionTestUtils.setField(producer, "errorCreateExchange", "exchange2");
        ReflectionTestUtils.setField(producer, "getAllNotificationExchange", "exchange3");
        ReflectionTestUtils.setField(producer, "updateNotificationExchange", "exchange4");
        ReflectionTestUtils.setField(producer, "updateErrorNotificationExchange", "exchange5");
    }

    @Test
    public void shouldSendToSuccessQueue(){
        doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(EventResponseDto.class));
        producer.sendEventForSuccessQueue(event);
        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), any(EventResponseDto.class));
    }

    @Test
    public void shouldSendToAllEventsQueue(){
        doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(List.class));
        producer.sendAllEvents(List.of(event));
        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), any(List.class));
    }

    @Test
    public void shouldSendToUpdateQueue(){
        doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(EventResponseDto.class));
        producer.sendEventForUpdateQueue(event);
        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), any(EventResponseDto.class));
    }

    @Test
    public void shouldSendToUpdateErrorQueue(){
        doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(EventRequestDto.class));
        producer.sendEventForUpdateErrorQueue(eventRequest);
        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), any(EventRequestDto.class));
    }

    @Test
    public void shouldSendToFailQueue(){
        doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(EventRequestDto.class));
        producer.sendEventForFailQueue(eventRequest);
        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), any(EventRequestDto.class));
    }
}
