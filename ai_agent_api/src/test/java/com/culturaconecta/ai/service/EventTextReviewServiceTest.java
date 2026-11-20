package com.culturaconecta.ai.service;

import com.culturaconecta.ai.config.exception.ServiceUnavailableException;
import com.culturaconecta.ai.dto.EventTextReviewRequestDto;
import com.culturaconecta.ai.dto.EventTextReviewResponseDto;
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
class EventTextReviewServiceTest {

    @Mock
    private AiProvider aiProvider;

    @InjectMocks
    private EventTextReviewService service;

    @Test
    void shouldSendOriginalTextAndEssentialInstructionsAndReturnReviewedText() {
        String originalText = "O evento acontecerá sabado no centro cultural.";
        when(aiProvider.generate(anyString(), eq(originalText)))
                .thenReturn("O evento acontecerá sábado no centro cultural.");
        ArgumentCaptor<String> systemPrompt = ArgumentCaptor.forClass(String.class);

        EventTextReviewResponseDto response =
                service.review(new EventTextReviewRequestDto(originalText));

        assertThat(response.reviewedText())
                .isEqualTo("O evento acontecerá sábado no centro cultural.");
        verify(aiProvider).generate(systemPrompt.capture(), eq(originalText));
        assertThat(systemPrompt.getValue())
                .contains("português brasileiro")
                .contains("Preserve o significado original")
                .contains("Não invente informações")
                .contains("Retorne somente o texto revisado");
    }

    @Test
    void shouldPropagateServiceUnavailableException() {
        ServiceUnavailableException failure =
                new ServiceUnavailableException("provider unavailable");
        when(aiProvider.generate(anyString(), anyString())).thenThrow(failure);

        assertThatThrownBy(() -> service.review(new EventTextReviewRequestDto("Texto original")))
                .isSameAs(failure);
    }

    @Test
    void shouldPropagateUnexpectedExceptionWithoutMaskingIt() {
        IllegalStateException failure = new IllegalStateException("unexpected provider failure");
        when(aiProvider.generate(anyString(), anyString())).thenThrow(failure);

        assertThatThrownBy(() -> service.review(new EventTextReviewRequestDto("Texto original")))
                .isSameAs(failure);
    }
}
