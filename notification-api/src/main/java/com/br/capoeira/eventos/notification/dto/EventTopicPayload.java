package com.br.capoeira.eventos.notification.dto;

import com.br.capoeira.eventos.notification.dto.enums.EventScope;

public interface EventTopicPayload {
    EventScope getScope();
    Long getOrganizationId();
    Long getOrganizationUnitId();
}
