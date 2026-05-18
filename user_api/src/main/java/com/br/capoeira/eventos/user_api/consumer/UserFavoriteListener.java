package com.br.capoeira.eventos.user_api.consumer;

import com.br.capoeira.eventos.user_api.dto.EventDeleteRequestDto;
import com.br.capoeira.eventos.user_api.service.UserFavoriteEventsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserFavoriteListener {

    private final UserFavoriteEventsService service;

    @RabbitListener(queues = "${rabbitmq.queue.delete-favorite.name}")
    public void deleteAllFavoritesByEventTransactionId(EventDeleteRequestDto dto){
        log.info("Delete All Favorites events by Transaction Id, {} ", dto);
        service.deleteFavoritesByEventTransactionId(dto);
    }
}
