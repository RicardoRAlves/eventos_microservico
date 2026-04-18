package com.br.capoeira.eventos.organization_api.dto;

import jakarta.validation.constraints.Email;
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
public class MainUnitDto {

    @NotBlank(message = "Unit name must be informed")
    private String name;

    @NotBlank(message = "Unit slug must be informed")
    private String slug;

    @NotBlank(message = "Unit description must be informed")
    private String description;

    @NotBlank(message = "City must be informed")
    private String city;

    @NotBlank(message = "State must be informed")
    private String state;

    @NotBlank(message = "Country must be informed")
    private String country;

    @NotBlank(message = "Address must be informed")
    private String address;

    @NotBlank(message = "Contact phone must be informed")
    private String contactPhone;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Contact email must be informed")
    private String contactEmail;

    @NotNull(message = "Unit active status must be informed")
    private Boolean active;
}
