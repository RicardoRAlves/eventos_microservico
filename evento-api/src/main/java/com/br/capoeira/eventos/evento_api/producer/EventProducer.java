package com.br.capoeira.eventos.evento_api.producer;

import com.br.capoeira.eventos.evento_api.model.Event;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventProducer {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.create.name}")
    private String createExchange;

    @Value("${rabbitmq.exchange.get-all.name}")
    private String getAllExchange;

    @Value("${rabbitmq.exchange.error.create.notification.name}")
    private String createErrorNotificationExchange;

    @Value("${rabbitmq.exchange.update.name}")
    private String exchangeUpdate;

    public void sendingNewEventToProcessor(Event event){
       log.info("Sending new Event to save on database {}", event);
       rabbitTemplate.convertAndSend(createExchange, "", event);
    }

    public void sendingErrorCreateEventToNotification(Event event){
        log.info("Error to save event {}, please try again", event);
        rabbitTemplate.convertAndSend(createErrorNotificationExchange, "", event);
    }

    public void askingForSendingAllEvents(){
        log.info("asking for sending all events from database");
        rabbitTemplate.convertAndSend(getAllExchange, "");
    }

    public void sendingEventUpdatedToProcessor(Event event){
        log.info("Sending updated Event to save on database {}", event);
        rabbitTemplate.convertAndSend(createExchange, "", event);
    }
}
