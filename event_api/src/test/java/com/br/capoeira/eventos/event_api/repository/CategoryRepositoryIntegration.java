package com.br.capoeira.eventos.event_api.repository;

import com.br.capoeira.eventos.event_api.model.Category;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
@Testcontainers
class CategoryRepositoryIntegrationTest {

    @Container
    static final MongoDBContainer mongoDBContainer =
            new MongoDBContainer("mongo:7.0");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private CategoryRepository repository;

    @Test
    @DisplayName("Should save category successfully")
    void shouldSaveCategorySuccessfully() {
        var category = new Category();
        category.setId(1L);
        category.setName("Capoeira");
        category.setActive(true);

        var saved = repository.save(category);

        assertNotNull(saved);
        assertNotNull(saved.getId());
        assertEquals("Capoeira", saved.getName());
        assertTrue(saved.getActive());
    }

    @Test
    @DisplayName("Should find category by id")
    void shouldFindCategoryById() {
        var category = new Category();
        category.setId(2L);
        category.setName("Workshop");
        category.setActive(true);

        var saved = repository.save(category);

        Optional<Category> result = repository.findById(saved.getId());

        assertTrue(result.isPresent());
        assertEquals(saved.getId(), result.get().getId());
        assertEquals("Workshop", result.get().getName());
    }

    @Test
    @DisplayName("Should return empty when category does not exist")
    void shouldReturnEmptyWhenCategoryDoesNotExist() {
        Optional<Category> result = repository.findByName("6803f1b9f9c123456789abcd");

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should delete category successfully")
    void shouldDeleteCategorySuccessfully() {
        var category = new Category();
        category.setId(1L);
        category.setName("Batizado");
        category.setActive(true);

        var saved = repository.save(category);

        repository.deleteById(saved.getId());

        Optional<Category> result = repository.findById(saved.getId());
        assertTrue(result.isEmpty());
    }
}