package com.br.capoeira.eventos.processor_api.consumer;

import com.br.capoeira.eventos.processor_api.dto.EventDeleteRequestDto;
import com.br.capoeira.eventos.processor_api.dto.EventRequestDto;
import com.br.capoeira.eventos.processor_api.service.EventProcessorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProcessorEventListener {

    private final EventProcessorService service;

    @RabbitListener(queues = "${rabbitmq.create.queue.name}")
    public void saveEvent(EventRequestDto dto){
        log.info("Event received, {} ", dto);
        service.createNewEvent(dto);
    }

    @RabbitListener(queues = "${rabbitmq.get-all.queue.name}")
    public void getAllEvents(){
        log.info("Getting All Events ");
        service.findAll();
    }

    @RabbitListener(queues = "${rabbitmq.update.queue.name}")
    public void updateEvents(EventRequestDto dto){
        log.info("updating event {} ", dto.getTransactionId());
        service.updateEvent(dto);
    }

    @RabbitListener(queues = "${rabbitmq.delete.queue.name}")
    public void deleteEvents(EventDeleteRequestDto dto){
        log.info("deleting event {} ", dto.getTransactionId());
        service.deleteEvent(dto);
    }
}
