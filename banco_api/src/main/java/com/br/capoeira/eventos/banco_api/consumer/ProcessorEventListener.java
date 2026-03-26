package com.br.capoeira.eventos.banco_api.consumer;

import com.br.capoeira.eventos.banco_api.entities.Event;
import com.br.capoeira.eventos.banco_api.service.ProcessorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProcessorEventListener {

    private final ProcessorService service;

    @RabbitListener(queues = "${rabbitmq.create.queue.name}")
    public void saveEvent(Event event){
        log.info("Event received, {} ", event);
        service.createNewEvent(event);
    }

    @RabbitListener(queues = "${rabbitmq.get-all.queue.name}")
    public void getAllEvents(){
        log.info("Getting All Events ");
        service.findAll();
    }
}
