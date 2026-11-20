package com.culturaconecta.ai.dto;

import com.culturaconecta.ai.entity.KnowledgeVisibility;

public record KnowledgeSearchResult(
        Long chunkId,
        Long documentId,
        String organizationKey,
        String documentTitle,
        KnowledgeVisibility visibility,
        String chunkContent
) {
}

