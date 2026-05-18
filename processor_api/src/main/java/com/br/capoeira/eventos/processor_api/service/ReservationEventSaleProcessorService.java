package com.br.capoeira.eventos.processor_api.service;

import com.br.capoeira.eventos.processor_api.config.exception.ValidationException;
import com.br.capoeira.eventos.processor_api.dto.UserReservationEventRequestDto;
import com.br.capoeira.eventos.processor_api.dto.UserReservationEventValidatedMessageDto;
import com.br.capoeira.eventos.processor_api.producer.ReservationEventSaleProcessorProducer;
import com.br.capoeira.eventos.processor_api.repository.EventSaleItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Slf4j
public class ReservationEventSaleProcessorService {

    private final EventSaleItemRepository repository;

    private final ReservationEventSaleProcessorProducer producer;

    @Transactional
    public void reserveEventSaleItem(UserReservationEventRequestDto dto){
        log.info("reserving a event sale. transactionId={}", dto.getEventSaleTransactionId());
        try {
            if (repository.existsByTransactionId(dto.getEventSaleTransactionId())) {
                validateReservationEventSale(dto);
                var savedEventSale = repository.findByTransactionId(dto.getEventSaleTransactionId()).get();
                var responseDto = UserReservationEventValidatedMessageDto.builder()
                        .userId(dto.getUserId())
                        .eventSaleId(savedEventSale.getId())
                        .eventId(savedEventSale.getEvent().getId())
                        .eventSaleTransactionId(savedEventSale.getTransactionId())
                        .description(savedEventSale.getDescription())
                        .reservedQuantity(1)
                        .value(savedEventSale.getValue())
                        .build();

                producer.sendReservationToUserQueue(responseDto);

                log.info("Reserved event Sale successfully. id={}, transactionId={}",
                        savedEventSale.getId(), savedEventSale.getTransactionId());
            }
        } catch (Exception e) {
            log.error("Unexpected error while reserving a event sale. transactionId={}", dto.getEventSaleTransactionId(), e);
            throw new ValidationException("Error while reserving a event sale: " + e.getMessage());
        }
    }


    private void validateReservationEventSale(
            UserReservationEventRequestDto dto
    ){
        if (dto == null){
            throw new ValidationException("Reservation event sale must be informed");
        }

        if (dto.getEventSaleTransactionId() == null
                || dto.getEventSaleTransactionId().isBlank()) {

            throw new ValidationException("Transaction id must be informed");
        }

        if (dto.getEventSaleId() == null
        || dto.getEventSaleId() <= 0){
            throw new ValidationException("Event sale id must be informed");
        }

        if (dto.getUserId() == null
                || dto.getUserId() <= 0){
            throw new ValidationException("User id must be informed");
        }
    }

}
