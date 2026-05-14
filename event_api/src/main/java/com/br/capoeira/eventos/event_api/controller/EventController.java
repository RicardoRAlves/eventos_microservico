package com.br.capoeira.eventos.event_api.controller;

import com.br.capoeira.eventos.event_api.dto.EventCreateRequestDto;
import com.br.capoeira.eventos.event_api.dto.EventResponseDto;
import com.br.capoeira.eventos.event_api.dto.EventUpdateRequestDto;
import com.br.capoeira.eventos.event_api.dto.UploadImageResponseDto;
import com.br.capoeira.eventos.event_api.service.EventService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api/v1/evento")
public class EventController {

    private final EventService eventService;

    @GetMapping("/all")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<String> requestAllEvents() {
        eventService.findAllEvents();
        return ResponseEntity.ok("Request sent to queue");
    }

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<EventResponseDto> createEvent(
            @Valid @RequestBody EventCreateRequestDto eventDto) {
        log.info("New event creation requested");
        var event = eventService.sendingNewEventToProcessor(eventDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(event);
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<UploadImageResponseDto> uploadImage(
            @Parameter(
                    description = "Select the image for upload",
                    schema = @Schema(type = "string", format = "binary")
            )
            @RequestParam("image") MultipartFile file
    ) {
        var photoPath = eventService.updatePhoto(file);
        log.info("returning image {}", photoPath);
        return ResponseEntity.ok(new UploadImageResponseDto(photoPath));
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<EventResponseDto> updateEvent(
            @Valid @RequestBody EventUpdateRequestDto eventDto) {
        log.info("Event update requested");
        var event = eventService.updateEvent(eventDto);
        return ResponseEntity.ok(event);
    }
}
