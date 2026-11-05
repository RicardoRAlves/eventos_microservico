package com.br.capoeira.eventos.event_api.controller;

import com.br.capoeira.eventos.event_api.dto.CategoryCreateRequestDto;
import com.br.capoeira.eventos.event_api.dto.CategoryReactivateRequestDto;
import com.br.capoeira.eventos.event_api.dto.CategoryResponseDto;
import com.br.capoeira.eventos.event_api.dto.CategoryUpdateRequestDto;
import com.br.capoeira.eventos.event_api.dto.PageResponseDto;
import com.br.capoeira.eventos.event_api.service.CategoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryController.class)
@AutoConfigureMockMvc
@Import(CategoryControllerIntegrationTest.TestSecurityConfig.class)
class CategoryControllerIntegrationTest {

    private static final String BASE_URL = "/api/v1/categoria";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CategoryService categoryService;

    @TestConfiguration
    @EnableMethodSecurity
    static class TestSecurityConfig {
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldFindAllCategoriesSuccessfully() throws Exception {
        var responseDto = getMockCategoryResponseDto(1L, "Capoeira", true);

        var pageResponse = new PageResponseDto<>(
                List.of(responseDto),
                0,
                10,
                1L,
                1,
                true
        );

        when(categoryService.findAll(0, 10)).thenReturn(pageResponse);

        mockMvc.perform(get(BASE_URL + "/all")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].name").value("Capoeira"))
                .andExpect(jsonPath("$.content[0].active").value(true))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.last").value(true));

        verify(categoryService).findAll(0, 10);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldFindCategoryByIdSuccessfully() throws Exception {
        var responseDto = getMockCategoryResponseDto(1L, "Capoeira", true);

        when(categoryService.findById(1L)).thenReturn(responseDto);

        mockMvc.perform(get(BASE_URL + "/id/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Capoeira"))
                .andExpect(jsonPath("$.active").value(true));

        verify(categoryService).findById(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldFindCategoryByNameSuccessfully() throws Exception {
        var responseDto = getMockCategoryResponseDto(1L, "Capoeira", true);

        when(categoryService.findByName("Capoeira")).thenReturn(responseDto);

        mockMvc.perform(get(BASE_URL + "/name/{name}", "Capoeira"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Capoeira"))
                .andExpect(jsonPath("$.active").value(true));

        verify(categoryService).findByName("Capoeira");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldCreateCategorySuccessfully() throws Exception {
        var requestDto = new CategoryCreateRequestDto("Capoeira");
        var responseDto = getMockCategoryResponseDto(1L, "Capoeira", true);

        when(categoryService.create(any(CategoryCreateRequestDto.class)))
                .thenReturn(responseDto);

        mockMvc.perform(post(BASE_URL)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Capoeira"))
                .andExpect(jsonPath("$.active").value(true));

        verify(categoryService).create(any(CategoryCreateRequestDto.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateCategorySuccessfully() throws Exception {
        var requestDto = new CategoryUpdateRequestDto(1L, "Capoeira");
        var responseDto = getMockCategoryResponseDto(1L, "Capoeira", true);

        when(categoryService.update(any(CategoryUpdateRequestDto.class)))
                .thenReturn(responseDto);

        mockMvc.perform(put(BASE_URL)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Capoeira"))
                .andExpect(jsonPath("$.active").value(true));

        verify(categoryService).update(any(CategoryUpdateRequestDto.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDeactivateCategorySuccessfully() throws Exception {
        var responseDto = getMockCategoryResponseDto(1L, "Capoeira", false);

        when(categoryService.deactivateCategory(1L)).thenReturn(responseDto);

        mockMvc.perform(delete(BASE_URL + "/{id}", 1L)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Capoeira"))
                .andExpect(jsonPath("$.active").value(false));

        verify(categoryService).deactivateCategory(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReactivateCategorySuccessfully() throws Exception {
        var requestDto = new CategoryReactivateRequestDto("Capoeira");
        var responseDto = getMockCategoryResponseDto(1L, "Capoeira", true);

        when(categoryService.reactivateCategory("Capoeira"))
                .thenReturn(responseDto);

        mockMvc.perform(patch(BASE_URL + "/reactivate")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Capoeira"))
                .andExpect(jsonPath("$.active").value(true));

        verify(categoryService).reactivateCategory("Capoeira");
    }

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    void shouldAllowSuperAdminToAccessCategoryEndpoints() throws Exception {
        var responseDto = getMockCategoryResponseDto(1L, "Capoeira", true);

        when(categoryService.findById(1L)).thenReturn(responseDto);

        mockMvc.perform(get(BASE_URL + "/id/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Capoeira"))
                .andExpect(jsonPath("$.active").value(true));

        verify(categoryService).findById(1L);
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void shouldReturnForbiddenWhenUserDoesNotHaveAdminRole() throws Exception {
        mockMvc.perform(get(BASE_URL + "/all"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(categoryService);
    }

    @Test
    void shouldReturnUnauthorizedWhenUserIsNotAuthenticated() throws Exception {
        mockMvc.perform(get(BASE_URL + "/all"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(categoryService);
    }

    private CategoryResponseDto getMockCategoryResponseDto(
            Long id,
            String name,
            Boolean active
    ) {
        return CategoryResponseDto.builder()
                .id(id)
                .name(name)
                .active(active)
                .build();
    }
}
