package com.culturaconecta.ai.repository;

import com.culturaconecta.ai.entity.KnowledgeBase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface KnowledgeBaseRepository
        extends JpaRepository<KnowledgeBase, Long> {

    Optional<KnowledgeBase> findByOrganizationKey(String organizationKey);

    boolean existsByOrganizationKey(String organizationKey);
}

