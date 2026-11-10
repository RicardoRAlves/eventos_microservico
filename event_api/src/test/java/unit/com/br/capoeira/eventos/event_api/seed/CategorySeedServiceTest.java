package unit.com.br.capoeira.eventos.event_api.seed;

import com.br.capoeira.eventos.event_api.config.exception.CategorySeedException;
import com.br.capoeira.eventos.event_api.repository.CategoryRepository;
import com.br.capoeira.eventos.event_api.seed.CategorySeedLock;
import com.br.capoeira.eventos.event_api.seed.CategorySeedItem;
import com.br.capoeira.eventos.event_api.seed.CategorySeedProperties;
import com.br.capoeira.eventos.event_api.seed.CategorySeedService;
import com.br.capoeira.eventos.event_api.seed.CategorySeedTransactionalImporter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ResourceLoader;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CategorySeedServiceTest {

    private CategoryRepository repository;
    private CategorySeedLock lock;
    private CategorySeedTransactionalImporter importer;
    private ResourceLoader resourceLoader;
    private CategorySeedProperties properties;
    private CategorySeedService service;

    @BeforeEach
    void setUp() {
        repository = mock(CategoryRepository.class);
        lock = mock(CategorySeedLock.class);
        importer = mock(CategorySeedTransactionalImporter.class);
        resourceLoader = mock(ResourceLoader.class);
        properties = new CategorySeedProperties();
        properties.setLockDuration(Duration.ofSeconds(30));
        properties.setLockWaitTimeout(Duration.ofSeconds(1));
        service = new CategorySeedService(
                repository,
                lock,
                importer,
                properties,
                resourceLoader,
                new ObjectMapper()
        );
    }

    @Test
    void doesNotLoadJsonWhenCollectionContainsAnyDocument() {
        when(repository.count()).thenReturn(1L);

        service.initialize();

        verify(resourceLoader, never()).getResource(anyString());
        verify(lock, never()).tryAcquire(anyString(), any());
        verify(importer, never()).importIfEmpty(any());
    }

    @Test
    void doesNotLoadJsonWhenCollectionContainsOnlyInactiveDocuments() {
        when(repository.count()).thenReturn(1L);

        service.initialize();

        verify(resourceLoader, never()).getResource(anyString());
        verify(importer, never()).importIfEmpty(any());
    }

    @Test
    void doesNotCompletePartiallyPopulatedCollection() {
        when(repository.count()).thenReturn(1L);

        service.initialize();

        verify(resourceLoader, never()).getResource(anyString());
        verify(importer, never()).importIfEmpty(any());
    }

    @Test
    void importsValidatedJsonWhenCollectionRemainsEmpty() {
        String json = """
                {
                  "schemaVersion": 1,
                  "categories": [
                    {"name": "roda"},
                    {"name": "CURSO"}
                  ]
                }
                """;
        when(repository.count()).thenReturn(0L);
        when(lock.tryAcquire(anyString(), any())).thenReturn(true);
        when(resourceLoader.getResource(properties.getResource()))
                .thenReturn(new ByteArrayResource(json.getBytes(StandardCharsets.UTF_8)));
        when(importer.importIfEmpty(any())).thenReturn(2);

        service.initialize();

        @SuppressWarnings("unchecked")
        var captor = org.mockito.ArgumentCaptor.forClass(List.class);
        verify(importer).importIfEmpty(captor.capture());
        List<CategorySeedItem> categories = captor.getValue();
        assertEquals(List.of("roda", "CURSO"), categories.stream().map(CategorySeedItem::getName).toList());
    }

    @Test
    void doesNotLoadJsonWhenCollectionIsPopulatedAfterLockAcquisition() {
        when(repository.count()).thenReturn(0L, 0L, 1L);
        when(lock.tryAcquire(anyString(), any())).thenReturn(true);

        service.initialize();

        verify(resourceLoader, never()).getResource(anyString());
        verify(importer, never()).importIfEmpty(any());
    }

    @Test
    void rejectsUnknownPropertiesBeforeImport() {
        assertInvalid("""
                {"schemaVersion":1,"categories":[{"name":"RODA","typo":1}]}
                """);
    }

    @Test
    void rejectsPersistenceFieldsOwnedByTheApplication() {
        assertInvalid("""
                {"schemaVersion":1,"categories":[{"id":1,"name":"RODA","active":true}]}
                """);
    }

    @Test
    void rejectsDuplicateNamesIgnoringCaseBeforeImport() {
        assertInvalid("""
                {"schemaVersion":1,"categories":[
                  {"name":"RODA"},
                  {"name":"roda"}
                ]}
                """);
    }

    @Test
    void rejectsNullRequiredValuesBeforeImport() {
        assertInvalid("""
                {"schemaVersion":1,"categories":[{"name":null}]}
                """);
    }

    @Test
    void rejectsNonStringNameBeforeImport() {
        assertInvalid("""
                {"schemaVersion":1,"categories":[{"name":123}]}
                """);
    }

    @Test
    void rejectsUnsupportedSchemaBeforeImport() {
        assertInvalid("""
                {"schemaVersion":2,"categories":[{"name":"RODA"}]}
                """);
    }

    @Test
    void disabledSeedDoesNotReadOrWrite() {
        properties.setEnabled(false);

        service.initialize();

        verify(repository, never()).count();
        verify(resourceLoader, never()).getResource(anyString());
        verify(importer, never()).importIfEmpty(any());
    }

    private void assertInvalid(String json) {
        when(repository.count()).thenReturn(0L);
        when(lock.tryAcquire(anyString(), any())).thenReturn(true);
        when(resourceLoader.getResource(properties.getResource()))
                .thenReturn(new ByteArrayResource(json.getBytes(StandardCharsets.UTF_8)));

        assertThrows(CategorySeedException.class, service::initialize);
        verify(importer, never()).importIfEmpty(any());
    }
}
