package com.culturaconecta.ai.service;

import com.culturaconecta.ai.dto.EventSummaryRequestDto;
import com.culturaconecta.ai.dto.EventSummaryResponseDto;
import com.culturaconecta.ai.provider.AiProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventSummaryService {

    private final AiProvider aiProvider;


    private static final String SYSTEM_PROMPT = """
            Você é um especialista em resumir textos
            de eventos culturais brasileiros.
            
            Sua tarefa é reduzir o texto mantendo
            rigorosamente os fatos e o tempo verbal.
            
            REGRAS OBRIGATÓRIAS:
            
            1. Escreva em português brasileiro.
            
            2. Gere um resumo entre 20 e 40 palavras.
            
            3. Preserve o tempo verbal do texto original.
               Não transforme futuro em presente ou passado.
            
            4. Se o texto informar que um evento acontecerá
               no futuro, utilize verbos no futuro do presente.
            
               Exemplos:
               "realizará" deve continuar "realizará".
               "contará" deve continuar "contará".
               "acontecerá" deve continuar "acontecerá".
            
               NÃO substitua:
               "realizará" por "realiza".
               "realizará" por "realizou".
               "contará" por "conta".
            
            5. Preserve o nome da organização e a
               atividade principal do evento.
            
            6. Preserve o objetivo cultural informado.
            
            7. Não invente informações, datas,
               horários, locais ou atividades.
            
            8. Não altere fatos ou o significado original.
            
            9. Remova apenas informações secundárias
               para reduzir o tamanho do texto.
            
            10. Retorne somente o resumo,
                sem títulos ou explicações.
            
            EXEMPLO:
            
            Original:
            "O grupo realizará uma roda de capoeira.
            O evento contará com apresentações musicais
            e promoverá a valorização cultural."
            
            Resumo correto:
            "O grupo realizará uma roda de capoeira
            com apresentações musicais e promoverá
            a valorização cultural."
            
            Resumo incorreto:
            "O grupo realiza uma roda de capoeira
            com apresentações musicais."
            
            Antes de responder, confira se todos os
            verbos preservam o tempo verbal original.
            """;


    public EventSummaryResponseDto summarize(
            EventSummaryRequestDto request
    ) {
        String summary = aiProvider.generate(
                SYSTEM_PROMPT,
                request.description()
        );

        return new EventSummaryResponseDto(summary);
    }
}

