package com.br.capoeira.eventos.event_api.repository;

import com.br.capoeira.eventos.event_api.model.Category;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CategoryRepository extends MongoRepository<Category, Long> {
    Optional<Category> findByName(String name);
    Optional<Category> findTopByOrderByIdDesc();
}
