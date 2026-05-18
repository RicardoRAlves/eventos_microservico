package com.br.capoeira.eventos.user_api.controller;

import com.br.capoeira.eventos.user_api.config.RestClientConfig;
import com.br.capoeira.eventos.user_api.config.filter.JwtAuthenticationFilter;
import com.br.capoeira.eventos.user_api.dto.AuthenticationRequestDto;
import com.br.capoeira.eventos.user_api.dto.AuthenticationResponseDto;
import com.br.capoeira.eventos.user_api.repository.UserFavoriteEventsRepository;
import com.br.capoeira.eventos.user_api.repository.UserReservationEventRepository;
import com.br.capoeira.eventos.user_api.restClient.OrganizationClient;
import com.br.capoeira.eventos.user_api.service.AuthenticationService;
import com.br.capoeira.eventos.user_api.service.CustomUserDetailsService;
import com.br.capoeira.eventos.user_api.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=" +
                "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
                "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration," +
                "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration",

        "spring.rabbitmq.listener.simple.auto-startup=false",
        "spring.rabbitmq.listener.direct.auto-startup=false",

        "rabbitmq.exchange.delete-notification.name=test.exchange",
        "rabbitmq.queue.delete-notification.name=test.queue",
        "rabbitmq.routing-key.delete-notification.name=test.routing"
})
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class AuthenticationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthenticationService service;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private RestClientConfig clientConfig;

    @MockitoBean
    private UserController userController;

    @MockitoBean
    private OrganizationClient organizationClient;

    @MockitoBean
    private RabbitAdmin rabbitAdmin;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserFavoriteEventsRepository userFavoriteEventsRepository;

    @MockitoBean
    private UserReservationEventRepository userReservationEventRepository;

    @Test
    void shouldLoginSuccessfully() throws Exception {
        var request = new AuthenticationRequestDto();
        request.setEmail("user@test.com");
        request.setPassword("123456");

        var response = new AuthenticationResponseDto();
        response.setToken("jwt-token");

        when(service.authenticate(any(AuthenticationRequestDto.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"));

        verify(service).authenticate(any(AuthenticationRequestDto.class));
    }

    @Test
    void shouldReturnBadRequestWhenEmailIsInvalid() throws Exception {
        var request = new AuthenticationRequestDto();
        request.setEmail("email-invalido");
        request.setPassword("123456");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenPasswordIsBlank() throws Exception {
        var request = new AuthenticationRequestDto();
        request.setEmail("user@test.com");
        request.setPassword("");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}