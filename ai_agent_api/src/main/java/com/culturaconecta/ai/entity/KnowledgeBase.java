package com.culturaconecta.ai.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "knowledge_base",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_knowledge_base_organization_key",
                columnNames = "organization_key"
        )
)
@Getter
@Setter
@NoArgsConstructor
public class KnowledgeBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "organization_key", nullable = false, length = 120)
    private String organizationKey;

    @Column(name = "created_at", nullable = false,
            insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false,
            insertable = false, updatable = false)
    private OffsetDateTime updatedAt;
}

