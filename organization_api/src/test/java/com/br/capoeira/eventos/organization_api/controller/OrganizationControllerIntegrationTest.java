package com.br.capoeira.eventos.organization_api.controller;

import com.br.capoeira.eventos.organization_api.dto.OrganizationDto;
import com.br.capoeira.eventos.organization_api.dto.OrganizationUnitDto;
import com.br.capoeira.eventos.organization_api.exception.GlobalHandlerException;
import com.br.capoeira.eventos.organization_api.exception.ValidationException;
import com.br.capoeira.eventos.organization_api.model.Organization;
import com.br.capoeira.eventos.organization_api.model.OrganizationUnit;
import com.br.capoeira.eventos.organization_api.service.OrganizationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static utils.MockUtils.*;

@WebMvcTest(OrganizationController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalHandlerException.class)
public class OrganizationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrganizationService service;

    @Test
    void shouldFindOrganizationById() throws Exception {
        var organization = getMockOrganization();

        when(service.findById(1L)).thenReturn(Optional.of(organization));

        mockMvc.perform(get("/api/v2/organizacao/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(organization.getId()))
                .andExpect(jsonPath("$.name").value(organization.getName()))
                .andExpect(jsonPath("$.slug").value(organization.getSlug()))
                .andExpect(jsonPath("$.description").value(organization.getDescription()))
                .andExpect(jsonPath("$.logoUrl").value(organization.getLogoUrl()))
                .andExpect(jsonPath("$.active").value(organization.getActive()));

        verify(service).findById(1L);
    }

    @Test
    void shouldReturnNotFoundWhenOrganizationByIdDoesNotExist() throws Exception {
        when(service.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v2/organizacao/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(service).findById(1L);
    }

    @Test
    void shouldFindOrganizationUnitById() throws Exception {
        var responseDto = getMockOrganizationUnitResponseDto();

        when(service.findUnitById(1L)).thenReturn(responseDto);

        mockMvc.perform(get("/api/v2/organizacao/unit/1")
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
    void shouldReturnNotFoundWhenOrganizationUnitByIdDoesNotExist() throws Exception {
        when(service.findUnitById(1L))
                .thenThrow(new ValidationException("Organization Unit not found"));

        mockMvc.perform(get("/api/v2/organizacao/unit/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Organization Unit not found"))
                .andExpect(jsonPath("$.path").value("/api/v2/organizacao/unit/1"));

        verify(service).findUnitById(1L);
    }

    @Test
    void shouldFindAllOrganizationUnitsByOrganizationId() throws Exception {
        var responseDto = getMockOrganizationUnitResponseDto();

        when(service.findAllByOrganizationId(1L)).thenReturn(List.of(responseDto));

        mockMvc.perform(get("/api/v2/organizacao/unit/all/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(responseDto.getId()))
                .andExpect(jsonPath("$[0].organizationId").value(responseDto.getOrganizationId()))
                .andExpect(jsonPath("$[0].name").value(responseDto.getName()))
                .andExpect(jsonPath("$[0].slug").value(responseDto.getSlug()))
                .andExpect(jsonPath("$[0].description").value(responseDto.getDescription()))
                .andExpect(jsonPath("$[0].city").value(responseDto.getCity()))
                .andExpect(jsonPath("$[0].state").value(responseDto.getState()))
                .andExpect(jsonPath("$[0].country").value(responseDto.getCountry()))
                .andExpect(jsonPath("$[0].address").value(responseDto.getAddress()))
                .andExpect(jsonPath("$[0].contactPhone").value(responseDto.getContactPhone()))
                .andExpect(jsonPath("$[0].contactEmail").value(responseDto.getContactEmail()))
                .andExpect(jsonPath("$[0].active").value(responseDto.getActive()));

        verify(service).findAllByOrganizationId(1L);
    }

    @Test
    void shouldCreateOrganization() throws Exception {
        var dto = getMockOrganizationDto();
        var organization = getMockOrganization();

        when(service.create(any(OrganizationDto.class))).thenReturn(organization);

        mockMvc.perform(post("/api/v2/organizacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(organization.getId()))
                .andExpect(jsonPath("$.name").value(organization.getName()))
                .andExpect(jsonPath("$.slug").value(organization.getSlug()))
                .andExpect(jsonPath("$.description").value(organization.getDescription()))
                .andExpect(jsonPath("$.logoUrl").value(organization.getLogoUrl()))
                .andExpect(jsonPath("$.active").value(organization.getActive()));

        verify(service).create(any(OrganizationDto.class));
    }

    @Test
    void shouldCreateOrganizationUnit() throws Exception {
        var dto = getMockOrganizationUnitDto();
        var organizationUnit = getMockOrganizationUnit();
        var responseDto = getMockOrganizationUnitResponseDto();

        when(service.create(any(OrganizationUnitDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/v2/organizacao/unit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
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
    void shouldUpdateOrganization() throws Exception {
        var organization = getMockOrganization();

        when(service.update(any(Organization.class))).thenReturn(organization);

        mockMvc.perform(put("/api/v2/organizacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(organization)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(organization.getId()))
                .andExpect(jsonPath("$.name").value(organization.getName()));

        verify(service).update(any(Organization.class));
    }

    @Test
    void shouldUpdateOrganizationUnit() throws Exception {
        var organizationUnit = getMockOrganizationUnit();
        var responseDto = getMockOrganizationUnitResponseDto();

        when(service.update(any(OrganizationUnit.class))).thenReturn(responseDto);

        mockMvc.perform(put("/api/v2/organizacao/unit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(organizationUnit)))
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

        verify(service).update(any(OrganizationUnit.class));
    }
}
