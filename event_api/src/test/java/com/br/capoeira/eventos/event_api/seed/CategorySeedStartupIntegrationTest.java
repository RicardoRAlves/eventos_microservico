package com.br.capoeira.eventos.event_api.seed;

import com.br.capoeira.eventos.event_api.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(properties = {
        "app.category-seed.enabled=true",
        "app.category-seed.resource=classpath:seed/categories-integration.json",
        "spring.rabbitmq.listener.simple.auto-startup=false"
})
@Testcontainers
class CategorySeedStartupIntegrationTest {

    @Container
    static final MongoDBContainer MONGO = new MongoDBContainer("mongo:7.0");

    @org.springframework.test.context.DynamicPropertySource
    static void mongoProperties(org.springframework.test.context.DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", MONGO::getReplicaSetUrl);
    }

    @Autowired
    private CategoryRepository repository;

    @MockitoBean
    private RabbitAdmin rabbitAdmin;

    @Test
    void applicationRunnerCompletesSeedBeforeContextIsReady() {
        var categories = repository.findAll().stream()
                .sorted((left, right) -> left.getId().compareTo(right.getId()))
                .toList();

        assertEquals(2, categories.size());
        assertEquals(1L, categories.get(0).getId());
        assertEquals("TESTE_ATIVA", categories.get(0).getName());
        assertEquals(true, categories.get(0).getActive());
        assertEquals(2L, categories.get(1).getId());
        assertEquals("TESTE_ATIVA_2", categories.get(1).getName());
        assertEquals(true, categories.get(1).getActive());
    }
}
