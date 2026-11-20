package com.culturaconecta.ai.repository;

import com.culturaconecta.ai.entity.KnowledgeChunk;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface KnowledgeChunkRepository
        extends JpaRepository<KnowledgeChunk, Long> {

    List<KnowledgeChunk> findByKnowledgeDocumentIdOrderByChunkIndexAsc(
            Long knowledgeDocumentId
    );

    Optional<KnowledgeChunk> findByIdAndKnowledgeDocumentKnowledgeBaseId(
            Long id,
            Long knowledgeBaseId
    );

    @Query(value = """
    SELECT kc.*
    FROM knowledge_chunk kc
    JOIN knowledge_document kd
      ON kd.id = kc.knowledge_document_id
    JOIN knowledge_base kb
      ON kb.id = kd.knowledge_base_id
    WHERE kc.embedding IS NOT NULL
      AND (
          kd.visibility = 'PUBLIC'
          OR kb.organization_key = :organizationKey
      )
    ORDER BY kc.embedding <=> CAST(:embedding AS vector)
    LIMIT :limit
    """, nativeQuery = true)
    List<KnowledgeChunk> searchSimilar(
            @Param("organizationKey") String organizationKey,
            @Param("embedding") String embedding,
            @Param("limit") int limit
    );

    @Query(value = """
    SELECT kc.*
    FROM knowledge_chunk kc
    JOIN knowledge_document kd
      ON kd.id = kc.knowledge_document_id
    JOIN knowledge_base kb
      ON kb.id = kd.knowledge_base_id
    WHERE kc.embedding IS NOT NULL
      AND (
          kd.visibility = 'PUBLIC'
          OR kb.organization_key = :organizationKey
      )
      AND (
          kc.embedding <=> CAST(:embedding AS vector)
      ) <= :maxDistance
    ORDER BY kc.embedding <=> CAST(:embedding AS vector)
    LIMIT :limit
    """, nativeQuery = true)
    List<KnowledgeChunk> searchSimilarWithThreshold(
            @Param("organizationKey") String organizationKey,
            @Param("embedding") String embedding,
            @Param("maxDistance") double maxDistance,
            @Param("limit") int limit
    );


    @Query(value = """
    SELECT
        kd.title AS title,
        kc.embedding <=> CAST(:embedding AS vector) AS distance
    FROM knowledge_chunk kc
    JOIN knowledge_document kd
      ON kd.id = kc.knowledge_document_id
    JOIN knowledge_base kb
      ON kb.id = kd.knowledge_base_id
    WHERE kc.embedding IS NOT NULL
      AND (
          kd.visibility = 'PUBLIC'
          OR kb.organization_key = :organizationKey
      )
    ORDER BY distance ASC
    LIMIT :limit
    """, nativeQuery = true)
    List<Object[]> findSimilarityDistances(
            @Param("organizationKey") String organizationKey,
            @Param("embedding") String embedding,
            @Param("limit") int limit
    );

}
