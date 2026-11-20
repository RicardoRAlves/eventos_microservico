package com.culturaconecta.ai.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class KnowledgeProcessingServiceIntegrationTest {

    @Autowired
    private KnowledgeProcessingService processingService;

    @Test
    void shouldGenerateEmbeddingsForAllChunks() {

        String content = IntStream.rangeClosed(1, 300)
                .mapToObj(i -> "palavra" + i)
                .collect(Collectors.joining(" "));

        var chunks = processingService.process(content);

        assertEquals(3, chunks.size());

        for (int i = 0; i < chunks.size(); i++) {
            var chunk = chunks.get(i);

            assertEquals(i, chunk.index());
            assertNotNull(chunk.content());
            assertFalse(chunk.content().isBlank());

            assertNotNull(chunk.embedding());
            assertEquals(768, chunk.embedding().length);
        }

        System.out.println("Chunks processados: " + chunks.size());
        System.out.println("Dimensões por embedding: "
                + chunks.get(0).embedding().length);
    }
}

