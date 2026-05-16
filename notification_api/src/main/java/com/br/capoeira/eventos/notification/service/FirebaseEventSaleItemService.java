package com.br.capoeira.eventos.notification.service;

import com.br.capoeira.eventos.notification.dto.EventSaleItemRequestDto;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.SetOptions;
import com.google.cloud.firestore.WriteResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FirebaseEventSaleItemService {

    private static final String COLLECTION_NAME = "event_sale_items_v1";

    private final Firestore firestore;

    public void addEventSaleItem(EventSaleItemRequestDto eventSaleItem) {
        persistEventSaleItem(eventSaleItem, "added");
    }

    public void updateEventSaleItem(EventSaleItemRequestDto eventSaleItem) {
        persistEventSaleItem(eventSaleItem, "updated");
    }

    public void deleteEventSaleItem(String transactionId) {
        try {
            ApiFuture<WriteResult> future = getDocumentReference(transactionId)
                    .delete();

            WriteResult result = future.get();

            log.info(
                    "Event sale item {} deleted from Firestore at {}",
                    transactionId,
                    result.getUpdateTime()
            );

        } catch (Exception e) {
            log.error(
                    "Error deleting event sale item {} from Firebase: {}",
                    transactionId,
                    e.getMessage(),
                    e
            );
            throw new RuntimeException("Error deleting event sale item", e);
        }
    }

    private void persistEventSaleItem(
            EventSaleItemRequestDto eventSaleItem,
            String operation
    ) {
        try {
            ApiFuture<WriteResult> future = getDocumentReference(
                    eventSaleItem.getTransactionId()
            ).set(eventSaleItem, SetOptions.merge());

            WriteResult result = future.get();

            log.info(
                    "Event sale item {} {} on Firestore at {}",
                    eventSaleItem.getTransactionId(),
                    operation,
                    result.getUpdateTime()
            );

        } catch (Exception e) {
            log.error(
                    "Error {} event sale item on Firebase: {}",
                    operation,
                    e.getMessage(),
                    e
            );
            throw new RuntimeException(
                    "Error trying to " + operation + " event sale item",
                    e
            );
        }
    }

    private DocumentReference getDocumentReference(String transactionId) {
        return firestore
                .collection(COLLECTION_NAME)
                .document(transactionId);
    }
}
