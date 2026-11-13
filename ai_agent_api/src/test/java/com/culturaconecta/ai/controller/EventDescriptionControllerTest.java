package com.culturaconecta.ai.controller;

import com.culturaconecta.ai.config.exception.GlobalHandlerException;
import com.culturaconecta.ai.config.exception.ServiceUnavailableException;
import com.culturaconecta.ai.dto.EventDescriptionRequestDto;
import com.culturaconecta.ai.service.EventDescriptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class EventDescriptionControllerTest {

    @Mock
    private EventDescriptionService service;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = standaloneSetup(new EventDescriptionController(service))
                .setControllerAdvice(new GlobalHandlerException())
                .build();
    }

    @Test
    void shouldReturnOkAndGeneratedDescriptionForValidData() throws Exception {
        when(service.generateDescription(any(EventDescriptionRequestDto.class)))
                .thenReturn("Uma descrição cultural convidativa.");

        mockMvc.perform(post("/api/v1/ai/events/description")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title":"Festival de Cinema",
                                  "category":"Cinema",
                                  "location":"Centro Cultural"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Uma descrição cultural convidativa."));
    }

    @Test
    void shouldAcceptRequestWithOptionalFieldsOmitted() throws Exception {
        when(service.generateDescription(any(EventDescriptionRequestDto.class))).thenReturn("Descrição");
        ArgumentCaptor<EventDescriptionRequestDto> request =
                ArgumentCaptor.forClass(EventDescriptionRequestDto.class);

        mockMvc.perform(post("/api/v1/ai/events/description")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Sarau","category":"Literatura"}
                                """))
                .andExpect(status().isOk());

        verify(service).generateDescription(request.capture());
        assertThat(request.getValue().getLocation()).isNull();
        assertThat(request.getValue().getTargetAudience()).isNull();
        assertThat(request.getValue().getAdditionalInformation()).isNull();
    }

    @Test
    void shouldReturnBadRequestWhenTitleIsMissing() throws Exception {
        performBadRequest("""
                {"category":"Música"}
                """, "Event title is required");
    }

    @Test
    void shouldReturnBadRequestWhenTitleIsBlank() throws Exception {
        performBadRequest("""
                {"title":"   ","category":"Música"}
                """, "Event title is required");
    }

    @Test
    void shouldReturnBadRequestWhenCategoryIsMissing() throws Exception {
        performBadRequest("""
                {"title":"Concerto"}
                """, "Event category is required");
    }

    @Test
    void shouldReturnBadRequestWhenCategoryIsBlank() throws Exception {
        performBadRequest("""
                {"title":"Concerto","category":""}
                """, "Event category is required");
    }

    @Test
    void shouldReturnServiceUnavailableWhenProviderIsUnavailable() throws Exception {
        when(service.generateDescription(any(EventDescriptionRequestDto.class)))
                .thenThrow(new ServiceUnavailableException("provider unavailable"));

        mockMvc.perform(post("/api/v1/ai/events/description")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Concerto","category":"Música"}
                                """))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503))
                .andExpect(jsonPath("$.message").value("External service unavailable"))
                .andExpect(jsonPath("$.path").value("/api/v1/ai/events/description"))
                .andExpect(jsonPath("$.error").value("AI service is temporarily unavailable"));
    }

    private void performBadRequest(String json, String expectedError) throws Exception {
        mockMvc.perform(post("/api/v1/ai/events/description")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value(expectedError))
                .andExpect(jsonPath("$.path").value("/api/v1/ai/events/description"));
    }
}
