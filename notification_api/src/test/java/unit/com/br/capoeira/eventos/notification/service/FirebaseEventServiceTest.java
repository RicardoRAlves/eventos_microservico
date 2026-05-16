package unit.com.br.capoeira.eventos.notification.service;

import com.br.capoeira.eventos.notification.dto.EventDocument;
import com.br.capoeira.eventos.notification.dto.EventRequestDto;
import com.br.capoeira.eventos.notification.dto.EventSyncDto;
import com.br.capoeira.eventos.notification.dto.enums.EventScope;
import com.br.capoeira.eventos.notification.dto.enums.TypeContact;
import com.br.capoeira.eventos.notification.mapper.EventMapper;
import com.br.capoeira.eventos.notification.service.FirebaseEventService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static com.br.capoeira.eventos.notification.dto.enums.Actions.CREATE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ExtendWith(MockitoExtension.class)
class FirebaseEventServiceTest {

    @Mock
    private Firestore firestore;

    @Mock
    private FirebaseMessaging firebaseMessaging;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private CollectionReference collectionReference;

    @Mock
    private DocumentReference documentReference;

    @Mock
    private ApiFuture<WriteResult> writeResultFuture;

    @Mock
    private ApiFuture<List<WriteResult>> writeResultsFuture;

    @Mock
    private WriteResult writeResult;

    @Mock
    private WriteBatch writeBatch;

    @InjectMocks
    private FirebaseEventService firebaseEventService;

    private EventRequestDto event;
    private EventDocument eventDocument;
    private final String events_collection = "events_v2";

    @BeforeEach
    void setUp() {
        event = EventRequestDto.builder()
                .id(1L)
                .transactionId("tx-123")
                .title("Evento teste")
                .description("Descrição teste")
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

        eventDocument = new EventDocument();
        eventDocument.setId(1L);
        eventDocument.setTransactionId("tx-123");
        eventDocument.setTitle("Evento teste");
        eventDocument.setDescription("Descrição teste");
        eventDocument.setLocationName("Academia");
        eventDocument.setAddress("Rua X");
        eventDocument.setTypeContact(TypeContact.WHATSAPP);
        eventDocument.setContact("11999999999");
        eventDocument.setImage("image.png");
        eventDocument.setCategoryName("Capoeira");
        eventDocument.setScope(EventScope.PUBLIC);
        eventDocument.setOrganizationId(null);
        eventDocument.setOrganizationUnitId(null);
        eventDocument.setActive(true);
    }

    @Test
    void shouldAddEventOnFirestore() throws Exception {
        try (MockedStatic<EventMapper> mapperMock = mockStatic(EventMapper.class)) {
            mapperMock.when(() -> EventMapper.toDocument(event)).thenReturn(eventDocument);

            when(firestore.collection(events_collection)).thenReturn(collectionReference);
            when(collectionReference.document("tx-123")).thenReturn(documentReference);
            when(documentReference.set(eventDocument, SetOptions.merge())).thenReturn(writeResultFuture);
            when(writeResultFuture.get()).thenReturn(writeResult);

            assertDoesNotThrow(() -> firebaseEventService.addEvent(event));

            verify(firestore).collection(events_collection);
            verify(collectionReference).document("tx-123");
            verify(documentReference).set(eventDocument, SetOptions.merge());
            verify(writeResultFuture).get();
        }
    }

    @Test
    void shouldThrowRuntimeExceptionWhenAddEventFails() throws Exception {
        try (MockedStatic<EventMapper> mapperMock = mockStatic(EventMapper.class)) {
            mapperMock.when(() -> EventMapper.toDocument(event)).thenReturn(eventDocument);

            when(firestore.collection(events_collection)).thenReturn(collectionReference);
            when(collectionReference.document("tx-123")).thenReturn(documentReference);
            when(documentReference.set(eventDocument, SetOptions.merge())).thenReturn(writeResultFuture);
            when(writeResultFuture.get()).thenThrow(new RuntimeException("Firestore error"));

            RuntimeException exception = assertThrows(
                    RuntimeException.class,
                    () -> firebaseEventService.addEvent(event)
            );

            assertTrue(exception.getMessage().contains("Error trying to added event"));
        }
    }

    @Test
    void shouldUpdateEventOnFirestore() throws Exception {
        try (MockedStatic<EventMapper> mapperMock = mockStatic(EventMapper.class)) {
            mapperMock.when(() -> EventMapper.toDocument(event)).thenReturn(eventDocument);

            when(firestore.collection(events_collection)).thenReturn(collectionReference);
            when(collectionReference.document("tx-123")).thenReturn(documentReference);
            when(documentReference.set(eventDocument, SetOptions.merge())).thenReturn(writeResultFuture);
            when(writeResultFuture.get()).thenReturn(writeResult);

            firebaseEventService.updateEvent(event);

            verify(firestore).collection(events_collection);
            verify(collectionReference).document("tx-123");
            verify(documentReference).set(eventDocument, SetOptions.merge());
            verify(writeResultFuture).get();
        }
    }

    @Test
    void shouldAddMultipleEventsOnFirestore() throws Exception {
        try (MockedStatic<EventMapper> mapperMock = mockStatic(EventMapper.class)) {
            mapperMock.when(() -> EventMapper.toDocument(event)).thenReturn(eventDocument);

            when(firestore.batch()).thenReturn(writeBatch);
            when(firestore.collection(events_collection)).thenReturn(collectionReference);
            when(collectionReference.document("tx-123")).thenReturn(documentReference);
            when(writeBatch.set(documentReference, eventDocument, SetOptions.merge())).thenReturn(writeBatch);
            when(writeBatch.commit()).thenReturn(writeResultsFuture);
            when(writeResultsFuture.get()).thenReturn(List.of(writeResult));

            firebaseEventService.addMultipleEventsBatch(List.of(event));

            verify(firestore).batch();
            verify(firestore).collection(events_collection);
            verify(collectionReference).document("tx-123");
            verify(writeBatch).set(documentReference, eventDocument, SetOptions.merge());
            verify(writeBatch).commit();
            verify(writeResultsFuture).get();
        }
    }

    @Test
    void shouldNotSendNotificationWithEventWhenCannotConvertObjectToJson() throws JsonProcessingException {
        when(objectMapper.writeValueAsString(any())).thenThrow(new RuntimeException(""));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> firebaseEventService.sendEventNotification(event, CREATE, "public")
        );

        assertTrue(exception.getMessage().contains("Error serializing payload to JSON"));
    }

    @Test
    void shouldSendNotificationWithEvent() throws Exception {
        when(objectMapper.writeValueAsString(any())).thenReturn("eventToJson");
        when(firebaseMessaging.send(any(Message.class))).thenReturn("ID");

        firebaseEventService.sendEventNotification(event, CREATE, "public");

        verify(objectMapper).writeValueAsString(event);
        verify(firebaseMessaging).send(any(Message.class));
    }

    @Test
    void shouldSendNotificationWithMultipleEvent() throws Exception {
        var syncDto = EventSyncDto.builder()
                .scope(EventScope.PUBLIC)
                .events(List.of(event))
                .build();

        when(objectMapper.writeValueAsString(any())).thenReturn("eventToJson");
        when(firebaseMessaging.send(any(Message.class))).thenReturn("ID");

        firebaseEventService.sendEventNotification(syncDto, CREATE, "public");

        verify(objectMapper).writeValueAsString(syncDto);
        verify(firebaseMessaging).send(any(Message.class));
    }
}
