package com.br.capoeira.eventos.processor_api.repository;

import com.br.capoeira.eventos.processor_api.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByNameIgnoreCase(String name);
}
