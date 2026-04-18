package com.br.capoeira.eventos.event_api.controller;

import com.br.capoeira.eventos.event_api.dto.EventDto;
import com.br.capoeira.eventos.event_api.mapper.EventoMapper;
import com.br.capoeira.eventos.event_api.model.Event;
import com.br.capoeira.eventos.event_api.service.EventService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/evento")
public class EventController {
    private final EventoMapper eventoMapper;
    private final EventService eventService;

    @GetMapping("/all")
    public ResponseEntity<String> buscaTodosEventos(){
        eventService.findAllEvents();
        return ResponseEntity.ok("Added request to queue");
    }

    @PostMapping("/create")
    public ResponseEntity<Event> createEvent(@RequestBody EventDto eventDto){
        log.info("New Event created, {}", eventDto);
        var event = eventoMapper.eventoDtoToEvento(eventDto);
        eventService.sendingNewEventToProcessor(event);
        return ResponseEntity.status(HttpStatus.CREATED).body(event);
    }

    @PostMapping(value ="/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadImage(
            @Parameter(description = "Select the image for upload",
                    schema = @Schema(type = "string", format = "binary"))
            @RequestParam("image") MultipartFile file){
        var photoPath = eventService.updatePhoto(file);
        return ResponseEntity.ok(photoPath);
    }

    @PutMapping
    public ResponseEntity<Event> updateEvent(@RequestBody Event event){
        log.info("Event updated, {}", event);
        eventService.updateEvent(event);
        return ResponseEntity.ok(event);
    }
}
