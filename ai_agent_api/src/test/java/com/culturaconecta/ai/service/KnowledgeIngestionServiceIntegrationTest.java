package com.culturaconecta.ai.service;

import com.culturaconecta.ai.entity.KnowledgeVisibility;
import com.culturaconecta.ai.repository.KnowledgeBaseRepository;
import com.culturaconecta.ai.repository.KnowledgeChunkRepository;
import com.culturaconecta.ai.repository.KnowledgeDocumentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class KnowledgeIngestionServiceIntegrationTest {

    @Autowired
    private KnowledgeIngestionService ingestionService;

    @Autowired
    private KnowledgeBaseRepository baseRepository;

    @Autowired
    private KnowledgeDocumentRepository documentRepository;

    @Autowired
    private KnowledgeChunkRepository chunkRepository;

    @Test
    void shouldPersistDocumentAndEmbeddings() {

        String organizationKey = "org-rag-test-" +
                java.util.UUID.randomUUID();

        String content = IntStream.rangeClosed(1, 300)
                .mapToObj(i -> "palavra" + i)
                .collect(Collectors.joining(" "));

        Long documentId = ingestionService.ingest(
                organizationKey,
                "Documento de teste RAG",
                content
        );

        var base = baseRepository
                .findByOrganizationKey(organizationKey)
                .orElseThrow();

        var document = documentRepository
                .findByIdAndKnowledgeBaseId(documentId, base.getId())
                .orElseThrow();

        var chunks = chunkRepository
                .findByKnowledgeDocumentIdOrderByChunkIndexAsc(
                        document.getId()
                );

        assertEquals(content, document.getContent());
        assertEquals(64, document.getContentHash().length());
        assertEquals(3, chunks.size());

        for (int i = 0; i < chunks.size(); i++) {
            assertEquals(i, chunks.get(i).getChunkIndex());
            assertNotNull(chunks.get(i).getEmbedding());
            assertEquals(768, chunks.get(i).getEmbedding().length);
        }

        System.out.println("Documento salvo: " + documentId);
        System.out.println("Organização: " + organizationKey);
        System.out.println("Chunks persistidos: " + chunks.size());
        System.out.println("Dimensões: " +
                chunks.get(0).getEmbedding().length);
    }


    @Test
    void shouldPersistDocumentVisibility() {

        String organizationKey =
                "org-visibility-" + java.util.UUID.randomUUID();

        Long publicDocumentId = ingestionService.ingest(
                organizationKey,
                "História da capoeira",
                "A capoeira é uma manifestação cultural brasileira."
        );

        Long privateDocumentId = ingestionService.ingest(
                organizationKey,
                "Critérios internos de graduação",
                "Os critérios de graduação são definidos internamente pelo mestre.",
                KnowledgeVisibility.PRIVATE
        );

        var publicDocument = documentRepository
                .findById(publicDocumentId)
                .orElseThrow();

        var privateDocument = documentRepository
                .findById(privateDocumentId)
                .orElseThrow();

        assertEquals(
                KnowledgeVisibility.PUBLIC,
                publicDocument.getVisibility()
        );

        assertEquals(
                KnowledgeVisibility.PRIVATE,
                privateDocument.getVisibility()
        );

        System.out.println(
                "Documento público: " + publicDocument.getVisibility()
        );

        System.out.println(
                "Documento privado: " + privateDocument.getVisibility()
        );
    }


}
