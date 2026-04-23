package unit.com.br.capoeira.eventos.notification.consumer;

import com.br.capoeira.eventos.notification.consumer.NotificationListener;
import com.br.capoeira.eventos.notification.dto.EventErrorDto;
import com.br.capoeira.eventos.notification.dto.EventRequestDto;
import com.br.capoeira.eventos.notification.dto.enums.EventScope;
import com.br.capoeira.eventos.notification.dto.enums.TypeContact;
import com.br.capoeira.eventos.notification.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@ExtendWith(MockitoExtension.class)
class NotificationListenerTest {

    @Mock
    private NotificationService service;

    @InjectMocks
    private NotificationListener listener;

    @Test
    void shouldSaveNewEvent() {
        var event = getMockEventRequestDto();

        doNothing().when(service).createNewEvent(any());

        listener.saveEvent(event);

        verify(service).createNewEvent(event);
    }

    @Test
    void shouldGetAllEvents() {
        var event = getMockEventRequestDto();

        doNothing().when(service).getAllEvents(any());

        listener.getAllEvents(List.of(event));

        verify(service).getAllEvents(any());
    }

    @Test
    void shouldUpdateEvent() {
        var event = getMockEventRequestDto();

        doNothing().when(service).updateEvent(any());

        listener.updateEvent(event);

        verify(service).updateEvent(event);
    }

    @Test
    void shouldNotSaveEvent() {
        var event = getMockEventErrorDto();

        doNothing().when(service).createErrorEvent(any());

        listener.createErrorEvent(event);

        verify(service).createErrorEvent(event);
    }

    @Test
    void shouldNotUpdateEvent() {
        var event = getMockEventErrorDto();

        doNothing().when(service).updateErrorEvent(any());

        listener.updateErrorEvent(event);

        verify(service).updateErrorEvent(event);
    }

    private EventRequestDto getMockEventRequestDto() {
        return EventRequestDto.builder()
                .id(1L)
                .transactionId("tx-123")
                .title("Batizado")
                .description("Evento teste")
                .dateStarted(LocalDateTime.of(2026, 5, 10, 19, 0))
                .dateFinished(LocalDateTime.of(2026, 5, 10, 22, 0))
                .locationName("Academia")
                .address("Rua X")
                .typeContact(TypeContact.WHATSAPP)
                .contact("11999999999")
                .image("image.png")
                .categoryName("Capoeira")
                .scope(EventScope.PUBLIC)
                .organizationId(null)
                .organizationUnitId(null)
                .active(true)
                .build();
    }

    private EventErrorDto getMockEventErrorDto() {
        return EventErrorDto.builder()
                .transactionId("tx-error")
                .title("Batizado")
                .description("Erro ao criar evento")
                .dateStarted(LocalDateTime.of(2026, 5, 10, 19, 0))
                .dateFinished(LocalDateTime.of(2026, 5, 10, 22, 0))
                .locationName("Academia")
                .address("Rua X")
                .typeContact(TypeContact.WHATSAPP)
                .contact("11999999999")
                .image("image.png")
                .categoryName("Capoeira")
                .scope(EventScope.PUBLIC)
                .organizationId(null)
                .organizationUnitId(null)
                .active(true)
                .build();
    }
}
