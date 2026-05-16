package com.br.capoeira.eventos.event_api.producer;

import com.br.capoeira.eventos.event_api.dto.EventDeleteResponseDto;
import com.br.capoeira.eventos.event_api.dto.EventResponseDto;
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

    @Value("${rabbitmq.exchange.delete.name}")
    private String exchangeDelete;

    public void sendingNewEventToProcessor(EventResponseDto eventResponseDto){
       log.info("Sending new Event to save on database {}", eventResponseDto);
       rabbitTemplate.convertAndSend(createExchange, "", eventResponseDto);
    }

    public void sendingErrorCreateEventToNotification(EventResponseDto eventResponseDto){
        log.info("Error to save event {}, please try again", eventResponseDto);
        rabbitTemplate.convertAndSend(createErrorNotificationExchange, "", eventResponseDto);
    }

    public void askingForSendingAllEvents(){
        log.info("asking for sending all events from database");
        rabbitTemplate.convertAndSend(getAllExchange, "","");
    }

    public void sendingEventUpdatedToProcessor(EventResponseDto eventResponseDto){
        log.info("Sending updated Event to save on database {}", eventResponseDto);
        rabbitTemplate.convertAndSend(exchangeUpdate, "", eventResponseDto);
    }

    public void sendingEventDeletedToProcessor(EventDeleteResponseDto eventResponseDto){
        log.info("Sending deleted Event to processor {}", eventResponseDto);
        rabbitTemplate.convertAndSend(exchangeDelete, "", eventResponseDto);
    }
}
