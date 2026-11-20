package com.culturaconecta.ai.service;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

@Service
public class KnowledgeEmbeddingService {

    private static final int EMBEDDING_DIMENSIONS = 768;

    private final EmbeddingModel embeddingModel;

    public KnowledgeEmbeddingService(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    public float[] generateEmbedding(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException(
                    "O texto para embedding não pode estar vazio."
            );
        }

        float[] embedding = embeddingModel.embed(text);

        if (embedding == null ||
                embedding.length != EMBEDDING_DIMENSIONS) {
            throw new IllegalStateException(
                    "O modelo retornou um embedding com dimensões inválidas."
            );
        }

        return embedding;
    }
}

