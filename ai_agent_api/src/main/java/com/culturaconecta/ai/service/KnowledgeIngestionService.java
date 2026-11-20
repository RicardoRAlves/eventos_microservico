
package com.culturaconecta.ai.service;

import com.culturaconecta.ai.entity.KnowledgeBase;
import com.culturaconecta.ai.entity.KnowledgeChunk;
import com.culturaconecta.ai.entity.KnowledgeDocument;
import com.culturaconecta.ai.entity.KnowledgeVisibility;
import com.culturaconecta.ai.repository.KnowledgeBaseRepository;
import com.culturaconecta.ai.repository.KnowledgeChunkRepository;
import com.culturaconecta.ai.repository.KnowledgeDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;

@RequiredArgsConstructor
@Service
public class KnowledgeIngestionService {

    private final KnowledgeBaseRepository baseRepository;
    private final KnowledgeDocumentRepository documentRepository;
    private final KnowledgeChunkRepository chunkRepository;
    private final KnowledgeProcessingService processingService;

    @Transactional
    public Long ingest(
            String organizationKey,
            String title,
            String content) {

        return ingest(
                organizationKey,
                title,
                content,
                KnowledgeVisibility.PUBLIC
        );
    }

    @Transactional
    public Long ingest(
            String organizationKey,
            String title,
            String content,
            KnowledgeVisibility visibility) {

        if (organizationKey == null || organizationKey.isBlank()) {
            throw new IllegalArgumentException(
                    "Organização obrigatória.");
        }

        if (organizationKey.length() > 120) {
            throw new IllegalArgumentException(
                    "Chave da organização muito longa.");
        }

        if (title == null || title.isBlank() || title.length() > 255) {
            throw new IllegalArgumentException(
                    "Título inválido.");
        }

        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException(
                    "Conteúdo obrigatório.");
        }

        if (visibility == null) {
            throw new IllegalArgumentException(
                    "A visibilidade é obrigatória.");
        }

        // Gera os chunks e embeddings.
        List<KnowledgeProcessingService.ProcessedChunk> processed =
                processingService.process(content);

        // Localiza ou cria a base da organização.
        KnowledgeBase base = baseRepository
                .findByOrganizationKey(organizationKey)
                .orElseGet(() -> {
                    KnowledgeBase newBase = new KnowledgeBase();
                    newBase.setOrganizationKey(organizationKey);
                    return baseRepository.saveAndFlush(newBase);
                });

        // Cria o documento.
        KnowledgeDocument document = new KnowledgeDocument();

        document.setKnowledgeBase(base);
        document.setTitle(title);
        document.setContent(content);
        document.setContentHash(sha256(content));
        document.setVisibility(visibility);

        document = documentRepository.saveAndFlush(document);

        // Persiste os chunks e seus embeddings.
        for (var processedChunk : processed) {

            KnowledgeChunk chunk = new KnowledgeChunk();

            chunk.setKnowledgeDocument(document);
            chunk.setChunkIndex(processedChunk.index());
            chunk.setContent(processedChunk.content());
            chunk.setEmbedding(processedChunk.embedding());

            chunkRepository.save(chunk);
        }

        chunkRepository.flush();

        return document.getId();
    }

    private String sha256(String content) {

        try {
            byte[] hash = MessageDigest
                    .getInstance("SHA-256")
                    .digest(content.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(
                    "SHA-256 indisponível.", e);
        }
    }
}
