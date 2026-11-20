package com.culturaconecta.ai.dto;

import jakarta.validation.constraints.NotBlank;

public record KnowledgeQuestionRequest(
        @NotBlank String organizationKey,
        @NotBlank String question
) {
}

