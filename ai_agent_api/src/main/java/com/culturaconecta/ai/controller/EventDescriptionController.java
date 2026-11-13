
package com.culturaconecta.ai.controller;

import com.culturaconecta.ai.dto.EventDescriptionRequestDto;
import com.culturaconecta.ai.dto.EventDescriptionResponseDto;
import com.culturaconecta.ai.service.EventDescriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/ai/events")
public class EventDescriptionController {

    private final EventDescriptionService eventDescriptionService;

    @PostMapping("/description")
    public ResponseEntity<EventDescriptionResponseDto> generateDescription(
            @Valid @RequestBody EventDescriptionRequestDto request) {

        log.info("Event description generation requested");

        String description =
                eventDescriptionService.generateDescription(request);

        log.info("Event description generated successfully");

        return ResponseEntity.ok(
                new EventDescriptionResponseDto(description)
        );
    }
}

