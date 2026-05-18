package com.br.capoeira.eventos.notification.consumer;

import com.br.capoeira.eventos.notification.dto.EventSaleDeleteRequestDto;
import com.br.capoeira.eventos.notification.dto.EventSaleItemRequestDto;
import com.br.capoeira.eventos.notification.service.NotificationEventSaleService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class NotificationEventSaleListener {

    private final NotificationEventSaleService service;

    @RabbitListener(queues = "${rabbitmq.queue.sale.create-notification.name}")
    public void createEventSale(EventSaleItemRequestDto dto){
        log.info("createEventSale - Event Sale received, {} ", dto);
        service.createNewEventSale(dto);
    }

    @RabbitListener(queues = "${rabbitmq.queue.sale.update-notification.name}")
    public void updateEventSale(EventSaleItemRequestDto dto){
        log.info("updateEventSale - Event Sale received, {} ", dto);
        service.updateNewEventSale(dto);
    }

    @RabbitListener(queues = "${rabbitmq.queue.sale.delete-notification.name}")
    public void deleteEventSale(EventSaleDeleteRequestDto dto){
        log.info("deleteEventSale - Event Sale received, {} ", dto);
        service.deleteNewEventSale(dto);
    }
}
