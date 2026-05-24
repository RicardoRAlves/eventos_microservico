package com.br.capoeira.eventos.user_api.controller;

import com.br.capoeira.eventos.user_api.dto.UserReservationCountUsersResponseDto;
import com.br.capoeira.eventos.user_api.dto.UserReservationDeleteDto;
import com.br.capoeira.eventos.user_api.dto.UserReservationEventRequestDto;
import com.br.capoeira.eventos.user_api.dto.UserReservationEventResponseDto;
import com.br.capoeira.eventos.user_api.service.UserReservationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users/event-reservations")
@Slf4j
public class UserReservationEventController {

    private final UserReservationService service;

    @GetMapping("/{userId}")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<List<UserReservationEventResponseDto>>
    findAllReservationEventsByUser(
            @PathVariable Long userId
    ) {
        log.info("Finding all reservation events for userId={}", userId);

        var reservations = service.findAllReservedEventsByUserId(userId);

        log.info("Returning {} reservation events", reservations.size());

        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/event/{eventId}")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<List<UserReservationEventResponseDto>>
    findAllReservationEventsByEvent(
            @PathVariable Long eventId
    ) {
        log.info("Finding all reservation events for eventId={}", eventId);

        var reservations = service.findAllReservedEventsByEventId(eventId);

        log.info("Returning {} reservation events for all users", reservations.size());

        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/count/users/event/{eventId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<UserReservationCountUsersResponseDto>
    countAllUsersByEventId(
            @PathVariable Long eventId
    ) {
        log.info("Counting all reservation events by eventId={}", eventId);

        var count = service.countingAllUsersReservedByEventId(eventId);

        log.info("Returning count of users reserved for event. count={}", count);

        return ResponseEntity.ok(new UserReservationCountUsersResponseDto(count));
    }

    @GetMapping("/count/users/event/sale/{eventSaleId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<UserReservationCountUsersResponseDto>
    countAllUsersByEventSaleId(
            @PathVariable Long eventSaleId
    ) {
        log.info("Counting all reservation events by eventSaleId={}", eventSaleId);

        var count = service.countingAllReservesByEventSaleId(eventSaleId);

        log.info("Returning count of users reserved for event sale. count={}", count);

        return ResponseEntity.ok(new UserReservationCountUsersResponseDto(count));
    }

    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> requestReserve(
            @RequestBody @Valid UserReservationEventRequestDto dto
    ) {
        log.info(
                "Requesting reservation for event sale. userId={}, eventSaleTransactionId={}",
                dto.getUserId(),
                dto.getEventSaleTransactionId()
        );

        service.requestReservation(dto);

        log.info("Reservation request accepted");

        return ResponseEntity.accepted().build();
    }

    @DeleteMapping
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> deleteReservationByUserIdAndEventSaleId(
            @RequestBody @Valid UserReservationDeleteDto dto
    ) {
        log.info(
                "Deleting reserved event sale. userId={}, eventSaleId={}",
                dto.getUserId(),
                dto.getEventSaleId()
        );

        service.deleteReservationsByEventSaleId(dto);

        log.info("Reserved event sale deleted successfully");

        return ResponseEntity.noContent().build();
    }
}
