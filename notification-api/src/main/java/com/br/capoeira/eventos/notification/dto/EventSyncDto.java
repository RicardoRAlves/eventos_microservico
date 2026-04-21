package com.br.capoeira.eventos.notification.dto;

import com.br.capoeira.eventos.notification.dto.enums.EventScope;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventSyncDto {
    private EventScope scope;
    private Long organizationId;
    private Long organizationUnitId;
    private List<EventRequestDto> events;
}
