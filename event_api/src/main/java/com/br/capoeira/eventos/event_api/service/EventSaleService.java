package com.br.capoeira.eventos.event_api.service;

import com.br.capoeira.eventos.event_api.config.exception.ValidationException;
import com.br.capoeira.eventos.event_api.dto.*;
import com.br.capoeira.eventos.event_api.mapper.EventSaleMapper;
import com.br.capoeira.eventos.event_api.producer.EventSaleProducer;
import com.br.capoeira.eventos.event_api.repository.EventSaleItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventSaleService {
    private static final String TRANSACTION_ID_PATTERN = "%s_%s";

    private final EventSaleMapper mapper;

    private final EventSaleItemRepository repository;

    private final EventSaleProducer producer;

    @Transactional
    public EventSaleItemResponseDto createItemSale(
            EventSaleItemCreateRequestDto dto
    ) {

        validateCreateItemSale(dto);

        var transactionId = String.format(
                TRANSACTION_ID_PATTERN,
                Instant.now().toEpochMilli(),
                UUID.randomUUID()
        );

        if (repository.existsByTransactionId(transactionId)) {

            throw new ValidationException(
                    "Transaction id already exists"
            );
        }

        var saleItem = mapper.createRequestDtoToEventSale(dto);

        saleItem.setTransactionId(transactionId);
        saleItem.setActive(true);

        try {

            log.info(
                    "Saving Event Sale Item on database. transactionId={}",
                    transactionId
            );

            repository.save(saleItem);

            var response = mapper.eventSaleToResponseDto(saleItem);

            log.info(
                    "Sending Event Sale Item to processor. transactionId={}",
                    transactionId
            );

            producer.sendingSaleCreateToProcessor(response);

            log.info(
                    "Event Sale Item created with success. transactionId={}",
                    transactionId
            );

            return response;

        } catch (Exception e) {
            var errorMessage =
                    "Error while trying to create event sale item. transactionId=%s"
                            .formatted(transactionId);

            log.error(errorMessage, e);

            throw new RuntimeException(errorMessage, e);
        }
    }

    @Transactional
    public EventSaleItemResponseDto updateItemSale(
            EventSaleItemUpdateRequestDto dto
    ) {

        validateUpdateItemSale(dto);

        var savedEventSale = repository.findByTransactionId(
                        dto.getTransactionId()
                )
                .orElseThrow(() ->
                        new ValidationException("Event Sale not found")
                );

        mapper.updateRequestDtoToEventSale(dto, savedEventSale);

        try {

            log.info(
                    "Updating event sale on database. transactionId={}",
                    savedEventSale.getTransactionId()
            );

            repository.save(savedEventSale);

            var eventResponseDto =
                    mapper.eventSaleToResponseDto(savedEventSale);

            log.info(
                    "Sending Event Sale Item to update queue processor. transactionId={}",
                    savedEventSale.getTransactionId()
            );

            producer.sendingSaleUpdateToProcessor(eventResponseDto);

            log.info(
                    "Event Sale Item updated with success. transactionId={}",
                    savedEventSale.getTransactionId()
            );

            return eventResponseDto;

        } catch (Exception e) {

            var errorMessage =
                    "Error while trying to update event sale. transactionId=%s"
                            .formatted(savedEventSale.getTransactionId());

            log.error(errorMessage, e);

            throw new RuntimeException(errorMessage, e);
        }
    }

    @Transactional
    public void deleteItemSale(
            EventSaleItemDeleteRequestDto dto
    ) {

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

            producer.sendingSaleDeleteToProcessor(response);

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

    private void validateCreateItemSale(
            EventSaleItemCreateRequestDto dto
    ) {

        if (dto == null) {
                throw new ValidationException(
                    "Event sale item request must be informed"
            );
        }

        if (dto.getEventTransactionId() == null
                || dto.getEventTransactionId().isBlank()) {

            throw new ValidationException(
                    "Event transaction id must be informed"
            );
        }

        if (dto.getDescription() == null
                || dto.getDescription().isBlank()) {

            throw new ValidationException(
                    "Description must be informed"
            );
        }

        if (dto.getDescription().length() > 150) {

            throw new ValidationException(
                    "Description must have a maximum of 150 characters"
            );
        }

        if (dto.getQuantity() == null
                || dto.getQuantity() < 0) {

            throw new ValidationException(
                    "Quantity must be zero or greater"
            );
        }

        if (dto.getValue() == null) {

            throw new ValidationException(
                    "Value must be informed"
            );
        }

        if (dto.getValue().compareTo(BigDecimal.ZERO) < 0) {

            throw new ValidationException(
                    "Value must be zero or greater"
            );
        }
    }

    private void validateUpdateItemSale(
            EventSaleItemUpdateRequestDto dto
    ) {

        if (dto == null) {
            throw new ValidationException(
                    "Event sale update request must be informed"
            );
        }

        if (dto.getTransactionId() == null
                || dto.getTransactionId().isBlank()) {

            throw new ValidationException(
                    "Transaction id must be informed"
            );
        }

        if (dto.getEventTransactionId() == null
                || dto.getEventTransactionId().isBlank()) {

            throw new ValidationException(
                    "Event transaction id must be informed"
            );
        }

        if (dto.getDescription() == null
                || dto.getDescription().isBlank()) {

            throw new ValidationException(
                    "Description must be informed"
            );
        }

        if (dto.getDescription().length() > 150) {

            throw new ValidationException(
                    "Description must have a maximum of 150 characters"
            );
        }

        if (dto.getQuantity() == null
                || dto.getQuantity() < 0) {

            throw new ValidationException(
                    "Quantity must be zero or greater"
            );
        }

        if (dto.getValue() == null) {

            throw new ValidationException(
                    "Value must be informed"
            );
        }

        if (dto.getValue().compareTo(BigDecimal.ZERO) < 0) {

            throw new ValidationException(
                    "Value must be zero or greater"
            );
        }

        if (dto.getActive() == null) {

            throw new ValidationException(
                    "Active must be informed"
            );
        }
    }

    private void validateDeleteItemSale(
            EventSaleItemDeleteRequestDto dto
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
