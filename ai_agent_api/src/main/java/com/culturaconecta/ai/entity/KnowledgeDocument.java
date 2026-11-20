package com.culturaconecta.ai.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.Objects;

@Entity
@Table(name = "knowledge_document")
@Getter
@Setter
@NoArgsConstructor
public class KnowledgeDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "knowledge_base_id", nullable = false)
    private KnowledgeBase knowledgeBase;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", nullable = false, length = 20)
    private KnowledgeVisibility visibility = KnowledgeVisibility.PUBLIC;

    @Column(name = "content_hash", nullable = false, length = 64)
    private String contentHash;

    @Column(name = "created_at", nullable = false,
            insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false,
            insertable = false, updatable = false)
    private OffsetDateTime updatedAt;


    public KnowledgeVisibility getVisibility() {
        return visibility;
    }

    public void setVisibility(KnowledgeVisibility visibility) {
        this.visibility = Objects.requireNonNull(
                visibility,
                "A visibilidade não pode ser nula."
        );
    }

}

