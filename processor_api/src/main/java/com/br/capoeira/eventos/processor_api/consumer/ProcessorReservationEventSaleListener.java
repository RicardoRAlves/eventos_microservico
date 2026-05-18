package com.br.capoeira.eventos.processor_api.consumer;

import com.br.capoeira.eventos.processor_api.dto.UserReservationEventRequestDto;
import com.br.capoeira.eventos.processor_api.service.ReservationEventSaleProcessorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProcessorReservationEventSaleListener {

    private final ReservationEventSaleProcessorService service;

    @RabbitListener(queues = "${rabbitmq.queue.sale.create-reservation-processor.name}")
    public void reserveEventSale(UserReservationEventRequestDto dto){
        log.info("Request reserve item to user, {} ", dto);
        service.reserveEventSaleItem(dto);
    }
}
