package com.br.capoeira.eventos.evento_api.controller;

import com.br.capoeira.eventos.evento_api.dto.EventDto;
import com.br.capoeira.eventos.evento_api.mapper.EventoMapper;
import com.br.capoeira.eventos.evento_api.model.Event;
import com.br.capoeira.eventos.evento_api.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1/evento")
public class EventController {
    private final EventoMapper eventoMapper;
    private final EventService eventService;


    @GetMapping("/all")
    @Operation(summary = "Retorna uma lista de Eventos", description = "Responsavel por retornar todos os eventos na base de dados",
            responses = @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Event.class)))
    )
    public ResponseEntity<String> buscaTodosEventos(){
        eventService.findAllEvents();
        return ResponseEntity.ok("Added request to queue");
    }

    @PostMapping("/create")
    @Operation(summary = "Cadastra um novo evento", description = "Contem a operação para criar um novo evento",
            responses = @ApiResponse(responseCode = "201", description = "Recurso criado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class)))
    )
    public ResponseEntity<Event> createEvent(@RequestBody EventDto eventDto){
        log.info("Event recebido, {}", eventDto);
        var event = eventoMapper.eventoDtoToEvento(eventDto);
        eventService.sendingNewEventToProcessor(event);
        return ResponseEntity.status(HttpStatus.CREATED).body(event);
    }

    @PostMapping("/upload")
    @Operation(summary = "Send Image to S3Bucket", description = "Endpoint is responsable to feed S3 buckets with image and return the URI",
            responses = @ApiResponse(responseCode = "201", description = "Resource created with success",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class)))
    )
    public ResponseEntity<String> uploadImage(
            @RequestParam("image") MultipartFile file){
        var photoPath = eventService.updatePhoto(file);
        return ResponseEntity.ok(photoPath);
    }

    @PutMapping
    @Operation(summary = "Send an request to update Event", description = "Endpoint is responsable to update Eventitself",
            responses = @ApiResponse(responseCode = "200", description = "Resource sent with success",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class)))
    )
    public ResponseEntity<Event> updateEvent(@RequestBody Event event){
        log.info("Event received, {}", event);
        eventService.updateEvent(event);
        return ResponseEntity.ok(event);
    }
}
