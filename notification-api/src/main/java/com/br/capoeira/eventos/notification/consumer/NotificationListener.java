package com.br.capoeira.eventos.notification.consumer;

import com.br.capoeira.eventos.notification.model.Event;
import com.br.capoeira.eventos.notification.service.NotificationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@AllArgsConstructor
public class NotificationListener {

    private final NotificationService service;

    @RabbitListener(queues = "${rabbitmq.queue.create-notification.name}")
    public void saveEvent(Event event){
        log.info("saveEvent - Event received, {} ", event);
        service.createNewEvent(event);
    }

    @RabbitListener(queues = "${rabbitmq.queue.get-all-notification.name}")
    public void getAllEvents(List<Event> event){
        log.info("getAllEvents - Event received, {} ", event);
        service.getAllEvents(event);
    }

    @RabbitListener(queues = "${rabbitmq.queue.update-notification.name}")
    public void updateEvent(Event event){
        log.info("updateEvent - Event received, {} ", event);
        service.updateEvent(event);
    }

    @RabbitListener(queues = "${rabbitmq.queue.error.create.notification.name}")
    public void createErrorEvent(Event event){
        log.info("createErrorEvent - Event received, {} ", event);
        service.createErrorEvent(event);
    }

    @RabbitListener(queues = "${rabbitmq.queue.update-error-notification.name}")
    public void updateErrorEvent(Event event){
        log.info("updateErrorEvent - Event received, {} ", event);
        service.updateErrorEvent(event);
    }
}
