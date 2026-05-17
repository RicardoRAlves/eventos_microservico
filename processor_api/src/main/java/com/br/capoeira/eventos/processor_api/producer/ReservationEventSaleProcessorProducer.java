package com.br.capoeira.eventos.processor_api.producer;

import com.br.capoeira.eventos.processor_api.dto.UserReservationEventValidatedMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationEventSaleProcessorProducer {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.sale.create-reservation.name}")
    private String reserveEventSaleExchange;

    public void sendReservationToUserQueue(UserReservationEventValidatedMessageDto dto){
        rabbitTemplate.convertAndSend(reserveEventSaleExchange, "", dto);
        log.info("Sending data to reserve event Sale to : {} successfully", reserveEventSaleExchange);
    }

}
