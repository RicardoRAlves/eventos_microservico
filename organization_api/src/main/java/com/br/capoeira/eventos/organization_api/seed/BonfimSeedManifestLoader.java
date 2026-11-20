package com.br.capoeira.eventos.organization_api.seed;

import com.br.capoeira.eventos.organization_api.config.exception.BonfimSeedException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class BonfimSeedManifestLoader {

    private final ObjectMapper objectMapper;
    private final ResourceLoader resourceLoader;

    public BonfimSeedManifest load(String location) {
        var resource = resourceLoader.getResource(location);
        if (!resource.exists()) {
            throw new BonfimSeedException("Bonfim seed resource was not found");
        }

        try (var input = resource.getInputStream()) {
            return objectMapper.copy()
                    .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                    .readValue(input, BonfimSeedManifest.class);
        } catch (IOException e) {
            throw new BonfimSeedException("Could not read Bonfim seed resource", e);
        }
    }
}
