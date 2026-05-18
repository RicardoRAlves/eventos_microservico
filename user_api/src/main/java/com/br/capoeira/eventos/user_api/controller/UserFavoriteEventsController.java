package com.br.capoeira.eventos.user_api.controller;

import com.br.capoeira.eventos.user_api.dto.UserFavoriteCreateRequestDto;
import com.br.capoeira.eventos.user_api.dto.UserFavoriteDeleteDto;
import com.br.capoeira.eventos.user_api.dto.UserFavoriteResponseDto;
import com.br.capoeira.eventos.user_api.service.UserFavoriteEventsService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users/favorites/events")
@Slf4j
public class UserFavoriteEventsController {

    private final UserFavoriteEventsService service;

    @GetMapping("/{userId}")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<List<UserFavoriteResponseDto>>
    findAllFavoritesEventsByUser(
            @PathVariable Long userId
    ) {

        log.info(
                "Finding all favorite events for user_id {}",
                userId
        );

        var favorites = service.findAllEventsByUserId(userId);

        log.info(
                "Returning {} favorite events",
                favorites.size()
        );

        return ResponseEntity.ok(favorites);
    }

    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<UserFavoriteResponseDto>
    createNewFavorite(
            @RequestBody @Valid UserFavoriteCreateRequestDto dto
    ) {

        log.info(
                "Creating new favorite event {} for user {}",
                dto.getEventId(),
                dto.getUserId()
        );

        var response = service.createFavorite(dto);

        log.info(
                "Favorite event created with success"
        );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void>
    deleteFavoriteByUserIdAndEventId(
            @RequestBody @Valid UserFavoriteDeleteDto dto
    ) {

        log.info(
                "Deleting favorite event {} for user {}",
                dto.getEventId(),
                dto.getUserId()
        );

        service.deleteFavoriteById(dto);

        log.info(
                "Favorite event deleted with success"
        );

        return ResponseEntity.noContent().build();
    }
}
