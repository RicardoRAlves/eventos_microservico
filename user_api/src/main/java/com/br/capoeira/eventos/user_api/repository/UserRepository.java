package com.br.capoeira.eventos.user_api.repository;

import com.br.capoeira.eventos.user_api.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    Optional<User> findByEmail(String email);
    Page<User> findAllByOrganizationIdOrderByIdAsc(Long organizationId, Pageable pageable);
    Page<User> findAllByOrganizationUnitIdOrderByIdAsc(Long organizationUnitId, Pageable pageable);

    Boolean existsByEmail(String email);
}
