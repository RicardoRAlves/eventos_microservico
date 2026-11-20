
package com.culturaconecta.ai.repository;

import com.culturaconecta.ai.entity.KnowledgeBase;
import com.culturaconecta.ai.entity.KnowledgeDocument;
import com.culturaconecta.ai.entity.KnowledgeChunk;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.test.database.replace=none"
})
class KnowledgeChunkRepositoryIntegrationTest {

    @Autowired
    private KnowledgeBaseRepository baseRepository;

    @Autowired
    private KnowledgeDocumentRepository documentRepository;

    @Autowired
    private KnowledgeChunkRepository chunkRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldPersistAndRetrieveEmbedding() {
        KnowledgeBase base = new KnowledgeBase();
        base.setOrganizationKey("rag-test-org");

        base = baseRepository.saveAndFlush(base);

        KnowledgeDocument document = new KnowledgeDocument();
        document.setKnowledgeBase(base);
        document.setTitle("Teste de capoeira");
        document.setContent("A capoeira é uma manifestação cultural.");
        document.setContentHash("a".repeat(64));

        document = documentRepository.saveAndFlush(document);

        float[] embedding = new float[768];
        embedding[0] = 0.15f;
        embedding[1] = 0.25f;
        embedding[767] = 0.75f;

        KnowledgeChunk chunk = new KnowledgeChunk();
        chunk.setKnowledgeDocument(document);
        chunk.setChunkIndex(0);
        chunk.setContent("A capoeira é uma manifestação cultural.");
        chunk.setEmbedding(embedding);

        Long chunkId = chunkRepository.saveAndFlush(chunk).getId();

        entityManager.clear();

        KnowledgeChunk recovered = chunkRepository.findById(chunkId)
                .orElseThrow();

        assertNotNull(recovered.getEmbedding());
        assertEquals(768, recovered.getEmbedding().length);
        assertArrayEquals(embedding, recovered.getEmbedding(), 0.00001f);
    }


    @Test
    void shouldIsolateChunksBetweenOrganizations() {
        KnowledgeBase baseA = new KnowledgeBase();
        baseA.setOrganizationKey("grupo-bonfim");
        baseA = baseRepository.saveAndFlush(baseA);

        KnowledgeBase baseB = new KnowledgeBase();
        baseB.setOrganizationKey("grupo-angola");
        baseB = baseRepository.saveAndFlush(baseB);

        KnowledgeDocument documentA = new KnowledgeDocument();
        documentA.setKnowledgeBase(baseA);
        documentA.setTitle("História do Grupo Bonfim");
        documentA.setContent("Conhecimento exclusivo do Grupo Bonfim.");
        documentA.setContentHash("b".repeat(64));
        documentA = documentRepository.saveAndFlush(documentA);

        KnowledgeChunk chunkA = new KnowledgeChunk();
        chunkA.setKnowledgeDocument(documentA);
        chunkA.setChunkIndex(0);
        chunkA.setContent("Conhecimento exclusivo do Grupo Bonfim.");

        Long chunkId = chunkRepository.saveAndFlush(chunkA).getId();

        entityManager.clear();

        assertTrue(
                chunkRepository
                        .findByIdAndKnowledgeDocumentKnowledgeBaseId(
                                chunkId, baseA.getId()
                        )
                        .isPresent()
        );

        assertTrue(
                chunkRepository
                        .findByIdAndKnowledgeDocumentKnowledgeBaseId(
                                chunkId, baseB.getId()
                        )
                        .isEmpty()
        );
    }

}

