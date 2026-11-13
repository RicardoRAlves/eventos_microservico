package com.culturaconecta.ai.provider;

import com.culturaconecta.ai.config.exception.ServiceUnavailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.retry.NonTransientAiException;
import org.springframework.ai.retry.TransientAiException;
import org.springframework.web.client.ResourceAccessException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OllamaAiProviderTest {

    @Mock
    private ChatClient.Builder builder;

    @Mock
    private ChatClient chatClient;

    @Mock
    private ChatClient.ChatClientRequestSpec requestSpec;

    @Mock
    private ChatClient.CallResponseSpec responseSpec;

    private OllamaAiProvider provider;

    @BeforeEach
    void setUp() {
        when(builder.build()).thenReturn(chatClient);
        provider = new OllamaAiProvider(builder);
    }

    @Test
    void shouldSendSystemAndUserPromptsAndReturnGeneratedContent() {
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system("system prompt")).thenReturn(requestSpec);
        when(requestSpec.user("user prompt")).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
        when(responseSpec.content()).thenReturn("generated content");

        String result = provider.generate("system prompt", "user prompt");

        assertThat(result).isEqualTo("generated content");
        verify(requestSpec).system("system prompt");
        verify(requestSpec).user("user prompt");
        verify(requestSpec).call();
        verify(responseSpec).content();
    }

    @Test
    void shouldWrapResourceAccessExceptionAndPreserveCause() {
        ResourceAccessException original = new ResourceAccessException("connection refused");
        when(chatClient.prompt()).thenThrow(original);

        assertThatThrownBy(() -> provider.generate("system", "user"))
                .isInstanceOf(ServiceUnavailableException.class)
                .hasMessage("AI service is temporarily unavailable")
                .hasCause(original);
    }

    @Test
    void shouldWrapTransientAiExceptionAndPreserveCause() {
        TransientAiException original = new TransientAiException("temporary failure");
        when(chatClient.prompt()).thenThrow(original);

        assertThatThrownBy(() -> provider.generate("system", "user"))
                .isInstanceOf(ServiceUnavailableException.class)
                .hasMessage("AI provider temporarily unavailable")
                .hasCause(original);
    }

    @Test
    void shouldPropagateNonTransientAiExceptionUnchanged() {
        NonTransientAiException original = new NonTransientAiException("permanent failure");
        when(chatClient.prompt()).thenThrow(original);

        assertThatThrownBy(() -> provider.generate("system", "user"))
                .isSameAs(original);
    }
}
