package com.culturaconecta.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EventSummaryRequestDto(
        @NotBlank(message = "Description is required")
        @Size(max = 5000, message = "Description must not exceed 5000 characters")
        String description

) {}

