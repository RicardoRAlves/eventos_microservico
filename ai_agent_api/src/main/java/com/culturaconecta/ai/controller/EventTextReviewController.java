package com.culturaconecta.ai.controller;

import com.culturaconecta.ai.dto.EventTextReviewRequestDto;
import com.culturaconecta.ai.dto.EventTextReviewResponseDto;
import com.culturaconecta.ai.service.EventTextReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ai/events")
@RequiredArgsConstructor
public class EventTextReviewController {

    private final EventTextReviewService eventTextReviewService;

    @PostMapping("/review")
    public ResponseEntity<EventTextReviewResponseDto> review(
            @Valid @RequestBody EventTextReviewRequestDto request
    ) {
        EventTextReviewResponseDto response =
                eventTextReviewService.review(request);

        return ResponseEntity.ok(response);
    }
}
