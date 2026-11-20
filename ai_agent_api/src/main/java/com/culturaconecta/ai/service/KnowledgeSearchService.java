
package com.culturaconecta.ai.service;

import com.culturaconecta.ai.dto.KnowledgeSearchResult;
import com.culturaconecta.ai.entity.KnowledgeChunk;
import com.culturaconecta.ai.entity.KnowledgeDocument;
import com.culturaconecta.ai.repository.KnowledgeChunkRepository;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
public class KnowledgeSearchService {

    private static final int MIN_LIMIT = 1;
    private static final int MAX_LIMIT = 20;

    private static final double MIN_DISTANCE = 0.0;
    private static final double MAX_DISTANCE = 2.0;

    private final KnowledgeEmbeddingService embeddingService;
    private final KnowledgeChunkRepository chunkRepository;
    private final MeterRegistry meterRegistry;

    public KnowledgeSearchService(
            KnowledgeEmbeddingService embeddingService,
            KnowledgeChunkRepository chunkRepository,
            MeterRegistry meterRegistry) {

        this.embeddingService = embeddingService;
        this.chunkRepository = chunkRepository;
        this.meterRegistry = meterRegistry;
    }

    @Transactional(readOnly = true)
    public List<KnowledgeChunk> search(
            String organizationKey,
            String question,
            int limit) {

        validateSearchParameters(organizationKey, question, limit);

        String vector = generateVector(question);

        return measureDatabaseQuery(() ->
                chunkRepository.searchSimilar(
                        organizationKey,
                        vector,
                        limit
                )
        );
    }

    @Transactional(readOnly = true)
    public List<KnowledgeSearchResult> searchWithSources(
            String organizationKey,
            String question,
            int limit) {

        List<KnowledgeChunk> chunks = search(
                organizationKey,
                question,
                limit
        );

        return mapToSearchResults(chunks);
    }

    @Transactional(readOnly = true)
    public List<KnowledgeChunk> searchWithThreshold(
            String organizationKey,
            String question,
            int limit,
            double maxDistance) {

        validateSearchParameters(organizationKey, question, limit);
        validateDistance(maxDistance);

        String vector = generateVector(question);

        return measureDatabaseQuery(() ->
                chunkRepository.searchSimilarWithThreshold(
                        organizationKey,
                        vector,
                        maxDistance,
                        limit
                )
        );
    }

    @Transactional(readOnly = true)
    public List<KnowledgeSearchResult> searchWithSourcesAndThreshold(
            String organizationKey,
            String question,
            int limit,
            double maxDistance) {

        List<KnowledgeChunk> chunks = searchWithThreshold(
                organizationKey,
                question,
                limit,
                maxDistance
        );

        return mapToSearchResults(chunks);
    }

    @Transactional(readOnly = true)
    public List<Object[]> getSimilarityDistances(
            String organizationKey,
            String question,
            int limit) {

        validateSearchParameters(organizationKey, question, limit);

        String vector = generateVector(question);

        return measureDatabaseQuery(() ->
                chunkRepository.findSimilarityDistances(
                        organizationKey,
                        vector,
                        limit
                )
        );
    }

    private void validateSearchParameters(
            String organizationKey,
            String question,
            int limit) {

        if (organizationKey == null || organizationKey.isBlank()) {
            throw new IllegalArgumentException(
                    "Organização obrigatória."
            );
        }

        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException(
                    "Pergunta obrigatória."
            );
        }

        if (limit < MIN_LIMIT || limit > MAX_LIMIT) {
            throw new IllegalArgumentException(
                    "O limite deve estar entre 1 e 20."
            );
        }
    }

    private void validateDistance(double maxDistance) {

        if (!Double.isFinite(maxDistance)
                || maxDistance < MIN_DISTANCE
                || maxDistance > MAX_DISTANCE) {

            throw new IllegalArgumentException(
                    "A distância deve estar entre 0 e 2."
            );
        }
    }

    private String generateVector(String question) {

        long start = System.nanoTime();

        try {
            float[] embedding =
                    embeddingService.generateEmbedding(question);

            return IntStream.range(0, embedding.length)
                    .mapToObj(i -> Float.toString(embedding[i]))
                    .collect(Collectors.joining(",", "[", "]"));

        } finally {
            long elapsed = System.nanoTime() - start;

            meterRegistry.timer("rag.embedding.duration")
                    .record(elapsed, TimeUnit.NANOSECONDS);

            log.info(
                    "RAG embedding finished: durationMs={}",
                    TimeUnit.NANOSECONDS.toMillis(elapsed)
            );
        }
    }

    private <T> T measureDatabaseQuery(Supplier<T> query) {

        long start = System.nanoTime();

        try {
            return query.get();

        } finally {
            long elapsed = System.nanoTime() - start;

            meterRegistry.timer("rag.database.duration")
                    .record(elapsed, TimeUnit.NANOSECONDS);

            log.info(
                    "RAG database query finished: durationMs={}",
                    TimeUnit.NANOSECONDS.toMillis(elapsed)
            );
        }
    }

    private List<KnowledgeSearchResult> mapToSearchResults(
            List<KnowledgeChunk> chunks) {

        return chunks.stream()
                .map(this::mapToSearchResult)
                .toList();
    }

    private KnowledgeSearchResult mapToSearchResult(
            KnowledgeChunk chunk) {

        KnowledgeDocument document =
                chunk.getKnowledgeDocument();

        return new KnowledgeSearchResult(
                chunk.getId(),
                document.getId(),
                document.getKnowledgeBase().getOrganizationKey(),
                document.getTitle(),
                document.getVisibility(),
                chunk.getContent()
        );
    }
}
