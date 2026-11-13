package com.culturaconecta.ai.service;

import com.culturaconecta.ai.config.exception.ServiceUnavailableException;
import com.culturaconecta.ai.dto.EventDescriptionRequestDto;
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
class EventDescriptionServiceTest {

    @Mock
    private AiProvider aiProvider;

    @InjectMocks
    private EventDescriptionService service;

    @Test
    void shouldGenerateDescriptionAndReturnProviderResponse() {
        EventDescriptionRequestDto event = event("Festival de Cinema", "Cinema");
        when(aiProvider.generate(anyString(), anyString()))
                .thenReturn("Descrição gerada pelo provider.");

        String result = service.generateDescription(event);

        assertThat(result).isEqualTo("Descrição gerada pelo provider.");
        verify(aiProvider).generate(anyString(), anyString());
    }

    @Test
    void shouldIncludeTitleAndCategoryInUserPrompt() {
        EventDescriptionRequestDto event = event("Mostra de Arte Urbana", "Artes visuais");
        ArgumentCaptor<String> systemPrompt = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> userPrompt = ArgumentCaptor.forClass(String.class);

        service.generateDescription(event);

        verify(aiProvider).generate(systemPrompt.capture(), userPrompt.capture());
        assertThat(systemPrompt.getValue()).contains("Cultura Conecta", "português brasileiro");
        assertThat(userPrompt.getValue())
                .contains("Título: Mostra de Arte Urbana")
                .contains("Categoria: Artes visuais");
    }

    @Test
    void shouldIncludeOptionalInformationWhenProvided() {
        EventDescriptionRequestDto event = event("Sarau da Praça", "Literatura");
        event.setLocation("Praça Central");
        event.setTargetAudience("Público adulto");
        event.setAdditionalInformation("Entrada gratuita");
        ArgumentCaptor<String> userPrompt = ArgumentCaptor.forClass(String.class);

        service.generateDescription(event);

        verify(aiProvider).generate(anyString(), userPrompt.capture());
        assertThat(userPrompt.getValue())
                .contains("Local: Praça Central")
                .contains("Público-alvo: Público adulto")
                .contains("Informações adicionais: Entrada gratuita");
    }

    @Test
    void shouldRepresentMissingOrBlankOptionalInformationAsNotProvided() {
        EventDescriptionRequestDto event = event("Concerto", "Música");
        event.setLocation(null);
        event.setTargetAudience("   ");
        event.setAdditionalInformation("");
        ArgumentCaptor<String> userPrompt = ArgumentCaptor.forClass(String.class);

        service.generateDescription(event);

        verify(aiProvider).generate(anyString(), userPrompt.capture());
        assertThat(userPrompt.getValue())
                .contains("Local: Não informado")
                .contains("Público-alvo: Não informado")
                .contains("Informações adicionais: Não informado");
    }

    @Test
    void shouldPropagateServiceUnavailableException() {
        EventDescriptionRequestDto event = event("Peça", "Teatro");
        ServiceUnavailableException failure = new ServiceUnavailableException("provider unavailable");
        when(aiProvider.generate(anyString(), anyString())).thenThrow(failure);

        assertThatThrownBy(() -> service.generateDescription(event))
                .isSameAs(failure);
    }

    @Test
    void shouldPropagateUnexpectedErrorsWithoutMaskingThem() {
        EventDescriptionRequestDto event = event("Peça", "Teatro");
        IllegalStateException failure = new IllegalStateException("unexpected provider failure");
        when(aiProvider.generate(anyString(), anyString())).thenThrow(failure);

        assertThatThrownBy(() -> service.generateDescription(event))
                .isSameAs(failure);
    }

    private EventDescriptionRequestDto event(String title, String category) {
        EventDescriptionRequestDto event = new EventDescriptionRequestDto();
        event.setTitle(title);
        event.setCategory(category);
        return event;
    }
}
