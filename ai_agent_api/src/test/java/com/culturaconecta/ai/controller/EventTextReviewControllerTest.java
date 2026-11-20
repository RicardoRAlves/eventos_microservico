package com.culturaconecta.ai.controller;

import com.culturaconecta.ai.config.exception.GlobalHandlerException;
import com.culturaconecta.ai.config.exception.ServiceUnavailableException;
import com.culturaconecta.ai.dto.EventTextReviewRequestDto;
import com.culturaconecta.ai.dto.EventTextReviewResponseDto;
import com.culturaconecta.ai.service.EventTextReviewService;
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
class EventTextReviewControllerTest {

    private static final String ENDPOINT = "/api/v1/ai/events/review";

    @Mock
    private EventTextReviewService service;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = standaloneSetup(new EventTextReviewController(service))
                .setControllerAdvice(new GlobalHandlerException())
                .build();
    }

    @Test
    void shouldReturnOkAndReviewedTextForValidRequest() throws Exception {
        when(service.review(any(EventTextReviewRequestDto.class)))
                .thenReturn(new EventTextReviewResponseDto("Texto revisado."));

        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"text":"Texto original."}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviewedText").value("Texto revisado."));

        verify(service).review(new EventTextReviewRequestDto("Texto original."));
    }

    @Test
    void shouldReturnBadRequestWhenTextIsMissing() throws Exception {
        performBadRequest("{}", "Text is required");
    }

    @Test
    void shouldReturnBadRequestWhenTextIsEmpty() throws Exception {
        performBadRequest("""
                {"text":""}
                """, "Text is required");
    }

    @Test
    void shouldReturnBadRequestWhenTextContainsOnlySpaces() throws Exception {
        performBadRequest("""
                {"text":"   "}
                """, "Text is required");
    }

    @Test
    void shouldReturnBadRequestWhenTextExceedsFiveThousandCharacters() throws Exception {
        String json = "{\"text\":\"" + "a".repeat(5001) + "\"}";

        performBadRequest(json, "Text must not exceed 5000 characters");
    }

    @Test
    void shouldReturnServiceUnavailableWhenServiceIsUnavailable() throws Exception {
        when(service.review(any(EventTextReviewRequestDto.class)))
                .thenThrow(new ServiceUnavailableException("provider unavailable"));

        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"text":"Texto original."}
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
