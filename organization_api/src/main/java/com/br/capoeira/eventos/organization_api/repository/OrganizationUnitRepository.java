package com.br.capoeira.eventos.organization_api.repository;

import com.br.capoeira.eventos.organization_api.model.OrganizationUnit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrganizationUnitRepository extends JpaRepository<OrganizationUnit, Long> {

    List<OrganizationUnit> findAllByOrganization_Id(Long organizationId);

    boolean existsByOrganization_IdAndSlug(Long organizationId, String slug);

    Optional<OrganizationUnit> findByOrganization_IdAndSlug(Long organizationId, String slug);
}
