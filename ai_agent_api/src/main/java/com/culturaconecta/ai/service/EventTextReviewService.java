package com.culturaconecta.ai.service;

import com.culturaconecta.ai.dto.EventTextReviewRequestDto;
import com.culturaconecta.ai.dto.EventTextReviewResponseDto;
import com.culturaconecta.ai.provider.AiProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventTextReviewService {

    private final AiProvider aiProvider;

    private static final String SYSTEM_PROMPT = """
            Você é um revisor profissional de textos
            especializado em eventos culturais brasileiros.

            Sua tarefa é revisar o texto fornecido,
            corrigindo:
            - Ortografia;
            - Gramática;
            - Pontuação;
            - Concordância verbal e nominal;
            - Clareza e fluidez.

            Regras obrigatórias:
            1. Utilize português brasileiro.
            2. Preserve o significado original.
            3. Não invente informações.
            4. Não acrescente datas, horários ou locais.
            5. Preserve nomes próprios e termos culturais.
            6. Não transforme o texto em propaganda.
            7. Não explique as alterações realizadas.
            8. Retorne somente o texto revisado.
            9. Preserve a estrutura em parágrafos,
               quando existir.
            """;

    public EventTextReviewResponseDto review(
            EventTextReviewRequestDto request
    ) {
        String reviewedText = aiProvider.generate(
                SYSTEM_PROMPT,
                request.text()
        );

        return new EventTextReviewResponseDto(reviewedText);
    }
}

