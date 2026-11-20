package com.culturaconecta.ai.service;

import com.culturaconecta.ai.config.exception.ServiceUnavailableException;
import com.culturaconecta.ai.dto.EventSummaryRequestDto;
import com.culturaconecta.ai.dto.EventSummaryResponseDto;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventSummaryServiceTest {

    @Mock
    private AiProvider aiProvider;

    @InjectMocks
    private EventSummaryService service;

    @Test
    void shouldSendOriginalDescriptionAndRequiredInstructionsAndReturnSummary() {
        String description = "O grupo realizará uma roda de capoeira e promoverá a cultura local.";
        String generatedSummary = "O grupo realizará uma roda de capoeira e promoverá a cultura local.";
        when(aiProvider.generate(anyString(), eq(description))).thenReturn(generatedSummary);
        ArgumentCaptor<String> systemPrompt = ArgumentCaptor.forClass(String.class);

        EventSummaryResponseDto response =
                service.summarize(new EventSummaryRequestDto(description));

        assertThat(response.summary()).isEqualTo(generatedSummary);
        verify(aiProvider).generate(systemPrompt.capture(), eq(description));
        assertThat(systemPrompt.getValue())
                .contains("Preserve o tempo verbal do texto original")
                .contains("Não transforme futuro em presente ou passado")
                .contains("Não invente informações")
                .contains("Retorne somente o resumo");
    }

    @Test
    void shouldPropagateServiceUnavailableException() {
        ServiceUnavailableException failure =
                new ServiceUnavailableException("provider unavailable");
        when(aiProvider.generate(anyString(), anyString())).thenThrow(failure);

        assertThatThrownBy(() -> service.summarize(new EventSummaryRequestDto("Descrição original")))
                .isSameAs(failure);
    }

    @Test
    void shouldPropagateUnexpectedExceptionWithoutMaskingIt() {
        IllegalArgumentException failure =
                new IllegalArgumentException("unexpected provider failure");
        when(aiProvider.generate(anyString(), anyString())).thenThrow(failure);

        assertThatThrownBy(() -> service.summarize(new EventSummaryRequestDto("Descrição original")))
                .isSameAs(failure);
    }
}
