package com.br.capoeira.eventos.user_api.consumer;

import com.br.capoeira.eventos.user_api.dto.EventSaleDeleteRequestDto;
import com.br.capoeira.eventos.user_api.dto.UserReservationEventValidatedMessageDto;
import com.br.capoeira.eventos.user_api.service.UserReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserReservationListener {

    private final UserReservationService service;

    @RabbitListener(queues = "${rabbitmq.queue.create-reservation.name}")
    public void createEventReservation(UserReservationEventValidatedMessageDto dto){
        log.info("create new reservation for, {} ", dto);
        service.createNewReservation(dto);
    }

    @RabbitListener(queues = "${rabbitmq.queue.delete-reservation.name}")
    public void deleteEventReservation(EventSaleDeleteRequestDto dto){
        log.info("Delete All Reservations events by Transaction Id, {} ", dto);
        service.deleteAllReservationsByEventSaleTransactionId(dto);
    }
}
