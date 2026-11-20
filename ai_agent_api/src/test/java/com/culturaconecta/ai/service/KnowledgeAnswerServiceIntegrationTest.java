
package com.culturaconecta.ai.service;

import com.culturaconecta.ai.dto.KnowledgeAnswerResponse;
import com.culturaconecta.ai.dto.KnowledgeSourceType;
import com.culturaconecta.ai.entity.KnowledgeVisibility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class KnowledgeAnswerServiceIntegrationTest {

    private static final String FALLBACK_MESSAGE =
            "Não encontrei informações na base de conhecimento.";

    @Autowired
    private KnowledgeIngestionService ingestionService;

    @Autowired
    private KnowledgeAnswerService answerService;

    @Test
    void shouldAnswerUsingKnowledgeBase() {

        String organizationKey =
                "org-rag-answer-" + UUID.randomUUID();

        Long documentId = ingestionService.ingest(
                organizationKey,
                "Instrumentos da capoeira",
                """
                O berimbau é um instrumento musical utilizado na capoeira.
                Ele é formado por uma verga de madeira, um arame e uma cabaça.
                O berimbau ajuda a conduzir o ritmo da roda de capoeira.
                """,
                KnowledgeVisibility.PRIVATE
        );

        String question =
                "Qual instrumento conduz o ritmo da roda de capoeira?";

        KnowledgeAnswerResponse response =
                answerService.answerWithSources(
                        organizationKey,
                        question
                );

        assertNotNull(response);
        assertNotNull(response.answer());
        assertFalse(response.answer().isBlank());

        assertEquals(
                KnowledgeSourceType.ORGANIZATION,
                response.sourceType()
        );

        assertTrue(
                response.answer().toLowerCase().contains("berimbau"),
                "A resposta deveria mencionar o berimbau."
        );

        assertTrue(
                response.sources().stream()
                        .anyMatch(source ->
                                source.documentId().equals(documentId)
                        ),
                "A resposta deveria incluir o documento recuperado."
        );
    }

    @Test
    void shouldNotInventAnswerWhenContextIsInsufficient() {

        String organizationKey =
                "org-rag-unknown-" + UUID.randomUUID();

        ingestionService.ingest(
                organizationKey,
                "Instrumentos da capoeira",
                """
                O berimbau é um instrumento musical utilizado na capoeira.
                Ele possui uma verga de madeira, arame e cabaça.
                """,
                KnowledgeVisibility.PRIVATE
        );

        String question =
                "Em que ano foi fundada a organização Nosso Senhor do Bonfim?";

        KnowledgeAnswerResponse response =
                answerService.answerWithSources(
                        organizationKey,
                        question
                );

        assertNotNull(response);
        assertNotNull(response.answer());
        assertFalse(response.answer().isBlank());

        System.out.println("PERGUNTA: " + question);
        System.out.println("RESPOSTA: " + response.answer());

        // A resposta não pode inventar uma data específica.
        assertFalse(
                response.answer().matches("(?s).*\\b(?:18|19|20)\\d{2}\\b.*"),
                "A resposta não deveria inventar um ano de fundação."
        );
    }

    @Test
    void shouldReturnFallbackWhenNoRelevantDocumentsExist() {

        String organizationKey =
                "org-empty-" + UUID.randomUUID();

        KnowledgeAnswerResponse response =
                answerService.answerWithSources(
                        organizationKey,
                        "Qual é a temperatura média da superfície de Marte?"
                );

        assertNotNull(response);

        assertEquals(
                KnowledgeSourceType.NONE,
                response.sourceType()
        );

        assertEquals(
                FALLBACK_MESSAGE,
                response.answer()
        );

        assertTrue(
                response.sources().isEmpty(),
                "Não deveria retornar fontes irrelevantes."
        );
    }

    @Test
    void shouldExplainDifferencesBetweenOrganizations() {

        String suffix = UUID.randomUUID().toString();

        String organizationA = "grupo-a-" + suffix;
        String organizationB = "grupo-b-" + suffix;

        Long documentA = ingestionService.ingest(
                organizationA,
                "Formação da bateria A",
                "Na organização " + organizationA
                        + ", a bateria de capoeira utiliza três berimbaus.",
                KnowledgeVisibility.PUBLIC
        );

        Long documentB = ingestionService.ingest(
                organizationB,
                "Formação da bateria B",
                "Na organização " + organizationB
                        + ", a bateria de capoeira utiliza dois berimbaus.",
                KnowledgeVisibility.PUBLIC
        );

        KnowledgeAnswerResponse response =
                answerService.answerWithSources(
                        organizationA,
                        "Compare a quantidade de berimbaus utilizada pelas "
                                + "organizações " + organizationA
                                + " e " + organizationB + "."
                );

        assertNotNull(response);
        assertNotNull(response.answer());
        assertFalse(response.answer().isBlank());

        System.out.println("RESPOSTA DO OLLAMA:");
        System.out.println(response.answer());

        assertEquals(
                KnowledgeSourceType.ORGANIZATION,
                response.sourceType()
        );

        assertTrue(
                response.sources().stream()
                        .anyMatch(source ->
                                source.documentId().equals(documentA)
                        ),
                "O documento da organização A deveria ser recuperado."
        );

        assertTrue(
                response.sources().stream()
                        .anyMatch(source ->
                                source.documentId().equals(documentB)
                        ),
                "O documento público da organização B deveria ser recuperado."
        );
    }

    @Test
    void shouldReturnNoneWhenQuestionIsUnrelatedToKnowledgeBase() {

        String organizationKey =
                "org-unrelated-" + UUID.randomUUID();

        ingestionService.ingest(
                organizationKey,
                "O papel do berimbau",
                """
                O berimbau é um instrumento musical utilizado
                na capoeira. Ele conduz o ritmo da roda e
                orienta o andamento do jogo.
                """,
                KnowledgeVisibility.PRIVATE
        );

        KnowledgeAnswerResponse response =
                answerService.answerWithSources(
                        organizationKey,
                        "Qual é a temperatura média da superfície de Marte?"
                );

        assertNotNull(response);

        assertEquals(
                KnowledgeSourceType.NONE,
                response.sourceType()
        );

        assertEquals(
                FALLBACK_MESSAGE,
                response.answer()
        );

        assertTrue(
                response.sources().isEmpty(),
                "Uma pergunta irrelevante não deve retornar fontes."
        );
    }
}
