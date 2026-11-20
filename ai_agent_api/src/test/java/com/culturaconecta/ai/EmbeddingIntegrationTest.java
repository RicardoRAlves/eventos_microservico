
package com.culturaconecta.ai;

import org.junit.jupiter.api.Test;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class EmbeddingIntegrationTest {

    @Autowired
    private EmbeddingModel embeddingModel;

    @Test
    void shouldGenerate768DimensionEmbedding() {

        String text = "A capoeira é uma manifestação cultural brasileira.";

        float[] embedding = embeddingModel.embed(text);

        assertNotNull(embedding);
        assertEquals(768, embedding.length);

        System.out.println("Modelo de embedding funcionando!");
        System.out.println("Dimensões: " + embedding.length);
        System.out.println("Primeiro valor: " + embedding[0]);
    }
}

