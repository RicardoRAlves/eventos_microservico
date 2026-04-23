package com.br.capoeira.eventos.event_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryUpdateRequestDto {
    @NotNull(message = "Category id must be informed")
    private Long id;
    @NotBlank(message = "Category name must be informed")
    private String name;
}
