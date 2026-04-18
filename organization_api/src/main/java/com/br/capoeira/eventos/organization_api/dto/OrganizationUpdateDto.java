package com.br.capoeira.eventos.organization_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationUpdateDto {
    @NotNull(message = "Organization id must be informed")
    private Long id;

    @NotBlank(message = "Organization name must be informed")
    private String name;

    @NotBlank(message = "Organization slug must be informed")
    private String slug;

    @NotBlank(message = "Organization description must be informed")
    private String description;

    @NotBlank(message = "Organization logo url must be informed")
    private String logoUrl;

    @NotNull(message = "Organization active status must be informed")
    private Boolean active;
}
