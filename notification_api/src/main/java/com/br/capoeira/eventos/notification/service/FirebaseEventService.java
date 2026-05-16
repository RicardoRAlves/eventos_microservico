package com.br.capoeira.eventos.notification.service;

import com.br.capoeira.eventos.notification.dto.EventRequestDto;
import com.br.capoeira.eventos.notification.dto.enums.Actions;
import com.br.capoeira.eventos.notification.mapper.EventMapper;
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
public class FirebaseEventService {

    private static final String COLLECTION_NAME = "events_v2";
    private static final String KEY_ACTION = "action";
    private static final String KEY_EVENT = "body";

    private final Firestore firestore;
    private final FirebaseMessaging firebaseMessaging;
    private final ObjectMapper objectMapper;

    public void addEvent(EventRequestDto event) {
        persistEvent(event, "added");
    }

    public void updateEvent(EventRequestDto event) {
        persistEvent(event, "updated");
    }

    public void addMultipleEventsBatch(List<EventRequestDto> events) {
        try {
            WriteBatch batch = firestore.batch();

            for (EventRequestDto event : events) {
                var document = EventMapper.toDocument(event);
                DocumentReference docRef = getDocumentReference(document.getTransactionId());
                batch.set(docRef, document, SetOptions.merge());

                log.info("Event '{}' added to batch.", document.getTransactionId());
            }

            ApiFuture<List<WriteResult>> future = batch.commit();
            List<WriteResult> results = future.get();

            log.info("Batch committed successfully. Total operations: {}", results.size());

        } catch (Exception e) {
            log.error("Error adding events batch to Firebase: {}", e.getMessage(), e);
            throw new RuntimeException("Error adding events batch", e);
        }
    }

    private void persistEvent(EventRequestDto event, String operation) {
        try {
            var document = EventMapper.toDocument(event);

            ApiFuture<WriteResult> future = getDocumentReference(event.getTransactionId())
                    .set(document, SetOptions.merge());

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

    public void sendEventNotification(Object payload, Actions action, String topic) {
        try {
            Message message = Message.builder()
                    .putData(KEY_ACTION, action.name())
                    .putData(KEY_EVENT, toJson(payload))
                    .setTopic(topic)
                    .build();

            String response = firebaseMessaging.send(message);

            log.info("Message sent to topic {} with action {}: {}", topic, action, response);

        } catch (FirebaseMessagingException e) {
            log.error("Error sending message to topic {}: {}", topic, e.getMessage(), e);
            throw new RuntimeException("Error sending Firebase notification", e);
        }
    }

    private String toJson(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            throw new RuntimeException("Error serializing payload to JSON", e);
        }
    }
}