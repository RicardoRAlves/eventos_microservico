package com.br.capoeira.eventos.event_api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventSaleItemDeleteRequestDto {

    @NotBlank(message = "Transaction Id must be informed")
    private String transactionId;
}
