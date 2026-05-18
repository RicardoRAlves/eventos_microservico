package com.br.capoeira.eventos.event_api.producer;

import com.br.capoeira.eventos.event_api.dto.EventSaleDeleteResponseDto;
import com.br.capoeira.eventos.event_api.dto.EventSaleItemResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventSaleProducer {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.sale.create.name}")
    private String exchangeCreateSaleProcessor;

    @Value("${rabbitmq.exchange.sale.update.name}")
    private String exchangeUpdateSaleProcessor;

    @Value("${rabbitmq.exchange.sale.delete.name}")
    private String exchangeDeleteSaleProcessor;


    public void sendingSaleCreateToProcessor(EventSaleItemResponseDto dto){
        log.info("Sending Create Sale to save on database {}", dto);
        rabbitTemplate.convertAndSend(exchangeCreateSaleProcessor, "", dto);
    }

    public void sendingSaleUpdateToProcessor(EventSaleItemResponseDto dto){
        log.info("Sending Update Sale to save on database {}", dto);
        rabbitTemplate.convertAndSend(exchangeUpdateSaleProcessor, "", dto);
    }

    public void sendingSaleDeleteToProcessor(EventSaleDeleteResponseDto dto){
        log.info("Sending Delete Sale to save on database {}", dto);
        rabbitTemplate.convertAndSend(exchangeDeleteSaleProcessor, "", dto);
    }
}
