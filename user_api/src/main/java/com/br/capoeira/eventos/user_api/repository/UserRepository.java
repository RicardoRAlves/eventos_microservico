package com.br.capoeira.eventos.user_api.repository;

import com.br.capoeira.eventos.user_api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    List<User> findAllByOrganizationIdOrderByIdAsc(Long organizationId);
    List<User> findAllByOrganizationUnitIdOrderByIdAsc(Long organizationUnitId);

    Boolean existsByEmail(String email);
}
