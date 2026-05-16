package com.br.capoeira.eventos.notification.mapper;

import com.br.capoeira.eventos.notification.dto.EventSaleItemDocument;
import com.br.capoeira.eventos.notification.dto.EventSaleItemRequestDto;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public final class EventSaleItemMapper {

    private static final ZoneId ZONE_ID = ZoneId.of("America/Sao_Paulo");

    private EventSaleItemMapper() {
    }

    public static EventSaleItemDocument toDocument(
            EventSaleItemRequestDto eventSaleItem
    ) {

        EventSaleItemDocument doc = new EventSaleItemDocument();

        doc.setTransactionId(eventSaleItem.getTransactionId());
        doc.setEventTransactionId(eventSaleItem.getEventTransactionId());
        doc.setDescription(eventSaleItem.getDescription());
        doc.setQuantity(eventSaleItem.getQuantity());
        doc.setValue(eventSaleItem.getValue());
        doc.setActive(eventSaleItem.getActive());
        doc.setCreatedAt(toDate(eventSaleItem.getCreatedAt()));
        doc.setUpdatedAt(toDate(eventSaleItem.getUpdatedAt()));

        return doc;
    }

    private static Date toDate(LocalDateTime localDateTime) {

        if (localDateTime == null) {
            return null;
        }

        return Date.from(
                localDateTime.atZone(ZONE_ID).toInstant()
        );
    }
}
