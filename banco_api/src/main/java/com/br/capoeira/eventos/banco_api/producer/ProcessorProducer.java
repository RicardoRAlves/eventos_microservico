package com.br.capoeira.eventos.banco_api.producer;

import com.br.capoeira.eventos.banco_api.entities.Event;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

import static java.util.Collections.singletonList;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProcessorProducer {

    @Value("${rabbitmq.exchange.create-notification.name}")
    private String createNotificationExchange;

    @Value("${rabbitmq.exchange.error.create.name}")
    private String errorCreateExchange;

    @Value("${rabbitmq.exchange.get-all-notification.name}")
    private String getAllNotificationExchange;

    @Value("${rabbitmq.exchange.update-notification.name}")
    private String updateNotificationExchange;

    @Value("${rabbitmq.exchange.update-error-notification.name}")
    private String updateErrorNotificationExchange;

    private final RabbitTemplate rabbitTemplate;

    public void sendEventForSuccessQueue(Event event){
        rabbitTemplate.convertAndSend(createNotificationExchange, "", event);
        log.info("Event sent to : {} successfuly", createNotificationExchange);
    }

    public void sendAllEvents(List<Event> events){
        rabbitTemplate.convertAndSend(getAllNotificationExchange, "", events);
        log.info("sending all events to : {} successfuly", getAllNotificationExchange);
    }

    public void sendEventForUpdateQueue(Event event){
        rabbitTemplate.convertAndSend(updateNotificationExchange, "", event);
        log.info("Update Event {}, sent to : {}", event, updateNotificationExchange);
    }

    public void sendEventForUpdateErrorQueue(Event event){
        rabbitTemplate.convertAndSend(updateErrorNotificationExchange, "", event);
        log.info("Error to try Update Event {}, sent to : {}", event, updateErrorNotificationExchange);
    }

    public void sendEventForFailQueue(Event event){
        rabbitTemplate.convertAndSend(errorCreateExchange, "", event);
        log.info("Error Event sent to : {}", errorCreateExchange);
    }
}
