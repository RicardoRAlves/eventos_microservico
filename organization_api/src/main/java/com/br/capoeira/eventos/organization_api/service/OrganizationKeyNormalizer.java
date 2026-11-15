package com.br.capoeira.eventos.organization_api.service;

import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

@Component
public class OrganizationKeyNormalizer {

    public static final int MAX_KEY_LENGTH = 120;

    private static final Pattern COMBINING_MARKS = Pattern.compile("\\p{M}+");
    private static final Pattern NON_ALPHANUMERIC_SEQUENCE = Pattern.compile("[^a-z0-9]+");
    private static final Pattern EDGE_HYPHENS = Pattern.compile("(^-+|-+$)");

    public String normalize(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Organization name must not be null or blank");
        }

        String unicodeNormalized = Normalizer.normalize(name.strip(), Normalizer.Form.NFKD);
        String withoutAccents = COMBINING_MARKS.matcher(unicodeNormalized).replaceAll("");
        String key = withoutAccents.toLowerCase(Locale.ROOT);
        key = NON_ALPHANUMERIC_SEQUENCE.matcher(key).replaceAll("-");
        key = EDGE_HYPHENS.matcher(key).replaceAll("");

        if (key.isEmpty()) {
            throw new IllegalArgumentException("Organization name must contain at least one ASCII letter or digit");
        }
        if (key.length() > MAX_KEY_LENGTH) {
            throw new IllegalArgumentException(
                    "Normalized organization key must not exceed " + MAX_KEY_LENGTH + " characters"
            );
        }

        return key;
    }
}
