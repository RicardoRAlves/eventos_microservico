package com.br.capoeira.eventos.event_api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventSaleItemCreateRequestDto {

    @NotBlank(message = "Event transaction Id must be informed")
    private String eventTransactionId;

    @NotBlank(message = "Description must be informed")
    private String description;

    @NotNull(message = "Quantity must be informed")
    @PositiveOrZero(message = "Quantity must be zero or greater")
    private Integer quantity;

    @NotNull(message = "Value must be informed")
    @DecimalMin(value = "0.00", message = "Value must be zero or greater")
    private BigDecimal value;
}
