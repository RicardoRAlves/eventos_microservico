package com.culturaconecta.ai.provider;

import com.culturaconecta.ai.config.exception.ServiceUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.retry.NonTransientAiException;
import org.springframework.ai.retry.TransientAiException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;

@Slf4j
@Component
public class OllamaAiProvider implements AiProvider {

    private final ChatClient chatClient;

    public OllamaAiProvider(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }


    @Override
    public String generate(String systemPrompt, String userPrompt) {

        try {
            return chatClient.prompt()
                    .system(systemPrompt)
                    .user(userPrompt)
                    .call()
                    .content();

        } catch (ResourceAccessException e) {
            throw new ServiceUnavailableException(
                    "AI service is temporarily unavailable",
                    e
            );

        } catch (TransientAiException e) {
            throw new ServiceUnavailableException(
                    "AI provider temporarily unavailable",
                    e
            );

        } catch (NonTransientAiException e) {
            log.error("Non-transient AI provider error", e);
            throw e;
        }
    }

}

