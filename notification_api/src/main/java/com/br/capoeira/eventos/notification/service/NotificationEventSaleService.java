package com.br.capoeira.eventos.notification.service;

import com.br.capoeira.eventos.notification.dto.EventSaleDeleteRequestDto;
import com.br.capoeira.eventos.notification.dto.EventSaleItemRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationEventSaleService {

    private final FirebaseEventSaleItemService firebaseEventSaleItemService;

    public void createNewEventSale(EventSaleItemRequestDto dto) {
        log.info(
                "Notify android that event sale item: {} has been created",
                dto
        );

        firebaseEventSaleItemService.addEventSaleItem(dto);
    }

    public void updateNewEventSale(EventSaleItemRequestDto dto) {
        log.info(
                "Notify android that event sale item: {} has been updated",
                dto
        );

        firebaseEventSaleItemService.updateEventSaleItem(dto);
    }

    public void deleteNewEventSale(EventSaleDeleteRequestDto dto) {
        log.info(
                "Notify android that event sale item: {} has been deleted",
                dto
        );

        firebaseEventSaleItemService.deleteEventSaleItem(
                dto.getTransactionId()
        );
    }
}
