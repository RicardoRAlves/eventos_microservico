package unit.com.br.capoeira.eventos.notification.service;

import com.br.capoeira.eventos.notification.model.Event;
import com.br.capoeira.eventos.notification.service.FirebaseService;
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
public class NotificationServiceTest {

    @Mock
    private FirebaseService firebaseService;

    @InjectMocks
    private NotificationService service;

    private final Event event = getMockEvent();

    @Test
    public void shouldCreateNewEventToFirestore(){
        doNothing().when(firebaseService).addEvent(any());
        doNothing().when(firebaseService).sendEventNotification(any(), any());

        service.createNewEvent(event);

        verify(firebaseService).addEvent(any());
        verify(firebaseService).sendEventNotification(any(), any());
    }

    @Test
    public void shouldUpdateEventToFirestore(){
        doNothing().when(firebaseService).updateEvent(any());
        doNothing().when(firebaseService).sendEventNotification(any(), any());

        service.updateEvent(event);

        verify(firebaseService).updateEvent(any());
        verify(firebaseService).sendEventNotification(any(), any());
    }

    @Test
    public void shouldGetAllEventsToFirestore(){
        doNothing().when(firebaseService).addMultipleEventsBatch(any());
        doNothing().when(firebaseService).sendEventsNotification(any(), any());

        service.getAllEvents(List.of(event));

        verify(firebaseService).addMultipleEventsBatch(any());
        verify(firebaseService).sendEventsNotification(any(), any());
    }

    @Test
    public void shouldSendErrorNotificationWhenCreateEvent(){
        doNothing().when(firebaseService).sendEventNotification(any(), any());

        service.createErrorEvent(event);

        verify(firebaseService, never()).addEvent(any());
        verify(firebaseService).sendEventNotification(any(), any());
    }

    @Test
    public void shouldSendErrorNotificationWhenUpdateEvent(){
        doNothing().when(firebaseService).sendEventNotification(any(), any());

        service.updateErrorEvent(event);

        verify(firebaseService, never()).addEvent(any());
        verify(firebaseService).sendEventNotification(any(), any());
    }
}
