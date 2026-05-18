package unit.com.br.capoeira.eventos.notification.service;

import com.br.capoeira.eventos.notification.dto.EventErrorDto;
import com.br.capoeira.eventos.notification.dto.EventRequestDto;
import com.br.capoeira.eventos.notification.dto.EventSyncDto;
import com.br.capoeira.eventos.notification.dto.enums.Actions;
import com.br.capoeira.eventos.notification.dto.enums.EventScope;
import com.br.capoeira.eventos.notification.dto.enums.TypeContact;
import com.br.capoeira.eventos.notification.service.FirebaseEventService;
import com.br.capoeira.eventos.notification.service.NotificationEventService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ExtendWith(MockitoExtension.class)
class NotificationEventServiceTest {

    @Mock
    private FirebaseEventService firebaseEventService;

    @InjectMocks
    private NotificationEventService service;

    @Test
    void shouldCreateNewEventToFirestore() {
        var event = getMockEventRequestDto(EventScope.PUBLIC, null, null);

        doNothing().when(firebaseEventService).addEvent(any(EventRequestDto.class));
        doNothing().when(firebaseEventService).sendEventNotification(any(), any(), anyString());

        service.createNewEvent(event);

        verify(firebaseEventService).addEvent(event);
        verify(firebaseEventService).sendEventNotification(event, Actions.CREATE, "public");
    }

    @Test
    void shouldUpdateEventToFirestore() {
        var event = getMockEventRequestDto(EventScope.ORGANIZATION, 1L, null);

        doNothing().when(firebaseEventService).updateEvent(any(EventRequestDto.class));
        doNothing().when(firebaseEventService).sendEventNotification(any(), any(), anyString());

        service.updateEvent(event);

        verify(firebaseEventService).updateEvent(event);
        verify(firebaseEventService).sendEventNotification(event, Actions.UPDATE, "org_1");
    }

    @Test
    void shouldGetAllEventsToFirestore() {
        var publicEvent = getMockEventRequestDto(EventScope.PUBLIC, null, null);
        publicEvent.setTransactionId("tx-public");

        var orgEvent = getMockEventRequestDto(EventScope.ORGANIZATION, 1L, null);
        orgEvent.setTransactionId("tx-org");

        var unitEvent = getMockEventRequestDto(EventScope.ORGANIZATION_UNIT, 1L, 10L);
        unitEvent.setTransactionId("tx-unit");

        var events = List.of(publicEvent, orgEvent, unitEvent);

        doNothing().when(firebaseEventService).addMultipleEventsBatch(any());
        doNothing().when(firebaseEventService).sendEventNotification(any(), any(), anyString());

        service.getAllEvents(events);

        verify(firebaseEventService).addMultipleEventsBatch(events);
        verify(firebaseEventService, times(3)).sendEventNotification(any(EventSyncDto.class), eq(Actions.GET_ALL), anyString());
        verify(firebaseEventService).sendEventNotification(any(EventSyncDto.class), eq(Actions.GET_ALL), eq("public"));
        verify(firebaseEventService).sendEventNotification(any(EventSyncDto.class), eq(Actions.GET_ALL), eq("org_1"));
        verify(firebaseEventService).sendEventNotification(any(EventSyncDto.class), eq(Actions.GET_ALL), eq("unit_10"));
    }

    @Test
    void shouldSendErrorNotificationWhenCreateEvent() {
        var event = getMockEventErrorDto(EventScope.PUBLIC, null, null);

        doNothing().when(firebaseEventService).sendEventNotification(any(), any(), anyString());

        service.createErrorEvent(event);

        verify(firebaseEventService, never()).addEvent(any());
        verify(firebaseEventService).sendEventNotification(event, Actions.ERROR_CREATE, "public");
    }

    @Test
    void shouldSendErrorNotificationWhenUpdateEvent() {
        var event = getMockEventErrorDto(EventScope.ORGANIZATION_UNIT, 1L, 10L);

        doNothing().when(firebaseEventService).sendEventNotification(any(), any(), anyString());

        service.updateErrorEvent(event);

        verify(firebaseEventService, never()).addEvent(any());
        verify(firebaseEventService).sendEventNotification(event, Actions.ERROR_UPDATE, "unit_10");
    }

    private EventRequestDto getMockEventRequestDto(
            EventScope scope,
            Long organizationId,
            Long organizationUnitId
    ) {
        return EventRequestDto.builder()
                .id(1L)
                .transactionId("tx-123")
                .title("Batizado")
                .description("Evento de teste")
                .dateStarted(LocalDateTime.of(2026, 5, 10, 19, 0, 0))
                .dateFinished(LocalDateTime.of(2026, 5, 10, 22, 0, 0))
                .locationName("Academia")
                .address("Rua X")
                .typeContact(TypeContact.WHATSAPP)
                .contact("11999999999")
                .image("image.png")
                .categoryName("Capoeira")
                .scope(scope)
                .organizationId(organizationId)
                .organizationUnitId(organizationUnitId)
                .active(true)
                .build();
    }

    private EventErrorDto getMockEventErrorDto(
            EventScope scope,
            Long organizationId,
            Long organizationUnitId
    ) {
        return EventErrorDto.builder()
                .transactionId("tx-error-123")
                .title("Batizado")
                .description("Erro no evento")
                .dateStarted(LocalDateTime.of(2026, 5, 10, 19, 0, 0))
                .dateFinished(LocalDateTime.of(2026, 5, 10, 22, 0, 0))
                .locationName("Academia")
                .address("Rua X")
                .typeContact(TypeContact.WHATSAPP)
                .contact("11999999999")
                .image("image.png")
                .categoryName("Capoeira")
                .scope(scope)
                .organizationId(organizationId)
                .organizationUnitId(organizationUnitId)
                .active(true)
                .build();
    }
}
