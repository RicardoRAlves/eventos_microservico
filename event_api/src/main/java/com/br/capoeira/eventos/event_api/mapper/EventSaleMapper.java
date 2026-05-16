package com.br.capoeira.eventos.event_api.mapper;

import com.br.capoeira.eventos.event_api.dto.EventSaleItemCreateRequestDto;
import com.br.capoeira.eventos.event_api.dto.EventSaleItemResponseDto;
import com.br.capoeira.eventos.event_api.dto.EventSaleItemUpdateRequestDto;
import com.br.capoeira.eventos.event_api.model.EventSaleItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EventSaleMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    EventSaleItem createRequestDtoToEventSale(
            EventSaleItemCreateRequestDto input
    );

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateRequestDtoToEventSale(
            EventSaleItemUpdateRequestDto input,
            @MappingTarget EventSaleItem eventSale
    );

    EventSaleItemResponseDto eventSaleToResponseDto(
            EventSaleItem eventSaleItem
    );

    List<EventSaleItemResponseDto> eventSaleListToResponseDtoList(
            List<EventSaleItem> eventSaleItems
    );
}
