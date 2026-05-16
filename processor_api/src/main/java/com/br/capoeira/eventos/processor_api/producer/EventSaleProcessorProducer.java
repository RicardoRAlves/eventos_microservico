package com.br.capoeira.eventos.processor_api.producer;

import com.br.capoeira.eventos.processor_api.dto.EventSaleDeleteResponseDto;
import com.br.capoeira.eventos.processor_api.dto.EventSaleItemResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventSaleProcessorProducer {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.sale.create-notification.name}")
    private String createSaleNotificationExchange;

    @Value("${rabbitmq.exchange.sale.update-notification.name}")
    private String updateSaleNotificationExchange;

    @Value("${rabbitmq.exchange.sale.delete-notification.name}")
    private String deleteSaleNotificationExchange;

    public void sendSaleToCreateNotificationQueue(EventSaleItemResponseDto dto){
        rabbitTemplate.convertAndSend(createSaleNotificationExchange, "", dto);
        log.info("Created event Sale sent to : {} successfully", createSaleNotificationExchange);
    }

    public void sendSaleToUpdateNotificationQueue(EventSaleItemResponseDto dto){
        rabbitTemplate.convertAndSend(updateSaleNotificationExchange, "", dto);
        log.info("Updated event Sale sent to : {} successfully", updateSaleNotificationExchange);
    }

    public void sendSaleToDeleteNotificationQueue(EventSaleDeleteResponseDto dto){
        rabbitTemplate.convertAndSend(deleteSaleNotificationExchange, "", dto);
        log.info("Deleted event Sale sent to : {} successfully", deleteSaleNotificationExchange);
    }
}
