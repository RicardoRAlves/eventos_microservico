package unit.com.br.capoeira.eventos.notification.service;

import com.br.capoeira.eventos.notification.model.Event;
import com.br.capoeira.eventos.notification.service.FirebaseService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.br.capoeira.eventos.notification.model.enums.Actions.CREATE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FirebaseServiceTest {

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
    private FirebaseService firebaseService;

    private Event event;

    @BeforeEach
    void setUp() {
        event = new Event();
        event.setTransactionId("tx-123");
        event.setTitle("Evento teste");
    }

    @Test
    void shouldAddEventOnFirestore() throws Exception {
        when(firestore.collection("events")).thenReturn(collectionReference);
        when(collectionReference.document("tx-123")).thenReturn(documentReference);
        when(documentReference.set(event, SetOptions.merge())).thenReturn(writeResultFuture);
        when(writeResultFuture.get()).thenReturn(writeResult);

        assertDoesNotThrow(() -> firebaseService.addEvent(event));

        verify(firestore).collection("events");
        verify(collectionReference).document("tx-123");
        verify(documentReference).set(event, SetOptions.merge());
        verify(writeResultFuture).get();
    }

    @Test
    void shouldThrowRuntimeExceptionWhenAddEventFails() throws Exception {
        when(firestore.collection("events")).thenReturn(collectionReference);
        when(collectionReference.document("tx-123")).thenReturn(documentReference);
        when(documentReference.set(event, SetOptions.merge())).thenReturn(writeResultFuture);
        when(writeResultFuture.get()).thenThrow(new RuntimeException("Firestore error"));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> firebaseService.addEvent(event)
        );

        assertTrue(exception.getMessage().contains("Error trying to added event"));
    }

    @Test
    void shouldUpdateEventOnFirestore() throws Exception {
        when(firestore.collection("events")).thenReturn(collectionReference);
        when(collectionReference.document("tx-123")).thenReturn(documentReference);
        when(documentReference.set(event, SetOptions.merge())).thenReturn(writeResultFuture);
        when(writeResultFuture.get()).thenReturn(writeResult);

        firebaseService.updateEvent(event);

        verify(firestore).collection("events");
        verify(collectionReference).document("tx-123");
        verify(documentReference).set(event, SetOptions.merge());
        verify(writeResultFuture).get();
    }

    @Test
    void shouldAddMultipleEventsOnFirestore() throws Exception {
        when(firestore.batch()).thenReturn(writeBatch);
        when(firestore.collection("events")).thenReturn(collectionReference);
        when(collectionReference.document("tx-123")).thenReturn(documentReference);
        when(writeBatch.set(documentReference, event, SetOptions.merge())).thenReturn(writeBatch);
        when(writeBatch.commit()).thenReturn(writeResultsFuture);
        when(writeResultsFuture.get()).thenReturn(List.of(writeResult));

        firebaseService.addMultipleEventsBatch(List.of(event));

        verify(firestore).batch();
        verify(firestore).collection("events");
        verify(collectionReference).document("tx-123");
        verify(writeBatch).set(documentReference, event, SetOptions.merge());
        verify(writeBatch).commit();
        verify(writeResultsFuture).get();
    }

    @Test
    public void shouldNotSendNotificationWithEventWhenCannotConvertObjectToJson() throws JsonProcessingException {

        when(objectMapper.writeValueAsString(any())).thenThrow(new RuntimeException(""));
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> firebaseService.sendEventNotification(event, CREATE)
        );
        assertTrue(exception.getMessage().contains("Error serializing payload to JSON"));
    }

    @Test
    public void shouldSendNotificationWithEvent() throws JsonProcessingException, FirebaseMessagingException {

        when(objectMapper.writeValueAsString(any())).thenReturn("eventToJson");
        when(firebaseMessaging.send(any())).thenReturn("ID");

        firebaseService.sendEventNotification(event, CREATE);

        verify(objectMapper).writeValueAsString(any());
        verify(firebaseMessaging).send(any());
    }

    @Test
    public void shouldSendNotificationWithMultipleEvent() throws JsonProcessingException, FirebaseMessagingException {

        when(objectMapper.writeValueAsString(any())).thenReturn("eventToJson");
        when(firebaseMessaging.send(any())).thenReturn("ID");

        firebaseService.sendEventNotification(event, CREATE);

        verify(objectMapper).writeValueAsString(any());
        verify(firebaseMessaging).send(any());
    }
}
