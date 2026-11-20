package com.culturaconecta.ai.dto;

import java.util.List;

public record KnowledgeAnswerResponse(
        String answer,
        KnowledgeSourceType sourceType,
        List<KnowledgeSource> sources
) {

    public KnowledgeAnswerResponse(String answer) {
        this(answer, KnowledgeSourceType.NONE, List.of());
    }
}


