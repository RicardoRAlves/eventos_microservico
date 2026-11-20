package com.culturaconecta.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cultura-conecta.rag")
public record RagProperties(
        double maxDistance,
        int maxResults
) {
}

