package com.br.capoeira.eventos.event_api.consumer;

import com.br.capoeira.eventos.event_api.dto.EventResponseDto;
import com.br.capoeira.eventos.event_api.service.EventService;
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
    public void errorCreateEvent(EventResponseDto eventResponseDto){
        log.info("Event transaction id {} could not be save, please try again", eventResponseDto.getTransactionId());
        service.sendingCreateErrorToNotification(eventResponseDto);
    }
}
