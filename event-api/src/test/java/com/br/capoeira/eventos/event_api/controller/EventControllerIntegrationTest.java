package com.br.capoeira.eventos.event_api.controller;

import com.br.capoeira.eventos.event_api.dto.EventDto;
import com.br.capoeira.eventos.event_api.mapper.EventoMapper;
import com.br.capoeira.eventos.event_api.service.EventService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.format.DateTimeFormatter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static unit.com.br.capoeira.eventos.event_api.utils.MockUtils.getMockEvent;

@WebMvcTest(EventController.class)
@AutoConfigureMockMvc(addFilters = false)
public class EventControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EventoMapper eventoMapper;

    @MockitoBean
    private EventService service;

    @Test
    public void whenSendAGetRequestShouldReturnOk() throws Exception {
        willDoNothing().given(service).findAllEvents();
        mockMvc.perform(
                get("/api/v1/evento/all")
                     .contentType(MediaType.APPLICATION_JSON))
                     .andExpect(status().isOk())
                .andExpect(content().string("Added request to queue"));
    }

    @Test
    public void whenSendAPostShouldReturnCreated() throws Exception {
        var dataTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        var eventDto = new EventDto();
        var event = getMockEvent();
        given( eventoMapper.eventoDtoToEvento(any(EventDto.class))).willReturn(event);
        mockMvc.perform(
                        post("/api/v1/evento/create")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(eventDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("title").value(event.getTitle()))
                .andExpect(jsonPath("description").value(event.getDescription()))
                .andExpect(jsonPath("contact").value(event.getContact()))
                .andExpect(jsonPath("typeContact").value(event.getTypeContact().toString()))
                .andExpect(jsonPath("dateStarted").value(event.getDateStarted().format(dataTimeFormatter)))
                .andExpect(jsonPath("dateFinished").value(event.getDateFinished().format(dataTimeFormatter)))
                .andExpect(jsonPath("address").value(event.getAddress()))
                .andExpect(jsonPath("image").value(event.getImage()))
                .andExpect(jsonPath("active").value(event.getActive()));
    }

    @Test
    public void whenSendAUpdatePhotoShouldReturnOK() throws Exception {
        var photoPath = "https://my-bucket.s3.amazonaws.com/photo.jpg";
        MockMultipartFile file = new MockMultipartFile(
                "image",
                "foto.jpg",
                "image/jpeg",
                "conteúdo da imagem".getBytes()
        );
        given(service.updatePhoto(any())).willReturn(photoPath);
        mockMvc.perform(
                        multipart("/api/v1/evento/upload")
                                .file(file))
                .andExpect(status().isOk())
                .andExpect(content().string(photoPath));
    }

    @Test
    public void whenSendAPutShouldReturnCreated() throws Exception {
        var dataTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        var event = getMockEvent();
        event.setTransactionId("ahahsahsash");
        given( eventoMapper.eventoDtoToEvento(any(EventDto.class))).willReturn(event);
        mockMvc.perform(
                        put("/api/v1/evento")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("transactionId").value(event.getTransactionId()))
                .andExpect(jsonPath("title").value(event.getTitle()))
                .andExpect(jsonPath("description").value(event.getDescription()))
                .andExpect(jsonPath("contact").value(event.getContact()))
                .andExpect(jsonPath("typeContact").value(event.getTypeContact().toString()))
                .andExpect(jsonPath("dateStarted").value(event.getDateStarted().format(dataTimeFormatter)))
                .andExpect(jsonPath("dateFinished").value(event.getDateFinished().format(dataTimeFormatter)))
                .andExpect(jsonPath("address").value(event.getAddress()))
                .andExpect(jsonPath("image").value(event.getImage()))
                .andExpect(jsonPath("active").value(event.getActive()));
    }
}
