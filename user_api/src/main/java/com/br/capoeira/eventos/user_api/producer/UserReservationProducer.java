package com.br.capoeira.eventos.user_api.producer;

import com.br.capoeira.eventos.user_api.dto.UserReservationEventRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserReservationProducer {

    @Value("${rabbitmq.exchange.sale.create-reservation-processor.name}")
    private String createReservationProcessorExchange;

    private final RabbitTemplate rabbitTemplate;

    public void sendReservedItemToQueue(UserReservationEventRequestDto dto){
        rabbitTemplate.convertAndSend(createReservationProcessorExchange, "", dto);
        log.info("Request reservation : {} successfuly", createReservationProcessorExchange);
    }
}
