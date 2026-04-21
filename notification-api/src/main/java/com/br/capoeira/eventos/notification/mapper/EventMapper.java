package com.br.capoeira.eventos.notification.mapper;

import com.br.capoeira.eventos.notification.dto.EventDocument;
import com.br.capoeira.eventos.notification.dto.EventRequestDto;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public final class EventMapper {

    private static final ZoneId ZONE_ID = ZoneId.of("America/Sao_Paulo");

    private EventMapper() {
    }

    public static EventDocument toDocument(EventRequestDto event) {
        EventDocument doc = new EventDocument();
        doc.setId(event.getId());
        doc.setTransactionId(event.getTransactionId());
        doc.setTitle(event.getTitle());
        doc.setDescription(event.getDescription());
        doc.setDateStarted(toDate(event.getDateStarted()));
        doc.setDateFinished(toDate(event.getDateFinished()));
        doc.setLocationName(event.getLocationName());
        doc.setAddress(event.getAddress());
        doc.setTypeContact(event.getTypeContact());
        doc.setContact(event.getContact());
        doc.setImage(event.getImage());
        doc.setCategoryName(event.getCategoryName());
        doc.setScope(event.getScope());
        doc.setOrganizationId(event.getOrganizationId());
        doc.setOrganizationUnitId(event.getOrganizationUnitId());
        doc.setActive(event.getActive());
        return doc;
    }

    private static Date toDate(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }

        return Date.from(localDateTime.atZone(ZONE_ID).toInstant());
    }
}