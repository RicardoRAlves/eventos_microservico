package unit.com.br.capoeira.eventos.notification.consumer;

import com.br.capoeira.eventos.notification.consumer.NotificationListener;
import com.br.capoeira.eventos.notification.model.Event;
import com.br.capoeira.eventos.notification.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static unit.com.br.capoeira.eventos.notification.utils.MockUtils.getMockEvent;

@ExtendWith(MockitoExtension.class)
public class NotificationListenerTest {

    @Mock
    private NotificationService service;

    @InjectMocks
    private NotificationListener listener;

    private final Event event = getMockEvent();

    @Test
    public void shouldSaveNewEvent(){

        doNothing().when(service).createNewEvent(any());

        listener.saveEvent(event);

        verify(service).createNewEvent(any());
    }

    @Test
    public void shouldGetAllEvents(){

        doNothing().when(service).getAllEvents(any());

        listener.getAllEvents(List.of(event));

        verify(service).getAllEvents(any());
    }

    @Test
    public void shouldUpdateEvent(){

        doNothing().when(service).updateEvent(any());

        listener.updateEvent(event);

        verify(service).updateEvent(any());
    }

    @Test
    public void shouldNotSaveEvent(){

        doNothing().when(service).createErrorEvent(any());

        listener.createErrorEvent(event);

        verify(service).createErrorEvent(any());
    }

    @Test
    public void shouldNotUpdateEvent(){

        doNothing().when(service).updateErrorEvent(any());

        listener.updateErrorEvent(event);

        verify(service).updateErrorEvent(any());
    }
}
