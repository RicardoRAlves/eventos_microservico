package com.br.capoeira.eventos.notification.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FirebaseCredentialResolverTest {

    private FirebaseCredentialResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new FirebaseCredentialResolver(new ObjectMapper());
    }

    @Test
    void shouldPrioritizeEnvironmentCredentialsWhenClasspathCredentialsAreAvailable() {
        String environmentJson = credentialsJson("events-capoeira-homologacao");
        var classpathResource = resource(credentialsJson("events-capoeira"));

        var resolved = resolver.resolve(
                environmentJson,
                true,
                classpathResource,
                true,
                "events-capoeira-homologacao"
        );

        assertEquals("events-capoeira-homologacao", resolved.projectId());
        assertEquals(FirebaseCredentialResolver.CredentialSource.ENVIRONMENT, resolved.source());
    }

    @Test
    void shouldUseClasspathCredentialsOnlyWhenFallbackIsExplicitlyAllowed() {
        var resolved = resolver.resolve(
                null,
                false,
                resource(credentialsJson("events-capoeira-dev")),
                true,
                "events-capoeira-dev"
        );

        assertEquals("events-capoeira-dev", resolved.projectId());
        assertEquals(FirebaseCredentialResolver.CredentialSource.CLASSPATH, resolved.source());
    }

    @Test
    void shouldRejectInvalidEnvironmentJsonWithoutFallingBack() {
        var exception = assertThrows(
                IllegalStateException.class,
                () -> resolver.resolve(
                        "not-json",
                        true,
                        resource(credentialsJson("events-capoeira")),
                        true,
                        "events-capoeira"
                )
        );

        assertTrue(exception.getMessage().contains("Invalid Firebase credentials JSON"));
    }

    @Test
    void shouldRejectCredentialsForUnexpectedProject() {
        var exception = assertThrows(
                IllegalStateException.class,
                () -> resolver.resolve(
                        credentialsJson("different-project"),
                        true,
                        resource(credentialsJson("different-project")),
                        false,
                        "expected-project"
                )
        );

        assertTrue(exception.getMessage().contains("expected=expected-project"));
        assertTrue(exception.getMessage().contains("configured=different-project"));
    }

    @Test
    void shouldRejectProductionCredentialsInHomologation() {
        assertThrows(
                IllegalStateException.class,
                () -> resolver.resolve(
                        credentialsJson("events-capoeira"),
                        true,
                        resource("{}"),
                        false,
                        "events-capoeira-homologacao"
                )
        );
    }

    @Test
    void shouldRejectHomologationCredentialsInProduction() {
        assertThrows(
                IllegalStateException.class,
                () -> resolver.resolve(
                        credentialsJson("events-capoeira-homologacao"),
                        true,
                        resource("{}"),
                        false,
                        "events-capoeira"
                )
        );
    }

    @Test
    void shouldKeepValidExistingConfigurationCompatibleWhenExpectedProjectIsNotConfigured() {
        var resolved = resolver.resolve(
                credentialsJson("explicit-development-project"),
                true,
                resource("{}"),
                false,
                null
        );

        assertEquals("explicit-development-project", resolved.projectId());
        assertEquals(FirebaseCredentialResolver.CredentialSource.ENVIRONMENT, resolved.source());
    }

    @Test
    void shouldNotUseClasspathCredentialsUnlessFallbackIsExplicitlyAllowed() {
        var exception = assertThrows(
                IllegalStateException.class,
                () -> resolver.resolve(
                        null,
                        false,
                        resource(credentialsJson("events-capoeira")),
                        false,
                        "events-capoeira"
                )
        );

        assertTrue(exception.getMessage().contains("fallback is disabled"));
    }

    private ByteArrayResource resource(String contents) {
        return new ByteArrayResource(contents.getBytes(StandardCharsets.UTF_8));
    }

    private String credentialsJson(String projectId) {
        return "{\"type\":\"service_account\",\"project_id\":\"" + projectId + "\"}";
    }
}
