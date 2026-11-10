package com.br.capoeira.eventos.event_api.seed;

import com.br.capoeira.eventos.event_api.config.MongoConfig;
import com.br.capoeira.eventos.event_api.config.exception.CategorySeedException;
import com.br.capoeira.eventos.event_api.config.exception.ValidationException;
import com.br.capoeira.eventos.event_api.mapper.CategoryMapperImpl;
import com.br.capoeira.eventos.event_api.model.Category;
import com.br.capoeira.eventos.event_api.repository.CategoryRepository;
import com.br.capoeira.eventos.event_api.service.CategoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataMongoTest(properties = "app.category-seed.enabled=false")
@Import({MongoConfig.class, CategorySeedTransactionalImporter.class, CategorySeedLock.class,
        CategoryService.class, CategoryMapperImpl.class})
@Testcontainers
class CategorySeedIntegrationTest {

    @Container
    static final MongoDBContainer MONGO = new MongoDBContainer("mongo:7.0");

    @org.springframework.test.context.DynamicPropertySource
    static void mongoProperties(org.springframework.test.context.DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", MONGO::getReplicaSetUrl);
    }

    @Autowired
    private CategoryRepository repository;

    @Autowired
    private CategorySeedTransactionalImporter importer;

    @Autowired
    private CategorySeedLock lock;

    @Autowired
    private MongoTemplate mongoTemplate;

    @AfterEach
    void cleanDatabase() {
        mongoTemplate.dropCollection(Category.class);
        mongoTemplate.dropCollection(CategorySeedLock.COLLECTION);
    }

    @Test
    void importsExactBatchAndRepeatedExecutionDoesNotChangeIt() {
        List<CategorySeedItem> categories = categories();

        assertEquals(2, importer.importIfEmpty(categories));
        List<Category> before = repository.findAll().stream().sorted((a, b) -> a.getId().compareTo(b.getId())).toList();
        assertEquals(0, importer.importIfEmpty(List.of(new CategorySeedItem("OUTRA"))));
        List<Category> after = repository.findAll().stream().sorted((a, b) -> a.getId().compareTo(b.getId())).toList();

        assertEquals(before, after);
        assertEquals(List.of(1L, 2L), after.stream().map(Category::getId).toList());
        assertEquals(List.of("RODA", "CURSO"), after.stream().map(Category::getName).toList());
        assertEquals(List.of(true, true), after.stream().map(Category::getActive).toList());
    }

    @Test
    void inactiveExistingDocumentBlocksImportAndIsNotReactivated() {
        Category inactive = new Category(99L, "INATIVA", false);
        repository.insert(inactive);

        assertEquals(0, importer.importIfEmpty(categories()));

        assertEquals(List.of(inactive), repository.findAll());
    }

    @Test
    void partiallyPopulatedCollectionIsNotCompleted() {
        Category existing = new Category(10L, "RODA", true);
        repository.insert(existing);

        assertEquals(0, importer.importIfEmpty(categories()));

        assertEquals(List.of(existing), repository.findAll());
    }

    @Test
    void persistenceFailureRollsBackEntireBatch() {
        List<CategorySeedItem> invalidAtPersistence = List.of(
                new CategorySeedItem("DUPLICADA"),
                new CategorySeedItem("duplicada")
        );

        assertThrows(ValidationException.class, () -> importer.importIfEmpty(invalidAtPersistence));

        assertEquals(0, repository.count());
    }

    @Test
    void distributedLockIsExclusiveRenewableAndOwnerOnly() {
        assertTrue(lock.tryAcquire("owner-one", Duration.ofSeconds(5)));
        assertFalse(lock.tryAcquire("owner-two", Duration.ofSeconds(5)));
        assertFalse(lock.release("owner-two"));
        assertTrue(lock.renew("owner-one", Duration.ofSeconds(5)));
        assertTrue(lock.release("owner-one"));
        assertTrue(lock.tryAcquire("owner-two", Duration.ofSeconds(5)));
    }

    @Test
    void expiredLeaseCanBeRecoveredByAnotherOwner() throws Exception {
        assertTrue(lock.tryAcquire("crashed-owner", Duration.ofMillis(50)));

        Thread.sleep(150);

        assertTrue(lock.tryAcquire("recovery-owner", Duration.ofSeconds(5)));
        assertFalse(lock.release("crashed-owner"));
        assertTrue(lock.release("recovery-owner"));
    }

    @Test
    void twoConcurrentInitializationsLoadAndImportOnlyOneBatch() throws Exception {
        String json = """
                {"schemaVersion":1,"categories":[
                  {"name":"roda"},
                  {"name":"curso"}
                ]}
                """;
        AtomicInteger loadCount = new AtomicInteger();
        CategorySeedProperties properties = new CategorySeedProperties();
        properties.setLockDuration(Duration.ofSeconds(5));
        properties.setLockWaitTimeout(Duration.ofSeconds(5));
        CategorySeedService service = new CategorySeedService(
                repository,
                lock,
                importer,
                properties,
                new DefaultResourceLoader() {
                    @Override
                    public Resource getResource(String location) {
                        loadCount.incrementAndGet();
                        return new ByteArrayResource(json.getBytes(StandardCharsets.UTF_8));
                    }
                },
                new ObjectMapper()
        );

        CountDownLatch start = new CountDownLatch(1);
        var executor = Executors.newFixedThreadPool(2);
        try {
            var first = executor.submit(() -> runAfter(start, service));
            var second = executor.submit(() -> runAfter(start, service));
            start.countDown();
            first.get(10, TimeUnit.SECONDS);
            second.get(10, TimeUnit.SECONDS);
        } finally {
            executor.shutdownNow();
        }

        assertEquals(2, repository.count());
        assertEquals(1, loadCount.get());
    }

    @Test
    void invalidJsonDoesNotPersistAnything() {
        String invalidJson = """
                {"schemaVersion":1,"categories":[
                  {"name":"   "}
                ]}
                """;
        CategorySeedProperties properties = new CategorySeedProperties();
        CategorySeedService service = new CategorySeedService(
                repository,
                lock,
                importer,
                properties,
                new DefaultResourceLoader() {
                    @Override
                    public Resource getResource(String location) {
                        return new ByteArrayResource(invalidJson.getBytes(StandardCharsets.UTF_8));
                    }
                },
                new ObjectMapper()
        );

        assertThrows(CategorySeedException.class, service::initialize);

        assertEquals(0, repository.count());
    }

    private void runAfter(CountDownLatch start, CategorySeedService service) {
        try {
            start.await(5, TimeUnit.SECONDS);
            service.initialize();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(exception);
        }
    }

    private List<CategorySeedItem> categories() {
        return List.of(
                new CategorySeedItem("roda"),
                new CategorySeedItem("CURSO")
        );
    }
}
