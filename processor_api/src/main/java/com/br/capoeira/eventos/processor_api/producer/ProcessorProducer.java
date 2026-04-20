package com.br.capoeira.eventos.processor_api.producer;

import com.br.capoeira.eventos.processor_api.dto.EventRequestDto;
import com.br.capoeira.eventos.processor_api.dto.EventResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

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

    public void sendEventForSuccessQueue(EventResponseDto dto){
        rabbitTemplate.convertAndSend(createNotificationExchange, "", dto);
        log.info("Event sent to : {} successfuly", createNotificationExchange);
    }

    public void sendAllEvents(List<EventResponseDto> dto){
        rabbitTemplate.convertAndSend(getAllNotificationExchange, "", dto);
        log.info("sending all events to : {} successfuly", getAllNotificationExchange);
    }

    public void sendEventForUpdateQueue(EventResponseDto dto){
        rabbitTemplate.convertAndSend(updateNotificationExchange, "", dto);
        log.info("Update Event {}, sent to : {}", dto, updateNotificationExchange);
    }

    public void sendEventForUpdateErrorQueue(EventRequestDto dto){
        rabbitTemplate.convertAndSend(updateErrorNotificationExchange, "", dto);
        log.info("Error to try Update Event {}, sent to : {}", dto, updateErrorNotificationExchange);
    }

    public void sendEventForFailQueue(EventRequestDto dto){
        rabbitTemplate.convertAndSend(errorCreateExchange, "", dto);
        log.info("Error Event sent to : {}", errorCreateExchange);
    }
}
