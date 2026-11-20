package com.culturaconecta.ai.service;

import com.culturaconecta.ai.dto.KnowledgeSearchResult;
import com.culturaconecta.ai.entity.KnowledgeChunk;
import com.culturaconecta.ai.entity.KnowledgeVisibility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class KnowledgeSearchServiceIntegrationTest {

    @Autowired
    private KnowledgeIngestionService ingestionService;

    @Autowired
    private KnowledgeSearchService searchService;


    @Test
    void shouldKeepPrivateDocumentsWithinTheirOrganization() {

        String suffix = UUID.randomUUID().toString();

        String organizationA = "org-a-" + suffix;
        String organizationB = "org-b-" + suffix;

        String contentA =
                "Documento privado da organização A: " + suffix;

        String contentB =
                "Documento privado da organização B: " + suffix;

        ingestionService.ingest(
                organizationA,
                "Documento privado A",
                contentA,
                KnowledgeVisibility.PRIVATE
        );

        ingestionService.ingest(
                organizationB,
                "Documento privado B",
                contentB,
                KnowledgeVisibility.PRIVATE
        );

        List<KnowledgeSearchResult> resultsA =
                searchService.searchWithSources(
                        organizationA,
                        "Documento privado da organização A",
                        20
                );

        List<KnowledgeSearchResult> resultsB =
                searchService.searchWithSources(
                        organizationB,
                        "Documento privado da organização B",
                        20
                );

        assertTrue(resultsA.stream().anyMatch(
                result -> result.chunkContent().contains(contentA)
        ));

        assertFalse(resultsA.stream().anyMatch(
                result -> result.chunkContent().contains(contentB)
        ));

        assertTrue(resultsB.stream().anyMatch(
                result -> result.chunkContent().contains(contentB)
        ));

        assertFalse(resultsB.stream().anyMatch(
                result -> result.chunkContent().contains(contentA)
        ));
    }


    @Test
    void shouldRankMostRelevantDocumentFirst() {

        String organizationKey = "org-ranking-" + UUID.randomUUID();

        System.out.println(
                "ORGANIZATION_KEY_TEST: " + organizationKey
        );

        Long berimbauDocumentId = ingestionService.ingest(
                organizationKey,
                "Instrumentos da capoeira",
                "O berimbau é um instrumento musical utilizado na capoeira. "
                        + "Ele possui uma verga de madeira, arame e cabaça. "
                        + "Seu som ajuda a conduzir o ritmo da roda.",
                KnowledgeVisibility.PRIVATE
        );

        ingestionService.ingest(
                organizationKey,
                "Regras do futebol",
                "O futebol é um esporte coletivo disputado entre duas equipes. "
                        + "Os jogadores tentam marcar gols utilizando uma bola.",
                KnowledgeVisibility.PRIVATE
        );

        ingestionService.ingest(
                organizationKey,
                "Culinária brasileira",
                "A feijoada é um prato tradicional brasileiro preparado "
                        + "com feijão preto e diferentes acompanhamentos.",
                KnowledgeVisibility.PRIVATE
        );

        List<KnowledgeSearchResult> results =
                searchService.searchWithSources(
                        organizationKey,
                        "Qual instrumento musical conduz o ritmo da roda de capoeira?",
                        20
                );

        List<KnowledgeSearchResult> ownResults = results.stream()
                .filter(result ->
                        organizationKey.equals(result.organizationKey()))
                .toList();

        assertEquals(
                3,
                ownResults.size(),
                "A busca deveria retornar os três documentos da organização."
        );

        assertEquals(
                berimbauDocumentId,
                ownResults.get(0).documentId(),
                "O documento sobre berimbau deveria ser o mais relevante."
        );

        for (int i = 0; i < ownResults.size(); i++) {
            KnowledgeSearchResult result = ownResults.get(i);

            System.out.println(
                    "Posição " + (i + 1)
                            + " | Documento: " + result.documentTitle()
                            + " | ID: " + result.documentId()
            );
        }
    }



    @Test
    void shouldRespectDocumentVisibilityAcrossOrganizations() {

        String suffix = UUID.randomUUID().toString();

        String organizationA = "org-private-" + suffix;
        String organizationB = "org-public-" + suffix;

        ingestionService.ingest(
                organizationA,
                "Regras internas de graduação",
                "A graduação interna utiliza uma sequência exclusiva de cordas.",
                KnowledgeVisibility.PRIVATE
        );

        ingestionService.ingest(
                organizationB,
                "Instrumentos da capoeira",
                "O berimbau é um instrumento musical utilizado na roda de capoeira.",
                KnowledgeVisibility.PUBLIC
        );

        var resultsForA = searchService.search(
                organizationA,
                "Qual instrumento musical é utilizado na capoeira?",
                20
        );

        assertTrue(resultsForA.stream().anyMatch(
                chunk -> chunk.getContent().contains("berimbau")
        ));

        var resultsForB = searchService.search(
                organizationB,
                "Como funciona a graduação interna?",
                20
        );

        assertFalse(resultsForB.stream().anyMatch(
                chunk -> chunk.getContent().contains("sequência exclusiva")
        ));
    }

    @Test
    void shouldReturnKnowledgeSourcesWithoutLazyInitializationException() {

        String organizationKey =
                "org-source-" + UUID.randomUUID();

        ingestionService.ingest(
                organizationKey,
                "História do berimbau",
                "O berimbau é um instrumento musical utilizado na capoeira.",
                KnowledgeVisibility.PUBLIC
        );

        List<KnowledgeSearchResult> results =
                searchService.searchWithSources(
                        organizationKey,
                        "Qual instrumento musical é utilizado na capoeira?",
                        20
                );

        KnowledgeSearchResult result = results.stream()
                .filter(r -> organizationKey.equals(r.organizationKey()))
                .filter(r -> r.chunkContent().contains("berimbau"))
                .findFirst()
                .orElseThrow();

        assertNotNull(result.chunkId());
        assertNotNull(result.documentId());

        assertEquals(
                "História do berimbau",
                result.documentTitle()
        );

        assertEquals(
                KnowledgeVisibility.PUBLIC,
                result.visibility()
        );

        assertEquals(
                organizationKey,
                result.organizationKey()
        );
    }


    @Test
    void shouldFilterDocumentsBySimilarityThreshold() {

        String organizationKey =
                "org-threshold-" + UUID.randomUUID();

        String question =
                "Qual instrumento musical orienta os movimentos dos capoeiristas durante a roda?";

        ingestionService.ingest(
                organizationKey,
                "Instrumentos da capoeira",
                "O berimbau é um instrumento musical que conduz "
                        + "o ritmo da roda de capoeira.",
                KnowledgeVisibility.PRIVATE
        );

        ingestionService.ingest(
                organizationKey,
                "Regras do futebol",
                "O futebol é um esporte coletivo disputado "
                        + "por duas equipes de onze jogadores.",
                KnowledgeVisibility.PRIVATE
        );

        // Busca original, sem limiar
        List<KnowledgeSearchResult> withoutThreshold =
                searchService.searchWithSources(
                        organizationKey,
                        question,
                        10
                );

        // Busca com distância cosseno máxima de 0.50
        List<KnowledgeChunk> withThreshold =
                searchService.searchWithThreshold(
                        organizationKey,
                        question,
                        10,
                        0.30
                );

        System.out.println("=== SEM LIMIAR ===");
        System.out.println("Resultados: " + withoutThreshold.size());

        withoutThreshold.forEach(result ->
                System.out.println(result.documentTitle())
        );

        System.out.println("=== COM LIMIAR 0.30 ===");
        System.out.println("Resultados: " + withThreshold.size());

        withThreshold.forEach(chunk ->
                System.out.println(
                        chunk.getKnowledgeDocument().getTitle()
                )
        );

        // O filtro não pode aumentar a quantidade de resultados
        assertTrue(
                withThreshold.size() <= withoutThreshold.size()
        );


        List<Object[]> distances =
                searchService.getSimilarityDistances(
                        organizationKey,
                        question,
                        10
                );

        System.out.println("=== DISTÂNCIAS REAIS ===");

        distances.forEach(row -> {
            String title = (String) row[0];
            double distance = ((Number) row[1]).doubleValue();

            System.out.printf(
                    "%s: %.4f%n",
                    title,
                    distance
            );
        });

    }

}

