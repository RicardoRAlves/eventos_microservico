package com.culturaconecta.ai.controller;

import com.culturaconecta.ai.dto.EventSummaryRequestDto;
import com.culturaconecta.ai.dto.EventSummaryResponseDto;
import com.culturaconecta.ai.service.EventSummaryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ai/events")
@RequiredArgsConstructor
public class EventSummaryController {

    private final EventSummaryService eventSummaryService;

    @PostMapping("/summary")
    public ResponseEntity<EventSummaryResponseDto> summarize(
            @Valid @RequestBody EventSummaryRequestDto request
    ) {
        EventSummaryResponseDto response =
                eventSummaryService.summarize(request);

        return ResponseEntity.ok(response);
    }
}
