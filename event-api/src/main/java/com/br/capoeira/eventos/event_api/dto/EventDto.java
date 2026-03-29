package com.br.capoeira.eventos.event_api.dto;

import com.br.capoeira.eventos.event_api.enums.TypeContact;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventDto {
    private String transactionId;
    private String title;
    private String description;
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime dateStarted;
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime dateFinished;
    private String address;
    private TypeContact typeContact;
    private String contact;
    private String image;
}
