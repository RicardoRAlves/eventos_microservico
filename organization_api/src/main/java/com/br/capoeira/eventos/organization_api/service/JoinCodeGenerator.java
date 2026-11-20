package com.br.capoeira.eventos.organization_api.service;

import com.br.capoeira.eventos.organization_api.config.exception.ValidationException;
import com.br.capoeira.eventos.organization_api.repository.OrganizationUnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
public class JoinCodeGenerator {

    private static final String JOIN_CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int JOIN_CODE_LENGTH = 6;
    private static final int MAX_ATTEMPTS = 20;

    private final OrganizationUnitRepository organizationUnitRepository;

    public String generateUnique() {
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            String code = generate();
            if (!organizationUnitRepository.existsByJoinCode(code)) {
                return code;
            }
        }

        throw new ValidationException("Could not generate a unique join code");
    }

    private String generate() {
        var result = new StringBuilder(JOIN_CODE_LENGTH);

        for (int i = 0; i < JOIN_CODE_LENGTH; i++) {
            int index = ThreadLocalRandom.current().nextInt(JOIN_CODE_CHARS.length());
            result.append(JOIN_CODE_CHARS.charAt(index));
        }

        return result.toString();
    }
}
