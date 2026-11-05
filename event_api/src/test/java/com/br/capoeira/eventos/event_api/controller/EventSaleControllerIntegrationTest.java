package com.br.capoeira.eventos.event_api.controller;

import com.br.capoeira.eventos.event_api.dto.EventSaleItemCreateRequestDto;
import com.br.capoeira.eventos.event_api.dto.EventSaleItemDeleteRequestDto;
import com.br.capoeira.eventos.event_api.dto.EventSaleItemResponseDto;
import com.br.capoeira.eventos.event_api.dto.EventSaleItemUpdateRequestDto;
import com.br.capoeira.eventos.event_api.service.EventSaleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EventSaleController.class)
@AutoConfigureMockMvc
class EventSaleControllerIntegrationTest {

    private static final String BASE_URL = "/api/v1/evento/sale/item";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EventSaleService service;

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldCreateSaleItemSuccessfully() throws Exception {

        var request = getCreateRequest();
        var response = getResponseDto();

        when(service.createItemSale(any(EventSaleItemCreateRequestDto.class)))
                .thenReturn(response);

        mockMvc.perform(post(BASE_URL)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionId").value("tx-123"))
                .andExpect(jsonPath("$.eventTransactionId").value("event-123"))
                .andExpect(jsonPath("$.description").value("Camiseta"))
                .andExpect(jsonPath("$.quantity").value(10))
                .andExpect(jsonPath("$.active").value(true));

        verify(service).createItemSale(any(EventSaleItemCreateRequestDto.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateSaleItemSuccessfully() throws Exception {

        var request = getUpdateRequest();
        var response = getResponseDto();

        when(service.updateItemSale(any(EventSaleItemUpdateRequestDto.class)))
                .thenReturn(response);

        mockMvc.perform(put(BASE_URL)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value("tx-123"))
                .andExpect(jsonPath("$.eventTransactionId").value("event-123"))
                .andExpect(jsonPath("$.description").value("Camiseta"))
                .andExpect(jsonPath("$.quantity").value(10))
                .andExpect(jsonPath("$.active").value(true));

        verify(service).updateItemSale(any(EventSaleItemUpdateRequestDto.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteSaleItemSuccessfully() throws Exception {

        var request = getDeleteRequest();

        doNothing().when(service)
                .deleteItemSale(any(EventSaleItemDeleteRequestDto.class));

        mockMvc.perform(delete(BASE_URL)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(service)
                .deleteItemSale(any(EventSaleItemDeleteRequestDto.class));
    }

    @Test
    void shouldReturnUnauthorizedWhenUserIsNotAuthenticated() throws Exception {

        mockMvc.perform(post(BASE_URL)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(getCreateRequest())))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(service);
    }

    private EventSaleItemCreateRequestDto getCreateRequest() {
        var dto = new EventSaleItemCreateRequestDto();
        dto.setEventTransactionId("event-123");
        dto.setDescription("Camiseta");
        dto.setQuantity(10);
        dto.setValue(BigDecimal.valueOf(50));
        return dto;
    }

    private EventSaleItemUpdateRequestDto getUpdateRequest() {
        var dto = new EventSaleItemUpdateRequestDto();
        dto.setTransactionId("tx-123");
        dto.setEventTransactionId("event-123");
        dto.setDescription("Camiseta");
        dto.setQuantity(10);
        dto.setValue(BigDecimal.valueOf(50));
        dto.setActive(true);
        return dto;
    }

    private EventSaleItemDeleteRequestDto getDeleteRequest() {
        var dto = new EventSaleItemDeleteRequestDto();
        dto.setTransactionId("tx-123");
        return dto;
    }

    private EventSaleItemResponseDto getResponseDto() {
        var dto = new EventSaleItemResponseDto();
        dto.setTransactionId("tx-123");
        dto.setEventTransactionId("event-123");
        dto.setDescription("Camiseta");
        dto.setQuantity(10);
        dto.setValue(BigDecimal.valueOf(50));
        dto.setActive(true);
        return dto;
    }
}
