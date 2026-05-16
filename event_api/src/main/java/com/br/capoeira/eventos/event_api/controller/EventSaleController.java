package com.br.capoeira.eventos.event_api.controller;

import com.br.capoeira.eventos.event_api.dto.EventSaleItemCreateRequestDto;
import com.br.capoeira.eventos.event_api.dto.EventSaleItemDeleteRequestDto;
import com.br.capoeira.eventos.event_api.dto.EventSaleItemResponseDto;
import com.br.capoeira.eventos.event_api.dto.EventSaleItemUpdateRequestDto;
import com.br.capoeira.eventos.event_api.service.EventSaleService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api/v1/evento/sale/item")
public class EventSaleController {

    private final EventSaleService service;

    @PostMapping
    public ResponseEntity<EventSaleItemResponseDto>
    createSaleItem(
            @Valid @RequestBody EventSaleItemCreateRequestDto dto
    ) {

        log.info("New event sale item creation requested");

        var response = service.createItemSale(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PutMapping
    public ResponseEntity<EventSaleItemResponseDto>
    updateSaleItem(
            @Valid @RequestBody EventSaleItemUpdateRequestDto dto
    ) {

        log.info(
                "Update requested for event sale item {}",
                dto.getTransactionId()
        );

        var response = service.updateItemSale(dto);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    public ResponseEntity<Void>
    deleteSaleItem(
            @Valid @RequestBody EventSaleItemDeleteRequestDto dto
    ) {

        log.info(
                "Delete requested for event sale item {}",
                dto.getTransactionId()
        );

        service.deleteItemSale(dto);

        return ResponseEntity.noContent().build();
    }
}
