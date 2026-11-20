package com.culturaconecta.ai.service;

import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class KnowledgeChunkingServiceTest {

    private final KnowledgeChunkingService service =
            new KnowledgeChunkingService();

    @Test
    void shouldSplitDocumentWithOverlap() {
        String content = IntStream.rangeClosed(1, 300)
                .mapToObj(i -> "palavra" + i)
                .reduce((a, b) -> a + " " + b)
                .orElseThrow();

        var chunks = service.split(content);

        assertEquals(3, chunks.size());
        assertTrue(chunks.get(0).endsWith("palavra150"));
        assertTrue(chunks.get(1).startsWith("palavra121"));
        assertTrue(chunks.get(2).startsWith("palavra241"));
        assertTrue(chunks.get(2).endsWith("palavra300"));
    }

    @Test
    void shouldRejectEmptyDocument() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.split(" ")
        );
    }
}

