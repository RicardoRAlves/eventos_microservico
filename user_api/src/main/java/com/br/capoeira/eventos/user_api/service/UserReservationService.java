package com.br.capoeira.eventos.user_api.service;

import com.br.capoeira.eventos.user_api.config.exception.ValidationException;
import com.br.capoeira.eventos.user_api.dto.*;
import com.br.capoeira.eventos.user_api.mapper.UserReservationEventMapper;
import com.br.capoeira.eventos.user_api.model.UserReservationEvent;
import com.br.capoeira.eventos.user_api.producer.UserReservationProducer;
import com.br.capoeira.eventos.user_api.repository.UserReservationEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserReservationService {

    private final UserReservationProducer producer;
    private final UserReservationEventRepository repository;
    private final UserReservationEventMapper mapper;

    @Transactional(readOnly = true)
    public List<UserReservationEventResponseDto> findAllReservedEventsByUserId(Long userId) {
        validateUserId(userId);

        log.info("Finding all reservations by userId={}", userId);

        return repository.findAllByUserId(userId)
                .stream()
                .map(mapper::entityToResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public Long countingAllReservesByEventId(Long eventId) {
        validateEventId(eventId);

        log.info("Counting reservations by eventId={}", eventId);

        return repository.countByEventId(eventId);
    }

    @Transactional(readOnly = true)
    public Long countingAllReservesByEventSaleId(Long eventSaleId) {
        validateEventSaleId(eventSaleId);

        log.info("Counting reservations by eventSaleId={}", eventSaleId);

        return repository.countByEventSaleId(eventSaleId);
    }

    public void requestReservation(UserReservationEventRequestDto dto) {
        validateRequestReservation(dto);

        log.info(
                "Requesting reservation. userId={}, eventSaleId={}",
                dto.getUserId(),
                dto.getEventSaleId()
        );

        if (repository.existsByUserIdAndEventSaleId(
                dto.getUserId(),
                dto.getEventSaleId()
        )) {
            log.info(
                    "Reservation already exists. Skipping request. userId={}, eventSaleId={}",
                    dto.getUserId(),
                    dto.getEventSaleId()
            );
            return;
        }

        producer.sendReservedItemToQueue(dto);

        log.info(
                "Reservation request sent to processor successfully. userId={}, eventSaleId={}",
                dto.getUserId(),
                dto.getEventSaleId()
        );
    }

    @Transactional
    public void createNewReservation(UserReservationEventValidatedMessageDto dto) {
        validateValidatedReservationMessage(dto);

        log.info(
                "Creating reservation from validated message. transactionId={}, userId={}, eventSaleId={}",
                dto.getEventSaleTransactionId(),
                dto.getUserId(),
                dto.getEventSaleId()
        );

        if (repository.existsByUserIdAndEventSaleId(
                dto.getUserId(),
                dto.getEventSaleId()
        )) {
            log.info(
                    "Reservation already exists. Skipping create. userId={}, eventSaleId={}",
                    dto.getUserId(),
                    dto.getEventSaleId()
            );
            return;
        }

        var reservation = mapper.validatedMessageDtoToEntity(dto);

        validateUserReservationEventSale(reservation);

        var savedReservation = repository.save(reservation);

        log.info(
                "Reservation created successfully. id={}, userId={}, eventSaleId={}, transactionId={}",
                savedReservation.getId(),
                savedReservation.getUserId(),
                savedReservation.getEventSaleId(),
                savedReservation.getEventSaleTransactionId()
        );
    }

    @Transactional
    public void deleteAllReservationsByEventSaleTransactionId(EventSaleDeleteRequestDto dto) {
        validateDeleteAllReservations(dto);

        var eventSaleTransactionId = dto.getTransactionId();

        int affectedRows = repository.deleteAllByEventSaleTransactionId(
                eventSaleTransactionId
        );

        if (affectedRows == 0) {
            log.info(
                    "No reservations found for eventSaleTransactionId={}",
                    eventSaleTransactionId
            );
            return;
        }

        log.info(
                "Deleted {} reservation records for eventSaleTransactionId={}",
                affectedRows,
                eventSaleTransactionId
        );
    }

    @Transactional
    public void deleteReservationsByEventSaleId(UserReservationDeleteDto dto) {
        validateDeleteReservation(dto);

        if (!repository.existsByUserIdAndEventSaleId(
                dto.getUserId(),
                dto.getEventSaleId()
        )) {
            log.info(
                    "Reservation not found. Skipping delete. userId={}, eventSaleId={}",
                    dto.getUserId(),
                    dto.getEventSaleId()
            );
            return;
        }

        repository.deleteByUserIdAndEventSaleId(
                dto.getUserId(),
                dto.getEventSaleId()
        );

        log.info(
                "Reservation deleted successfully. userId={}, eventSaleId={}",
                dto.getUserId(),
                dto.getEventSaleId()
        );
    }

    private void validateRequestReservation(UserReservationEventRequestDto dto) {
        if (dto == null) {
            throw new ValidationException("Reservation request must be informed");
        }

        if (dto.getUserId() == null) {
            throw new ValidationException("User id must be informed");
        }

        if (dto.getEventSaleId() == null) {
            throw new ValidationException("Event sale id must be informed");
        }

        if (dto.getEventSaleTransactionId() == null ||
                dto.getEventSaleTransactionId().isBlank()) {
            throw new ValidationException("Event sale transaction id must be informed");
        }
    }

    private void validateValidatedReservationMessage(
            UserReservationEventValidatedMessageDto dto
    ) {
        if (dto == null) {
            throw new ValidationException("Validated reservation message must be informed");
        }

        if (dto.getUserId() == null) {
            throw new ValidationException("User id must be informed");
        }

        if (dto.getEventSaleId() == null) {
            throw new ValidationException("Event sale id must be informed");
        }

        if (dto.getEventId() == null) {
            throw new ValidationException("Event id must be informed");
        }

        if (dto.getEventSaleTransactionId() == null ||
                dto.getEventSaleTransactionId().isBlank()) {
            throw new ValidationException("Event sale transaction id must be informed");
        }

        if (dto.getDescription() == null || dto.getDescription().isBlank()) {
            throw new ValidationException("Description must be informed");
        }

        if (dto.getDescription().length() > 150) {
            throw new ValidationException("Description must have a maximum of 150 characters");
        }

        if (dto.getReservedQuantity() == null || dto.getReservedQuantity() <= 0) {
            throw new ValidationException("Reserved quantity must be greater than zero");
        }

        if (dto.getValue() == null || dto.getValue().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Value must be greater than zero");
        }
    }

    private void validateUserReservationEventSale(UserReservationEvent reservation) {
        if (reservation.getUserId() == null) {
            throw new ValidationException("User id must be informed");
        }

        if (reservation.getEventSaleId() == null) {
            throw new ValidationException("Event sale id must be informed");
        }

        if (reservation.getEventId() == null) {
            throw new ValidationException("Event id must be informed");
        }

        if (reservation.getEventSaleTransactionId() == null ||
                reservation.getEventSaleTransactionId().isBlank()) {
            throw new ValidationException("Event sale transaction id must be informed");
        }

        if (reservation.getDescription() == null ||
                reservation.getDescription().isBlank()) {
            throw new ValidationException("Description must be informed");
        }

        if (reservation.getReservedQuantity() == null ||
                reservation.getReservedQuantity() <= 0) {
            throw new ValidationException("Reserved quantity must be greater than zero");
        }

        if (reservation.getValue() == null ||
                reservation.getValue().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Value must be greater than zero");
        }
    }

    private void validateDeleteReservation(UserReservationDeleteDto dto) {
        if (dto == null) {
            throw new ValidationException("Delete reservation request must be informed");
        }

        if (dto.getUserId() == null) {
            throw new ValidationException("User id must be informed");
        }

        if (dto.getEventSaleId() == null) {
            throw new ValidationException("Event sale id must be informed");
        }
    }

    private void validateDeleteAllReservations(EventSaleDeleteRequestDto dto) {
        if (dto == null) {
            throw new ValidationException("Delete event sale request must be informed");
        }

        if (dto.getTransactionId() == null || dto.getTransactionId().isBlank()) {
            throw new ValidationException("Event sale transaction id must be informed");
        }
    }

    private void validateUserId(Long userId) {
        if (userId == null) {
            throw new ValidationException("User id must be informed");
        }
    }

    private void validateEventId(Long eventId) {
        if (eventId == null) {
            throw new ValidationException("Event id must be informed");
        }
    }

    private void validateEventSaleId(Long eventSaleId) {
        if (eventSaleId == null) {
            throw new ValidationException("Event sale id must be informed");
        }
    }
}
