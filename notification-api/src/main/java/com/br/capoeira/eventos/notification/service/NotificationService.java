package com.br.capoeira.eventos.notification.service;

import com.br.capoeira.eventos.notification.model.Event;
import com.br.capoeira.eventos.notification.model.enums.Actions;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class NotificationService {

    private final FirebaseService firebaseService;

    public void createNewEvent(Event event) {
        log.info("Notify android that event : {}, has been created", event);
        firebaseService.addEvent(event);
        //firebaseService.sendEventUpdateNotification(event.getTransactionId(), Actions.CREATED);
    }

    public void getAllEvents(List<Event> event) {
        log.info("Notify android that it has been updated all events : {}", event);
        firebaseService.addMultipleEventsBatch(event);
        //firebaseService.sendEventUpdateNotification(null, Actions.GETALL);
    }

    public void updateEvent(Event event) {
        log.info("Notify android that event : {} has been updated", event);
        firebaseService.updateEvent(event);
        //firebaseService.sendEventUpdateNotification(event.getTransactionId(), Actions.UPDATED);
    }

    public void createErrorEvent(Event event) {
        log.info("Notify android that event : {} has not added with sucessfuly, please try again", event);
        firebaseService.sendEventUpdateNotification(event.getTransactionId(), Actions.ERROR_CREATE);
    }

    public void updateErrorEvent(Event event) {
        log.info("Notify android that event : {} has not updated with sucessfuly, please try again", event);
        firebaseService.sendEventUpdateNotification(event.getTransactionId(), Actions.ERROR_UPDATE);
    }
}
