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
        log.info("Notify android that event: {} has been created", event);
        firebaseService.addEvent(event);
        firebaseService.sendEventNotification(event, Actions.CREATE);
    }

    public void getAllEvents(List<Event> events) {
        log.info("Notify android that all events have been loaded: {}", events);
        firebaseService.addMultipleEventsBatch(events);
        firebaseService.sendEventsNotification(events, Actions.GET_ALL);
    }

    public void updateEvent(Event event) {
        log.info("Notify android that event: {} has been updated", event);
        firebaseService.updateEvent(event);
        firebaseService.sendEventNotification(event, Actions.UPDATE);
    }

    public void createErrorEvent(Event event) {
        log.info("Notify android that event: {} was not created successfully, please try again", event);
        firebaseService.sendEventNotification(event, Actions.ERROR_CREATE);
    }

    public void updateErrorEvent(Event event) {
        log.info("Notify android that event: {} was not updated successfully, please try again", event);
        firebaseService.sendEventNotification(event, Actions.ERROR_UPDATE);
    }
}
