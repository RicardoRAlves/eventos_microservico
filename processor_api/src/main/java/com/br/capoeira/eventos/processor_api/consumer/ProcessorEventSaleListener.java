package com.br.capoeira.eventos.processor_api.consumer;

import com.br.capoeira.eventos.processor_api.dto.EventSaleDeleteRequestDto;
import com.br.capoeira.eventos.processor_api.dto.EventSaleItemRequestDto;
import com.br.capoeira.eventos.processor_api.service.EventSaleProcessorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProcessorEventSaleListener {

    private final EventSaleProcessorService service;

    @RabbitListener(queues = "${rabbitmq.sale.create.queue.name}")
    public void createEventSale(EventSaleItemRequestDto dto){
        log.info("Event Sale received from create-queue, {} ", dto);
        service.createEventSales(dto);
    }

    @RabbitListener(queues = "${rabbitmq.sale.update.queue.name}")
    public void updateEventSale(EventSaleItemRequestDto dto){
        log.info("Event Sale received from updated-queue, {} ", dto);
        service.updateEventSale(dto);
    }

    @RabbitListener(queues = "${rabbitmq.sale.delete.queue.name}")
    public void deleteEventSale(EventSaleDeleteRequestDto dto){
        log.info("Event Sale received from delete-queue, {} ", dto);
        service.deleteEventSale(dto);
    }
}
