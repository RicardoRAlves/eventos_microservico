package com.br.capoeira.eventos.evento_api.consumer;

import com.br.capoeira.eventos.evento_api.model.Event;
import com.br.capoeira.eventos.evento_api.service.EventService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class EventErrorListener {

    private final EventService service;
    @RabbitListener(queues = "${rabbitmq.create.error.queue.name}")
    public void errorCreateEvent(Event event){
        log.info("Event transaction id {} could not be save, please try again", event.getTransactionId());
        service.sendingCreateErrorToNotification(event);
    }
}
