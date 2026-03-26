package com.br.capoeira.eventos.banco_api.producer;

import com.br.capoeira.eventos.banco_api.entities.Event;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import static java.util.Collections.singletonList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProcessorProducer {

    @Value("${rabbitmq.exchange.create-notification.name}")
    private String createNotificationExchange;

    @Value("${rabbitmq.exchange.error.create.name}")
    private String errorCreateExchange;

    private final RabbitTemplate rabbitTemplate;

    public void sendEventForSuccessQueue(Event event){
        rabbitTemplate.convertAndSend(createNotificationExchange, "", singletonList(event));
        log.info("Event sent to : {} successfuly", createNotificationExchange);
    }

    public void sendAllEvents(List<Event> events){
        rabbitTemplate.convertAndSend(createNotificationExchange, "", events);
        log.info("sending all events to : {} successfuly", createNotificationExchange);
    }

    public void sendEventForFailQueue(Event event){
        rabbitTemplate.convertAndSend(errorCreateExchange, "", event);
        log.info("Error Event sent to : {}", errorCreateExchange);
    }

}
