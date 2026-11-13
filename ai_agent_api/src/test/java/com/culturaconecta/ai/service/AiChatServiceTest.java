package com.culturaconecta.ai.service;

import com.culturaconecta.ai.config.exception.ServiceUnavailableException;
import com.culturaconecta.ai.provider.AiProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiChatServiceTest {

    @Mock
    private AiProvider aiProvider;

    @InjectMocks
    private AiChatService service;

    @Test
    void shouldForwardQuestionWithoutContextAndReturnGeneratedAnswer() {
        when(aiProvider.generate(anyString(), anyString())).thenReturn("Resposta gerada");
        ArgumentCaptor<String> systemPrompt = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> userPrompt = ArgumentCaptor.forClass(String.class);

        String result = service.question("Quais eventos estão disponíveis?", null);

        assertThat(result).isEqualTo("Resposta gerada");
        verify(aiProvider).generate(systemPrompt.capture(), userPrompt.capture());
        assertThat(systemPrompt.getValue()).contains("Cultura Conecta", "português brasileiro");
        assertThat(userPrompt.getValue())
                .contains("Solicitação:", "Quais eventos estão disponíveis?", "Contexto:")
                .doesNotContain("null");
    }

    @Test
    void shouldForwardQuestionWithContext() {
        ArgumentCaptor<String> userPrompt = ArgumentCaptor.forClass(String.class);

        service.question("Recomende uma atividade", "Usuário interessado em música");

        verify(aiProvider).generate(anyString(), userPrompt.capture());
        assertThat(userPrompt.getValue())
                .contains("Recomende uma atividade")
                .contains("Usuário interessado em música");
    }

    @Test
    void shouldPropagateProviderUnavailability() {
        ServiceUnavailableException failure = new ServiceUnavailableException("provider unavailable");
        when(aiProvider.generate(anyString(), anyString())).thenThrow(failure);

        assertThatThrownBy(() -> service.question("Pergunta", null))
                .isSameAs(failure);
    }

    @Test
    void shouldReturnNullWhenProviderReturnsNull() {
        when(aiProvider.generate(anyString(), anyString())).thenReturn(null);

        assertThat(service.question("Pergunta", null)).isNull();
    }

    @Test
    void shouldReturnBlankAnswerWhenProviderReturnsBlankAnswer() {
        when(aiProvider.generate(anyString(), anyString())).thenReturn("   ");

        assertThat(service.question("Pergunta", "Contexto")).isEqualTo("   ");
    }
}
