package com.br.capoeira.eventos.processor_api.service;

import com.br.capoeira.eventos.processor_api.config.exception.ValidationException;
import com.br.capoeira.eventos.processor_api.dto.EventSaleDeleteRequestDto;
import com.br.capoeira.eventos.processor_api.dto.EventSaleDeleteResponseDto;
import com.br.capoeira.eventos.processor_api.dto.EventSaleItemRequestDto;
import com.br.capoeira.eventos.processor_api.entities.EventSaleItem;
import com.br.capoeira.eventos.processor_api.mapper.EventSaleMapper;
import com.br.capoeira.eventos.processor_api.producer.EventSaleProcessorProducer;
import com.br.capoeira.eventos.processor_api.repository.EventSaleItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@RequiredArgsConstructor
@Service
@Slf4j
public class EventSaleProcessorService {

    private final EventSaleItemRepository repository;

    private final EventSaleProcessorProducer producer;

    private final EventSaleMapper mapper;

    @Transactional
    public void createEventSales(EventSaleItemRequestDto dto){
        log.info("Creating new event sale. transactionId={}", dto.getTransactionId());
        try {
            var eventSale = mapper.requestDtoToEventSaleItem(dto);

            if (repository.existsByTransactionId(eventSale.getTransactionId())) {
                log.info(
                        "Event sale already exists. Skipping create. transactionId={}",
                        eventSale.getTransactionId()
                );
                return;
            }

            validateEventSale(eventSale);
            var savedEventSale = repository.save(eventSale);
            var responseDto = mapper.eventSaleItemToResponseDto(savedEventSale);

            producer.sendSaleToCreateNotificationQueue(responseDto);

            log.info("Event Sale created successfully. id={}, transactionId={}",
                    savedEventSale.getId(), savedEventSale.getTransactionId());

        } catch (Exception e) {
            log.error("Unexpected error while creating event sale. transactionId={}", dto.getTransactionId(), e);
            throw new ValidationException("Error while creating event sale: " + e.getMessage());
        }
    }

    @Transactional
    public void updateEventSale(EventSaleItemRequestDto dto){
        log.info("Updating event sale. transactionId={}", dto.getTransactionId());

        try {
            var savedEventSale = repository.findByTransactionId(dto.getTransactionId())
                    .orElseThrow(() ->
                    new ValidationException("Event Sale not found for transactionId: " + dto.getTransactionId()));

            mapper.updateRequestDtoToEventSaleItem(dto, savedEventSale);


            var updatedEventSale = repository.save(savedEventSale);
            var responseDto = mapper.eventSaleItemToResponseDto(updatedEventSale);

            producer.sendSaleToUpdateNotificationQueue(responseDto);

            log.info("Event Sale updated successfully. id={}, transactionId={}",
                    updatedEventSale.getId(), updatedEventSale.getTransactionId());

        } catch (Exception e) {
            log.error("Unexpected error while updating event sale. transactionId={}", dto.getTransactionId(), e);
            throw new ValidationException("Error while updating event sale: " + e.getMessage());
        }
    }

    @Transactional
    public void deleteEventSale(EventSaleDeleteRequestDto dto) {
        log.info("Deleting event sale. transactionId={}", dto.getTransactionId());

        validateDeleteItemSale(dto);

        var savedEventSaleOptional =
                repository.findByTransactionId(
                        dto.getTransactionId()
                );

        if (savedEventSaleOptional.isEmpty()) {

            log.warn(
                    "Event sale item not found for delete. transactionId={}",
                    dto.getTransactionId()
            );

            return;
        }

        var savedEventSale = savedEventSaleOptional.get();

        try {

            if (Boolean.FALSE.equals(savedEventSale.getActive())) {

                log.info(
                        "Event sale item already inactive. transactionId={}",
                        savedEventSale.getTransactionId()
                );

            } else {

                log.info(
                        "Deleting logic event sale item. transactionId={}",
                        savedEventSale.getTransactionId()
                );

                savedEventSale.setActive(false);

                repository.save(savedEventSale);
            }

            log.info(
                    "Sending event sale item to delete queue processor. transactionId={}",
                    savedEventSale.getTransactionId()
            );

            var response = new EventSaleDeleteResponseDto(savedEventSale.getTransactionId());

            producer.sendSaleToDeleteNotificationQueue(response);

            log.info(
                    "Delete event sale item message sent with success. transactionId={}",
                    savedEventSale.getTransactionId()
            );
        } catch (Exception e) {

            var errorMessage =
                    "Error while trying to delete event sale item. transactionId=%s"
                            .formatted(savedEventSale.getTransactionId());

            log.error(errorMessage, e);

            throw new RuntimeException(errorMessage, e);
        }
    }

    private void validateEventSale(
            EventSaleItem eventSale
    ) {

        if (eventSale == null) {
            throw new ValidationException("Event sale must be informed");
        }

        if (eventSale.getTransactionId() == null
                || eventSale.getTransactionId().isBlank()) {

            throw new ValidationException("Transaction id must be informed");
        }

        if (eventSale.getEventTransactionId() == null
                || eventSale.getEventTransactionId().isBlank()) {

            throw new ValidationException("Event transaction id must be informed");
        }

        if (eventSale.getDescription() == null
                || eventSale.getDescription().isBlank()) {

            throw new ValidationException("Description must be informed");
        }

        if (eventSale.getDescription().length() > 150) {
            throw new ValidationException(
                    "Description must have a maximum of 150 characters"
            );
        }

        if (eventSale.getQuantity() == null || eventSale.getQuantity() < 0) {
            throw new ValidationException("Quantity must be zero or greater");
        }

        if (eventSale.getValue() == null) {
            throw new ValidationException("Value must be informed");
        }

        if (eventSale.getValue().compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Value must be zero or greater");
        }

        if (eventSale.getActive() == null) {
            throw new ValidationException("Active must be informed");
        }
    }

    private void validateDeleteItemSale(
            EventSaleDeleteRequestDto dto
    ) {

        if (dto == null) {
            throw new ValidationException(
                    "Event sale delete request must be informed"
            );
        }

        if (dto.getTransactionId() == null
                || dto.getTransactionId().isBlank()) {

            throw new ValidationException(
                    "Transaction id must be informed"
            );
        }
    }
}
