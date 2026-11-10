package com.br.capoeira.eventos.notification.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class FirebaseCredentialResolver {

    private final ObjectMapper objectMapper;

    public FirebaseCredentialResolver(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    ResolvedCredentials resolve(
            String environmentCredentials,
            boolean environmentCredentialsPresent,
            Resource classpathCredentials,
            boolean classpathFallbackAllowed,
            String expectedProjectId
    ) {
        if (environmentCredentialsPresent) {
            return parseAndValidate(
                    environmentCredentials == null
                            ? new byte[0]
                            : environmentCredentials.getBytes(StandardCharsets.UTF_8),
                    CredentialSource.ENVIRONMENT,
                    expectedProjectId
            );
        }

        if (!classpathFallbackAllowed) {
            throw new IllegalStateException(
                    "FIREBASE_CONFIG is not set and classpath credential fallback is disabled"
            );
        }

        if (!classpathCredentials.exists()) {
            throw new IllegalStateException(
                    "FIREBASE_CONFIG is not set and firebase_config.json was not found"
            );
        }

        try (var inputStream = classpathCredentials.getInputStream()) {
            return parseAndValidate(
                    inputStream.readAllBytes(),
                    CredentialSource.CLASSPATH,
                    expectedProjectId
            );
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read firebase_config.json", exception);
        }
    }

    private ResolvedCredentials parseAndValidate(
            byte[] json,
            CredentialSource source,
            String expectedProjectId
    ) {
        JsonNode root;
        try {
            root = objectMapper.readTree(json);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(
                    "Invalid Firebase credentials JSON from " + source,
                    exception
            );
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Unable to read Firebase credentials JSON from " + source,
                    exception
            );
        }

        if (root == null || !root.isObject()) {
            throw new IllegalStateException("Invalid Firebase credentials JSON from " + source);
        }

        JsonNode projectIdNode = root.get("project_id");
        String projectId = projectIdNode == null || !projectIdNode.isTextual()
                ? null
                : projectIdNode.asText().trim();
        if (projectId == null || projectId.isBlank()) {
            throw new IllegalStateException(
                    "Firebase credentials from " + source + " do not contain a valid project_id"
            );
        }

        if (expectedProjectId != null
                && !expectedProjectId.isBlank()
                && !expectedProjectId.trim().equals(projectId)) {
            throw new IllegalStateException(
                    "Firebase project ID mismatch. expected=" + expectedProjectId.trim()
                            + ", configured=" + projectId + ", source=" + source
            );
        }

        return new ResolvedCredentials(json, projectId, source);
    }

    enum CredentialSource {
        ENVIRONMENT,
        CLASSPATH
    }

    record ResolvedCredentials(byte[] json, String projectId, CredentialSource source) {

        ResolvedCredentials {
            json = json.clone();
        }

        @Override
        public byte[] json() {
            return json.clone();
        }
    }
}
