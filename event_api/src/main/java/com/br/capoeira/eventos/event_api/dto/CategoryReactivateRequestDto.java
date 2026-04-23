package com.br.capoeira.eventos.event_api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryReactivateRequestDto {
    @NotBlank(message = "Category name must be informed")
    private String name;
}
