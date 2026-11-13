package com.culturaconecta.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EventDescriptionRequestDto {

    @NotBlank(message = "Event title is required")
    private String title;

    @NotBlank(message = "Event category is required")
    private String category;

    private String location;

    private String targetAudience;

    private String additionalInformation;
}

