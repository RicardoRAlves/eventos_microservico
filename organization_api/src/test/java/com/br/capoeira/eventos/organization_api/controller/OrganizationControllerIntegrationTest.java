package com.br.capoeira.eventos.organization_api.controller;

import com.br.capoeira.eventos.organization_api.config.SecurityConfig;
import com.br.capoeira.eventos.organization_api.config.exception.GlobalHandlerException;
import com.br.capoeira.eventos.organization_api.dto.*;
import com.br.capoeira.eventos.organization_api.service.OrganizationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static utils.MockUtils.*;

@WebMvcTest(OrganizationController.class)
@AutoConfigureMockMvc
@Import({GlobalHandlerException.class, SecurityConfig.class})
class OrganizationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrganizationService service;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    @WithMockUser
    void shouldFindOrganizationById() throws Exception {
        var responseDto = getMockOrganizationResponseDto();

        when(service.findOrganizationById(1L)).thenReturn(responseDto);

        mockMvc.perform(get("/api/v1/organizacao/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(responseDto.getId()))
                .andExpect(jsonPath("$.name").value(responseDto.getName()))
                .andExpect(jsonPath("$.slug").value(responseDto.getSlug()))
                .andExpect(jsonPath("$.description").value(responseDto.getDescription()))
                .andExpect(jsonPath("$.logoUrl").value(responseDto.getLogoUrl()))
                .andExpect(jsonPath("$.active").value(responseDto.getActive()));

        verify(service).findOrganizationById(1L);
    }

    @Test
    void shouldReturnUnauthorizedWhenFindOrganizationByIdWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/organizacao/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        verify(service, never()).findOrganizationById(anyLong());
    }

    @Test
    @WithMockUser
    void shouldFindOrganizationUnitById() throws Exception {
        var responseDto = getMockOrganizationUnitResponseDto();

        when(service.findUnitById(1L)).thenReturn(responseDto);

        mockMvc.perform(get("/api/v1/organizacao/unit/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(responseDto.getId()))
                .andExpect(jsonPath("$.organizationId").value(responseDto.getOrganizationId()))
                .andExpect(jsonPath("$.name").value(responseDto.getName()))
                .andExpect(jsonPath("$.slug").value(responseDto.getSlug()))
                .andExpect(jsonPath("$.description").value(responseDto.getDescription()))
                .andExpect(jsonPath("$.city").value(responseDto.getCity()))
                .andExpect(jsonPath("$.state").value(responseDto.getState()))
                .andExpect(jsonPath("$.country").value(responseDto.getCountry()))
                .andExpect(jsonPath("$.address").value(responseDto.getAddress()))
                .andExpect(jsonPath("$.contactPhone").value(responseDto.getContactPhone()))
                .andExpect(jsonPath("$.contactEmail").value(responseDto.getContactEmail()))
                .andExpect(jsonPath("$.active").value(responseDto.getActive()));

        verify(service).findUnitById(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldFindAllOrganizationUnitsByOrganizationId() throws Exception {
        var responseDto = getMockOrganizationUnitResponseDto();

        var response = new PageResponseDto<>(
                List.of(responseDto),
                0,
                10,
                1L,
                1,
                true
        );

        when(service.findAllByOrganizationId(1L, 0, 10)).thenReturn(response);

        mockMvc.perform(get("/api/v1/organizacao/unit/all/1")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(responseDto.getId()))
                .andExpect(jsonPath("$.content[0].organizationId").value(responseDto.getOrganizationId()))
                .andExpect(jsonPath("$.content[0].name").value(responseDto.getName()))
                .andExpect(jsonPath("$.content[0].slug").value(responseDto.getSlug()))
                .andExpect(jsonPath("$.content[0].description").value(responseDto.getDescription()))
                .andExpect(jsonPath("$.content[0].city").value(responseDto.getCity()))
                .andExpect(jsonPath("$.content[0].state").value(responseDto.getState()))
                .andExpect(jsonPath("$.content[0].country").value(responseDto.getCountry()))
                .andExpect(jsonPath("$.content[0].address").value(responseDto.getAddress()))
                .andExpect(jsonPath("$.content[0].contactPhone").value(responseDto.getContactPhone()))
                .andExpect(jsonPath("$.content[0].contactEmail").value(responseDto.getContactEmail()))
                .andExpect(jsonPath("$.content[0].active").value(responseDto.getActive()))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.last").value(true));

        verify(service).findAllByOrganizationId(1L, 0, 10);
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void shouldReturnForbiddenWhenFindAllOrganizationUnitsByOrganizationIdWithClientRole() throws Exception {
        mockMvc.perform(get("/api/v1/organizacao/unit/all/1")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verifyNoInteractions(service);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldCreateOrganization() throws Exception {
        var requestDto = getMockOrganizationCreateRequestDto();
        var responseDto = getMockOrganizationResponseDto();

        when(service.createWithMainUnit(any(OrganizationCreateRequestDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/organizacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(responseDto.getId()))
                .andExpect(jsonPath("$.name").value(responseDto.getName()))
                .andExpect(jsonPath("$.slug").value(responseDto.getSlug()))
                .andExpect(jsonPath("$.description").value(responseDto.getDescription()))
                .andExpect(jsonPath("$.logoUrl").value(responseDto.getLogoUrl()))
                .andExpect(jsonPath("$.active").value(responseDto.getActive()));

        verify(service).createWithMainUnit(any(OrganizationCreateRequestDto.class));
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void shouldReturnForbiddenWhenCreateOrganizationWithClientRole() throws Exception {
        var requestDto = getMockOrganizationCreateRequestDto();

        mockMvc.perform(post("/api/v1/organizacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isForbidden());

        verify(service, never()).createWithMainUnit(any(OrganizationCreateRequestDto.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldCreateOrganizationUnit() throws Exception {
        var requestDto = getMockOrganizationUnitDto();
        var responseDto = getMockOrganizationUnitResponseDto();

        when(service.create(any(OrganizationUnitDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/organizacao/unit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(responseDto.getId()))
                .andExpect(jsonPath("$.organizationId").value(responseDto.getOrganizationId()))
                .andExpect(jsonPath("$.name").value(responseDto.getName()))
                .andExpect(jsonPath("$.slug").value(responseDto.getSlug()))
                .andExpect(jsonPath("$.description").value(responseDto.getDescription()))
                .andExpect(jsonPath("$.city").value(responseDto.getCity()))
                .andExpect(jsonPath("$.state").value(responseDto.getState()))
                .andExpect(jsonPath("$.country").value(responseDto.getCountry()))
                .andExpect(jsonPath("$.address").value(responseDto.getAddress()))
                .andExpect(jsonPath("$.contactPhone").value(responseDto.getContactPhone()))
                .andExpect(jsonPath("$.contactEmail").value(responseDto.getContactEmail()))
                .andExpect(jsonPath("$.active").value(responseDto.getActive()));

        verify(service).create(any(OrganizationUnitDto.class));
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void shouldReturnForbiddenWhenCreateOrganizationUnitWithClientRole() throws Exception {
        var requestDto = getMockOrganizationUnitDto();

        mockMvc.perform(post("/api/v1/organizacao/unit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isForbidden());

        verify(service, never()).create(any(OrganizationUnitDto.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateOrganization() throws Exception {
        var requestDto = getMockOrganizationUpdateDto();
        var responseDto = getMockOrganizationResponseDto();

        when(service.update(any(OrganizationUpdateDto.class))).thenReturn(responseDto);

        mockMvc.perform(put("/api/v1/organizacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(responseDto.getId()))
                .andExpect(jsonPath("$.name").value(responseDto.getName()))
                .andExpect(jsonPath("$.slug").value(responseDto.getSlug()))
                .andExpect(jsonPath("$.description").value(responseDto.getDescription()))
                .andExpect(jsonPath("$.logoUrl").value(responseDto.getLogoUrl()))
                .andExpect(jsonPath("$.active").value(responseDto.getActive()));

        verify(service).update(any(OrganizationUpdateDto.class));
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void shouldReturnForbiddenWhenUpdateOrganizationWithClientRole() throws Exception {
        var requestDto = getMockOrganizationUpdateDto();

        mockMvc.perform(put("/api/v1/organizacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isForbidden());

        verify(service, never()).update(any(OrganizationUpdateDto.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateOrganizationUnit() throws Exception {
        var requestDto = getMockOrganizationUnitUpdateDto();
        var responseDto = getMockOrganizationUnitResponseDto();

        when(service.update(any(OrganizationUnitUpdateDto.class))).thenReturn(responseDto);

        mockMvc.perform(put("/api/v1/organizacao/unit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(responseDto.getId()))
                .andExpect(jsonPath("$.organizationId").value(responseDto.getOrganizationId()))
                .andExpect(jsonPath("$.name").value(responseDto.getName()))
                .andExpect(jsonPath("$.slug").value(responseDto.getSlug()))
                .andExpect(jsonPath("$.description").value(responseDto.getDescription()))
                .andExpect(jsonPath("$.city").value(responseDto.getCity()))
                .andExpect(jsonPath("$.state").value(responseDto.getState()))
                .andExpect(jsonPath("$.country").value(responseDto.getCountry()))
                .andExpect(jsonPath("$.address").value(responseDto.getAddress()))
                .andExpect(jsonPath("$.contactPhone").value(responseDto.getContactPhone()))
                .andExpect(jsonPath("$.contactEmail").value(responseDto.getContactEmail()))
                .andExpect(jsonPath("$.active").value(responseDto.getActive()));

        verify(service).update(any(OrganizationUnitUpdateDto.class));
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void shouldReturnForbiddenWhenUpdateOrganizationUnitWithClientRole() throws Exception {
        var requestDto = getMockOrganizationUnitUpdateDto();

        mockMvc.perform(put("/api/v1/organizacao/unit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isForbidden());

        verify(service, never()).update(any(OrganizationUnitUpdateDto.class));
    }
}
