
package com.culturaconecta.ai.controller;

import com.culturaconecta.ai.dto.KnowledgeQuestionRequest;
import com.culturaconecta.ai.service.KnowledgeSearchService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Profile("local")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/knowledge/diagnostics")
public class KnowledgeDiagnosticsController {

    private final KnowledgeSearchService searchService;

    @PostMapping("/similarity")
    public List<SimilarityResult> similarity(
            @Valid @RequestBody KnowledgeQuestionRequest request) {

        List<Object[]> results =
                searchService.getSimilarityDistances(
                        request.organizationKey(),
                        request.question(),
                        10
                );

        return results.stream()
                .map(row -> new SimilarityResult(
                        (String) row[0],
                        ((Number) row[1]).doubleValue()
                ))
                .toList();
    }

    public record SimilarityResult(
            String documentTitle,
            double distance
    ) {}
}
