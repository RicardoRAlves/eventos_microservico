package com.culturaconecta.ai.controller;

import com.culturaconecta.ai.dto.AiChatInputDto;
import com.culturaconecta.ai.dto.AiChatResponseDto;
import com.culturaconecta.ai.service.AiChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/ai/chat")
public class AiChatController {

    private final AiChatService service;

    @PostMapping
    public ResponseEntity<AiChatResponseDto> questionAI(
            @Valid @RequestBody AiChatInputDto input) {

        log.info("AI chat request received");

        String response = service.question(
                input.getMessage(),
                input.getContext()
        );

        log.info("AI response generated successfully");

        return ResponseEntity.ok(
                new AiChatResponseDto(response)
        );
    }
}

