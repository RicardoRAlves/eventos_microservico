package com.br.capoeira.eventos.organization_api.repository;

import com.br.capoeira.eventos.organization_api.model.OrganizationUnit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrganizationUnitRepository extends JpaRepository<OrganizationUnit, Long> {

    Page<OrganizationUnit> findAllByOrganization_IdOrderByIdAsc(Long organizationId, Pageable pageable);

    boolean existsByOrganization_IdAndSlug(Long organizationId, String slug);

    Optional<OrganizationUnit> findByOrganization_IdAndSlug(Long organizationId, String slug);

    boolean existsByJoinCode(String joinCode);

    Optional<OrganizationUnit> findByJoinCode(String joinCode);
}
