package com.culturaconecta.ai.dto;

import jakarta.validation.constraints.NotBlank;

public record KnowledgeTextIngestionRequest(

        @NotBlank
        String organizationKey,

        @NotBlank
        String title,

        @NotBlank
        String content

) {}
