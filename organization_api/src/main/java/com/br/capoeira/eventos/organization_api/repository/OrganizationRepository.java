package com.br.capoeira.eventos.organization_api.repository;

import com.br.capoeira.eventos.organization_api.model.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {

}
