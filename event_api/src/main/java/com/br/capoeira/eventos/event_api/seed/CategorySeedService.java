package com.br.capoeira.eventos.event_api.seed;

import com.br.capoeira.eventos.event_api.config.exception.CategorySeedException;
import com.br.capoeira.eventos.event_api.repository.CategoryRepository;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.CoercionAction;
import com.fasterxml.jackson.databind.cfg.CoercionInputShape;
import com.fasterxml.jackson.databind.type.LogicalType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
public class CategorySeedService {

    private static final int SUPPORTED_SCHEMA_VERSION = 1;
    private static final long LOCK_POLL_MILLIS = 200;

    private final CategoryRepository categoryRepository;
    private final CategorySeedLock categorySeedLock;
    private final CategorySeedTransactionalImporter importer;
    private final CategorySeedProperties properties;
    private final ResourceLoader resourceLoader;
    private final ObjectMapper strictObjectMapper;

    public CategorySeedService(
            CategoryRepository categoryRepository,
            CategorySeedLock categorySeedLock,
            CategorySeedTransactionalImporter importer,
            CategorySeedProperties properties,
            ResourceLoader resourceLoader,
            ObjectMapper objectMapper
    ) {
        this.categoryRepository = categoryRepository;
        this.categorySeedLock = categorySeedLock;
        this.importer = importer;
        this.properties = properties;
        this.resourceLoader = resourceLoader;
        ObjectMapper strictMapper = objectMapper.copy()
                .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
                .disable(DeserializationFeature.ACCEPT_FLOAT_AS_INT)
                .disable(MapperFeature.ALLOW_COERCION_OF_SCALARS);
        strictMapper.coercionConfigFor(LogicalType.Textual)
                .setCoercion(CoercionInputShape.Integer, CoercionAction.Fail)
                .setCoercion(CoercionInputShape.Float, CoercionAction.Fail)
                .setCoercion(CoercionInputShape.Boolean, CoercionAction.Fail);
        this.strictObjectMapper = strictMapper;
    }

    public void initialize() {
        validateConfiguration();
        if (!properties.isEnabled()) {
            log.info("category_seed status=disabled");
            return;
        }

        if (categoryRepository.count() > 0) {
            log.info("category_seed status=skipped_non_empty");
            return;
        }

        waitForLockOrPopulation();
    }

    private void waitForLockOrPopulation() {
        Instant deadline = Instant.now().plus(properties.getLockWaitTimeout());
        String ownerId = UUID.randomUUID().toString();
        boolean waitLogged = false;

        while (Instant.now().isBefore(deadline)) {
            if (categoryRepository.count() > 0) {
                log.info("category_seed status=skipped_concurrent_population");
                return;
            }

            if (categorySeedLock.tryAcquire(ownerId, properties.getLockDuration())) {
                runAsLockOwner(ownerId);
                return;
            }

            if (!waitLogged) {
                log.info("category_seed status=lock_wait");
                waitLogged = true;
            }
            pauseBeforeRetry();
        }

        if (categoryRepository.count() > 0) {
            log.info("category_seed status=skipped_concurrent_population");
            return;
        }
        throw new CategorySeedException("Timed out waiting for distributed lock 'category-seed'");
    }

    private void runAsLockOwner(String ownerId) {
        AtomicBoolean lockLost = new AtomicBoolean(false);
        ScheduledExecutorService renewer = startLockRenewer(ownerId, lockLost);
        try {
            if (categoryRepository.count() > 0) {
                log.info("category_seed status=skipped_concurrent_population");
                return;
            }

            log.info("category_seed status=started");
            List<CategorySeedItem> categories = loadAndValidate();
            assertLockOwned(lockLost);
            int imported = importer.importIfEmpty(categories);
            assertLockOwned(lockLost);

            if (imported == 0) {
                log.info("category_seed status=skipped_concurrent_population");
            } else {
                log.info("category_seed status=completed imported={}", imported);
            }
        } catch (CategorySeedException exception) {
            log.error("category_seed status=failed reason={}", exception.getMessage());
            throw exception;
        } catch (RuntimeException exception) {
            log.error("category_seed status=failed reason={}", exception.getMessage());
            throw new CategorySeedException("Category seed failed", exception);
        } finally {
            renewer.shutdownNow();
            categorySeedLock.release(ownerId);
        }
    }

    private ScheduledExecutorService startLockRenewer(String ownerId, AtomicBoolean lockLost) {
        long renewalMillis = Math.max(100, properties.getLockDuration().toMillis() / 3);
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(task -> {
            Thread thread = new Thread(task, "category-seed-lock-renewer");
            thread.setDaemon(true);
            return thread;
        });
        executor.scheduleAtFixedRate(() -> {
            try {
                if (!categorySeedLock.renew(ownerId, properties.getLockDuration())) {
                    lockLost.set(true);
                }
            } catch (RuntimeException exception) {
                lockLost.set(true);
                log.error("category_seed status=lock_renew_failed", exception);
            }
        }, renewalMillis, renewalMillis, TimeUnit.MILLISECONDS);
        return executor;
    }

    List<CategorySeedItem> loadAndValidate() {
        Resource resource = resourceLoader.getResource(properties.getResource());
        try (var input = resource.getInputStream()) {
            CategorySeedFile seedFile = strictObjectMapper.readValue(input, CategorySeedFile.class);
            validate(seedFile);
            return List.copyOf(seedFile.getCategories());
        } catch (IOException exception) {
            throw new CategorySeedException("Unable to read or deserialize category seed resource", exception);
        }
    }

    private void validate(CategorySeedFile seedFile) {
        if (seedFile == null || !Integer.valueOf(SUPPORTED_SCHEMA_VERSION).equals(seedFile.getSchemaVersion())) {
            throw new CategorySeedException("Unsupported or missing category seed schemaVersion");
        }
        if (seedFile.getCategories() == null || seedFile.getCategories().isEmpty()) {
            throw new CategorySeedException("Category seed categories must not be null or empty");
        }

        Set<String> names = new HashSet<>();
        for (int index = 0; index < seedFile.getCategories().size(); index++) {
            CategorySeedItem item = seedFile.getCategories().get(index);
            if (item == null) {
                throw invalidItem(index, "item must not be null");
            }
            String name = item.getName();
            if (name == null || name.isBlank()) {
                throw invalidItem(index, "name must not be null or blank");
            }
            if (!name.equals(name.trim())) {
                throw invalidItem(index, "name must not have surrounding whitespace");
            }
            if (!names.add(name.toUpperCase(java.util.Locale.ROOT))) {
                throw invalidItem(index, "name must be unique ignoring case");
            }
        }
    }

    private CategorySeedException invalidItem(int index, String reason) {
        return new CategorySeedException("Invalid category seed item at index %d: %s".formatted(index, reason));
    }

    private void validateConfiguration() {
        if (properties.getResource() == null || properties.getResource().isBlank()) {
            throw new CategorySeedException("Category seed resource must be configured");
        }
        requirePositive(properties.getLockDuration(), "lock-duration");
        requirePositive(properties.getLockWaitTimeout(), "lock-wait-timeout");
    }

    private void requirePositive(Duration duration, String property) {
        if (duration == null || duration.isZero() || duration.isNegative()) {
            throw new CategorySeedException("app.category-seed.%s must be positive".formatted(property));
        }
    }

    private void assertLockOwned(AtomicBoolean lockLost) {
        if (lockLost.get()) {
            throw new CategorySeedException("Distributed lock 'category-seed' was lost during initialization");
        }
    }

    private void pauseBeforeRetry() {
        try {
            Thread.sleep(LOCK_POLL_MILLIS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new CategorySeedException("Interrupted while waiting for distributed lock 'category-seed'", exception);
        }
    }
}
