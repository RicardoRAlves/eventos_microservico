package com.br.capoeira.eventos.event_api.controller;

import com.br.capoeira.eventos.event_api.config.SecurityConfig;
import com.br.capoeira.eventos.event_api.config.exception.GlobalHandlerException;
import com.br.capoeira.eventos.event_api.dto.EventCreateRequestDto;
import com.br.capoeira.eventos.event_api.dto.EventResponseDto;
import com.br.capoeira.eventos.event_api.dto.EventUpdateRequestDto;
import com.br.capoeira.eventos.event_api.enums.EventScope;
import com.br.capoeira.eventos.event_api.enums.TypeContact;
import com.br.capoeira.eventos.event_api.service.EventService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EventController.class)
@AutoConfigureMockMvc
@Import({GlobalHandlerException.class, SecurityConfig.class})
class EventControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EventService service;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    void whenSendAGetRequestShouldReturnOk() throws Exception {
        willDoNothing().given(service).findAllEvents();

        mockMvc.perform(get("/api/v1/evento/all")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Request sent to queue"));

        verify(service).findAllEvents();
    }

    @Test
    void whenSendAGetRequestWithoutAuthenticationShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/evento/all")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        verify(service, never()).findAllEvents();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void whenSendAPostShouldReturnCreated() throws Exception {
        var requestDto = getMockEventCreateRequestDto();
        var responseDto = getMockEventResponseDto();

        given(service.sendingNewEventToProcessor(any(EventCreateRequestDto.class))).willReturn(responseDto);

        mockMvc.perform(post("/api/v1/evento/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionId").value(responseDto.getTransactionId()))
                .andExpect(jsonPath("$.title").value(responseDto.getTitle()))
                .andExpect(jsonPath("$.description").value(responseDto.getDescription()))
                .andExpect(jsonPath("$.contact").value(responseDto.getContact()))
                .andExpect(jsonPath("$.typeContact").value(responseDto.getTypeContact().toString()))
                .andExpect(jsonPath("$.dateStarted").value("10-05-2026 19:00:00"))
                .andExpect(jsonPath("$.dateFinished").value("10-05-2026 22:00:00"))
                .andExpect(jsonPath("$.address").value(responseDto.getAddress()))
                .andExpect(jsonPath("$.image").value(responseDto.getImage()))
                .andExpect(jsonPath("$.categoryName").value(responseDto.getCategoryName()))
                .andExpect(jsonPath("$.scope").value(responseDto.getScope().toString()))
                .andExpect(jsonPath("$.organizationId").value(responseDto.getOrganizationId()))
                .andExpect(jsonPath("$.organizationUnitId").value(responseDto.getOrganizationUnitId()))
                .andExpect(jsonPath("$.active").value(responseDto.getActive()));

        verify(service).sendingNewEventToProcessor(any(EventCreateRequestDto.class));
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void whenClientSendAPostShouldReturnForbidden() throws Exception {
        var requestDto = getMockEventCreateRequestDto();

        mockMvc.perform(post("/api/v1/evento/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isForbidden());

        verify(service, never()).sendingNewEventToProcessor(any(EventCreateRequestDto.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void whenSendAUpdatePhotoShouldReturnOk() throws Exception {
        var photoPath = "https://my-bucket.s3.amazonaws.com/photo.jpg";

        var file = new MockMultipartFile(
                "image",
                "foto.jpg",
                "image/jpeg",
                "conteudo da imagem".getBytes()
        );

        given(service.updatePhoto(any()))
                .willReturn(photoPath);

        mockMvc.perform(multipart("/api/v1/evento/upload")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageUrl").value(photoPath));

        verify(service).updatePhoto(any());
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void whenClientSendAUpdatePhotoShouldReturnForbidden() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "image",
                "foto.jpg",
                "image/jpeg",
                "conteudo da imagem".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/evento/upload")
                        .file(file))
                .andExpect(status().isForbidden());

        verify(service, never()).updatePhoto(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void whenSendAPutShouldReturnOk() throws Exception {
        var requestDto = getMockEventUpdateRequestDto();
        var responseDto = getMockEventResponseDto();

        given(service.updateEvent(any(EventUpdateRequestDto.class))).willReturn(responseDto);

        mockMvc.perform(put("/api/v1/evento")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value(responseDto.getTransactionId()))
                .andExpect(jsonPath("$.title").value(responseDto.getTitle()))
                .andExpect(jsonPath("$.description").value(responseDto.getDescription()))
                .andExpect(jsonPath("$.contact").value(responseDto.getContact()))
                .andExpect(jsonPath("$.typeContact").value(responseDto.getTypeContact().toString()))
                .andExpect(jsonPath("$.dateStarted").value("10-05-2026 19:00:00"))
                .andExpect(jsonPath("$.dateFinished").value("10-05-2026 22:00:00"))
                .andExpect(jsonPath("$.address").value(responseDto.getAddress()))
                .andExpect(jsonPath("$.image").value(responseDto.getImage()))
                .andExpect(jsonPath("$.categoryName").value(responseDto.getCategoryName()))
                .andExpect(jsonPath("$.scope").value(responseDto.getScope().toString()))
                .andExpect(jsonPath("$.organizationId").value(responseDto.getOrganizationId()))
                .andExpect(jsonPath("$.organizationUnitId").value(responseDto.getOrganizationUnitId()))
                .andExpect(jsonPath("$.active").value(responseDto.getActive()));

        verify(service).updateEvent(any(EventUpdateRequestDto.class));
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void whenClientSendAPutShouldReturnForbidden() throws Exception {
        var requestDto = getMockEventUpdateRequestDto();

        mockMvc.perform(put("/api/v1/evento")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isForbidden());

        verify(service, never()).updateEvent(any(EventUpdateRequestDto.class));
    }

    private EventCreateRequestDto getMockEventCreateRequestDto() {
        return new EventCreateRequestDto(
                "Batizado Bonfim",
                "Evento anual de capoeira",
                LocalDateTime.of(2026, 5, 10, 19, 0, 0),
                LocalDateTime.of(2026, 5, 10, 22, 0, 0),
                "Academia Central",
                "Rua das Flores, 100",
                TypeContact.WHATSAPP,
                "11999999999",
                "https://image.com/evento.png",
                "Capoeira",
                EventScope.ORGANIZATION_UNIT,
                1L,   // userId
                1L,   // organizationId
                10L   // organizationUnitId
        );
    }

    private EventUpdateRequestDto getMockEventUpdateRequestDto() {
        return new EventUpdateRequestDto(
                "1715536800000_uuid",
                "Batizado Bonfim",
                "Evento anual de capoeira",
                LocalDateTime.of(2026, 5, 10, 19, 0, 0),
                LocalDateTime.of(2026, 5, 10, 22, 0, 0),
                "Academia Central",
                "Rua das Flores, 100",
                TypeContact.WHATSAPP,
                "11999999999",
                "https://image.com/evento.png",
                "Capoeira",
                EventScope.ORGANIZATION_UNIT,
                1L,
                10L,
                1L
        );
    }

    private EventResponseDto getMockEventResponseDto() {
        return EventResponseDto.builder()
                .transactionId("1715536800000_uuid")
                .title("Batizado Bonfim")
                .description("Evento anual de capoeira")
                .dateStarted(LocalDateTime.of(2026, 5, 10, 19, 0, 0))
                .dateFinished(LocalDateTime.of(2026, 5, 10, 22, 0, 0))
                .locationName("Academia Central")
                .address("Rua das Flores, 100")
                .typeContact(TypeContact.WHATSAPP)
                .contact("11999999999")
                .image("https://image.com/evento.png")
                .categoryName("Capoeira")
                .scope(EventScope.ORGANIZATION_UNIT)
                .organizationId(1L)
                .organizationUnitId(10L)
                .active(true)
                .build();
    }
}