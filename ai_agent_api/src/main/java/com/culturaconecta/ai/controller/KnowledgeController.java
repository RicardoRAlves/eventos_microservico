package com.culturaconecta.ai.controller;

import com.culturaconecta.ai.dto.KnowledgeAnswerResponse;
import com.culturaconecta.ai.dto.KnowledgeQuestionRequest;
import com.culturaconecta.ai.dto.KnowledgeTextIngestionRequest;
import com.culturaconecta.ai.entity.KnowledgeVisibility;
import com.culturaconecta.ai.service.KnowledgeAnswerService;
import com.culturaconecta.ai.service.KnowledgeFileIngestionService;
import com.culturaconecta.ai.service.KnowledgeIngestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/knowledge")
public class KnowledgeController {

    private final KnowledgeAnswerService answerService;
    private final KnowledgeIngestionService ingestionService;
    private final KnowledgeFileIngestionService fileIngestionService;


    @PostMapping("/ask")
    public KnowledgeAnswerResponse ask(
            @Valid @RequestBody KnowledgeQuestionRequest request) {

        return answerService.answerWithSources(
                request.organizationKey(),
                request.question()
        );
    }

    @PostMapping("/documents/text")
    public ResponseEntity<Map<String, Long>> ingestText(
            @Valid @RequestBody KnowledgeTextIngestionRequest request) {

        Long documentId = ingestionService.ingest(
                request.organizationKey(),
                request.title(),
                request.content()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of("documentId", documentId));
    }



    @PostMapping(
            value = "/documents/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<Map<String, Long>> uploadFile(
            @RequestParam("organizationKey")
            String organizationKey,

            @RequestParam("file")
            MultipartFile file,

            @RequestParam(
                    value = "visibility",
                    defaultValue = "PRIVATE"
            )
            KnowledgeVisibility visibility) {

        Long documentId = fileIngestionService.ingestFile(
                organizationKey,
                file,
                visibility
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of("documentId", documentId));
    }

}

