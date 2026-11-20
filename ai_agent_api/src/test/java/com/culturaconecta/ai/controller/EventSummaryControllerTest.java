package com.culturaconecta.ai.controller;

import com.culturaconecta.ai.config.exception.GlobalHandlerException;
import com.culturaconecta.ai.config.exception.ServiceUnavailableException;
import com.culturaconecta.ai.dto.EventSummaryRequestDto;
import com.culturaconecta.ai.dto.EventSummaryResponseDto;
import com.culturaconecta.ai.service.EventSummaryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class EventSummaryControllerTest {

    private static final String ENDPOINT = "/api/v1/ai/events/summary";

    @Mock
    private EventSummaryService service;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = standaloneSetup(new EventSummaryController(service))
                .setControllerAdvice(new GlobalHandlerException())
                .build();
    }

    @Test
    void shouldReturnOkAndSummaryForValidRequest() throws Exception {
        when(service.summarize(any(EventSummaryRequestDto.class)))
                .thenReturn(new EventSummaryResponseDto("Resumo do evento."));

        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"description":"Descrição completa do evento."}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary").value("Resumo do evento."));

        verify(service).summarize(
                new EventSummaryRequestDto("Descrição completa do evento."));
    }

    @Test
    void shouldReturnBadRequestWhenDescriptionIsMissing() throws Exception {
        performBadRequest("{}", "Description is required");
    }

    @Test
    void shouldReturnBadRequestWhenDescriptionIsEmpty() throws Exception {
        performBadRequest("""
                {"description":""}
                """, "Description is required");
    }

    @Test
    void shouldReturnBadRequestWhenDescriptionContainsOnlySpaces() throws Exception {
        performBadRequest("""
                {"description":"   "}
                """, "Description is required");
    }

    @Test
    void shouldReturnBadRequestWhenDescriptionExceedsFiveThousandCharacters() throws Exception {
        String json = "{\"description\":\"" + "a".repeat(5001) + "\"}";

        performBadRequest(json, "Description must not exceed 5000 characters");
    }

    @Test
    void shouldReturnServiceUnavailableWhenServiceIsUnavailable() throws Exception {
        when(service.summarize(any(EventSummaryRequestDto.class)))
                .thenThrow(new ServiceUnavailableException("provider unavailable"));

        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"description":"Descrição completa do evento."}
                                """))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503))
                .andExpect(jsonPath("$.message").value("External service unavailable"))
                .andExpect(jsonPath("$.path").value(ENDPOINT))
                .andExpect(jsonPath("$.error").value("AI service is temporarily unavailable"));
    }

    private void performBadRequest(String json, String expectedError) throws Exception {
        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.path").value(ENDPOINT))
                .andExpect(jsonPath("$.error").value(expectedError));
    }
}
