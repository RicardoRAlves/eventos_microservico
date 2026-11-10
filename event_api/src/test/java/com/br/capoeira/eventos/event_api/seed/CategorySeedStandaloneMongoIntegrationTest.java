package com.br.capoeira.eventos.event_api.seed;

import com.br.capoeira.eventos.event_api.config.MongoConfig;
import com.br.capoeira.eventos.event_api.config.exception.CategorySeedException;
import com.br.capoeira.eventos.event_api.mapper.CategoryMapperImpl;
import com.br.capoeira.eventos.event_api.repository.CategoryRepository;
import com.br.capoeira.eventos.event_api.service.CategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataMongoTest(properties = "app.category-seed.enabled=false")
@Import({MongoConfig.class, CategorySeedTransactionalImporter.class, CategoryService.class, CategoryMapperImpl.class})
@Testcontainers
class CategorySeedStandaloneMongoIntegrationTest {

    @Container
    static final GenericContainer<?> MONGO = new GenericContainer<>("mongo:7.0")
            .withExposedPorts(27017);

    @org.springframework.test.context.DynamicPropertySource
    static void mongoProperties(org.springframework.test.context.DynamicPropertyRegistry registry) {
        registry.add(
                "spring.data.mongodb.uri",
                () -> "mongodb://%s:%d/category_seed_standalone".formatted(MONGO.getHost(), MONGO.getMappedPort(27017))
        );
    }

    @Autowired
    private CategorySeedTransactionalImporter importer;

    @Autowired
    private CategoryRepository repository;

    @Test
    void failsClearlyWithoutTransactionsAndDoesNotFallback() {
        CategorySeedException exception = assertThrows(
                CategorySeedException.class,
                () -> importer.importIfEmpty(List.of(
                        new CategorySeedItem("RODA"),
                        new CategorySeedItem("CURSO")
                ))
        );

        assertTrue(exception.getMessage().contains("replica set or sharded cluster"));
        assertEquals(0, repository.count());
    }
}
