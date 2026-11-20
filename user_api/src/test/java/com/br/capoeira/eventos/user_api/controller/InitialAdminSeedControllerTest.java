package com.br.capoeira.eventos.user_api.controller;

import com.br.capoeira.eventos.user_api.config.SecurityConfig;
import com.br.capoeira.eventos.user_api.config.filter.JwtAuthenticationFilter;
import com.br.capoeira.eventos.user_api.dto.InitialAdminSeedRequestDto;
import com.br.capoeira.eventos.user_api.dto.UserResponseDto;
import com.br.capoeira.eventos.user_api.enums.Role;
import com.br.capoeira.eventos.user_api.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = InitialAdminSeedController.class,
        properties = "app.initial-data-seed.enabled=true"
)
@Import(SecurityConfig.class)
@ActiveProfiles("local")
class InitialAdminSeedControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void allowFilterChain() throws Exception {
        doAnswer(invocation -> {
            var chain = invocation.getArgument(2, FilterChain.class);
            chain.doFilter(
                    invocation.getArgument(0, ServletRequest.class),
                    invocation.getArgument(1, ServletResponse.class)
            );
            return null;
        }).when(jwtAuthenticationFilter).doFilter(
                any(ServletRequest.class),
                any(ServletResponse.class),
                any(FilterChain.class)
        );
    }

    @Test
    @WithMockUser(roles = "SERVICE")
    void shouldAllowServiceToEnsureInitialAdministrator() throws Exception {
        var request = request();
        var response = new UserResponseDto(
                1L,
                request.name(),
                request.email(),
                Role.CLIENT,
                true,
                null,
                null,
                request.avatarUrl()
        );
        when(userService.ensureInitialAdmin(request)).thenReturn(response);

        mockMvc.perform(post("/internal/users/initial-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value(request.email()));
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void shouldRejectNonServiceUser() throws Exception {
        mockMvc.perform(post("/internal/users/initial-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isForbidden());
    }

    private InitialAdminSeedRequestDto request() {
        return new InitialAdminSeedRequestDto(
                "Initial Administrator",
                "seed@test.local",
                "environment-secret",
                "https://assets.test/admin.png"
        );
    }
}
