package com.culturaconecta.ai.service;

import com.culturaconecta.ai.provider.AiProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiChatService {

    private final AiProvider aiProvider;

    public String question(String message, String context) {

        String systemPrompt = """
                Você é o assistente Cultura Conecta.
                Responda em português brasileiro.
                Não invente informações.
                """;

        String userPrompt = """
                Solicitação:
                %s

                Contexto:
                %s
                """.formatted(
                message,
                context == null ? "" : context
        );

        return aiProvider.generate(systemPrompt, userPrompt);
    }
}
