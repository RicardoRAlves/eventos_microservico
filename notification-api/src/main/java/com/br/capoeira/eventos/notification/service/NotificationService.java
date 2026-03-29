package com.br.capoeira.eventos.notification.service;

import com.br.capoeira.eventos.notification.model.Event;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class NotificationService {
    public void createNewEvent(Event event) {
        log.info("Notificar usuario !!! event : {}", event);
    }

    public void getAllEvents(List<Event> event) {
        log.info("Notificar usuario !!! get ALL event : {}", event);
    }

    public void updateEvent(Event event) {
        log.info("Notificar usuario que foi atualizado o event : {}", event);
    }

    public void createErrorEvent(Event event) {
        log.info("Notificar usuario que deu erro ao tentar inserir event : {}", event);
    }

    public void updateErrorEvent(Event event) {
        log.info("Notificar usuario que deu erro ao tentar atualizar event : {}", event);
    }
}
