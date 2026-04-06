package unit.com.br.capoeira.eventos.event_api.consumer;

import com.br.capoeira.eventos.event_api.consumer.EventErrorListener;
import com.br.capoeira.eventos.event_api.service.EventService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static unit.com.br.capoeira.eventos.event_api.utils.MockUtils.getMockEvent;

@ExtendWith(MockitoExtension.class)
public class EventErrorListenerTest {

    @Mock
    private EventService service;

    @InjectMocks
    private EventErrorListener eventErrorListener;

    @Test
    public void whenErrorCreateEventShouldSendToQueue(){
        var event = getMockEvent();
        doNothing().when(service).sendingCreateErrorToNotification(any());

        eventErrorListener.errorCreateEvent(event);
        verify(service).sendingCreateErrorToNotification(any());
    }
}
