package com.culturaconecta.ai.repository;

import com.culturaconecta.ai.entity.KnowledgeDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface KnowledgeDocumentRepository
        extends JpaRepository<KnowledgeDocument, Long> {

    List<KnowledgeDocument> findByKnowledgeBaseId(Long knowledgeBaseId);

    Optional<KnowledgeDocument> findByIdAndKnowledgeBaseId(
            Long id,
            Long knowledgeBaseId
    );
}
