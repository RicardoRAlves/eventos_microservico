package com.br.capoeira.eventos.event_api.dto;

import com.br.capoeira.eventos.event_api.enums.EventScope;
import com.br.capoeira.eventos.event_api.enums.TypeContact;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventCreateRequestDto {

    @NotBlank(message = "Event title must be informed")
    private String title;

    @NotBlank(message = "Event description must be informed")
    private String description;

    @NotNull(message = "Event start date must be informed")
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime dateStarted;

    @NotNull(message = "Event finish date must be informed")
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime dateFinished;

    @NotBlank(message = "Location name must be informed")
    private String locationName;

    @NotBlank(message = "Address must be informed")
    private String address;

    @NotNull(message = "Contact type must be informed")
    private TypeContact typeContact;

    @NotBlank(message = "Contact must be informed")
    private String contact;

    @NotBlank(message = "Image must be informed")
    private String image;

    @NotBlank(message = "Category name must be informed")
    private String categoryName;

    @NotNull(message = "Event scope must be informed")
    private EventScope scope;

    private Long organizationId;

    private Long organizationUnitId;
}
