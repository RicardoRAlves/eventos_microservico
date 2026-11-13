package com.culturaconecta.ai.config.exception;

import com.culturaconecta.ai.dto.AiChatInputDto;
import jakarta.validation.Valid;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class GlobalHandlerExceptionTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = standaloneSetup(new FailingController())
                .setControllerAdvice(new GlobalHandlerException())
                .build();
    }

    @Test
    void shouldMapServiceUnavailableExceptionToSafe503Response() throws Exception {
        mockMvc.perform(get("/handler-test/unavailable"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp", greaterThan(0L)))
                .andExpect(jsonPath("$.status").value(503))
                .andExpect(jsonPath("$.message").value("External service unavailable"))
                .andExpect(jsonPath("$.path").value("/handler-test/unavailable"))
                .andExpect(jsonPath("$.error").value("AI service is temporarily unavailable"))
                .andExpect(jsonPath("$.exception").doesNotExist())
                .andExpect(jsonPath("$.trace").doesNotExist());
    }

    @Test
    void shouldMapValidationExceptionTo400Response() throws Exception {
        mockMvc.perform(post("/handler-test/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp", greaterThan(0L)))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Error on trying to validate attributes"))
                .andExpect(jsonPath("$.path").value("/handler-test/validation"))
                .andExpect(jsonPath("$.error").value("Message is required"))
                .andExpect(jsonPath("$.exception").doesNotExist())
                .andExpect(jsonPath("$.trace").doesNotExist());
    }

    @Test
    void shouldMapGenericExceptionToSafe500Response() throws Exception {
        mockMvc.perform(get("/handler-test/generic"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.timestamp", greaterThan(0L)))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Unexpected internal error"))
                .andExpect(jsonPath("$.path").value("/handler-test/generic"))
                .andExpect(jsonPath("$.error").value("An unexpected error occurred"))
                .andExpect(jsonPath("$.exception").doesNotExist())
                .andExpect(jsonPath("$.trace").doesNotExist())
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("sensitive internal detail"))));
    }

    @Test
    void shouldExposeStandardExceptionFieldsInTheirCorrectPositions() {
        StandardException exception = new StandardException(
                123L, 503, "message", "/correct-path", "correct-error");

        assertThat(exception.getTimestamp()).isEqualTo(123L);
        assertThat(exception.getStatus()).isEqualTo(503);
        assertThat(exception.getMessage()).isEqualTo("message");
        assertThat(exception.getPath()).isEqualTo("/correct-path");
        assertThat(exception.getError()).isEqualTo("correct-error");
    }

    @RestController
    @RequestMapping("/handler-test")
    static class FailingController {

        @GetMapping("/unavailable")
        void unavailable() {
            throw new ServiceUnavailableException("sensitive provider detail");
        }

        @PostMapping("/validation")
        void validation(@Valid @RequestBody AiChatInputDto input) {
        }

        @GetMapping("/generic")
        void generic() {
            throw new IllegalStateException("sensitive internal detail");
        }
    }
}
