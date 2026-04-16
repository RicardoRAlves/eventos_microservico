package com.br.capoeira.eventos.notification.mapper;

import com.br.capoeira.eventos.notification.model.Event;
import com.br.capoeira.eventos.notification.model.EventDocument;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class EventMapper {

    private static final ZoneId ZONE_ID = ZoneId.of("America/Sao_Paulo");

    public static EventDocument toDocument(Event event) {
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