package com.br.capoeira.eventos.notification.service;

import com.br.capoeira.eventos.notification.model.Event;
import com.br.capoeira.eventos.notification.model.enums.Actions;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FirebaseService {

    private static final String COLLECTION_NAME = "events";
    private static final String TOPIC = "event_updates";

    private static final String KEY_ACTION = "action";
    private static final String KEY_EVENT = "body";

    private final Firestore firestore;
    private final FirebaseMessaging firebaseMessaging;
    private final ObjectMapper objectMapper;

    public void addEvent(Event event) {
        persistEvent(event, "added");
    }

    public void updateEvent(Event event) {
        persistEvent(event, "updated");
    }

    public void addMultipleEventsBatch(List<Event> events) {
        try {
            WriteBatch batch = firestore.batch();

            for (Event event : events) {
                DocumentReference docRef = getDocumentReference(event.getTransactionId());
                batch.set(docRef, event, SetOptions.merge());

                log.info("Event '{}' added to batch.", event.getTransactionId());
            }

            ApiFuture<List<WriteResult>> future = batch.commit();
            List<WriteResult> results = future.get();

            log.info("Batch committed successfully. Total operations: {}", results.size());

        } catch (Exception e) {
            log.error("Error adding events batch to Firebase: {}", e.getMessage(), e);
            throw new RuntimeException("Error adding events batch", e);
        }
    }

    private void persistEvent(Event event, String operation) {
        try {
            ApiFuture<WriteResult> future = getDocumentReference(event.getTransactionId())
                    .set(event, SetOptions.merge());

            WriteResult result = future.get();

            log.info("Event {} {} on Firestore at {}", event.getTransactionId(), operation, result.getUpdateTime());

        } catch (Exception e) {
            log.error("Error {} event on Firebase: {}", operation, e.getMessage(), e);
            throw new RuntimeException("Error trying to " + operation + " event", e);
        }
    }

    private DocumentReference getDocumentReference(String transactionId) {
        return firestore.collection(COLLECTION_NAME).document(transactionId);
    }

    public void sendEventNotification(Event event, Actions action) {
        sendNotification(event, action);
    }

    public void sendEventsNotification(List<Event> events, Actions action) {
        sendNotification(events, action);
    }

    private void sendNotification(Object payload, Actions action) {
        try {
            Message message = buildMessage(FirebaseService.KEY_EVENT, payload, action);

            String response = firebaseMessaging.send(message);

            log.info("Message sent to topic {} with action {}: {}", TOPIC, action, response);

        } catch (FirebaseMessagingException e) {
            log.error("Error sending message to topic {}: {}", TOPIC, e.getMessage(), e);
            throw new RuntimeException("Error sending Firebase notification", e);
        }
    }

    private Message buildMessage(String payloadKey, Object payload, Actions action) {
        return Message.builder()
                .putData(KEY_ACTION, action.name())
                .putData(payloadKey, toJson(payload))
                .setTopic(TOPIC)
                .build();
    }

    private String toJson(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            throw new RuntimeException("Error serializing payload to JSON", e);
        }
    }
}