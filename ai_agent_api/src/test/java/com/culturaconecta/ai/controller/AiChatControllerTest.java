package com.culturaconecta.ai.controller;

import com.culturaconecta.ai.config.exception.GlobalHandlerException;
import com.culturaconecta.ai.config.exception.ServiceUnavailableException;
import com.culturaconecta.ai.service.AiChatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class AiChatControllerTest {

    @Mock
    private AiChatService service;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = standaloneSetup(new AiChatController(service))
                .setControllerAdvice(new GlobalHandlerException())
                .build();
    }

    @Test
    void shouldReturnOkAndResponseJsonForValidMessage() throws Exception {
        when(service.question("Quais eventos existem?", "São Paulo"))
                .thenReturn("Há uma mostra disponível.");

        mockMvc.perform(post("/api/v1/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"message":"Quais eventos existem?","context":"São Paulo"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Há uma mostra disponível."));

        verify(service).question("Quais eventos existem?", "São Paulo");
    }

    @Test
    void shouldAcceptRequestWithoutOptionalContext() throws Exception {
        when(service.question("Quais eventos existem?", null)).thenReturn("Resposta");

        mockMvc.perform(post("/api/v1/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"message":"Quais eventos existem?"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Resposta"));

        verify(service).question("Quais eventos existem?", null);
    }

    @Test
    void shouldReturnBadRequestForBlankMessage() throws Exception {
        mockMvc.perform(post("/api/v1/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"message":"   "}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Message is required"))
                .andExpect(jsonPath("$.path").value("/api/v1/ai/chat"));
    }

    @Test
    void shouldReturnBadRequestForMissingMessage() throws Exception {
        mockMvc.perform(post("/api/v1/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Message is required"));
    }

    @Test
    void shouldReturnServiceUnavailableWhenServiceIsUnavailable() throws Exception {
        when(service.question("Pergunta", null))
                .thenThrow(new ServiceUnavailableException("provider unavailable"));

        mockMvc.perform(post("/api/v1/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"message":"Pergunta"}
                                """))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503))
                .andExpect(jsonPath("$.message").value("External service unavailable"))
                .andExpect(jsonPath("$.path").value("/api/v1/ai/chat"))
                .andExpect(jsonPath("$.error").value("AI service is temporarily unavailable"));
    }
}
