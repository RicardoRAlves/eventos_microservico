package com.br.capoeira.eventos.organization_api.repository;

import com.br.capoeira.eventos.organization_api.model.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {

    boolean existsBySlug(String slug);

    Optional<Organization> findBySlug(String slug);
}
