package com.culturaconecta.ai.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class KnowledgeProcessingService {

    private final KnowledgeChunkingService chunkingService;
    private final KnowledgeEmbeddingService embeddingService;

    public KnowledgeProcessingService(
            KnowledgeChunkingService chunkingService,
            KnowledgeEmbeddingService embeddingService) {

        this.chunkingService = chunkingService;
        this.embeddingService = embeddingService;
    }

    public List<ProcessedChunk> process(String content) {

        List<String> chunks = chunkingService.split(content);
        List<ProcessedChunk> processedChunks = new ArrayList<>();

        for (int index = 0; index < chunks.size(); index++) {

            String chunkContent = chunks.get(index);

            float[] embedding =
                    embeddingService.generateEmbedding(chunkContent);

            processedChunks.add(
                    new ProcessedChunk(index, chunkContent, embedding)
            );
        }

        return processedChunks;
    }

    public record ProcessedChunk(
            int index,
            String content,
            float[] embedding
    ) {}
}
